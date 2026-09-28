package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CmsSyncResult(
    val success: Boolean,
    val message: String,
    val httpStatus: Int = 200,
    val endpoint: String = "",
    val fetchedItems: List<ContentItemEntity> = emptyList(),
    val publishedEntryId: String = "",
    val publishedAssetId: String = "",
    val publishedVersion: Int = 1
)

/**
 * Contentful Content Management API (CMA) & Content Delivery API (CDA) Client.
 *
 * Implements the official Contentful CMA REST specification:
 * - Binary Image Upload: `POST https://upload.contentful.com/spaces/{spaceId}/uploads`
 * - Asset Creation & Processing: `POST /spaces/{spaceId}/environments/{env}/assets` + `/process` + `/published`
 * - Rich Media Entry Upsert (Images, Descriptions, Download Links):
 *   `POST/PUT https://api.contentful.com/spaces/{spaceId}/environments/{env}/entries`
 * - Entry Activation for Live Website Updates:
 *   `PUT https://api.contentful.com/spaces/{spaceId}/environments/{env}/entries/{entryId}/published`
 * - Entry Unpublish:
 *   `DELETE https://api.contentful.com/spaces/{spaceId}/environments/{env}/entries/{entryId}/published`
 */
object HeadlessCmsClient {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val octetStreamMediaType = "application/octet-stream".toMediaType()
    private val contentfulCmaMediaType = "application/vnd.contentful.management.v1+json".toMediaType()

    suspend fun fetchFromCms(
        config: SiteConfigEntity,
        existingCategories: List<CategoryEntity>
    ): CmsSyncResult = withContext(Dispatchers.IO) {
        try {
            if (config.cmsProvider == CmsProviderType.STRAPI) {
                fetchFromStrapi(config, existingCategories)
            } else {
                fetchFromContentful(config, existingCategories)
            }
        } catch (e: Exception) {
            CmsSyncResult(
                success = false,
                httpStatus = 500,
                endpoint = "https://api.contentful.com",
                message = "Contentful Network Error: ${e.localizedMessage ?: "Connection failed"}"
            )
        }
    }

    suspend fun publishItemToCms(
        context: Context,
        config: SiteConfigEntity,
        item: ContentItemEntity
    ): CmsSyncResult = withContext(Dispatchers.IO) {
        try {
            if (config.cmsProvider == CmsProviderType.STRAPI) {
                publishToStrapi(config, item)
            } else {
                publishToContentfulCma(context, config, item)
            }
        } catch (e: Exception) {
            CmsSyncResult(
                success = false,
                httpStatus = 500,
                endpoint = "https://api.contentful.com",
                message = "Contentful CMA Error: ${e.localizedMessage ?: "Request failed"}"
            )
        }
    }

    suspend fun unpublishEntryInContentful(
        config: SiteConfigEntity,
        item: ContentItemEntity
    ): CmsSyncResult = withContext(Dispatchers.IO) {
        val spaceId = config.spaceIdFromSecrets
        val env = config.contentfulEnvironment.trim().ifBlank { "master" }
        val cmaToken = config.cmaTokenFromSecrets
        val entryId = item.cmsEntryId.trim()

        val endpoint = "https://api.contentful.com/spaces/${spaceId.ifBlank { "{space_id}" }}/environments/$env/entries/${entryId.ifBlank { "{entry_id}" }}/published"

        if (spaceId.isBlank() || cmaToken.isBlank()) {
            return@withContext CmsSyncResult(
                success = false,
                httpStatus = 401,
                endpoint = endpoint,
                message = "Configure CONTENTFUL_SPACE_ID and CONTENTFUL_MANAGEMENT_TOKEN in the AI Studio Secrets panel to unpublish live entries."
            )
        }
        if (entryId.isBlank()) {
            return@withContext CmsSyncResult(
                success = false,
                httpStatus = 400,
                endpoint = endpoint,
                message = "Entry has not been published to Contentful yet."
            )
        }

        try {
            val request = Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer $cmaToken")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext CmsSyncResult(
                        success = false,
                        httpStatus = response.code,
                        endpoint = endpoint,
                        message = "Contentful CMA Unpublish HTTP ${response.code}: ${extractErrorMessage(bodyStr)}"
                    )
                }
                val json = JSONObject(bodyStr)
                val newVersion = json.optJSONObject("sys")?.optInt("version", item.cmsVersion + 1) ?: (item.cmsVersion + 1)
                CmsSyncResult(
                    success = true,
                    httpStatus = response.code,
                    endpoint = endpoint,
                    message = "Unpublished '${item.title}' from live website via Contentful CMA.",
                    publishedEntryId = entryId,
                    publishedVersion = newVersion
                )
            }
        } catch (e: Exception) {
            CmsSyncResult(
                success = false,
                httpStatus = 500,
                endpoint = endpoint,
                message = "Unpublish failed: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Full Contentful CMA Rich Media Pipeline:
     * 1. If the user selected a local image URI (`content://`), uploads the binary bytes to
     *    `upload.contentful.com`, creates a Contentful Asset, processes it, and publishes it.
     * 2. Creates or updates the Contentful Entry with rich fields:
     *    `title`, `slug`, `contentType`, `categoryName`, `summary` (description),
     *    `markdownBody`, `mediaSource`, `downloadUrl`, `downloadLabel`, `downloadFileSize`,
     *    `versionTag`, `photoCaption`, `liveDemoUrl`, `repoUrl`, and `media` Asset link.
     * 3. Publishes the Entry via `PUT /entries/{entryId}/published` for immediate live website updates.
     */
    private suspend fun publishToContentfulCma(
        context: Context,
        config: SiteConfigEntity,
        item: ContentItemEntity
    ): CmsSyncResult {
        val spaceId = config.spaceIdFromSecrets
        val env = config.contentfulEnvironment.trim().ifBlank { "master" }
        val cmaToken = config.cmaTokenFromSecrets
        val locale = config.contentfulLocale.trim().ifBlank { "en-US" }
        val contentTypeId = config.contentfulContentType.trim().ifBlank { "portfolioItem" }

        val entriesBaseUrl = "https://api.contentful.com/spaces/${spaceId.ifBlank { "{space_id}" }}/environments/$env/entries"

        if (spaceId.isBlank() || cmaToken.isBlank()) {
            return CmsSyncResult(
                success = false,
                httpStatus = 401,
                endpoint = entriesBaseUrl,
                message = "Missing Contentful CMA credentials. Add CONTENTFUL_SPACE_ID and CONTENTFUL_MANAGEMENT_TOKEN in the AI Studio Secrets panel."
            )
        }

        // Step 1: Optional Binary Image Asset Upload if mediaSource is a local content:// URI
        var uploadedAssetId = item.cmsAssetId
        var resolvedMediaUrl = item.mediaSource
        if (item.mediaSource.startsWith("content://")) {
            val assetResult = uploadLocalUriAsContentfulAsset(
                context = context,
                spaceId = spaceId,
                env = env,
                locale = locale,
                cmaToken = cmaToken,
                title = item.title,
                description = item.summary,
                uriString = item.mediaSource
            )
            if (assetResult != null) {
                uploadedAssetId = assetResult.first
                if (assetResult.second.isNotBlank()) {
                    resolvedMediaUrl = assetResult.second
                }
            }
        }

        fun localized(value: Any): JSONObject = JSONObject().put(locale, value)

        val fieldsObj = JSONObject().apply {
            put("title", localized(item.title))
            put("slug", localized(item.slug))
            put("contentType", localized(item.contentType))
            put("categoryName", localized(item.categoryName))
            put("summary", localized(item.summary))
            put("description", localized(item.summary))
            put("markdownBody", localized(item.markdownBody))
            put("mediaSource", localized(resolvedMediaUrl))
            put("downloadUrl", localized(item.downloadUrl))
            put("downloadLabel", localized(item.downloadLabel))
            put("downloadFileSize", localized(item.downloadFileSize))
            put("versionTag", localized(item.versionTag))
            put("photoCaption", localized(item.photoCaption))
            put("photoLocation", localized(item.photoLocation))
            put("exifCamera", localized(item.exifCamera))
            put("techStackCsv", localized(item.techStackCsv))
            put("liveDemoUrl", localized(item.liveDemoUrl))
            put("repoUrl", localized(item.repoUrl))
            put("readingTimeMinutes", localized(item.readingTimeMinutes))
            put("isFeatured", localized(item.isFeatured))

            if (uploadedAssetId.isNotBlank()) {
                val assetLink = JSONObject().put(
                    "sys",
                    JSONObject()
                        .put("type", "Link")
                        .put("linkType", "Asset")
                        .put("id", uploadedAssetId)
                )
                put("media", localized(assetLink))
            }
        }

        val payload = JSONObject().put("fields", fieldsObj).toString()

        // Step 2: Create or Update Entry in Contentful CMA
        val hasExistingCmaId = item.cmsEntryId.isNotBlank() && !item.cmsEntryId.startsWith("cf-entry-")
        val requestUrl = if (hasExistingCmaId) {
            "$entriesBaseUrl/${item.cmsEntryId}"
        } else {
            entriesBaseUrl
        }

        val reqBuilder = Request.Builder()
            .url(requestUrl)
            .header("Authorization", "Bearer $cmaToken")
            .header("X-Contentful-Content-Type", contentTypeId)

        val entryRequest = if (hasExistingCmaId) {
            reqBuilder
                .header("X-Contentful-Version", item.cmsVersion.toString())
                .put(payload.toRequestBody(contentfulCmaMediaType))
                .build()
        } else {
            reqBuilder
                .post(payload.toRequestBody(contentfulCmaMediaType))
                .build()
        }

        httpClient.newCall(entryRequest).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return CmsSyncResult(
                    success = false,
                    httpStatus = response.code,
                    endpoint = requestUrl,
                    message = "Contentful CMA Entry HTTP ${response.code}: ${extractErrorMessage(bodyStr)}"
                )
            }

            val createdJson = JSONObject(bodyStr)
            val sys = createdJson.optJSONObject("sys")
            val entryId = sys?.optString("id").orEmpty()
            var version = sys?.optInt("version", 1) ?: 1

            // Step 3: Publish Entry immediately so live website updates right away
            if (entryId.isNotBlank()) {
                val publishUrl = "$entriesBaseUrl/$entryId/published"
                val pubReq = Request.Builder()
                    .url(publishUrl)
                    .header("Authorization", "Bearer $cmaToken")
                    .header("X-Contentful-Version", version.toString())
                    .put("".toRequestBody(null))
                    .build()

                httpClient.newCall(pubReq).execute().use { pubRes ->
                    if (pubRes.isSuccessful) {
                        val pubBody = pubRes.body?.string().orEmpty()
                        val pubJson = runCatching { JSONObject(pubBody) }.getOrNull()
                        version = pubJson?.optJSONObject("sys")?.optInt("version", version + 1) ?: (version + 1)
                    }
                }
            }

            return CmsSyncResult(
                success = true,
                httpStatus = response.code,
                endpoint = requestUrl,
                message = "Published '${item.title}' (v$version) with download link & media to Contentful CMA (ID: $entryId).",
                publishedEntryId = entryId,
                publishedAssetId = uploadedAssetId,
                publishedVersion = version
            )
        }
    }

    /**
     * Uploads raw bytes from an Android `content://` URI to `upload.contentful.com`,
     * links it to a new Contentful Asset, processes it, and publishes it.
     * Returns `Pair(assetId, publishedCdnUrl)` on success, or `null` if skipped.
     */
    private suspend fun uploadLocalUriAsContentfulAsset(
        context: Context,
        spaceId: String,
        env: String,
        locale: String,
        cmaToken: String,
        title: String,
        description: String,
        uriString: String
    ): Pair<String, String>? {
        return try {
            val uri = Uri.parse(uriString)
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null

            // 1. Upload binary stream to upload.contentful.com
            val uploadUrl = "https://upload.contentful.com/spaces/$spaceId/uploads"
            val uploadReq = Request.Builder()
                .url(uploadUrl)
                .header("Authorization", "Bearer $cmaToken")
                .post(bytes.toRequestBody(octetStreamMediaType))
                .build()

            val uploadId = httpClient.newCall(uploadReq).execute().use { res ->
                if (!res.isSuccessful) return null
                val json = JSONObject(res.body?.string().orEmpty())
                json.optJSONObject("sys")?.optString("id").orEmpty()
            }
            if (uploadId.isBlank()) return null

            // 2. Create Asset entry referencing the uploadId
            val fileName = "${slugify(title)}.jpg"
            val fileObj = JSONObject()
                .put("contentType", mimeType)
                .put("fileName", fileName)
                .put(
                    "uploadFrom",
                    JSONObject().put(
                        "sys",
                        JSONObject()
                            .put("type", "Link")
                            .put("linkType", "Upload")
                            .put("id", uploadId)
                    )
                )

            val assetFields = JSONObject()
                .put("title", JSONObject().put(locale, title))
                .put("description", JSONObject().put(locale, description))
                .put("file", JSONObject().put(locale, fileObj))

            val createAssetUrl = "https://api.contentful.com/spaces/$spaceId/environments/$env/assets"
            val createAssetReq = Request.Builder()
                .url(createAssetUrl)
                .header("Authorization", "Bearer $cmaToken")
                .post(JSONObject().put("fields", assetFields).toString().toRequestBody(contentfulCmaMediaType))
                .build()

            var assetId = ""
            var assetVersion = 1
            httpClient.newCall(createAssetReq).execute().use { res ->
                if (!res.isSuccessful) return null
                val json = JSONObject(res.body?.string().orEmpty())
                val sys = json.optJSONObject("sys")
                assetId = sys?.optString("id").orEmpty()
                assetVersion = sys?.optInt("version", 1) ?: 1
            }
            if (assetId.isBlank()) return null

            // 3. Process the Asset file
            val processUrl = "$createAssetUrl/$assetId/files/$locale/process"
            val processReq = Request.Builder()
                .url(processUrl)
                .header("Authorization", "Bearer $cmaToken")
                .header("X-Contentful-Version", assetVersion.toString())
                .put("".toRequestBody(null))
                .build()
            httpClient.newCall(processReq).execute().close()

            delay(700)

            // 4. Publish the Asset
            val publishAssetUrl = "$createAssetUrl/$assetId/published"
            val publishReq = Request.Builder()
                .url(publishAssetUrl)
                .header("Authorization", "Bearer $cmaToken")
                .header("X-Contentful-Version", (assetVersion + 1).toString())
                .put("".toRequestBody(null))
                .build()

            var cdnUrl = ""
            httpClient.newCall(publishReq).execute().use { res ->
                if (res.isSuccessful) {
                    val json = JSONObject(res.body?.string().orEmpty())
                    val rawUrl = json.optJSONObject("fields")
                        ?.optJSONObject("file")
                        ?.optJSONObject(locale)
                        ?.optString("url")
                        .orEmpty()
                    cdnUrl = if (rawUrl.startsWith("//")) "https:$rawUrl" else rawUrl
                }
            }
            Pair(assetId, cdnUrl)
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchFromContentful(
        config: SiteConfigEntity,
        existingCategories: List<CategoryEntity>
    ): CmsSyncResult {
        val spaceId = config.spaceIdFromSecrets
        val env = config.contentfulEnvironment.trim().ifBlank { "master" }
        val cdaToken = config.cdaTokenFromSecrets
        val cmaToken = config.cmaTokenFromSecrets
        val locale = config.contentfulLocale.trim().ifBlank { "en-US" }

        // Prefer CDA for fast CDN reads, fallback to CMA if only CMA token is provided
        val useCmaEndpoint = cdaToken.isBlank() && cmaToken.isNotBlank()
        val tokenToUse = if (useCmaEndpoint) cmaToken else cdaToken
        val host = if (useCmaEndpoint) "https://api.contentful.com" else "https://cdn.contentful.com"
        val url = "$host/spaces/${spaceId.ifBlank { "{space_id}" }}/environments/$env/entries?limit=100"

        if (spaceId.isBlank() || tokenToUse.isBlank()) {
            return CmsSyncResult(
                success = false,
                httpStatus = 401,
                endpoint = url,
                message = "Configure CONTENTFUL_SPACE_ID and CONTENTFUL_MANAGEMENT_TOKEN (or CONTENTFUL_DELIVERY_TOKEN) in the AI Studio Secrets panel."
            )
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $tokenToUse")
            .header("Accept", "application/json")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return CmsSyncResult(
                    success = false,
                    httpStatus = response.code,
                    endpoint = url,
                    message = "Contentful API HTTP ${response.code}: ${extractErrorMessage(bodyStr)}"
                )
            }

            val root = JSONObject(bodyStr)
            val assetMap = mutableMapOf<String, String>()
            val includes = root.optJSONObject("includes")
            val assets = includes?.optJSONArray("Asset") ?: JSONArray()
            for (i in 0 until assets.length()) {
                val assetObj = assets.optJSONObject(i) ?: continue
                val assetId = assetObj.optJSONObject("sys")?.optString("id").orEmpty()
                val fileObj = assetObj.optJSONObject("fields")?.optJSONObject("file")
                val fileUrl = fileObj?.optString("url")
                    ?.ifBlank { fileObj.optJSONObject(locale)?.optString("url").orEmpty() }
                    .orEmpty()
                if (assetId.isNotBlank() && fileUrl.isNotBlank()) {
                    assetMap[assetId] = if (fileUrl.startsWith("//")) "https:$fileUrl" else fileUrl
                }
            }

            val itemsArray = root.optJSONArray("items") ?: JSONArray()
            val fetchedItems = mutableListOf<ContentItemEntity>()
            val defaultCat = existingCategories.firstOrNull()

            fun readField(fields: JSONObject, key: String, fallback: String = ""): String {
                val raw = fields.opt(key) ?: return fallback
                return when (raw) {
                    is JSONObject -> raw.optString(locale, fallback)
                    is String -> raw
                    else -> raw.toString()
                }
            }

            fun readBoolean(fields: JSONObject, key: String): Boolean {
                val raw = fields.opt(key) ?: return false
                return when (raw) {
                    is JSONObject -> raw.optBoolean(locale, false)
                    is Boolean -> raw
                    else -> false
                }
            }

            fun readInt(fields: JSONObject, key: String, defaultVal: Int = 5): Int {
                val raw = fields.opt(key) ?: return defaultVal
                return when (raw) {
                    is JSONObject -> raw.optInt(locale, defaultVal)
                    is Number -> raw.toInt()
                    else -> defaultVal
                }
            }

            for (i in 0 until itemsArray.length()) {
                val entry = itemsArray.optJSONObject(i) ?: continue
                val sys = entry.optJSONObject("sys")
                val entryId = sys?.optString("id").orEmpty()
                val version = sys?.optInt("version", 1) ?: 1
                val fields = entry.optJSONObject("fields") ?: continue

                val title = readField(fields, "title").ifBlank { continue }
                val rawType = readField(fields, "contentType", ContentType.BLOG).uppercase()
                val contentType = when {
                    rawType.contains("PROJ") -> ContentType.PROJECT
                    rawType.contains("PHOT") -> ContentType.PHOTO
                    else -> ContentType.BLOG
                }
                val categoryName = readField(fields, "categoryName", defaultCat?.name ?: "React & Headless CMS")
                val matchedCat = existingCategories.firstOrNull {
                    it.name.equals(categoryName, ignoreCase = true)
                } ?: defaultCat

                var mediaSource = readField(fields, "mediaSource")
                if (mediaSource.isBlank()) {
                    val mediaField = fields.optJSONObject("media")
                    val mediaAssetId = mediaField?.optJSONObject("sys")?.optString("id")
                        ?: mediaField?.optJSONObject(locale)?.optJSONObject("sys")?.optString("id")
                        ?: ""
                    mediaSource = assetMap[mediaAssetId] ?: defaultDrawableForType(contentType)
                }

                val summary = readField(fields, "summary").ifBlank { readField(fields, "description") }

                fetchedItems.add(
                    ContentItemEntity(
                        id = 0L,
                        title = title,
                        slug = readField(fields, "slug", slugify(title)),
                        contentType = contentType,
                        categoryId = matchedCat?.id ?: 1L,
                        categoryName = categoryName,
                        summary = summary,
                        markdownBody = readField(fields, "markdownBody", readField(fields, "body", summary)),
                        mediaSource = mediaSource,
                        photoCaption = readField(fields, "photoCaption"),
                        photoLocation = readField(fields, "photoLocation"),
                        exifCamera = readField(fields, "exifCamera"),
                        downloadUrl = readField(fields, "downloadUrl"),
                        downloadLabel = readField(fields, "downloadLabel", "Download Package"),
                        downloadFileSize = readField(fields, "downloadFileSize"),
                        versionTag = readField(fields, "versionTag", "v1.0"),
                        techStackCsv = readField(fields, "techStackCsv"),
                        liveDemoUrl = readField(fields, "liveDemoUrl"),
                        repoUrl = readField(fields, "repoUrl"),
                        readingTimeMinutes = readInt(fields, "readingTimeMinutes", 5),
                        isFeatured = readBoolean(fields, "isFeatured"),
                        isPublished = true,
                        createdAtEpoch = System.currentTimeMillis(),
                        updatedAtEpoch = System.currentTimeMillis(),
                        cmsEntryId = entryId,
                        cmsVersion = version,
                        cmsStatus = CmaEntryStatus.PUBLISHED,
                        cmsProvider = CmsProviderType.CONTENTFUL,
                        cmsSyncedAt = System.currentTimeMillis()
                    )
                )
            }

            return CmsSyncResult(
                success = true,
                httpStatus = response.code,
                endpoint = url,
                message = "Synced ${fetchedItems.size} live entries from Contentful space '$spaceId' ($env).",
                fetchedItems = fetchedItems
            )
        }
    }

    private fun fetchFromStrapi(
        config: SiteConfigEntity,
        existingCategories: List<CategoryEntity>
    ): CmsSyncResult {
        val baseUrl = config.strapiBaseUrl.trim().trimEnd('/')
        val url = "$baseUrl/api/portfolio-items?populate=*&pagination[pageSize]=100&sort=updatedAt:desc"
        if (!baseUrl.startsWith("http")) {
            return CmsSyncResult(
                success = false,
                httpStatus = 400,
                endpoint = url,
                message = "Valid Strapi Base URL is required."
            )
        }

        val reqBuilder = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .get()

        val strapiToken = config.strapiTokenFromSecrets
        if (strapiToken.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer $strapiToken")
        }

        httpClient.newCall(reqBuilder.build()).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return CmsSyncResult(
                    success = false,
                    httpStatus = response.code,
                    endpoint = url,
                    message = "Strapi API HTTP ${response.code}: ${extractErrorMessage(bodyStr)}"
                )
            }

            val root = JSONObject(bodyStr)
            val dataArray = root.optJSONArray("data") ?: JSONArray()
            val fetchedItems = mutableListOf<ContentItemEntity>()
            val defaultCat = existingCategories.firstOrNull()

            for (i in 0 until dataArray.length()) {
                val rawItem = dataArray.optJSONObject(i) ?: continue
                val entryId = rawItem.optString("documentId", rawItem.optString("id", ""))
                val attrs = rawItem.optJSONObject("attributes") ?: rawItem

                val title = attrs.optString("title", "").ifBlank { continue }
                val rawType = attrs.optString("contentType", ContentType.BLOG).uppercase()
                val contentType = when {
                    rawType.contains("PROJ") -> ContentType.PROJECT
                    rawType.contains("PHOT") -> ContentType.PHOTO
                    else -> ContentType.BLOG
                }
                val categoryName = attrs.optString("categoryName", defaultCat?.name ?: "React & Headless CMS")
                val matchedCat = existingCategories.firstOrNull {
                    it.name.equals(categoryName, ignoreCase = true)
                } ?: defaultCat

                fetchedItems.add(
                    ContentItemEntity(
                        id = 0L,
                        title = title,
                        slug = attrs.optString("slug", slugify(title)),
                        contentType = contentType,
                        categoryId = matchedCat?.id ?: 1L,
                        categoryName = categoryName,
                        summary = attrs.optString("summary", ""),
                        markdownBody = attrs.optString("markdownBody", attrs.optString("body", "")),
                        mediaSource = attrs.optString("mediaSource", defaultDrawableForType(contentType)),
                        photoCaption = attrs.optString("photoCaption", ""),
                        photoLocation = attrs.optString("photoLocation", ""),
                        exifCamera = attrs.optString("exifCamera", ""),
                        downloadUrl = attrs.optString("downloadUrl", ""),
                        downloadLabel = attrs.optString("downloadLabel", "Download Asset"),
                        downloadFileSize = attrs.optString("downloadFileSize", ""),
                        versionTag = attrs.optString("versionTag", "v1.0"),
                        techStackCsv = attrs.optString("techStackCsv", ""),
                        liveDemoUrl = attrs.optString("liveDemoUrl", ""),
                        repoUrl = attrs.optString("repoUrl", ""),
                        readingTimeMinutes = attrs.optInt("readingTimeMinutes", 5),
                        isFeatured = attrs.optBoolean("isFeatured", false),
                        isPublished = true,
                        createdAtEpoch = System.currentTimeMillis(),
                        updatedAtEpoch = System.currentTimeMillis(),
                        cmsEntryId = entryId,
                        cmsStatus = CmaEntryStatus.PUBLISHED,
                        cmsProvider = CmsProviderType.STRAPI,
                        cmsSyncedAt = System.currentTimeMillis()
                    )
                )
            }

            return CmsSyncResult(
                success = true,
                httpStatus = response.code,
                endpoint = url,
                message = "Synced ${fetchedItems.size} live entries from Strapi ($baseUrl).",
                fetchedItems = fetchedItems
            )
        }
    }

    private fun publishToStrapi(
        config: SiteConfigEntity,
        item: ContentItemEntity
    ): CmsSyncResult {
        val baseUrl = config.strapiBaseUrl.trim().trimEnd('/')
        val apiToken = config.strapiTokenFromSecrets
        val url = "$baseUrl/api/portfolio-items"

        if (!baseUrl.startsWith("http") || apiToken.isBlank()) {
            return CmsSyncResult(
                success = false,
                httpStatus = 401,
                endpoint = url,
                message = "Configure STRAPI_BASE_URL and STRAPI_API_TOKEN in the AI Studio Secrets panel."
            )
        }

        val dataObj = JSONObject().apply {
            put("title", item.title)
            put("slug", item.slug)
            put("contentType", item.contentType)
            put("categoryName", item.categoryName)
            put("summary", item.summary)
            put("markdownBody", item.markdownBody)
            put("mediaSource", item.mediaSource)
            put("downloadUrl", item.downloadUrl)
            put("downloadLabel", item.downloadLabel)
            put("downloadFileSize", item.downloadFileSize)
            put("versionTag", item.versionTag)
            put("photoCaption", item.photoCaption)
            put("photoLocation", item.photoLocation)
            put("exifCamera", item.exifCamera)
            put("techStackCsv", item.techStackCsv)
            put("liveDemoUrl", item.liveDemoUrl)
            put("repoUrl", item.repoUrl)
            put("readingTimeMinutes", item.readingTimeMinutes)
            put("isFeatured", item.isFeatured)
        }

        val payload = JSONObject().put("data", dataObj).toString()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiToken")
            .header("Content-Type", "application/json")
            .post(payload.toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return CmsSyncResult(
                    success = false,
                    httpStatus = response.code,
                    endpoint = url,
                    message = "Strapi POST HTTP ${response.code}: ${extractErrorMessage(bodyStr)}"
                )
            }

            val root = JSONObject(bodyStr)
            val data = root.optJSONObject("data")
            val entryId = data?.optString("documentId", data.optString("id", "")).orEmpty()

            return CmsSyncResult(
                success = true,
                httpStatus = response.code,
                endpoint = url,
                message = "Published '${item.title}' to Strapi CMS (ID: $entryId).",
                publishedEntryId = entryId
            )
        }
    }

    private fun extractErrorMessage(body: String): String {
        if (body.isBlank()) return "Empty response from server"
        return try {
            val json = JSONObject(body)
            json.optString("message").ifBlank {
                json.optJSONObject("error")?.optString("message").orEmpty()
            }.ifBlank {
                body.take(120)
            }
        } catch (_: Exception) {
            body.take(120)
        }
    }

    private fun defaultDrawableForType(contentType: String): String = when (contentType) {
        ContentType.PROJECT -> "drawable:img_project_cloud"
        ContentType.PHOTO -> "drawable:img_photo_dhaka"
        else -> "drawable:img_hero_banner"
    }

    private fun slugify(input: String): String =
        input.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "post-${System.currentTimeMillis()}" }
}
