package com.example.data

import kotlinx.coroutines.flow.Flow

class PortfolioRepository(private val dao: PortfolioDao) {

    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allContentItems: Flow<List<ContentItemEntity>> = dao.getAllContentItems()
    val publishedContentItems: Flow<List<ContentItemEntity>> = dao.getPublishedContentItems()
    val siteConfig: Flow<SiteConfigEntity?> = dao.getSiteConfig()

    suspend fun ensureSeedData() {
        if (dao.getSiteConfigSnapshot() == null) {
            dao.upsertSiteConfig(SiteConfigEntity())
        }
        if (dao.getCategoryCount() == 0) {
            dao.insertCategories(InitialSeedData.defaultCategories())
        }
        if (dao.getContentCount() == 0) {
            dao.insertContentItems(InitialSeedData.defaultContentItems())
        }
    }

    suspend fun saveCategory(category: CategoryEntity): Long {
        val id = dao.insertCategory(category)
        if (category.id != 0L) {
            dao.updateCategoryNameForItems(category.id, category.name)
        }
        return id
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        dao.deleteCategory(category)
    }

    suspend fun saveContentItem(item: ContentItemEntity): Long {
        return dao.insertContentItem(item.copy(updatedAtEpoch = System.currentTimeMillis()))
    }

    suspend fun deleteContentItem(item: ContentItemEntity) {
        dao.deleteContentItem(item)
    }

    suspend fun updateSiteConfig(config: SiteConfigEntity) {
        dao.upsertSiteConfig(config)
    }

    suspend fun syncFromHeadlessCms(): CmsSyncResult {
        val currentConfig = dao.getSiteConfigSnapshot() ?: SiteConfigEntity()
        val categories = dao.getAllCategoriesSnapshot()
        val result = HeadlessCmsClient.fetchFromCms(currentConfig, categories)

        if (result.success && result.fetchedItems.isNotEmpty()) {
            // Upsert fetched items by matching slug so local items update seamlessly
            val existingItems = dao.getAllContentItemsSnapshot()
            for (remote in result.fetchedItems) {
                val match = existingItems.firstOrNull {
                    it.slug.equals(remote.slug, ignoreCase = true) ||
                        (remote.cmsEntryId.isNotBlank() && it.cmsEntryId == remote.cmsEntryId)
                }
                if (match != null) {
                    dao.insertContentItem(remote.copy(id = match.id))
                } else {
                    dao.insertContentItem(remote)
                }
            }
            dao.upsertSiteConfig(
                currentConfig.copy(
                    lastCmsSyncEpoch = System.currentTimeMillis(),
                    lastCmsSyncStatus = result.message
                )
            )
        } else {
            dao.upsertSiteConfig(
                currentConfig.copy(
                    lastCmsSyncStatus = result.message
                )
            )
        }
        return result
    }

    suspend fun publishItemToHeadlessCms(item: ContentItemEntity): CmsSyncResult {
        val currentConfig = dao.getSiteConfigSnapshot() ?: SiteConfigEntity()
        val result = HeadlessCmsClient.publishItemToCms(currentConfig, item)
        if (result.success) {
            val updatedItem = item.copy(
                cmsEntryId = result.publishedEntryId.ifBlank { item.cmsEntryId },
                cmsProvider = currentConfig.cmsProvider,
                cmsSyncedAt = System.currentTimeMillis(),
                updatedAtEpoch = System.currentTimeMillis()
            )
            dao.insertContentItem(updatedItem)
            dao.upsertSiteConfig(
                currentConfig.copy(
                    lastCmsSyncEpoch = System.currentTimeMillis(),
                    lastCmsSyncStatus = result.message
                )
            )
        }
        return result
    }

    suspend fun importStaticBundle(bundle: ParsedStaticBundle) {
        bundle.config?.let { dao.upsertSiteConfig(it) }
        if (bundle.categories.isNotEmpty()) {
            dao.deleteAllCategories()
            dao.insertCategories(bundle.categories)
        }
        if (bundle.items.isNotEmpty()) {
            dao.deleteAllContentItems()
            dao.insertContentItems(bundle.items)
        }
    }

    suspend fun resetToFactoryDemo() {
        dao.deleteAllContentItems()
        dao.deleteAllCategories()
        dao.upsertSiteConfig(SiteConfigEntity())
        dao.insertCategories(InitialSeedData.defaultCategories())
        dao.insertContentItems(InitialSeedData.defaultContentItems())
    }
}
