package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CategoryEntity
import com.example.data.ContentItemEntity
import com.example.data.ContentType
import com.example.data.SiteConfigEntity
import com.example.ui.AdminTab
import com.example.ui.AppRoutes
import com.example.ui.components.ContentItemCard
import com.example.ui.components.DynamicCategoryFilterBar
import com.example.ui.components.EmptyStatePanel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortfolioHomeScreen(
    siteConfig: SiteConfigEntity,
    categories: List<CategoryEntity>,
    publishedItems: List<ContentItemEntity>,
    selectedCategoryId: Long?,
    searchQuery: String,
    onSelectCategory: (Long?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenItem: (ContentItemEntity) -> Unit,
    onLikeItem: (Long) -> Unit,
    onNavigateRoute: (String) -> Unit,
    onOpenAdminTab: (AdminTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColorMap = categories.associate { it.id to it.colorHex }

    val filteredItems = publishedItems.filter { item ->
        val matchesCategory = selectedCategoryId == null || item.categoryId == selectedCategoryId
        val matchesQuery = searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.summaryOrCaption.contains(searchQuery, ignoreCase = true) ||
            item.tagsCsv.contains(searchQuery, ignoreCase = true) ||
            item.categoryName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    val projects = filteredItems.filter { it.contentType == ContentType.PROJECT }
    val blogs = filteredItems.filter { it.contentType == ContentType.BLOG }
    val photos = filteredItems.filter { it.contentType == ContentType.PHOTO }

    val skills = siteConfig.skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("portfolio_home_list"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Hero Developer Banner Card
        item {
            DeveloperHeroSection(
                siteConfig = siteConfig,
                projectsCount = publishedItems.count { it.contentType == ContentType.PROJECT },
                blogsCount = publishedItems.count { it.contentType == ContentType.BLOG },
                photosCount = publishedItems.count { it.contentType == ContentType.PHOTO },
                categoriesCount = categories.size,
                onOpenAdmin = {
                    onOpenAdminTab(AdminTab.CONTENT)
                    onNavigateRoute(AppRoutes.ROUTE_ADMIN)
                },
                onOpenStaticExport = {
                    onOpenAdminTab(AdminTab.GITHUB_PAGES)
                    onNavigateRoute(AppRoutes.ROUTE_ADMIN)
                }
            )
        }

        // 2. Search Bar & Dynamic Category Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("portfolio_search_input")
                )

                DynamicCategoryFilterBar(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = onSelectCategory,
                    scopeFilter = ContentType.ALL
                )
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                EmptyStatePanel(
                    title = stringResource(R.string.empty_content_title),
                    subtitle = stringResource(R.string.empty_content_subtitle),
                    onResetAction = {
                        onSelectCategory(null)
                        onSearchQueryChange("")
                    }
                )
            }
        } else {
            // 3. Featured Project Showcases Section
            if (projects.isNotEmpty()) {
                item {
                    SectionHeaderRow(
                        title = stringResource(R.string.featured_projects_title),
                        badgeText = "${projects.size} Showcases",
                        onSeeAll = { onNavigateRoute(AppRoutes.ROUTE_PROJECTS) },
                        testTag = "see_all_projects_btn"
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(projects, key = { it.id }) { project ->
                            ContentItemCard(
                                item = project,
                                categoryColorHex = categoryColorMap[project.categoryId] ?: "#10B981",
                                onClick = { onOpenItem(project) },
                                onLike = { onLikeItem(project.id) },
                                modifier = Modifier.width(320.dp)
                            )
                        }
                    }
                }
            }

            // 4. Latest Engineering Blog Posts Section
            if (blogs.isNotEmpty()) {
                item {
                    SectionHeaderRow(
                        title = stringResource(R.string.latest_blogs_title),
                        badgeText = "${blogs.size} Articles",
                        onSeeAll = { onNavigateRoute(AppRoutes.ROUTE_BLOG) },
                        testTag = "see_all_blogs_btn"
                    )
                }
                items(blogs.take(3), key = { it.id }) { blog ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ContentItemCard(
                            item = blog,
                            categoryColorHex = categoryColorMap[blog.categoryId] ?: "#06B6D4",
                            onClick = { onOpenItem(blog) },
                            onLike = { onLikeItem(blog.id) }
                        )
                    }
                }
            }

            // 5. Photos with Captions Section
            if (photos.isNotEmpty()) {
                item {
                    SectionHeaderRow(
                        title = stringResource(R.string.photo_stories_title),
                        badgeText = "${photos.size} Captions",
                        onSeeAll = { onNavigateRoute(AppRoutes.ROUTE_PHOTOS) },
                        testTag = "see_all_photos_btn"
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            ContentItemCard(
                                item = photo,
                                categoryColorHex = categoryColorMap[photo.categoryId] ?: "#F59E0B",
                                onClick = { onOpenItem(photo) },
                                onLike = { onLikeItem(photo.id) },
                                modifier = Modifier.width(300.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Core Tech Stack & GitHub Pages Static Readiness Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Tech Stack",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tech_stack_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = siteConfig.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skills.forEach { skill ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = skill,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeveloperHeroSection(
    siteConfig: SiteConfigEntity,
    projectsCount: Int,
    blogsCount: Int,
    photosCount: Int,
    categoriesCount: Int,
    onOpenAdmin: () -> Unit,
    onOpenStaticExport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("developer_hero_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = "Rasel Dev BD Hero Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF090E1A).copy(alpha = 0.72f),
                                Color(0xFF090E1A).copy(alpha = 0.86f),
                                Color(0xFF090E1A).copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Pill & GitHub Pages Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "STATIC SSG READY • BD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Domain",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = siteConfig.githubPagesDomain.removePrefix("https://"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCFFAFE)
                        )
                    }
                }

                // Avatar + Title + Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_avatar_rasel),
                        contentDescription = "Rasel Developer Avatar",
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF10B981), CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = siteConfig.siteTitle,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${siteConfig.location} • @${siteConfig.githubUsername}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Text(
                    text = siteConfig.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0),
                    maxLines = 2
                )

                // Live Stats Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroStatPill(value = projectsCount.toString(), label = "Projects", modifier = Modifier.weight(1f))
                    HeroStatPill(value = blogsCount.toString(), label = "Blogs", modifier = Modifier.weight(1f))
                    HeroStatPill(value = photosCount.toString(), label = "Photos", modifier = Modifier.weight(1f))
                    HeroStatPill(value = categoriesCount.toString(), label = "Categories", modifier = Modifier.weight(1f))
                }

                // Quick Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenAdmin,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("hero_open_admin_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color(0xFF022C22)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Studio",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Admin Dashboard", style = MaterialTheme.typography.labelLarge)
                    }

                    FilledTonalButton(
                        onClick = onOpenStaticExport,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("hero_gh_pages_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "GitHub Pages Export",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GitHub Pages SSG", style = MaterialTheme.typography.labelLarge)
                    }
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
        color = Color(0xFF1E293B).copy(alpha = 0.75f),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1)
            )
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    badgeText: String,
    onSeeAll: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        TextButton(
            onClick = onSeeAll,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag(testTag)
        ) {
            Text("Explore All")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Explore All",
                modifier = Modifier.size(16.dp)
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
    onSelectCategory: (Long?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenItem: (ContentItemEntity) -> Unit,
    onLikeItem: (Long) -> Unit,
    onQuickAddInAdmin: (String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    val categoryColorMap = categories.associate { it.id to it.colorHex }
    val filteredItems = publishedItems.filter { item ->
        val matchesType = item.contentType == contentType
        val matchesCat = selectedCategoryId == null || item.categoryId == selectedCategoryId
        val matchesQuery = searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.summaryOrCaption.contains(searchQuery, ignoreCase = true) ||
            item.tagsCsv.contains(searchQuery, ignoreCase = true) ||
            item.categoryName.contains(searchQuery, ignoreCase = true)
        matchesType && matchesCat && matchesQuery
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("feed_screen_${contentType.lowercase()}")
    ) {
        val isWide = maxWidth >= 680.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { onQuickAddInAdmin(contentType) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("feed_quick_add_${contentType.lowercase()}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add $contentType",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create")
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Filter ${title.lowercase()} by keyword or tag…") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("feed_search_input")
                    )

                    DynamicCategoryFilterBar(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onSelectCategory = onSelectCategory,
                        scopeFilter = contentType
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    EmptyStatePanel(
                        title = stringResource(R.string.empty_content_title),
                        subtitle = stringResource(R.string.empty_content_subtitle),
                        onResetAction = {
                            onSelectCategory(null)
                            onSearchQueryChange("")
                        }
                    )
                }
            } else if (isWide) {
                val rows = filteredItems.chunked(2)
                items(rows) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { item ->
                            ContentItemCard(
                                item = item,
                                categoryColorHex = categoryColorMap[item.categoryId] ?: "#10B981",
                                onClick = { onOpenItem(item) },
                                onLike = { onLikeItem(item.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ContentItemCard(
                            item = item,
                            categoryColorHex = categoryColorMap[item.categoryId] ?: "#10B981",
                            onClick = { onOpenItem(item) },
                            onLike = { onLikeItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}
