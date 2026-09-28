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

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val slug: String,
    val contentTypeScope: String = ContentType.ALL, // ALL, PROJECT, BLOG, PHOTO
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
    val summary: String,
    val markdownBody: String,
    val mediaSource: String, // "drawable:img_...", content:// URI, or remote https:// URL from CMS
    val photoCaption: String = "",
    val photoLocation: String = "",
    val exifCamera: String = "",
    val techStackCsv: String = "",
    val liveDemoUrl: String = "",
    val repoUrl: String = "",
    val readingTimeMinutes: Int = 4,
    val isFeatured: Boolean = false,
    val isPublished: Boolean = true,
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis(),
    val cmsEntryId: String = "",
    val cmsProvider: String = "",
    val cmsSyncedAt: Long = 0L
)

@Entity(tableName = "site_config")
data class SiteConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val siteTitle: String = "Rasel Dev BD",
    val tagline: String = "Full-Stack React & Android Architect • Content & Open-Source Platform",
    val ownerName: String = "Rasel Chowdhury",
    val ownerRole: String = "Senior Full-Stack Engineer & Creative Technologist",
    val bio: String = "Building resilient cloud-native web apps, native Android experiences, and headless CMS developer platforms from Dhaka, Bangladesh.",
    val location: String = "Dhaka, Bangladesh",
    val email: String = "alexraselchodhury@gmail.com",
    val githubUrl: String = "https://github.com/raseldevbd",
    val githubPagesRepo: String = "raseldevbd/raseldevbd.github.io",
    val customDomain: String = "raseldevbd.github.io",
    val adminPin: String = "2026",
    val avatarSource: String = "drawable:img_avatar_rasel",
    val heroBannerSource: String = "drawable:img_hero_banner",
    val lastExportedEpoch: Long = 0L,
    // Headless CMS Integration Settings (Contentful & Strapi)
    val cmsProvider: String = BuildConfig.CMS_PROVIDER.ifBlank { CmsProviderType.CONTENTFUL },
    val contentfulSpaceId: String = BuildConfig.CONTENTFUL_SPACE_ID.takeIf {
        it.isNotBlank() && it != "YOUR_CONTENTFUL_SPACE_ID"
    } ?: "",
    val contentfulEnvironment: String = BuildConfig.CONTENTFUL_ENVIRONMENT.ifBlank { "master" },
    val contentfulDeliveryToken: String = BuildConfig.CONTENTFUL_DELIVERY_TOKEN.takeIf {
        it.isNotBlank() && it != "YOUR_CONTENTFUL_DELIVERY_TOKEN"
    } ?: "",
    val contentfulManagementToken: String = BuildConfig.CONTENTFUL_MANAGEMENT_TOKEN.takeIf {
        it.isNotBlank() && it != "YOUR_CONTENTFUL_MANAGEMENT_TOKEN"
    } ?: "",
    val strapiBaseUrl: String = BuildConfig.STRAPI_BASE_URL.ifBlank { "https://cms.raseldevbd.com" },
    val strapiApiToken: String = BuildConfig.STRAPI_API_TOKEN.takeIf {
        it.isNotBlank() && it != "YOUR_STRAPI_API_TOKEN"
    } ?: "",
    val autoSyncCmsOnPublish: Boolean = true,
    val lastCmsSyncEpoch: Long = 0L,
    val lastCmsSyncStatus: String = "Ready • Local Room Cache + Headless CMS Bridge"
) {
    val isContentfulConfigured: Boolean
        get() = contentfulSpaceId.isNotBlank() && contentfulDeliveryToken.isNotBlank()

    val isContentfulPublishReady: Boolean
        get() = contentfulSpaceId.isNotBlank() && contentfulManagementToken.isNotBlank()

    val isStrapiConfigured: Boolean
        get() = strapiBaseUrl.startsWith("http") && strapiBaseUrl.length > 10

    val isActiveCmsConfigured: Boolean
        get() = if (cmsProvider == CmsProviderType.STRAPI) isStrapiConfigured else isContentfulConfigured
}
