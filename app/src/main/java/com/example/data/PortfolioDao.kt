package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllCategoriesSnapshot(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("SELECT * FROM content_items ORDER BY isFeatured DESC, updatedAtEpoch DESC")
    fun getAllContentItems(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items ORDER BY isFeatured DESC, updatedAtEpoch DESC")
    suspend fun getAllContentItemsSnapshot(): List<ContentItemEntity>

    @Query("SELECT * FROM content_items WHERE isPublished = 1 ORDER BY isFeatured DESC, updatedAtEpoch DESC")
    fun getPublishedContentItems(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE slug = :slug LIMIT 1")
    suspend fun getContentBySlug(slug: String): ContentItemEntity?

    @Query("SELECT COUNT(*) FROM content_items")
    suspend fun getContentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItem(item: ContentItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItems(items: List<ContentItemEntity>)

    @Update
    suspend fun updateContentItem(item: ContentItemEntity)

    @Delete
    suspend fun deleteContentItem(item: ContentItemEntity)

    @Query("DELETE FROM content_items")
    suspend fun deleteAllContentItems()

    @Query("UPDATE content_items SET categoryName = :newName WHERE categoryId = :categoryId")
    suspend fun updateCategoryNameForItems(categoryId: Long, newName: String)

    @Query("SELECT * FROM cma_audit_logs ORDER BY timestampEpoch DESC LIMIT 30")
    fun getRecentAuditLogs(): Flow<List<CmaAuditLogEntity>>

    @Query("SELECT COUNT(*) FROM cma_audit_logs")
    suspend fun getAuditLogCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: CmaAuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<CmaAuditLogEntity>)

    @Query("DELETE FROM cma_audit_logs")
    suspend fun clearAuditLogs()

    @Query("SELECT * FROM site_config WHERE id = 1 LIMIT 1")
    fun getSiteConfig(): Flow<SiteConfigEntity?>

    @Query("SELECT * FROM site_config WHERE id = 1 LIMIT 1")
    suspend fun getSiteConfigSnapshot(): SiteConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSiteConfig(config: SiteConfigEntity)
}
