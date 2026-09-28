package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ContentType
import com.example.ui.AdminSubTab
import com.example.ui.NavRoutes
import com.example.ui.PortfolioViewModel
import com.example.ui.PortfolioViewModelFactory
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CategoryEditorDialog
import com.example.ui.screens.ContentDetailScreen
import com.example.ui.screens.ContentEditorDialog
import com.example.ui.screens.ContentFeedScreen
import com.example.ui.screens.PortfolioHomeScreen
import com.example.ui.theme.RaselDevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val portfolioViewModel: PortfolioViewModel = viewModel(
                factory = PortfolioViewModelFactory(context)
            )
            val isDarkTheme by portfolioViewModel.isDarkTheme.collectAsStateWithLifecycle()

            RaselDevTheme(darkTheme = isDarkTheme) {
                RaselDevBdApp(viewModel = portfolioViewModel)
            }
        }
    }
}

private data class NavDestination(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaselDevBdApp(
    viewModel: PortfolioViewModel,
    modifier: Modifier = Modifier
) {
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allItems by viewModel.allContentItems.collectAsStateWithLifecycle()
    val publishedItems by viewModel.publishedContentItems.collectAsStateWithLifecycle()
    val auditLogs by viewModel.recentAuditLogs.collectAsStateWithLifecycle()
    val siteConfig by viewModel.siteConfig.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedContentItem by viewModel.selectedContentItem.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val adminAuthError by viewModel.adminAuthError.collectAsStateWithLifecycle()
    val activeAdminTab by viewModel.activeAdminTab.collectAsStateWithLifecycle()
    val adminContentFilterType by viewModel.adminContentFilterType.collectAsStateWithLifecycle()
    val isContentModalOpen by viewModel.isContentModalOpen.collectAsStateWithLifecycle()
    val editingContentItem by viewModel.editingContentItem.collectAsStateWithLifecycle()
    val isCategoryModalOpen by viewModel.isCategoryModalOpen.collectAsStateWithLifecycle()
    val editingCategory by viewModel.editingCategory.collectAsStateWithLifecycle()
    val isCmsSyncing by viewModel.isCmsSyncing.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val destinations = listOf(
        NavDestination(NavRoutes.ROUTE_HOME, R.string.nav_home, Icons.Default.Home, "nav_home"),
        NavDestination(NavRoutes.ROUTE_PROJECTS, R.string.nav_projects, Icons.Default.Code, "nav_projects"),
        NavDestination(NavRoutes.ROUTE_BLOG, R.string.nav_blog, Icons.AutoMirrored.Filled.Article, "nav_blog"),
        NavDestination(NavRoutes.ROUTE_PHOTOS, R.string.nav_photos, Icons.Default.CameraAlt, "nav_photos"),
        NavDestination(NavRoutes.ROUTE_ADMIN, R.string.nav_admin, Icons.Default.AdminPanelSettings, "nav_admin")
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        Scaffold(
            topBar = {
                if (currentRoute != NavRoutes.ROUTE_DETAIL) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.navigateTo(NavRoutes.ROUTE_HOME) }
                                    .testTag("top_bar_brand")
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_icon),
                                    contentDescription = "Rasel Dev BD Logo",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = siteConfig.siteTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Contentful CMA Studio • ${siteConfig.customDomain}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        actions = {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        val defaultType = when (currentRoute) {
                                            NavRoutes.ROUTE_BLOG -> ContentType.BLOG
                                            NavRoutes.ROUTE_PHOTOS -> ContentType.PHOTO
                                            else -> ContentType.PROJECT
                                        }
                                        viewModel.openCreateContentModal(defaultType)
                                    }
                                    .testTag("top_bar_cms_status_pill")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Post to Contentful CMA",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "+ Post CMA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.toggleTheme() },
                                modifier = Modifier.testTag("toggle_theme_button")
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle theme"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                if (!isWideScreen && currentRoute != NavRoutes.ROUTE_DETAIL) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = stringResource(dest.labelResId)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelResId),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentRoute != NavRoutes.ROUTE_DETAIL) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            val defaultType = when (currentRoute) {
                                NavRoutes.ROUTE_BLOG -> ContentType.BLOG
                                NavRoutes.ROUTE_PHOTOS -> ContentType.PHOTO
                                else -> ContentType.PROJECT
                            }
                            viewModel.openCreateContentModal(defaultType)
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create & Post to Contentful CMA"
                            )
                        },
                        text = {
                            Text(
                                text = "Post to Contentful",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("main_fab_button")
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen && currentRoute != NavRoutes.ROUTE_DETAIL) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("side_navigation_rail")
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = stringResource(dest.labelResId)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelResId),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("rail_${dest.testTag}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (currentRoute) {
                        NavRoutes.ROUTE_HOME -> PortfolioHomeScreen(
                            config = siteConfig,
                            categories = categories,
                            publishedItems = publishedItems,
                            selectedCategoryId = selectedCategoryId,
                            searchQuery = searchQuery,
                            isAdminUnlocked = true,
                            isCmsSyncing = isCmsSyncing,
                            onSelectCategory = viewModel::selectCategory,
                            onSearchChange = viewModel::updateSearchQuery,
                            onOpenItem = viewModel::openContentDetail,
                            onEditItem = viewModel::openEditContentModal,
                            onPublishItemToCms = viewModel::publishItemToHeadlessCms,
                            onSyncCms = viewModel::syncFromHeadlessCms,
                            onNavigateRoute = viewModel::navigateTo,
                            onOpenCmsSettings = {
                                viewModel.setAdminTab(AdminSubTab.HEADLESS_CMS)
                                viewModel.navigateTo(NavRoutes.ROUTE_ADMIN)
                            },
                            onQuickCreate = { type ->
                                viewModel.openCreateContentModal(type)
                            }
                        )

                        NavRoutes.ROUTE_PROJECTS -> ContentFeedScreen(
                            contentType = ContentType.PROJECT,
                            title = "Project Releases & Downloads",
                            subtitle = "Rich media project showcases with downloadable packages & Contentful CMA sync",
                            categories = categories,
                            publishedItems = publishedItems,
                            selectedCategoryId = selectedCategoryId,
                            searchQuery = searchQuery,
                            isAdminUnlocked = true,
                            onSelectCategory = viewModel::selectCategory,
                            onSearchChange = viewModel::updateSearchQuery,
                            onOpenItem = viewModel::openContentDetail,
                            onEditItem = viewModel::openEditContentModal,
                            onPublishItemToCms = viewModel::publishItemToHeadlessCms,
                            onCreateNewOfType = {
                                viewModel.openCreateContentModal(ContentType.PROJECT)
                            },
                            onBackToHome = { viewModel.navigateBack() }
                        )

                        NavRoutes.ROUTE_BLOG -> ContentFeedScreen(
                            contentType = ContentType.BLOG,
                            title = "Rich Media Articles & Guides",
                            subtitle = "Articles with images, descriptions, and downloadable resources via Contentful CMA",
                            categories = categories,
                            publishedItems = publishedItems,
                            selectedCategoryId = selectedCategoryId,
                            searchQuery = searchQuery,
                            isAdminUnlocked = true,
                            onSelectCategory = viewModel::selectCategory,
                            onSearchChange = viewModel::updateSearchQuery,
                            onOpenItem = viewModel::openContentDetail,
                            onEditItem = viewModel::openEditContentModal,
                            onPublishItemToCms = viewModel::publishItemToHeadlessCms,
                            onCreateNewOfType = {
                                viewModel.openCreateContentModal(ContentType.BLOG)
                            },
                            onBackToHome = { viewModel.navigateBack() }
                        )

                        NavRoutes.ROUTE_PHOTOS -> ContentFeedScreen(
                            contentType = ContentType.PHOTO,
                            title = "Visual Media & 4K Downloads",
                            subtitle = "High-res photography with captions, EXIF metadata, and direct download links",
                            categories = categories,
                            publishedItems = publishedItems,
                            selectedCategoryId = selectedCategoryId,
                            searchQuery = searchQuery,
                            isAdminUnlocked = true,
                            onSelectCategory = viewModel::selectCategory,
                            onSearchChange = viewModel::updateSearchQuery,
                            onOpenItem = viewModel::openContentDetail,
                            onEditItem = viewModel::openEditContentModal,
                            onPublishItemToCms = viewModel::publishItemToHeadlessCms,
                            onCreateNewOfType = {
                                viewModel.openCreateContentModal(ContentType.PHOTO)
                            },
                            onBackToHome = { viewModel.navigateBack() }
                        )

                        NavRoutes.ROUTE_ADMIN -> AdminDashboardScreen(
                            config = siteConfig,
                            categories = categories,
                            allItems = allItems,
                            auditLogs = auditLogs,
                            isAdminUnlocked = isAdminUnlocked,
                            authError = adminAuthError,
                            activeTab = activeAdminTab,
                            contentFilterType = adminContentFilterType,
                            isCmsSyncing = isCmsSyncing,
                            onUnlock = viewModel::unlockAdmin,
                            onLock = viewModel::lockAdmin,
                            onSelectTab = viewModel::setAdminTab,
                            onSelectContentFilter = viewModel::setAdminContentFilterType,
                            onCreateContent = viewModel::openCreateContentModal,
                            onEditContent = viewModel::openEditContentModal,
                            onTogglePublish = viewModel::togglePublishStatus,
                            onToggleFeatured = viewModel::toggleFeaturedStatus,
                            onDeleteContent = viewModel::deleteContentItem,
                            onPublishItemToCms = viewModel::publishItemToHeadlessCms,
                            onUnpublishItemInCms = viewModel::unpublishItemInHeadlessCms,
                            onSyncFromCms = viewModel::syncFromHeadlessCms,
                            onClearAuditLogs = viewModel::clearCmaAuditLogs,
                            onSaveCmsModelSettings = viewModel::saveHeadlessCmsModelSettings,
                            onCreateCategory = viewModel::openCreateCategoryModal,
                            onEditCategory = viewModel::openEditCategoryModal,
                            onDeleteCategory = viewModel::deleteCategory,
                            onSaveSiteConfig = viewModel::saveSiteConfig,
                            onGenerateJson = viewModel::generateJsonBundle,
                            onGenerateCmsClientJs = viewModel::generateCmsClientCode,
                            onGenerateReactJsx = viewModel::generateReactComponentCode,
                            onGenerateIndexHtml = viewModel::generateIndexHtmlCode,
                            onGenerateWorkflowYaml = viewModel::generateWorkflowYamlCode,
                            onMarkExported = viewModel::markExportedNow,
                            onImportJson = viewModel::importJsonBundle,
                            onResetDemoData = viewModel::resetDemoData,
                            onShowMessage = viewModel::showBanner,
                            onBackToHome = { viewModel.navigateBack() }
                        )

                        NavRoutes.ROUTE_DETAIL -> ContentDetailScreen(
                            item = selectedContentItem,
                            isAdminUnlocked = true,
                            onBack = { viewModel.navigateBack() },
                            onEdit = viewModel::openEditContentModal,
                            onPublishToCms = viewModel::publishItemToHeadlessCms,
                            onDelete = viewModel::deleteContentItem
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = statusBannerMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("status_toast_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = statusBannerMessage ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.dismissBanner() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss notification",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isContentModalOpen && editingContentItem != null) {
        ContentEditorDialog(
            initialItem = editingContentItem!!,
            categories = categories,
            activeCmsProvider = siteConfig.cmsProvider,
            onDismiss = viewModel::closeContentModal,
            onSave = viewModel::saveContentItem
        )
    }

    if (isCategoryModalOpen && editingCategory != null) {
        CategoryEditorDialog(
            initialCategory = editingCategory!!,
            onDismiss = viewModel::closeCategoryModal,
            onSave = viewModel::saveCategory
        )
    }
}
