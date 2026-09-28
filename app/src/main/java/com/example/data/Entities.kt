package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.BuildConfig

object ContentType {
    const val PROJECT = "PROJECT"
    const val BLOG = "BLOG"
    const val PHOTO = "PHOTO"
    const val ALL = "ALL"
}

object CmsProviderType {
    const val CONTENTFUL = "CONTENTFUL"
    const val STRAPI = "STRAPI"
}

object CmaEntryStatus {
    const val PUBLISHED = "PUBLISHED"
    const val DRAFT = "DRAFT"
    const val LOCAL_READY = "LOCAL_READY"
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val slug: String,
    val contentTypeScope: String = ContentType.ALL,
    val accentHex: String = "#10B981",
    val description: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "content_items")
data class ContentItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val slug: String,
    val contentType: String, // PROJECT, BLOG, PHOTO
    val categoryId: Long,
    val categoryName: String,
    val summary: String, // Short Description
    val markdownBody: String, // Full Rich Media Description & Markdown
    val mediaSource: String, // "drawable:img_...", content:// URI, or remote https:// URL
    val photoCaption: String = "",
    val photoLocation: String = "",
    val exifCamera: String = "",
    // First-Class Download Link & Release Asset Metadata for Contentful CMA
    val downloadUrl: String = "",
    val downloadLabel: String = "Download Asset Pack",
    val downloadFileSize: String = "",
    val versionTag: String = "v1.0.0",
    val techStackCsv: String = "",
    val liveDemoUrl: String = "",
    val repoUrl: String = "",
    val readingTimeMinutes: Int = 4,
    val isFeatured: Boolean = false,
    val isPublished: Boolean = true,
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis(),
    // Contentful Content Management API (CMA) Tracking
    val cmsEntryId: String = "",
    val cmsAssetId: String = "",
    val cmsVersion: Int = 1,
    val cmsStatus: String = CmaEntryStatus.PUBLISHED,
    val cmsProvider: String = CmsProviderType.CONTENTFUL,
    val cmsSyncedAt: Long = 0L
)

@Entity(tableName = "cma_audit_logs")
data class CmaAuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val actionType: String, // PUBLISH_ENTRY, UPLOAD_ASSET, UPDATE_ENTRY, UNPUBLISH_ENTRY, SYNC_SPACE
    val entryTitle: String,
    val cmsEntryId: String,
    val endpoint: String,
    val httpStatus: Int,
    val success: Boolean,
    val message: String,
    val timestampEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "site_config")
data class SiteConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val siteTitle: String = "Rasel Dev BD",
    val tagline: String = "Contentful Content Management Studio & Live Website Publisher",
    val ownerName: String = "Rasel Chowdhury",
    val ownerRole: String = "Senior Full-Stack Engineer & Contentful Architect",
    val bio: String = "Create and publish rich media content—including images, descriptions, and direct download links—straight to Contentful via the Content Management API for instant live website updates.",
    val location: String = "Dhaka, Bangladesh",
    val email: String = "alexraselchodhury@gmail.com",
    val githubUrl: String = "https://github.com/raseldevbd",
    val githubPagesRepo: String = "raseldevbd/raseldevbd.github.io",
    val customDomain: String = "raseldevbd.github.io",
    val adminPin: String = "2026",
    val avatarSource: String = "drawable:img_avatar_rasel",
    val heroBannerSource: String = "drawable:img_hero_banner",
    val lastExportedEpoch: Long = 0L,
    // Non-secret Contentful CMA Model Configuration
    val cmsProvider: String = CmsProviderType.CONTENTFUL,
    val contentfulEnvironment: String = BuildConfig.CONTENTFUL_ENVIRONMENT.ifBlank { "master" },
    val contentfulContentType: String = BuildConfig.CONTENTFUL_CONTENT_TYPE.ifBlank { "portfolioItem" },
    val contentfulLocale: String = BuildConfig.CONTENTFUL_LOCALE.ifBlank { "en-US" },
    val strapiBaseUrl: String = BuildConfig.STRAPI_BASE_URL.ifBlank { "https://cms.raseldevbd.com" },
    val autoSyncCmsOnPublish: Boolean = true,
    val lastCmsSyncEpoch: Long = 0L,
    val lastCmsSyncStatus: String = "Contentful CMA Ready • Instant Live Website Updates"
) {
    val spaceIdFromSecrets: String
        get() = BuildConfig.CONTENTFUL_SPACE_ID.takeIf {
            it.isNotBlank() && it != "YOUR_CONTENTFUL_SPACE_ID"
        } ?: ""

    val cmaTokenFromSecrets: String
        get() = BuildConfig.CONTENTFUL_MANAGEMENT_TOKEN.takeIf {
            it.isNotBlank() && it != "YOUR_CONTENTFUL_MANAGEMENT_TOKEN"
        } ?: ""

    val cdaTokenFromSecrets: String
        get() = BuildConfig.CONTENTFUL_DELIVERY_TOKEN.takeIf {
            it.isNotBlank() && it != "YOUR_CONTENTFUL_DELIVERY_TOKEN"
        } ?: ""

    val strapiTokenFromSecrets: String
        get() = BuildConfig.STRAPI_API_TOKEN.takeIf {
            it.isNotBlank() && it != "YOUR_STRAPI_API_TOKEN"
        } ?: ""

    val isContentfulCmaConfigured: Boolean
        get() = spaceIdFromSecrets.isNotBlank() && cmaTokenFromSecrets.isNotBlank()

    val isContentfulCdaConfigured: Boolean
        get() = spaceIdFromSecrets.isNotBlank() && (cdaTokenFromSecrets.isNotBlank() || cmaTokenFromSecrets.isNotBlank())

    val isActiveCmsConfigured: Boolean
        get() = if (cmsProvider == CmsProviderType.STRAPI) {
            strapiBaseUrl.startsWith("http") && strapiTokenFromSecrets.isNotBlank()
        } else {
            isContentfulCmaConfigured
        }
}
