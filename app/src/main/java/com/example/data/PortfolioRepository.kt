package com.example.data

import kotlinx.coroutines.flow.Flow

class PortfolioRepository(private val dao: PortfolioDao) {

    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allContentItems: Flow<List<ContentItemEntity>> = dao.getAllContentItems()
    val publishedContentItems: Flow<List<ContentItemEntity>> = dao.getPublishedContentItems()
    val siteConfig: Flow<SiteConfigEntity?> = dao.getSiteConfig()

    suspend fun ensureSeeded() {
        val existingConfig = dao.getSiteConfigOnce()
        if (existingConfig == null) {
            dao.saveSiteConfig(SiteConfigEntity())
        }
        val catCount = dao.getCategoriesCount()
        if (catCount == 0) {
            dao.insertCategories(SeedPortfolioData.defaultCategories())
        }
        val itemCount = dao.getContentCount()
        if (itemCount == 0) {
            dao.insertContentItems(SeedPortfolioData.defaultContentItems())
        }
    }

    suspend fun saveCategory(category: CategoryEntity): Long {
        val id = dao.insertCategory(category)
        if (category.id != 0L) {
            dao.syncCategoryNameInItems(category.id, category.name)
        }
        return id
    }

    suspend fun deleteCategory(categoryId: Long) {
        dao.deleteCategoryById(categoryId)
    }

    suspend fun saveContentItem(item: ContentItemEntity): Long {
        return dao.insertContentItem(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteContentItem(itemId: Long) {
        dao.deleteContentItemById(itemId)
    }

    suspend fun togglePublished(itemId: Long, isPublished: Boolean) {
        dao.setPublishedState(itemId, isPublished)
    }

    suspend fun toggleFeatured(itemId: Long, isFeatured: Boolean) {
        dao.setFeaturedState(itemId, isFeatured)
    }

    suspend fun incrementLikes(itemId: Long) {
        dao.incrementLikes(itemId)
    }

    suspend fun saveSiteConfig(config: SiteConfigEntity) {
        dao.saveSiteConfig(config)
    }

    suspend fun importStaticBundle(jsonString: String): Result<Int> {
        return runCatching {
            val currentConfig = dao.getSiteConfigOnce() ?: SiteConfigEntity()
            val parsed = StaticSiteGenerator.parseStaticJsonBundle(
                jsonString = jsonString,
                existingPasscode = currentConfig.adminPasscode
            )
            parsed.config?.let { dao.saveSiteConfig(it) }
            if (parsed.categories.isNotEmpty()) {
                dao.insertCategories(parsed.categories)
            }
            if (parsed.items.isNotEmpty()) {
                dao.insertContentItems(parsed.items)
            }
            parsed.items.size
        }
    }
}
