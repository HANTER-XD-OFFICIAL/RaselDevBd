package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ContentType
import com.example.ui.AdminTab
import com.example.ui.AppRoutes
import com.example.ui.PortfolioUiState
import com.example.ui.PortfolioViewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CategoryEditorModal
import com.example.ui.screens.ContentDetailScreen
import com.example.ui.screens.ContentEditorModal
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
                factory = PortfolioViewModel.provideFactory(context)
            )
            val uiState by portfolioViewModel.uiState.collectAsStateWithLifecycle()

            RaselDevTheme(darkTheme = uiState.isDarkTheme) {
                RaselDevPortfolioApp(
                    uiState = uiState,
                    viewModel = portfolioViewModel
                )
            }
        }
    }
}

private data class NavDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaselDevPortfolioApp(
    uiState: PortfolioUiState,
    viewModel: PortfolioViewModel
) {
    val navDestinations = listOf(
        NavDestination(
            route = AppRoutes.ROUTE_HOME,
            labelRes = R.string.nav_home,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            testTag = "nav_portfolio_home"
        ),
        NavDestination(
            route = AppRoutes.ROUTE_PROJECTS,
            labelRes = R.string.nav_projects,
            selectedIcon = Icons.Filled.Code,
            unselectedIcon = Icons.Outlined.Code,
            testTag = "nav_projects"
        ),
        NavDestination(
            route = AppRoutes.ROUTE_BLOG,
            labelRes = R.string.nav_blog,
            selectedIcon = Icons.AutoMirrored.Filled.Article,
            unselectedIcon = Icons.AutoMirrored.Filled.Article,
            testTag = "nav_blog"
        ),
        NavDestination(
            route = AppRoutes.ROUTE_PHOTOS,
            labelRes = R.string.nav_photos,
            selectedIcon = Icons.Filled.CameraAlt,
            unselectedIcon = Icons.Outlined.CameraAlt,
            testTag = "nav_photos"
        ),
        NavDestination(
            route = AppRoutes.ROUTE_ADMIN,
            labelRes = R.string.nav_admin,
            selectedIcon = Icons.Filled.AdminPanelSettings,
            unselectedIcon = Icons.Outlined.AdminPanelSettings,
            testTag = "nav_admin"
        )
    )

    // Full-screen Modal Priority:
    // 1. Content Editor Modal
    if (uiState.isContentEditorOpen && uiState.editingContentItem != null) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            ContentEditorModal(
                initialItem = uiState.editingContentItem,
                categories = uiState.categories,
                onSave = { viewModel.saveContentItem(it) },
                onCancel = { viewModel.closeContentEditor() },
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    // 2. Category Editor Modal
    if (uiState.isCategoryEditorOpen && uiState.editingCategory != null) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            CategoryEditorModal(
                initialCategory = uiState.editingCategory,
                onSave = { viewModel.saveCategory(it) },
                onCancel = { viewModel.closeCategoryEditor() },
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    // 3. Content Detail Screen
    if (uiState.selectedDetailItem != null) {
        val item = uiState.selectedDetailItem
        val catColor = uiState.categories.find { it.id == item.categoryId }?.colorHex ?: "#10B981"
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            ContentDetailScreen(
                item = item,
                categoryColorHex = catColor,
                isAdminUnlocked = uiState.isAdminUnlocked,
                onBack = { viewModel.closeDetailItem() },
                onLike = { viewModel.likeItem(item.id) },
                onEditInAdmin = {
                    if (uiState.isAdminUnlocked) {
                        viewModel.openEditContentEditor(item)
                    } else {
                        viewModel.closeDetailItem()
                        viewModel.navigateTo(AppRoutes.ROUTE_ADMIN)
                        viewModel.showStatusMessage("Unlock Admin Studio (PIN: ${uiState.siteConfig.adminPasscode}) to edit content")
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    // 4. Main Adaptive Portfolio + Admin Shell
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppRoutes.ROUTE_HOME) }
                                .testTag("top_bar_brand")
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "<R/>",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = uiState.siteConfig.siteTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (uiState.isAdminUnlocked) "Admin Unlocked • SSG Ready" else "Developer Portfolio & CMS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (uiState.isAdminUnlocked) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark/Light Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        navDestinations.forEach { dest ->
                            val selected = uiState.currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("side_navigation_rail")
                    ) {
                        navDestinations.forEach { dest ->
                            val selected = uiState.currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (uiState.currentRoute) {
                        AppRoutes.ROUTE_HOME -> {
                            PortfolioHomeScreen(
                                siteConfig = uiState.siteConfig,
                                categories = uiState.categories,
                                publishedItems = uiState.publishedItems,
                                selectedCategoryId = uiState.selectedCategoryId,
                                searchQuery = uiState.searchQuery,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onOpenItem = { viewModel.openDetailItem(it) },
                                onLikeItem = { viewModel.likeItem(it) },
                                onNavigateRoute = { viewModel.navigateTo(it) },
                                onOpenAdminTab = { viewModel.selectAdminTab(it) }
                            )
                        }
                        AppRoutes.ROUTE_PROJECTS -> {
                            ContentFeedScreen(
                                contentType = ContentType.PROJECT,
                                title = "Project Showcases",
                                subtitle = "Full-stack React, Cloud, and Native Android engineering case studies",
                                categories = uiState.categories,
                                publishedItems = uiState.publishedItems,
                                selectedCategoryId = uiState.selectedCategoryId,
                                searchQuery = uiState.searchQuery,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onOpenItem = { viewModel.openDetailItem(it) },
                                onLikeItem = { viewModel.likeItem(it) },
                                onQuickAddInAdmin = { type ->
                                    if (uiState.isAdminUnlocked) {
                                        viewModel.openNewContentEditor(type)
                                    } else {
                                        viewModel.navigateTo(AppRoutes.ROUTE_ADMIN)
                                        viewModel.showStatusMessage("Unlock Admin Studio to add a new Project Showcase")
                                    }
                                },
                                onBackToHome = { viewModel.navigateTo(AppRoutes.ROUTE_HOME) }
                            )
                        }
                        AppRoutes.ROUTE_BLOG -> {
                            ContentFeedScreen(
                                contentType = ContentType.BLOG,
                                title = "Engineering Blog",
                                subtitle = "Technical deep dives on React SSG, Jetpack Compose, and System Design",
                                categories = uiState.categories,
                                publishedItems = uiState.publishedItems,
                                selectedCategoryId = uiState.selectedCategoryId,
                                searchQuery = uiState.searchQuery,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onOpenItem = { viewModel.openDetailItem(it) },
                                onLikeItem = { viewModel.likeItem(it) },
                                onQuickAddInAdmin = { type ->
                                    if (uiState.isAdminUnlocked) {
                                        viewModel.openNewContentEditor(type)
                                    } else {
                                        viewModel.navigateTo(AppRoutes.ROUTE_ADMIN)
                                        viewModel.showStatusMessage("Unlock Admin Studio to write a new Blog Post")
                                    }
                                },
                                onBackToHome = { viewModel.navigateTo(AppRoutes.ROUTE_HOME) }
                            )
                        }
                        AppRoutes.ROUTE_PHOTOS -> {
                            ContentFeedScreen(
                                contentType = ContentType.PHOTO,
                                title = "Photos & Captions",
                                subtitle = "Visual stories, Dhaka twilight photography, and behind-the-shot EXIF notes",
                                categories = uiState.categories,
                                publishedItems = uiState.publishedItems,
                                selectedCategoryId = uiState.selectedCategoryId,
                                searchQuery = uiState.searchQuery,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onOpenItem = { viewModel.openDetailItem(it) },
                                onLikeItem = { viewModel.likeItem(it) },
                                onQuickAddInAdmin = { type ->
                                    if (uiState.isAdminUnlocked) {
                                        viewModel.openNewContentEditor(type)
                                    } else {
                                        viewModel.navigateTo(AppRoutes.ROUTE_ADMIN)
                                        viewModel.showStatusMessage("Unlock Admin Studio to upload a new Photo with Caption")
                                    }
                                },
                                onBackToHome = { viewModel.navigateTo(AppRoutes.ROUTE_HOME) }
                            )
                        }
                        AppRoutes.ROUTE_ADMIN -> {
                            AdminDashboardScreen(
                                siteConfig = uiState.siteConfig,
                                categories = uiState.categories,
                                allItems = uiState.allItems,
                                publishedItems = uiState.publishedItems,
                                isAdminUnlocked = uiState.isAdminUnlocked,
                                adminAuthError = uiState.adminAuthError,
                                activeAdminTab = uiState.activeAdminTab,
                                adminTypeFilter = uiState.adminTypeFilter,
                                onUnlockAttempt = { viewModel.attemptAdminUnlock(it) },
                                onLockSession = { viewModel.lockAdminSession() },
                                onSelectAdminTab = { viewModel.selectAdminTab(it) },
                                onSetAdminTypeFilter = { viewModel.setAdminTypeFilter(it) },
                                onOpenNewContent = { viewModel.openNewContentEditor(it) },
                                onOpenEditContent = { viewModel.openEditContentEditor(it) },
                                onDeleteContent = { viewModel.deleteContentItem(it) },
                                onTogglePublished = { viewModel.toggleItemPublished(it) },
                                onToggleFeatured = { viewModel.toggleItemFeatured(it) },
                                onOpenNewCategory = { viewModel.openNewCategoryEditor() },
                                onOpenEditCategory = { viewModel.openEditCategoryEditor(it) },
                                onDeleteCategory = { viewModel.deleteCategory(it) },
                                onSaveSiteConfig = { viewModel.saveSiteConfig(it) },
                                onImportStaticBundle = { viewModel.importStaticJsonBundle(it) },
                                onShowStatus = { viewModel.showStatusMessage(it) },
                                onBackToHome = { viewModel.navigateTo(AppRoutes.ROUTE_HOME) }
                            )
                        }
                    }

                    // Floating Status Toast Banner
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.statusBannerMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        uiState.statusBannerMessage?.let { message ->
                            Surface(
                                color = Color(0xFF064E3B),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                shape = RoundedCornerShape(14.dp),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("status_toast_banner")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.dismissStatusMessage() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = Color.White,
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
    }
}
