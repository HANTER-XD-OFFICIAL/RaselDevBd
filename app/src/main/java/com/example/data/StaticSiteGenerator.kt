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

        root.put("schemaVersion", "2.0.0-headless-cms")
        root.put("generatedBy", "Rasel Dev BD — Headless CMS & GitHub Pages Engine")
        root.put("generatedAtIso", isoFormatter.format(Date()))

        val cmsMeta = JSONObject().apply {
            put("provider", config.cmsProvider)
            put("contentfulSpaceId", config.contentfulSpaceId)
            put("contentfulEnvironment", config.contentfulEnvironment)
            put("strapiBaseUrl", config.strapiBaseUrl)
            put("supportsDirectPublishing", true)
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
                put("markdownBody", item.markdownBody)
                put("mediaUrl", resolveWebMediaPath(item.mediaSource))
                put("rawMediaSource", item.mediaSource)
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
                contentfulSpaceId = cmsObj?.optString("contentfulSpaceId", "") ?: "",
                contentfulEnvironment = cmsObj?.optString("contentfulEnvironment", "master") ?: "master",
                strapiBaseUrl = cmsObj?.optString("strapiBaseUrl", "https://cms.raseldevbd.com") ?: "https://cms.raseldevbd.com"
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
                    summary = item.optString("summary", ""),
                    markdownBody = item.optString("markdownBody", ""),
                    mediaSource = item.optString("rawMediaSource", item.optString("mediaUrl", "drawable:img_project_cloud")),
                    photoCaption = item.optString("photoCaption", ""),
                    photoLocation = item.optString("photoLocation", ""),
                    exifCamera = item.optString("exifCamera", ""),
                    techStackCsv = techCsv,
                    liveDemoUrl = item.optString("liveDemoUrl", ""),
                    repoUrl = item.optString("repoUrl", ""),
                    readingTimeMinutes = item.optInt("readingTimeMinutes", 4),
                    isFeatured = item.optBoolean("isFeatured", false),
                    isPublished = item.optBoolean("isPublished", true),
                    createdAtEpoch = item.optLong("createdAtEpoch", System.currentTimeMillis()),
                    updatedAtEpoch = item.optLong("updatedAtEpoch", System.currentTimeMillis()),
                    cmsEntryId = item.optString("cmsEntryId", ""),
                    cmsProvider = item.optString("cmsProvider", "")
                )
            )
        }

        return ParsedStaticBundle(parsedConfig, parsedCategories, parsedItems)
    }

    fun generateHeadlessCmsClientJs(config: SiteConfigEntity): String {
        return """
/**
 * Rasel Dev BD — Headless CMS Integration Client (Contentful & Strapi)
 * Allows fetching and posting blogs, project showcases, and photos with captions
 * directly to Contentful or Strapi without changing code or redeploying GitHub Pages.
 */

const STORAGE_KEY = 'rasel_dev_bd_cms_settings_v2';

export function getCmsConfig() {
  const saved = typeof window !== 'undefined' ? localStorage.getItem(STORAGE_KEY) : null;
  const parsed = saved ? JSON.parse(saved) : {};
  const env = typeof import.meta !== 'undefined' && import.meta.env ? import.meta.env : {};

  return {
    provider: parsed.provider || env.VITE_CMS_PROVIDER || '${config.cmsProvider}',
    contentfulSpaceId: parsed.contentfulSpaceId || env.VITE_CONTENTFUL_SPACE_ID || '${config.contentfulSpaceId}',
    contentfulEnvironment: parsed.contentfulEnvironment || env.VITE_CONTENTFUL_ENVIRONMENT || '${config.contentfulEnvironment}',
    contentfulDeliveryToken: parsed.contentfulDeliveryToken || env.VITE_CONTENTFUL_DELIVERY_TOKEN || '',
    contentfulManagementToken: parsed.contentfulManagementToken || env.VITE_CONTENTFUL_MANAGEMENT_TOKEN || '',
    strapiBaseUrl: parsed.strapiBaseUrl || env.VITE_STRAPI_BASE_URL || '${config.strapiBaseUrl}',
    strapiApiToken: parsed.strapiApiToken || env.VITE_STRAPI_API_TOKEN || '',
  };
}

export function saveCmsConfig(newConfig) {
  if (typeof window !== 'undefined') {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(newConfig));
  }
}

export async function fetchHeadlessContent(customConfig) {
  const cfg = customConfig || getCmsConfig();
  if (cfg.provider === 'STRAPI') {
    return fetchFromStrapi(cfg);
  }
  return fetchFromContentful(cfg);
}

export async function publishToHeadlessCms(postItem, customConfig) {
  const cfg = customConfig || getCmsConfig();
  if (cfg.provider === 'STRAPI') {
    return publishToStrapi(postItem, cfg);
  }
  return publishToContentful(postItem, cfg);
}

async function fetchFromContentful(cfg) {
  if (!cfg.contentfulSpaceId || !cfg.contentfulDeliveryToken) {
    throw new Error('Contentful Space ID and Delivery Token are not configured.');
  }
  const env = cfg.contentfulEnvironment || 'master';
  const url = `https://cdn.contentful.com/spaces/${'$'}{cfg.contentfulSpaceId}/environments/${'$'}{env}/entries?include=2&limit=100`;
  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${'$'}{cfg.contentfulDeliveryToken}` }
  });
  if (!res.ok) {
    throw new Error(`Contentful CDA HTTP ${'$'}{res.status}`);
  }
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
      cmsProvider: 'CONTENTFUL',
      title: f.title || 'Untitled Entry',
      slug: f.slug || `entry-${'$'}{idx}`,
      contentType: (f.contentType || 'BLOG').toUpperCase(),
      categoryName: f.categoryName || 'React & Headless CMS',
      summary: f.summary || '',
      markdownBody: f.markdownBody || f.body || '',
      mediaUrl: f.mediaSource || assetMap[assetId] || '',
      photoCaption: f.photoCaption || '',
      photoLocation: f.photoLocation || '',
      exifCamera: f.exifCamera || '',
      techStack: typeof f.techStackCsv === 'string'
        ? f.techStackCsv.split(',').map((s) => s.trim()).filter(Boolean)
        : (f.techStack || []),
      liveDemoUrl: f.liveDemoUrl || '',
      repoUrl: f.repoUrl || '',
      readingTimeMinutes: f.readingTimeMinutes || 5,
      isFeatured: Boolean(f.isFeatured),
      isPublished: true
    };
  });
}

async function publishToContentful(item, cfg) {
  if (!cfg.contentfulSpaceId || !cfg.contentfulManagementToken) {
    throw new Error('Contentful Space ID and Management Token (CMA) are required to publish.');
  }
  const env = cfg.contentfulEnvironment || 'master';
  const loc = (v) => ({ 'en-US': v });
  const body = {
    fields: {
      title: loc(item.title),
      slug: loc(item.slug),
      contentType: loc(item.contentType),
      categoryName: loc(item.categoryName),
      summary: loc(item.summary || ''),
      markdownBody: loc(item.markdownBody || ''),
      mediaSource: loc(item.mediaUrl || ''),
      photoCaption: loc(item.photoCaption || ''),
      photoLocation: loc(item.photoLocation || ''),
      exifCamera: loc(item.exifCamera || ''),
      techStackCsv: loc((item.techStack || []).join(',')),
      liveDemoUrl: loc(item.liveDemoUrl || ''),
      repoUrl: loc(item.repoUrl || ''),
      readingTimeMinutes: loc(Number(item.readingTimeMinutes || 5)),
      isFeatured: loc(Boolean(item.isFeatured))
    }
  };

  const createRes = await fetch(
    `https://api.contentful.com/spaces/${'$'}{cfg.contentfulSpaceId}/environments/${'$'}{env}/entries`,
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${'$'}{cfg.contentfulManagementToken}`,
        'Content-Type': 'application/vnd.contentful.management.v1+json',
        'X-Contentful-Content-Type': 'portfolioItem'
      },
      body: JSON.stringify(body)
    }
  );
  if (!createRes.ok) {
    throw new Error(`Contentful CMA HTTP ${'$'}{createRes.status}`);
  }
  const created = await createRes.json();
  const entryId = created.sys?.id;
  const version = created.sys?.version || 1;

  if (entryId) {
    await fetch(
      `https://api.contentful.com/spaces/${'$'}{cfg.contentfulSpaceId}/environments/${'$'}{env}/entries/${'$'}{entryId}/published`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${'$'}{cfg.contentfulManagementToken}`,
          'X-Contentful-Version': String(version)
        }
      }
    );
  }
  return { entryId, provider: 'CONTENTFUL' };
}

async function fetchFromStrapi(cfg) {
  const base = (cfg.strapiBaseUrl || '').replace(/\/+$/, '');
  if (!base.startsWith('http')) {
    throw new Error('Strapi Base URL is not configured.');
  }
  const headers = { Accept: 'application/json' };
  if (cfg.strapiApiToken) {
    headers.Authorization = `Bearer ${'$'}{cfg.strapiApiToken}`;
  }
  const res = await fetch(`${'$'}{base}/api/portfolio-items?populate=*&sort=updatedAt:desc`, { headers });
  if (!res.ok) {
    throw new Error(`Strapi REST HTTP ${'$'}{res.status}`);
  }
  const json = await res.json();
  return (json.data || []).map((row, idx) => {
    const a = row.attributes || row;
    return {
      id: row.documentId || row.id || `strapi-${'$'}{idx}`,
      cmsEntryId: String(row.documentId || row.id || ''),
      cmsProvider: 'STRAPI',
      title: a.title || 'Untitled',
      slug: a.slug || `post-${'$'}{idx}`,
      contentType: (a.contentType || 'BLOG').toUpperCase(),
      categoryName: a.categoryName || 'React & Headless CMS',
      summary: a.summary || '',
      markdownBody: a.markdownBody || a.body || '',
      mediaUrl: a.mediaSource || '',
      photoCaption: a.photoCaption || '',
      photoLocation: a.photoLocation || '',
      exifCamera: a.exifCamera || '',
      techStack: typeof a.techStackCsv === 'string'
        ? a.techStackCsv.split(',').map((s) => s.trim()).filter(Boolean)
        : (a.techStack || []),
      liveDemoUrl: a.liveDemoUrl || '',
      repoUrl: a.repoUrl || '',
      readingTimeMinutes: a.readingTimeMinutes || 5,
      isFeatured: Boolean(a.isFeatured),
      isPublished: true
    };
  });
}

async function publishToStrapi(item, cfg) {
  const base = (cfg.strapiBaseUrl || '').replace(/\/+$/, '');
  if (!base.startsWith('http') || !cfg.strapiApiToken) {
    throw new Error('Strapi Base URL and API Token are required to publish.');
  }
  const res = await fetch(`${'$'}{base}/api/portfolio-items`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${'$'}{cfg.strapiApiToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      data: {
        title: item.title,
        slug: item.slug,
        contentType: item.contentType,
        categoryName: item.categoryName,
        summary: item.summary || '',
        markdownBody: item.markdownBody || '',
        mediaSource: item.mediaUrl || '',
        photoCaption: item.photoCaption || '',
        photoLocation: item.photoLocation || '',
        exifCamera: item.exifCamera || '',
        techStackCsv: (item.techStack || []).join(','),
        liveDemoUrl: item.liveDemoUrl || '',
        repoUrl: item.repoUrl || '',
        readingTimeMinutes: Number(item.readingTimeMinutes || 5),
        isFeatured: Boolean(item.isFeatured)
      }
    })
  });
  if (!res.ok) {
    throw new Error(`Strapi POST HTTP ${'$'}{res.status}`);
  }
  const created = await res.json();
  return { entryId: created.data?.documentId || created.data?.id || '', provider: 'STRAPI' };
}
""".trimIndent()
    }

    fun generateReactAppJsx(
        config: SiteConfigEntity,
        categories: List<CategoryEntity>,
        items: List<ContentItemEntity>
    ): String {
        return """
import React, { useState, useEffect, useMemo } from 'react';
import {
  getCmsConfig,
  saveCmsConfig,
  fetchHeadlessContent,
  publishToHeadlessCms
} from './cmsClient';

export default function App() {
  const [siteData, setSiteData] = useState(null);
  const [activeTab, setActiveTab] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [cmsConfig, setCmsConfig] = useState(() => getCmsConfig());
  const [cmsStatus, setCmsStatus] = useState('Hybrid Mode • Contentful / Strapi + Static JSON');
  const [showCmsModal, setShowCmsModal] = useState(false);
  const [showComposerModal, setShowComposerModal] = useState(false);

  // Load fallback static bundle first, then hydrate live from Contentful or Strapi
  useEffect(() => {
    fetch('./data/content-bundle.json')
      .then((res) => res.json())
      .then((bundle) => {
        setSiteData(bundle);
        return syncWithHeadlessCms(bundle);
      })
      .catch(() => {});
  }, []);

  async function syncWithHeadlessCms(baseBundle) {
    const cfg = getCmsConfig();
    const hasContentful = cfg.provider === 'CONTENTFUL' && cfg.contentfulSpaceId && cfg.contentfulDeliveryToken;
    const hasStrapi = cfg.provider === 'STRAPI' && cfg.strapiBaseUrl && cfg.strapiApiToken;
    if (!hasContentful && !hasStrapi) {
      setCmsStatus(`Static Snapshot Ready • Connect ${'$'}{cfg.provider} for Live Zero-Code Posting`);
      return;
    }
    try {
      setCmsStatus(`Syncing live entries from ${'$'}{cfg.provider}...`);
      const liveItems = await fetchHeadlessContent(cfg);
      if (liveItems.length > 0) {
        setSiteData((prev) => ({
          ...(prev || baseBundle),
          items: liveItems
        }));
        setCmsStatus(`Live ${'$'}{cfg.provider} Connected (${'$'}{liveItems.length} entries synced)`);
      }
    } catch (err) {
      setCmsStatus(`${'$'}{cfg.provider} Offline Fallback (${'$'}{err.message})`);
    }
  }

  const categories = siteData?.categories || [];
  const items = siteData?.items || [];

  const filteredItems = useMemo(() => {
    return items.filter((item) => {
      const matchesTab = activeTab === 'ALL' || item.contentType === activeTab;
      const matchesCat = selectedCategory === 'ALL' || item.categoryName === selectedCategory;
      const matchesQuery =
        !searchQuery ||
        item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (item.summary || '').toLowerCase().includes(searchQuery.toLowerCase());
      return matchesTab && matchesCat && matchesQuery;
    });
  }, [items, activeTab, selectedCategory, searchQuery]);

  return (
    <div className="min-h-screen bg-[#090E1A] text-slate-100 font-sans">
      <header className="sticky top-0 z-30 backdrop-blur-md bg-[#090E1A]/90 border-b border-slate-800">
        <div className="max-w-6xl mx-auto px-4 py-4 flex flex-wrap items-center justify-between gap-4">
          <div>
            <span className="text-xs font-mono uppercase tracking-widest text-emerald-400">
              {cmsStatus}
            </span>
            <h1 className="text-2xl font-bold tracking-tight text-white">
              {siteData?.siteConfig?.siteTitle || '${config.siteTitle}'}
            </h1>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowComposerModal(true)}
              className="px-4 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold text-sm transition"
            >
              + Post via Headless CMS
            </button>
            <button
              onClick={() => setShowCmsModal(true)}
              className="px-3 py-2 rounded-lg border border-slate-700 hover:border-cyan-400 text-xs font-mono text-cyan-300"
            >
              CMS Settings ({cmsConfig.provider})
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
          <div className="flex gap-2">
            {['ALL', 'PROJECT', 'BLOG', 'PHOTO'].map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-4 py-2 rounded-full text-xs font-mono ${'$'}{
                  activeTab === tab
                    ? 'bg-emerald-500 text-slate-950 font-bold'
                    : 'bg-slate-800/80 text-slate-300'
                }`}
              >
                {tab}
              </button>
            ))}
          </div>
          <input
            type="search"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search blogs, projects, photos..."
            className="px-4 py-2 rounded-lg bg-slate-900 border border-slate-700 text-sm text-white"
          />
        </div>

        <div className="flex gap-2 overflow-x-auto pb-4 mb-6">
          <button
            onClick={() => setSelectedCategory('ALL')}
            className={`px-3 py-1.5 rounded-lg text-xs font-mono ${'$'}{
              selectedCategory === 'ALL' ? 'bg-cyan-500/20 border border-cyan-400 text-cyan-300' : 'bg-slate-900 text-slate-400'
            }`}
          >
            All Categories
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id || cat.slug}
              onClick={() => setSelectedCategory(cat.name)}
              className={`px-3 py-1.5 rounded-lg text-xs font-mono whitespace-nowrap ${'$'}{
                selectedCategory === cat.name
                  ? 'bg-emerald-500/20 border border-emerald-400 text-emerald-300'
                  : 'bg-slate-900 text-slate-400'
              }`}
            >
              {cat.name}
            </button>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredItems.map((item) => (
            <article
              key={item.id || item.slug}
              className="rounded-2xl bg-[#111827] border border-slate-800 p-5 flex flex-col justify-between hover:border-emerald-500/50 transition"
            >
              <div>
                <div className="flex items-center justify-between text-xs font-mono text-emerald-400 mb-2">
                  <span>{item.contentType} • {item.categoryName}</span>
                  {item.cmsProvider && <span className="text-cyan-400">{item.cmsProvider}</span>}
                </div>
                <h2 className="text-lg font-bold text-white mb-2">{item.title}</h2>
                <p className="text-sm text-slate-300 mb-4">{item.summary}</p>
                {item.photoCaption && (
                  <p className="text-xs italic text-amber-300 mb-3">“{item.photoCaption}”</p>
                )}
              </div>
              <div className="flex flex-wrap gap-1.5 pt-3 border-t border-slate-800/80">
                {(item.techStack || []).map((t) => (
                  <span key={t} className="px-2 py-0.5 rounded bg-slate-800 text-[11px] font-mono text-slate-300">
                    {t}
                  </span>
                ))}
              </div>
            </article>
          ))}
        </div>
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
<body class="bg-[#090E1A] text-slate-100 antialiased selection:bg-emerald-500 selection:text-slate-950">
  <div id="root"></div>
  <script type="module" src="/src/main.jsx"></script>
</body>
</html>
""".trimIndent()
    }

    fun generateGithubActionsWorkflow(config: SiteConfigEntity): String {
        return """
name: Deploy Rasel Dev BD (React + Headless CMS) to GitHub Pages

on:
  push:
    branches: ["main"]
  repository_dispatch:
    types: ["cms-publish", "contentful-webhook", "strapi-webhook"]
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: "pages"
  cancel-in-progress: false

jobs:
  build-and-deploy:
    environment:
      name: github-pages
      url: ${'$'}{{ steps.deployment.outputs.page_url }}
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Setup Node.js 20
        uses: actions/setup-node@v4
        with:
          node-version: 20

      - name: Install & Build React Static Bundle
        working-directory: ./github-pages-site
        env:
          VITE_CMS_PROVIDER: ${'$'}{{ vars.VITE_CMS_PROVIDER || '${config.cmsProvider}' }}
          VITE_CONTENTFUL_SPACE_ID: ${'$'}{{ secrets.VITE_CONTENTFUL_SPACE_ID }}
          VITE_CONTENTFUL_ENVIRONMENT: ${'$'}{{ vars.VITE_CONTENTFUL_ENVIRONMENT || '${config.contentfulEnvironment}' }}
          VITE_CONTENTFUL_DELIVERY_TOKEN: ${'$'}{{ secrets.VITE_CONTENTFUL_DELIVERY_TOKEN }}
          VITE_STRAPI_BASE_URL: ${'$'}{{ vars.VITE_STRAPI_BASE_URL || '${config.strapiBaseUrl}' }}
          VITE_STRAPI_API_TOKEN: ${'$'}{{ secrets.VITE_STRAPI_API_TOKEN }}
        run: |
          npm install
          npm run build
          cp dist/index.html dist/404.html
          touch dist/.nojekyll

      - name: Upload Pages Artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: ./github-pages-site/dist

      - name: Deploy to GitHub Pages
        id: deployment
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
