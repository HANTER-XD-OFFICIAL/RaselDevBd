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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
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
import com.example.data.CmaAuditLogEntity
import com.example.data.CmsProviderType
import com.example.data.ContentItemEntity
import com.example.data.ContentType
import com.example.data.SiteConfigEntity
import com.example.ui.AdminSubTab
import com.example.ui.components.CmsEntryBadge
import com.example.ui.components.ContentTypeBadge
import com.example.ui.components.PortfolioMediaImage
import com.example.ui.components.formatShortDate
import com.example.ui.components.parseHexColor
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun AdminDashboardScreen(
    config: SiteConfigEntity,
    categories: List<CategoryEntity>,
    allItems: List<ContentItemEntity>,
    auditLogs: List<CmaAuditLogEntity>,
    isAdminUnlocked: Boolean,
    authError: String?,
    activeTab: AdminSubTab,
    contentFilterType: String,
    isCmsSyncing: Boolean,
    onUnlock: (String) -> Unit,
    onLock: () -> Unit,
    onSelectTab: (AdminSubTab) -> Unit,
    onSelectContentFilter: (String) -> Unit,
    onCreateContent: (String) -> Unit,
    onEditContent: (ContentItemEntity) -> Unit,
    onTogglePublish: (ContentItemEntity) -> Unit,
    onToggleFeatured: (ContentItemEntity) -> Unit,
    onDeleteContent: (ContentItemEntity) -> Unit,
    onPublishItemToCms: (ContentItemEntity) -> Unit,
    onUnpublishItemInCms: (ContentItemEntity) -> Unit,
    onSyncFromCms: () -> Unit,
    onClearAuditLogs: () -> Unit,
    onSaveCmsModelSettings: (
        provider: String,
        contentfulEnvironment: String,
        contentfulContentType: String,
        contentfulLocale: String,
        strapiBaseUrl: String,
        autoSyncOnPublish: Boolean
    ) -> Unit,
    onCreateCategory: () -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onSaveSiteConfig: (SiteConfigEntity) -> Unit,
    onGenerateJson: () -> String,
    onGenerateCmsClientJs: () -> String,
    onGenerateReactJsx: () -> String,
    onGenerateIndexHtml: () -> String,
    onGenerateWorkflowYaml: () -> String,
    onMarkExported: () -> Unit,
    onImportJson: (String) -> Unit,
    onResetDemoData: () -> Unit,
    onShowMessage: (String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    if (!isAdminUnlocked) {
        AdminSecurityGate(
            config = config,
            authError = authError,
            onUnlock = onUnlock,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_unlocked")
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONTENTFUL CMA STUDIO • RASEL DEV BD",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${allItems.count { it.isPublished }} Live Entries • ${allItems.count { it.downloadUrl.isNotBlank() }} Download Links • CMA Ready",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onLock,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("admin_lock_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock CMA Studio",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.admin_lock_session))
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 12.dp
                ) {
                    Tab(
                        selected = activeTab == AdminSubTab.CONTENT,
                        onClick = { onSelectTab(AdminSubTab.CONTENT) },
                        text = { Text("CMA Entries (${allItems.size})") },
                        icon = { Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null) },
                        modifier = Modifier.testTag("admin_tab_content")
                    )
                    Tab(
                        selected = activeTab == AdminSubTab.HEADLESS_CMS,
                        onClick = { onSelectTab(AdminSubTab.HEADLESS_CMS) },
                        text = { Text("Contentful CMA Hub") },
                        icon = { Icon(Icons.Default.CloudSync, contentDescription = null) },
                        modifier = Modifier.testTag("admin_tab_headless_cms")
                    )
                    Tab(
                        selected = activeTab == AdminSubTab.CATEGORIES,
                        onClick = { onSelectTab(AdminSubTab.CATEGORIES) },
                        text = { Text("Categories (${categories.size})") },
                        icon = { Icon(Icons.Default.Category, contentDescription = null) },
                        modifier = Modifier.testTag("admin_tab_categories")
                    )
                    Tab(
                        selected = activeTab == AdminSubTab.STATIC_EXPORT,
                        onClick = { onSelectTab(AdminSubTab.STATIC_EXPORT) },
                        text = { Text("Live Website Code") },
                        icon = { Icon(Icons.Default.CloudDownload, contentDescription = null) },
                        modifier = Modifier.testTag("admin_tab_ssg")
                    )
                    Tab(
                        selected = activeTab == AdminSubTab.SETTINGS,
                        onClick = { onSelectTab(AdminSubTab.SETTINGS) },
                        text = { Text("Studio Settings") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        modifier = Modifier.testTag("admin_tab_settings")
                    )
                }
            }
        }

        when (activeTab) {
            AdminSubTab.CONTENT -> AdminContentManagerPane(
                allItems = allItems,
                contentFilterType = contentFilterType,
                activeCmsProvider = config.cmsProvider,
                isCmsSyncing = isCmsSyncing,
                onSelectFilter = onSelectContentFilter,
                onCreateContent = onCreateContent,
                onEditContent = onEditContent,
                onTogglePublish = onTogglePublish,
                onToggleFeatured = onToggleFeatured,
                onPublishItemToCms = onPublishItemToCms,
                onUnpublishItemInCms = onUnpublishItemInCms,
                onDeleteContent = onDeleteContent
            )

            AdminSubTab.HEADLESS_CMS -> AdminHeadlessCmsPane(
                config = config,
                allItems = allItems,
                auditLogs = auditLogs,
                isCmsSyncing = isCmsSyncing,
                onSaveCmsModelSettings = onSaveCmsModelSettings,
                onSyncFromCms = onSyncFromCms,
                onPublishItemToCms = onPublishItemToCms,
                onClearAuditLogs = onClearAuditLogs,
                onShowMessage = onShowMessage
            )

            AdminSubTab.CATEGORIES -> AdminCategoryManagerPane(
                categories = categories,
                allItems = allItems,
                onCreateCategory = onCreateCategory,
                onEditCategory = onEditCategory,
                onDeleteCategory = onDeleteCategory
            )

            AdminSubTab.STATIC_EXPORT -> AdminStaticExportPane(
                config = config,
                onGenerateJson = onGenerateJson,
                onGenerateCmsClientJs = onGenerateCmsClientJs,
                onGenerateReactJsx = onGenerateReactJsx,
                onGenerateIndexHtml = onGenerateIndexHtml,
                onGenerateWorkflowYaml = onGenerateWorkflowYaml,
                onMarkExported = onMarkExported,
                onImportJson = onImportJson,
                onShowMessage = onShowMessage
            )

            AdminSubTab.SETTINGS -> AdminSiteSettingsPane(
                config = config,
                onSaveConfig = onSaveSiteConfig,
                onResetDemoData = onResetDemoData
            )
        }
    }
}

@Composable
private fun AdminSecurityGate(
    config: SiteConfigEntity,
    authError: String?,
    onUnlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pinInput by remember { mutableStateOf("") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("admin_security_gate")
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Studio Access PIN",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.admin_locked_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.admin_locked_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("Enter Studio Access PIN") },
                    placeholder = { Text("Default PIN: ${config.adminPin}") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )

                if (authError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onUnlock(pinInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("admin_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.admin_unlock_btn), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { onUnlock(config.adminPin) },
                    modifier = Modifier.testTag("admin_quick_demo_unlock_btn")
                ) {
                    Text("Quick Demo Unlock (PIN: ${config.adminPin})")
                }
            }
        }
    }
}

@Composable
private fun AdminHeadlessCmsPane(
    config: SiteConfigEntity,
    allItems: List<ContentItemEntity>,
    auditLogs: List<CmaAuditLogEntity>,
    isCmsSyncing: Boolean,
    onSaveCmsModelSettings: (
        provider: String,
        contentfulEnvironment: String,
        contentfulContentType: String,
        contentfulLocale: String,
        strapiBaseUrl: String,
        autoSyncOnPublish: Boolean
    ) -> Unit,
    onSyncFromCms: () -> Unit,
    onPublishItemToCms: (ContentItemEntity) -> Unit,
    onClearAuditLogs: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var provider by remember(config.cmsProvider) { mutableStateOf(config.cmsProvider) }
    var contentfulEnv by remember(config.contentfulEnvironment) { mutableStateOf(config.contentfulEnvironment) }
    var contentfulContentType by remember(config.contentfulContentType) { mutableStateOf(config.contentfulContentType) }
    var contentfulLocale by remember(config.contentfulLocale) { mutableStateOf(config.contentfulLocale) }
    var strapiBaseUrl by remember(config.strapiBaseUrl) { mutableStateOf(config.strapiBaseUrl) }
    var autoSyncOnPublish by remember(config.autoSyncCmsOnPublish) { mutableStateOf(config.autoSyncCmsOnPublish) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_headless_cms_pane")
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Contentful Content Management API (CMA) Hub",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Post rich media content (images, descriptions, and download links) directly to Contentful via api.contentful.com & upload.contentful.com for immediate live website updates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isCmsSyncing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = config.lastCmsSyncStatus,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Secrets Panel Status: " +
                                    if (config.isContentfulCmaConfigured) {
                                        "CONTENTFUL_SPACE_ID & CONTENTFUL_MANAGEMENT_TOKEN loaded via BuildConfig"
                                    } else {
                                        "Set CONTENTFUL_SPACE_ID & CONTENTFUL_MANAGEMENT_TOKEN in the AI Studio Secrets panel"
                                    },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (config.isContentfulCmaConfigured) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onSyncFromCms,
                            enabled = !isCmsSyncing,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("cms_pull_live_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Sync from Contentful",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pull from $provider")
                        }

                        OutlinedButton(
                            onClick = {
                                val firstItem = allItems.firstOrNull { it.isPublished }
                                if (firstItem != null) {
                                    onPublishItemToCms(firstItem)
                                } else {
                                    onShowMessage("No published entry available to push.")
                                }
                            },
                            enabled = !isCmsSyncing,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("cms_push_latest_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Push to Contentful CMA",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish Latest CMA")
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "CONTENTFUL CMA ENVIRONMENT & CONTENT-TYPE TARGET",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = provider == CmsProviderType.CONTENTFUL,
                            onClick = { provider = CmsProviderType.CONTENTFUL },
                            label = { Text("Contentful CMA (Primary)") },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("cms_provider_contentful_chip")
                        )
                        FilterChip(
                            selected = provider == CmsProviderType.STRAPI,
                            onClick = { provider = CmsProviderType.STRAPI },
                            label = { Text("Strapi REST v4/v5") },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("cms_provider_strapi_chip")
                        )
                    }

                    if (provider == CmsProviderType.CONTENTFUL) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = contentfulEnv,
                                onValueChange = { contentfulEnv = it },
                                label = { Text("Environment ID") },
                                placeholder = { Text("master") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cms_contentful_env_input")
                            )
                            OutlinedTextField(
                                value = contentfulContentType,
                                onValueChange = { contentfulContentType = it },
                                label = { Text("Content-Type ID") },
                                placeholder = { Text("portfolioItem") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("cms_contentful_type_input")
                            )
                            OutlinedTextField(
                                value = contentfulLocale,
                                onValueChange = { contentfulLocale = it },
                                label = { Text("Locale") },
                                placeholder = { Text("en-US") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cms_contentful_locale_input")
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = strapiBaseUrl,
                            onValueChange = { strapiBaseUrl = it },
                            label = { Text("Strapi Public Base URL") },
                            placeholder = { Text("https://cms.raseldevbd.com") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cms_strapi_url_input")
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Publish to Contentful CMA on Save",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Automatically dispatches POST /entries + PUT /published when saving rich media items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoSyncOnPublish,
                            onCheckedChange = { autoSyncOnPublish = it },
                            modifier = Modifier.testTag("cms_auto_sync_switch")
                        )
                    }

                    Button(
                        onClick = {
                            onSaveCmsModelSettings(
                                provider,
                                contentfulEnv,
                                contentfulContentType,
                                contentfulLocale,
                                strapiBaseUrl,
                                autoSyncOnPublish
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("cms_save_config_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save CMA Model Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Contentful CMA Activity & Audit Log
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Contentful CMA API Dispatch Log (${auditLogs.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (auditLogs.isNotEmpty()) {
                            TextButton(
                                onClick = onClearAuditLogs,
                                modifier = Modifier.testTag("cms_clear_logs_btn")
                            ) {
                                Text("Clear")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (auditLogs.isEmpty()) {
                        Text(
                            text = "No CMA API dispatches recorded yet. Create or publish an entry to view live Contentful CMA requests.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            auditLogs.take(8).forEach { log ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (log.success) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "${log.actionType} • HTTP ${log.httpStatus}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (log.success) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = formatShortDate(log.timestampEpoch),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = log.entryTitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = log.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            val schemaSnippet = """
// Contentful Content Management API (CMA) Content-Type: "${config.contentfulContentType}"
// Endpoint: POST https://api.contentful.com/spaces/{SPACE_ID}/environments/${config.contentfulEnvironment}/entries
{
  "name": "Portfolio Rich Media Item",
  "fields": [
    { "id": "title", "type": "Symbol", "required": true },
    { "id": "slug", "type": "Symbol" },
    { "id": "contentType", "type": "Symbol" }, // PROJECT | BLOG | PHOTO
    { "id": "categoryName", "type": "Symbol" },
    { "id": "summary", "type": "Text" },
    { "id": "description", "type": "Text" },
    { "id": "markdownBody", "type": "Text" },
    { "id": "mediaSource", "type": "Symbol" },
    { "id": "media", "type": "Link", "linkType": "Asset" },
    { "id": "downloadUrl", "type": "Symbol" },
    { "id": "downloadLabel", "type": "Symbol" },
    { "id": "downloadFileSize", "type": "Symbol" },
    { "id": "versionTag", "type": "Symbol" }
  ]
}
            """.trimIndent()

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF060A12)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Contentful CMA Rich Media Schema",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("CMA Schema", schemaSnippet))
                                onShowMessage("Copied Contentful CMA schema JSON to clipboard")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy schema",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = schemaSnippet,
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminContentManagerPane(
    allItems: List<ContentItemEntity>,
    contentFilterType: String,
    activeCmsProvider: String,
    isCmsSyncing: Boolean,
    onSelectFilter: (String) -> Unit,
    onCreateContent: (String) -> Unit,
    onEditContent: (ContentItemEntity) -> Unit,
    onTogglePublish: (ContentItemEntity) -> Unit,
    onToggleFeatured: (ContentItemEntity) -> Unit,
    onPublishItemToCms: (ContentItemEntity) -> Unit,
    onUnpublishItemInCms: (ContentItemEntity) -> Unit,
    onDeleteContent: (ContentItemEntity) -> Unit
) {
    val filtered = allItems.filter {
        contentFilterType == ContentType.ALL || it.contentType == contentFilterType
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_content_pane")
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onCreateContent(ContentType.PROJECT) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("admin_new_project_btn")
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Release", maxLines = 1)
                }

                Button(
                    onClick = { onCreateContent(ContentType.BLOG) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("admin_new_blog_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Article", maxLines = 1)
                }

                Button(
                    onClick = { onCreateContent(ContentType.PHOTO) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("admin_new_photo_btn")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Media", maxLines = 1)
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                listOf(
                    ContentType.ALL to "All (${allItems.size})",
                    ContentType.PROJECT to "Projects (${allItems.count { it.contentType == ContentType.PROJECT }})",
                    ContentType.BLOG to "Articles (${allItems.count { it.contentType == ContentType.BLOG }})",
                    ContentType.PHOTO to "Media (${allItems.count { it.contentType == ContentType.PHOTO }})"
                ).forEach { (typeKey, label) ->
                    FilterChip(
                        selected = contentFilterType == typeKey,
                        onClick = { onSelectFilter(typeKey) },
                        label = { Text(label) },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("admin_filter_${typeKey.lowercase()}")
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { item ->
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (item.isPublished) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_item_row_${item.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PortfolioMediaImage(
                            mediaSource = item.mediaSource,
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ContentTypeBadge(contentType = item.contentType)
                                CmsEntryBadge(
                                    cmsProvider = item.cmsProvider,
                                    cmsEntryId = item.cmsEntryId,
                                    cmsVersion = item.cmsVersion
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = buildString {
                                    append("${item.categoryName} • ")
                                    if (item.downloadUrl.isNotBlank()) {
                                        append("⬇ ${item.downloadLabel} (${item.downloadFileSize.ifBlank { "Link" }}) • ")
                                    }
                                    append(formatShortDate(item.updatedAtEpoch))
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { onPublishItemToCms(item) },
                                enabled = !isCmsSyncing,
                                modifier = Modifier.testTag("admin_push_cms_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Publish to Contentful CMA",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Push CMA")
                            }

                            TextButton(
                                onClick = {
                                    if (item.isPublished && item.cmsEntryId.isNotBlank()) {
                                        onUnpublishItemInCms(item)
                                    } else {
                                        onTogglePublish(item)
                                    }
                                },
                                modifier = Modifier.testTag("admin_toggle_pub_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (item.isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle publish",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (item.isPublished) "Live" else "Draft")
                            }
                        }

                        Row {
                            IconButton(
                                onClick = { onToggleFeatured(item) },
                                modifier = Modifier.testTag("admin_toggle_feat_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Toggle featured",
                                    tint = if (item.isFeatured) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { onEditContent(item) },
                                modifier = Modifier.testTag("admin_edit_item_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit ${item.title}",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { onDeleteContent(item) },
                                modifier = Modifier.testTag("admin_delete_item_${item.id}")
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
private fun AdminCategoryManagerPane(
    categories: List<CategoryEntity>,
    allItems: List<ContentItemEntity>,
    onCreateCategory: () -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_categories_pane")
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dynamic Content Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Organize rich media entries, articles, and downloads",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onCreateCategory,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("admin_new_category_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Category")
                }
            }
        }

        items(categories, key = { it.id }) { category ->
            val usageCount = allItems.count { it.categoryId == category.id }
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                border = BorderStroke(1.dp, parseHexColor(category.accentHex).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_category_row_${category.slug}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(category.accentHex))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${category.contentTypeScope} • $usageCount items",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            if (category.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
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
                            onClick = { onEditCategory(category) },
                            modifier = Modifier.testTag("admin_edit_cat_${category.slug}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${category.name}",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { onDeleteCategory(category) },
                            modifier = Modifier.testTag("admin_delete_cat_${category.slug}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete ${category.name}",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminStaticExportPane(
    config: SiteConfigEntity,
    onGenerateJson: () -> String,
    onGenerateCmsClientJs: () -> String,
    onGenerateReactJsx: () -> String,
    onGenerateIndexHtml: () -> String,
    onGenerateWorkflowYaml: () -> String,
    onMarkExported: () -> Unit,
    onImportJson: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedArtifactIndex by remember { mutableIntStateOf(0) }

    val currentCode = remember(selectedArtifactIndex, config) {
        when (selectedArtifactIndex) {
            0 -> onGenerateCmsClientJs()
            1 -> onGenerateReactJsx()
            2 -> onGenerateJson()
            3 -> onGenerateIndexHtml()
            else -> onGenerateWorkflowYaml()
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(onGenerateJson().toByteArray(Charsets.UTF_8))
                }
                onMarkExported()
                onShowMessage("Saved static content-bundle.json to device storage")
            }.onFailure {
                onShowMessage("Export failed: ${it.localizedMessage}")
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (!content.isNullOrBlank()) {
                    onImportJson(content)
                }
            }.onFailure {
                onShowMessage("Failed to read JSON file: ${it.localizedMessage}")
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_ssg_export_pane")
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Live Website & GitHub Pages Bundle Generator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target Website: https://${config.customDomain} • Contentful CMA Sync Enabled",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Exports the connected React website code (`src/cmsClient.js`, `src/App.jsx`, `public/data/content-bundle.json`) with built-in support for images, descriptions, and direct download links.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                createDocumentLauncher.launch("content-bundle.json")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("ssg_save_file_btn")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Bundle JSON")
                        }

                        OutlinedButton(
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "text/*"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("ssg_import_file_btn")
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON")
                        }
                    }
                }
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "src/cmsClient.js",
                    1 to "src/App.jsx (React + CMA)",
                    2 to "public/data/content-bundle.json",
                    3 to "index.html",
                    4 to ".github/workflows/deploy-pages.yml"
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = selectedArtifactIndex == idx,
                        onClick = { selectedArtifactIndex = idx },
                        label = { Text(label) },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("ssg_artifact_chip_$idx")
                    )
                }
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF060A12)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (selectedArtifactIndex) {
                                0 -> "src/cmsClient.js"
                                1 -> "src/App.jsx"
                                2 -> "public/data/content-bundle.json"
                                3 -> "index.html"
                                else -> ".github/workflows/deploy-pages.yml"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Rasel Dev BD Export", currentCode)
                                    clipboard.setPrimaryClip(clip)
                                    onMarkExported()
                                    onShowMessage("Copied artifact code to clipboard")
                                },
                                modifier = Modifier.testTag("ssg_copy_code_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Rasel Dev BD Static Site Artifact")
                                        putExtra(Intent.EXTRA_TEXT, currentCode)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Export Code"))
                                    onMarkExported()
                                },
                                modifier = Modifier.testTag("ssg_share_code_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share code",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = currentCode,
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA7F3D0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminSiteSettingsPane(
    config: SiteConfigEntity,
    onSaveConfig: (SiteConfigEntity) -> Unit,
    onResetDemoData: () -> Unit
) {
    var siteTitle by remember(config) { mutableStateOf(config.siteTitle) }
    var ownerName by remember(config) { mutableStateOf(config.ownerName) }
    var ownerRole by remember(config) { mutableStateOf(config.ownerRole) }
    var bio by remember(config) { mutableStateOf(config.bio) }
    var location by remember(config) { mutableStateOf(config.location) }
    var email by remember(config) { mutableStateOf(config.email) }
    var githubUrl by remember(config) { mutableStateOf(config.githubUrl) }
    var githubPagesRepo by remember(config) { mutableStateOf(config.githubPagesRepo) }
    var customDomain by remember(config) { mutableStateOf(config.customDomain) }
    var adminPin by remember(config) { mutableStateOf(config.adminPin) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_settings_pane")
    ) {
        item {
            Text(
                text = "Website Profile & Studio Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = siteTitle,
                onValueChange = { siteTitle = it },
                label = { Text("Platform Brand Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_site_title_input")
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Developer Name") },
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
                value = ownerRole,
                onValueChange = { ownerRole = it },
                label = { Text("Headline Role") },
                singleLine = true,
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = githubPagesRepo,
                    onValueChange = { githubPagesRepo = it },
                    label = { Text("GitHub Pages Repo") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = customDomain,
                    onValueChange = { customDomain = it },
                    label = { Text("Live Website Domain") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = adminPin,
                    onValueChange = { adminPin = it },
                    label = { Text("Studio Lock PIN") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_admin_pin_input")
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onSaveConfig(
                            config.copy(
                                siteTitle = siteTitle.trim().ifBlank { "Rasel Dev BD" },
                                ownerName = ownerName.trim(),
                                ownerRole = ownerRole.trim(),
                                bio = bio.trim(),
                                location = location.trim(),
                                email = email.trim(),
                                githubUrl = githubUrl.trim(),
                                githubPagesRepo = githubPagesRepo.trim(),
                                customDomain = customDomain.trim(),
                                adminPin = adminPin.trim().ifBlank { "2026" }
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("settings_save_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Configuration")
                }

                OutlinedButton(
                    onClick = onResetDemoData,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("settings_reset_demo_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Demo")
                }
            }
        }
    }
}
