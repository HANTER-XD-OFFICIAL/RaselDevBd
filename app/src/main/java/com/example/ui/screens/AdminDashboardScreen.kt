package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CategoryEntity
import com.example.data.ContentItemEntity
import com.example.data.ContentType
import com.example.data.SiteConfigEntity
import com.example.data.StaticSiteGenerator
import com.example.ui.AdminTab
import com.example.ui.components.ContentTypeBadge
import com.example.ui.components.PortfolioMediaImage
import com.example.ui.components.parseHexColor
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun AdminDashboardScreen(
    siteConfig: SiteConfigEntity,
    categories: List<CategoryEntity>,
    allItems: List<ContentItemEntity>,
    publishedItems: List<ContentItemEntity>,
    isAdminUnlocked: Boolean,
    adminAuthError: String?,
    activeAdminTab: AdminTab,
    adminTypeFilter: String,
    onUnlockAttempt: (String) -> Unit,
    onLockSession: () -> Unit,
    onSelectAdminTab: (AdminTab) -> Unit,
    onSetAdminTypeFilter: (String) -> Unit,
    onOpenNewContent: (String) -> Unit,
    onOpenEditContent: (ContentItemEntity) -> Unit,
    onDeleteContent: (ContentItemEntity) -> Unit,
    onTogglePublished: (ContentItemEntity) -> Unit,
    onToggleFeatured: (ContentItemEntity) -> Unit,
    onOpenNewCategory: () -> Unit,
    onOpenEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onSaveSiteConfig: (SiteConfigEntity) -> Unit,
    onImportStaticBundle: (String) -> Unit,
    onShowStatus: (String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    if (!isAdminUnlocked) {
        AdminSecurityGate(
            expectedPasscode = siteConfig.adminPasscode,
            errorMessage = adminAuthError,
            onUnlock = onUnlockAttempt,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_unlocked")
    ) {
        // Admin Header Strip
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.18f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Unlocked",
                                tint = Color(0xFF10B981),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Rasel Dev BD • Admin Studio",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${publishedItems.size}/${allItems.size} Published • ${categories.size} Dynamic Categories",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onLockSession,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("admin_lock_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Session",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.admin_lock_session))
                    }
                }

                // Sub-navigation tabs
                ScrollableTabRow(
                    selectedTabIndex = activeAdminTab.ordinal,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent
                ) {
                    val tabs = listOf(
                        Triple(AdminTab.CONTENT, "Content (${allItems.size})", Icons.AutoMirrored.Filled.Article),
                        Triple(AdminTab.CATEGORIES, "Categories (${categories.size})", Icons.Default.Category),
                        Triple(AdminTab.GITHUB_PAGES, "GitHub Pages SSG", Icons.Default.Code),
                        Triple(AdminTab.SETTINGS, "Profile & Auth", Icons.Default.Settings)
                    )
                    tabs.forEach { (tab, label, icon) ->
                        Tab(
                            selected = activeAdminTab == tab,
                            onClick = { onSelectAdminTab(tab) },
                            text = { Text(label, style = MaterialTheme.typography.labelLarge) },
                            icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("admin_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Active Tab Body
        when (activeAdminTab) {
            AdminTab.CONTENT -> AdminContentManagerTab(
                allItems = allItems,
                adminTypeFilter = adminTypeFilter,
                onSetTypeFilter = onSetAdminTypeFilter,
                onOpenNewContent = onOpenNewContent,
                onOpenEditContent = onOpenEditContent,
                onDeleteContent = onDeleteContent,
                onTogglePublished = onTogglePublished,
                onToggleFeatured = onToggleFeatured
            )
            AdminTab.CATEGORIES -> AdminCategoriesManagerTab(
                categories = categories,
                allItems = allItems,
                onOpenNewCategory = onOpenNewCategory,
                onOpenEditCategory = onOpenEditCategory,
                onDeleteCategory = onDeleteCategory
            )
            AdminTab.GITHUB_PAGES -> AdminGithubPagesGeneratorTab(
                siteConfig = siteConfig,
                categories = categories,
                publishedItems = publishedItems,
                onImportStaticBundle = onImportStaticBundle,
                onShowStatus = onShowStatus
            )
            AdminTab.SETTINGS -> AdminProfileSettingsTab(
                siteConfig = siteConfig,
                onSaveConfig = onSaveSiteConfig
            )
        }
    }
}

@Composable
private fun AdminSecurityGate(
    expectedPasscode: String,
    errorMessage: String?,
    onUnlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pinInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("admin_security_gate"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Admin Security",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.admin_locked_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.admin_locked_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("Admin Passcode") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = { onUnlock(pinInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("admin_unlock_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Unlock")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.admin_unlock_btn))
                }

                FilledTonalButton(
                    onClick = {
                        pinInput = expectedPasscode
                        onUnlock(expectedPasscode)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("admin_quick_unlock_btn")
                ) {
                    Text("Quick Auto-Fill & Unlock (PIN: $expectedPasscode)")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminContentManagerTab(
    allItems: List<ContentItemEntity>,
    adminTypeFilter: String,
    onSetTypeFilter: (String) -> Unit,
    onOpenNewContent: (String) -> Unit,
    onOpenEditContent: (ContentItemEntity) -> Unit,
    onDeleteContent: (ContentItemEntity) -> Unit,
    onTogglePublished: (ContentItemEntity) -> Unit,
    onToggleFeatured: (ContentItemEntity) -> Unit
) {
    val filteredItems = allItems.filter {
        adminTypeFilter == ContentType.ALL || it.contentType == adminTypeFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_content_tab_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Create Bar for all 3 Content Types
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CREATE & UPLOAD NEW CONTENT",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenNewContent(ContentType.BLOG) },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("admin_new_blog_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Blog", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = { onOpenNewContent(ContentType.PHOTO) },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("admin_new_photo_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Photo", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = { onOpenNewContent(ContentType.PROJECT) },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("admin_new_project_btn")
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Project", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Filter Chips by Type
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ContentType.ALL to "All (${allItems.size})",
                    ContentType.PROJECT to "Projects (${allItems.count { it.contentType == ContentType.PROJECT }})",
                    ContentType.BLOG to "Blogs (${allItems.count { it.contentType == ContentType.BLOG }})",
                    ContentType.PHOTO to "Photos (${allItems.count { it.contentType == ContentType.PHOTO }})"
                ).forEach { (typeKey, label) ->
                    FilterChip(
                        selected = adminTypeFilter == typeKey,
                        onClick = { onSetTypeFilter(typeKey) },
                        label = { Text(label) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("admin_filter_${typeKey.lowercase()}")
                    )
                }
            }
        }

        items(filteredItems, key = { it.id }) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_item_row_${item.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PortfolioMediaImage(
                            mediaSource = item.mediaSource,
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ContentTypeBadge(contentType = item.contentType)
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.summaryOrCaption,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Switch(
                                checked = item.isPublished,
                                onCheckedChange = { onTogglePublished(item) },
                                modifier = Modifier.testTag("admin_publish_switch_${item.id}")
                            )
                            Text(
                                text = if (item.isPublished) "Published" else "Draft",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (item.isPublished) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onToggleFeatured(item) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("admin_feature_btn_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (item.isFeatured) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Toggle Featured",
                                    tint = if (item.isFeatured) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onOpenEditContent(item) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("admin_edit_item_btn_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit ${item.title}",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { onDeleteContent(item) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("admin_delete_item_btn_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete ${item.title}",
                                    tint = MaterialTheme.colorScheme.error
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
private fun AdminCategoriesManagerTab(
    categories: List<CategoryEntity>,
    allItems: List<ContentItemEntity>,
    onOpenNewCategory: () -> Unit,
    onOpenEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_categories_tab_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dynamic Categories Taxonomy",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Organize blog posts, photos, and projects dynamically",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenNewCategory,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("admin_add_category_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Category")
                }
            }
        }

        items(categories, key = { it.id }) { category ->
            val accent = parseHexColor(category.colorHex)
            val assignedCount = allItems.count { it.categoryId == category.id }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_category_row_${category.slug}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = accent.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = category.contentTypeScope,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "/category/${category.slug} • $assignedCount items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            if (category.description.isNotBlank()) {
                                Text(
                                    text = category.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { onOpenEditCategory(category) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_category_${category.slug}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Category", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { onDeleteCategory(category) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("delete_category_${category.slug}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminGithubPagesGeneratorTab(
    siteConfig: SiteConfigEntity,
    categories: List<CategoryEntity>,
    publishedItems: List<ContentItemEntity>,
    onImportStaticBundle: (String) -> Unit,
    onShowStatus: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedArtifactIndex by remember { mutableIntStateOf(0) }
    var showImportBox by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }

    val artifacts = remember(siteConfig, categories, publishedItems) {
        listOf(
            Triple(
                "public/data/content-bundle.json",
                "content-bundle.json",
                StaticSiteGenerator.generateStaticJsonBundle(siteConfig, categories, publishedItems)
            ),
            Triple(
                "src/App.jsx (React 19 SSG)",
                "App.jsx",
                StaticSiteGenerator.generateReactAppJsx(siteConfig, categories, publishedItems)
            ),
            Triple(
                "index.html",
                "index.html",
                StaticSiteGenerator.generateIndexHtml(siteConfig)
            ),
            Triple(
                ".github/workflows/deploy-gh-pages.yml",
                "deploy-gh-pages.yml",
                StaticSiteGenerator.generateGithubActionsWorkflow(siteConfig)
            )
        )
    }

    val activeArtifact = artifacts[selectedArtifactIndex]

    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(activeArtifact.third.toByteArray(Charsets.UTF_8))
                }
                onShowStatus("Saved ${activeArtifact.second} to device storage")
            }.onFailure {
                onShowStatus("Could not write file: ${it.localizedMessage}")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_gh_pages_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "GitHub Pages Static Site Generator",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Compiles all published categories (${categories.size}), blog posts (${publishedItems.count { it.contentType == ContentType.BLOG }}), photos (${publishedItems.count { it.contentType == ContentType.PHOTO }}), and projects (${publishedItems.count { it.contentType == ContentType.PROJECT }}) into static React + JSON files ready for ${siteConfig.githubPagesDomain}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText(activeArtifact.second, activeArtifact.third))
                                onShowStatus("Copied ${activeArtifact.first} to clipboard")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("gh_pages_copy_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy File")
                        }

                        FilledTonalButton(
                            onClick = {
                                saveDocumentLauncher.launch(activeArtifact.second)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("gh_pages_save_file_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Save File", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Device")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, activeArtifact.first)
                                    putExtra(Intent.EXTRA_TEXT, activeArtifact.third)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export ${activeArtifact.second}"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("gh_pages_share_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Bundle")
                        }

                        OutlinedButton(
                            onClick = { showImportBox = !showImportBox },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("gh_pages_toggle_import_btn")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = "Import", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showImportBox) "Hide Import" else "Import JSON")
                        }
                    }
                }
            }
        }

        if (showImportBox) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Paste content-bundle.json to Sync / Restore Content",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = importJsonInput,
                            onValueChange = { importJsonInput = it },
                            placeholder = { Text("{ \"schemaVersion\": \"1.0.0\", ... }") },
                            minLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_json_textfield")
                        )
                        Button(
                            onClick = {
                                if (importJsonInput.isNotBlank()) {
                                    onImportStaticBundle(importJsonInput)
                                    showImportBox = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("confirm_import_json_btn")
                        ) {
                            Text("Import & Sync into Database")
                        }
                    }
                }
            }
        }

        // File selector chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                artifacts.forEachIndexed { index, (label, shortName, _) ->
                    FilterChip(
                        selected = selectedArtifactIndex == index,
                        onClick = { selectedArtifactIndex = index },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("artifact_chip_$shortName")
                    )
                }
            }
        }

        // Live Code Output Preview
        item {
            Surface(
                color = Color(0xFF050811),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("static_code_preview_box")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeArtifact.first,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "${activeArtifact.third.lines().size} lines",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Text(
                        text = activeArtifact.third,
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminProfileSettingsTab(
    siteConfig: SiteConfigEntity,
    onSaveConfig: (SiteConfigEntity) -> Unit
) {
    var siteTitle by remember(siteConfig) { mutableStateOf(siteConfig.siteTitle) }
    var tagline by remember(siteConfig) { mutableStateOf(siteConfig.tagline) }
    var bio by remember(siteConfig) { mutableStateOf(siteConfig.bio) }
    var githubUsername by remember(siteConfig) { mutableStateOf(siteConfig.githubUsername) }
    var githubPagesDomain by remember(siteConfig) { mutableStateOf(siteConfig.githubPagesDomain) }
    var email by remember(siteConfig) { mutableStateOf(siteConfig.email) }
    var location by remember(siteConfig) { mutableStateOf(siteConfig.location) }
    var skillsCsv by remember(siteConfig) { mutableStateOf(siteConfig.skillsCsv) }
    var adminPasscode by remember(siteConfig) { mutableStateOf(siteConfig.adminPasscode) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_settings_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Portfolio Identity & Security Config",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            OutlinedTextField(
                value = siteTitle,
                onValueChange = { siteTitle = it },
                label = { Text("Site Name / Brand") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_site_title_input")
            )
        }

        item {
            OutlinedTextField(
                value = tagline,
                onValueChange = { tagline = it },
                label = { Text("Hero Tagline") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Developer Bio") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = githubUsername,
                    onValueChange = { githubUsername = it },
                    label = { Text("GitHub Username") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = githubPagesDomain,
                onValueChange = { githubPagesDomain = it },
                label = { Text("GitHub Pages Target URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Contact Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = skillsCsv,
                onValueChange = { skillsCsv = it },
                label = { Text("Core Tech Stack Skills (comma-separated)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = adminPasscode,
                onValueChange = { adminPasscode = it },
                label = { Text("Admin Studio Passcode / PIN") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_admin_pin_input")
            )
        }

        item {
            Button(
                onClick = {
                    onSaveConfig(
                        siteConfig.copy(
                            siteTitle = siteTitle.trim().ifBlank { "Rasel Dev BD" },
                            tagline = tagline.trim(),
                            bio = bio.trim(),
                            githubUsername = githubUsername.trim(),
                            githubPagesDomain = githubPagesDomain.trim(),
                            email = email.trim(),
                            location = location.trim(),
                            skillsCsv = skillsCsv.trim(),
                            adminPasscode = adminPasscode.trim().ifBlank { "2026" }
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("save_site_settings_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Settings")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Portfolio & Security Settings")
            }
        }
    }
}
