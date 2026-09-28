package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CategoryEntity
import com.example.data.ContentItemEntity
import com.example.data.ContentType
import com.example.ui.components.PortfolioMediaImage
import com.example.ui.components.parseHexColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentEditorDialog(
    initialItem: ContentItemEntity,
    categories: List<CategoryEntity>,
    activeCmsProvider: String,
    onDismiss: () -> Unit,
    onSave: (ContentItemEntity, Boolean) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current

    var title by remember(initialItem) { mutableStateOf(initialItem.title) }
    var slug by remember(initialItem) { mutableStateOf(initialItem.slug) }
    var contentType by remember(initialItem) { mutableStateOf(initialItem.contentType) }
    var selectedCategoryId by remember(initialItem) { mutableStateOf(initialItem.categoryId) }
    var summary by remember(initialItem) { mutableStateOf(initialItem.summary) }
    var markdownBody by remember(initialItem) { mutableStateOf(initialItem.markdownBody) }
    var mediaSource by remember(initialItem) { mutableStateOf(initialItem.mediaSource) }
    var downloadUrl by remember(initialItem) { mutableStateOf(initialItem.downloadUrl) }
    var downloadLabel by remember(initialItem) { mutableStateOf(initialItem.downloadLabel) }
    var downloadFileSize by remember(initialItem) { mutableStateOf(initialItem.downloadFileSize) }
    var versionTag by remember(initialItem) { mutableStateOf(initialItem.versionTag) }
    var photoCaption by remember(initialItem) { mutableStateOf(initialItem.photoCaption) }
    var photoLocation by remember(initialItem) { mutableStateOf(initialItem.photoLocation) }
    var exifCamera by remember(initialItem) { mutableStateOf(initialItem.exifCamera) }
    var techStackCsv by remember(initialItem) { mutableStateOf(initialItem.techStackCsv) }
    var liveDemoUrl by remember(initialItem) { mutableStateOf(initialItem.liveDemoUrl) }
    var repoUrl by remember(initialItem) { mutableStateOf(initialItem.repoUrl) }
    var readingTime by remember(initialItem) { mutableStateOf(initialItem.readingTimeMinutes.toString()) }
    var isFeatured by remember(initialItem) { mutableStateOf(initialItem.isFeatured) }
    var isPublished by remember(initialItem) { mutableStateOf(initialItem.isPublished) }
    var pushToCmsImmediately by remember { mutableStateOf(true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            mediaSource = uri.toString()
        }
    }

    val compatibleCategories = categories.filter {
        it.contentTypeScope == ContentType.ALL || it.contentTypeScope == contentType
    }.ifEmpty { categories }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .testTag("content_editor_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("editor_close_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close editor")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (initialItem.id == 0L) "Post to Contentful CMA" else "Update Contentful Entry",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Images • Descriptions • Download Links → Live Website",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (title.isBlank()) {
                                    validationError = "Please enter an entry title."
                                    return@Button
                                }
                                val finalCat = compatibleCategories.find { it.id == selectedCategoryId }
                                    ?: compatibleCategories.firstOrNull()
                                onSave(
                                    initialItem.copy(
                                        title = title.trim(),
                                        slug = slug.trim(),
                                        contentType = contentType,
                                        categoryId = finalCat?.id ?: 1L,
                                        categoryName = finalCat?.name ?: "General",
                                        summary = summary.trim().ifBlank { title.trim() },
                                        markdownBody = markdownBody.trim().ifBlank { summary.trim().ifBlank { title.trim() } },
                                        mediaSource = mediaSource.trim(),
                                        downloadUrl = downloadUrl.trim(),
                                        downloadLabel = downloadLabel.trim().ifBlank { "Download Asset" },
                                        downloadFileSize = downloadFileSize.trim(),
                                        versionTag = versionTag.trim().ifBlank { "v1.0.0" },
                                        photoCaption = photoCaption.trim(),
                                        photoLocation = photoLocation.trim(),
                                        exifCamera = exifCamera.trim(),
                                        techStackCsv = techStackCsv.trim(),
                                        liveDemoUrl = liveDemoUrl.trim(),
                                        repoUrl = repoUrl.trim(),
                                        readingTimeMinutes = readingTime.toIntOrNull()?.coerceIn(1, 60) ?: 4,
                                        isFeatured = isFeatured,
                                        isPublished = isPublished
                                    ),
                                    pushToCmsImmediately
                                )
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("editor_save_btn")
                        ) {
                            Icon(
                                imageVector = if (pushToCmsImmediately) Icons.Default.CloudUpload else Icons.Default.Save,
                                contentDescription = "Publish to Contentful CMA",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (pushToCmsImmediately) "Post to CMA" else "Save Local")
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (validationError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = validationError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Text(
                        text = "1. SELECT ENTRY TYPE & DYNAMIC CATEGORY",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            ContentType.PROJECT to "Project / Release",
                            ContentType.BLOG to "Article / Guide",
                            ContentType.PHOTO to "Media / Photo"
                        ).forEach { (typeKey, label) ->
                            FilterChip(
                                selected = contentType == typeKey,
                                onClick = { contentType = typeKey },
                                label = { Text(label) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("editor_type_chip_${typeKey.lowercase()}")
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        compatibleCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.name) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("editor_cat_chip_${cat.slug}")
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = "2. RICH MEDIA IMAGE & CONTENTFUL BINARY ASSET",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                PortfolioMediaImage(
                                    mediaSource = mediaSource,
                                    contentDescription = "Media preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("editor_pick_photo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Pick photo from device",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Select Image from Device (Uploads to Contentful Asset API)")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Or pick a Studio Media Preset:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(top = 6.dp)
                            ) {
                                listOf(
                                    "drawable:img_project_cloud" to "Cloud Dashboard",
                                    "drawable:img_photo_dhaka" to "Dhaka Twilight",
                                    "drawable:img_hero_banner" to "Cyber Matrix",
                                    "drawable:img_avatar_rasel" to "Developer Portrait"
                                ).forEach { (presetKey, presetName) ->
                                    FilterChip(
                                        selected = mediaSource == presetKey,
                                        onClick = { mediaSource = presetKey },
                                        label = { Text(presetName) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = mediaSource,
                                onValueChange = { mediaSource = it },
                                label = { Text("Image URI / Remote Contentful Asset URL") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("editor_media_source_input")
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = "3. DIRECT DOWNLOAD LINK & RELEASE PACKAGE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Attach a downloadable file, APK, ZIP, PDF, or 4K media link",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedTextField(
                                value = downloadUrl,
                                onValueChange = { downloadUrl = it },
                                label = { Text("Direct Download Link URL (https://...)") },
                                placeholder = { Text("https://github.com/raseldevbd/.../releases/download/v1.0/app.apk") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("editor_download_url_input")
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = downloadLabel,
                                    onValueChange = { downloadLabel = it },
                                    label = { Text("Download Button Label") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.6f)
                                        .testTag("editor_download_label_input")
                                )
                                OutlinedTextField(
                                    value = downloadFileSize,
                                    onValueChange = { downloadFileSize = it },
                                    label = { Text("File Size") },
                                    placeholder = { Text("14.8 MB") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("editor_download_size_input")
                                )
                                OutlinedTextField(
                                    value = versionTag,
                                    onValueChange = { versionTag = it },
                                    label = { Text("Version") },
                                    placeholder = { Text("v1.0.0") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("editor_version_tag_input")
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        downloadUrl = "https://github.com/raseldevbd/raseldevbd.github.io/archive/refs/heads/main.zip"
                                        downloadLabel = "Download Source Bundle (.ZIP)"
                                        downloadFileSize = "14.8 MB"
                                        versionTag = "v2.4.0"
                                    },
                                    label = { Text("Preset: GitHub ZIP") }
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        downloadUrl = "https://github.com/raseldevbd/raseldevbd.github.io/releases/latest/download/rasel-dev-bd.apk"
                                        downloadLabel = "Download Android APK"
                                        downloadFileSize = "18.2 MB"
                                        versionTag = "v1.0-APK"
                                    },
                                    label = { Text("Preset: Android APK") }
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        downloadUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=3840&q=90"
                                        downloadLabel = "Download 4K Media Pack"
                                        downloadFileSize = "28.4 MB"
                                        versionTag = "4K-UHD"
                                    },
                                    label = { Text("Preset: 4K Media") }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = "4. TITLE, DESCRIPTIONS & MARKDOWN CONTENT",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            validationError = null
                        },
                        label = { Text("Entry Title *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_title_input")
                    )

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Description / Summary * (Synced to Contentful 'summary' & 'description')") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_summary_input")
                    )

                    OutlinedTextField(
                        value = markdownBody,
                        onValueChange = { markdownBody = it },
                        label = { Text("Extended Rich Description / Markdown Body") },
                        minLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_markdown_input")
                    )

                    if (contentType == ContentType.PHOTO) {
                        OutlinedTextField(
                            value = photoCaption,
                            onValueChange = { photoCaption = it },
                            label = { Text("Photo Story Caption") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_photo_caption_input")
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = photoLocation,
                                onValueChange = { photoLocation = it },
                                label = { Text("Location (e.g., Dhaka)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("editor_photo_location_input")
                            )
                            OutlinedTextField(
                                value = exifCamera,
                                onValueChange = { exifCamera = it },
                                label = { Text("Camera / EXIF") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("editor_photo_exif_input")
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = liveDemoUrl,
                            onValueChange = { liveDemoUrl = it },
                            label = { Text("Live Website URL") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("editor_live_url_input")
                        )
                        OutlinedTextField(
                            value = repoUrl,
                            onValueChange = { repoUrl = it },
                            label = { Text("GitHub Repo URL") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("editor_repo_url_input")
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = techStackCsv,
                            onValueChange = { techStackCsv = it },
                            label = { Text("Tags / Technologies (comma-separated)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(2f)
                                .testTag("editor_tags_input")
                        )

                        OutlinedTextField(
                            value = slug,
                            onValueChange = { slug = it },
                            label = { Text("URL Slug") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("editor_slug_input")
                        )
                    }

                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Publish Directly to Contentful CMA",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Dispatches POST /entries + PUT /published via Contentful Content Management API for immediate live website updates",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = pushToCmsImmediately,
                                    onCheckedChange = { pushToCmsImmediately = it },
                                    modifier = Modifier.testTag("editor_push_cms_switch")
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "Feature Entry on Live Website",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Pin at the top of the live website feed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isFeatured,
                                    onCheckedChange = { isFeatured = it },
                                    modifier = Modifier.testTag("editor_featured_switch")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryEditorDialog(
    initialCategory: CategoryEntity,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit
) {
    var name by remember(initialCategory) { mutableStateOf(initialCategory.name) }
    var slug by remember(initialCategory) { mutableStateOf(initialCategory.slug) }
    var scope by remember(initialCategory) { mutableStateOf(initialCategory.contentTypeScope) }
    var accentHex by remember(initialCategory) { mutableStateOf(initialCategory.accentHex) }
    var description by remember(initialCategory) { mutableStateOf(initialCategory.description) }

    val colorPresets = listOf(
        "#10B981",
        "#06B6D4",
        "#3B82F6",
        "#F43F5E",
        "#F59E0B",
        "#A855F7"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("category_editor_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (initialCategory.id == 0L) "New Dynamic Category" else "Edit Dynamic Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cat_name_input")
                )

                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it },
                    label = { Text("Category Slug (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cat_slug_input")
                )

                Text(
                    text = "Content Type Scope:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    listOf(
                        ContentType.ALL to "All Types",
                        ContentType.PROJECT to "Projects",
                        ContentType.BLOG to "Articles",
                        ContentType.PHOTO to "Media"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = scope == key,
                            onClick = { scope = key },
                            label = { Text(label) }
                        )
                    }
                }

                Text(
                    text = "Accent Color:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorPresets.forEach { hex ->
                        val isSelected = accentHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable { accentHex = hex }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cat_desc_input")
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cat_cancel_btn")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    initialCategory.copy(
                                        name = name.trim(),
                                        slug = slug.trim(),
                                        contentTypeScope = scope,
                                        accentHex = accentHex,
                                        description = description.trim()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("cat_save_btn")
                    ) {
                        Text("Save Category")
                    }
                }
            }
        }
    }
}
