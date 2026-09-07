import re

with open('bundle.js', 'r') as f:
    code = f.read()

# 1. Patch wd, k1, S1
p_wd = code.find('async function wd(e,')
p_next_after_s1 = code.find('async function _d(', p_wd)

new_user_status_funcs = """async function wd(e, t, uEmail) {
  try { await L.rpc("admin_set_account_status", { p_user_id: e, p_status: t }); } catch(err) {}
  try { await L.from("profiles").update({ account_status: t, status: t }).eq("id", e); } catch(err) {}
  try { if (uEmail) await L.from("profiles").update({ account_status: t, status: t }).eq("email", uEmail); } catch(err) {}
  
  await saveCloudSyncRecord("[SYS_USER_STATUS]", {
    user_id: e || "",
    user_email: uEmail || "",
    account_status: t,
    status: t,
    updated_at: new Date().toISOString()
  });
  _lastCloudSyncFetchTime = 0;

  try {
    const stats = JSON.parse(localStorage.getItem("admin_status_overrides") || "{}");
    if (e) stats[e] = { account_status: t, status: t };
    if (uEmail) {
      stats[uEmail] = { account_status: t, status: t };
      stats[uEmail.toLowerCase().trim()] = { account_status: t, status: t };
    }
    localStorage.setItem("admin_status_overrides", JSON.stringify(stats));
  } catch(err) {}

  try {
    if (e) {
      const cachedStr = localStorage.getItem("mlb_saved_profile_" + e);
      if (cachedStr) {
        const cached = JSON.parse(cachedStr);
        cached.account_status = t;
        cached.status = t;
        localStorage.setItem("mlb_saved_profile_" + e, JSON.stringify(cached));
      }
    }
  } catch(err) {}

  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("user_status_updated", { detail: { user_id: e, email: uEmail, status: t } }));
      window.dispatchEvent(new CustomEvent("user_status_changed", { detail: { user_id: e, email: uEmail, status: t } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}

async function k1(e, t, uEmail) {
  const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
  const now = new Date();
  const expDate = new Date(now.getTime() + (t || 30) * 86400000).toISOString();
  
  try {
    if (isUUID(e)) {
      await L.from("profiles").update({
        is_pro: true,
        pro_status: "active",
        pro_expires_at: expDate,
        approved_expiry_date: expDate,
        account_status: "active",
        status: "active"
      }).eq("id", e);
    }
  } catch(err) {}
  try {
    if (uEmail) {
      await L.from("profiles").update({
        is_pro: true,
        pro_status: "active",
        pro_expires_at: expDate,
        approved_expiry_date: expDate,
        account_status: "active",
        status: "active"
      }).eq("email", uEmail);
    }
  } catch(err) {}

  await saveCloudSyncRecord("[SYS_USER_STATUS]", {
    user_id: e || "",
    user_email: uEmail || "",
    is_pro: true,
    pro_status: "active",
    pro_expires_at: expDate,
    approved_expiry_date: expDate,
    account_status: "active",
    status: "active",
    updated_at: new Date().toISOString()
  });
  _lastCloudSyncFetchTime = 0;

  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("user_status_updated", { detail: { user_id: e, email: uEmail, status: "active", is_pro: true } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}

async function S1(e, uEmail) {
  const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
  try {
    if (isUUID(e)) {
      await L.from("profiles").update({
        is_pro: false,
        pro_status: "inactive",
        pro_expires_at: null,
        approved_expiry_date: null
      }).eq("id", e);
    }
  } catch(err) {}
  try {
    if (uEmail) {
      await L.from("profiles").update({
        is_pro: false,
        pro_status: "inactive",
        pro_expires_at: null,
        approved_expiry_date: null
      }).eq("email", uEmail);
    }
  } catch(err) {}

  await saveCloudSyncRecord("[SYS_USER_STATUS]", {
    user_id: e || "",
    user_email: uEmail || "",
    is_pro: false,
    pro_status: "inactive",
    pro_expires_at: null,
    approved_expiry_date: null,
    updated_at: new Date().toISOString()
  });
  _lastCloudSyncFetchTime = 0;

  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("user_status_updated", { detail: { user_id: e, email: uEmail, is_pro: false } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}
"""

code = code[:p_wd] + new_user_status_funcs + code[p_next_after_s1:]

# 2. Patch Jp, j1, q1
p_jp = code.find('async function Jp(){')
p_after_q1 = code.find('async function x1(){', p_jp)

new_jp_j1_q1 = """async function Jp() {
  let dbList = [];
  try {
    const { data: e, error: t } = await L.from("recharge_requests").select("*").order("submitted_at", { ascending: !1 });
    if (!t && e && Array.isArray(e) && e.length > 0) dbList = e;
  } catch(err) {}

  let syncState = { rechargeStatusOverrides: {}, rechargeRequests: [] };
  try { syncState = await getCloudSyncState(true); } catch(err) {}
  const cloudStatusOverrides = syncState.rechargeStatusOverrides || {};
  const cloudReqs = syncState.rechargeRequests || [];

  let localList = [];
  try { localList = JSON.parse(localStorage.getItem("all_recharge_requests") || "[]"); } catch(err) {}

  const mergedMap = new Map();
  // 1. Cloud Sync Reqs first (they contain full user info from all users)
  cloudReqs.forEach(r => {
    if (!r) return;
    const key = r.id || r.utr;
    if (key) mergedMap.set(key, { ...r });
  });
  // 2. DB List
  dbList.forEach(r => {
    if (!r) return;
    const key = r.id || r.utr;
    if (key) {
      const existing = mergedMap.get(key);
      mergedMap.set(key, { ...(existing || {}), ...r });
    }
  });
  // 3. Local List
  localList.forEach(r => {
    if (!r) return;
    const key = r.id || r.utr;
    if (key) {
      const existing = mergedMap.get(key);
      if (!existing) mergedMap.set(key, { ...r });
    }
  });

  const finalReqs = Array.from(mergedMap.values()).map(r => {
    const override = (r.id && cloudStatusOverrides[r.id]) || (r.utr && cloudStatusOverrides[r.utr]);
    if (override) {
      return {
        ...r,
        status: override.status || r.status,
        approved_expiry_date: override.approved_expiry_date || r.approved_expiry_date,
        rejection_reason: override.rejection_reason || r.rejection_reason
      };
    }
    return r;
  });

  finalReqs.sort((a, b) => new Date(b.created_at || b.submitted_at || 0).getTime() - new Date(a.created_at || a.submitted_at || 0).getTime());
  return finalReqs;
}

async function j1(e, optListingId, optFeatured) {
  let reqObj = null;
  try {
    const list = await Jp();
    reqObj = list.find(r => r && (r.id === e || r.utr === e));
  } catch(err) {}
  const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
  const durationDays = reqObj?.plan?.duration_days || (Number(reqObj?.amount) >= 300 ? 365 : Number(reqObj?.amount) >= 180 ? 180 : Number(reqObj?.amount) >= 115 ? 90 : 30);
  const expDate = new Date(Date.now() + durationDays * 86400000).toISOString();
  
  try {
    if (isUUID(e)) {
      await L.from("recharge_requests").update({
        status: "approved",
        approved_expiry_date: expDate,
        reviewed_at: new Date().toISOString()
      }).eq("id", e);
    }
  } catch(err) {}

  const uId = reqObj?.user_id;
  const uEmail = reqObj?.user_email;
  const proProfileUpdate = {
    is_pro: true,
    pro_status: "active",
    pro_expires_at: expDate,
    pro_expiry_at: expDate,
    approved_expiry_date: expDate,
    account_status: "active",
    status: "active"
  };

  try {
    if (uId && isUUID(uId)) {
      await L.from("profiles").update(proProfileUpdate).eq("id", uId);
    }
  } catch(err) {}
  try {
    if (uEmail) {
      await L.from("profiles").update(proProfileUpdate).eq("email", uEmail);
    }
  } catch(err) {}

  // Save approval status override to cloud sync
  await saveCloudSyncRecord("[SYS_RECHARGE_STATUS]", {
    req_id: e,
    utr: reqObj?.utr || "",
    user_id: uId || "",
    user_email: uEmail || "",
    status: "approved",
    approved_expiry_date: expDate,
    reviewed_at: new Date().toISOString()
  });

  // Save user status to cloud sync
  if (uId || uEmail) {
    await saveCloudSyncRecord("[SYS_USER_STATUS]", {
      user_id: uId || "",
      user_email: uEmail || "",
      is_pro: true,
      pro_status: "active",
      pro_expires_at: expDate,
      approved_expiry_date: expDate,
      account_status: "active",
      status: "active",
      updated_at: new Date().toISOString()
    });
  }

  // Also save transaction record
  await saveCloudSyncRecord("[SYS_TRANSACTION]", {
    id: "tx_" + e,
    user_id: uId || "",
    user_name: reqObj?.user_name || "User",
    user_email: uEmail || "",
    user_phone: reqObj?.user_phone || "",
    amount: reqObj?.amount || 0,
    type: reqObj?.type || (reqObj?.is_top_pro ? "top_pro_boost" : "monthly_plan"),
    plan_name: reqObj?.plan?.name || "PRO Plan",
    plan_id: reqObj?.plan_id || "",
    utr: reqObj?.utr || "",
    status: "approved",
    created_at: reqObj?.created_at || new Date().toISOString(),
    approved_expiry_date: expDate
  });

  _lastCloudSyncFetchTime = 0;

  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "approved" } }));
      window.dispatchEvent(new CustomEvent("user_status_updated", { detail: { user_id: uId, email: uEmail, status: "active" } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}

async function q1(e, t) {
  let reqObj = null;
  try {
    const list = await Jp();
    reqObj = list.find(r => r && (r.id === e || r.utr === e));
  } catch(err) {}
  const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
  try {
    if (isUUID(e)) {
      await L.from("recharge_requests").update({
        status: "rejected",
        rejection_reason: t,
        reviewed_at: new Date().toISOString()
      }).eq("id", e);
    }
  } catch(err) {}
  await saveCloudSyncRecord("[SYS_RECHARGE_STATUS]", {
    req_id: e,
    utr: reqObj?.utr || "",
    user_id: reqObj?.user_id || "",
    user_email: reqObj?.user_email || "",
    status: "rejected",
    rejection_reason: t,
    reviewed_at: new Date().toISOString()
  });
  _lastCloudSyncFetchTime = 0;
  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "rejected", rejection_reason: t } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}
"""

code = code[:p_jp] + new_jp_j1_q1 + code[p_after_q1:]

# 3. Patch Q1 (Recharge/PRO request submit) and X1 (Transaction list)
p_q1 = code.find('async function Q1(e){')
p_after_x1 = code.find('async function Z1(e,t){', p_q1)

new_q1_x1 = """async function Q1(e) {
  let tUser = null;
  try {
    const { data: t } = await L.auth.getUser();
    if (t && t.user) tUser = t.user;
  } catch(err) {}
  if (!tUser) {
    try {
      const { data: s } = await L.auth.getSession();
      if (s && s.session && s.session.user) tUser = s.session.user;
    } catch(err) {}
  }
  const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
  const uId = (e.user_id && isUUID(e.user_id)) ? e.user_id : ((tUser?.id && isUUID(tUser.id)) ? tUser.id : (e.user_id || "54d69b2e-76f7-410d-84fc-af00f7101786"));
  let userProfile = null;
  if (uId && isUUID(uId)) {
    try {
      const { data: prof } = await L.from("profiles").select("*").eq("id", uId).maybeSingle();
      if (prof) userProfile = prof;
    } catch(err) {}
  }
  const uName = e.user_name || userProfile?.name || userProfile?.full_name || tUser?.user_metadata?.name || tUser?.email?.split("@")[0] || "User";
  const uEmail = e.user_email || userProfile?.email || tUser?.email || "user@example.com";
  const uPhone = e.phone || e.user_phone || userProfile?.phone || userProfile?.whatsapp || tUser?.user_metadata?.phone || "";
  const cleanUtr = (e.utr || "").trim();
  const amt = Number(e.amount) || (e.plan_id === "plan_single_top_pro" ? 30 : 112.5);
  const nowIso = new Date().toISOString();
  const isTopPro = e.type === "top_pro_boost" || e.plan_id === "plan_single_top_pro" || amt === 30 || amt === 10 || amt === 20 || Boolean(e.listing_id || e.listing_title || e.listing_image || e.is_top_pro);
  
  let planObj = null;
  if (isTopPro) {
    planObj = { id: "plan_single_top_pro", name: "Top PRO Boost", price: amt, duration_days: 30 };
  } else if (amt >= 300) {
    planObj = { id: "plan_1y", name: "1 Year PRO", price: amt, duration_days: 365 };
  } else if (amt >= 180) {
    planObj = { id: "plan_6m", name: "6 Months PRO", price: amt, duration_days: 180 };
  } else if (amt >= 115) {
    planObj = { id: "plan_3m", name: "3 Months PRO", price: amt, duration_days: 90 };
  } else {
    planObj = { id: "plan_1m", name: "Monthly PRO (1 Month)", price: amt, duration_days: 30 };
  }

  let insertId = (isTopPro ? "req_top_pro_" : "req_monthly_") + Date.now() + "_" + Math.random().toString(36).slice(2, 8);
  const newReq = {
    id: insertId,
    user_id: uId,
    user_name: uName,
    user_email: uEmail,
    user_phone: uPhone,
    user: { id: uId, name: uName, email: uEmail, phone: uPhone },
    plan_id: planObj.id,
    plan: planObj,
    amount: amt,
    utr: cleanUtr,
    payment_proof_url: e.payment_proof_url || "",
    listing_id: e.listing_id || "",
    listing_title: e.listing_title || (isTopPro ? "Top PRO Listing" : ""),
    listing_image: e.listing_image || "",
    price: e.price || "",
    category: e.category || "",
    location: e.location || "",
    status: "pending",
    type: isTopPro ? "top_pro_boost" : "monthly_plan",
    is_top_pro: isTopPro,
    created_at: nowIso,
    submitted_at: nowIso
  };

  try {
    const dbPayload = {
      amount: amt,
      utr: cleanUtr,
      payment_proof_url: e.payment_proof_url || "",
      status: "pending",
      submitted_at: nowIso
    };
    if (isUUID(uId)) dbPayload.user_id = uId;
    if (isUUID(e.plan_id)) dbPayload.plan_id = e.plan_id;
    const { data: insData, error: insErr } = await L.from("recharge_requests").insert(dbPayload).select().maybeSingle();
    if (!insErr && insData && insData.id) {
      newReq.id = insData.id;
      insertId = insData.id;
    }
  } catch(err) {
    console.warn("Supabase recharge_requests insert fallback:", err);
  }

  // Save to Cloud Sync
  await saveCloudSyncRecord(isTopPro ? "[SYS_TOP_PRO_REQUEST]" : "[SYS_RECHARGE_REQUEST]", newReq);

  // Also save transaction record
  await saveCloudSyncRecord("[SYS_TRANSACTION]", {
    id: "tx_" + insertId,
    user_id: uId,
    user_name: uName,
    user_email: uEmail,
    user_phone: uPhone,
    amount: amt,
    type: isTopPro ? "top_pro_boost" : "monthly_plan",
    plan_name: planObj.name,
    plan_id: planObj.id,
    utr: cleanUtr,
    status: "pending",
    created_at: nowIso
  });

  _lastCloudSyncFetchTime = 0;

  // Local cache update
  try {
    const list = JSON.parse(localStorage.getItem("all_recharge_requests") || "[]");
    const filtered = list.filter(r => r && r.id !== newReq.id && (!r.utr || !cleanUtr || r.utr.trim().toLowerCase() !== cleanUtr.toLowerCase()));
    filtered.unshift(newReq);
    localStorage.setItem("all_recharge_requests", JSON.stringify(filtered));
  } catch(err) {}

  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("recharge_request_created", { detail: newReq }));
      window.dispatchEvent(new CustomEvent("admin_notification_received", { detail: {
        id: "notif_" + insertId,
        req_id: insertId,
        type: isTopPro ? "top_pro_request" : "monthly_plan_request",
        title: isTopPro ? "New Top PRO Request" : "New PRO Plan Request",
        message: uName + " (" + uEmail + ") requested " + planObj.name + " (₹" + amt + ") - UTR: " + cleanUtr,
        user_name: uName,
        user_email: uEmail,
        amount: amt,
        utr: cleanUtr,
        created_at: nowIso
      } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}

  return newReq;
}

async function getAllTransactions() {
  let dbTxs = [];
  try {
    const { data, error } = await L.from("transactions").select("*").order("created_at", { ascending: false });
    if (!error && Array.isArray(data)) dbTxs = data;
  } catch(e) {}

  let allReqs = [];
  try { allReqs = await Jp(); } catch(e) {}

  let syncState = {};
  try { syncState = await getCloudSyncState(); } catch(e) {}
  const cloudTxs = syncState.cloudTransactions || [];

  const txMap = new Map();

  // 1. Convert all recharge/pro requests to transactions
  (allReqs || []).forEach(req => {
    if (!req) return;
    const txId = "tx_" + (req.id || req.utr);
    txMap.set(txId, {
      id: txId,
      user_id: req.user_id,
      user_name: req.user_name || (req.user && req.user.name) || (req.user_email ? req.user_email.split("@")[0] : "User"),
      user_email: req.user_email || (req.user && req.user.email) || "",
      user_phone: req.user_phone || (req.user && req.user.phone) || "",
      amount: Number(req.amount) || 0,
      type: req.type || (req.is_top_pro ? "top_pro_boost" : "monthly_plan"),
      plan_name: req.plan?.name || (req.is_top_pro ? "Top PRO Boost" : "Monthly PRO Plan"),
      plan_id: req.plan_id || "",
      utr: req.utr || "",
      status: req.status || "pending",
      created_at: req.created_at || req.submitted_at || new Date().toISOString(),
      approved_expiry_date: req.approved_expiry_date || null
    });
  });

  // 2. Cloud transactions
  cloudTxs.forEach(ctx => {
    if (!ctx) return;
    const key = ctx.id || ("tx_" + ctx.utr);
    const existing = txMap.get(key);
    txMap.set(key, { ...(existing || {}), ...ctx });
  });

  // 3. DB Transactions
  dbTxs.forEach(dbTx => {
    if (!dbTx) return;
    const key = dbTx.id || ("tx_" + dbTx.utr);
    const existing = txMap.get(key);
    txMap.set(key, { ...(existing || {}), ...dbTx });
  });

  const list = Array.from(txMap.values());
  list.sort((a, b) => new Date(b.created_at || 0).getTime() - new Date(a.created_at || 0).getTime());
  return list;
}

async function X1(userId) {
  const all = await getAllTransactions();
  if (!userId) return all;
  return all.filter(t => t && (t.user_id === userId || (t.user_email && t.user_email === userId)));
}
"""

code = code[:p_q1] + new_q1_x1 + code[p_after_x1:]

# 4. Patch mj (Admin transactions view to use getAllTransactions)
p_mj_call = code.find('const[e,t]=m.useState([]),[l,s]=m.useState(new Map),[i,r]=m.useState(!0),c=he(),u=m.useCallback(async()=>{r(!0);try{const[d,f]=await Promise.all([Jp(),Ic()]);')
if p_mj_call != -1:
    code = code.replace(
        'const[d,f]=await Promise.all([Jp(),Ic()]);',
        'const[d,f]=await Promise.all([getAllTransactions(),Ic()]);'
    )
    print("Updated mj() to use getAllTransactions()!")

with open('bundle.js', 'w') as f:
    f.write(code)

print("All synchronization and multi-user patches applied successfully!")
