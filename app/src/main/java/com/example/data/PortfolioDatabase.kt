package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CategoryEntity::class,
        ContentItemEntity::class,
        SiteConfigEntity::class
    ],
    version = 2,
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
            description = "React 18, Contentful CDA/CMA, Strapi REST v4/v5, and hybrid GitHub Pages architectures.",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 2L,
            name = "Android & Kotlin",
            slug = "android-kotlin",
            contentTypeScope = ContentType.ALL,
            accentHex = "#06B6D4",
            description = "Jetpack Compose, Material 3 adaptive layouts, Room offline-first caching, and Coroutines.",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 3L,
            name = "Cloud & DevOps",
            slug = "cloud-devops",
            contentTypeScope = ContentType.ALL,
            accentHex = "#3B82F6",
            description = "GitHub Actions CI/CD, Docker microservices, edge webhooks, and automated static builds.",
            sortOrder = 3
        ),
        CategoryEntity(
            id = 4L,
            name = "Dhaka Street & Tech Life",
            slug = "dhaka-street-tech",
            contentTypeScope = ContentType.PHOTO,
            accentHex = "#F43F5E",
            description = "Visual stories, twilight cityscapes, and developer workspace photography from Bangladesh.",
            sortOrder = 4
        ),
        CategoryEntity(
            id = 5L,
            name = "System Architecture",
            slug = "system-architecture",
            contentTypeScope = ContentType.BLOG,
            accentHex = "#A855F7",
            description = "Deep-dive engineering essays on API design, performance, and distributed content pipelines.",
            sortOrder = 5
        )
    )

    fun defaultContentItems(): List<ContentItemEntity> {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        return listOf(
            ContentItemEntity(
                id = 1L,
                title = "Rasel Dev BD — Headless CMS + GitHub Pages Hybrid Platform",
                slug = "rasel-dev-bd-headless-cms-platform",
                contentType = ContentType.PROJECT,
                categoryId = 1L,
                categoryName = "React & Headless CMS",
                summary = "Zero-redeploy React 18 & Contentful/Strapi developer platform that fetches live CMS entries at runtime and falls back to static GitHub Pages JSON.",
                markdownBody = """
## Why a Hybrid Headless CMS + Static Architecture?

Traditional GitHub Pages static sites require editing source code or waiting for a full CI/CD build every time you publish a blog post, upload a photo with captions, or showcase a new project.

**Rasel Dev BD** solves this with a dual-engine architecture:

### 1. Live Headless CMS Delivery (Contentful & Strapi)
- **Contentful Content Delivery API (CDA)**: Fetches live `portfolioItem` entries and resolves linked media Assets (`https://cdn.contentful.com/spaces/{spaceId}/environments/{env}/entries?include=2`).
- **Strapi REST API v4/v5**: Queries `/api/portfolio-items?populate=*&sort=updatedAt:desc` with automatic media URL normalization.
- **Instant No-Code Publishing**: Using the **Contentful Management API (CMA)** or **Strapi Bearer Token**, the Admin Studio publishes new posts straight to the cloud without touching a single line of code.

### 2. Resilient Static Fallback (`content-bundle.json`)
- If the visitor is offline or if CMS credentials are not yet configured, the React app seamlessly renders the pre-generated `public/data/content-bundle.json` snapshot hosted on GitHub Pages.
                """.trimIndent(),
                mediaSource = "drawable:img_project_cloud",
                techStackCsv = "React 18,Contentful API,Strapi v5,Tailwind CSS,GitHub Pages",
                liveDemoUrl = "https://raseldevbd.github.io",
                repoUrl = "https://github.com/raseldevbd/raseldevbd.github.io",
                readingTimeMinutes = 6,
                isFeatured = true,
                isPublished = true,
                createdAtEpoch = now - 5 * dayMs,
                updatedAtEpoch = now - 1 * dayMs,
                cmsEntryId = "cf-entry-rasel-cms-01",
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 3_600_000L
            ),
            ContentItemEntity(
                id = 2L,
                title = "Integrating Contentful & Strapi with a React Static Site on GitHub Pages",
                slug = "integrating-contentful-strapi-react-github-pages",
                contentType = ContentType.BLOG,
                categoryId = 1L,
                categoryName = "React & Headless CMS",
                summary = "A step-by-step engineering guide to posting blogs, project showcases, and photo stories via Contentful or Strapi without changing code.",
                markdownBody = """
## Posting Content Without Changing Code

When hosting a React portfolio on **GitHub Pages**, decoupling your content layer from your presentation code gives you the best of both worlds: free global CDN hosting and dynamic CMS publishing.

### Contentful Setup (`portfolioItem` Content Model)
Create a Content Type with ID `portfolioItem` and the following fields:
- `title` (Short text, required)
- `slug` (Short text)
- `contentType` (`PROJECT`, `BLOG`, or `PHOTO`)
- `categoryName` (Short text — e.g. `React & Headless CMS`)
- `summary` (Short text)
- `markdownBody` (Long text / Markdown)
- `mediaSource` (Short text URL or linked `media` Asset)
- `photoCaption`, `photoLocation`, `exifCamera` (Short text for photography)
- `techStackCsv`, `liveDemoUrl`, `repoUrl` (Short text for projects)

### Strapi v4/v5 Setup (`portfolio-items` Collection)
Create a Collection Type named `portfolio-item` (plural `portfolio-items`) with the same attributes, and enable `find`, `findOne`, and authenticated `create`/`update` permissions under **Settings → Users & Permissions → Roles**.
                """.trimIndent(),
                mediaSource = "drawable:img_hero_banner",
                techStackCsv = "Contentful,Strapi,React,REST API,JSON",
                readingTimeMinutes = 7,
                isFeatured = true,
                isPublished = true,
                createdAtEpoch = now - 4 * dayMs,
                updatedAtEpoch = now - 12 * 3_600_000L,
                cmsEntryId = "cf-entry-headless-guide-02",
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 3_600_000L
            ),
            ContentItemEntity(
                id = 3L,
                title = "Blue Hour Over Hatirjheel Lake, Dhaka",
                slug = "blue-hour-hatirjheel-dhaka",
                contentType = ContentType.PHOTO,
                categoryId = 4L,
                categoryName = "Dhaka Street & Tech Life",
                summary = "Long-exposure twilight reflection of Dhaka's illuminated Hatirjheel bridge after an evening coding sprint.",
                markdownBody = """
Captured during blue hour in the heart of Dhaka. The neon emerald and cyan reflections across the water inspired the color palette of the **Rasel Dev BD** developer studio.

Uploaded and captioned via the Headless CMS media pipeline with full EXIF metadata preservation.
                """.trimIndent(),
                mediaSource = "drawable:img_photo_dhaka",
                photoCaption = "Twilight reflections along the Hatirjheel amphitheater bridge — Dhaka, Bangladesh.",
                photoLocation = "Hatirjheel, Dhaka, Bangladesh",
                exifCamera = "Sony A7 IV • 24mm f/1.4 GM • ISO 100 • 2.5s",
                techStackCsv = "Street Photography,Blue Hour,Dhaka,Sony Alpha",
                readingTimeMinutes = 2,
                isFeatured = true,
                isPublished = true,
                createdAtEpoch = now - 3 * dayMs,
                updatedAtEpoch = now - 2 * dayMs,
                cmsEntryId = "cf-entry-photo-dhaka-03",
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 3_600_000L
            ),
            ContentItemEntity(
                id = 4L,
                title = "BongoCloud — Multi-Region Kubernetes Telemetry & Cost Radar",
                slug = "bongocloud-kubernetes-telemetry",
                contentType = ContentType.PROJECT,
                categoryId = 3L,
                categoryName = "Cloud & DevOps",
                summary = "Real-time cloud cluster health, pod autoscaling metrics, and anomaly alerting dashboard built for South Asian fintech teams.",
                markdownBody = """
## Overview

**BongoCloud** aggregates Prometheus metrics and OpenTelemetry traces into a sub-second reactive dashboard.

### Key Capabilities
- Real-time node CPU/Memory pressure heatmaps.
- Automated Slack & webhook incident dispatch.
- Synchronized release notes powered by Strapi Headless CMS webhooks.
                """.trimIndent(),
                mediaSource = "drawable:img_project_cloud",
                techStackCsv = "Kotlin,Go,React,Kubernetes,Prometheus",
                liveDemoUrl = "https://raseldevbd.github.io/#projects",
                repoUrl = "https://github.com/raseldevbd/bongocloud-radar",
                readingTimeMinutes = 5,
                isFeatured = true,
                isPublished = true,
                createdAtEpoch = now - 6 * dayMs,
                updatedAtEpoch = now - 3 * dayMs,
                cmsEntryId = "strapi-entry-bongocloud-04",
                cmsProvider = CmsProviderType.STRAPI,
                cmsSyncedAt = now - 7_200_000L
            ),
            ContentItemEntity(
                id = 5L,
                title = "Offline-First Android Apps with Room + Headless CMS Sync",
                slug = "offline-first-android-room-headless-cms",
                contentType = ContentType.BLOG,
                categoryId = 2L,
                categoryName = "Android & Kotlin",
                summary = "Designing resilient mobile architectures that pair Jetpack Compose and Room SQLite caching with Contentful and Strapi REST APIs.",
                markdownBody = """
## Single Source of Truth with Room + Headless CMS

Mobile networks can be unpredictable. By treating **Room** as the local reactive source of truth (`Flow<List<ContentItemEntity>>`) and synchronizing delta updates from **Contentful** or **Strapi** in background coroutines, the UI renders in 0ms on cold start while staying up to date with remote CMS edits.
                """.trimIndent(),
                mediaSource = "drawable:img_avatar_rasel",
                techStackCsv = "Kotlin,Jetpack Compose,Room,OkHttp,Coroutines",
                readingTimeMinutes = 6,
                isFeatured = false,
                isPublished = true,
                createdAtEpoch = now - 7 * dayMs,
                updatedAtEpoch = now - 4 * dayMs,
                cmsEntryId = "cf-entry-room-cms-05",
                cmsProvider = CmsProviderType.CONTENTFUL,
                cmsSyncedAt = now - 3_600_000L
            )
        )
    }
}
