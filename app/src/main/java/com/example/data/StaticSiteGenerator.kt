package com.example.data

import android.content.Context
import com.example.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ParsedStaticBundle(
    val config: SiteConfigEntity?,
    val categories: List<CategoryEntity>,
    val items: List<ContentItemEntity>
)

object StaticSiteGenerator {

    private fun formatIsoDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun toSlug(input: String): String {
        return input.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "item-${System.currentTimeMillis() % 10000}" }
    }

    fun generateStaticJsonBundle(
        config: SiteConfigEntity,
        categories: List<CategoryEntity>,
        publishedItems: List<ContentItemEntity>
    ): String {
        val root = JSONObject()
        root.put("schemaVersion", "1.0.0")
        root.put("generator", "Rasel Dev BD Static Site Engine (GitHub Pages Compatible)")
        root.put("generatedAt", formatIsoDate(System.currentTimeMillis()))

        val siteObj = JSONObject().apply {
            put("siteTitle", config.siteTitle)
            put("tagline", config.tagline)
            put("bio", config.bio)
            put("githubUsername", config.githubUsername)
            put("githubPagesDomain", config.githubPagesDomain)
            put("email", config.email)
            put("location", config.location)
            put("adminPasscode", config.adminPasscode)
            put("skills", JSONArray(config.skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }))
        }
        root.put("siteConfig", siteObj)

        val categoriesArray = JSONArray()
        categories.forEach { cat ->
            val catObj = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("slug", cat.slug)
                put("contentTypeScope", cat.contentTypeScope)
                put("colorHex", cat.colorHex)
                put("description", cat.description)
                put("displayOrder", cat.displayOrder)
            }
            categoriesArray.put(catObj)
        }
        root.put("categories", categoriesArray)

        val itemsArray = JSONArray()
        publishedItems.forEach { item ->
            val staticMediaPath = when {
                item.mediaSource.startsWith("drawable:") ->
                    "./assets/images/${item.mediaSource.removePrefix("drawable:")}.jpg"
                item.mediaSource.startsWith("content://") || item.mediaSource.startsWith("file://") ->
                    "./assets/uploads/${item.slug}.jpg"
                else -> item.mediaSource
            }
            val itemObj = JSONObject().apply {
                put("id", item.id)
                put("contentType", item.contentType)
                put("title", item.title)
                put("slug", item.slug)
                put("summaryOrCaption", item.summaryOrCaption)
                put("bodyMarkdown", item.bodyMarkdown)
                put("categoryId", item.categoryId)
                put("categoryName", item.categoryName)
                put("mediaSource", item.mediaSource)
                put("staticAssetPath", staticMediaPath)
                put("tags", JSONArray(item.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }))
                put("liveUrl", item.liveUrl)
                put("repoUrl", item.repoUrl)
                put("isFeatured", item.isFeatured)
                put("isPublished", item.isPublished)
                put("likesCount", item.likesCount)
                put("createdAt", item.createdAt)
                put("publishedDateIso", formatIsoDate(item.createdAt))
            }
            itemsArray.put(itemObj)
        }
        root.put("contentItems", itemsArray)

        return root.toString(2)
    }

    fun parseStaticJsonBundle(jsonString: String, existingPasscode: String): ParsedStaticBundle {
        val root = JSONObject(jsonString)
        val siteObj = root.optJSONObject("siteConfig")
        val parsedConfig = if (siteObj != null) {
            val skillsArr = siteObj.optJSONArray("skills")
            val skillsList = mutableListOf<String>()
            if (skillsArr != null) {
                for (i in 0 until skillsArr.length()) {
                    skillsList.add(skillsArr.optString(i))
                }
            }
            SiteConfigEntity(
                id = 1,
                siteTitle = siteObj.optString("siteTitle", "Rasel Dev BD"),
                tagline = siteObj.optString("tagline", ""),
                bio = siteObj.optString("bio", ""),
                githubUsername = siteObj.optString("githubUsername", "rasel-dev-bd"),
                githubPagesDomain = siteObj.optString("githubPagesDomain", "https://rasel-dev-bd.github.io"),
                email = siteObj.optString("email", "alexraselchodhury@gmail.com"),
                location = siteObj.optString("location", "Dhaka, Bangladesh"),
                skillsCsv = skillsList.joinToString(","),
                adminPasscode = siteObj.optString("adminPasscode", existingPasscode).ifBlank { existingPasscode },
                lastStaticExportTimestamp = System.currentTimeMillis()
            )
        } else null

        val catsArr = root.optJSONArray("categories") ?: JSONArray()
        val parsedCategories = mutableListOf<CategoryEntity>()
        for (i in 0 until catsArr.length()) {
            val c = catsArr.getJSONObject(i)
            parsedCategories.add(
                CategoryEntity(
                    id = c.optLong("id", 0L),
                    name = c.optString("name", "General"),
                    slug = c.optString("slug", "general"),
                    contentTypeScope = c.optString("contentTypeScope", ContentType.ALL),
                    colorHex = c.optString("colorHex", "#10B981"),
                    description = c.optString("description", ""),
                    displayOrder = c.optInt("displayOrder", i + 1)
                )
            )
        }

        val itemsArr = root.optJSONArray("contentItems") ?: JSONArray()
        val parsedItems = mutableListOf<ContentItemEntity>()
        for (i in 0 until itemsArr.length()) {
            val item = itemsArr.getJSONObject(i)
            val tagsArr = item.optJSONArray("tags")
            val tagsList = mutableListOf<String>()
            if (tagsArr != null) {
                for (j in 0 until tagsArr.length()) {
                    tagsList.add(tagsArr.optString(j))
                }
            }
            parsedItems.add(
                ContentItemEntity(
                    id = item.optLong("id", 0L),
                    contentType = item.optString("contentType", ContentType.BLOG),
                    title = item.optString("title", "Untitled"),
                    slug = item.optString("slug", "untitled-$i"),
                    summaryOrCaption = item.optString("summaryOrCaption", ""),
                    bodyMarkdown = item.optString("bodyMarkdown", ""),
                    categoryId = item.optLong("categoryId", 1L),
                    categoryName = item.optString("categoryName", "General"),
                    mediaSource = item.optString("mediaSource", "drawable:img_hero_banner"),
                    tagsCsv = tagsList.joinToString(","),
                    liveUrl = item.optString("liveUrl", ""),
                    repoUrl = item.optString("repoUrl", ""),
                    isFeatured = item.optBoolean("isFeatured", false),
                    isPublished = item.optBoolean("isPublished", true),
                    likesCount = item.optInt("likesCount", 0),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        return ParsedStaticBundle(
            config = parsedConfig,
            categories = parsedCategories,
            items = parsedItems
        )
    }

    fun generateStandaloneIndexHtml(
        config: SiteConfigEntity,
        categories: List<CategoryEntity>,
        publishedItems: List<ContentItemEntity>
    ): String {
        val embeddedJson = generateStaticJsonBundle(config, categories, publishedItems)
        return """
<!DOCTYPE html>
<html lang="en" class="dark">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${config.siteTitle} — ${config.tagline}</title>
  <meta name="description" content="${config.bio}" />
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;700&family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Space+Grotesk:wght@600;700&display=swap" rel="stylesheet">
  <script src="https://cdn.tailwindcss.com"></script>
  <script crossorigin src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
  <script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
</head>
<body class="bg-[#090E1A] text-slate-100 antialiased selection:bg-emerald-500 selection:text-slate-950">
  <div id="root"></div>
  <script id="initial-bundle" type="application/json">
$embeddedJson
  </script>
  <script type="text/babel">
    const { useState, useEffect, useMemo } = React;
    const STORAGE_KEY = 'rasel_dev_bd_static_cms_v1';
    const INITIAL_DATA = JSON.parse(document.getElementById('initial-bundle').textContent);

    function toSlug(str) {
      return (str || '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '') || ('item-' + Date.now());
    }

    function RaselDevBDApp() {
      const [siteConfig, setSiteConfig] = useState(INITIAL_DATA.siteConfig);
      const [categories, setCategories] = useState(INITIAL_DATA.categories || []);
      const [items, setItems] = useState(INITIAL_DATA.contentItems || []);
      const [activeNav, setActiveNav] = useState('HOME');
      const [selectedCat, setSelectedCat] = useState(null);
      const [searchQuery, setSearchQuery] = useState('');
      const [detailItem, setDetailItem] = useState(null);
      const [isAdminUnlocked, setIsAdminUnlocked] = useState(false);
      const [pinInput, setPinInput] = useState('');

      useEffect(() => {
        fetch('./data/content-bundle.json')
          .then((r) => r.json())
          .then((data) => {
            if (data.siteConfig) setSiteConfig(data.siteConfig);
            if (Array.isArray(data.categories)) setCategories(data.categories);
            if (Array.isArray(data.contentItems)) setItems(data.contentItems);
          })
          .catch(() => {});
      }, []);

      const published = useMemo(() => items.filter((i) => i.isPublished !== false), [items]);
      const filtered = useMemo(() => {
        return published.filter((item) => {
          const matchesType = activeNav === 'HOME' || activeNav === 'ADMIN' || item.contentType === activeNav;
          const matchesCat = selectedCat === null || Number(item.categoryId) === Number(selectedCat);
          const q = searchQuery.toLowerCase();
          const matchesQuery = !q || (item.title || '').toLowerCase().includes(q) || (item.summaryOrCaption || '').toLowerCase().includes(q);
          return matchesType && matchesCat && matchesQuery;
        });
      }, [published, activeNav, selectedCat, searchQuery]);

      const exportJson = () => {
        const blob = new Blob([JSON.stringify({ schemaVersion: '1.0.0', siteConfig, categories, contentItems: items }, null, 2)], { type: 'application/json' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'content-bundle.json';
        a.click();
      };

      return (
        <div className="min-h-screen flex flex-col bg-[#090E1A] text-slate-100">
          <header className="border-b border-slate-800 bg-[#111827]/90 backdrop-blur sticky top-0 z-30">
            <div className="max-w-6xl mx-auto px-6 py-4 flex flex-wrap items-center justify-between gap-4">
              <div onClick={() => { setActiveNav('HOME'); setDetailItem(null); }} className="flex items-center gap-3 cursor-pointer">
                <span className="px-2.5 py-1 rounded-lg bg-emerald-500/15 border border-emerald-500/40 text-emerald-400 font-mono font-bold">&lt;R/&gt;</span>
                <div>
                  <h1 className="text-xl font-bold">{siteConfig.siteTitle}</h1>
                  <p className="text-xs text-slate-400">{siteConfig.location}</p>
                </div>
              </div>
              <nav className="flex gap-2 flex-wrap">
                {['HOME', 'PROJECT', 'BLOG', 'PHOTO', 'ADMIN'].map((tab) => (
                  <button
                    key={tab}
                    onClick={() => { setActiveNav(tab); setDetailItem(null); }}
                    className={`px-3.5 py-1.5 rounded-full text-xs font-mono ${'$'}{activeNav === tab ? 'bg-emerald-500 text-slate-950 font-bold' : 'bg-slate-800 text-slate-300'}`}
                  >
                    {tab}
                  </button>
                ))}
              </nav>
            </div>
          </header>

          <main className="flex-1 max-w-6xl w-full mx-auto px-6 py-8">
            {detailItem ? (
              <div className="max-w-3xl mx-auto bg-[#111827] border border-slate-800 rounded-3xl overflow-hidden p-6 space-y-4">
                <button onClick={() => setDetailItem(null)} className="px-4 py-1.5 rounded-xl bg-slate-800 text-xs font-mono">← Back</button>
                <img src={detailItem.staticAssetPath} alt={detailItem.title} className="w-full h-72 object-cover rounded-2xl bg-slate-900" />
                <div className="text-xs font-mono text-emerald-400">{detailItem.contentType} • {detailItem.categoryName}</div>
                <h2 className="text-2xl font-bold">{detailItem.title}</h2>
                <p className="text-slate-300">{detailItem.summaryOrCaption}</p>
                <pre className="whitespace-pre-wrap text-sm text-slate-200 font-sans pt-4 border-t border-slate-800">{detailItem.bodyMarkdown}</pre>
              </div>
            ) : activeNav === 'ADMIN' ? (
              !isAdminUnlocked ? (
                <div className="max-w-md mx-auto my-12 p-8 rounded-3xl bg-[#111827] border border-emerald-500/40 text-center space-y-4">
                  <h2 className="text-2xl font-bold">Admin Studio Security</h2>
                  <input
                    type="password"
                    value={pinInput}
                    onChange={(e) => setPinInput(e.target.value)}
                    placeholder="Enter Admin PIN"
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-700 text-center font-mono"
                  />
                  <button
                    onClick={() => { if (pinInput === (siteConfig.adminPasscode || '2026')) setIsAdminUnlocked(true); }}
                    className="w-full py-2.5 rounded-xl bg-emerald-500 text-slate-950 font-mono font-bold text-sm"
                  >
                    Unlock Admin Studio
                  </button>
                  <button
                    onClick={() => { setPinInput(siteConfig.adminPasscode || '2026'); setIsAdminUnlocked(true); }}
                    className="w-full py-2 rounded-xl bg-slate-800 text-slate-300 font-mono text-xs"
                  >
                    Quick Auto-Fill & Unlock (PIN: {siteConfig.adminPasscode || '2026'})
                  </button>
                </div>
              ) : (
                <div className="space-y-6">
                  <div className="flex justify-between items-center bg-[#111827] p-5 rounded-2xl border border-slate-800">
                    <h2 className="text-xl font-bold text-emerald-400">Admin Studio • Static Site Manager</h2>
                    <button onClick={exportJson} className="px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 font-mono font-bold text-xs">
                      Download content-bundle.json
                    </button>
                  </div>
                </div>
              )
            ) : (
              <div className="space-y-8">
                <section className="rounded-3xl bg-gradient-to-br from-slate-900 via-[#0F172A] to-emerald-950/40 border border-slate-800 p-8">
                  <span className="inline-block px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 text-xs font-mono mb-3">
                    GITHUB PAGES READY • {siteConfig.githubPagesDomain}
                  </span>
                  <h2 className="text-3xl font-bold mb-2">{siteConfig.tagline}</h2>
                  <p className="text-slate-300 max-w-3xl mb-5">{siteConfig.bio}</p>
                  <input
                    type="search"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search blogs, photos, or projects..."
                    className="w-full max-w-md px-4 py-2.5 rounded-xl bg-slate-950/80 border border-slate-700 text-sm"
                  />
                </section>

                <div className="flex gap-2 overflow-x-auto pb-2">
                  <button
                    onClick={() => setSelectedCat(null)}
                    className={`px-4 py-2 rounded-xl text-xs font-mono shrink-0 ${'$'}{selectedCat === null ? 'bg-emerald-500 text-slate-950 font-bold' : 'bg-slate-900 text-slate-300'}`}
                  >
                    All Categories ({categories.length})
                  </button>
                  {categories.map((cat) => (
                    <button
                      key={cat.id}
                      onClick={() => setSelectedCat(selectedCat === cat.id ? null : cat.id)}
                      className={`px-4 py-2 rounded-xl text-xs font-mono shrink-0 ${'$'}{Number(selectedCat) === Number(cat.id) ? 'bg-emerald-500 text-slate-950 font-bold' : 'bg-slate-900 text-slate-300'}`}
                    >
                      {cat.name}
                    </button>
                  ))}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filtered.map((item) => (
                    <article
                      key={item.id}
                      onClick={() => setDetailItem(item)}
                      className="cursor-pointer rounded-2xl bg-[#111827] border border-slate-800 overflow-hidden flex flex-col justify-between hover:border-emerald-500/50 transition"
                    >
                      <div>
                        <img src={item.staticAssetPath} alt={item.title} className="w-full h-48 object-cover bg-slate-900" />
                        <div className="p-5">
                          <div className="flex justify-between text-xs font-mono text-emerald-400 mb-2">
                            <span>{item.contentType}</span>
                            <span>{item.categoryName}</span>
                          </div>
                          <h3 className="text-lg font-bold mb-2">{item.title}</h3>
                          <p className="text-sm text-slate-300 line-clamp-3">{item.summaryOrCaption}</p>
                        </div>
                      </div>
                      <div className="px-5 py-3 border-t border-slate-800 flex justify-between text-xs font-mono text-slate-400">
                        <span>{item.liveUrl || (item.publishedDateIso || '').slice(0, 10)}</span>
                        <span>♥ {item.likesCount}</span>
                      </div>
                    </article>
                  ))}
                </div>
              </div>
            )}
          </main>
        </div>
      );
    }

    ReactDOM.createRoot(document.getElementById('root')).render(<RaselDevBDApp />);
  </script>
</body>
</html>
        """.trimIndent()
    }

    fun generateReactAppJsx(
        config: SiteConfigEntity,
        categories: List<CategoryEntity>,
        publishedItems: List<ContentItemEntity>
    ): String {
        val projectsCount = publishedItems.count { it.contentType == ContentType.PROJECT }
        val blogsCount = publishedItems.count { it.contentType == ContentType.BLOG }
        val photosCount = publishedItems.count { it.contentType == ContentType.PHOTO }

        return """
// src/App.jsx — Generated by ${config.siteTitle} Static Site Generator
// Compatible with Vite + React 19 + GitHub Pages (${config.githubPagesDomain})
import React, { useState, useEffect, useMemo } from 'react';

export default function RaselDevBDApp() {
  const [bundle, setBundle] = useState(null);
  const [activeType, setActiveType] = useState('ALL'); // ALL | PROJECT | BLOG | PHOTO
  const [activeCategory, setActiveCategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    fetch('./data/content-bundle.json')
      .then((res) => res.json())
      .then((data) => setBundle(data))
      .catch((err) => console.error('Failed to load static bundle:', err));
  }, []);

  const categories = bundle?.categories || [];
  const items = bundle?.contentItems || [];
  const site = bundle?.siteConfig || {
    siteTitle: "${config.siteTitle.replace("\"", "\\\"")}",
    tagline: "${config.tagline.replace("\"", "\\\"")}",
    location: "${config.location.replace("\"", "\\\"")}",
    email: "${config.email.replace("\"", "\\\"")}"
  };

  const filteredItems = useMemo(() => {
    return items.filter((item) => {
      const matchesType = activeType === 'ALL' || item.contentType === activeType;
      const matchesCat = activeCategory === 'all' || String(item.categoryId) === String(activeCategory);
      const matchesQuery = !searchQuery ||
        item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.summaryOrCaption.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesType && matchesCat && matchesQuery;
    });
  }, [items, activeType, activeCategory, searchQuery]);

  return (
    <div className="min-h-screen bg-[#090E1A] text-slate-100 font-sans">
      <header className="border-b border-slate-800 bg-[#111827]/90 backdrop-blur sticky top-0 z-30">
        <div className="max-w-6xl mx-auto px-6 py-4 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span className="px-2.5 py-1 rounded-lg bg-emerald-500/15 border border-emerald-500/40 text-emerald-400 font-mono font-bold">
              &lt;R/&gt;
            </span>
            <div>
              <h1 className="text-xl font-bold tracking-tight">{site.siteTitle}</h1>
              <p className="text-xs text-slate-400">{site.location}</p>
            </div>
          </div>
          <nav className="flex gap-2">
            {['ALL', 'PROJECT', 'BLOG', 'PHOTO'].map((type) => (
              <button
                key={type}
                onClick={() => setActiveType(type)}
                className={`px-3.5 py-1.5 rounded-full text-xs font-mono transition ${'$'}{
                  activeType === type
                    ? 'bg-emerald-500 text-slate-950 font-bold'
                    : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                }`}
              >
                {type === 'ALL' ? 'All Content' : type + 'S'}
              </button>
            ))}
          </nav>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-10">
        <section className="rounded-3xl bg-gradient-to-br from-slate-900 via-[#0F172A] to-emerald-950/40 border border-slate-800 p-8 mb-8">
          <span className="inline-block px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 text-xs font-mono mb-3">
            Static Site Bundle • $projectsCount Projects • $blogsCount Blogs • $photosCount Photo Stories
          </span>
          <h2 className="text-3xl md:text-4xl font-bold mb-3">{site.tagline}</h2>
          <p className="text-slate-300 max-w-3xl mb-6">{site.bio}</p>
          <input
            type="search"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search blogs, photos, or projects..."
            className="w-full max-w-md px-4 py-2.5 rounded-xl bg-slate-950/80 border border-slate-700 text-sm"
          />
        </section>

        <div className="flex gap-2 overflow-x-auto pb-4 mb-6">
          <button
            onClick={() => setActiveCategory('all')}
            className={`px-4 py-2 rounded-xl text-xs font-mono shrink-0 ${'$'}{
              activeCategory === 'all' ? 'bg-cyan-500 text-slate-950 font-bold' : 'bg-slate-900 text-slate-300'
            }`}
          >
            All Categories ({categories.length})
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              onClick={() => setActiveCategory(cat.id)}
              className={`px-4 py-2 rounded-xl text-xs font-mono shrink-0 border ${'$'}{
                String(activeCategory) === String(cat.id)
                  ? 'bg-emerald-500 text-slate-950 border-emerald-400 font-bold'
                  : 'bg-slate-900/90 text-slate-300 border-slate-800'
              }`}
            >
              {cat.name}
            </button>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredItems.map((item) => (
            <article
              key={item.id}
              className="rounded-2xl bg-[#111827] border border-slate-800 overflow-hidden flex flex-col justify-between hover:border-emerald-500/50 transition"
            >
              <div>
                <img
                  src={item.staticAssetPath}
                  alt={item.title}
                  className="w-full h-48 object-cover bg-slate-900"
                />
                <div className="p-5">
                  <div className="flex items-center justify-between text-xs font-mono text-emerald-400 mb-2">
                    <span>{item.contentType}</span>
                    <span>{item.categoryName}</span>
                  </div>
                  <h3 className="text-lg font-bold mb-2">{item.title}</h3>
                  <p className="text-sm text-slate-300 line-clamp-3">{item.summaryOrCaption}</p>
                </div>
              </div>
              <div className="px-5 pb-4 pt-2 border-t border-slate-800/80 flex justify-between items-center text-xs text-slate-400 font-mono">
                <span>{item.liveUrl || item.publishedDateIso?.slice(0, 10)}</span>
                <span>♥ {item.likesCount}</span>
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

    fun generatePackageJson(): String {
        return """
{
  "name": "rasel-dev-bd-github-pages",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.3.4",
    "vite": "^6.0.0"
  }
}
        """.trimIndent()
    }

    fun generateViteConfig(): String {
        return """
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  base: './',
});
        """.trimIndent()
    }

    fun generateIndexHtml(config: SiteConfigEntity): String {
        return """
<!DOCTYPE html>
<html lang="en" class="dark">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${config.siteTitle} — ${config.tagline}</title>
    <meta name="description" content="${config.bio}" />
    <meta property="og:title" content="${config.siteTitle}" />
    <meta property="og:description" content="${config.tagline}" />
    <meta property="og:url" content="${config.githubPagesDomain}" />
    <script src="https://cdn.tailwindcss.com"></script>
  </head>
  <body class="bg-[#090E1A] text-slate-100 antialiased selection:bg-emerald-500 selection:text-slate-950">
    <div id="root"></div>
    <script type="module" src="./src/main.jsx"></script>
  </body>
</html>
        """.trimIndent()
    }

    fun generateGithubActionsWorkflow(config: SiteConfigEntity): String {
        return """
# .github/workflows/deploy-gh-pages.yml
# Automated Static Site Generation & Deployment for ${config.siteTitle} (${config.githubPagesDomain})
name: Deploy Rasel Dev BD to GitHub Pages

on:
  push:
    branches: ["main", "master"]
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: "pages"
  cancel-in-progress: true

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

      - name: Install dependencies & build static production bundle
        run: |
          npm install
          npm run build
          cp dist/index.html dist/404.html

      - name: Upload Pages artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: './dist'

      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v4
        """.trimIndent()
    }

    fun writeGithubPagesZipArchive(
        context: Context,
        outputStream: OutputStream,
        config: SiteConfigEntity,
        categories: List<CategoryEntity>,
        publishedItems: List<ContentItemEntity>
    ) {
        val jsonBundle = generateStaticJsonBundle(config, categories, publishedItems)
        val standaloneHtml = generateStandaloneIndexHtml(config, categories, publishedItems)
        val reactAppJsx = generateReactAppJsx(config, categories, publishedItems)
        val packageJson = generatePackageJson()
        val viteConfig = generateViteConfig()
        val workflowYml = generateGithubActionsWorkflow(config)
        val mainJsx = """
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
        """.trimIndent()

        val readmeGuide = """
# ${config.siteTitle} — GitHub Pages Hosting Bundle

This ZIP archive contains everything needed to host **${config.siteTitle}** on GitHub Pages:

## Method 1: Instant Zero-Build Hosting (Easiest)
1. Upload the files in the `docs/` folder (`index.html`, `404.html`, `.nojekyll`, `data/content-bundle.json`, and `assets/images/`) to your GitHub repository.
2. Go to **Settings -> Pages** on GitHub.
3. Choose **Deploy from a branch** -> select `main` and `/docs` (or `/ (root)` if uploaded to root).
4. Your site is immediately live! Admin Studio PIN: `${config.adminPasscode}`.

## Method 2: Vite + React + GitHub Actions
1. Push the root project (`package.json`, `vite.config.js`, `src/`, `public/`, and `.github/workflows/deploy-gh-pages.yml`) to GitHub.
2. In **Settings -> Pages**, set Source to **GitHub Actions**.
        """.trimIndent()

        ZipOutputStream(outputStream).use { zip ->
            fun putTextEntry(path: String, text: String) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(text.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }

            // 1. Instant Zero-Build files (both in root index.html and docs/ folder)
            putTextEntry("docs/index.html", standaloneHtml)
            putTextEntry("docs/404.html", standaloneHtml)
            putTextEntry("docs/.nojekyll", "")
            putTextEntry("docs/data/content-bundle.json", jsonBundle)

            // 2. Full Vite + React project files
            putTextEntry("package.json", packageJson)
            putTextEntry("vite.config.js", viteConfig)
            putTextEntry("index.html", standaloneHtml)
            putTextEntry("404.html", standaloneHtml)
            putTextEntry(".nojekyll", "")
            putTextEntry("data/content-bundle.json", jsonBundle)
            putTextEntry("public/data/content-bundle.json", jsonBundle)
            putTextEntry("src/main.jsx", mainJsx)
            putTextEntry("src/App.jsx", reactAppJsx)
            putTextEntry(".github/workflows/deploy-gh-pages.yml", workflowYml)
            putTextEntry("README-GITHUB-PAGES.md", readmeGuide)

            // 3. Copy bundled drawable images into assets/images/ and docs/assets/images/
            val drawablesToCopy = listOf(
                R.drawable.img_hero_banner to "img_hero_banner.jpg",
                R.drawable.img_project_cloud to "img_project_cloud.jpg",
                R.drawable.img_photo_dhaka to "img_photo_dhaka.jpg",
                R.drawable.img_avatar_rasel to "img_avatar_rasel.jpg"
            )

            drawablesToCopy.forEach { (resId, fileName) ->
                runCatching {
                    val bytes = context.resources.openRawResource(resId).use { it.readBytes() }
                    listOf(
                        "assets/images/$fileName",
                        "public/assets/images/$fileName",
                        "docs/assets/images/$fileName"
                    ).forEach { targetPath ->
                        zip.putNextEntry(ZipEntry(targetPath))
                        zip.write(bytes)
                        zip.closeEntry()
                    }
                }
            }
        }
    }
}
