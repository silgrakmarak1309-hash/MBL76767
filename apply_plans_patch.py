with open("bundle.js", "r") as f:
    code = f.read()

idx_v1 = code.find("async function v1(")
idx_L1 = code.find("async function L1(")
idx_A1 = code.find("async function A1(")

print(f"v1 to A1 bounds: {idx_v1} to {idx_A1}")

new_plans_block = """async function v1(){
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

async function L1(e){
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
"""

code = code[:idx_v1] + new_plans_block + code[idx_A1:]

with open("bundle.js", "w") as f:
    f.write(code)

print("v1, L1, O1 updated successfully!")
