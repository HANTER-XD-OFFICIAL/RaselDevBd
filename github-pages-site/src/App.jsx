import React, { useState, useEffect, useMemo } from 'react';
import {
  getCmsConfig,
  saveCmsConfig,
  isCmsReadConfigured,
  isCmsWriteConfigured,
  fetchHeadlessCmsItems,
  publishItemToHeadlessCms
} from './cmsClient.js';

const LOCAL_ITEMS_CACHE_KEY = 'rasel_dev_bd_items_cache_v2';
const LOCAL_CATS_CACHE_KEY = 'rasel_dev_bd_cats_cache_v2';

export default function App() {
  const [siteConfig, setSiteConfig] = useState({
    siteTitle: 'Rasel Dev BD',
    tagline: 'Full-Stack React & Android Architect • Headless CMS & Open-Source Platform',
    ownerName: 'Rasel Chowdhury',
    ownerRole: 'Senior Full-Stack Engineer & Creative Technologist',
    bio: 'Building resilient cloud-native web apps, native Android experiences, and headless CMS developer platforms from Dhaka, Bangladesh.',
    location: 'Dhaka, Bangladesh',
    email: 'alexraselchodhury@gmail.com',
    githubUrl: 'https://github.com/raseldevbd',
    customDomain: 'raseldevbd.github.io',
    adminPin: '2026'
  });

  const [categories, setCategories] = useState([]);
  const [items, setItems] = useState([]);
  const [activeNav, setActiveNav] = useState('ALL'); // ALL, PROJECT, BLOG, PHOTO, ADMIN
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedItem, setSelectedItem] = useState(null);

  // Headless CMS state
  const [cmsConfig, setCmsConfig] = useState(() => getCmsConfig());
  const [cmsSyncState, setCmsSyncState] = useState({
    syncing: false,
    badge: 'Hybrid Headless CMS Ready (Contentful & Strapi)',
    lastMessage: 'Loaded static bundle fallback. Connect Contentful or Strapi for live no-code publishing.'
  });
  const [isCmsDrawerOpen, setIsCmsDrawerOpen] = useState(false);

  // Admin Studio state
  const [isAdminUnlocked, setIsAdminUnlocked] = useState(false);
  const [pinInput, setPinInput] = useState('');
  const [pinError, setPinError] = useState('');
  const [toastMessage, setToastMessage] = useState(null);

  // Post Composer state (No-code publishing to Contentful / Strapi + instant feed update)
  const [composerOpen, setComposerOpen] = useState(false);
  const [draft, setDraft] = useState({
    title: '',
    slug: '',
    contentType: 'BLOG',
    categoryName: 'React & Headless CMS',
    summary: '',
    markdownBody: '',
    mediaUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80',
    photoCaption: '',
    photoLocation: 'Dhaka, Bangladesh',
    exifCamera: 'Sony A7 IV • 24mm f/1.4 GM',
    techStackCsv: 'React,Contentful,Strapi,GitHub Pages',
    liveDemoUrl: '',
    repoUrl: '',
    readingTimeMinutes: 5,
    isFeatured: true,
    publishToRemoteCms: true
  });

  useEffect(() => {
    fetch('./data/content-bundle.json')
      .then((res) => res.json())
      .then((bundle) => {
        if (bundle.siteConfig) setSiteConfig((prev) => ({ ...prev, ...bundle.siteConfig }));
        const cachedCats = localStorage.getItem(LOCAL_CATS_CACHE_KEY);
        const cachedItems = localStorage.getItem(LOCAL_ITEMS_CACHE_KEY);
        const initialCats = cachedCats ? JSON.parse(cachedCats) : bundle.categories || [];
        const initialItems = cachedItems ? JSON.parse(cachedItems) : bundle.items || [];
        setCategories(initialCats);
        setItems(initialItems);

        const cfg = getCmsConfig();
        if (isCmsReadConfigured(cfg)) {
          handleLiveCmsSync(cfg, initialItems);
        }
      })
      .catch(() => {});
  }, []);

  function showToast(msg) {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 4500);
  }

  async function handleLiveCmsSync(cfgOverride, baseItems = items) {
    const cfg = cfgOverride || cmsConfig;
    if (!isCmsReadConfigured(cfg)) {
      setIsCmsDrawerOpen(true);
      showToast(`Configure ${cfg.provider} credentials to pull live cloud entries.`);
      return;
    }

    try {
      setCmsSyncState((prev) => ({
        ...prev,
        syncing: true,
        badge: `Syncing ${cfg.provider}...`
      }));
      const remoteItems = await fetchHeadlessCmsItems(cfg);
      if (remoteItems.length > 0) {
        // Merge remote CMS entries with local items by slug
        const mergedMap = new Map();
        remoteItems.forEach((r) => mergedMap.set(r.slug, r));
        baseItems.forEach((loc) => {
          if (!mergedMap.has(loc.slug)) mergedMap.set(loc.slug, loc);
        });
        const mergedList = Array.from(mergedMap.values());
        setItems(mergedList);
        localStorage.setItem(LOCAL_ITEMS_CACHE_KEY, JSON.stringify(mergedList));
      }
      setCmsSyncState({
        syncing: false,
        badge: `${cfg.provider} Live (${remoteItems.length} cloud entries)`,
        lastMessage: `Synced ${remoteItems.length} live entries from ${cfg.provider} at ${new Date().toLocaleTimeString()}`
      });
      showToast(`Synced ${remoteItems.length} entries from ${cfg.provider} Headless CMS!`);
    } catch (err) {
      setCmsSyncState({
        syncing: false,
        badge: `${cfg.provider} Offline Fallback`,
        lastMessage: err.message
      });
      showToast(`CMS Sync Notice: ${err.message}`);
    }
  }

  async function handleCreatePost(e) {
    e.preventDefault();
    if (!draft.title.trim()) {
      showToast('Please enter a post title.');
      return;
    }

    const cleanSlug =
      draft.slug.trim() ||
      draft.title
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/(^-|-$)/g, '');

    const newItem = {
      id: `item-${Date.now()}`,
      title: draft.title.trim(),
      slug: cleanSlug,
      contentType: draft.contentType,
      categoryId: 1,
      categoryName: draft.categoryName,
      summary: draft.summary.trim() || draft.title.trim(),
      markdownBody: draft.markdownBody.trim() || draft.summary.trim(),
      mediaUrl: draft.mediaUrl.trim(),
      photoCaption: draft.photoCaption.trim(),
      photoLocation: draft.photoLocation.trim(),
      exifCamera: draft.exifCamera.trim(),
      techStack: draft.techStackCsv
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean),
      liveDemoUrl: draft.liveDemoUrl.trim(),
      repoUrl: draft.repoUrl.trim(),
      readingTimeMinutes: Number(draft.readingTimeMinutes || 5),
      isFeatured: Boolean(draft.isFeatured),
      isPublished: true,
      updatedAtEpoch: Date.now(),
      cmsProvider: cmsConfig.provider,
      cmsEntryId: ''
    };

    if (draft.publishToRemoteCms && isCmsWriteConfigured(cmsConfig)) {
      try {
        setCmsSyncState((p) => ({ ...p, syncing: true }));
        const pubResult = await publishItemToHeadlessCms(newItem, cmsConfig);
        newItem.cmsEntryId = pubResult.entryId;
        newItem.cmsProvider = pubResult.provider;
        showToast(`Published '${newItem.title}' directly to ${pubResult.provider} CMS!`);
      } catch (err) {
        showToast(`Saved locally; ${cmsConfig.provider} remote push returned: ${err.message}`);
      } finally {
        setCmsSyncState((p) => ({ ...p, syncing: false }));
      }
    } else {
      showToast(`Published '${newItem.title}' to feed! Connect ${cmsConfig.provider} CMA/API token for cloud push.`);
    }

    const updated = [newItem, ...items];
    setItems(updated);
    localStorage.setItem(LOCAL_ITEMS_CACHE_KEY, JSON.stringify(updated));
    setComposerOpen(false);
  }

  function handleMediaFileUpload(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === 'string') {
        setDraft((prev) => ({ ...prev, mediaUrl: reader.result }));
      }
    };
    reader.readAsDataURL(file);
  }

  function handleSaveCmsSettings(e) {
    e.preventDefault();
    saveCmsConfig(cmsConfig);
    setIsCmsDrawerOpen(false);
    showToast(`Saved ${cmsConfig.provider} Headless CMS settings!`);
    if (isCmsReadConfigured(cmsConfig)) {
      handleLiveCmsSync(cmsConfig);
    }
  }

  const filteredItems = useMemo(() => {
    return items.filter((item) => {
      const matchesType =
        activeNav === 'ALL' || activeNav === 'ADMIN' || item.contentType === activeNav;
      const matchesCat =
        selectedCategory === 'ALL' || item.categoryName === selectedCategory;
      const q = searchQuery.toLowerCase();
      const matchesSearch =
        !q ||
        item.title.toLowerCase().includes(q) ||
        (item.summary || '').toLowerCase().includes(q) ||
        (item.photoCaption || '').toLowerCase().includes(q) ||
        (item.techStack || []).some((t) => t.toLowerCase().includes(q));
      return matchesType && matchesCat && matchesSearch;
    });
  }, [items, activeNav, selectedCategory, searchQuery]);

  return (
    <div className="min-h-screen bg-[#090E1A] text-slate-100 flex flex-col">
      {/* Top Toast Banner */}
      {toastMessage && (
        <div className="fixed bottom-5 right-5 z-50 max-w-md bg-emerald-950/95 border border-emerald-400 text-emerald-100 px-4 py-3 rounded-xl shadow-2xl flex items-center justify-between gap-3">
          <span className="text-sm font-medium">{toastMessage}</span>
          <button
            onClick={() => setToastMessage(null)}
            className="text-xs font-mono text-emerald-300 hover:text-white"
          >
            ✕
          </button>
        </div>
      )}

      {/* Navigation Header */}
      <header className="sticky top-0 z-30 backdrop-blur-xl bg-[#090E1A]/90 border-b border-slate-800/80">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-3.5 flex flex-wrap items-center justify-between gap-4">
          <div
            onClick={() => {
              setSelectedItem(null);
              setActiveNav('ALL');
            }}
            className="flex items-center gap-3 cursor-pointer"
          >
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-400 to-cyan-500 flex items-center justify-center text-slate-950 font-mono font-bold text-lg shadow-lg shadow-emerald-500/20">
              &lt;R/&gt;
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-display font-bold text-lg tracking-tight text-white">
                  {siteConfig.siteTitle}
                </span>
                <span className="px-2 py-0.5 text-[10px] font-mono uppercase rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                  {cmsConfig.provider} CMS
                </span>
              </div>
              <p className="text-xs text-slate-400 hidden sm:block">{siteConfig.ownerName} • {siteConfig.location}</p>
            </div>
          </div>

          <nav className="flex items-center gap-1.5 flex-wrap">
            {[
              { id: 'ALL', label: 'Portfolio' },
              { id: 'PROJECT', label: 'Projects' },
              { id: 'BLOG', label: 'Blog' },
              { id: 'PHOTO', label: 'Photos' },
              { id: 'ADMIN', label: 'CMS Studio' }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  setSelectedItem(null);
                  setActiveNav(tab.id);
                }}
                className={`px-3.5 py-2 rounded-lg text-xs font-mono transition ${
                  activeNav === tab.id
                    ? 'bg-emerald-500 text-slate-950 font-bold'
                    : 'text-slate-300 hover:bg-slate-800/70'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </nav>

          <div className="flex items-center gap-2">
            <button
              onClick={() => handleLiveCmsSync()}
              className="px-3 py-2 rounded-lg bg-slate-900 hover:bg-slate-800 border border-slate-700 text-xs font-mono text-cyan-300 flex items-center gap-1.5"
            >
              <span>↻</span>
              <span>{cmsSyncState.syncing ? 'Syncing...' : 'Sync CMS'}</span>
            </button>
            <button
              onClick={() => setIsCmsDrawerOpen(true)}
              className="px-3 py-2 rounded-lg bg-slate-900 hover:bg-slate-800 border border-emerald-500/40 text-xs font-mono text-emerald-300"
            >
              CMS Config
            </button>
            <button
              onClick={() => setComposerOpen(true)}
              className="px-3.5 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold text-xs shadow-lg shadow-emerald-500/20"
            >
              + Post Content
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 py-8">
        {selectedItem ? (
          <div className="max-w-3xl mx-auto bg-[#111827] border border-slate-800 rounded-3xl overflow-hidden shadow-2xl">
            <div className="relative h-72 sm:h-96 bg-slate-900">
              <img
                src={selectedItem.mediaUrl}
                alt={selectedItem.title}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#111827] via-transparent to-black/50" />
              <button
                onClick={() => setSelectedItem(null)}
                className="absolute top-4 left-4 px-3.5 py-2 rounded-full bg-black/70 hover:bg-black text-white text-xs font-mono"
              >
                ← Back to Feed
              </button>
              <div className="absolute bottom-4 left-6 right-6">
                <div className="flex flex-wrap items-center gap-2 mb-2">
                  <span className="px-2.5 py-1 rounded-md bg-emerald-500 text-slate-950 font-mono font-bold text-xs">
                    {selectedItem.contentType}
                  </span>
                  <span className="px-2.5 py-1 rounded-md bg-slate-900/90 text-cyan-300 font-mono text-xs">
                    {selectedItem.categoryName}
                  </span>
                  {selectedItem.cmsProvider && (
                    <span className="px-2.5 py-1 rounded-md bg-slate-900/90 border border-emerald-500/40 text-emerald-300 font-mono text-xs">
                      Synced via {selectedItem.cmsProvider}
                    </span>
                  )}
                </div>
                <h1 className="text-2xl sm:text-3xl font-display font-bold text-white">
                  {selectedItem.title}
                </h1>
              </div>
            </div>

            <div className="p-6 sm:p-8 space-y-6">
              {selectedItem.photoCaption && (
                <blockquote className="p-4 rounded-xl bg-slate-900/90 border-l-4 border-rose-500 italic text-slate-200">
                  “{selectedItem.photoCaption}”
                  {selectedItem.photoLocation && (
                    <div className="not-italic text-xs font-mono text-slate-400 mt-1">
                      📍 {selectedItem.photoLocation} {selectedItem.exifCamera ? `• 📷 ${selectedItem.exifCamera}` : ''}
                    </div>
                  )}
                </blockquote>
              )}

              <p className="text-base text-slate-200 font-medium leading-relaxed">
                {selectedItem.summary}
              </p>

              <div className="prose prose-invert max-w-none text-slate-300 whitespace-pre-line leading-relaxed border-t border-slate-800 pt-6">
                {selectedItem.markdownBody}
              </div>

              {(selectedItem.liveDemoUrl || selectedItem.repoUrl) && (
                <div className="flex flex-wrap gap-3 pt-4">
                  {selectedItem.liveDemoUrl && (
                    <a
                      href={selectedItem.liveDemoUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="px-4 py-2.5 rounded-xl bg-emerald-500 text-slate-950 font-semibold text-sm"
                    >
                      Live Deployment ↗
                    </a>
                  )}
                  {selectedItem.repoUrl && (
                    <a
                      href={selectedItem.repoUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="px-4 py-2.5 rounded-xl border border-slate-700 hover:border-cyan-400 text-slate-200 text-sm font-mono"
                    >
                      Source Repository
                    </a>
                  )}
                </div>
              )}
            </div>
          </div>
        ) : activeNav === 'ADMIN' ? (
          <div className="max-w-4xl mx-auto space-y-6">
            {!isAdminUnlocked ? (
              <div className="max-w-md mx-auto bg-[#111827] border border-slate-800 rounded-3xl p-8 text-center space-y-4">
                <div className="w-14 h-14 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center mx-auto text-emerald-400 font-mono text-xl">
                  🔒
                </div>
                <h2 className="text-xl font-display font-bold text-white">
                  Unlock Headless CMS Studio
                </h2>
                <p className="text-sm text-slate-400">
                  Post blog articles, photos with captions, and project showcases directly to Contentful or Strapi without changing code.
                </p>
                <input
                  type="password"
                  value={pinInput}
                  onChange={(e) => setPinInput(e.target.value)}
                  placeholder="Enter Studio PIN (Default: 2026)"
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-center font-mono text-white"
                />
                {pinError && <p className="text-xs text-rose-400">{pinError}</p>}
                <div className="flex gap-2">
                  <button
                    onClick={() => {
                      if (pinInput === siteConfig.adminPin || pinInput === '2026') {
                        setIsAdminUnlocked(true);
                        setPinError('');
                      } else {
                        setPinError('Incorrect PIN. Use 2026.');
                      }
                    }}
                    className="flex-1 py-2.5 rounded-xl bg-emerald-500 text-slate-950 font-bold text-sm"
                  >
                    Unlock Studio
                  </button>
                  <button
                    onClick={() => setIsAdminUnlocked(true)}
                    className="px-4 py-2.5 rounded-xl border border-slate-700 text-xs font-mono text-cyan-300"
                  >
                    Demo Unlock
                  </button>
                </div>
              </div>
            ) : (
              <div className="bg-[#111827] border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
                <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 pb-4">
                  <div>
                    <span className="text-xs font-mono text-emerald-400 uppercase">
                      No-Code Headless CMS Publisher
                    </span>
                    <h2 className="text-2xl font-display font-bold text-white">
                      Rasel Dev BD • Admin Dashboard
                    </h2>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => setComposerOpen(true)}
                      className="px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-xs"
                    >
                      + New Post / Photo / Project
                    </button>
                    <button
                      onClick={() => setIsCmsDrawerOpen(true)}
                      className="px-4 py-2 rounded-xl border border-cyan-500/40 text-cyan-300 text-xs font-mono"
                    >
                      Configure {cmsConfig.provider}
                    </button>
                  </div>
                </div>

                <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800 text-xs font-mono text-slate-300 flex flex-wrap items-center justify-between gap-2">
                  <span>Status: {cmsSyncState.lastMessage}</span>
                  <button
                    onClick={() => handleLiveCmsSync()}
                    className="text-emerald-400 hover:underline"
                  >
                    Pull Latest from {cmsConfig.provider} →
                  </button>
                </div>

                <div className="space-y-3">
                  {items.map((item) => (
                    <div
                      key={item.id || item.slug}
                      className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800 flex flex-wrap items-center justify-between gap-4"
                    >
                      <div className="flex items-center gap-3">
                        <img
                          src={item.mediaUrl}
                          alt={item.title}
                          className="w-14 h-14 rounded-xl object-cover"
                        />
                        <div>
                          <div className="flex items-center gap-2 text-[11px] font-mono">
                            <span className="text-emerald-400">{item.contentType}</span>
                            <span className="text-slate-500">•</span>
                            <span className="text-cyan-300">{item.categoryName}</span>
                          </div>
                          <h3 className="font-bold text-white">{item.title}</h3>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={async () => {
                            try {
                              const res = await publishItemToHeadlessCms(item, cmsConfig);
                              showToast(`Synced '${item.title}' to ${res.provider} (${res.entryId})`);
                            } catch (err) {
                              showToast(err.message);
                            }
                          }}
                          className="px-3 py-1.5 rounded-lg bg-emerald-500/15 border border-emerald-500/40 text-emerald-300 text-xs font-mono"
                        >
                          Push to {cmsConfig.provider}
                        </button>
                        <button
                          onClick={() => {
                            const next = items.filter((i) => i.slug !== item.slug);
                            setItems(next);
                            localStorage.setItem(LOCAL_ITEMS_CACHE_KEY, JSON.stringify(next));
                          }}
                          className="px-3 py-1.5 rounded-lg bg-rose-500/15 text-rose-300 text-xs font-mono"
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ) : (
          <>
            {/* Hero Banner */}
            <section className="relative rounded-3xl overflow-hidden border border-emerald-500/30 bg-gradient-to-br from-[#111827] via-[#090E1A] to-[#064E3B]/30 p-6 sm:p-10 mb-8 shadow-2xl">
              <div className="max-w-3xl space-y-4">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/40 text-emerald-300 text-xs font-mono">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                  <span>{cmsSyncState.badge}</span>
                </div>
                <h1 className="text-3xl sm:text-5xl font-display font-bold tracking-tight text-white">
                  {siteConfig.siteTitle} — <span className="text-emerald-400">{siteConfig.ownerName}</span>
                </h1>
                <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
                  {siteConfig.bio}
                </p>
                <div className="flex flex-wrap gap-3 pt-2">
                  <button
                    onClick={() => setComposerOpen(true)}
                    className="px-5 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-sm"
                  >
                    + Post Content Without Changing Code
                  </button>
                  <button
                    onClick={() => setIsCmsDrawerOpen(true)}
                    className="px-5 py-2.5 rounded-xl border border-slate-700 hover:border-cyan-400 text-slate-200 text-sm font-mono"
                  >
                    Connect Contentful / Strapi
                  </button>
                </div>
              </div>
            </section>

            {/* Search & Dynamic Categories */}
            <section className="space-y-4 mb-8">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-2 overflow-x-auto pb-1">
                  <button
                    onClick={() => setSelectedCategory('ALL')}
                    className={`px-3.5 py-2 rounded-xl text-xs font-mono whitespace-nowrap ${
                      selectedCategory === 'ALL'
                        ? 'bg-emerald-500 text-slate-950 font-bold'
                        : 'bg-[#111827] text-slate-300 border border-slate-800'
                    }`}
                  >
                    All Categories
                  </button>
                  {categories.map((cat) => (
                    <button
                      key={cat.id || cat.slug}
                      onClick={() => setSelectedCategory(cat.name)}
                      className={`px-3.5 py-2 rounded-xl text-xs font-mono whitespace-nowrap ${
                        selectedCategory === cat.name
                          ? 'bg-emerald-500/20 border border-emerald-400 text-emerald-300 font-bold'
                          : 'bg-[#111827] text-slate-300 border border-slate-800'
                      }`}
                    >
                      {cat.name}
                    </button>
                  ))}
                </div>

                <input
                  type="search"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search blogs, projects, photo captions..."
                  className="w-full sm:w-72 px-4 py-2 rounded-xl bg-[#111827] border border-slate-800 text-sm text-white focus:outline-none focus:border-emerald-400"
                />
              </div>
            </section>

            {/* Content Cards Grid */}
            <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredItems.map((item) => (
                <article
                  key={item.id || item.slug}
                  onClick={() => setSelectedItem(item)}
                  className="group cursor-pointer rounded-2xl bg-[#111827] border border-slate-800/90 hover:border-emerald-500/60 overflow-hidden flex flex-col justify-between transition shadow-lg"
                >
                  <div>
                    <div className="relative h-48 overflow-hidden bg-slate-900">
                      <img
                        src={item.mediaUrl}
                        alt={item.title}
                        className="w-full h-full object-cover group-hover:scale-105 transition duration-500"
                      />
                      <div className="absolute top-3 left-3 flex gap-1.5">
                        <span className="px-2.5 py-1 rounded-md bg-slate-950/85 text-emerald-400 font-mono text-[11px] font-bold">
                          {item.contentType}
                        </span>
                        {item.cmsProvider && (
                          <span className="px-2 py-1 rounded-md bg-slate-950/85 text-cyan-300 font-mono text-[10px]">
                            {item.cmsProvider}
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="p-5">
                      <div className="text-xs font-mono text-cyan-400 mb-1.5">
                        {item.categoryName}
                      </div>
                      <h2 className="text-lg font-display font-bold text-white group-hover:text-emerald-400 transition mb-2">
                        {item.title}
                      </h2>
                      {item.contentType === 'PHOTO' && item.photoCaption ? (
                        <p className="text-xs italic text-slate-300 mb-3">
                          “{item.photoCaption}”
                        </p>
                      ) : (
                        <p className="text-sm text-slate-400 line-clamp-3 mb-3">
                          {item.summary}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="px-5 pb-5 pt-3 border-t border-slate-800/70 flex flex-wrap gap-1.5">
                    {(item.techStack || []).slice(0, 4).map((tag) => (
                      <span
                        key={tag}
                        className="px-2 py-0.5 rounded bg-slate-900 text-slate-300 font-mono text-[11px]"
                      >
                        #{tag}
                      </span>
                    ))}
                  </div>
                </article>
              ))}
            </section>
          </>
        )}
      </main>

      {/* Headless CMS Connection Drawer Modal */}
      {isCmsDrawerOpen && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <form
            onSubmit={handleSaveCmsSettings}
            className="w-full max-w-lg bg-[#111827] border border-slate-700 rounded-3xl p-6 space-y-4 shadow-2xl"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-display font-bold text-white">
                Headless CMS Configuration (Contentful / Strapi)
              </h3>
              <button
                type="button"
                onClick={() => setIsCmsDrawerOpen(false)}
                className="text-slate-400 hover:text-white"
              >
                ✕
              </button>
            </div>

            <p className="text-xs text-slate-400">
              Connect Contentful or Strapi to fetch live entries and publish new posts directly from the browser without changing code.
            </p>

            <div className="flex gap-2">
              {['CONTENTFUL', 'STRAPI'].map((p) => (
                <button
                  type="button"
                  key={p}
                  onClick={() => setCmsConfig((prev) => ({ ...prev, provider: p }))}
                  className={`flex-1 py-2 rounded-xl font-mono text-xs font-bold border ${
                    cmsConfig.provider === p
                      ? 'bg-emerald-500/20 border-emerald-400 text-emerald-300'
                      : 'bg-slate-900 border-slate-800 text-slate-400'
                  }`}
                >
                  {p}
                </button>
              ))}
            </div>

            {cmsConfig.provider === 'CONTENTFUL' ? (
              <div className="space-y-3">
                <input
                  type="text"
                  value={cmsConfig.contentfulSpaceId}
                  onChange={(e) => setCmsConfig({ ...cmsConfig, contentfulSpaceId: e.target.value })}
                  placeholder="Contentful Space ID"
                  className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
                />
                <input
                  type="text"
                  value={cmsConfig.contentfulEnvironment}
                  onChange={(e) => setCmsConfig({ ...cmsConfig, contentfulEnvironment: e.target.value })}
                  placeholder="Environment (default: master)"
                  className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
                />
                <input
                  type="password"
                  value={cmsConfig.contentfulDeliveryToken}
                  onChange={(e) => setCmsConfig({ ...cmsConfig, contentfulDeliveryToken: e.target.value })}
                  placeholder="Content Delivery API Token (CDA - Read)"
                  className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
                />
                <input
                  type="password"
                  value={cmsConfig.contentfulManagementToken}
                  onChange={(e) => setCmsConfig({ ...cmsConfig, contentfulManagementToken: e.target.value })}
                  placeholder="Content Management API Token (CMA - Write/Publish)"
                  className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
                />
              </div>
            ) : (
              <div className="space-y-3">
                <input
                  type="url"
                  value={cmsConfig.strapiBaseUrl}
                  onChange={(e) => setCmsConfig({ ...cmsConfig, strapiBaseUrl: e.target.value })}
                  placeholder="Strapi Base URL (e.g. https://cms.raseldevbd.com)"
                  className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
                />
                <input
                  type="password"
                  value={cmsConfig.strapiApiToken}
                  onChange={(e) => setCmsConfig({ ...cmsConfig, strapiApiToken: e.target.value })}
                  placeholder="Strapi API Token (Bearer)"
                  className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
                />
              </div>
            )}

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setIsCmsDrawerOpen(false)}
                className="px-4 py-2 rounded-xl border border-slate-700 text-xs font-mono text-slate-300"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-xs"
              >
                Save & Sync CMS
              </button>
            </div>
          </form>
        </div>
      )}

      {/* No-Code Headless CMS Post Composer Modal */}
      {composerOpen && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <form
            onSubmit={handleCreatePost}
            className="w-full max-w-xl bg-[#111827] border border-slate-700 rounded-3xl p-6 space-y-3 shadow-2xl my-8"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-display font-bold text-white">
                Post Content via Headless CMS ({cmsConfig.provider})
              </h3>
              <button
                type="button"
                onClick={() => setComposerOpen(false)}
                className="text-slate-400 hover:text-white"
              >
                ✕
              </button>
            </div>

            <div className="grid grid-cols-3 gap-2">
              {['BLOG', 'PROJECT', 'PHOTO'].map((t) => (
                <button
                  type="button"
                  key={t}
                  onClick={() => setDraft({ ...draft, contentType: t })}
                  className={`py-2 rounded-xl font-mono text-xs font-bold border ${
                    draft.contentType === t
                      ? 'bg-emerald-500 text-slate-950 border-emerald-400'
                      : 'bg-slate-900 border-slate-800 text-slate-300'
                  }`}
                >
                  {t}
                </button>
              ))}
            </div>

            <input
              type="text"
              required
              value={draft.title}
              onChange={(e) => setDraft({ ...draft, title: e.target.value })}
              placeholder="Post / Project / Photo Title *"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              <input
                type="text"
                value={draft.categoryName}
                onChange={(e) => setDraft({ ...draft, categoryName: e.target.value })}
                placeholder="Dynamic Category Name"
                className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
              />
              <input
                type="text"
                value={draft.techStackCsv}
                onChange={(e) => setDraft({ ...draft, techStackCsv: e.target.value })}
                placeholder="Tags (comma-separated)"
                className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
              />
            </div>

            {draft.contentType === 'PHOTO' && (
              <input
                type="text"
                value={draft.photoCaption}
                onChange={(e) => setDraft({ ...draft, photoCaption: e.target.value })}
                placeholder="Photo Caption & Story"
                className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
              />
            )}

            <div className="space-y-1">
              <label className="text-xs font-mono text-slate-400">
                Media Image URL or Upload Local Photo
              </label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={draft.mediaUrl}
                  onChange={(e) => setDraft({ ...draft, mediaUrl: e.target.value })}
                  placeholder="https://..."
                  className="flex-1 px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
                />
                <label className="px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-mono text-cyan-300 cursor-pointer flex items-center">
                  Upload
                  <input type="file" accept="image/*" onChange={handleMediaFileUpload} className="hidden" />
                </label>
              </div>
            </div>

            <textarea
              rows={2}
              value={draft.summary}
              onChange={(e) => setDraft({ ...draft, summary: e.target.value })}
              placeholder="Short Summary"
              className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
            />

            <textarea
              rows={4}
              value={draft.markdownBody}
              onChange={(e) => setDraft({ ...draft, markdownBody: e.target.value })}
              placeholder="Full Markdown Article / Project Details..."
              className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm text-white"
            />

            <label className="flex items-center gap-2 text-xs text-emerald-300 font-mono">
              <input
                type="checkbox"
                checked={draft.publishToRemoteCms}
                onChange={(e) => setDraft({ ...draft, publishToRemoteCms: e.target.checked })}
              />
              <span>Publish directly to {cmsConfig.provider} API (Zero-Code Cloud Post)</span>
            </label>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setComposerOpen(false)}
                className="px-4 py-2 rounded-xl border border-slate-700 text-xs font-mono text-slate-300"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-xs"
              >
                Publish Now
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
