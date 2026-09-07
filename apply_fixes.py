import re

with open("bundle.js", "r") as f:
    code = f.read()

print("Original length:", len(code))

# 1. Update syncCloudConfig to also support merging user_status_overrides and user_pro_overrides
sync_old_idx = code.find("async function syncCloudConfig(")
sync_end_idx = code.find("async function sendAdminNotification(", sync_old_idx)
print("syncCloudConfig bounds:", sync_old_idx, sync_end_idx)

new_syncCloudConfig = """async function syncCloudConfig(updates) {
  try {
    let tUser = null;
    try { const { data: t } = await L.auth.getUser(); if (t && t.user) tUser = t.user; } catch(e) {}
    if (!tUser) {
      try { const { data: s } = await L.auth.getSession(); if (s && s.session && s.session.user) tUser = s.session.user; } catch(e) {}
    }
    const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
    const syncUid = (tUser?.id && isUUID(tUser.id)) ? tUser.id : "54d69b2e-76f7-410d-84fc-af00f7101786";
    let currentConfig = {};
    try {
      const { data: cData } = await L.from("listings").select("description").eq("title", "[SYS_APP_CONFIG]").order("created_at", { ascending: false }).limit(1);
      if (cData && cData[0] && cData[0].description) {
        currentConfig = JSON.parse(cData[0].description) || {};
      }
    } catch(e) {}
    
    // Deep merge nested overrides if present
    const user_status_overrides = Object.assign({}, currentConfig.user_status_overrides || {}, updates?.user_status_overrides || {});
    const user_pro_overrides = Object.assign({}, currentConfig.user_pro_overrides || {}, updates?.user_pro_overrides || {});
    
    const merged = Object.assign({}, currentConfig, updates, {
      user_status_overrides,
      user_pro_overrides,
      updated_at: new Date().toISOString()
    });
    
    const { error: insErr } = await L.from("listings").insert({
      user_id: syncUid,
      title: "[SYS_APP_CONFIG]",
      category_id: "3ed03846-ea53-4f52-9db5-17550b75f3f2",
      location_id: "02ef9e15-c49f-459e-916c-2432e90dd230",
      price: 0,
      condition: "new",
      description: JSON.stringify(merged),
      phone: "9876543210",
      whatsapp: "9876543210",
      images: [],
      status: "active",
      is_featured: false
    });
    if (insErr) console.warn("Cloud config sync warning:", insErr);
  } catch(err) {
    console.warn("Cloud config sync error:", err);
  }
}
"""

code = code[:sync_old_idx] + new_syncCloudConfig + code[sync_end_idx:]

print("Updated syncCloudConfig, new length:", len(code))

with open("bundle.js", "w") as f:
    f.write(code)

