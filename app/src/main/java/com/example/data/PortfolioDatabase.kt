package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CategoryEntity::class,
        ContentItemEntity::class,
        CmaAuditLogEntity::class,
        SiteConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PortfolioDatabase : RoomDatabase() {

    abstract fun portfolioDao(): PortfolioDao

    companion object {
        @Volatile
        private var INSTANCE: PortfolioDatabase? = null

        fun getDatabase(context: Context): PortfolioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PortfolioDatabase::class.java,
                    "rasel_dev_bd_cms.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object InitialSeedData {

    fun defaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(
            id = 1L,
            name = "React & Headless CMS",
            slug = "react-headless-cms",
            contentTypeScope = ContentType.ALL,
            accentHex = "#10B981",
            description = "Contentful Content Management API (CMA), rich media releases, and live React web updates.",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 2L,
            name = "Android & Kotlin",
            slug = "android-kotlin",
            contentTypeScope = ContentType.ALL,
            accentHex = "#06B6D4",
            description = "APK releases, Jetpack Compose starter templates, and mobile SDK packages.",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 3L,
            name = "Cloud & DevOps",
            slug = "cloud-devops",
            contentTypeScope = ContentType.ALL,
            accentHex = "#3B82F6",
            description = "Docker bundles, Kubernetes helm charts, and GitHub Actions automation kits.",
            sortOrder = 3
        ),
        CategoryEntity(
            id = 4L,
            name = "Dhaka Street & Tech Life",
            slug = "dhaka-street-tech",
            contentTypeScope = ContentType.PHOTO,
            accentHex = "#F43F5E",
            description = "High-resolution 4K wallpaper packs, RAW photography presets, and visual stories.",
            sortOrder = 4
        ),
        CategoryEntity(
            id = 5L,
            name = "System Architecture",
            slug = "system-architecture",
            contentTypeScope = ContentType.BLOG,
            accentHex = "#A855F7",
            description = "Downloadable PDF system blueprints, schema templates, and API engineering guides.",
            sortOrder = 5
        )
    )

    fun defaultContentItems(): List<ContentItemEntity> {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        return listOf(
            ContentItemEntity(
                id = 1L,
                title = "Rasel Dev BD — Contentful CMA & React Starter Kit",
                slug = "rasel-dev-bd-headless-cms-platform",
                contentType = ContentType.PROJECT,
                categoryId = 1L,
                categoryName = "React & Headless CMS",
                summary = "Complete Contentful Content Management API (CMA) & React 18 platform with rich media image uploads, markdown descriptions, and instant download links.",
                markdownBody = """
## Direct Contentful CMA Publishing for Immediate Website Updates

**Rasel Dev BD** connects directly to the **Contentful Content Management API (`api.contentful.com`)** and **Asset Upload API (`upload.contentful.com`)** so you can create and publish rich media entries from your Android device without touching code:

### What Gets Published to Contentful CMA
- **Rich Images & Binary Assets**: Pick any image from your device gallery to upload via `upload.contentful.com`, process, and link to your Contentful entry, or attach a remote CDN image URL.
- **Structured Descriptions**: Short card summary (`summary` / `description`) and full Markdown body (`markdownBody`).
- **Direct Download Links**: Attach release packages, APK builds, ZIP templates, or high-resolution asset bundles (`downloadUrl`, `downloadLabel`, `downloadFileSize`, `versionTag`) that appear immediately on the live website.
                """.trimIndent(),
                mediaSource = "drawable:img_project_cloud",
                downloadUrl = "https://github.com/raseldevbd/raseldevbd.github.io/archive/refs/heads/main.zip",
                downloadLabel = "Download Starter Kit (.ZIP)",
                downloadFileSize = "14.8 MB",
                versionTag = "v2.4.0",
                techStackCsv = "Contentful CMA,React 18,Kotlin,Jetpack Compose,GitHub Pages",
                liveDemoUrl = "https://raseldevbd.github.io",
                repoUrl = "https://github.com/raseldevbd/raseldevbd.github.io",
                readingTimeMinutes = 6,
                isFeatured = true,
                isPublished = true,
                createdAtEpoch = now - 5 * dayMs,
                updatedAtEpoch = now - 1 * dayMs,
                cmsEntryId = "cf-entry-rasel-cms-01",
                cmsAssetId = "cf-asset-cloud-01",
                cmsVersion = 3,
                cmsStatus = CmaEntryStatus.PUBLISHED,
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 1_800_000L
            ),
            ContentItemEntity(
                id = 2L,
                title = "Contentful Content Management API (CMA) Schema & Blueprint",
                slug = "integrating-contentful-strapi-react-github-pages",
                contentType = ContentType.BLOG,
                categoryId = 5L,
                categoryName = "System Architecture",
                summary = "Step-by-step guide and downloadable JSON Content-Type migration script for posting images, descriptions, and download links to Contentful CMA.",
                markdownBody = """
## Posting Rich Media Content via Contentful CMA

Using the **Contentful Content Management API**, entries follow a deterministic two-phase commit:

### 1. Create or Update Entry (`POST` / `PUT`)
Send localized field values (`en-US`) to:
```
POST https://api.contentful.com/spaces/{space_id}/environments/master/entries
X-Contentful-Content-Type: portfolioItem
Authorization: Bearer {CONTENTFUL_MANAGEMENT_TOKEN}
```

### 2. Activate Live Website Update (`PUT /published`)
Immediately publish the entry version so your connected React website reflects the new image, description, and download button:
```
PUT https://api.contentful.com/spaces/{space_id}/environments/master/entries/{entry_id}/published
X-Contentful-Version: {version}
```
                """.trimIndent(),
                mediaSource = "drawable:img_hero_banner",
                downloadUrl = "https://raw.githubusercontent.com/raseldevbd/raseldevbd.github.io/main/docs/data/content-bundle.json",
                downloadLabel = "Download CMA Schema (.JSON)",
                downloadFileSize = "42 KB",
                versionTag = "v2.0-schema",
                techStackCsv = "Contentful CMA,REST API,JSON Schema,Webhooks",
                readingTimeMinutes = 7,
                isFeatured = true,
                isPublished = true,
                createdAtEpoch = now - 4 * dayMs,
                updatedAtEpoch = now - 12 * 3_600_000L,
                cmsEntryId = "cf-entry-headless-guide-02",
                cmsAssetId = "cf-asset-hero-02",
                cmsVersion = 2,
                cmsStatus = CmaEntryStatus.PUBLISHED,
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 3_600_000L
            ),
            ContentItemEntity(
                id = 3L,
                title = "Blue Hour Over Hatirjheel Lake — 4K Wallpaper & LUT Pack",
                slug = "blue-hour-hatirjheel-dhaka",
                contentType = ContentType.PHOTO,
                categoryId = 4L,
                categoryName = "Dhaka Street & Tech Life",
                summary = "High-resolution twilight photography from Dhaka with downloadable 4K uncompressed wallpaper and Sony Alpha color-grading LUTs.",
                markdownBody = """
Captured during blue hour in the heart of Dhaka. The neon emerald and cyan reflections across the water inspired the color palette of the **Rasel Dev BD** developer studio.

Published via the Contentful CMA binary asset pipeline with full EXIF metadata and a direct 4K wallpaper download link.
                """.trimIndent(),
                mediaSource = "drawable:img_photo_dhaka",
                photoCaption = "Twilight reflections along the Hatirjheel amphitheater bridge — Dhaka, Bangladesh.",
                photoLocation = "Hatirjheel, Dhaka, Bangladesh",
                exifCamera = "Sony A7 IV • 24mm f/1.4 GM • ISO 100 • 2.5s",
                downloadUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=3840&q=90",
                downloadLabel = "Download 4K Photo & LUT (.JPG)",
                downloadFileSize = "28.4 MB",
                versionTag = "4K-RAW",
                techStackCsv = "4K Wallpaper,Dhaka,Sony Alpha,Contentful Asset",
                readingTimeMinutes = 2,
                isFeatured = true,
                isPublished = true,
                createdAtEpoch = now - 3 * dayMs,
                updatedAtEpoch = now - 2 * dayMs,
                cmsEntryId = "cf-entry-photo-dhaka-03",
                cmsAssetId = "cf-asset-dhaka-03",
                cmsVersion = 1,
                cmsStatus = CmaEntryStatus.PUBLISHED,
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 3_600_000L
            ),
            ContentItemEntity(
                id = 4L,
                title = "BongoCloud — Kubernetes Telemetry CLI & Helm Chart Release",
                slug = "bongocloud-kubernetes-telemetry",
                contentType = ContentType.PROJECT,
                categoryId = 3L,
                categoryName = "Cloud & DevOps",
                summary = "Real-time cloud cluster health, pod autoscaling metrics, and downloadable multi-region Helm chart bundle.",
                markdownBody = """
## Overview

**BongoCloud** aggregates Prometheus metrics and OpenTelemetry traces into a sub-second reactive dashboard.

### Included in the Release Download
- Pre-configured Kubernetes Helm chart (`bongocloud-chart-v1.8.tgz`).
- Grafana JSON dashboards and alert rules.
- Contentful CMA release notes webhook listener.
                """.trimIndent(),
                mediaSource = "drawable:img_project_cloud",
                downloadUrl = "https://github.com/raseldevbd/bongocloud-radar/releases/latest",
                downloadLabel = "Download Helm Release (.TGZ)",
                downloadFileSize = "8.2 MB",
                versionTag = "v1.8.2",
                techStackCsv = "Kotlin,Go,Kubernetes,Prometheus,Contentful CMA",
                liveDemoUrl = "https://raseldevbd.github.io/#projects",
                repoUrl = "https://github.com/raseldevbd/bongocloud-radar",
                readingTimeMinutes = 5,
                isFeatured = true,
                isPublished = true,
                createdAtEpoch = now - 6 * dayMs,
                updatedAtEpoch = now - 3 * dayMs,
                cmsEntryId = "cf-entry-bongocloud-04",
                cmsAssetId = "cf-asset-bongo-04",
                cmsVersion = 4,
                cmsStatus = CmaEntryStatus.PUBLISHED,
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 7_200_000L
            ),
            ContentItemEntity(
                id = 5L,
                title = "Compose + Room Offline-First Contentful CMA Companion Architecture",
                slug = "offline-first-android-room-headless-cms",
                contentType = ContentType.BLOG,
                categoryId = 2L,
                categoryName = "Android & Kotlin",
                summary = "Architectural deep dive with downloadable Kotlin sample code for pairing Jetpack Compose and Room with Contentful's Content Management API.",
                markdownBody = """
## Single Source of Truth with Room + Contentful CMA

By treating **Room** as the local reactive source of truth and dispatching `POST /entries` + `PUT /entries/{id}/published` calls to **Contentful CMA**, creators can draft rich media entries offline and publish them to their live website in one tap.
                """.trimIndent(),
                mediaSource = "drawable:img_avatar_rasel",
                downloadUrl = "https://github.com/raseldevbd/android-contentful-cma-kit/archive/refs/heads/main.zip",
                downloadLabel = "Download Kotlin Sample (.ZIP)",
                downloadFileSize = "6.1 MB",
                versionTag = "v1.2.0",
                techStackCsv = "Kotlin,Jetpack Compose,Room,Contentful CMA,OkHttp",
                readingTimeMinutes = 6,
                isFeatured = false,
                isPublished = true,
                createdAtEpoch = now - 7 * dayMs,
                updatedAtEpoch = now - 4 * dayMs,
                cmsEntryId = "cf-entry-room-cms-05",
                cmsAssetId = "cf-asset-avatar-05",
                cmsVersion = 1,
                cmsStatus = CmaEntryStatus.PUBLISHED,
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 3_600_000L
            )
        )
    }

    fun defaultAuditLogs(): List<CmaAuditLogEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            CmaAuditLogEntity(
                id = 1L,
                actionType = "PUBLISH_ENTRY",
                entryTitle = "Rasel Dev BD — Contentful CMA & React Starter Kit",
                cmsEntryId = "cf-entry-rasel-cms-01",
                endpoint = "PUT /spaces/{space_id}/environments/master/entries/cf-entry-rasel-cms-01/published",
                httpStatus = 200,
                success = true,
                message = "Published entry v3 with downloadUrl & media asset to live website.",
                timestampEpoch = now - 1_800_000L
            ),
            CmaAuditLogEntity(
                id = 2L,
                actionType = "UPLOAD_ASSET",
                entryTitle = "Blue Hour Over Hatirjheel Lake — 4K Wallpaper & LUT Pack",
                cmsEntryId = "cf-asset-dhaka-03",
                endpoint = "POST https://upload.contentful.com/spaces/{space_id}/uploads",
                httpStatus = 201,
                success = true,
                message = "Processed & published 4K image asset (28.4 MB) to Contentful CDN.",
                timestampEpoch = now - 3_600_000L
            )
        )
    }
}
