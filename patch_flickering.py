import sys

with open("bundle.js", "r") as f:
    code = f.read()

# Helper function definition to add at the top of bundle.js or before components
# isDataEqual helper
equality_helper = """
function areDataEqual(a, b) {
  if (a === b) return true;
  if (!a || !b) return false;
  if (typeof a !== typeof b) return false;
  try {
    return JSON.stringify(a) === JSON.stringify(b);
  } catch(e) {
    return false;
  }
}
"""

# Check where to insert areDataEqual
if "function areDataEqual(" not in code:
    idx_tp = code.find("var Tp=m.createContext(null);")
    if idx_tp != -1:
        code = code[:idx_tp] + equality_helper + "\n" + code[idx_tp:]
    else:
        code = equality_helper + "\n" + code

print("areDataEqual inserted.")

# 1. Patch AuthProvider (bw)
idx_bw = code.find("function bw({children:e}){")
idx_bw_end = code.find("function Ae(){", idx_bw)
bw_code = code[idx_bw:idx_bw_end]

# Optimize bw:
# Ensure profile state `l` only updates if data changed:
# In `u` (profile fetcher):
old_profile_set = "l(p_data);"
new_profile_set = "l(prev => areDataEqual(prev, p_data) ? prev : p_data);"
bw_code = bw_code.replace(old_profile_set, new_profile_set)

# In onAuthStateChange and handleProfileSync:
# Remove window.addEventListener("storage", handleProfileSync) from AuthProvider to break storage loops
bw_code = bw_code.replace('window.addEventListener("storage", handleProfileSync);', '// window.addEventListener("storage", handleProfileSync);')
bw_code = bw_code.replace('window.removeEventListener("storage", handleProfileSync);', '// window.removeEventListener("storage", handleProfileSync);')

code = code[:idx_bw] + bw_code + code[idx_bw_end:]
print("AuthProvider patched!")

# 2. Patch Home / Marketplace (W1)
idx_w1 = code.find("function W1(){")
idx_w1_end = code.find("function Z1(", idx_w1)
if idx_w1_end == -1:
    idx_w1_end = code.find("function lj(", idx_w1)
w1_code = code[idx_w1:idx_w1_end]

# Find callback O
old_O = """const O=m.useCallback(async()=>{k(!0);try{const E=await Vp({search:l||void 0,locationId:d||void 0,limit:50});v(E)}catch{n.show("Failed to load listings","error")}finally{k(!1)}},[l,d,n]);"""
new_O = """const hasLoadedListingsRef = m.useRef(false);
  const O = m.useCallback(async () => {
    if (!hasLoadedListingsRef.current) k(!0);
    try {
      const E = await Vp({ search: l || void 0, locationId: d || void 0, limit: 50 });
      hasLoadedListingsRef.current = true;
      v(prev => areDataEqual(prev, E) ? prev : E);
    } catch {
      n.show("Failed to load listings", "error");
    } finally {
      k(!1);
    }
  }, [l, d, n]);"""

if old_O in w1_code:
    w1_code = w1_code.replace(old_O, new_O)
    print("Home callback O replaced successfully!")
else:
    print("WARNING: old_O not matched exactly in W1, looking for subpattern...")
    # Find k(!0);try{const E=await Vp
    sub_old = "k(!0);try{const E=await Vp({search:l||void 0,locationId:d||void 0,limit:50});v(E)}"
    sub_new = "try{const E=await Vp({search:l||void 0,locationId:d||void 0,limit:50});v(prev => areDataEqual(prev, E) ? prev : E)}"
    w1_code = w1_code.replace(sub_old, sub_new)

# Guard categories, locations, banners setters in W1
w1_code = w1_code.replace("Ac().then(w)", "Ac().then(res => w(prev => areDataEqual(prev, res) ? prev : res))")
w1_code = w1_code.replace("$c().then(f)", "$c().then(res => f(prev => areDataEqual(prev, res) ? prev : res))")
w1_code = w1_code.replace("p1().then(y)", "p1().then(res => y(prev => areDataEqual(prev, res) ? prev : res))")

code = code[:idx_w1] + w1_code + code[idx_w1_end:]
print("Home view patched!")

# 3. Patch Admin Layout (uj)
idx_uj = code.find("function uj(){")
idx_uj_end = code.find("function dj(", idx_uj)
uj_code = code[idx_uj:idx_uj_end]

uj_code = uj_code.replace("setAdminNotifs(list);", "setAdminNotifs(prev => areDataEqual(prev, list) ? prev : list);")
uj_code = uj_code.replace("setPostPendingCount(pCount);", "setPostPendingCount(prev => prev === pCount ? prev : pCount);")

code = code[:idx_uj] + uj_code + code[idx_uj_end:]
print("Admin layout patched!")

# 4. Patch NormalPostRequestsView
idx_np = code.find("function NormalPostRequestsView(")
idx_np_end = code.find("function TopProRequestsView(", idx_np)
np_code = code[idx_np:idx_np_end]

np_code = np_code.replace("setListings(enriched);", "setListings(prev => areDataEqual(prev, enriched) ? prev : enriched);")

code = code[:idx_np] + np_code + code[idx_np_end:]
print("NormalPostRequestsView patched!")

# 5. Patch TopProRequestsView
idx_tp = code.find("function TopProRequestsView(")
idx_tp_end = code.find("function pj(", idx_tp)
tp_code = code[idx_tp:idx_tp_end]

tp_code = tp_code.replace("if (Array.isArray(reqs)) n(reqs);", "if (Array.isArray(reqs)) n(prev => areDataEqual(prev, reqs) ? prev : reqs);")
tp_code = tp_code.replace("if (Array.isArray(listings)) setAllListings(listings);", "if (Array.isArray(listings)) setAllListings(prev => areDataEqual(prev, listings) ? prev : listings);")

code = code[:idx_tp] + tp_code + code[idx_tp_end:]
print("TopProRequestsView patched!")

# 6. Patch MonthlyPlanRequestsView (pj)
idx_pj = code.find("function pj(")
idx_pj_end = code.find("function vj(", idx_pj)
pj_code = code[idx_pj:idx_pj_end]

pj_code = pj_code.replace("setMonthlyReqs(reqs);", "setMonthlyReqs(prev => areDataEqual(prev, reqs) ? prev : reqs);")

code = code[:idx_pj] + pj_code + code[idx_pj_end:]
print("MonthlyPlanRequestsView patched!")

# 7. Patch AdminOverview (dj)
idx_dj = code.find("function dj({onNavigate}){")
idx_dj_end = code.find("function hj(", idx_dj)
dj_code = code[idx_dj:idx_dj_end]

dj_code = dj_code.replace("r(p),i(v),o(x),u(w)", "r(prev => areDataEqual(prev, p) ? prev : p), i(prev => areDataEqual(prev, v) ? prev : v), o(prev => areDataEqual(prev, x) ? prev : x), u(prev => areDataEqual(prev, w) ? prev : w)")

code = code[:idx_dj] + dj_code + code[idx_dj_end:]
print("AdminOverview patched!")

# 8. Patch AdminUsersTable (hj)
idx_hj = code.find("function hj({isSuperAdmin:e}){")
idx_hj_end = code.find("function vj(", idx_hj)
hj_code = code[idx_hj:idx_hj_end]

hj_code = hj_code.replace("r(await Ic())", "const users = await Ic(); r(prev => areDataEqual(prev, users) ? prev : users)")

code = code[:idx_hj] + hj_code + code[idx_hj_end:]
print("AdminUsersTable patched!")

# 9. Patch AdminListingsTable (vj)
idx_vj = code.find("function vj(){")
idx_vj_end = code.find("function xj(", idx_vj)
vj_code = code[idx_vj:idx_vj_end]

vj_code = vj_code.replace("r(await Qp())", "const lists = await Qp(); r(prev => areDataEqual(prev, lists) ? prev : lists)")

code = code[:idx_vj] + vj_code + code[idx_vj_end:]
print("AdminListingsTable patched!")

# 10. Patch UserRecharge (lj)
idx_lj = code.find("function lj(){")
idx_lj_end = code.find("function pj(", idx_lj)
if idx_lj_end == -1:
    idx_lj_end = code.find("function cj(", idx_lj)
lj_code = code[idx_lj:idx_lj_end]

lj_code = lj_code.replace("l(C);\n        c(T);\n        d(D);\n        p(W);", """l(prev => areDataEqual(prev, C) ? prev : C);
        c(prev => areDataEqual(prev, T) ? prev : T);
        d(prev => areDataEqual(prev, D) ? prev : D);
        p(prev => areDataEqual(prev, W) ? prev : W);""")
lj_code = lj_code.replace("l(C);        c(T);        d(D);        p(W);", """l(prev => areDataEqual(prev, C) ? prev : C);
        c(prev => areDataEqual(prev, T) ? prev : T);
        d(prev => areDataEqual(prev, D) ? prev : D);
        p(prev => areDataEqual(prev, W) ? prev : W);""")

code = code[:idx_lj] + lj_code + code[idx_lj_end:]
print("UserRecharge patched!")

with open("bundle.js", "w") as f:
    f.write(code)

print("All anti-flicker optimizations written successfully!")
