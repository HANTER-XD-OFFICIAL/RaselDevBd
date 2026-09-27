package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object ContentType {
    const val PROJECT = "PROJECT"
    const val BLOG = "BLOG"
    const val PHOTO = "PHOTO"
    const val ALL = "ALL"
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val slug: String,
    val contentTypeScope: String = ContentType.ALL, // ALL, PROJECT, BLOG, PHOTO
    val colorHex: String = "#10B981",
    val description: String = "",
    val displayOrder: Int = 0
)

@Entity(tableName = "content_items")
data class ContentItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentType: String, // PROJECT, BLOG, PHOTO
    val title: String,
    val slug: String,
    val summaryOrCaption: String, // Excerpt for blog, caption for photo, pitch for project
    val bodyMarkdown: String, // Full markdown post, case study, or behind-the-shot story
    val categoryId: Long,
    val categoryName: String,
    val mediaSource: String, // "drawable:img_project_cloud", content:// URI, or "/assets/..."
    val tagsCsv: String,
    val liveUrl: String = "", // Demo URL for Project, Location for Photo, Read time for Blog
    val repoUrl: String = "", // GitHub URL for Project, Camera EXIF for Photo, Canonical slug for Blog
    val isFeatured: Boolean = false,
    val isPublished: Boolean = true,
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "site_config")
data class SiteConfigEntity(
    @PrimaryKey val id: Int = 1,
    val siteTitle: String = "Rasel Dev BD",
    val tagline: String = "Full-Stack React & Android Architect • Building Cloud-Native Products from Dhaka",
    val bio: String = "Hi, I'm Rasel — a full-stack engineer and open-source creator based in Dhaka, Bangladesh. I specialize in React 19 static/SSR architectures, TypeScript design systems, and native Android apps with Jetpack Compose.",
    val githubUsername: String = "rasel-dev-bd",
    val githubPagesDomain: String = "https://rasel-dev-bd.github.io",
    val email: String = "alexraselchodhury@gmail.com",
    val location: String = "Dhaka, Bangladesh",
    val skillsCsv: String = "React 19,TypeScript,Next.js,Tailwind CSS,Kotlin,Jetpack Compose,Node.js,PostgreSQL,GitHub Actions,Docker",
    val adminPasscode: String = "2026",
    val lastStaticExportTimestamp: Long = System.currentTimeMillis()
)
