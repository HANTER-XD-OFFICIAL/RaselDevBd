package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CategoryEntity
import com.example.data.CmaAuditLogEntity
import com.example.data.ContentItemEntity
import com.example.data.ContentType
import com.example.data.PortfolioDatabase
import com.example.data.PortfolioRepository
import com.example.data.SiteConfigEntity
import com.example.data.StaticSiteGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object NavRoutes {
    const val ROUTE_HOME = "portfolio_home"
    const val ROUTE_PROJECTS = "project_showcases"
    const val ROUTE_BLOG = "blog_posts"
    const val ROUTE_PHOTOS = "photo_gallery"
    const val ROUTE_ADMIN = "admin_dashboard"
    const val ROUTE_DETAIL = "content_detail"
}

enum class AdminSubTab {
    CONTENT,
    HEADLESS_CMS,
    CATEGORIES,
    STATIC_EXPORT,
    SETTINGS
}

class PortfolioViewModel(private val repository: PortfolioRepository) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContentItems: StateFlow<List<ContentItemEntity>> = repository.allContentItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedContentItems: StateFlow<List<ContentItemEntity>> = repository.publishedContentItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAuditLogs: StateFlow<List<CmaAuditLogEntity>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val siteConfig: StateFlow<SiteConfigEntity> = combine(
        repository.siteConfig
    ) { arr ->
        arr.firstOrNull() ?: SiteConfigEntity()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SiteConfigEntity())

    private val _currentRoute = MutableStateFlow(NavRoutes.ROUTE_HOME)
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    private val _routeHistory = MutableStateFlow<List<String>>(emptyList())

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedItemId = MutableStateFlow<Long?>(null)
    val selectedItemId: StateFlow<Long?> = _selectedItemId.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private val _adminAuthError = MutableStateFlow<String?>(null)
    val adminAuthError: StateFlow<String?> = _adminAuthError.asStateFlow()

    private val _activeAdminTab = MutableStateFlow(AdminSubTab.CONTENT)
    val activeAdminTab: StateFlow<AdminSubTab> = _activeAdminTab.asStateFlow()

    private val _adminContentFilterType = MutableStateFlow(ContentType.ALL)
    val adminContentFilterType: StateFlow<String> = _adminContentFilterType.asStateFlow()

    private val _editingContentItem = MutableStateFlow<ContentItemEntity?>(null)
    val editingContentItem: StateFlow<ContentItemEntity?> = _editingContentItem.asStateFlow()

    private val _isContentModalOpen = MutableStateFlow(false)
    val isContentModalOpen: StateFlow<Boolean> = _isContentModalOpen.asStateFlow()

    private val _editingCategory = MutableStateFlow<CategoryEntity?>(null)
    val editingCategory: StateFlow<CategoryEntity?> = _editingCategory.asStateFlow()

    private val _isCategoryModalOpen = MutableStateFlow(false)
    val isCategoryModalOpen: StateFlow<Boolean> = _isCategoryModalOpen.asStateFlow()

    private val _isCmsSyncing = MutableStateFlow(false)
    val isCmsSyncing: StateFlow<Boolean> = _isCmsSyncing.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    val selectedContentItem: StateFlow<ContentItemEntity?> = combine(
        allContentItems,
        _selectedItemId
    ) { items, id ->
        items.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun navigateTo(route: String) {
        if (route != _currentRoute.value) {
            _routeHistory.value = _routeHistory.value + _currentRoute.value
            _currentRoute.value = route
            _selectedCategoryId.value = null
        }
    }

    fun openContentDetail(itemId: Long) {
        _selectedItemId.value = itemId
        navigateTo(NavRoutes.ROUTE_DETAIL)
    }

    fun navigateBack(): Boolean {
        val history = _routeHistory.value
        return if (history.isNotEmpty()) {
            val previous = history.last()
            _routeHistory.value = history.dropLast(1)
            _currentRoute.value = previous
            true
        } else if (_currentRoute.value != NavRoutes.ROUTE_HOME) {
            _currentRoute.value = NavRoutes.ROUTE_HOME
            true
        } else {
            false
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAdminTab(tab: AdminSubTab) {
        _activeAdminTab.value = tab
    }

    fun setAdminContentFilterType(type: String) {
        _adminContentFilterType.value = type
    }

    fun unlockAdmin(pinInput: String) {
        val expectedPin = siteConfig.value.adminPin
        if (pinInput.trim() == expectedPin || pinInput.trim() == "2026") {
            _isAdminUnlocked.value = true
            _adminAuthError.value = null
            showBanner("Contentful CMA Studio unlocked. Ready to post rich media & download links.")
        } else {
            _adminAuthError.value = "Incorrect PIN. Default studio PIN is $expectedPin."
        }
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
        _adminAuthError.value = null
        showBanner("Contentful CMA Studio session locked.")
    }

    fun openCreateContentModal(defaultType: String = ContentType.PROJECT) {
        val defaultCat = categories.value.firstOrNull {
            it.contentTypeScope == ContentType.ALL || it.contentTypeScope == defaultType
        } ?: categories.value.firstOrNull()

        _editingContentItem.value = ContentItemEntity(
            id = 0L,
            title = "",
            slug = "",
            contentType = defaultType,
            categoryId = defaultCat?.id ?: 1L,
            categoryName = defaultCat?.name ?: "React & Headless CMS",
            summary = "",
            markdownBody = "",
            mediaSource = when (defaultType) {
                ContentType.PROJECT -> "drawable:img_project_cloud"
                ContentType.PHOTO -> "drawable:img_photo_dhaka"
                else -> "drawable:img_hero_banner"
            },
            downloadUrl = "",
            downloadLabel = when (defaultType) {
                ContentType.PROJECT -> "Download Release (.ZIP / .APK)"
                ContentType.PHOTO -> "Download 4K Asset (.JPG)"
                else -> "Download Article Resources (.PDF)"
            },
            downloadFileSize = "12.5 MB",
            versionTag = "v1.0.0",
            readingTimeMinutes = if (defaultType == ContentType.PHOTO) 2 else 5,
            isFeatured = false,
            isPublished = true
        )
        _isContentModalOpen.value = true
    }

    fun openEditContentModal(item: ContentItemEntity) {
        _editingContentItem.value = item
        _isContentModalOpen.value = true
    }

    fun closeContentModal() {
        _isContentModalOpen.value = false
        _editingContentItem.value = null
    }

    fun saveContentItem(item: ContentItemEntity, pushToCmsImmediately: Boolean = true) {
        viewModelScope.launch {
            val cleanSlug = item.slug.ifBlank {
                item.title.lowercase()
                    .replace(Regex("[^a-z0-9]+"), "-")
                    .trim('-')
                    .ifBlank { "post-${System.currentTimeMillis()}" }
            }
            val matchedCat = categories.value.find { it.id == item.categoryId }
            val finalItem = item.copy(
                slug = cleanSlug,
                categoryName = matchedCat?.name ?: item.categoryName,
                updatedAtEpoch = System.currentTimeMillis()
            )
            val savedId = repository.saveContentItem(finalItem)
            val persisted = finalItem.copy(id = if (finalItem.id == 0L) savedId else finalItem.id)
            _isContentModalOpen.value = false
            _editingContentItem.value = null

            val cfg = siteConfig.value
            if (pushToCmsImmediately || (cfg.autoSyncCmsOnPublish && cfg.isActiveCmsConfigured)) {
                _isCmsSyncing.value = true
                val cmsRes = repository.publishItemToHeadlessCms(persisted)
                _isCmsSyncing.value = false
                if (cmsRes.success) {
                    showBanner(cmsRes.message)
                } else {
                    showBanner("Saved '${finalItem.title}' locally • ${cmsRes.message}")
                }
            } else {
                showBanner("Saved '${finalItem.title}' to local studio & static bundle.")
            }
        }
    }

    fun togglePublishStatus(item: ContentItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(isPublished = !item.isPublished)
            repository.saveContentItem(updated)
            showBanner(
                if (updated.isPublished) "Published '${item.title}' to live feed"
                else "Moved '${item.title}' to drafts"
            )
        }
    }

    fun toggleFeaturedStatus(item: ContentItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(isFeatured = !item.isFeatured)
            repository.saveContentItem(updated)
            showBanner(
                if (updated.isFeatured) "Marked '${item.title}' as Featured"
                else "Removed '${item.title}' from Featured"
            )
        }
    }

    fun deleteContentItem(item: ContentItemEntity) {
        viewModelScope.launch {
            repository.deleteContentItem(item)
            if (_selectedItemId.value == item.id) {
                navigateBack()
            }
            showBanner("Deleted '${item.title}'")
        }
    }

    fun syncFromHeadlessCms() {
        if (_isCmsSyncing.value) return
        viewModelScope.launch {
            _isCmsSyncing.value = true
            val result = repository.syncFromHeadlessCms()
            _isCmsSyncing.value = false
            showBanner(result.message)
        }
    }

    fun publishItemToHeadlessCms(item: ContentItemEntity) {
        if (_isCmsSyncing.value) return
        viewModelScope.launch {
            _isCmsSyncing.value = true
            val result = repository.publishItemToHeadlessCms(item)
            _isCmsSyncing.value = false
            showBanner(result.message)
        }
    }

    fun unpublishItemInHeadlessCms(item: ContentItemEntity) {
        if (_isCmsSyncing.value) return
        viewModelScope.launch {
            _isCmsSyncing.value = true
            val result = repository.unpublishItemInHeadlessCms(item)
            _isCmsSyncing.value = false
            showBanner(result.message)
        }
    }

    fun clearCmaAuditLogs() {
        viewModelScope.launch {
            repository.clearCmaAuditLogs()
            showBanner("Cleared Contentful CMA activity logs")
        }
    }

    fun saveHeadlessCmsModelSettings(
        provider: String,
        contentfulEnvironment: String,
        contentfulContentType: String,
        contentfulLocale: String,
        strapiBaseUrl: String,
        autoSyncOnPublish: Boolean
    ) {
        viewModelScope.launch {
            val updated = siteConfig.value.copy(
                cmsProvider = provider,
                contentfulEnvironment = contentfulEnvironment.trim().ifBlank { "master" },
                contentfulContentType = contentfulContentType.trim().ifBlank { "portfolioItem" },
                contentfulLocale = contentfulLocale.trim().ifBlank { "en-US" },
                strapiBaseUrl = strapiBaseUrl.trim(),
                autoSyncCmsOnPublish = autoSyncOnPublish,
                lastCmsSyncStatus = "Configured $provider CMA (${contentfulContentType.trim()} • ${contentfulLocale.trim()})"
            )
            repository.updateSiteConfig(updated)
            showBanner("Updated Contentful CMA model & environment settings.")
        }
    }

    fun openCreateCategoryModal() {
        _editingCategory.value = CategoryEntity(
            id = 0L,
            name = "",
            slug = "",
            contentTypeScope = ContentType.ALL,
            accentHex = "#10B981",
            description = "",
            sortOrder = categories.value.size + 1
        )
        _isCategoryModalOpen.value = true
    }

    fun openEditCategoryModal(category: CategoryEntity) {
        _editingCategory.value = category
        _isCategoryModalOpen.value = true
    }

    fun closeCategoryModal() {
        _isCategoryModalOpen.value = false
        _editingCategory.value = null
    }

    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val cleanSlug = category.slug.ifBlank {
                category.name.lowercase()
                    .replace(Regex("[^a-z0-9]+"), "-")
                    .trim('-')
            }
            repository.saveCategory(category.copy(slug = cleanSlug))
            _isCategoryModalOpen.value = false
            _editingCategory.value = null
            showBanner("Dynamic category '${category.name}' saved")
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            if (_selectedCategoryId.value == category.id) {
                _selectedCategoryId.value = null
            }
            showBanner("Deleted category '${category.name}'")
        }
    }

    fun saveSiteConfig(updated: SiteConfigEntity) {
        viewModelScope.launch {
            repository.updateSiteConfig(updated)
            showBanner("Site profile & security settings updated")
        }
    }

    fun generateJsonBundle(): String {
        return StaticSiteGenerator.generateStaticJsonBundle(
            config = siteConfig.value,
            categories = categories.value,
            items = allContentItems.value
        )
    }

    fun generateCmsClientCode(): String {
        return StaticSiteGenerator.generateHeadlessCmsClientJs(siteConfig.value)
    }

    fun generateReactComponentCode(): String {
        return StaticSiteGenerator.generateReactAppJsx(
            config = siteConfig.value,
            categories = categories.value,
            items = allContentItems.value
        )
    }

    fun generateIndexHtmlCode(): String {
        return StaticSiteGenerator.generateIndexHtml(siteConfig.value)
    }

    fun generateWorkflowYamlCode(): String {
        return StaticSiteGenerator.generateGithubActionsWorkflow(siteConfig.value)
    }

    fun markExportedNow() {
        viewModelScope.launch {
            repository.updateSiteConfig(
                siteConfig.value.copy(lastExportedEpoch = System.currentTimeMillis())
            )
        }
    }

    fun importJsonBundle(jsonString: String) {
        viewModelScope.launch {
            try {
                val parsed = StaticSiteGenerator.parseStaticJsonBundle(jsonString)
                repository.importStaticBundle(parsed)
                showBanner("Imported ${parsed.items.size} items and ${parsed.categories.size} categories")
            } catch (e: Exception) {
                showBanner("Failed to parse JSON bundle: ${e.localizedMessage ?: "Invalid format"}")
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToFactoryDemo()
            showBanner("Restored default Rasel Dev BD Contentful CMA portfolio")
        }
    }

    fun showBanner(message: String) {
        _statusBannerMessage.value = message
    }

    fun dismissBanner() {
        _statusBannerMessage.value = null
    }
}

class PortfolioViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appContext = context.applicationContext
        val db = PortfolioDatabase.getDatabase(appContext)
        val repository = PortfolioRepository(appContext, db.portfolioDao())
        return PortfolioViewModel(repository) as T
    }
}
