import React, { useState, useEffect, useMemo } from 'react';

const STORAGE_KEY = 'rasel_dev_bd_static_cms_v1';

const FALLBACK_BUNDLE = {
  schemaVersion: '1.0.0',
  siteConfig: {
    siteTitle: 'Rasel Dev BD',
    tagline: 'Full-Stack React & Android Architect • Building Cloud-Native Products from Dhaka',
    bio: "Hi, I'm Rasel — a full-stack engineer and open-source creator based in Dhaka, Bangladesh. I specialize in React 19 static/SSR architectures, TypeScript design systems, and native Android apps with Jetpack Compose.",
    githubUsername: 'rasel-dev-bd',
    githubPagesDomain: 'https://rasel-dev-bd.github.io',
    email: 'alexraselchodhury@gmail.com',
    location: 'Dhaka, Bangladesh 🇧🇩',
    adminPasscode: '2026',
    skills: [
      'React 19',
      'TypeScript',
      'Next.js',
      'Tailwind CSS',
      'Kotlin',
      'Jetpack Compose',
      'Node.js',
      'PostgreSQL',
      'GitHub Actions',
      'Docker',
    ],
  },
  categories: [
    {
      id: 1,
      name: 'React & Static Web',
      slug: 'react-static-web',
      contentTypeScope: 'ALL',
      colorHex: '#10B981',
      description: 'React 19, Vite, Next.js, and GitHub Pages static site architectures.',
    },
    {
      id: 2,
      name: 'Android & Kotlin',
      slug: 'android-kotlin',
      contentTypeScope: 'ALL',
      colorHex: '#06B6D4',
      description: 'Jetpack Compose, Room offline-first databases, and Material 3 apps.',
    },
    {
      id: 3,
      name: 'Cloud & DevOps',
      slug: 'cloud-devops',
      contentTypeScope: 'ALL',
      colorHex: '#8B5CF6',
      description: 'GitHub Actions CI/CD, Docker containers, edge functions, and telemetry.',
    },
    {
      id: 4,
      name: 'Dhaka Tech & Street',
      slug: 'dhaka-tech-street',
      contentTypeScope: 'PHOTO',
      colorHex: '#F59E0B',
      description: 'Visual stories, twilight architecture, and developer life across Bangladesh.',
    },
    {
      id: 5,
      name: 'System Architecture',
      slug: 'system-architecture',
      contentTypeScope: 'BLOG',
      colorHex: '#F43F5E',
      description: 'Deep-dive engineering notes on scalability, state synchronization, and UI performance.',
    },
  ],
  contentItems: [
    {
      id: 101,
      contentType: 'PROJECT',
      title: 'Rasel Dev BD — Static CMS & GitHub Pages Engine',
      slug: 'rasel-dev-bd-static-cms',
      summaryOrCaption:
        'Zero-backend React 19 + GitHub Pages portfolio and content platform with automated JSON bundle generation, dynamic category routing, and offline-first authoring.',
      bodyMarkdown:
        "## Overview\n**Rasel Dev BD Static CMS** bridges local content authoring with zero-cost static hosting on GitHub Pages (`rasel-dev-bd.github.io`).\n\n### Key Engineering Highlights\n- **Dynamic Category Taxonomy**: Manage unified or scoped categories across Projects, Blog Articles, and Photo Galleries.\n- **Deterministic Static JSON Bundle**: Compiles all published entries into `content-bundle.json`.\n\n```tsx\nexport async function loadPortfolioBundle() {\n  const res = await fetch('./data/content-bundle.json');\n  return await res.json();\n}\n```",
      categoryId: 1,
      categoryName: 'React & Static Web',
      staticAssetPath:
        'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80',
      tags: ['React 19', 'TypeScript', 'Vite', 'GitHub Pages', 'Tailwind CSS'],
      liveUrl: 'https://rasel-dev-bd.github.io',
      repoUrl: 'https://github.com/rasel-dev-bd/rasel-dev-bd.github.io',
      isFeatured: true,
      isPublished: true,
      likesCount: 128,
      publishedDateIso: '2026-09-25',
    },
    {
      id: 201,
      contentType: 'BLOG',
      title: 'Architecting a Static-First React CMS for GitHub Pages Without a Backend Server',
      slug: 'static-first-react-cms-github-pages',
      summaryOrCaption:
        'How to combine an offline-capable Admin Dashboard with deterministic JSON asset generation so your portfolio scales to 100k+ views for $0/month.',
      bodyMarkdown:
        '## Why Static Site Generation Still Wins\nWhen hosting on **GitHub Pages**, you do not have a runtime Node.js server or SQL database executing on every request. Yet developers still want a rich **Admin Dashboard** to organize dynamic categories, write markdown blog posts, and upload project & photo galleries.',
      categoryId: 1,
      categoryName: 'React & Static Web',
      staticAssetPath:
        'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=1200&q=80',
      tags: ['React', 'GitHub Pages', 'SSG', 'CMS'],
      liveUrl: '6 min read',
      repoUrl: '/blog/static-first-react-cms-github-pages',
      isFeatured: true,
      isPublished: true,
      likesCount: 142,
      publishedDateIso: '2026-09-26',
    },
    {
      id: 301,
      contentType: 'PHOTO',
      title: 'Blue Hour Reflections Over Hatirjheel',
      slug: 'blue-hour-hatirjheel-dhaka',
      summaryOrCaption:
        "Captured just 20 minutes after sunset as the curved bridges of Hatirjheel lit up in neon cyan and emerald against Dhaka's twilight sky.",
      bodyMarkdown:
        '### Behind the Shot\nCaptured from the Hatirjheel overpass in Dhaka, Bangladesh.\n\n- **Location**: Hatirjheel, Dhaka\n- **Camera**: Sony A7 IV • 24mm f/1.4 GM',
      categoryId: 4,
      categoryName: 'Dhaka Tech & Street',
      staticAssetPath:
        'https://images.unsplash.com/photo-1596895111956-bf1cf0599ce5?auto=format&fit=crop&w=1200&q=80',
      tags: ['Dhaka', 'Hatirjheel', 'Twilight', 'Bangladesh'],
      liveUrl: 'Hatirjheel, Dhaka 🇧🇩',
      repoUrl: '24mm • f/5.6 • 2.5s • ISO 200',
      isFeatured: true,
      isPublished: true,
      likesCount: 215,
      publishedDateIso: '2026-09-24',
    },
  ],
};

function toSlug(str) {
  return (
    (str || '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '') || 'item-' + Date.now()
  );
}

function MarkdownBlocks({ markdown }) {
  const segments = (markdown || '').split('```');
  return (
    <div className="space-y-4">
      {segments.map((seg, idx) => {
        if (idx % 2 === 1) {
          const lines = seg.trim().split('\n');
          const lang =
            lines[0] && lines[0].length < 16 && !lines[0].includes(' ') ? lines[0] : 'code';
          const code = lang !== 'code' ? lines.slice(1).join('\n') : seg.trim();
          return (
            <div
              key={idx}
              className="rounded-xl bg-[#050811] border border-slate-800 p-4 overflow-x-auto"
            >
              <div className="text-[11px] font-mono text-emerald-400 uppercase mb-2">{lang}</div>
              <pre className="text-xs font-mono text-slate-200 leading-relaxed">{code}</pre>
            </div>
          );
        }
        return seg
          .trim()
          .split('\n\n')
          .filter(Boolean)
          .map((p, pIdx) => {
            const t = p.trim();
            if (t.startsWith('## ')) {
              return (
                <h3 key={pIdx} className="text-xl font-bold text-emerald-400 pt-2">
                  {t.replace('## ', '')}
                </h3>
              );
            }
            if (t.startsWith('### ')) {
              return (
                <h4 key={pIdx} className="text-base font-bold text-white pt-1">
                  {t.replace('### ', '')}
                </h4>
              );
            }
            return (
              <p key={pIdx} className="text-slate-300 leading-relaxed whitespace-pre-line">
                {t}
              </p>
            );
          });
      })}
    </div>
  );
}

export default function App() {
  const [siteConfig, setSiteConfig] = useState(FALLBACK_BUNDLE.siteConfig);
  const [categories, setCategories] = useState(FALLBACK_BUNDLE.categories);
  const [items, setItems] = useState(FALLBACK_BUNDLE.contentItems);
  const [activeNav, setActiveNav] = useState('HOME');
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [detailItem, setDetailItem] = useState(null);
  const [toastMsg, setToastMsg] = useState(null);

  const [isAdminUnlocked, setIsAdminUnlocked] = useState(false);
  const [pinInput, setPinInput] = useState('');
  const [adminTab, setAdminTab] = useState('CONTENT');
  const [editingItem, setEditingItem] = useState(null);
  const [editingCategory, setEditingCategory] = useState(null);

  const showToast = (msg) => {
    setToastMsg(msg);
    setTimeout(() => setToastMsg(null), 3800);
  };

  useEffect(() => {
    const cached = localStorage.getItem(STORAGE_KEY);
    if (cached) {
      try {
        const parsed = JSON.parse(cached);
        if (parsed.siteConfig) setSiteConfig({ adminPasscode: '2026', ...parsed.siteConfig });
        if (Array.isArray(parsed.categories)) setCategories(parsed.categories);
        if (Array.isArray(parsed.contentItems)) setItems(parsed.contentItems);
        return;
      } catch (e) {}
    }
    fetch('./data/content-bundle.json')
      .then((r) => r.json())
      .then((data) => {
        if (data.siteConfig) setSiteConfig({ adminPasscode: '2026', ...data.siteConfig });
        if (Array.isArray(data.categories)) setCategories(data.categories);
        if (Array.isArray(data.contentItems)) setItems(data.contentItems);
      })
      .catch(() => {});
  }, []);

  const persistToStorage = (nextConfig, nextCategories, nextItems) => {
    const payload = {
      schemaVersion: '1.0.0',
      generatedAt: new Date().toISOString(),
      siteConfig: nextConfig,
      categories: nextCategories,
      contentItems: nextItems,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload, null, 2));
  };

  const publishedItems = useMemo(
    () => items.filter((i) => i.isPublished !== false),
    [items]
  );

  const filteredItems = useMemo(() => {
    return publishedItems.filter((item) => {
      const matchesType =
        activeNav === 'HOME' || activeNav === 'ADMIN' || item.contentType === activeNav;
      const matchesCat =
        selectedCategoryId === null || Number(item.categoryId) === Number(selectedCategoryId);
      const q = searchQuery.toLowerCase();
      const matchesQuery =
        !q ||
        (item.title || '').toLowerCase().includes(q) ||
        (item.summaryOrCaption || '').toLowerCase().includes(q) ||
        (item.categoryName || '').toLowerCase().includes(q) ||
        (item.tags || []).some((t) => t.toLowerCase().includes(q));
      return matchesType && matchesCat && matchesQuery;
    });
  }, [publishedItems, activeNav, selectedCategoryId, searchQuery]);

  const handleLike = (id, e) => {
    if (e) e.stopPropagation();
    const next = items.map((item) =>
      item.id === id ? { ...item, likesCount: (item.likesCount || 0) + 1 } : item
    );
    setItems(next);
    if (detailItem && detailItem.id === id) {
      setDetailItem({ ...detailItem, likesCount: (detailItem.likesCount || 0) + 1 });
    }
    persistToStorage(siteConfig, categories, next);
  };

  const handleDownloadStaticBundle = () => {
    const bundleObj = {
      schemaVersion: '1.0.0',
      generator: 'Rasel Dev BD Static Site Engine (GitHub Pages Compatible)',
      generatedAt: new Date().toISOString(),
      siteConfig,
      categories,
      contentItems: publishedItems,
    };
    const blob = new Blob([JSON.stringify(bundleObj, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'content-bundle.json';
    a.click();
    URL.revokeObjectURL(url);
    showToast('Downloaded content-bundle.json for GitHub Pages!');
  };

  const handleSaveContentItem = (e) => {
    e.preventDefault();
    const catObj =
      categories.find((c) => Number(c.id) === Number(editingItem.categoryId)) || categories[0];
    const finalItem = {
      ...editingItem,
      id: editingItem.id || Date.now(),
      slug: toSlug(editingItem.slug || editingItem.title),
      categoryId: catObj ? catObj.id : 1,
      categoryName: catObj ? catObj.name : 'General',
      tags:
        typeof editingItem.tags === 'string'
          ? editingItem.tags
              .split(',')
              .map((t) => t.trim())
              .filter(Boolean)
          : editingItem.tags || [],
      publishedDateIso: editingItem.publishedDateIso || new Date().toISOString().slice(0, 10),
    };
    const exists = items.some((i) => i.id === finalItem.id);
    const next = exists
      ? items.map((i) => (i.id === finalItem.id ? finalItem : i))
      : [finalItem, ...items];
    setItems(next);
    persistToStorage(siteConfig, categories, next);
    setEditingItem(null);
    showToast(`Saved "${finalItem.title}" (${finalItem.contentType})`);
  };

  const handleSaveCategory = (e) => {
    e.preventDefault();
    const finalCat = {
      ...editingCategory,
      id: editingCategory.id || Date.now(),
      slug: toSlug(editingCategory.slug || editingCategory.name),
    };
    const exists = categories.some((c) => c.id === finalCat.id);
    const nextCats = exists
      ? categories.map((c) => (c.id === finalCat.id ? finalCat : c))
      : [...categories, finalCat];
    const nextItems = items.map((item) =>
      Number(item.categoryId) === Number(finalCat.id)
        ? { ...item, categoryName: finalCat.name }
        : item
    );
    setCategories(nextCats);
    setItems(nextItems);
    persistToStorage(siteConfig, nextCats, nextItems);
    setEditingCategory(null);
    showToast(`Saved category "${finalCat.name}"`);
  };

  const handlePhotoUploadFile = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      setEditingItem({
        ...editingItem,
        staticAssetPath: reader.result,
        mediaSource: reader.result,
      });
      showToast('Uploaded image converted for static hosting!');
    };
    reader.readAsDataURL(file);
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#090E1A] text-slate-100">
      <header className="sticky top-0 z-30 border-b border-slate-800/80 bg-[#090E1A]/90 backdrop-blur-md">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 py-3.5 flex flex-wrap items-center justify-between gap-4">
          <div
            onClick={() => {
              setActiveNav('HOME');
              setDetailItem(null);
            }}
            className="flex items-center gap-3 cursor-pointer"
          >
            <span className="px-2.5 py-1.5 rounded-xl bg-emerald-500/15 border border-emerald-500/50 text-emerald-400 font-mono font-bold text-sm">
              &lt;R/&gt;
            </span>
            <div>
              <h1 className="font-bold text-lg tracking-tight text-white">{siteConfig.siteTitle}</h1>
              <p className="text-[11px] font-mono text-emerald-400">
                {isAdminUnlocked
                  ? 'Admin Studio Unlocked • SSG Ready'
                  : 'Developer Portfolio & Content Platform'}
              </p>
            </div>
          </div>

          <nav className="flex items-center gap-1.5 flex-wrap">
            {[
              { id: 'HOME', label: 'Portfolio' },
              { id: 'PROJECT', label: 'Projects' },
              { id: 'BLOG', label: 'Blog' },
              { id: 'PHOTO', label: 'Photos' },
              { id: 'ADMIN', label: isAdminUnlocked ? 'Admin Studio (Unlocked)' : 'Admin Dashboard' },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  setActiveNav(tab.id);
                  setDetailItem(null);
                }}
                className={`px-3.5 py-2 rounded-xl text-xs font-mono transition ${
                  activeNav === tab.id
                    ? 'bg-emerald-500 text-slate-950 font-bold shadow-lg shadow-emerald-500/20'
                    : 'bg-slate-900 text-slate-300 hover:bg-slate-800 border border-slate-800'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </div>
      </header>

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 py-8">
        {detailItem ? (
          <div className="max-w-3xl mx-auto bg-[#111827] border border-slate-800 rounded-3xl overflow-hidden shadow-2xl">
            <div className="relative h-72 sm:h-96 bg-slate-900">
              <img
                src={detailItem.staticAssetPath || detailItem.mediaSource}
                alt={detailItem.title}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#090E1A] via-[#090E1A]/40 to-transparent" />
              <button
                onClick={() => setDetailItem(null)}
                className="absolute top-4 left-4 px-4 py-2 rounded-xl bg-slate-950/80 border border-slate-700 text-xs font-mono text-white hover:border-emerald-400"
              >
                ← Back
              </button>
              <div className="absolute bottom-5 left-6 right-6">
                <div className="flex items-center gap-2 mb-2">
                  <span className="px-2.5 py-1 rounded-md bg-emerald-500/20 border border-emerald-500/50 text-emerald-300 font-mono text-xs font-bold">
                    {detailItem.contentType}
                  </span>
                  <span className="px-2.5 py-1 rounded-md bg-slate-900/80 border border-slate-700 text-cyan-300 font-mono text-xs">
                    {detailItem.categoryName}
                  </span>
                </div>
                <h2 className="text-2xl sm:text-3xl font-bold text-white">{detailItem.title}</h2>
              </div>
            </div>

            <div className="p-6 sm:p-8 space-y-6">
              <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 pb-4">
                <div className="text-xs font-mono text-slate-400">
                  Published {(detailItem.publishedDateIso || '').slice(0, 10)} • /
                  {detailItem.contentType.toLowerCase()}/{detailItem.slug}
                </div>
                <button
                  onClick={(e) => handleLike(detailItem.id, e)}
                  className="px-4 py-2 rounded-xl bg-rose-500/15 border border-rose-500/40 text-rose-300 text-xs font-mono font-bold hover:bg-rose-500/25"
                >
                  ♥ {detailItem.likesCount || 0} Appreciations
                </button>
              </div>

              <div className="p-4 rounded-2xl bg-slate-900/90 border border-emerald-500/30">
                <p className="text-slate-200 leading-relaxed">{detailItem.summaryOrCaption}</p>
              </div>

              <MarkdownBlocks markdown={detailItem.bodyMarkdown} />
            </div>
          </div>
        ) : activeNav === 'ADMIN' ? (
          !isAdminUnlocked ? (
            <div className="max-w-md mx-auto my-12 p-8 rounded-3xl bg-[#111827] border border-emerald-500/40 shadow-2xl text-center space-y-5">
              <h2 className="text-2xl font-bold">Admin Studio Security</h2>
              <p className="text-sm text-slate-400">
                Enter your admin passcode to manage dynamic categories, blog posts, photo uploads,
                project showcases, and GitHub Pages static exports.
              </p>
              <input
                type="password"
                value={pinInput}
                onChange={(e) => setPinInput(e.target.value)}
                placeholder="Enter Admin Passcode"
                className="w-full px-4 py-3 rounded-xl bg-slate-950 border border-slate-700 text-center font-mono text-white"
              />
              <button
                onClick={() => {
                  if (pinInput.trim() === (siteConfig.adminPasscode || '2026')) {
                    setIsAdminUnlocked(true);
                    showToast('Admin Studio unlocked!');
                  } else {
                    showToast('Invalid PIN! Default PIN is ' + (siteConfig.adminPasscode || '2026'));
                  }
                }}
                className="w-full py-3 rounded-xl bg-emerald-500 text-slate-950 font-mono font-bold text-sm"
              >
                Unlock Admin Studio
              </button>
              <button
                onClick={() => {
                  setPinInput(siteConfig.adminPasscode || '2026');
                  setIsAdminUnlocked(true);
                  showToast('Unlocked with default PIN (2026)');
                }}
                className="w-full py-2.5 rounded-xl bg-slate-800 text-slate-300 font-mono text-xs"
              >
                Quick Auto-Fill & Unlock (PIN: {siteConfig.adminPasscode || '2026'})
              </button>
            </div>
          ) : (
            <div className="space-y-6">
              <div className="p-6 rounded-3xl bg-[#111827] border border-slate-800 flex flex-wrap items-center justify-between gap-4">
                <div>
                  <span className="text-xs font-mono text-emerald-400 uppercase font-bold">
                    ● Admin Studio Active
                  </span>
                  <h2 className="text-2xl font-bold">Content & Static Site Manager</h2>
                </div>
                <div className="flex flex-wrap gap-2">
                  <button
                    onClick={handleDownloadStaticBundle}
                    className="px-4 py-2.5 rounded-xl bg-emerald-500 text-slate-950 font-mono font-bold text-xs"
                  >
                    ⬇ Export content-bundle.json
                  </button>
                  <button
                    onClick={() => setIsAdminUnlocked(false)}
                    className="px-4 py-2.5 rounded-xl bg-slate-800 text-slate-300 font-mono text-xs"
                  >
                    Lock Session
                  </button>
                </div>
              </div>

              <div className="flex gap-2 overflow-x-auto pb-2">
                {[
                  { id: 'CONTENT', label: `Content Items (${items.length})` },
                  { id: 'CATEGORIES', label: `Dynamic Categories (${categories.length})` },
                ].map((t) => (
                  <button
                    key={t.id}
                    onClick={() => setAdminTab(t.id)}
                    className={`px-4 py-2.5 rounded-xl text-xs font-mono shrink-0 ${
                      adminTab === t.id
                        ? 'bg-cyan-500 text-slate-950 font-bold'
                        : 'bg-slate-900 text-slate-300 border border-slate-800'
                    }`}
                  >
                    {t.label}
                  </button>
                ))}
              </div>

              {adminTab === 'CONTENT' && (
                <div className="space-y-6">
                  <div className="flex flex-wrap gap-3">
                    {['BLOG', 'PHOTO', 'PROJECT'].map((type) => (
                      <button
                        key={type}
                        onClick={() =>
                          setEditingItem({
                            id: 0,
                            contentType: type,
                            title: '',
                            slug: '',
                            summaryOrCaption: '',
                            bodyMarkdown: '',
                            categoryId: categories[0]?.id || 1,
                            categoryName: categories[0]?.name || 'General',
                            staticAssetPath:
                              'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80',
                            tags: 'React 19, TypeScript, GitHub Pages',
                            liveUrl:
                              type === 'BLOG'
                                ? '5 min read'
                                : type === 'PHOTO'
                                ? 'Dhaka, Bangladesh 🇧🇩'
                                : 'https://rasel-dev-bd.github.io',
                            repoUrl:
                              type === 'PHOTO'
                                ? '35mm • f/1.8 • ISO 200'
                                : 'https://github.com/rasel-dev-bd',
                            isFeatured: false,
                            isPublished: true,
                            likesCount: 1,
                          })
                        }
                        className="px-4 py-2.5 rounded-xl bg-emerald-500/15 border border-emerald-500/40 text-emerald-300 font-mono text-xs font-bold"
                      >
                        + New {type}
                      </button>
                    ))}
                  </div>

                  {editingItem && (
                    <form
                      onSubmit={handleSaveContentItem}
                      className="p-6 rounded-3xl bg-[#111827] border border-emerald-500/50 space-y-4"
                    >
                      <input
                        type="text"
                        required
                        placeholder="Title *"
                        value={editingItem.title}
                        onChange={(e) =>
                          setEditingItem({
                            ...editingItem,
                            title: e.target.value,
                            slug: toSlug(e.target.value),
                          })
                        }
                        className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-700 text-sm"
                      />
                      <input
                        type="file"
                        accept="image/*"
                        onChange={handlePhotoUploadFile}
                        className="w-full text-xs text-slate-300"
                      />
                      <textarea
                        rows="2"
                        required
                        placeholder="Caption or Summary *"
                        value={editingItem.summaryOrCaption}
                        onChange={(e) =>
                          setEditingItem({ ...editingItem, summaryOrCaption: e.target.value })
                        }
                        className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-700 text-sm"
                      />
                      <textarea
                        rows="4"
                        placeholder="Markdown Body"
                        value={editingItem.bodyMarkdown}
                        onChange={(e) =>
                          setEditingItem({ ...editingItem, bodyMarkdown: e.target.value })
                        }
                        className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-700 text-sm font-mono"
                      />
                      <div className="flex gap-3">
                        <button
                          type="submit"
                          className="px-6 py-2.5 rounded-xl bg-emerald-500 text-slate-950 font-mono font-bold text-xs"
                        >
                          Save Content
                        </button>
                        <button
                          type="button"
                          onClick={() => setEditingItem(null)}
                          className="px-5 py-2.5 rounded-xl bg-slate-800 text-slate-300 font-mono text-xs"
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                  )}

                  <div className="space-y-3">
                    {items.map((item) => (
                      <div
                        key={item.id}
                        className="p-4 rounded-2xl bg-[#111827] border border-slate-800 flex flex-wrap items-center justify-between gap-4"
                      >
                        <div>
                          <span className="text-xs font-mono text-emerald-400 font-bold mr-2">
                            {item.contentType}
                          </span>
                          <span className="text-xs font-mono text-cyan-400">{item.categoryName}</span>
                          <h4 className="font-bold text-white">{item.title}</h4>
                        </div>
                        <div className="flex gap-2">
                          <button
                            onClick={() => setEditingItem(item)}
                            className="px-3 py-1.5 rounded-lg bg-slate-800 text-emerald-400 text-xs font-mono"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => {
                              const next = items.filter((i) => i.id !== item.id);
                              setItems(next);
                              persistToStorage(siteConfig, categories, next);
                            }}
                            className="px-3 py-1.5 rounded-lg bg-rose-500/15 text-rose-400 text-xs font-mono"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {adminTab === 'CATEGORIES' && (
                <div className="space-y-4">
                  <button
                    onClick={() =>
                      setEditingCategory({
                        id: 0,
                        name: '',
                        slug: '',
                        contentTypeScope: 'ALL',
                        colorHex: '#10B981',
                        description: '',
                      })
                    }
                    className="px-4 py-2.5 rounded-xl bg-emerald-500 text-slate-950 font-mono font-bold text-xs"
                  >
                    + Add Dynamic Category
                  </button>
                  {editingCategory && (
                    <form
                      onSubmit={handleSaveCategory}
                      className="p-5 rounded-2xl bg-[#111827] border border-emerald-500/50 space-y-3"
                    >
                      <input
                        type="text"
                        required
                        placeholder="Category Name *"
                        value={editingCategory.name}
                        onChange={(e) =>
                          setEditingCategory({
                            ...editingCategory,
                            name: e.target.value,
                            slug: toSlug(e.target.value),
                          })
                        }
                        className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-700 text-sm"
                      />
                      <button
                        type="submit"
                        className="px-5 py-2 rounded-xl bg-emerald-500 text-slate-950 font-mono font-bold text-xs"
                      >
                        Save Category
                      </button>
                    </form>
                  )}
                </div>
              )}
            </div>
          )
        ) : (
          <div className="space-y-8">
            {activeNav === 'HOME' && (
              <section className="rounded-3xl border border-emerald-500/30 bg-gradient-to-br from-[#111827] via-[#0F172A] to-emerald-950/50 p-6 sm:p-10">
                <span className="inline-block px-3 py-1 rounded-full bg-emerald-500/15 text-emerald-300 text-xs font-mono mb-3">
                  STATIC SSG READY • GITHUB PAGES
                </span>
                <h2 className="text-3xl sm:text-5xl font-bold text-white mb-2">
                  {siteConfig.siteTitle}
                </h2>
                <p className="text-lg text-emerald-300 mb-3">{siteConfig.tagline}</p>
                <p className="text-sm text-slate-300 max-w-3xl">{siteConfig.bio}</p>
              </section>
            )}

            <div className="flex items-center gap-2 overflow-x-auto pb-2">
              <button
                onClick={() => setSelectedCategoryId(null)}
                className={`px-4 py-2 rounded-xl text-xs font-mono shrink-0 ${
                  selectedCategoryId === null
                    ? 'bg-emerald-500 text-slate-950 font-bold'
                    : 'bg-[#111827] text-slate-300 border border-slate-800'
                }`}
              >
                All Categories ({categories.length})
              </button>
              {categories.map((cat) => (
                <button
                  key={cat.id}
                  onClick={() =>
                    setSelectedCategoryId(selectedCategoryId === cat.id ? null : cat.id)
                  }
                  className={`px-4 py-2 rounded-xl text-xs font-mono shrink-0 border ${
                    Number(selectedCategoryId) === Number(cat.id)
                      ? 'bg-emerald-500 text-slate-950 border-emerald-400 font-bold'
                      : 'bg-[#111827] text-slate-300 border-slate-800'
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
                  onClick={() => setDetailItem(item)}
                  className="cursor-pointer rounded-3xl bg-[#111827] border border-slate-800 overflow-hidden flex flex-col justify-between hover:border-emerald-500/60 transition"
                >
                  <div>
                    <img
                      src={item.staticAssetPath || item.mediaSource}
                      alt={item.title}
                      className="w-full h-48 object-cover bg-slate-900"
                    />
                    <div className="p-5 space-y-2">
                      <div className="flex justify-between text-xs font-mono text-emerald-400">
                        <span>{item.contentType}</span>
                        <span>{item.categoryName}</span>
                      </div>
                      <h3 className="text-lg font-bold text-white">{item.title}</h3>
                      <p className="text-sm text-slate-300 line-clamp-3">
                        {item.summaryOrCaption}
                      </p>
                    </div>
                  </div>
                  <div className="px-5 py-3 border-t border-slate-800 flex justify-between text-xs font-mono text-slate-400">
                    <span>{(item.publishedDateIso || '').slice(0, 10)}</span>
                    <button
                      onClick={(e) => handleLike(item.id, e)}
                      className="text-rose-400 font-bold"
                    >
                      ♥ {item.likesCount || 0}
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </div>
        )}
      </main>

      {toastMsg && (
        <div className="fixed bottom-5 right-5 z-50 px-5 py-3 rounded-2xl bg-emerald-950 border border-emerald-400 text-emerald-200 text-xs font-mono shadow-2xl">
          ✓ {toastMsg}
        </div>
      )}
    </div>
  );
}
