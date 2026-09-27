package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CategoryEntity
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

object AppRoutes {
    const val ROUTE_HOME = "portfolio_home"
    const val ROUTE_PROJECTS = "project_showcases"
    const val ROUTE_BLOG = "blog_posts"
    const val ROUTE_PHOTOS = "photo_gallery"
    const val ROUTE_ADMIN = "admin_dashboard"
}

enum class AdminTab {
    CONTENT,
    CATEGORIES,
    GITHUB_PAGES,
    SETTINGS
}

data class PortfolioUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val allItems: List<ContentItemEntity> = emptyList(),
    val publishedItems: List<ContentItemEntity> = emptyList(),
    val siteConfig: SiteConfigEntity = SiteConfigEntity(),
    val currentRoute: String = AppRoutes.ROUTE_HOME,
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val selectedDetailItem: ContentItemEntity? = null,
    val isAdminUnlocked: Boolean = false,
    val adminAuthError: String? = null,
    val activeAdminTab: AdminTab = AdminTab.CONTENT,
    val adminTypeFilter: String = ContentType.ALL,
    val editingContentItem: ContentItemEntity? = null,
    val isContentEditorOpen: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val isCategoryEditorOpen: Boolean = false,
    val isDarkTheme: Boolean = true,
    val statusBannerMessage: String? = null
)

private data class BaseDataSnapshot(
    val categories: List<CategoryEntity>,
    val allItems: List<ContentItemEntity>,
    val publishedItems: List<ContentItemEntity>,
    val siteConfig: SiteConfigEntity
)

private data class UiControlState(
    val currentRoute: String = AppRoutes.ROUTE_HOME,
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val selectedDetailItemId: Long? = null,
    val isAdminUnlocked: Boolean = false,
    val adminAuthError: String? = null,
    val activeAdminTab: AdminTab = AdminTab.CONTENT,
    val adminTypeFilter: String = ContentType.ALL,
    val editingContentItem: ContentItemEntity? = null,
    val isContentEditorOpen: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val isCategoryEditorOpen: Boolean = false,
    val isDarkTheme: Boolean = true,
    val statusBannerMessage: String? = null
)

class PortfolioViewModel(
    private val repository: PortfolioRepository
) : ViewModel() {

    private val _controls = MutableStateFlow(UiControlState())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private val baseDataFlow = combine(
        repository.allCategories,
        repository.allContentItems,
        repository.publishedContentItems,
        repository.siteConfig
    ) { categories, allItems, publishedItems, siteConfig ->
        BaseDataSnapshot(
            categories = categories,
            allItems = allItems,
            publishedItems = publishedItems,
            siteConfig = siteConfig ?: SiteConfigEntity()
        )
    }

    val uiState: StateFlow<PortfolioUiState> = combine(
        baseDataFlow,
        _controls
    ) { data, ctrl ->
        val detailItem = ctrl.selectedDetailItemId?.let { id ->
            data.allItems.find { it.id == id }
        }
        PortfolioUiState(
            categories = data.categories,
            allItems = data.allItems,
            publishedItems = data.publishedItems,
            siteConfig = data.siteConfig,
            currentRoute = ctrl.currentRoute,
            selectedCategoryId = ctrl.selectedCategoryId,
            searchQuery = ctrl.searchQuery,
            selectedDetailItem = detailItem,
            isAdminUnlocked = ctrl.isAdminUnlocked,
            adminAuthError = ctrl.adminAuthError,
            activeAdminTab = ctrl.activeAdminTab,
            adminTypeFilter = ctrl.adminTypeFilter,
            editingContentItem = ctrl.editingContentItem,
            isContentEditorOpen = ctrl.isContentEditorOpen,
            editingCategory = ctrl.editingCategory,
            isCategoryEditorOpen = ctrl.isCategoryEditorOpen,
            isDarkTheme = ctrl.isDarkTheme,
            statusBannerMessage = ctrl.statusBannerMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PortfolioUiState()
    )

    fun navigateTo(route: String) {
        _controls.value = _controls.value.copy(
            currentRoute = route,
            selectedDetailItemId = null
        )
    }

    fun selectCategory(categoryId: Long?) {
        _controls.value = _controls.value.copy(selectedCategoryId = categoryId)
    }

    fun updateSearchQuery(query: String) {
        _controls.value = _controls.value.copy(searchQuery = query)
    }

    fun openDetailItem(item: ContentItemEntity) {
        _controls.value = _controls.value.copy(selectedDetailItemId = item.id)
    }

    fun closeDetailItem() {
        _controls.value = _controls.value.copy(selectedDetailItemId = null)
    }

    fun toggleTheme() {
        _controls.value = _controls.value.copy(isDarkTheme = !_controls.value.isDarkTheme)
    }

    fun dismissStatusMessage() {
        _controls.value = _controls.value.copy(statusBannerMessage = null)
    }

    fun showStatusMessage(msg: String) {
        _controls.value = _controls.value.copy(statusBannerMessage = msg)
    }

    // Likes
    fun likeItem(itemId: Long) {
        viewModelScope.launch {
            repository.incrementLikes(itemId)
        }
    }

    // Admin Security
    fun attemptAdminUnlock(enteredPin: String) {
        val expectedPin = uiState.value.siteConfig.adminPasscode
        if (enteredPin.trim() == expectedPin.trim()) {
            _controls.value = _controls.value.copy(
                isAdminUnlocked = true,
                adminAuthError = null,
                statusBannerMessage = "Admin Studio unlocked"
            )
        } else {
            _controls.value = _controls.value.copy(
                adminAuthError = "Invalid passcode. Hint: Default PIN is ${expectedPin}"
            )
        }
    }

    fun lockAdminSession() {
        _controls.value = _controls.value.copy(
            isAdminUnlocked = false,
            adminAuthError = null,
            isContentEditorOpen = false,
            isCategoryEditorOpen = false,
            statusBannerMessage = "Admin Studio session locked"
        )
    }

    fun selectAdminTab(tab: AdminTab) {
        _controls.value = _controls.value.copy(activeAdminTab = tab)
    }

    fun setAdminTypeFilter(type: String) {
        _controls.value = _controls.value.copy(adminTypeFilter = type)
    }

    // Content CRUD
    fun openNewContentEditor(defaultType: String = ContentType.BLOG) {
        val cats = uiState.value.categories
        val defaultCat = cats.firstOrNull {
            it.contentTypeScope == ContentType.ALL || it.contentTypeScope == defaultType
        } ?: cats.firstOrNull()

        val defaultMedia = when (defaultType) {
            ContentType.PROJECT -> "drawable:img_project_cloud"
            ContentType.PHOTO -> "drawable:img_photo_dhaka"
            else -> "drawable:img_hero_banner"
        }

        val blankItem = ContentItemEntity(
            id = 0L,
            contentType = defaultType,
            title = "",
            slug = "",
            summaryOrCaption = "",
            bodyMarkdown = "",
            categoryId = defaultCat?.id ?: 1L,
            categoryName = defaultCat?.name ?: "General",
            mediaSource = defaultMedia,
            tagsCsv = "React,TypeScript,GitHub Pages",
            liveUrl = if (defaultType == ContentType.BLOG) "5 min read" else if (defaultType == ContentType.PHOTO) "Dhaka, Bangladesh" else "https://rasel-dev-bd.github.io",
            repoUrl = if (defaultType == ContentType.PHOTO) "35mm • f/1.8 • ISO 200" else "https://github.com/rasel-dev-bd",
            isFeatured = false,
            isPublished = true,
            likesCount = 1
        )
        _controls.value = _controls.value.copy(
            editingContentItem = blankItem,
            isContentEditorOpen = true
        )
    }

    fun openEditContentEditor(item: ContentItemEntity) {
        _controls.value = _controls.value.copy(
            editingContentItem = item,
            isContentEditorOpen = true
        )
    }

    fun closeContentEditor() {
        _controls.value = _controls.value.copy(
            editingContentItem = null,
            isContentEditorOpen = false
        )
    }

    fun saveContentItem(item: ContentItemEntity) {
        viewModelScope.launch {
            val cleanSlug = if (item.slug.isBlank()) StaticSiteGenerator.toSlug(item.title) else StaticSiteGenerator.toSlug(item.slug)
            val categoryObj = uiState.value.categories.find { it.id == item.categoryId }
            val finalItem = item.copy(
                slug = cleanSlug,
                categoryName = categoryObj?.name ?: item.categoryName.ifBlank { "General" },
                updatedAt = System.currentTimeMillis()
            )
            repository.saveContentItem(finalItem)
            _controls.value = _controls.value.copy(
                editingContentItem = null,
                isContentEditorOpen = false,
                statusBannerMessage = "Saved \"${finalItem.title}\" (${finalItem.contentType})"
            )
        }
    }

    fun deleteContentItem(item: ContentItemEntity) {
        viewModelScope.launch {
            repository.deleteContentItem(item.id)
            _controls.value = _controls.value.copy(
                selectedDetailItemId = if (_controls.value.selectedDetailItemId == item.id) null else _controls.value.selectedDetailItemId,
                statusBannerMessage = "Deleted \"${item.title}\""
            )
        }
    }

    fun toggleItemPublished(item: ContentItemEntity) {
        viewModelScope.launch {
            val next = !item.isPublished
            repository.togglePublished(item.id, next)
            showStatusMessage(
                if (next) "Published \"${item.title}\" to live portfolio & static bundle"
                else "Moved \"${item.title}\" to Drafts"
            )
        }
    }

    fun toggleItemFeatured(item: ContentItemEntity) {
        viewModelScope.launch {
            val next = !item.isFeatured
            repository.toggleFeatured(item.id, next)
            showStatusMessage(
                if (next) "Marked \"${item.title}\" as Featured"
                else "Removed Featured badge from \"${item.title}\""
            )
        }
    }

    // Category CRUD
    fun openNewCategoryEditor() {
        val nextOrder = (uiState.value.categories.maxOfOrNull { it.displayOrder } ?: 0) + 1
        val blankCat = CategoryEntity(
            id = 0L,
            name = "",
            slug = "",
            contentTypeScope = ContentType.ALL,
            colorHex = "#10B981",
            description = "",
            displayOrder = nextOrder
        )
        _controls.value = _controls.value.copy(
            editingCategory = blankCat,
            isCategoryEditorOpen = true
        )
    }

    fun openEditCategoryEditor(category: CategoryEntity) {
        _controls.value = _controls.value.copy(
            editingCategory = category,
            isCategoryEditorOpen = true
        )
    }

    fun closeCategoryEditor() {
        _controls.value = _controls.value.copy(
            editingCategory = null,
            isCategoryEditorOpen = false
        )
    }

    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val cleanSlug = if (category.slug.isBlank()) StaticSiteGenerator.toSlug(category.name) else StaticSiteGenerator.toSlug(category.slug)
            repository.saveCategory(category.copy(slug = cleanSlug))
            _controls.value = _controls.value.copy(
                editingCategory = null,
                isCategoryEditorOpen = false,
                statusBannerMessage = "Saved category \"${category.name}\""
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category.id)
            if (_controls.value.selectedCategoryId == category.id) {
                _controls.value = _controls.value.copy(selectedCategoryId = null)
            }
            showStatusMessage("Deleted category \"${category.name}\"")
        }
    }

    // Site Config & Static Site Import
    fun saveSiteConfig(newConfig: SiteConfigEntity) {
        viewModelScope.launch {
            repository.saveSiteConfig(newConfig.copy(lastStaticExportTimestamp = System.currentTimeMillis()))
            showStatusMessage("Updated portfolio settings & credentials")
        }
    }

    fun importStaticJsonBundle(jsonString: String) {
        viewModelScope.launch {
            val result = repository.importStaticBundle(jsonString)
            result.onSuccess { count ->
                showStatusMessage("Imported static bundle ($count items synced)")
            }.onFailure { err ->
                showStatusMessage("Invalid JSON bundle: ${err.localizedMessage ?: "Parse error"}")
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = PortfolioDatabase.getInstance(context)
                    val repo = PortfolioRepository(db.portfolioDao())
                    return PortfolioViewModel(repo) as T
                }
            }
        }
    }
}
