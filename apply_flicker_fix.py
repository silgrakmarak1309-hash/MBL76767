with open("bundle.js", "r") as f:
    code = f.read()

# 1. Update W1 loading logic
old_w1_logic = """  const hasLoadedListingsRef = m.useRef(false);
  const prevFilterRef = m.useRef({ l: null, d: null });
  const O = m.useCallback(async (isUserSearch = false) => {
    if (!hasLoadedListingsRef.current || isUserSearch) {
      k(true);
    }
    try {
      const E = await Vp({ search: l || void 0, locationId: d || void 0, limit: 50 });
      hasLoadedListingsRef.current = true;
      v(prev => areDataEqual(prev, E) ? prev : E);
    } catch {
      if (toastRef.current && !hasLoadedListingsRef.current) {
        toastRef.current.show("Failed to load listings", "error");
      }
    } finally {
      k(false);
    }
  }, [l, d]);
  m.useEffect(() => {
    const isFilterChange = (prevFilterRef.current.l !== l || prevFilterRef.current.d !== d);
    if (prevFilterRef.current.l === null) {
      // First initial mount
      prevFilterRef.current = { l, d };
      O(false);
    } else if (isFilterChange) {
      prevFilterRef.current = { l, d };
      O(true);
    }
  }, [O, l, d]);
  const userId = e?.id;
  m.useEffect(() => {
    if (userId) {
      syncLocalListingsToSupabase().then(() => O(false)).catch(() => {});
      Yp(userId).then(favs => {
        I(prev => {
          const arr = Array.from(prev);
          return areDataEqual(arr, favs) ? prev : new Set(favs);
        });
      }).catch(() => {});
    }
  }, [userId, O]);"""

new_w1_logic = """  const hasLoadedListingsRef = m.useRef(false);
  const prevFilterRef = m.useRef({ l: null, d: null });
  const O = m.useCallback(async (isUserSearch = false) => {
    if (!hasLoadedListingsRef.current) {
      k(true);
    }
    try {
      const E = await Vp({ search: l || void 0, locationId: d || void 0, limit: 50 });
      hasLoadedListingsRef.current = true;
      v(prev => areDataEqual(prev, E) ? prev : E);
    } catch {
      if (toastRef.current && !hasLoadedListingsRef.current) {
        toastRef.current.show("Failed to load listings", "error");
      }
    } finally {
      k(false);
    }
  }, [l, d]);
  m.useEffect(() => {
    const isFilterChange = (prevFilterRef.current.l !== l || prevFilterRef.current.d !== d);
    if (prevFilterRef.current.l === null) {
      prevFilterRef.current = { l, d };
      O(false);
    } else if (isFilterChange) {
      prevFilterRef.current = { l, d };
      O(true);
    }
  }, [O, l, d]);
  const userId = e?.id;
  m.useEffect(() => {
    if (userId) {
      Yp(userId).then(favs => {
        I(prev => {
          const arr = Array.from(prev);
          return areDataEqual(arr, favs) ? prev : new Set(favs);
        });
      }).catch(() => {});
    }
  }, [userId]);"""

if old_w1_logic in code:
    code = code.replace(old_w1_logic, new_w1_logic)
    print("Replaced W1 logic successfully")
else:
    print("WARNING: old_w1_logic not found verbatim, searching partially...")

# 2. Update JSX render condition in W1
old_w1_jsx = '_?a.jsx(Oc,{}):recentOnly.length===0?'
new_w1_jsx = '(_&&p.length===0)?a.jsx(Oc,{}):recentOnly.length===0?'

if old_w1_jsx in code:
    code = code.replace(old_w1_jsx, new_w1_jsx)
    print("Replaced W1 JSX condition successfully")
else:
    print("WARNING: old_w1_jsx not found")

# 3. Update Search V1 JSX condition
old_v1_jsx = 'M?a.jsx(Oc,{}):b.length===0?'
new_v1_jsx = '(M&&b.length===0)?a.jsx(Oc,{}):b.length===0?'

if old_v1_jsx in code:
    code = code.replace(old_v1_jsx, new_v1_jsx)
    print("Replaced V1 JSX condition successfully")
else:
    print("WARNING: old_v1_jsx not found")

# 4. Update Favorites rj JSX condition
old_rj_jsx = 'i?a.jsx(Oc,{}):r.length===0?'
new_rj_jsx = '(i&&r.length===0)?a.jsx(Oc,{}):r.length===0?'

if old_rj_jsx in code:
    code = code.replace(old_rj_jsx, new_rj_jsx)
    print("Replaced rj JSX condition successfully")
else:
    print("WARNING: old_rj_jsx not found")

with open("bundle.js", "w") as f:
    f.write(code)

