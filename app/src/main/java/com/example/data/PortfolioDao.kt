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

    // Categories
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoriesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    // Content Items
    @Query("SELECT * FROM content_items ORDER BY isFeatured DESC, createdAt DESC")
    fun getAllContentItems(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE isPublished = 1 ORDER BY isFeatured DESC, createdAt DESC")
    fun getPublishedContentItems(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE contentType = :type AND isPublished = 1 ORDER BY isFeatured DESC, createdAt DESC")
    fun getPublishedByType(type: String): Flow<List<ContentItemEntity>>

    @Query("SELECT COUNT(*) FROM content_items")
    suspend fun getContentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItem(item: ContentItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItems(items: List<ContentItemEntity>)

    @Update
    suspend fun updateContentItem(item: ContentItemEntity)

    @Query("DELETE FROM content_items WHERE id = :itemId")
    suspend fun deleteContentItemById(itemId: Long)

    @Query("UPDATE content_items SET likesCount = likesCount + 1 WHERE id = :itemId")
    suspend fun incrementLikes(itemId: Long)

    @Query("UPDATE content_items SET isPublished = :published, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun setPublishedState(itemId: Long, published: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE content_items SET isFeatured = :featured, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun setFeaturedState(itemId: Long, featured: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE content_items SET categoryName = :newName WHERE categoryId = :categoryId")
    suspend fun syncCategoryNameInItems(categoryId: Long, newName: String)

    // Site Config
    @Query("SELECT * FROM site_config WHERE id = 1 LIMIT 1")
    fun getSiteConfig(): Flow<SiteConfigEntity?>

    @Query("SELECT * FROM site_config WHERE id = 1 LIMIT 1")
    suspend fun getSiteConfigOnce(): SiteConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSiteConfig(config: SiteConfigEntity)
}
