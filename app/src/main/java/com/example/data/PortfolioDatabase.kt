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
    version = 1,
    exportSchema = false
)
abstract class PortfolioDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao

    companion object {
        @Volatile
        private var INSTANCE: PortfolioDatabase? = null

        fun getInstance(context: Context): PortfolioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PortfolioDatabase::class.java,
                    "rasel_dev_bd_portfolio.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object SeedPortfolioData {
    fun defaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(
            id = 1L,
            name = "React & Static Web",
            slug = "react-static-web",
            contentTypeScope = ContentType.ALL,
            colorHex = "#10B981",
            description = "React 19, Vite, Next.js, and GitHub Pages static site architectures.",
            displayOrder = 1
        ),
        CategoryEntity(
            id = 2L,
            name = "Android & Kotlin",
            slug = "android-kotlin",
            contentTypeScope = ContentType.ALL,
            colorHex = "#06B6D4",
            description = "Jetpack Compose, Room offline-first databases, and Material 3 apps.",
            displayOrder = 2
        ),
        CategoryEntity(
            id = 3L,
            name = "Cloud & DevOps",
            slug = "cloud-devops",
            contentTypeScope = ContentType.ALL,
            colorHex = "#8B5CF6",
            description = "GitHub Actions CI/CD, Docker containers, edge functions, and telemetry.",
            displayOrder = 3
        ),
        CategoryEntity(
            id = 4L,
            name = "Dhaka Tech & Street",
            slug = "dhaka-tech-street",
            contentTypeScope = ContentType.PHOTO,
            colorHex = "#F59E0B",
            description = "Visual stories, twilight architecture, and developer life across Bangladesh.",
            displayOrder = 4
        ),
        CategoryEntity(
            id = 5L,
            name = "System Architecture",
            slug = "system-architecture",
            contentTypeScope = ContentType.BLOG,
            colorHex = "#F43F5E",
            description = "Deep-dive engineering notes on scalability, state synchronization, and UI performance.",
            displayOrder = 5
        )
    )

    fun defaultContentItems(now: Long = System.currentTimeMillis()): List<ContentItemEntity> = listOf(
        // PROJECTS
        ContentItemEntity(
            id = 101L,
            contentType = ContentType.PROJECT,
            title = "Rasel Dev BD — Static CMS & GitHub Pages Engine",
            slug = "rasel-dev-bd-static-cms",
            summaryOrCaption = "Zero-backend React 19 + GitHub Pages portfolio and content platform with automated JSON bundle generation, dynamic category routing, and offline-first authoring.",
            bodyMarkdown = """
## Overview
**Rasel Dev BD Static CMS** bridges local content authoring with zero-cost static hosting on GitHub Pages (`rasel-dev-bd.github.io`).

### Key Engineering Highlights
- **Dynamic Category Taxonomy**: Manage unified or scoped categories across Projects, Blog Articles, and Photo Galleries without touching routing code.
- **Deterministic Static JSON Bundle**: Compiles all published entries into a lightweight `content-bundle.json` consumed at build-time or client-hydration time.
- **GitHub Actions CI/CD**: Automated workflow builds the Vite + React SPA and deploys directly to the `gh-pages` branch.

```tsx
export async function loadPortfolioBundle() {
  const res = await fetch('./data/content-bundle.json');
  return await res.json();
}
```
            """.trimIndent(),
            categoryId = 1L,
            categoryName = "React & Static Web",
            mediaSource = "drawable:img_project_cloud",
            tagsCsv = "React 19,TypeScript,Vite,GitHub Pages,Tailwind CSS",
            liveUrl = "https://rasel-dev-bd.github.io",
            repoUrl = "https://github.com/rasel-dev-bd/rasel-dev-bd.github.io",
            isFeatured = true,
            isPublished = true,
            likesCount = 128,
            createdAt = now - 86400000L * 2,
            updatedAt = now - 86400000L * 2
        ),
        ContentItemEntity(
            id = 102L,
            contentType = ContentType.PROJECT,
            title = "BengalPay Pulse — Real-Time Fintech Telemetry",
            slug = "bengalpay-pulse-telemetry",
            summaryOrCaption = "High-frequency merchant analytics dashboard & Kotlin multiplatform companion app monitoring payment gateway latency across Dhaka & Chattogram.",
            bodyMarkdown = """
## Problem Statement
Fintech teams in Bangladesh need sub-second visibility into mobile financial service (MFS) webhook latencies and settlement queues.

### Architecture
- **Frontend**: React + Canvas streaming charts with 60fps virtualized transaction tables.
- **Mobile Companion**: Native Android app built with Jetpack Compose and Room for offline incident runbooks.
- **Edge Aggregation**: Cloudflare Workers + WebSocket delta compression reducing payload size by 74%.
            """.trimIndent(),
            categoryId = 3L,
            categoryName = "Cloud & DevOps",
            mediaSource = "drawable:img_hero_banner",
            tagsCsv = "Kotlin,Jetpack Compose,React,WebSockets,Docker",
            liveUrl = "https://rasel-dev-bd.github.io/bengalpay-pulse",
            repoUrl = "https://github.com/rasel-dev-bd/bengalpay-pulse",
            isFeatured = true,
            isPublished = true,
            likesCount = 94,
            createdAt = now - 86400000L * 5,
            updatedAt = now - 86400000L * 5
        ),
        ContentItemEntity(
            id = 103L,
            contentType = ContentType.PROJECT,
            title = "Kotha Compose UI Kit — Bangla-First Design System",
            slug = "kotha-compose-ui-kit",
            summaryOrCaption = "Accessible Material 3 component library tuned for bilingual Bangla & English typography, dynamic color tokens, and foldable layouts.",
            bodyMarkdown = """
## Why Kotha UI?
Rendering bilingual Bangla and English interfaces often suffers from baseline clipping and inconsistent line-heights.

### Features
- **Adaptive Typography Scale**: Pre-tuned line heights and optical letter spacing for mixed script headers.
- **WCAG AAA Contrast Tokens**: High-legibility dark and light surfaces tested under outdoor sunlight conditions.
- **Zero-Boilerplate Components**: Drop-in filter bars, code preview blocks, and media cards.
            """.trimIndent(),
            categoryId = 2L,
            categoryName = "Android & Kotlin",
            mediaSource = "drawable:img_project_cloud",
            tagsCsv = "Jetpack Compose,Material 3,Kotlin,Accessibility,Design System",
            liveUrl = "https://rasel-dev-bd.github.io/kotha-ui",
            repoUrl = "https://github.com/rasel-dev-bd/kotha-compose-ui",
            isFeatured = false,
            isPublished = true,
            likesCount = 76,
            createdAt = now - 86400000L * 9,
            updatedAt = now - 86400000L * 9
        ),

        // BLOG POSTS
        ContentItemEntity(
            id = 201L,
            contentType = ContentType.BLOG,
            title = "Architecting a Static-First React CMS for GitHub Pages Without a Backend Server",
            slug = "static-first-react-cms-github-pages",
            summaryOrCaption = "How to combine an offline-capable Admin Dashboard with deterministic JSON asset generation so your portfolio scales to 100k+ views for $0/month.",
            bodyMarkdown = """
## Why Static Site Generation Still Wins for Developer Portfolios
When hosting on **GitHub Pages**, you don't have a runtime Node.js server or SQL database executing on every request. Yet developers still want a rich **Admin Dashboard** to organize dynamic categories, write markdown blog posts, and upload project & photo galleries.

### The Hybrid Local-CMS + Static Bundle Pattern
1. **Author Locally in Structured Storage**: Manage categories, drafts, and media metadata in a local database (Room on Android or IndexedDB/localStorage in Web Studio).
2. **Compile to `content-bundle.json`**: Export a clean, versioned JSON schema containing only `isPublished == true` items.
3. **Commit & Deploy via GitHub Actions**: Push the updated JSON and media assets to your repository, triggering a 25-second static build.

```json
{
  "site": "Rasel Dev BD",
  "generatedAt": "2026-09-27T10:30:00Z",
  "categories": ["react-static-web", "android-kotlin", "cloud-devops"],
  "stats": { "projects": 3, "blogs": 3, "photos": 3 }
}
```

### Handling Client-Side Routing on GitHub Pages
Use either hash-based routing (`createHashRouter`) or generate a `404.html` fallback that redirects SPA deep links back to `index.html` while preserving the category or post slug.
            """.trimIndent(),
            categoryId = 1L,
            categoryName = "React & Static Web",
            mediaSource = "drawable:img_hero_banner",
            tagsCsv = "React,GitHub Pages,Static Site Generation,Architecture,CMS",
            liveUrl = "6 min read",
            repoUrl = "/blog/static-first-react-cms-github-pages",
            isFeatured = true,
            isPublished = true,
            likesCount = 142,
            createdAt = now - 86400000L * 1,
            updatedAt = now - 86400000L * 1
        ),
        ContentItemEntity(
            id = 202L,
            contentType = ContentType.BLOG,
            title = "Mastering Offline-First State Sync with Room & Kotlin Flows in Jetpack Compose",
            slug = "offline-first-room-kotlin-flows-compose",
            summaryOrCaption = "Practical patterns for reactive DAO queries, derived UI states, and zero-jank filtering across dynamic content categories.",
            bodyMarkdown = """
## Single Source of Truth in Modern Android
In high-responsiveness Android apps, the UI should never mutate screen lists directly in memory. Instead, write every change to **Room** and let `Flow<List<T>>` emit the updated snapshot.

### Combining Multiple Flows Cleanly
When building a multi-faceted portfolio with dynamic categories, search queries, and content type tabs:

```kotlin
val filteredContent = combine(
    repository.publishedContent,
    selectedCategoryId,
    searchQuery
) { items, catId, query ->
    items.filter { item ->
        (catId == null || item.categoryId == catId) &&
        (query.isBlank() || item.title.contains(query, ignoreCase = true))
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

This guarantees that adding or renaming a category in the Admin Dashboard immediately updates every chip and card across the entire application.
            """.trimIndent(),
            categoryId = 2L,
            categoryName = "Android & Kotlin",
            mediaSource = "drawable:img_project_cloud",
            tagsCsv = "Kotlin,Jetpack Compose,Room Database,Coroutines,MVVM",
            liveUrl = "8 min read",
            repoUrl = "/blog/offline-first-room-kotlin-flows-compose",
            isFeatured = true,
            isPublished = true,
            likesCount = 89,
            createdAt = now - 86400000L * 4,
            updatedAt = now - 86400000L * 4
        ),
        ContentItemEntity(
            id = 203L,
            contentType = ContentType.BLOG,
            title = "Zero-Downtime CI/CD Pipelines with GitHub Actions for Full-Stack Engineers",
            slug = "zero-downtime-cicd-github-actions",
            summaryOrCaption = "Automating lint checks, unit tests, static asset optimization, and atomic GitHub Pages deployments on every main branch push.",
            bodyMarkdown = """
## Speeding Up Your Build Pipeline
Slow CI pipelines kill developer momentum. By caching Gradle and npm dependencies properly and running matrix builds in parallel, we cut our deployment time from 4 minutes down to 38 seconds.

### Key Optimization Checklist
- Enable dependency caching (`actions/cache@v4`).
- Validate JSON schemas and broken asset links before publishing to `gh-pages`.
- Generate immutable hashed filenames for instant CDN cache invalidation.
            """.trimIndent(),
            categoryId = 5L,
            categoryName = "System Architecture",
            mediaSource = "drawable:img_hero_banner",
            tagsCsv = "GitHub Actions,CI/CD,DevOps,Automation",
            liveUrl = "5 min read",
            repoUrl = "/blog/zero-downtime-cicd-github-actions",
            isFeatured = false,
            isPublished = true,
            likesCount = 64,
            createdAt = now - 86400000L * 7,
            updatedAt = now - 86400000L * 7
        ),

        // PHOTOS WITH CAPTIONS
        ContentItemEntity(
            id = 301L,
            contentType = ContentType.PHOTO,
            title = "Blue Hour Reflections Over Hatirjheel",
            slug = "blue-hour-hatirjheel-dhaka",
            summaryOrCaption = "Captured just 20 minutes after sunset as the curved bridges of Hatirjheel lit up in neon cyan and emerald against Dhaka's twilight sky.",
            bodyMarkdown = """
### Behind the Shot
After wrapping up a late afternoon sprint review in Gulshan, I walked over to the Hatirjheel overpass with a compact tripod. The monsoon clouds broke just in time to reflect the bridge lights across the calm water.

- **Location**: Hatirjheel Amphitheater Viewpoint, Dhaka, Bangladesh
- **Camera & Lens**: Sony A7 IV • FE 24mm f/1.4 GM
- **Settings**: ISO 200 • f/5.6 • 2.5s long exposure
            """.trimIndent(),
            categoryId = 4L,
            categoryName = "Dhaka Tech & Street",
            mediaSource = "drawable:img_photo_dhaka",
            tagsCsv = "Dhaka,Hatirjheel,Twilight,Long Exposure,Bangladesh",
            liveUrl = "Hatirjheel, Dhaka 🇧🇩",
            repoUrl = "24mm • f/5.6 • 2.5s • ISO 200",
            isFeatured = true,
            isPublished = true,
            likesCount = 215,
            createdAt = now - 86400000L * 3,
            updatedAt = now - 86400000L * 3
        ),
        ContentItemEntity(
            id = 302L,
            contentType = ContentType.PHOTO,
            title = "Late-Night Release Workstation in Banani",
            slug = "late-night-release-workstation-banani",
            summaryOrCaption = "Dual-monitor setup running Jetpack Compose previews on the left and Vite static bundle telemetry on the right, fueled by strong Sylheti cha.",
            bodyMarkdown = """
### Studio Setup Notes
Clean ergonomics matter during deep engineering sessions. This shot captures the exact moment v1.0 of the Rasel Dev BD static site generator passed all automated checks.

- **Location**: Home Studio, Banani, Dhaka
- **Gear**: Custom Keychron Q1 Pro (Tactile switches), LG UltraFine 4K, Pixel test device
- **Lighting**: Dual diffused RGB key lights set to Cyber Emerald (#10B981)
            """.trimIndent(),
            categoryId = 4L,
            categoryName = "Dhaka Tech & Street",
            mediaSource = "drawable:img_hero_banner",
            tagsCsv = "Workspace,Developer Setup,Dhaka,Night Coding",
            liveUrl = "Banani, Dhaka 🇧🇩",
            repoUrl = "35mm • f/1.8 • 1/60s • ISO 400",
            isFeatured = true,
            isPublished = true,
            likesCount = 167,
            createdAt = now - 86400000L * 6,
            updatedAt = now - 86400000L * 6
        ),
        ContentItemEntity(
            id = 303L,
            contentType = ContentType.PHOTO,
            title = "Cloud Architecture Whiteboard & System Design Jam",
            slug = "cloud-architecture-design-jam",
            summaryOrCaption = "Visualizing distributed caching and static edge invalidation during our weekend Dhaka JS & Kotlin community meetup.",
            bodyMarkdown = """
### Community Knowledge Sharing
Every month, local engineers in Dhaka gather to dissect real-world system design challenges—from handling sudden traffic spikes during Eid sales to building offline-resilient mobile apps.

- **Event**: Dhaka Full-Stack & Mobile Engineering Meetup
- **Topic**: Static Edge Caching & Local-First Mobile Databases
            """.trimIndent(),
            categoryId = 3L,
            categoryName = "Cloud & DevOps",
            mediaSource = "drawable:img_project_cloud",
            tagsCsv = "Community,Meetup,System Design,Dhaka Devs",
            liveUrl = "Dhanmondi, Dhaka 🇧🇩",
            repoUrl = "28mm • f/2.8 • 1/125s • ISO 320",
            isFeatured = false,
            isPublished = true,
            likesCount = 112,
            createdAt = now - 86400000L * 10,
            updatedAt = now - 86400000L * 10
        )
    )
}
