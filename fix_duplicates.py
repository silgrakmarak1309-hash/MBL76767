with open("bundle.js", "r") as f:
    code = f.read()

idx_L1_first = code.find("async function L1(")
idx_w1 = code.find("async function w1(")

print(f"Replacing duplicate region from {idx_L1_first} to {idx_w1}")

clean_block = """async function L1(e){
  let item = Object.assign({}, e);
  if (!item.id) item.id = "plan_" + Date.now() + "_" + Math.random().toString(36).slice(2, 7);
  try {
    const { data: existing } = await L.from("pro_plans").select("id").eq("id", item.id).maybeSingle();
    if (existing) {
      await L.from("pro_plans").update(item).eq("id", item.id);
    } else {
      await L.from("pro_plans").insert(item);
    }
  } catch(err) {}
  
  let saved = [];
  try {
    saved = JSON.parse(localStorage.getItem("admin_plans") || "[]");
  } catch(err) {}
  if (!Array.isArray(saved) || saved.length === 0) {
    saved = [
      { id: "plan_1m", name: "1 Month PRO", duration_days: 30, price: 112.5, description: "30 days priority listings & PRO badge", is_active: true, sort_order: 1 },
      { id: "plan_3m", name: "3 Months PRO", duration_days: 90, price: 120, description: "90 days priority listings & PRO badge", is_active: true, sort_order: 2 },
      { id: "plan_6m", name: "6 Months PRO", duration_days: 180, price: 200, description: "180 days priority listings & PRO badge", is_active: true, sort_order: 3 },
      { id: "plan_1y", name: "1 Year PRO", duration_days: 365, price: 350, description: "365 days priority listings & PRO badge", is_active: true, sort_order: 4 }
    ];
  }
  
  const idx = saved.findIndex(function(p) { return p && (p.id === item.id || (p.name && item.name && p.name.toLowerCase().trim() === item.name.toLowerCase().trim())); });
  if (idx >= 0) {
    saved[idx] = Object.assign({}, saved[idx], item);
    item.id = saved[idx].id;
  } else {
    saved.push(item);
  }
  
  try {
    localStorage.setItem("admin_plans", JSON.stringify(saved));
  } catch(err) {}
  
  try {
    await syncCloudConfig({ plans: saved });
  } catch(err) {}
  
  if (typeof window !== "undefined") {
    window.dispatchEvent(new CustomEvent("pro_plans_updated", { detail: saved }));
    window.dispatchEvent(new Event("storage"));
  }
  return item;
}

async function O1(e){
  try {
    await L.from("pro_plans").delete().eq("id", e);
  } catch(err) {}
  let saved = [];
  try {
    saved = JSON.parse(localStorage.getItem("admin_plans") || "[]");
  } catch(err) {}
  saved = saved.filter(function(p) { return p && p.id !== e; });
  try {
    localStorage.setItem("admin_plans", JSON.stringify(saved));
  } catch(err) {}
  try {
    await syncCloudConfig({ plans: saved });
  } catch(err) {}
  if (typeof window !== "undefined") {
    window.dispatchEvent(new CustomEvent("pro_plans_updated", { detail: saved }));
    window.dispatchEvent(new Event("storage"));
  }
}

async function A1(e, t, n) {
  const cleanVal = typeof t === "string" ? t.trim() : (t !== undefined && t !== null ? String(t) : "");
  try {
    L.from("settings").upsert({ key: e, value: cleanVal, is_public: n, updated_at: new Date().toISOString() }, { onConflict: "key" }).catch(function(){});
  } catch(err) {}
  try {
    const saved = JSON.parse(localStorage.getItem("admin_settings") || "{}");
    saved[e] = { key: e, value: cleanVal, is_public: n, updated_at: new Date().toISOString() };
    localStorage.setItem("admin_settings", JSON.stringify(saved));
    if (e === "upi_id") {
      localStorage.setItem("settings_upi_id", cleanVal);
      localStorage.setItem("app_upi_id", cleanVal);
    }
    if (e === "payment_qr_code" || e === "upi_qr_code") {
      localStorage.setItem("settings_payment_qr_code", cleanVal);
      localStorage.setItem("app_payment_qr_code", cleanVal);
    }
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("app_settings_updated", { detail: { key: e, value: cleanVal } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
  try {
    await syncCloudConfig({ [e]: cleanVal });
  } catch(e) {}
}

async function $1(e){const{data:t,error:n}=await L.from("favorites").select(`
      *,
      listing:listings(
        *,
        category:categories(*),
        location:locations(*)
      )
    `).eq("user_id",e).order("created_at",{ascending:!1});if(n)throw n;return t}

async function I1(e,t){const{data:n,error:r}=await L.from("favorites").select("id").eq("user_id",e).eq("listing_id",t).maybeSingle();return r?!1:!!n}

async function p1(){
  try{
    const{data:e,error:t}=await L.from("banners").select("*").eq("is_active",!0).order("sort_order",{ascending:!0});
    if(!t&&e&&e.length>0)return e;
  }catch(err){}
  try{
    const saved=JSON.parse(localStorage.getItem("admin_banners")||"[]");
    const active=saved.filter(b=>b&&b.is_active!==!1);
    if(active.length>0)return active;
  }catch(err){}
  try{
    const res=await fetch("/banners.json");
    if(res.ok){
      const data=await res.json();
      if(Array.isArray(data)&&data.length>0)return data;
    }
  }catch(err){}
  return [];
}

async function m1(){
  try{
    const{data:e,error:t}=await L.from("banners").select("*").order("sort_order",{ascending:!0});
    if(!t&&e&&e.length>0)return e;
  }catch(err){}
  try{
    const saved=JSON.parse(localStorage.getItem("admin_banners")||"[]");
    if(saved&&saved.length>0)return saved;
  }catch(err){}
  try{
    const res=await fetch("/banners.json");
    if(res.ok){
      const data=await res.json();
      if(Array.isArray(data)&&data.length>0)return data;
    }
  }catch(err){}
  return [];
}

async function v1(){
  const defaultPlans = [
    { id: "plan_1m", name: "1 Month PRO", duration_days: 30, price: 112.5, description: "30 days priority listings & PRO badge", is_active: true, sort_order: 1 },
    { id: "plan_3m", name: "3 Months PRO", duration_days: 90, price: 120, description: "90 days priority listings & PRO badge", is_active: true, sort_order: 2 },
    { id: "plan_6m", name: "6 Months PRO", duration_days: 180, price: 200, description: "180 days priority listings & PRO badge", is_active: true, sort_order: 3 },
    { id: "plan_1y", name: "1 Year PRO", duration_days: 365, price: 350, description: "365 days priority listings & PRO badge", is_active: true, sort_order: 4 }
  ];
  let plans = [];
  try {
    const { data: e, error: t } = await L.from("pro_plans").select("*").order("sort_order", { ascending: true });
    if (!t && e && Array.isArray(e) && e.length > 0) plans = e;
  } catch(err) {}
  
  if (!plans || plans.length === 0) {
    try {
      const { data: cData } = await L.from("listings").select("description, created_at").eq("title", "[SYS_APP_CONFIG]").order("created_at", { ascending: false }).limit(5);
      if (cData && Array.isArray(cData)) {
        for (const row of cData) {
          if (row && row.description) {
            try {
              const cfg = JSON.parse(row.description);
              if (cfg && Array.isArray(cfg.plans) && cfg.plans.length > 0) {
                plans = cfg.plans.filter(p => !p.is_deleted);
                if (plans.length > 0) break;
              }
            } catch(e) {}
          }
        }
      }
    } catch(err) {}
  }
  
  if (!plans || plans.length === 0) {
    try {
      const local = JSON.parse(localStorage.getItem("admin_plans") || "[]");
      if (Array.isArray(local) && local.length > 0) {
        plans = local.filter(p => !p.is_deleted);
      }
    } catch(err) {}
  }
  
  if (!plans || plans.length === 0) {
    plans = defaultPlans;
  }
  
  return plans.map(p => ({
    id: p.id || ("plan_" + Math.random().toString(36).slice(2, 8)),
    name: p.name || "PRO Plan",
    price: Number(p.price) || 0,
    duration_days: Number(p.duration_days) || 30,
    is_active: p.is_active !== undefined ? Boolean(p.is_active) : true,
    sort_order: Number(p.sort_order) || 0,
    description: p.description || (p.duration_days + " days priority listings & PRO badge")
  }));
}
"""

code = code[:idx_L1_first] + clean_block + "\n" + code[idx_w1:]

with open("bundle.js", "w") as f:
    f.write(code)

print("Duplicates cleaned!")

