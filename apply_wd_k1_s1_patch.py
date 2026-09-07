with open("bundle.js", "r") as f:
    code = f.read()

idx_wd = code.find("async function wd(")
idx_n1 = code.find("async function N1(", idx_wd)
print(f"wd to N1 bounds: {idx_wd} to {idx_n1}, length: {idx_n1 - idx_wd}")

new_wd_k1_s1_block = """async function wd(e, t, uEmail) {
  try { await L.rpc("admin_set_account_status", { p_user_id: e, p_status: t }); } catch(err) {}
  try { if (e) await L.from("profiles").update({ account_status: t, status: t, is_blocked: t === "blocked" }).eq("id", e); } catch(err) {}
  try { if (uEmail) await L.from("profiles").update({ account_status: t, status: t, is_blocked: t === "blocked" }).eq("email", uEmail); } catch(err) {}
  try {
    const stats = JSON.parse(localStorage.getItem("admin_status_overrides") || "{}");
    if (e) stats[e] = t;
    if (uEmail) {
      stats[uEmail] = t;
      stats[uEmail.toLowerCase().trim()] = t;
    }
    localStorage.setItem("admin_status_overrides", JSON.stringify(stats));
  } catch(err) {}
  try {
    const sMap = {};
    if (e) sMap[e] = t;
    if (uEmail) {
      sMap[uEmail] = t;
      sMap[uEmail.toLowerCase().trim()] = t;
    }
    await syncCloudConfig({ user_status_overrides: sMap });
  } catch(err) {}
  try {
    if (e) {
      const cachedStr = localStorage.getItem("mlb_saved_profile_" + e);
      if (cachedStr) {
        const cached = JSON.parse(cachedStr);
        cached.account_status = t;
        cached.status = t;
        cached.is_blocked = t === "blocked";
        localStorage.setItem("mlb_saved_profile_" + e, JSON.stringify(cached));
      }
    }
  } catch(err) {}
  try {
    const users = JSON.parse(localStorage.getItem("admin_users") || "[]");
    if (Array.isArray(users)) {
      users.forEach(u => {
        const matchId = e && (u.id === e || u.user_id === e);
        const cleanU = (u.email || "").toLowerCase().trim();
        const matchEmail = uEmail && cleanU === uEmail.toLowerCase().trim();
        if (matchId || matchEmail) {
          u.account_status = t;
          u.status = t;
          u.is_blocked = t === "blocked";
        }
      });
      localStorage.setItem("admin_users", JSON.stringify(users));
    }
  } catch(err) {}
  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("user_profile_updated", { detail: { id: e, email: uEmail, account_status: t, status: t } }));
      window.dispatchEvent(new CustomEvent("user_status_changed", { detail: { id: e, email: uEmail, account_status: t } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}

async function k1(e, t, n, uEmail) {
  const days = Number(t) || 30;
  const expiry = new Date(Date.now() + days * 86400000).toISOString();
  try { await L.rpc("admin_activate_pro", { p_user_id: e, p_duration_days: days, p_reason: n || ("Admin activated PRO: " + days + " days") }); } catch(err) {}
  try { if (e) await L.from("profiles").update({ is_pro: !0, pro_status: "active", pro_expires_at: expiry, pro_expiry_at: expiry, approved_expiry_date: expiry }).eq("id", e); } catch(err) {}
  try { if (uEmail) await L.from("profiles").update({ is_pro: !0, pro_status: "active", pro_expires_at: expiry, pro_expiry_at: expiry, approved_expiry_date: expiry }).eq("email", uEmail); } catch(err) {}
  const pData = { is_pro: !0, pro_status: "active", pro_expires_at: expiry, pro_expiry_at: expiry, approved_expiry_date: expiry };
  try {
    const localProList = JSON.parse(localStorage.getItem("admin_pro_overrides") || "{}");
    if (e) localProList[e] = pData;
    if (uEmail) {
      localProList[uEmail] = pData;
      localProList[uEmail.toLowerCase().trim()] = pData;
    }
    localStorage.setItem("admin_pro_overrides", JSON.stringify(localProList));
    localStorage.setItem("pro_status_overrides", JSON.stringify(localProList));
  } catch(err) {}
  try {
    const pMap = {};
    if (e) pMap[e] = pData;
    if (uEmail) {
      pMap[uEmail] = pData;
      pMap[uEmail.toLowerCase().trim()] = pData;
    }
    await syncCloudConfig({ user_pro_overrides: pMap });
  } catch(err) {}
  try {
    const users = JSON.parse(localStorage.getItem("admin_users") || "[]");
    if (Array.isArray(users)) {
      users.forEach(u => {
        const matchId = e && (u.id === e || u.user_id === e);
        const cleanU = (u.email || "").toLowerCase().trim();
        const matchEmail = uEmail && cleanU === uEmail.toLowerCase().trim();
        if (matchId || matchEmail) {
          u.is_pro = !0;
          u.pro_status = "active";
          u.pro_expires_at = expiry;
          u.pro_expiry_at = expiry;
          u.approved_expiry_date = expiry;
        }
      });
      localStorage.setItem("admin_users", JSON.stringify(users));
    }
  } catch(err) {}
  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "approved" } }));
      window.dispatchEvent(new CustomEvent("user_profile_updated", { detail: { id: e, email: uEmail, is_pro: !0, pro_status: "active" } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}

async function S1(e, uEmail) {
  try { await L.rpc("admin_remove_pro", { p_user_id: e }); } catch(err) {}
  try { if (e) await L.from("profiles").update({ is_pro: !1, pro_status: "inactive", pro_expires_at: null, pro_expiry_at: null, approved_expiry_date: null }).eq("id", e); } catch(err) {}
  try { if (uEmail) await L.from("profiles").update({ is_pro: !1, pro_status: "inactive", pro_expires_at: null, pro_expiry_at: null, approved_expiry_date: null }).eq("email", uEmail); } catch(err) {}
  const pData = { is_pro: !1, pro_status: "inactive", pro_expires_at: null, pro_expiry_at: null, approved_expiry_date: null };
  try {
    const localProList = JSON.parse(localStorage.getItem("admin_pro_overrides") || "{}");
    if (e) localProList[e] = pData;
    if (uEmail) {
      localProList[uEmail] = pData;
      localProList[uEmail.toLowerCase().trim()] = pData;
    }
    localStorage.setItem("admin_pro_overrides", JSON.stringify(localProList));
    localStorage.setItem("pro_status_overrides", JSON.stringify(localProList));
  } catch(err) {}
  try {
    const pMap = {};
    if (e) pMap[e] = pData;
    if (uEmail) {
      pMap[uEmail] = pData;
      pMap[uEmail.toLowerCase().trim()] = pData;
    }
    await syncCloudConfig({ user_pro_overrides: pMap });
  } catch(err) {}
  try {
    const users = JSON.parse(localStorage.getItem("admin_users") || "[]");
    if (Array.isArray(users)) {
      users.forEach(u => {
        const matchId = e && (u.id === e || u.user_id === e);
        const cleanU = (u.email || "").toLowerCase().trim();
        const matchEmail = uEmail && cleanU === uEmail.toLowerCase().trim();
        if (matchId || matchEmail) {
          u.is_pro = !1;
          u.pro_status = "inactive";
          u.pro_expires_at = null;
          u.pro_expiry_at = null;
          u.approved_expiry_date = null;
        }
      });
      localStorage.setItem("admin_users", JSON.stringify(users));
    }
  } catch(err) {}
  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "rejected" } }));
      window.dispatchEvent(new CustomEvent("user_profile_updated", { detail: { id: e, email: uEmail, is_pro: !1, pro_status: "inactive" } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}
"""

code = code[:idx_wd] + new_wd_k1_s1_block + code[idx_n1:]

with open("bundle.js", "w") as f:
    f.write(code)

print("wd, k1, S1 patched successfully!")
