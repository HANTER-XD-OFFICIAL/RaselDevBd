package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParsedStaticBundle(
    val config: SiteConfigEntity?,
    val categories: List<CategoryEntity>,
    val items: List<ContentItemEntity>
)

object StaticSiteGenerator {

    fun generateStaticJsonBundle(
        config: SiteConfigEntity,
        categories: List<CategoryEntity>,
        items: List<ContentItemEntity>
    ): String {
        val root = JSONObject()
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

        root.put("schemaVersion", "3.0.0-contentful-cma")
        root.put("generatedBy", "Rasel Dev BD — Contentful CMA & GitHub Pages Engine")
        root.put("generatedAtIso", isoFormatter.format(Date()))

        val cmsMeta = JSONObject().apply {
            put("provider", config.cmsProvider)
            put("contentfulEnvironment", config.contentfulEnvironment)
            put("contentfulContentType", config.contentfulContentType)
            put("contentfulLocale", config.contentfulLocale)
            put("supportsDirectCmaPublishing", true)
            put("supportsBinaryAssetUploads", true)
            put("supportsDownloadLinks", true)
        }
        root.put("headlessCms", cmsMeta)

        val siteMeta = JSONObject().apply {
            put("siteTitle", config.siteTitle)
            put("tagline", config.tagline)
            put("ownerName", config.ownerName)
            put("ownerRole", config.ownerRole)
            put("bio", config.bio)
            put("location", config.location)
            put("email", config.email)
            put("githubUrl", config.githubUrl)
            put("githubPagesRepo", config.githubPagesRepo)
            put("customDomain", config.customDomain)
        }
        root.put("siteConfig", siteMeta)

        val catArray = JSONArray()
        categories.sortedBy { it.sortOrder }.forEach { cat ->
            val catObj = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("slug", cat.slug)
                put("contentTypeScope", cat.contentTypeScope)
                put("accentHex", cat.accentHex)
                put("description", cat.description)
                put("sortOrder", cat.sortOrder)
            }
            catArray.put(catObj)
        }
        root.put("categories", catArray)

        val itemsArray = JSONArray()
        items.filter { it.isPublished }.forEach { item ->
            val itemObj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("slug", item.slug)
                put("contentType", item.contentType)
                put("categoryId", item.categoryId)
                put("categoryName", item.categoryName)
                put("summary", item.summary)
                put("description", item.summary)
                put("markdownBody", item.markdownBody)
                put("mediaUrl", resolveWebMediaPath(item.mediaSource))
                put("rawMediaSource", item.mediaSource)
                put("downloadUrl", item.downloadUrl)
                put("downloadLabel", item.downloadLabel)
                put("downloadFileSize", item.downloadFileSize)
                put("versionTag", item.versionTag)
                put("photoCaption", item.photoCaption)
                put("photoLocation", item.photoLocation)
                put("exifCamera", item.exifCamera)
                put("techStack", JSONArray(item.techStackCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }))
                put("liveDemoUrl", item.liveDemoUrl)
                put("repoUrl", item.repoUrl)
                put("readingTimeMinutes", item.readingTimeMinutes)
                put("isFeatured", item.isFeatured)
                put("isPublished", item.isPublished)
                put("createdAtEpoch", item.createdAtEpoch)
                put("updatedAtEpoch", item.updatedAtEpoch)
                put("cmsEntryId", item.cmsEntryId)
                put("cmsAssetId", item.cmsAssetId)
                put("cmsVersion", item.cmsVersion)
                put("cmsProvider", item.cmsProvider)
            }
            itemsArray.put(itemObj)
        }
        root.put("items", itemsArray)

        return root.toString(2)
    }

    fun parseStaticJsonBundle(jsonString: String): ParsedStaticBundle {
        val root = JSONObject(jsonString)

        val siteObj = root.optJSONObject("siteConfig")
        val cmsObj = root.optJSONObject("headlessCms")
        val parsedConfig = if (siteObj != null) {
            SiteConfigEntity(
                id = 1,
                siteTitle = siteObj.optString("siteTitle", "Rasel Dev BD"),
                tagline = siteObj.optString("tagline", ""),
                ownerName = siteObj.optString("ownerName", "Rasel Chowdhury"),
                ownerRole = siteObj.optString("ownerRole", "Senior Full-Stack Engineer"),
                bio = siteObj.optString("bio", ""),
                location = siteObj.optString("location", "Dhaka, Bangladesh"),
                email = siteObj.optString("email", "alexraselchodhury@gmail.com"),
                githubUrl = siteObj.optString("githubUrl", "https://github.com/raseldevbd"),
                githubPagesRepo = siteObj.optString("githubPagesRepo", "raseldevbd/raseldevbd.github.io"),
                customDomain = siteObj.optString("customDomain", "raseldevbd.github.io"),
                cmsProvider = cmsObj?.optString("provider", CmsProviderType.CONTENTFUL) ?: CmsProviderType.CONTENTFUL,
                contentfulEnvironment = cmsObj?.optString("contentfulEnvironment", "master") ?: "master",
                contentfulContentType = cmsObj?.optString("contentfulContentType", "portfolioItem") ?: "portfolioItem",
                contentfulLocale = cmsObj?.optString("contentfulLocale", "en-US") ?: "en-US"
            )
        } else null

        val parsedCategories = mutableListOf<CategoryEntity>()
        val catArray = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until catArray.length()) {
            val c = catArray.getJSONObject(i)
            parsedCategories.add(
                CategoryEntity(
                    id = c.optLong("id", (i + 1).toLong()),
                    name = c.optString("name", "Category"),
                    slug = c.optString("slug", "category-$i"),
                    contentTypeScope = c.optString("contentTypeScope", ContentType.ALL),
                    accentHex = c.optString("accentHex", "#10B981"),
                    description = c.optString("description", ""),
                    sortOrder = c.optInt("sortOrder", i)
                )
            )
        }

        val parsedItems = mutableListOf<ContentItemEntity>()
        val itemsArray = root.optJSONArray("items") ?: JSONArray()
        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            val techArr = item.optJSONArray("techStack")
            val techCsv = if (techArr != null) {
                (0 until techArr.length()).joinToString(",") { techArr.optString(it) }
            } else {
                item.optString("techStackCsv", "")
            }
            parsedItems.add(
                ContentItemEntity(
                    id = item.optLong("id", (i + 1).toLong()),
                    title = item.optString("title", "Untitled"),
                    slug = item.optString("slug", "item-$i"),
                    contentType = item.optString("contentType", ContentType.BLOG),
                    categoryId = item.optLong("categoryId", 1L),
                    categoryName = item.optString("categoryName", "General"),
                    summary = item.optString("summary", item.optString("description", "")),
                    markdownBody = item.optString("markdownBody", ""),
                    mediaSource = item.optString("rawMediaSource", item.optString("mediaUrl", "drawable:img_project_cloud")),
                    photoCaption = item.optString("photoCaption", ""),
                    photoLocation = item.optString("photoLocation", ""),
                    exifCamera = item.optString("exifCamera", ""),
                    downloadUrl = item.optString("downloadUrl", ""),
                    downloadLabel = item.optString("downloadLabel", "Download Asset"),
                    downloadFileSize = item.optString("downloadFileSize", ""),
                    versionTag = item.optString("versionTag", "v1.0"),
                    techStackCsv = techCsv,
                    liveDemoUrl = item.optString("liveDemoUrl", ""),
                    repoUrl = item.optString("repoUrl", ""),
                    readingTimeMinutes = item.optInt("readingTimeMinutes", 4),
                    isFeatured = item.optBoolean("isFeatured", false),
                    isPublished = item.optBoolean("isPublished", true),
                    createdAtEpoch = item.optLong("createdAtEpoch", System.currentTimeMillis()),
                    updatedAtEpoch = item.optLong("updatedAtEpoch", System.currentTimeMillis()),
                    cmsEntryId = item.optString("cmsEntryId", ""),
                    cmsAssetId = item.optString("cmsAssetId", ""),
                    cmsVersion = item.optInt("cmsVersion", 1),
                    cmsProvider = item.optString("cmsProvider", CmsProviderType.CONTENTFUL)
                )
            )
        }

        return ParsedStaticBundle(parsedConfig, parsedCategories, parsedItems)
    }

    fun generateHeadlessCmsClientJs(config: SiteConfigEntity): String {
        return """
/**
 * Rasel Dev BD — Contentful Content Management API (CMA) & Delivery API Client
 * Reads and publishes rich media content (images, descriptions, download links)
 * directly to Contentful for immediate live website updates.
 */

export async function fetchFromContentful(spaceId, environment = '${config.contentfulEnvironment}', token) {
  const url = `https://cdn.contentful.com/spaces/${'$'}{spaceId}/environments/${'$'}{environment}/entries?include=2&limit=100`;
  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${'$'}{token}` }
  });
  if (!res.ok) throw new Error(`Contentful HTTP ${'$'}{res.status}`);
  const data = await res.json();

  const assetMap = {};
  (data.includes?.Asset || []).forEach((asset) => {
    const fileUrl = asset.fields?.file?.url || '';
    if (asset.sys?.id && fileUrl) {
      assetMap[asset.sys.id] = fileUrl.startsWith('//') ? `https:${'$'}{fileUrl}` : fileUrl;
    }
  });

  return (data.items || []).map((entry, idx) => {
    const f = entry.fields || {};
    const assetId = f.media?.sys?.id;
    return {
      id: entry.sys?.id || `cf-${'$'}{idx}`,
      cmsEntryId: entry.sys?.id || '',
      cmsVersion: entry.sys?.version || 1,
      cmsProvider: 'CONTENTFUL',
      title: f.title || 'Untitled Entry',
      slug: f.slug || `entry-${'$'}{idx}`,
      contentType: (f.contentType || 'BLOG').toUpperCase(),
      categoryName: f.categoryName || 'React & Headless CMS',
      summary: f.summary || f.description || '',
      markdownBody: f.markdownBody || f.body || '',
      mediaUrl: f.mediaSource || assetMap[assetId] || '',
      downloadUrl: f.downloadUrl || '',
      downloadLabel: f.downloadLabel || 'Download Package',
      downloadFileSize: f.downloadFileSize || '',
      versionTag: f.versionTag || 'v1.0',
      photoCaption: f.photoCaption || '',
      liveDemoUrl: f.liveDemoUrl || '',
      repoUrl: f.repoUrl || '',
      isFeatured: Boolean(f.isFeatured)
    };
  });
}

export async function publishToContentfulCma(item, spaceId, environment = '${config.contentfulEnvironment}', cmaToken) {
  const loc = (v) => ({ '${config.contentfulLocale}': v });
  const body = {
    fields: {
      title: loc(item.title),
      slug: loc(item.slug),
      contentType: loc(item.contentType),
      categoryName: loc(item.categoryName),
      summary: loc(item.summary || ''),
      description: loc(item.summary || ''),
      markdownBody: loc(item.markdownBody || ''),
      mediaSource: loc(item.mediaUrl || ''),
      downloadUrl: loc(item.downloadUrl || ''),
      downloadLabel: loc(item.downloadLabel || 'Download Asset'),
      downloadFileSize: loc(item.downloadFileSize || ''),
      versionTag: loc(item.versionTag || 'v1.0'),
      photoCaption: loc(item.photoCaption || ''),
      techStackCsv: loc((item.techStack || []).join(',')),
      liveDemoUrl: loc(item.liveDemoUrl || ''),
      repoUrl: loc(item.repoUrl || ''),
      isFeatured: loc(Boolean(item.isFeatured))
    }
  };

  const createRes = await fetch(
    `https://api.contentful.com/spaces/${'$'}{spaceId}/environments/${'$'}{environment}/entries`,
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${'$'}{cmaToken}`,
        'Content-Type': 'application/vnd.contentful.management.v1+json',
        'X-Contentful-Content-Type': '${config.contentfulContentType}'
      },
      body: JSON.stringify(body)
    }
  );
  if (!createRes.ok) throw new Error(`Contentful CMA HTTP ${'$'}{createRes.status}`);
  const created = await createRes.json();
  const entryId = created.sys?.id;
  const version = created.sys?.version || 1;

  if (entryId) {
    await fetch(
      `https://api.contentful.com/spaces/${'$'}{spaceId}/environments/${'$'}{environment}/entries/${'$'}{entryId}/published`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${'$'}{cmaToken}`,
          'X-Contentful-Version': String(version)
        }
      }
    );
  }
  return { entryId, version: version + 1 };
}
""".trimIndent()
    }

    fun generateReactAppJsx(
        config: SiteConfigEntity,
        categories: List<CategoryEntity>,
        items: List<ContentItemEntity>
    ): String {
        return """
import React, { useState, useEffect } from 'react';
import { fetchFromContentful } from './cmsClient';

export default function App() {
  const [items, setItems] = useState([]);
  const [selectedType, setSelectedType] = useState('ALL');

  useEffect(() => {
    fetch('./data/content-bundle.json')
      .then((r) => r.json())
      .then((bundle) => setItems(bundle.items || []))
      .catch(() => {});
  }, []);

  const visible = items.filter((i) => selectedType === 'ALL' || i.contentType === selectedType);

  return (
    <div className="min-h-screen bg-[#090E1A] text-slate-100 p-6">
      <header className="max-w-6xl mx-auto mb-8 flex items-center justify-between">
        <div>
          <span className="text-xs font-mono text-emerald-400">CONTENTFUL CMA LIVE WEBSITE</span>
          <h1 className="text-3xl font-bold text-white">${config.siteTitle}</h1>
        </div>
        <div className="flex gap-2">
          {['ALL', 'PROJECT', 'BLOG', 'PHOTO'].map((t) => (
            <button
              key={t}
              onClick={() => setSelectedType(t)}
              className="px-4 py-2 rounded-lg bg-slate-800 text-xs font-mono"
            >
              {t}
            </button>
          ))}
        </div>
      </header>
      <main className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
        {visible.map((item) => (
          <article key={item.id || item.slug} className="rounded-2xl bg-[#111827] border border-slate-800 overflow-hidden">
            <img src={item.mediaUrl} alt={item.title} className="w-full h-48 object-cover" />
            <div className="p-5 space-y-3">
              <h2 className="text-lg font-bold text-white">{item.title}</h2>
              <p className="text-sm text-slate-300">{item.summary}</p>
              {item.downloadUrl && (
                <a
                  href={item.downloadUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-xs"
                >
                  ⬇ {item.downloadLabel || 'Download'} {item.downloadFileSize ? `(${'$'}{item.downloadFileSize})` : ''}
                </a>
              )}
            </div>
          </article>
        ))}
      </main>
    </div>
  );
}
""".trimIndent()
    }

    fun generateIndexHtml(config: SiteConfigEntity): String {
        return """
<!DOCTYPE html>
<html lang="en" class="dark">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${config.siteTitle} — ${config.ownerName}</title>
  <meta name="description" content="${config.bio}" />
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-[#090E1A] text-slate-100 antialiased">
  <div id="root"></div>
  <script type="module" src="/src/main.jsx"></script>
</body>
</html>
""".trimIndent()
    }

    fun generateGithubActionsWorkflow(config: SiteConfigEntity): String {
        return """
name: Deploy Rasel Dev BD (Contentful CMA + React) to GitHub Pages

on:
  push:
    branches: ["main"]
  repository_dispatch:
    types: ["contentful-cma-publish"]
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

jobs:
  build-and-deploy:
    environment:
      name: github-pages
      url: ${'$'}{{ steps.deployment.outputs.page_url }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 20
      - working-directory: ./github-pages-site
        env:
          VITE_CONTENTFUL_SPACE_ID: ${'$'}{{ secrets.VITE_CONTENTFUL_SPACE_ID }}
          VITE_CONTENTFUL_ENVIRONMENT: ${'$'}{{ vars.VITE_CONTENTFUL_ENVIRONMENT || '${config.contentfulEnvironment}' }}
          VITE_CONTENTFUL_DELIVERY_TOKEN: ${'$'}{{ secrets.VITE_CONTENTFUL_DELIVERY_TOKEN }}
        run: |
          npm install
          npm run build
          cp dist/index.html dist/404.html
          touch dist/.nojekyll
      - uses: actions/upload-pages-artifact@v3
        with:
          path: ./github-pages-site/dist
      - id: deployment
        uses: actions/deploy-pages@v4
""".trimIndent()
    }

    private fun resolveWebMediaPath(mediaSource: String): String {
        return when {
            mediaSource.startsWith("http://") || mediaSource.startsWith("https://") -> mediaSource
            mediaSource.contains("img_project_cloud") -> "https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80"
            mediaSource.contains("img_photo_dhaka") -> "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80"
            mediaSource.contains("img_avatar_rasel") -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=600&q=80"
            else -> "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=1200&q=80"
        }
    }
}
