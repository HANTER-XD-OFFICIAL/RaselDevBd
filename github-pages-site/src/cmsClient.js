/**
 * Rasel Dev BD — Headless CMS Integration Layer (Contentful & Strapi)
 *
 * Supports:
 * 1. Contentful Content Delivery API (CDA) for live reading of entries & media assets
 * 2. Contentful Content Management API (CMA) for direct no-code publishing from Admin Studio
 * 3. Strapi v4/v5 REST API (/api/portfolio-items & /api/categories) for reading & publishing
 * 4. Automatic fallback to static ./data/content-bundle.json on GitHub Pages
 */

const CMS_STORAGE_KEY = 'rasel_dev_bd_headless_cms_config_v2';

export function getCmsConfig() {
  let saved = {};
  try {
    const raw = localStorage.getItem(CMS_STORAGE_KEY);
    if (raw) saved = JSON.parse(raw);
  } catch (_) {}

  const env = (typeof import.meta !== 'undefined' && import.meta.env) || {};

  return {
    provider: saved.provider || env.VITE_CMS_PROVIDER || 'CONTENTFUL',
    contentfulSpaceId: saved.contentfulSpaceId || env.VITE_CONTENTFUL_SPACE_ID || '',
    contentfulEnvironment: saved.contentfulEnvironment || env.VITE_CONTENTFUL_ENVIRONMENT || 'master',
    contentfulDeliveryToken: saved.contentfulDeliveryToken || env.VITE_CONTENTFUL_DELIVERY_TOKEN || '',
    contentfulManagementToken: saved.contentfulManagementToken || env.VITE_CONTENTFUL_MANAGEMENT_TOKEN || '',
    strapiBaseUrl: saved.strapiBaseUrl || env.VITE_STRAPI_BASE_URL || 'https://cms.raseldevbd.com',
    strapiApiToken: saved.strapiApiToken || env.VITE_STRAPI_API_TOKEN || ''
  };
}

export function saveCmsConfig(config) {
  localStorage.setItem(CMS_STORAGE_KEY, JSON.stringify(config));
}

export function isCmsReadConfigured(cfg = getCmsConfig()) {
  if (cfg.provider === 'STRAPI') {
    return Boolean(cfg.strapiBaseUrl && cfg.strapiBaseUrl.startsWith('http'));
  }
  return Boolean(
    cfg.contentfulSpaceId &&
      cfg.contentfulSpaceId !== 'YOUR_CONTENTFUL_SPACE_ID' &&
      cfg.contentfulDeliveryToken &&
      cfg.contentfulDeliveryToken !== 'YOUR_CONTENTFUL_DELIVERY_TOKEN'
  );
}

export function isCmsWriteConfigured(cfg = getCmsConfig()) {
  if (cfg.provider === 'STRAPI') {
    return Boolean(
      cfg.strapiBaseUrl &&
        cfg.strapiBaseUrl.startsWith('http') &&
        cfg.strapiApiToken &&
        cfg.strapiApiToken !== 'YOUR_STRAPI_API_TOKEN'
    );
  }
  return Boolean(
    cfg.contentfulSpaceId &&
      cfg.contentfulSpaceId !== 'YOUR_CONTENTFUL_SPACE_ID' &&
      cfg.contentfulManagementToken &&
      cfg.contentfulManagementToken !== 'YOUR_CONTENTFUL_MANAGEMENT_TOKEN'
  );
}

export async function fetchHeadlessCmsItems(customConfig) {
  const cfg = customConfig || getCmsConfig();
  if (cfg.provider === 'STRAPI') {
    return fetchFromStrapi(cfg);
  }
  return fetchFromContentful(cfg);
}

export async function publishItemToHeadlessCms(item, customConfig) {
  const cfg = customConfig || getCmsConfig();
  if (cfg.provider === 'STRAPI') {
    return publishToStrapi(item, cfg);
  }
  return publishToContentful(item, cfg);
}

async function fetchFromContentful(cfg) {
  const spaceId = (cfg.contentfulSpaceId || '').trim();
  const env = (cfg.contentfulEnvironment || 'master').trim();
  const token = (cfg.contentfulDeliveryToken || '').trim();

  if (!spaceId || !token) {
    throw new Error('Contentful Space ID and Delivery Token are required.');
  }

  const url = `https://cdn.contentful.com/spaces/${spaceId}/environments/${env}/entries?include=2&limit=100`;
  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: 'application/json'
    }
  });

  if (!response.ok) {
    const errText = await response.text();
    throw new Error(`Contentful CDA ${response.status}: ${errText.slice(0, 100)}`);
  }

  const data = await response.json();
  const assetMap = {};
  (data.includes?.Asset || []).forEach((asset) => {
    const id = asset.sys?.id;
    const fileUrl = asset.fields?.file?.url;
    if (id && fileUrl) {
      assetMap[id] = fileUrl.startsWith('//') ? `https:${fileUrl}` : fileUrl;
    }
  });

  return (data.items || [])
    .map((entry, idx) => {
      const f = entry.fields || {};
      if (!f.title) return null;
      const linkedAssetId = f.media?.sys?.id;
      const techStack = Array.isArray(f.techStack)
        ? f.techStack
        : typeof f.techStackCsv === 'string'
        ? f.techStackCsv.split(',').map((s) => s.trim()).filter(Boolean)
        : [];

      return {
        id: entry.sys?.id || `cf-${idx}`,
        cmsEntryId: entry.sys?.id || '',
        cmsProvider: 'CONTENTFUL',
        title: f.title,
        slug: f.slug || f.title.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
        contentType: (f.contentType || 'BLOG').toUpperCase(),
        categoryId: f.categoryId || 1,
        categoryName: f.categoryName || 'React & Headless CMS',
        summary: f.summary || '',
        markdownBody: f.markdownBody || f.body || '',
        mediaUrl:
          f.mediaSource ||
          assetMap[linkedAssetId] ||
          'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80',
        photoCaption: f.photoCaption || '',
        photoLocation: f.photoLocation || '',
        exifCamera: f.exifCamera || '',
        techStack,
        liveDemoUrl: f.liveDemoUrl || '',
        repoUrl: f.repoUrl || '',
        readingTimeMinutes: Number(f.readingTimeMinutes || 5),
        isFeatured: Boolean(f.isFeatured),
        isPublished: true,
        updatedAtEpoch: entry.sys?.updatedAt ? new Date(entry.sys.updatedAt).getTime() : Date.now()
      };
    })
    .filter(Boolean);
}

async function publishToContentful(item, cfg) {
  const spaceId = (cfg.contentfulSpaceId || '').trim();
  const env = (cfg.contentfulEnvironment || 'master').trim();
  const cmaToken = (cfg.contentfulManagementToken || '').trim();

  if (!spaceId || !cmaToken) {
    throw new Error('Contentful Space ID and Content Management Token (CMA) are required.');
  }

  const loc = (val) => ({ 'en-US': val });
  const payload = {
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
    `https://api.contentful.com/spaces/${spaceId}/environments/${env}/entries`,
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${cmaToken}`,
        'Content-Type': 'application/vnd.contentful.management.v1+json',
        'X-Contentful-Content-Type': 'portfolioItem'
      },
      body: JSON.stringify(payload)
    }
  );

  if (!createRes.ok) {
    const errText = await createRes.text();
    throw new Error(`Contentful CMA ${createRes.status}: ${errText.slice(0, 120)}`);
  }

  const created = await createRes.json();
  const entryId = created.sys?.id;
  const version = created.sys?.version || 1;

  if (entryId) {
    await fetch(
      `https://api.contentful.com/spaces/${spaceId}/environments/${env}/entries/${entryId}/published`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${cmaToken}`,
          'X-Contentful-Version': String(version)
        }
      }
    );
  }

  return {
    entryId: entryId || `cf-${Date.now()}`,
    provider: 'CONTENTFUL'
  };
}

async function fetchFromStrapi(cfg) {
  const baseUrl = (cfg.strapiBaseUrl || '').trim().replace(/\/+$/, '');
  if (!baseUrl.startsWith('http')) {
    throw new Error('Valid Strapi Base URL is required.');
  }

  const headers = { Accept: 'application/json' };
  if (cfg.strapiApiToken) {
    headers.Authorization = `Bearer ${cfg.strapiApiToken.trim()}`;
  }

  const response = await fetch(
    `${baseUrl}/api/portfolio-items?populate=*&pagination[pageSize]=100&sort=updatedAt:desc`,
    { headers }
  );

  if (!response.ok) {
    const errText = await response.text();
    throw new Error(`Strapi HTTP ${response.status}: ${errText.slice(0, 100)}`);
  }

  const json = await response.json();
  return (json.data || [])
    .map((row, idx) => {
      const a = row.attributes || row;
      if (!a.title) return null;
      const techStack = Array.isArray(a.techStack)
        ? a.techStack
        : typeof a.techStackCsv === 'string'
        ? a.techStackCsv.split(',').map((s) => s.trim()).filter(Boolean)
        : [];

      const coverPath =
        a.cover?.url || a.cover?.data?.attributes?.url || '';
      const resolvedCover = coverPath.startsWith('http')
        ? coverPath
        : coverPath.startsWith('/')
        ? `${baseUrl}${coverPath}`
        : '';

      return {
        id: row.documentId || row.id || `strapi-${idx}`,
        cmsEntryId: String(row.documentId || row.id || ''),
        cmsProvider: 'STRAPI',
        title: a.title,
        slug: a.slug || a.title.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
        contentType: (a.contentType || 'BLOG').toUpperCase(),
        categoryId: a.categoryId || 1,
        categoryName: a.categoryName || 'React & Headless CMS',
        summary: a.summary || '',
        markdownBody: a.markdownBody || a.body || '',
        mediaUrl:
          a.mediaSource ||
          resolvedCover ||
          'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80',
        photoCaption: a.photoCaption || '',
        photoLocation: a.photoLocation || '',
        exifCamera: a.exifCamera || '',
        techStack,
        liveDemoUrl: a.liveDemoUrl || '',
        repoUrl: a.repoUrl || '',
        readingTimeMinutes: Number(a.readingTimeMinutes || 5),
        isFeatured: Boolean(a.isFeatured),
        isPublished: true,
        updatedAtEpoch: a.updatedAt ? new Date(a.updatedAt).getTime() : Date.now()
      };
    })
    .filter(Boolean);
}

async function publishToStrapi(item, cfg) {
  const baseUrl = (cfg.strapiBaseUrl || '').trim().replace(/\/+$/, '');
  const apiToken = (cfg.strapiApiToken || '').trim();

  if (!baseUrl.startsWith('http') || !apiToken) {
    throw new Error('Strapi Base URL and API Token are required to publish.');
  }

  const response = await fetch(`${baseUrl}/api/portfolio-items`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${apiToken}`,
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

  if (!response.ok) {
    const errText = await response.text();
    throw new Error(`Strapi POST ${response.status}: ${errText.slice(0, 120)}`);
  }

  const created = await response.json();
  return {
    entryId: String(created.data?.documentId || created.data?.id || `strapi-${Date.now()}`),
    provider: 'STRAPI'
  };
}
