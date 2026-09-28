package com.example.data

import kotlinx.coroutines.Dispatchers
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
    val fetchedItems: List<ContentItemEntity> = emptyList(),
    val fetchedCategories: List<CategoryEntity> = emptyList(),
    val publishedEntryId: String = ""
)

/**
 * Real Headless CMS HTTP Client supporting both:
 * 1. Contentful (Content Delivery API + Content Management API)
 * 2. Strapi v4/v5 (REST API `/api/portfolio-items` & `/api/categories`)
 *
 * Allows Rasel Dev BD to fetch and publish blog posts, project showcases, and photos
 * with captions dynamically without changing code or rebuilding the React bundle.
 */
object HeadlessCmsClient {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
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
                message = "CMS Network Error (${config.cmsProvider}): ${e.localizedMessage ?: "Connection failed"}"
            )
        }
    }

    suspend fun publishItemToCms(
        config: SiteConfigEntity,
        item: ContentItemEntity
    ): CmsSyncResult = withContext(Dispatchers.IO) {
        try {
            if (config.cmsProvider == CmsProviderType.STRAPI) {
                publishToStrapi(config, item)
            } else {
                publishToContentful(config, item)
            }
        } catch (e: Exception) {
            CmsSyncResult(
                success = false,
                message = "Publish Error (${config.cmsProvider}): ${e.localizedMessage ?: "Request failed"}"
            )
        }
    }

    private fun fetchFromContentful(
        config: SiteConfigEntity,
        existingCategories: List<CategoryEntity>
    ): CmsSyncResult {
        val spaceId = config.contentfulSpaceId.trim()
        val env = config.contentfulEnvironment.trim().ifBlank { "master" }
        val token = config.contentfulDeliveryToken.trim()

        if (spaceId.isBlank() || token.isBlank()) {
            return CmsSyncResult(
                success = false,
                message = "Contentful Space ID and Delivery API Token are required. Configure them in CMS Studio or the AI Studio Secrets panel."
            )
        }

        val url = "https://cdn.contentful.com/spaces/$spaceId/environments/$env/entries?include=2&limit=100"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return CmsSyncResult(
                    success = false,
                    message = "Contentful API returned HTTP ${response.code}: ${extractErrorMessage(bodyStr)}"
                )
            }

            val root = JSONObject(bodyStr)
            val assetMap = mutableMapOf<String, String>()
            val includes = root.optJSONObject("includes")
            val assets = includes?.optJSONArray("Asset") ?: JSONArray()
            for (i in 0 until assets.length()) {
                val assetObj = assets.optJSONObject(i) ?: continue
                val assetId = assetObj.optJSONObject("sys")?.optString("id").orEmpty()
                val fileUrl = assetObj.optJSONObject("fields")
                    ?.optJSONObject("file")
                    ?.optString("url")
                    .orEmpty()
                if (assetId.isNotBlank() && fileUrl.isNotBlank()) {
                    assetMap[assetId] = if (fileUrl.startsWith("//")) "https:$fileUrl" else fileUrl
                }
            }

            val itemsArray = root.optJSONArray("items") ?: JSONArray()
            val fetchedItems = mutableListOf<ContentItemEntity>()
            val defaultCat = existingCategories.firstOrNull()

            for (i in 0 until itemsArray.length()) {
                val entry = itemsArray.optJSONObject(i) ?: continue
                val sys = entry.optJSONObject("sys")
                val entryId = sys?.optString("id").orEmpty()
                val fields = entry.optJSONObject("fields") ?: continue

                val title = fields.optString("title", "").ifBlank { continue }
                val rawType = fields.optString("contentType", ContentType.BLOG).uppercase()
                val contentType = when {
                    rawType.contains("PROJ") -> ContentType.PROJECT
                    rawType.contains("PHOT") -> ContentType.PHOTO
                    else -> ContentType.BLOG
                }
                val categoryName = fields.optString("categoryName", defaultCat?.name ?: "Cloud & DevOps")
                val matchedCat = existingCategories.firstOrNull {
                    it.name.equals(categoryName, ignoreCase = true)
                } ?: defaultCat

                // Resolve mediaSource from either direct string field or linked Contentful Asset
                var mediaSource = fields.optString("mediaSource", "")
                if (mediaSource.isBlank()) {
                    val mediaAssetId = fields.optJSONObject("media")
                        ?.optJSONObject("sys")
                        ?.optString("id")
                        .orEmpty()
                    mediaSource = assetMap[mediaAssetId] ?: defaultDrawableForType(contentType)
                }

                fetchedItems.add(
                    ContentItemEntity(
                        id = 0L,
                        title = title,
                        slug = fields.optString("slug", slugify(title)),
                        contentType = contentType,
                        categoryId = matchedCat?.id ?: 1L,
                        categoryName = categoryName,
                        summary = fields.optString("summary", ""),
                        markdownBody = fields.optString("markdownBody", fields.optString("body", "")),
                        mediaSource = mediaSource,
                        photoCaption = fields.optString("photoCaption", ""),
                        photoLocation = fields.optString("photoLocation", ""),
                        exifCamera = fields.optString("exifCamera", ""),
                        techStackCsv = fields.optString("techStackCsv", ""),
                        liveDemoUrl = fields.optString("liveDemoUrl", ""),
                        repoUrl = fields.optString("repoUrl", ""),
                        readingTimeMinutes = fields.optInt("readingTimeMinutes", 5),
                        isFeatured = fields.optBoolean("isFeatured", false),
                        isPublished = true,
                        createdAtEpoch = System.currentTimeMillis(),
                        updatedAtEpoch = System.currentTimeMillis(),
                        cmsEntryId = entryId,
                        cmsProvider = CmsProviderType.CONTENTFUL,
                        cmsSyncedAt = System.currentTimeMillis()
                    )
                )
            }

            return CmsSyncResult(
                success = true,
                message = "Synced ${fetchedItems.size} live entries from Contentful space '$spaceId' ($env).",
                fetchedItems = fetchedItems
            )
        }
    }

    private fun publishToContentful(
        config: SiteConfigEntity,
        item: ContentItemEntity
    ): CmsSyncResult {
        val spaceId = config.contentfulSpaceId.trim()
        val env = config.contentfulEnvironment.trim().ifBlank { "master" }
        val cmaToken = config.contentfulManagementToken.trim()

        if (spaceId.isBlank() || cmaToken.isBlank()) {
            return CmsSyncResult(
                success = false,
                message = "Contentful Space ID & Management Token (CMA) are required to publish entries directly from the app."
            )
        }

        val locale = "en-US"
        fun localized(value: Any): JSONObject = JSONObject().put(locale, value)

        val fieldsObj = JSONObject().apply {
            put("title", localized(item.title))
            put("slug", localized(item.slug))
            put("contentType", localized(item.contentType))
            put("categoryName", localized(item.categoryName))
            put("summary", localized(item.summary))
            put("markdownBody", localized(item.markdownBody))
            put("mediaSource", localized(item.mediaSource))
            put("photoCaption", localized(item.photoCaption))
            put("photoLocation", localized(item.photoLocation))
            put("exifCamera", localized(item.exifCamera))
            put("techStackCsv", localized(item.techStackCsv))
            put("liveDemoUrl", localized(item.liveDemoUrl))
            put("repoUrl", localized(item.repoUrl))
            put("readingTimeMinutes", localized(item.readingTimeMinutes))
            put("isFeatured", localized(item.isFeatured))
        }

        val payload = JSONObject().put("fields", fieldsObj).toString()
        val createUrl = "https://api.contentful.com/spaces/$spaceId/environments/$env/entries"

        val createReq = Request.Builder()
            .url(createUrl)
            .header("Authorization", "Bearer $cmaToken")
            .header("X-Contentful-Content-Type", "portfolioItem")
            .post(payload.toRequestBody(contentfulCmaMediaType))
            .build()

        httpClient.newCall(createReq).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return CmsSyncResult(
                    success = false,
                    message = "Contentful CMA HTTP ${response.code}: ${extractErrorMessage(bodyStr)}"
                )
            }

            val createdJson = JSONObject(bodyStr)
            val sys = createdJson.optJSONObject("sys")
            val entryId = sys?.optString("id").orEmpty()
            val version = sys?.optInt("version", 1) ?: 1

            // Automatically publish the created entry in Contentful
            if (entryId.isNotBlank()) {
                val publishUrl = "https://api.contentful.com/spaces/$spaceId/environments/$env/entries/$entryId/published"
                val pubReq = Request.Builder()
                    .url(publishUrl)
                    .header("Authorization", "Bearer $cmaToken")
                    .header("X-Contentful-Version", version.toString())
                    .put("".toRequestBody( null))
                    .build()
                httpClient.newCall(pubReq).execute().close()
            }

            return CmsSyncResult(
                success = true,
                message = "Published '${item.title}' to Contentful (Entry ID: $entryId).",
                publishedEntryId = entryId
            )
        }
    }

    private fun fetchFromStrapi(
        config: SiteConfigEntity,
        existingCategories: List<CategoryEntity>
    ): CmsSyncResult {
        val baseUrl = config.strapiBaseUrl.trim().trimEnd('/')
        if (!baseUrl.startsWith("http")) {
            return CmsSyncResult(
                success = false,
                message = "Valid Strapi Base URL (e.g., https://cms.raseldevbd.com) is required."
            )
        }

        val url = "$baseUrl/api/portfolio-items?populate=*&pagination[pageSize]=100&sort=updatedAt:desc"
        val reqBuilder = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .get()

        if (config.strapiApiToken.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer ${config.strapiApiToken.trim()}")
        }

        httpClient.newCall(reqBuilder.build()).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return CmsSyncResult(
                    success = false,
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
                // Support both Strapi v4 (nested attributes) and Strapi v5 (flat attributes)
                val attrs = rawItem.optJSONObject("attributes") ?: rawItem

                val title = attrs.optString("title", "").ifBlank { continue }
                val rawType = attrs.optString("contentType", ContentType.BLOG).uppercase()
                val contentType = when {
                    rawType.contains("PROJ") -> ContentType.PROJECT
                    rawType.contains("PHOT") -> ContentType.PHOTO
                    else -> ContentType.BLOG
                }
                val categoryName = attrs.optString("categoryName", defaultCat?.name ?: "React & Next.js")
                val matchedCat = existingCategories.firstOrNull {
                    it.name.equals(categoryName, ignoreCase = true)
                } ?: defaultCat

                var mediaSource = attrs.optString("mediaSource", "")
                if (mediaSource.isBlank()) {
                    val coverUrl = attrs.optJSONObject("cover")
                        ?.optString("url")
                        ?: attrs.optJSONObject("cover")
                            ?.optJSONObject("data")
                            ?.optJSONObject("attributes")
                            ?.optString("url")
                        ?: ""
                    mediaSource = when {
                        coverUrl.startsWith("http") -> coverUrl
                        coverUrl.startsWith("/") -> "$baseUrl$coverUrl"
                        else -> defaultDrawableForType(contentType)
                    }
                }

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
                        mediaSource = mediaSource,
                        photoCaption = attrs.optString("photoCaption", ""),
                        photoLocation = attrs.optString("photoLocation", ""),
                        exifCamera = attrs.optString("exifCamera", ""),
                        techStackCsv = attrs.optString("techStackCsv", ""),
                        liveDemoUrl = attrs.optString("liveDemoUrl", ""),
                        repoUrl = attrs.optString("repoUrl", ""),
                        readingTimeMinutes = attrs.optInt("readingTimeMinutes", 5),
                        isFeatured = attrs.optBoolean("isFeatured", false),
                        isPublished = true,
                        createdAtEpoch = System.currentTimeMillis(),
                        updatedAtEpoch = System.currentTimeMillis(),
                        cmsEntryId = entryId,
                        cmsProvider = CmsProviderType.STRAPI,
                        cmsSyncedAt = System.currentTimeMillis()
                    )
                )
            }

            return CmsSyncResult(
                success = true,
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
        val apiToken = config.strapiApiToken.trim()

        if (!baseUrl.startsWith("http") || apiToken.isBlank()) {
            return CmsSyncResult(
                success = false,
                message = "Strapi Base URL and API Token (with Create/Update permission) are required to publish directly."
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
        val url = "$baseUrl/api/portfolio-items"

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
                    message = "Strapi POST HTTP ${response.code}: ${extractErrorMessage(bodyStr)}"
                )
            }

            val root = JSONObject(bodyStr)
            val data = root.optJSONObject("data")
            val entryId = data?.optString("documentId", data.optString("id", "")).orEmpty()

            return CmsSyncResult(
                success = true,
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
