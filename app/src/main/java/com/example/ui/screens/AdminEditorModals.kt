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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CategoryEntity
import com.example.data.ContentItemEntity
import com.example.data.ContentType
import com.example.data.StaticSiteGenerator
import com.example.ui.components.PortfolioMediaImage
import com.example.ui.components.parseHexColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentEditorModal(
    initialItem: ContentItemEntity,
    categories: List<CategoryEntity>,
    onSave: (ContentItemEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onCancel)

    val context = LocalContext.current
    var contentType by remember(initialItem) { mutableStateOf(initialItem.contentType) }
    var title by remember(initialItem) { mutableStateOf(initialItem.title) }
    var slug by remember(initialItem) { mutableStateOf(initialItem.slug) }
    var summaryOrCaption by remember(initialItem) { mutableStateOf(initialItem.summaryOrCaption) }
    var bodyMarkdown by remember(initialItem) { mutableStateOf(initialItem.bodyMarkdown) }
    var categoryId by remember(initialItem) { mutableLongStateOf(initialItem.categoryId) }
    var mediaSource by remember(initialItem) { mutableStateOf(initialItem.mediaSource) }
    var tagsCsv by remember(initialItem) { mutableStateOf(initialItem.tagsCsv) }
    var liveUrl by remember(initialItem) { mutableStateOf(initialItem.liveUrl) }
    var repoUrl by remember(initialItem) { mutableStateOf(initialItem.repoUrl) }
    var isFeatured by remember(initialItem) { mutableStateOf(initialItem.isFeatured) }
    var isPublished by remember(initialItem) { mutableStateOf(initialItem.isPublished) }
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

    val studioPresets = listOf(
        "drawable:img_project_cloud" to "Cloud Dashboard",
        "drawable:img_photo_dhaka" to "Dhaka Twilight",
        "drawable:img_hero_banner" to "Cyber Matrix",
        "drawable:img_avatar_rasel" to "Developer Portrait"
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("content_editor_modal"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (initialItem.id == 0L) "Create New Content" else "Edit Content Item",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Changes sync with local Room DB & GitHub Pages Static Bundle",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("close_content_editor_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            }

            // 1. Content Type Selector (PROJECT / BLOG / PHOTO)
            Text(
                text = "1. CONTENT TYPE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ContentType.BLOG to "Blog Post",
                    ContentType.PHOTO to "Photo + Caption",
                    ContentType.PROJECT to "Project Showcase"
                ).forEach { (typeKey, typeLabel) ->
                    FilterChip(
                        selected = contentType == typeKey,
                        onClick = { contentType = typeKey },
                        label = { Text(typeLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("editor_type_chip_${typeKey.lowercase()}")
                    )
                }
            }

            // 2. Dynamic Category Assignment
            Text(
                text = "2. DYNAMIC CATEGORY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = categoryId == cat.id
                    val accent = parseHexColor(cat.colorHex, MaterialTheme.colorScheme.primary)
                    FilterChip(
                        selected = isSelected,
                        onClick = { categoryId = cat.id },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.Black else accent)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accent,
                            selectedLabelColor = Color(0xFF04131A)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_category_${cat.slug}")
                    )
                }
            }

            // 3. Title & URL Slug
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (initialItem.id == 0L) {
                        slug = StaticSiteGenerator.toSlug(it)
                    }
                    validationError = null
                },
                label = {
                    Text(
                        when (contentType) {
                            ContentType.PHOTO -> "Photo Title *"
                            ContentType.PROJECT -> "Project Name *"
                            else -> "Blog Article Title *"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_title_input")
            )

            OutlinedTextField(
                value = slug,
                onValueChange = { slug = it },
                label = { Text("Static Route Slug (for GitHub Pages)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_slug_input")
            )

            // 4. Media Upload & Preview
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "3. MEDIA UPLOAD & STATIC ASSET SOURCE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    PortfolioMediaImage(
                        mediaSource = mediaSource,
                        contentDescription = "Media preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("editor_pick_device_photo_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick Photo")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Photo from Device Gallery")
                        }
                    }

                    Text(
                        text = "Or choose a bundled studio visual:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        studioPresets.forEach { (presetKey, presetLabel) ->
                            FilterChip(
                                selected = mediaSource == presetKey,
                                onClick = { mediaSource = presetKey },
                                label = { Text(presetLabel, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }

                    OutlinedTextField(
                        value = mediaSource,
                        onValueChange = { mediaSource = it },
                        label = { Text("Media Source URI or Static Path (e.g., ./assets/photo.jpg)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_media_source_input")
                    )
                }
            }

            // 5. Summary / Photo Caption & Markdown Body
            OutlinedTextField(
                value = summaryOrCaption,
                onValueChange = {
                    summaryOrCaption = it
                    validationError = null
                },
                label = {
                    Text(
                        when (contentType) {
                            ContentType.PHOTO -> "Photo Caption & Visual Story *"
                            ContentType.PROJECT -> "Project Elevator Pitch *"
                            else -> "Article Summary / Excerpt *"
                        }
                    )
                },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_caption_input")
            )

            OutlinedTextField(
                value = bodyMarkdown,
                onValueChange = { bodyMarkdown = it },
                label = {
                    Text(
                        when (contentType) {
                            ContentType.PHOTO -> "Behind-the-Shot Notes (Markdown supported)"
                            ContentType.PROJECT -> "Technical Architecture & Case Study (Markdown + ```code```)"
                            else -> "Full Blog Post Content (Markdown + ```code``` blocks)"
                        }
                    )
                },
                minLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_body_input")
            )

            // 6. Context Metadata Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = liveUrl,
                    onValueChange = { liveUrl = it },
                    label = {
                        Text(
                            when (contentType) {
                                ContentType.PHOTO -> "Location (e.g. Dhaka)"
                                ContentType.PROJECT -> "Live Demo URL"
                                else -> "Reading Time (e.g. 5 min)"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_live_url_input")
                )

                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = { repoUrl = it },
                    label = {
                        Text(
                            when (contentType) {
                                ContentType.PHOTO -> "Camera / EXIF"
                                ContentType.PROJECT -> "GitHub Repo URL"
                                else -> "Canonical Path"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_repo_url_input")
                )
            }

            OutlinedTextField(
                value = tagsCsv,
                onValueChange = { tagsCsv = it },
                label = { Text("Tags (comma-separated, e.g., React 19, TypeScript, Dhaka)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_tags_input")
            )

            // 7. Published & Featured Switches
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Publish to Live Portfolio & GitHub Pages", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Turn off to save as a private Draft in Admin Studio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPublished,
                            onCheckedChange = { isPublished = it },
                            modifier = Modifier.testTag("editor_published_switch")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Highlight as Featured Showcase", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Pins item to the top of feeds with a Featured badge",
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

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Save & Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("editor_cancel_btn")
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            validationError = "Please enter a title."
                            return@Button
                        }
                        if (summaryOrCaption.isBlank()) {
                            validationError = "Please enter a caption or summary."
                            return@Button
                        }
                        val selectedCat = categories.find { it.id == categoryId }
                        onSave(
                            initialItem.copy(
                                contentType = contentType,
                                title = title.trim(),
                                slug = slug.trim().ifBlank { StaticSiteGenerator.toSlug(title) },
                                summaryOrCaption = summaryOrCaption.trim(),
                                bodyMarkdown = bodyMarkdown.trim().ifBlank { summaryOrCaption.trim() },
                                categoryId = categoryId,
                                categoryName = selectedCat?.name ?: "General",
                                mediaSource = mediaSource.trim().ifBlank { "drawable:img_hero_banner" },
                                tagsCsv = tagsCsv.trim(),
                                liveUrl = liveUrl.trim(),
                                repoUrl = repoUrl.trim(),
                                isFeatured = isFeatured,
                                isPublished = isPublished
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("editor_save_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Content")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorModal(
    initialCategory: CategoryEntity,
    onSave: (CategoryEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onCancel)

    var name by remember(initialCategory) { mutableStateOf(initialCategory.name) }
    var slug by remember(initialCategory) { mutableStateOf(initialCategory.slug) }
    var scope by remember(initialCategory) { mutableStateOf(initialCategory.contentTypeScope) }
    var colorHex by remember(initialCategory) { mutableStateOf(initialCategory.colorHex) }
    var description by remember(initialCategory) { mutableStateOf(initialCategory.description) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val colorPalette = listOf(
        "#10B981", // Cyber Emerald
        "#06B6D4", // Electric Cyan
        "#8B5CF6", // Violet Syntax
        "#F59E0B", // Amber Highlight
        "#F43F5E", // Bengal Coral
        "#3B82F6", // Royal Blue
        "#EC4899"  // Neon Magenta
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("category_editor_modal"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialCategory.id == 0L) "New Dynamic Category" else "Edit Dynamic Category",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (initialCategory.id == 0L) {
                        slug = StaticSiteGenerator.toSlug(it)
                    }
                    errorText = null
                },
                label = { Text("Category Name * (e.g. Next.js & Edge)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_name_input")
            )

            OutlinedTextField(
                value = slug,
                onValueChange = { slug = it },
                label = { Text("Category URL Slug") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_slug_input")
            )

            Text(
                text = "CONTENT TYPE SCOPE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ContentType.ALL to "All Types",
                    ContentType.PROJECT to "Projects Only",
                    ContentType.BLOG to "Blogs Only",
                    ContentType.PHOTO to "Photos Only"
                ).forEach { (scopeKey, scopeLabel) ->
                    FilterChip(
                        selected = scope == scopeKey,
                        onClick = { scope = scopeKey },
                        label = { Text(scopeLabel) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("category_scope_${scopeKey.lowercase()}")
                    )
                }
            }

            Text(
                text = "ACCENT COLOR TOKEN",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colorPalette.forEach { hex ->
                    val swatchColor = parseHexColor(hex)
                    val isSelected = colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { colorHex = hex }
                            .testTag("color_swatch_${hex.removePrefix("#")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected color",
                                tint = Color(0xFF04131A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Category Description") },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_description_input")
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorText = "Category name is required."
                            return@Button
                        }
                        onSave(
                            initialCategory.copy(
                                name = name.trim(),
                                slug = slug.trim().ifBlank { StaticSiteGenerator.toSlug(name) },
                                contentTypeScope = scope,
                                colorHex = colorHex,
                                description = description.trim()
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("save_category_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save Category")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Category")
                }
            }
        }
    }
}
