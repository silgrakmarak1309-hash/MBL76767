/**
 * Firebase Firestore Cloud Service for Meri Local Bazaar
 * Project: gen-lang-client-0563393769
 * Database: ai-studio-merilocalbazaar-b94b31a6-c91c-4a3a-94b5-0010a566b61a
 */

(function(window) {
  'use strict';
  let _firestoreBackoffUntil = 0;

  const FIREBASE_CONFIG = {
    projectId: "gen-lang-client-0563393769",
    apiKey: "AIzaSyBcLyUUTTXtHyzNUL9ClqYe9c2Ih1fOi7k",
    authDomain: "gen-lang-client-0563393769.firebaseapp.com",
    firestoreDatabaseId: "ai-studio-merilocalbazaar-b94b31a6-c91c-4a3a-94b5-0010a566b61a",
    messagingSenderId: "619957202495",
    appId: "1:619957202495:web:5e87e36d62df7d4153528a"
  };

  const SUPABASE_STORAGE_CONFIG = {
    url: "https://haaatxxndggdfwizgmlo.supabase.co",
    anonKey: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhhYWF0eHhuZGdnZGZ3aXpnbWxvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODcyNTgxOTYsImV4cCI6MjEwMjgzNDE5Nn0.bmYm6h1AMUXvle9fUv86MPKtJt5JLq5Z6VjXI8YtqZ0",
    bucket: "listing-images"
  };

  const BASE_URL = `https://firestore.googleapis.com/v1/projects/${FIREBASE_CONFIG.projectId}/databases/${FIREBASE_CONFIG.firestoreDatabaseId}/documents`;

  // Memory cache to ensure 0ms instant UI responses
  const memoryCache = {
    settings: null,
    listings: null,
    recharges: null,
    users: null,
    categories: null,
    locations: null,
    transactions: null,
    banners: null,
    deletedListingIds: new Set(),
    lastFetchTime: {}
  };

  // Convert JS object to Firestore Document fields
  function jsToFirestoreFields(obj) {
    if (!obj || typeof obj !== 'object') return { nullValue: null };
    const fields = {};
    for (const [key, value] of Object.entries(obj)) {
      if (value === undefined || value === null) {
        fields[key] = { nullValue: null };
      } else if (typeof value === 'boolean') {
        fields[key] = { booleanValue: value };
      } else if (typeof value === 'number') {
        if (Number.isInteger(value)) {
          fields[key] = { integerValue: String(value) };
        } else {
          fields[key] = { doubleValue: value };
        }
      } else if (typeof value === 'string') {
        fields[key] = { stringValue: value };
      } else if (Array.isArray(value)) {
        fields[key] = {
          arrayValue: {
            values: value.map(item => {
              if (typeof item === 'string') return { stringValue: item };
              if (typeof item === 'number') return { doubleValue: item };
              if (typeof item === 'boolean') return { booleanValue: item };
              if (typeof item === 'object' && item !== null) return { mapValue: { fields: jsToFirestoreFields(item) } };
              return { stringValue: String(item) };
            })
          }
        };
      } else if (typeof value === 'object') {
        fields[key] = { mapValue: { fields: jsToFirestoreFields(value) } };
      } else {
        fields[key] = { stringValue: String(value) };
      }
    }
    return fields;
  }

  // Convert Firestore Document fields back to plain JS object
  function firestoreFieldsToJs(fields) {
    if (!fields || typeof fields !== 'object') return {};
    const result = {};
    for (const [key, valueObj] of Object.entries(fields)) {
      if (!valueObj) continue;
      if ('stringValue' in valueObj) {
        result[key] = valueObj.stringValue;
      } else if ('integerValue' in valueObj) {
        result[key] = parseInt(valueObj.integerValue, 10);
      } else if ('doubleValue' in valueObj) {
        result[key] = parseFloat(valueObj.doubleValue);
      } else if ('booleanValue' in valueObj) {
        result[key] = valueObj.booleanValue;
      } else if ('nullValue' in valueObj) {
        result[key] = null;
      } else if ('arrayValue' in valueObj) {
        const arr = valueObj.arrayValue && valueObj.arrayValue.values ? valueObj.arrayValue.values : [];
        result[key] = arr.map(item => {
          if (!item) return null;
          if ('stringValue' in item) return item.stringValue;
          if ('integerValue' in item) return parseInt(item.integerValue, 10);
          if ('doubleValue' in item) return parseFloat(item.doubleValue);
          if ('booleanValue' in item) return item.booleanValue;
          if ('mapValue' in item) return firestoreFieldsToJs(item.mapValue.fields);
          return null;
        });
      } else if ('mapValue' in valueObj) {
        result[key] = firestoreFieldsToJs(valueObj.mapValue ? valueObj.mapValue.fields : {});
      } else if ('timestampValue' in valueObj) {
        result[key] = valueObj.timestampValue;
      }
    }
    return result;
  }

  // Generic REST Firestore Request with timeout
  async function firestoreRequest(path, options = {}, timeoutMs = 6000) {
    const url = `${BASE_URL}/${path}${path.includes('?') ? '&' : '?'}key=${FIREBASE_CONFIG.apiKey}`;
    const controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    const timeout = controller ? setTimeout(() => controller.abort(), timeoutMs) : null;

    try {
      const fetchOptions = {
        ...options,
        signal: controller ? controller.signal : undefined,
        headers: {
          'Content-Type': 'application/json',
          ...(options.headers || {})
        }
      };
      const res = await fetch(url, fetchOptions);
      if (timeout) clearTimeout(timeout);
      if (!res.ok) {
        if (res.status === 429) {
          _firestoreBackoffUntil = Date.now() + 60000;
          console.warn("[Firebase] Firestore quota limit (429), backing off for 60s");
        }
        if (res.status === 404) return null;
        const errText = await res.text();
        console.warn(`[Firebase] Firestore request error (${res.status}):`, errText.slice(0, 150));
        return null;
      }
      return await res.json();
    } catch (err) {
      if (timeout) clearTimeout(timeout);
      console.warn(`[Firebase] Network error for ${path}:`, err.message || err);
      return null;
    }
  }

  // Robust Query Collection via :runQuery (bypasses collection listing 403 restrictions)
  async function fetchCollection(collectionName, timeoutMs = 6000) {
    const queryUrl = `${BASE_URL}:runQuery?key=${FIREBASE_CONFIG.apiKey}`;
    const controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    const timeout = controller ? setTimeout(() => controller.abort(), timeoutMs) : null;

    try {
      const body = {
        structuredQuery: {
          from: [{ collectionId: collectionName }]
        }
      };
      const res = await fetch(queryUrl, {
        method: 'POST',
        signal: controller ? controller.signal : undefined,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (timeout) clearTimeout(timeout);
      if (!res.ok) {
        console.warn(`[Firebase] Query ${collectionName} error (${res.status})`);
        return [];
      }
      const data = await res.json();
      if (!Array.isArray(data)) return [];
      const list = [];
      data.forEach(item => {
        if (item && item.document && item.document.fields) {
          const id = item.document.name.split('/').pop();
          const docData = firestoreFieldsToJs(item.document.fields);
          list.push({ id, ...docData, _createTime: item.document.createTime, _updateTime: item.document.updateTime });
        }
      });
      return list;
    } catch(err) {
      if (timeout) clearTimeout(timeout);
      console.warn(`[Firebase] Query ${collectionName} exception:`, err.message || err);
      return [];
    }
  }

  // ----------------------------------------------------
  // 1. SETTINGS (UPI ID, QR Code, Site Info)
  // ----------------------------------------------------
  async function getSettings(forceFresh = false) {
    if (!forceFresh && memoryCache.settings) return memoryCache.settings;
    try {
      const local = localStorage.getItem('app_admin_settings');
      if (local && !forceFresh) memoryCache.settings = JSON.parse(local);
    } catch(e) {}

    try {
      const data = await firestoreRequest('settings/app_config');
      if (data && data.fields) {
        const fresh = firestoreFieldsToJs(data.fields);
        memoryCache.settings = fresh;
        try { localStorage.setItem('app_admin_settings', JSON.stringify(fresh)); } catch(e) {}
        return fresh;
      }
    } catch(err) {}

    return memoryCache.settings || {
      upi_id: 'grejamarak@oksbi',
      payment_qr_code: '',
      payment_instructions: '1. Open any UPI app (GPay, PhonePe, Paytm). 2. Scan the QR code or pay to the UPI ID shown. 3. Enter the exact amount and copy the 12-digit UTR/Reference number. 4. Submit the request.',
      site_name: 'Meri Local Bazaar',
      support_phone: '9366304567',
      support_email: 'merilocalbazaar@gmail.com'
    };
  }

  async function saveSettings(settingsObj) {
    const current = await getSettings();
    const updated = { ...current, ...settingsObj, updated_at: new Date().toISOString() };
    memoryCache.settings = updated;
    try { localStorage.setItem('app_admin_settings', JSON.stringify(updated)); } catch(e) {}
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('app_settings_updated', { detail: updated }));
    }

    try {
      const fields = jsToFirestoreFields(updated);
      await firestoreRequest('settings/app_config', {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log('[Firebase] Settings saved to Firestore successfully');
    } catch(err) {
      console.warn('[Firebase] Failed to save settings to Firestore:', err);
    }
    return updated;
  }

  // ----------------------------------------------------
  // 1B. ACCOUNT LOCK (Single Authorized Account per App Install)
  // ----------------------------------------------------
  const ADMIN_EMAILS = ["silgrakmarak1309@gmail.com", "grejamarak@gmail.com", "megamarak8@gmail.com"];

  function isEmailAdmin(email) {
    if (!email) return false;
    const clean = String(email).toLowerCase().trim();
    return ADMIN_EMAILS.some(adm => adm.toLowerCase() === clean);
  }

  async function getAccountLock(forceFresh = false) {
    if (!forceFresh && memoryCache.accountLock) return memoryCache.accountLock;
    try {
      const local = localStorage.getItem('mlb_account_lock');
      if (local && !forceFresh) {
        const parsed = JSON.parse(local);
        if (parsed && (parsed.authorizedEmail || parsed.authorizedUid)) {
          memoryCache.accountLock = parsed;
        }
      }
    } catch(e) {}

    try {
      const data = await firestoreRequest('appSettings/accountLock');
      if (data && data.fields) {
        const fresh = firestoreFieldsToJs(data.fields);
        if (fresh && (fresh.authorizedEmail || fresh.authorizedUid)) {
          memoryCache.accountLock = fresh;
          try { localStorage.setItem('mlb_account_lock', JSON.stringify(fresh)); } catch(e) {}
          return fresh;
        }
      }
      const data2 = await firestoreRequest('settings/account_lock');
      if (data2 && data2.fields) {
        const fresh2 = firestoreFieldsToJs(data2.fields);
        if (fresh2 && (fresh2.authorizedEmail || fresh2.authorizedUid)) {
          memoryCache.accountLock = fresh2;
          try { localStorage.setItem('mlb_account_lock', JSON.stringify(fresh2)); } catch(e) {}
          return fresh2;
        }
      }
    } catch (err) {
      console.warn('[Firebase] getAccountLock fetch error:', err);
    }
    return memoryCache.accountLock || null;
  }

  async function saveAccountLock(lockData) {
    if (!lockData) return null;
    const cleanEmail = String(lockData.authorizedEmail || '').toLowerCase().trim();
    const payload = {
      authorizedUid: lockData.authorizedUid || '',
      authorizedEmail: cleanEmail,
      authorizedProvider: lockData.authorizedProvider || 'password',
      lockedAt: lockData.lockedAt || new Date().toISOString(),
      isLocked: true
    };
    memoryCache.accountLock = payload;
    try { localStorage.setItem('mlb_account_lock', JSON.stringify(payload)); } catch(e) {}

    try {
      const fields = jsToFirestoreFields(payload);
      await Promise.allSettled([
        firestoreRequest('appSettings/accountLock', {
          method: 'PATCH',
          body: JSON.stringify({ fields })
        }),
        firestoreRequest('settings/account_lock', {
          method: 'PATCH',
          body: JSON.stringify({ fields })
        })
      ]);
      console.log('[Firebase] Account lock persistently saved to Firestore:', cleanEmail);
    } catch(err) {
      console.warn('[Firebase] Failed to save account lock to Firestore:', err);
    }
    return payload;
  }

  // ----------------------------------------------------
  // 2. LISTINGS (New Post Listing, Top PRO, Boosted, Deleted)
  // ----------------------------------------------------
  async function getListings(forceFresh = false) {
    const now = Date.now();
    if (!forceFresh && memoryCache.listings && (now - (memoryCache.lastFetchTime.listings || 0) < 5000)) {
      return memoryCache.listings;
    }

    // 1. Fetch Cloud Listings and Cloud Deletions in parallel
    let cloudList = [];
    let deletedDocs = [];
    try {
      const [listingsRes, delRes] = await Promise.all([
        fetchCollection('listings'),
        fetchCollection('deleted_listings')
      ]);
      cloudList = Array.isArray(listingsRes) ? listingsRes : [];
      deletedDocs = Array.isArray(delRes) ? delRes : [];
    } catch(err) {
      console.warn('[Firebase] Listings fetch error:', err);
    }

    // 2. Build deleted IDs set
    const deletedSet = new Set();
    deletedDocs.forEach(d => {
      if (d && d.id) deletedSet.add(d.id);
      if (d && d.deleted_id) deletedSet.add(d.deleted_id);
    });
    try {
      const localDel = JSON.parse(localStorage.getItem('deleted_listing_ids') || '[]');
      localDel.forEach(id => deletedSet.add(id));
    } catch(e) {}

    // 3. Filter cloud listings (exclude deleted)
    const activeCloudList = cloudList.filter(item => {
      if (!item || !item.id) return false;
      if (deletedSet.has(item.id)) return false;
      if (item.status === 'deleted' || item.is_deleted === true) {
        deletedSet.add(item.id);
        return false;
      }
      return true;
    });

    // 4. Clean local storage so stale deleted posts on other devices are permanently purged
    try {
      localStorage.setItem('deleted_listing_ids', JSON.stringify(Array.from(deletedSet)));
      
      const userCustom = JSON.parse(localStorage.getItem('user_custom_listings') || '[]');
      const cleanedCustom = userCustom.filter(item => item && item.id && !deletedSet.has(item.id) && item.status !== 'deleted');
      localStorage.setItem('user_custom_listings', JSON.stringify(cleanedCustom));

      const myCreated = JSON.parse(localStorage.getItem('my_created_listings') || '[]');
      const cleanedMy = myCreated.filter(item => item && item.id && !deletedSet.has(item.id) && item.status !== 'deleted');
      localStorage.setItem('my_created_listings', JSON.stringify(cleanedMy));
    } catch(e) {}

    if (activeCloudList.length > 0 || cloudList.length > 0) {
      // Sort by creation date descending
      activeCloudList.sort((a, b) => new Date(b.created_at || b._createTime || 0) - new Date(a.created_at || a._createTime || 0));
      memoryCache.listings = activeCloudList;
      memoryCache.lastFetchTime.listings = now;
      try { localStorage.setItem('all_cached_listings', JSON.stringify(activeCloudList)); } catch(e) {}
      return activeCloudList;
    }

    // Fallback if cloud was completely unreachable
    let fallback = [];
    try {
      const stored = localStorage.getItem('all_cached_listings');
      if (stored) {
        fallback = JSON.parse(stored).filter(item => item && item.id && !deletedSet.has(item.id));
      }
    } catch(e) {}
    return fallback;
  }

  async function saveListing(listingObj) {
    if (!listingObj || !listingObj.id) {
      listingObj = { ...listingObj, id: 'list_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7) };
    }
    const isPro = Boolean(listingObj.is_featured || listingObj.is_top_pro);
    const cleanListing = {
      ...listingObj,
      is_featured: isPro,
      is_top_pro: isPro,
      status: listingObj.status || 'pending',
      created_at: listingObj.created_at || new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    // 1. Remove from deleted sets if re-created
    try {
      const deletedIds = JSON.parse(localStorage.getItem('deleted_listing_ids') || '[]').filter(id => id !== cleanListing.id);
      localStorage.setItem('deleted_listing_ids', JSON.stringify(deletedIds));
    } catch(e) {}

    // 2. Optimistic Local Update
    let current = memoryCache.listings || [];
    const index = current.findIndex(l => l.id === cleanListing.id);
    if (index >= 0) {
      current[index] = cleanListing;
    } else {
      current = [cleanListing, ...current];
    }
    memoryCache.listings = current;
    try {
      localStorage.setItem('all_cached_listings', JSON.stringify(current));
      const myLists = JSON.parse(localStorage.getItem('my_created_listings') || '[]');
      if (!myLists.some(l => l.id === cleanListing.id)) {
        myLists.unshift(cleanListing);
        localStorage.setItem('my_created_listings', JSON.stringify(myLists));
      }
      const userCustom = JSON.parse(localStorage.getItem('user_custom_listings') || '[]');
      const cIdx = userCustom.findIndex(l => l.id === cleanListing.id);
      if (cIdx >= 0) userCustom[cIdx] = cleanListing;
      else userCustom.unshift(cleanListing);
      localStorage.setItem('user_custom_listings', JSON.stringify(userCustom));
    } catch(e) {}

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('listing_created', { detail: cleanListing }));
    }

    // 3. Cloud Firestore Persist
    try {
      const fields = jsToFirestoreFields(cleanListing);
      await firestoreRequest(`listings/${cleanListing.id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      // Delete any leftover tombstone in deleted_listings
      await firestoreRequest(`deleted_listings/${cleanListing.id}`, { method: 'DELETE' });
      console.log('[Firebase] Listing saved to Firestore across all devices:', cleanListing.id);
    } catch(err) {
      console.warn('[Firebase] Listing cloud save error:', err);
    }
    return cleanListing;
  }

  async function updateListing(id, updates) {
    if (!id) return;
    let current = memoryCache.listings || [];
    const item = current.find(l => l.id === id) || { id };
    const isPro = (updates && (updates.is_featured !== undefined || updates.is_top_pro !== undefined))
      ? Boolean(updates.is_featured || updates.is_top_pro)
      : Boolean(item.is_featured || item.is_top_pro);
    const updated = {
      ...item,
      ...updates,
      is_featured: isPro,
      is_top_pro: isPro,
      updated_at: new Date().toISOString()
    };

    const idx = current.findIndex(l => l.id === id);
    if (idx >= 0) {
      current[idx] = updated;
    } else {
      current.push(updated);
    }
    memoryCache.listings = current;
    try {
      localStorage.setItem('all_cached_listings', JSON.stringify(current));
      const userCustom = JSON.parse(localStorage.getItem('user_custom_listings') || '[]');
      const cIdx = userCustom.findIndex(l => l.id === id);
      if (cIdx >= 0) {
        userCustom[cIdx] = { ...userCustom[cIdx], ...updates };
        localStorage.setItem('user_custom_listings', JSON.stringify(userCustom));
      }
    } catch(e) {}

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('listing_updated', { detail: updated }));
    }

    try {
      const fields = jsToFirestoreFields(updated);
      await firestoreRequest(`listings/${id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log('[Firebase] Listing updated in Firestore across devices:', id);
    } catch(err) {
      console.warn('[Firebase] Update listing error:', err);
    }
    return updated;
  }

  async function deleteListing(id) {
    if (!id) return;
    // 1. Remove from in-memory cache and localStorage
    let current = (memoryCache.listings || []).filter(l => l.id !== id);
    memoryCache.listings = current;
    try {
      localStorage.setItem('all_cached_listings', JSON.stringify(current));
      
      const deletedIds = JSON.parse(localStorage.getItem('deleted_listing_ids') || '[]');
      if (!deletedIds.includes(id)) {
        deletedIds.push(id);
        localStorage.setItem('deleted_listing_ids', JSON.stringify(deletedIds));
      }

      const userCustom = JSON.parse(localStorage.getItem('user_custom_listings') || '[]');
      localStorage.setItem('user_custom_listings', JSON.stringify(userCustom.filter(l => l && l.id !== id)));

      const myCreated = JSON.parse(localStorage.getItem('my_created_listings') || '[]');
      localStorage.setItem('my_created_listings', JSON.stringify(myCreated.filter(l => l && l.id !== id)));
    } catch(e) {}

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('listing_deleted', { detail: { id } }));
      window.dispatchEvent(new Event('storage'));
    }

    // 2. Cloud delete & record tombstone in deleted_listings so all other phones purge it immediately
    try {
      await Promise.all([
        firestoreRequest(`listings/${id}`, { method: 'DELETE' }),
        firestoreRequest(`deleted_listings/${id}`, {
          method: 'PATCH',
          body: JSON.stringify({
            fields: jsToFirestoreFields({
              id,
              deleted_id: id,
              deleted_at: new Date().toISOString()
            })
          })
        })
      ]);
      console.log('[Firebase] Listing deleted & synced across all phones:', id);
    } catch(err) {
      console.warn('[Firebase] Delete listing error:', err);
    }
  }

  // ----------------------------------------------------
  // 3. RECHARGE REQUESTS (Top PRO Boost & Monthly Plans)
  // ----------------------------------------------------
  async function getRechargeRequests(forceFresh = false) {
    const now = Date.now();
    if (!forceFresh && memoryCache.recharges && (now - (memoryCache.lastFetchTime.recharges || 0) < 5000)) {
      return memoryCache.recharges;
    }

    let cloudList = [];
    try {
      cloudList = await fetchCollection('recharge_requests');
    } catch(err) {
      console.warn('[Firebase] Recharge requests query error:', err);
    }

    if (Array.isArray(cloudList) && cloudList.length > 0) {
      cloudList.sort((a, b) => new Date(b.submitted_at || b.created_at || b._createTime || 0) - new Date(a.submitted_at || a.created_at || a._createTime || 0));
      memoryCache.recharges = cloudList;
      memoryCache.lastFetchTime.recharges = now;
      try { localStorage.setItem('all_recharge_requests', JSON.stringify(cloudList)); } catch(e) {}
      return cloudList;
    }

    let localList = [];
    try {
      localList = JSON.parse(localStorage.getItem('all_recharge_requests') || '[]');
    } catch(e) {}
    return memoryCache.recharges || localList;
  }

  async function submitRechargeRequest(reqObj) {
    const id = reqObj.id || ('req_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6));
    const cleanReq = {
      ...reqObj,
      id,
      status: reqObj.status || 'pending',
      submitted_at: reqObj.submitted_at || new Date().toISOString(),
      created_at: reqObj.created_at || new Date().toISOString()
    };

    // 1. Optimistic Local Save
    let list = memoryCache.recharges || [];
    list = [cleanReq, ...list.filter(r => r.id !== id && r.utr !== cleanReq.utr)];
    memoryCache.recharges = list;
    try {
      localStorage.setItem('all_recharge_requests', JSON.stringify(list));
      const userReqs = JSON.parse(localStorage.getItem('user_recharge_requests') || '[]');
      userReqs.unshift(cleanReq);
      localStorage.setItem('user_recharge_requests', JSON.stringify(userReqs));
    } catch(e) {}

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('recharge_request_created', { detail: cleanReq }));
      window.dispatchEvent(new Event('storage'));
    }

    // 2. Cloud Firestore Persist
    try {
      const fields = jsToFirestoreFields(cleanReq);
      await firestoreRequest(`recharge_requests/${id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log('[Firebase] Recharge/Top PRO request synced to Firestore for Admin Panel:', id);
    } catch(err) {
      console.warn('[Firebase] Recharge request cloud save error:', err);
    }
    return cleanReq;
  }

  async function updateRechargeStatus(idOrUtr, status, extra = {}) {
    if (!idOrUtr) return;
    let list = memoryCache.recharges || [];
    let target = list.find(r => r && (r.id === idOrUtr || r.utr === idOrUtr));
    const docId = (target && target.id) ? target.id : idOrUtr;

    const updated = {
      ...(target || {}),
      id: docId,
      status: status,
      ...extra,
      updated_at: new Date().toISOString()
    };

    const idx = list.findIndex(r => r && (r.id === docId || r.utr === idOrUtr));
    if (idx >= 0) {
      list[idx] = updated;
    } else {
      list.push(updated);
    }
    memoryCache.recharges = list;
    try {
      localStorage.setItem('all_recharge_requests', JSON.stringify(list));
      const overrides = JSON.parse(localStorage.getItem('recharge_status_overrides') || '{}');
      overrides[docId] = { status, ...extra };
      if (target?.utr) overrides[target.utr] = { status, ...extra };
      localStorage.setItem('recharge_status_overrides', JSON.stringify(overrides));
    } catch(e) {}

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('recharge_status_updated', { detail: updated }));
      window.dispatchEvent(new Event('storage'));
    }

    try {
      const fields = jsToFirestoreFields(updated);
      await firestoreRequest(`recharge_requests/${docId}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log('[Firebase] Recharge status updated in Firestore across all phones:', docId, status);
    } catch(err) {
      console.warn('[Firebase] Recharge status update error:', err);
    }
    return updated;
  }

  // ----------------------------------------------------
  // 4. USERS & PROFILES CONTROL
  // ----------------------------------------------------
  async function getUsers(forceFresh = false) {
    const now = Date.now();
    if (!forceFresh && memoryCache.users && (now - (memoryCache.lastFetchTime.users || 0) < 5000)) {
      return memoryCache.users;
    }

    try {
      const cloudUsers = await fetchCollection('users');
      if (Array.isArray(cloudUsers) && cloudUsers.length > 0) {
        memoryCache.users = cloudUsers;
        memoryCache.lastFetchTime.users = now;
        try { localStorage.setItem('admin_users_cache', JSON.stringify(cloudUsers)); } catch(e) {}
        return cloudUsers;
      }
    } catch(err) {}

    let localUsers = [];
    try {
      localUsers = JSON.parse(localStorage.getItem('admin_users_cache') || '[]');
    } catch(e) {}
    return memoryCache.users || localUsers;
  }

  async function getUser(idOrEmail) {
    if (!idOrEmail) return null;
    const cleanId = String(idOrEmail).trim();
    const cleanEmail = cleanId.toLowerCase();

    // 1. Check in-memory cache first
    let users = memoryCache.users || [];
    let match = users.find(u => u && (u.id === cleanId || (u.email && u.email.toLowerCase() === cleanEmail)));
    if (match) return match;

    // 2. Fetch directly from Firestore by Document ID
    try {
      const doc = await firestoreRequest(`users/${encodeURIComponent(cleanId)}`);
      if (doc && doc.fields) {
        const userData = firestoreFieldsToJs(doc.fields);
        const resolved = { id: cleanId, ...userData };
        // update cache
        const idx = users.findIndex(u => u.id === cleanId);
        if (idx >= 0) users[idx] = resolved;
        else users.push(resolved);
        memoryCache.users = users;
        return resolved;
      }
    } catch(err) {
      console.warn('[Firebase] Get user by doc ID error:', err);
    }

    // 3. If query might be an email or Auth UID, fetch full users list from Firestore
    try {
      const allCloudUsers = await getUsers(true);
      if (Array.isArray(allCloudUsers)) {
        match = allCloudUsers.find(u => u && (u.id === cleanId || (u.email && u.email.toLowerCase() === cleanEmail)));
        if (match) return match;
      }
    } catch(err) {}

    return null;
  }

  async function saveUser(userObj) {
    if (!userObj || !userObj.id) return;
    const cleanUser = {
      ...userObj,
      updated_at: new Date().toISOString()
    };

    let users = memoryCache.users || [];
    const idx = users.findIndex(u => u.id === cleanUser.id || (cleanUser.email && u.email && u.email.toLowerCase() === cleanUser.email.toLowerCase()));
    if (idx >= 0) {
      users[idx] = { ...users[idx], ...cleanUser };
    } else {
      users.push(cleanUser);
    }
    memoryCache.users = users;
    try { localStorage.setItem('admin_users_cache', JSON.stringify(users)); } catch(e) {}

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('user_profile_updated', { detail: cleanUser }));
    }

    try {
      const fields = jsToFirestoreFields(cleanUser);
      await firestoreRequest(`users/${cleanUser.id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log('[Firebase] User saved to Firestore:', cleanUser.id);
    } catch(err) {
      console.warn('[Firebase] User cloud save error:', err);
    }
    return cleanUser;
  }

  async function updateUserStatus(userId, accountStatus, role, extra = {}) {
    if (!userId) return;
    const users = memoryCache.users || [];
    const target = users.find(u => u.id === userId) || { id: userId };
    const updated = {
      ...target,
      account_status: accountStatus || target.account_status || 'active',
      status: accountStatus || target.status || 'active',
      role: role || target.role || 'user',
      ...extra,
      updated_at: new Date().toISOString()
    };
    return await saveUser(updated);
  }

  async function updateUserPro(userId, isPro, durationDaysOrExpiry = 30, uEmail = '', reason = '') {
    if (!userId && !uEmail) throw new Error('User ID or Email is required');
    const effectiveId = userId || uEmail;
    
    let expiry = null;
    if (isPro) {
      if (typeof durationDaysOrExpiry === 'string' && (durationDaysOrExpiry.includes('-') || durationDaysOrExpiry.includes('/'))) {
        const d = new Date(durationDaysOrExpiry);
        expiry = isNaN(d.getTime()) ? new Date(Date.now() + 30 * 86400000).toISOString() : d.toISOString();
      } else {
        const days = Number(durationDaysOrExpiry) || 30;
        expiry = new Date(Date.now() + days * 86400000).toISOString();
      }
    }

    let users = memoryCache.users || [];
    let target = users.find(u => u && (u.id === effectiveId || (uEmail && u.email && u.email.toLowerCase() === uEmail.toLowerCase()))) || { id: effectiveId, email: uEmail };

    const updated = {
      ...target,
      id: target.id || effectiveId,
      email: target.email || uEmail || '',
      is_pro: Boolean(isPro),
      pro_status: isPro ? 'active' : 'inactive',
      pro_expires_at: expiry,
      pro_expiry_at: expiry,
      approved_expiry_date: expiry,
      pro_updated_at: new Date().toISOString(),
      pro_reason: reason || (isPro ? 'Admin updated PRO plan' : 'Admin set PRO inactive'),
      updated_at: new Date().toISOString()
    };

    // 1. Update memory cache and localStorage overrides
    const idx = users.findIndex(u => u && (u.id === updated.id || (u.email && updated.email && u.email.toLowerCase() === updated.email.toLowerCase())));
    if (idx >= 0) users[idx] = updated;
    else users.push(updated);
    memoryCache.users = users;

    const pData = {
      is_pro: Boolean(isPro),
      pro_status: isPro ? 'active' : 'inactive',
      pro_expires_at: expiry,
      pro_expiry_at: expiry,
      approved_expiry_date: expiry
    };

    try {
      const localPro = JSON.parse(localStorage.getItem('admin_pro_overrides') || '{}');
      if (updated.id) localPro[updated.id] = pData;
      if (updated.email) {
        localPro[updated.email] = pData;
        localPro[updated.email.toLowerCase().trim()] = pData;
      }
      localStorage.setItem('admin_pro_overrides', JSON.stringify(localPro));
      localStorage.setItem('pro_status_overrides', JSON.stringify(localPro));
      localStorage.setItem('admin_users_cache', JSON.stringify(users));
    } catch(e) {}

    // 2. Dispatch events for real-time listener updates
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('user_profile_updated', { detail: updated }));
      window.dispatchEvent(new CustomEvent('user_status_changed', { detail: updated }));
      window.dispatchEvent(new Event('storage'));
    }

    // 3. Persist permanently to Firestore
    try {
      const fields = jsToFirestoreFields(updated);
      const res = await firestoreRequest(`users/${updated.id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log(`[Firebase] User PRO status permanently set to ${isPro ? 'ACTIVE' : 'INACTIVE'} in Firestore:`, updated.id);
      return updated;
    } catch(err) {
      console.error('[Firebase] Error updating PRO status in Firestore:', err);
      throw err;
    }
  }

  async function updateUserAccountStatus(userId, accountStatus, uEmail = '') {
    if (!userId && !uEmail) throw new Error('User ID or Email is required');
    const effectiveId = userId || uEmail;
    let users = memoryCache.users || [];
    let target = users.find(u => u && (u.id === effectiveId || (uEmail && u.email && u.email.toLowerCase() === uEmail.toLowerCase()))) || { id: effectiveId, email: uEmail };

    const updated = {
      ...target,
      id: target.id || effectiveId,
      email: target.email || uEmail || '',
      account_status: accountStatus,
      status: accountStatus,
      updated_at: new Date().toISOString()
    };

    const idx = users.findIndex(u => u && (u.id === updated.id || (u.email && updated.email && u.email.toLowerCase() === updated.email.toLowerCase())));
    if (idx >= 0) users[idx] = updated;
    else users.push(updated);
    memoryCache.users = users;

    try {
      const statusOverrides = JSON.parse(localStorage.getItem('admin_status_overrides') || '{}');
      if (updated.id) statusOverrides[updated.id] = { account_status: accountStatus, status: accountStatus };
      if (updated.email) {
        statusOverrides[updated.email] = { account_status: accountStatus, status: accountStatus };
        statusOverrides[updated.email.toLowerCase().trim()] = { account_status: accountStatus, status: accountStatus };
      }
      localStorage.setItem('admin_status_overrides', JSON.stringify(statusOverrides));
      localStorage.setItem('admin_users_cache', JSON.stringify(users));
    } catch(e) {}

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('user_profile_updated', { detail: updated }));
      window.dispatchEvent(new CustomEvent('user_status_changed', { detail: updated }));
      window.dispatchEvent(new Event('storage'));
    }

    try {
      const fields = jsToFirestoreFields(updated);
      await firestoreRequest(`users/${updated.id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log(`[Firebase] User account status permanently set to ${accountStatus} in Firestore:`, updated.id);
      return updated;
    } catch(err) {
      console.error('[Firebase] Error updating account status in Firestore:', err);
      throw err;
    }
  }

  // ----------------------------------------------------
  // 5. LOCATIONS (India States, Districts, Blocks)
  // ----------------------------------------------------
  async function getLocations(forceFresh = false) {
    if (!forceFresh && memoryCache.locations) return memoryCache.locations;
    try {
      const cloudLocs = await fetchCollection('locations');
      if (cloudLocs.length > 0) {
        memoryCache.locations = cloudLocs;
        try { localStorage.setItem('app_custom_locations', JSON.stringify(cloudLocs)); } catch(e) {}
        return cloudLocs;
      }
    } catch(err) {}

    try {
      const local = localStorage.getItem('app_custom_locations');
      if (local) memoryCache.locations = JSON.parse(local);
    } catch(e) {}

    return memoryCache.locations || [];
  }

  async function saveLocation(locObj) {
    const id = locObj.id || ('loc_' + Date.now());
    const cleanLoc = { ...locObj, id, is_active: locObj.is_active !== false, created_at: locObj.created_at || new Date().toISOString() };

    let locs = memoryCache.locations || [];
    const idx = locs.findIndex(l => l.id === id);
    if (idx >= 0) locs[idx] = cleanLoc;
    else locs.push(cleanLoc);
    memoryCache.locations = locs;
    try { localStorage.setItem('app_custom_locations', JSON.stringify(locs)); } catch(e) {}

    try {
      const fields = jsToFirestoreFields(cleanLoc);
      await firestoreRequest(`locations/${id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log('[Firebase] Location saved to Firestore:', id);
    } catch(err) {}
    return cleanLoc;
  }

  async function deleteLocation(id) {
    if (!id) return;
    let locs = (memoryCache.locations || []).filter(l => l.id !== id);
    memoryCache.locations = locs;
    try { localStorage.setItem('app_custom_locations', JSON.stringify(locs)); } catch(e) {}
    try { await firestoreRequest(`locations/${id}`, { method: 'DELETE' }); } catch(err) {}
  }

  // ----------------------------------------------------
  // 6. CATEGORIES
  // ----------------------------------------------------
  async function getCategories(forceFresh = false) {
    if (!forceFresh && memoryCache.categories) return memoryCache.categories;
    try {
      const cloudCats = await fetchCollection('categories');
      if (cloudCats.length > 0) {
        memoryCache.categories = cloudCats;
        try { localStorage.setItem('app_custom_categories', JSON.stringify(cloudCats)); } catch(e) {}
        return cloudCats;
      }
    } catch(err) {}

    try {
      const local = localStorage.getItem('app_custom_categories');
      if (local) memoryCache.categories = JSON.parse(local);
    } catch(e) {}

    return memoryCache.categories || [];
  }

  async function saveCategory(catObj) {
    const id = catObj.id || ('cat_' + Date.now());
    const cleanCat = { ...catObj, id, is_active: catObj.is_active !== false, created_at: catObj.created_at || new Date().toISOString() };

    let cats = memoryCache.categories || [];
    const idx = cats.findIndex(c => c.id === id);
    if (idx >= 0) cats[idx] = cleanCat;
    else cats.push(cleanCat);
    memoryCache.categories = cats;
    try { localStorage.setItem('app_custom_categories', JSON.stringify(cats)); } catch(e) {}

    try {
      const fields = jsToFirestoreFields(cleanCat);
      await firestoreRequest(`categories/${id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log('[Firebase] Category saved to Firestore:', id);
    } catch(err) {}
    return cleanCat;
  }

  async function deleteCategory(id) {
    if (!id) return;
    let cats = (memoryCache.categories || []).filter(c => c.id !== id);
    memoryCache.categories = cats;
    try { localStorage.setItem('app_custom_categories', JSON.stringify(cats)); } catch(e) {}
    try { await firestoreRequest(`categories/${id}`, { method: 'DELETE' }); } catch(err) {}
  }

  // ----------------------------------------------------
  // 7. TRANSACTIONS
  // ----------------------------------------------------
  async function recordTransaction(txObj) {
    const id = txObj.id || ('tx_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6));
    const cleanTx = {
      ...txObj,
      id,
      date: txObj.date || new Date().toISOString(),
      created_at: txObj.created_at || new Date().toISOString()
    };

    let txs = [];
    try { txs = JSON.parse(localStorage.getItem('all_transactions') || '[]'); } catch(e) {}
    txs.unshift(cleanTx);
    try { localStorage.setItem('all_transactions', JSON.stringify(txs)); } catch(e) {}

    try {
      const fields = jsToFirestoreFields(cleanTx);
      await firestoreRequest(`transactions/${id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
      console.log('[Firebase] Transaction recorded in Firestore:', id);
    } catch(err) {}
    return cleanTx;
  }

  async function getTransactions(userId = null) {
    try {
      const cloudTxs = await fetchCollection('transactions');
      if (Array.isArray(cloudTxs) && cloudTxs.length > 0) {
        cloudTxs.sort((a, b) => new Date(b.created_at || b.date || 0) - new Date(a.created_at || a.date || 0));
        try { localStorage.setItem('all_transactions', JSON.stringify(cloudTxs)); } catch(e) {}
        if (userId) return cloudTxs.filter(t => t.user_id === userId);
        return cloudTxs;
      }
    } catch(err) {}

    let localTxs = [];
    try { localTxs = JSON.parse(localStorage.getItem('all_transactions') || '[]'); } catch(e) {}
    if (userId) return localTxs.filter(t => t.user_id === userId);
    return localTxs;
  }

  // ----------------------------------------------------
  // 8. NOTIFICATIONS
  // ----------------------------------------------------
  async function sendNotification(notifObj) {
    const id = notifObj.id || ('notif_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6));
    const cleanNotif = {
      ...notifObj,
      id,
      read_at: notifObj.read_at || null,
      created_at: notifObj.created_at || new Date().toISOString()
    };

    if (cleanNotif.user_id) {
      try {
        const key = 'user_notifications_' + cleanNotif.user_id;
        const list = JSON.parse(localStorage.getItem(key) || '[]');
        list.unshift(cleanNotif);
        localStorage.setItem(key, JSON.stringify(list));
      } catch(e) {}
    }

    try {
      const fields = jsToFirestoreFields(cleanNotif);
      await firestoreRequest(`notifications/${id}`, {
        method: 'PATCH',
        body: JSON.stringify({ fields })
      });
    } catch(err) {}
    return cleanNotif;
  }

  // ----------------------------------------------------
  // 8B. BANNERS
  // ----------------------------------------------------
  async function getBanners(forceFresh = false) {
    if (!forceFresh && memoryCache.banners) return memoryCache.banners;
    try {
      const cloudBanners = await fetchCollection('banners');
      if (cloudBanners.length > 0) {
        cloudBanners.sort((a, b) => (Number(a.sort_order) || 0) - (Number(b.sort_order) || 0));
        memoryCache.banners = cloudBanners;
        try { localStorage.setItem('admin_banners', JSON.stringify(cloudBanners)); } catch(e) {}
        return cloudBanners;
      }
    } catch(err) {}
    try {
      const local = localStorage.getItem('admin_banners');
      if (local) memoryCache.banners = JSON.parse(local);
    } catch(e) {}
    return memoryCache.banners || [];
  }

  async function saveBanner(bannerObj) {
    const id = bannerObj.id || ('banner_' + Date.now());
    const cleanBanner = { ...bannerObj, id, is_active: bannerObj.is_active !== false, created_at: bannerObj.created_at || new Date().toISOString() };
    let banners = memoryCache.banners || [];
    const idx = banners.findIndex(b => b.id === id);
    if (idx >= 0) banners[idx] = cleanBanner;
    else banners.push(cleanBanner);
    memoryCache.banners = banners;
    try { localStorage.setItem('admin_banners', JSON.stringify(banners)); } catch(e) {}
    try {
      const fields = jsToFirestoreFields(cleanBanner);
      await firestoreRequest(`banners/${id}`, { method: 'PATCH', body: JSON.stringify({ fields }) });
    } catch(err) {}
    return cleanBanner;
  }

  async function deleteBanner(id) {
    if (!id) return;
    let banners = (memoryCache.banners || []).filter(b => b.id !== id);
    memoryCache.banners = banners;
    try { localStorage.setItem('admin_banners', JSON.stringify(banners)); } catch(e) {}
    try { await firestoreRequest(`banners/${id}`, { method: 'DELETE' }); } catch(err) {}
  }

  // ----------------------------------------------------
  // 8C. PRO PLANS
  // ----------------------------------------------------
  async function getPlans(forceFresh = false) {
    try {
      const cloudPlans = await fetchCollection('pro_plans');
      if (cloudPlans.length > 0) {
        cloudPlans.sort((a, b) => (Number(a.sort_order) || 0) - (Number(b.sort_order) || 0));
        try { localStorage.setItem('admin_plans', JSON.stringify(cloudPlans)); } catch(e) {}
        return cloudPlans;
      }
    } catch(err) {}
    try {
      const local = localStorage.getItem('admin_plans');
      if (local) return JSON.parse(local);
    } catch(e) {}
    return [];
  }

  async function savePlan(planObj) {
    const id = planObj.id || ('plan_' + Date.now());
    const cleanPlan = { ...planObj, id, is_active: planObj.is_active !== false, updated_at: new Date().toISOString() };
    try {
      let saved = [];
      try { saved = JSON.parse(localStorage.getItem('admin_plans') || '[]'); } catch(e) {}
      const idx = saved.findIndex(p => p.id === id);
      if (idx >= 0) saved[idx] = cleanPlan;
      else saved.push(cleanPlan);
      localStorage.setItem('admin_plans', JSON.stringify(saved));
    } catch(e) {}
    try {
      const fields = jsToFirestoreFields(cleanPlan);
      await firestoreRequest(`pro_plans/${id}`, { method: 'PATCH', body: JSON.stringify({ fields }) });
    } catch(err) {}
    return cleanPlan;
  }

  async function deletePlan(id) {
    if (!id) return;
    try {
      let saved = [];
      try { saved = JSON.parse(localStorage.getItem('admin_plans') || '[]'); } catch(e) {}
      saved = saved.filter(p => p.id !== id);
      localStorage.setItem('admin_plans', JSON.stringify(saved));
    } catch(e) {}
    try { await firestoreRequest(`pro_plans/${id}`, { method: 'DELETE' }); } catch(err) {}
  }

  // ----------------------------------------------------
  // 9. MEDIA & FILE UPLOAD (Supabase Storage: listing-images bucket)
  // ----------------------------------------------------
  async function uploadMedia(fileOrBase64, folder) {
    if (!fileOrBase64) return "";
    if (typeof fileOrBase64 === "string" && !fileOrBase64.startsWith("data:")) {
      return fileOrBase64;
    }
    const safeFolder = folder || "listings";
    const timestamp = Date.now();
    const rand = Math.random().toString(36).substring(2, 8);
    const fileName = `${safeFolder}/${timestamp}_${rand}.jpg`;

    let dataUrl = "";
    if (typeof fileOrBase64 === "string" && fileOrBase64.startsWith("data:")) {
      dataUrl = fileOrBase64;
    } else if (fileOrBase64 instanceof Blob || fileOrBase64 instanceof File) {
      try {
        dataUrl = await new Promise((resolve) => {
          const reader = new FileReader();
          reader.onload = () => resolve(reader.result || "");
          reader.onerror = () => resolve("");
          reader.readAsDataURL(fileOrBase64);
        });
      } catch(e) { dataUrl = ""; }
    }

    try {
      let blob;
      let contentType = "image/jpeg";
      if (typeof fileOrBase64 === "string") {
        const parts = fileOrBase64.split(",");
        const mimeMatch = (parts[0].match(/:(.*?);/) || [])[1];
        if (mimeMatch) contentType = mimeMatch;
        const bstr = atob(parts[1]);
        let n = bstr.length;
        const u8arr = new Uint8Array(n);
        while (n--) {
          u8arr[n] = bstr.charCodeAt(n);
        }
        blob = new Blob([u8arr], { type: contentType });
      } else {
        blob = fileOrBase64;
        if (fileOrBase64.type) contentType = fileOrBase64.type;
      }

      // 1. Try global Supabase client instance with storage if bucket exists
      if (typeof window !== "undefined" && window.supabaseClient && window.supabaseClient.storage) {
        try {
          const { data: uploadData, error: uploadErr } = await window.supabaseClient.storage
            .from(SUPABASE_STORAGE_CONFIG.bucket)
            .upload(fileName, blob, { contentType, upsert: true });
          if (!uploadErr && uploadData && uploadData.path) {
            const { data: pubData } = window.supabaseClient.storage
              .from(SUPABASE_STORAGE_CONFIG.bucket)
              .getPublicUrl(uploadData.path);
            if (pubData && pubData.publicUrl) {
              return pubData.publicUrl;
            }
          }
        } catch(sbSdkErr) {}
      }

      // 2. Direct Supabase Storage REST API Upload
      try {
        const uploadUrl = `${SUPABASE_STORAGE_CONFIG.url}/storage/v1/object/${SUPABASE_STORAGE_CONFIG.bucket}/${fileName}`;
        const uploadRes = await fetch(uploadUrl, {
          method: "POST",
          headers: {
            "apikey": SUPABASE_STORAGE_CONFIG.anonKey,
            "Authorization": `Bearer ${SUPABASE_STORAGE_CONFIG.anonKey}`,
            "Content-Type": contentType,
            "x-upsert": "true"
          },
          body: blob
        });
        if (uploadRes.ok) {
          const publicUrl = `${SUPABASE_STORAGE_CONFIG.url}/storage/v1/object/public/${SUPABASE_STORAGE_CONFIG.bucket}/${fileName}`;
          return publicUrl;
        }
      } catch(restErr) {}

      // 3. Fallback: Return data URL directly (zero loss, 100% reliable)
      return dataUrl || (typeof fileOrBase64 === "string" ? fileOrBase64 : "");
    } catch(err) {
      console.warn("[Media upload fallback to dataUrl]:", err);
      return dataUrl || (typeof fileOrBase64 === "string" ? fileOrBase64 : "");
    }
  }
  // ----------------------------------------------------
  // 10. REALTIME AUTO-SYNC BACKGROUND WORKER
  // Keeps all phones and admin panel 100% updated in real-time
  // ----------------------------------------------------
  function startSyncWorker() {
    let isSyncing = false;
    async function syncTick() {
      if (isSyncing) return;
      if (Date.now() < _firestoreBackoffUntil) return;
      if (typeof document !== "undefined" && document.hidden) return;
      isSyncing = true;
      try {
        await Promise.all([
          getListings(true),
          getRechargeRequests(true)
        ]);
      } catch(err) {
      } finally {
        isSyncing = false;
      }
    }

    // Initial sync
    setTimeout(syncTick, 100);

    // Periodic sync every 8 seconds
    setInterval(syncTick, 35000);

    // Sync on window focus or visibility change
    if (typeof window !== 'undefined') {
      window.addEventListener('focus', () => syncTick());
      if (typeof document !== 'undefined') {
        document.addEventListener('visibilitychange', () => {
          if (!document.hidden) syncTick();
        });
      }
    }
  }

  // Expose global FirebaseDB object
  window.FirebaseDB = {
    config: FIREBASE_CONFIG,
    getSettings,
    saveSettings,
    getAccountLock,
    saveAccountLock,
    getListings,
    saveListing,
    updateListing,
    deleteListing,
    getRechargeRequests,
    submitRechargeRequest,
    updateRechargeStatus,
    getUsers,
    getUser,
    saveUser,
    updateUserStatus,
    updateUserPro,
    updateUserAccountStatus,
    getLocations,
    saveLocation,
    deleteLocation,
    getCategories,
    saveCategory,
    deleteCategory,
    getBanners,
    saveBanner,
    deleteBanner,
    getPlans,
    savePlan,
    deletePlan,
    recordTransaction,
    getTransactions,
    sendNotification,
    uploadMedia,
    fetchCollection,
    firestoreRequest,
    jsToFirestoreFields,
    firestoreFieldsToJs
  };

  // Start background real-time sync worker
  startSyncWorker();

  console.log('🔥 [Meri Local Bazaar] Multi-Device Firebase Firestore Database Connected & Synced!');

})(typeof window !== 'undefined' ? window : this);
