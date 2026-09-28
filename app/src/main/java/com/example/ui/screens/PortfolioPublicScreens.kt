package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CategoryEntity
import com.example.data.ContentItemEntity
import com.example.data.ContentType
import com.example.data.SiteConfigEntity
import com.example.ui.AdminSubTab
import com.example.ui.NavRoutes
import com.example.ui.components.ContentItemCard
import com.example.ui.components.DynamicCategoryFilterBar
import com.example.ui.components.PortfolioMediaImage
import com.example.ui.components.parseHexColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortfolioHomeScreen(
    config: SiteConfigEntity,
    categories: List<CategoryEntity>,
    publishedItems: List<ContentItemEntity>,
    selectedCategoryId: Long?,
    searchQuery: String,
    isAdminUnlocked: Boolean,
    isCmsSyncing: Boolean,
    onSelectCategory: (Long?) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenItem: (Long) -> Unit,
    onEditItem: (ContentItemEntity) -> Unit,
    onPublishItemToCms: (ContentItemEntity) -> Unit,
    onSyncCms: () -> Unit,
    onNavigateRoute: (String) -> Unit,
    onOpenCmsSettings: () -> Unit,
    onQuickCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredItems = publishedItems.filter { item ->
        val matchesCat = selectedCategoryId == null || item.categoryId == selectedCategoryId
        val matchesQuery = searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.summary.contains(searchQuery, ignoreCase = true) ||
            item.techStackCsv.contains(searchQuery, ignoreCase = true) ||
            item.photoCaption.contains(searchQuery, ignoreCase = true) ||
            item.categoryName.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesQuery
    }

    val projectsCount = publishedItems.count { it.contentType == ContentType.PROJECT }
    val blogsCount = publishedItems.count { it.contentType == ContentType.BLOG }
    val photosCount = publishedItems.count { it.contentType == ContentType.PHOTO }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("portfolio_home_screen")
    ) {
        item {
            DeveloperHeroSection(
                config = config,
                projectsCount = projectsCount,
                blogsCount = blogsCount,
                photosCount = photosCount,
                categoriesCount = categories.size,
                isCmsSyncing = isCmsSyncing,
                onExploreProjects = { onNavigateRoute(NavRoutes.ROUTE_PROJECTS) },
                onOpenAdmin = { onNavigateRoute(NavRoutes.ROUTE_ADMIN) },
                onSyncCms = onSyncCms,
                onOpenCmsSettings = onOpenCmsSettings
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search portfolio"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DYNAMIC CONTENT CATEGORIES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (selectedCategoryId != null) {
                        TextButton(
                            onClick = { onSelectCategory(null) },
                            modifier = Modifier.testTag("reset_category_filter_btn")
                        ) {
                            Text("Show All")
                        }
                    }
                }

                DynamicCategoryFilterBar(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    contentTypeScope = ContentType.ALL,
                    onSelectCategory = onSelectCategory
                )
            }
        }

        if (selectedCategoryId != null || searchQuery.isNotBlank()) {
            item {
                SectionHeaderRow(
                    title = "Filtered Feed (${filteredItems.size})",
                    subtitle = "Dynamic category & live Headless CMS search results",
                    actionLabel = "Clear Filters",
                    onActionClick = {
                        onSelectCategory(null)
                        onSearchChange("")
                    }
                )
            }

            item {
                AdaptiveContentGrid(
                    items = filteredItems,
                    isAdminUnlocked = isAdminUnlocked,
                    onOpenItem = onOpenItem,
                    onEditItem = onEditItem,
                    onPublishItemToCms = onPublishItemToCms,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            val featuredProjects = publishedItems.filter { it.contentType == ContentType.PROJECT }.take(4)
            val latestBlogs = publishedItems.filter { it.contentType == ContentType.BLOG }.take(4)
            val photoShowcase = publishedItems.filter { it.contentType == ContentType.PHOTO }.take(4)

            item {
                SectionHeaderRow(
                    title = stringResource(R.string.featured_projects_title),
                    subtitle = "Full-stack React, Cloud, and Android builds synced via Headless CMS",
                    actionLabel = "All Projects ($projectsCount)",
                    onActionClick = { onNavigateRoute(NavRoutes.ROUTE_PROJECTS) }
                )
            }

            item {
                AdaptiveContentGrid(
                    items = featuredProjects,
                    isAdminUnlocked = isAdminUnlocked,
                    onOpenItem = onOpenItem,
                    onEditItem = onEditItem,
                    onPublishItemToCms = onPublishItemToCms,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                SectionHeaderRow(
                    title = stringResource(R.string.latest_blogs_title),
                    subtitle = "Posted dynamically via Contentful / Strapi without changing code",
                    actionLabel = "All Articles ($blogsCount)",
                    onActionClick = { onNavigateRoute(NavRoutes.ROUTE_BLOG) }
                )
            }

            item {
                AdaptiveContentGrid(
                    items = latestBlogs,
                    isAdminUnlocked = isAdminUnlocked,
                    onOpenItem = onOpenItem,
                    onEditItem = onEditItem,
                    onPublishItemToCms = onPublishItemToCms,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                SectionHeaderRow(
                    title = stringResource(R.string.photo_stories_title),
                    subtitle = "High-res photography with captions & EXIF metadata",
                    actionLabel = "Full Gallery ($photosCount)",
                    onActionClick = { onNavigateRoute(NavRoutes.ROUTE_PHOTOS) }
                )
            }

            item {
                AdaptiveContentGrid(
                    items = photoShowcase,
                    isAdminUnlocked = isAdminUnlocked,
                    onOpenItem = onOpenItem,
                    onEditItem = onEditItem,
                    onPublishItemToCms = onPublishItemToCms,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                TechStackAndArchitectureCard(
                    config = config,
                    categories = categories,
                    onOpenAdmin = { onNavigateRoute(NavRoutes.ROUTE_ADMIN) },
                    onQuickCreate = onQuickCreate,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun DeveloperHeroSection(
    config: SiteConfigEntity,
    projectsCount: Int,
    blogsCount: Int,
    photosCount: Int,
    categoriesCount: Int,
    isCmsSyncing: Boolean,
    onExploreProjects: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSyncCms: () -> Unit,
    onOpenCmsSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = "Rasel Dev BD Hero Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090E1A).copy(alpha = 0.68f),
                            Color(0xFF090E1A).copy(alpha = 0.86f),
                            Color(0xFF090E1A).copy(alpha = 0.97f)
                        )
                    )
                )
        )

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable(onClick = onOpenCmsSettings)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${config.cmsProvider} Headless CMS • Zero-Code Publishing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onSyncCms,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            CircleShape
                        )
                        .testTag("hero_sync_cms_button")
                ) {
                    if (isCmsSyncing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Live Content from Headless CMS",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PortfolioMediaImage(
                    mediaSource = config.avatarSource,
                    contentDescription = config.ownerName,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = config.siteTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Developer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${config.ownerName} • ${config.ownerRole}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${config.location} • ${config.customDomain}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = config.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HeroStatPill(
                    value = projectsCount.toString(),
                    label = "Projects",
                    modifier = Modifier.weight(1f)
                )
                HeroStatPill(
                    value = blogsCount.toString(),
                    label = "Blogs",
                    modifier = Modifier.weight(1f)
                )
                HeroStatPill(
                    value = photosCount.toString(),
                    label = "Photos",
                    modifier = Modifier.weight(1f)
                )
                HeroStatPill(
                    value = categoriesCount.toString(),
                    label = "Categories",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onExploreProjects,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("hero_explore_projects_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Projects",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Showcase", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenAdmin,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("hero_open_admin_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Headless CMS Studio",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CMS Studio", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun HeroStatPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1E293B).copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun ContentFeedScreen(
    contentType: String,
    title: String,
    subtitle: String,
    categories: List<CategoryEntity>,
    publishedItems: List<ContentItemEntity>,
    selectedCategoryId: Long?,
    searchQuery: String,
    isAdminUnlocked: Boolean,
    onSelectCategory: (Long?) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenItem: (Long) -> Unit,
    onEditItem: (ContentItemEntity) -> Unit,
    onPublishItemToCms: (ContentItemEntity) -> Unit,
    onCreateNewOfType: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    val filteredItems = publishedItems.filter { item ->
        val matchesType = item.contentType == contentType
        val matchesCat = selectedCategoryId == null || item.categoryId == selectedCategoryId
        val matchesQuery = searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.summary.contains(searchQuery, ignoreCase = true) ||
            item.photoCaption.contains(searchQuery, ignoreCase = true) ||
            item.techStackCsv.contains(searchQuery, ignoreCase = true)
        matchesType && matchesCat && matchesQuery
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("feed_screen_${contentType.lowercase()}")
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onCreateNewOfType,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("feed_add_btn_${contentType.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create new",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post New")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Filter $title by title, tag, or caption…") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feed_search_${contentType.lowercase()}")
            )
        }

        item {
            DynamicCategoryFilterBar(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                contentTypeScope = contentType,
                onSelectCategory = onSelectCategory
            )
        }

        if (filteredItems.isEmpty()) {
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.empty_content_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.empty_content_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCreateNewOfType,
                            modifier = Modifier.testTag("empty_state_create_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create First Entry")
                        }
                    }
                }
            }
        } else {
            item {
                AdaptiveContentGrid(
                    items = filteredItems,
                    isAdminUnlocked = isAdminUnlocked,
                    onOpenItem = onOpenItem,
                    onEditItem = onEditItem,
                    onPublishItemToCms = onPublishItemToCms
                )
            }
        }
    }
}

@Composable
fun AdaptiveContentGrid(
    items: List<ContentItemEntity>,
    isAdminUnlocked: Boolean,
    onOpenItem: (Long) -> Unit,
    onEditItem: (ContentItemEntity) -> Unit,
    onPublishItemToCms: (ContentItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth >= 840.dp -> 3
            maxWidth >= 560.dp -> 2
            else -> 1
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.chunked(columns).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { item ->
                        ContentItemCard(
                            item = item,
                            onClick = { onOpenItem(item.id) },
                            isAdminUnlocked = isAdminUnlocked,
                            onEditClick = { onEditItem(item) },
                            onPublishToCmsClick = { onPublishItemToCms(item) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(
            onClick = onActionClick,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("section_action_${title.take(10).lowercase().replace(" ", "_")}")
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = actionLabel,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TechStackAndArchitectureCard(
    config: SiteConfigEntity,
    categories: List<CategoryEntity>,
    onOpenAdmin: () -> Unit,
    onQuickCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.tech_stack_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Active Headless CMS: ${config.cmsProvider} (${config.lastCmsSyncStatus}) • Posts, photo captions, and project showcases can be published without changing code.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, parseHexColor(cat.accentHex).copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(cat.accentHex))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${cat.name} (${cat.contentTypeScope})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickUploadActionChip(
                    label = "+ Project",
                    icon = Icons.Default.Code,
                    onClick = { onQuickCreate(ContentType.PROJECT) },
                    modifier = Modifier.weight(1f),
                    testTag = "home_quick_add_project"
                )
                QuickUploadActionChip(
                    label = "+ Blog Post",
                    icon = Icons.AutoMirrored.Filled.Article,
                    onClick = { onQuickCreate(ContentType.BLOG) },
                    modifier = Modifier.weight(1f),
                    testTag = "home_quick_add_blog"
                )
                QuickUploadActionChip(
                    label = "+ Photo",
                    icon = Icons.Default.CameraAlt,
                    onClick = { onQuickCreate(ContentType.PHOTO) },
                    modifier = Modifier.weight(1f),
                    testTag = "home_quick_add_photo"
                )
            }
        }
    }
}

@Composable
private fun QuickUploadActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
