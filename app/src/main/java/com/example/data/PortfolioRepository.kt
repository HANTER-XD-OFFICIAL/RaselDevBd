package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class PortfolioRepository(
    private val context: Context,
    private val dao: PortfolioDao
) {

    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allContentItems: Flow<List<ContentItemEntity>> = dao.getAllContentItems()
    val publishedContentItems: Flow<List<ContentItemEntity>> = dao.getPublishedContentItems()
    val recentAuditLogs: Flow<List<CmaAuditLogEntity>> = dao.getRecentAuditLogs()
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
        if (dao.getAuditLogCount() == 0) {
            dao.insertAuditLogs(InitialSeedData.defaultAuditLogs())
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

    suspend fun clearCmaAuditLogs() {
        dao.clearAuditLogs()
    }

    suspend fun syncFromHeadlessCms(): CmsSyncResult {
        val currentConfig = dao.getSiteConfigSnapshot() ?: SiteConfigEntity()
        val categories = dao.getAllCategoriesSnapshot()
        val result = HeadlessCmsClient.fetchFromCms(currentConfig, categories)

        dao.insertAuditLog(
            CmaAuditLogEntity(
                actionType = "SYNC_SPACE",
                entryTitle = "Space Sync (${currentConfig.cmsProvider})",
                cmsEntryId = currentConfig.spaceIdFromSecrets.ifBlank { "local-space" },
                endpoint = result.endpoint,
                httpStatus = result.httpStatus,
                success = result.success,
                message = result.message
            )
        )

        if (result.success && result.fetchedItems.isNotEmpty()) {
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
        val result = HeadlessCmsClient.publishItemToCms(context, currentConfig, item)

        dao.insertAuditLog(
            CmaAuditLogEntity(
                actionType = "PUBLISH_ENTRY",
                entryTitle = item.title,
                cmsEntryId = result.publishedEntryId.ifBlank { item.cmsEntryId },
                endpoint = result.endpoint,
                httpStatus = result.httpStatus,
                success = result.success,
                message = result.message
            )
        )

        if (result.success) {
            val updatedItem = item.copy(
                cmsEntryId = result.publishedEntryId.ifBlank { item.cmsEntryId },
                cmsAssetId = result.publishedAssetId.ifBlank { item.cmsAssetId },
                cmsVersion = result.publishedVersion,
                cmsStatus = CmaEntryStatus.PUBLISHED,
                cmsProvider = currentConfig.cmsProvider,
                cmsSyncedAt = System.currentTimeMillis(),
                updatedAtEpoch = System.currentTimeMillis(),
                isPublished = true
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

    suspend fun unpublishItemInHeadlessCms(item: ContentItemEntity): CmsSyncResult {
        val currentConfig = dao.getSiteConfigSnapshot() ?: SiteConfigEntity()
        val result = HeadlessCmsClient.unpublishEntryInContentful(currentConfig, item)

        dao.insertAuditLog(
            CmaAuditLogEntity(
                actionType = "UNPUBLISH_ENTRY",
                entryTitle = item.title,
                cmsEntryId = item.cmsEntryId,
                endpoint = result.endpoint,
                httpStatus = result.httpStatus,
                success = result.success,
                message = result.message
            )
        )

        if (result.success) {
            dao.insertContentItem(
                item.copy(
                    isPublished = false,
                    cmsStatus = CmaEntryStatus.DRAFT,
                    cmsVersion = result.publishedVersion,
                    updatedAtEpoch = System.currentTimeMillis()
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
        dao.clearAuditLogs()
        dao.upsertSiteConfig(SiteConfigEntity())
        dao.insertCategories(InitialSeedData.defaultCategories())
        dao.insertContentItems(InitialSeedData.defaultContentItems())
        dao.insertAuditLogs(InitialSeedData.defaultAuditLogs())
    }
}
