import sys

with open("bundle.js", "r") as f:
    code = f.read()

# 1. Replace r1 (ToastProvider)
idx_r1 = code.find("function r1({children:e}){")
idx_he = code.find("function he(){", idx_r1)
if idx_r1 != -1 and idx_he != -1:
    r1_old = code[idx_r1:idx_he]
    print("Found r1 from", idx_r1, "to", idx_he)
    
    # We will rewrite r1 with memoized context value and memoized callbacks
    # Keep getToastMeta intact
    idx_gtm = r1_old.find("const getToastMeta")
    gtm_part = r1_old[idx_gtm:r1_old.find("return a.jsxs(zp.Provider")]
    
    r1_new = """function r1({children:e}){
  const [t, n] = m.useState([]);
  const r = m.useCallback((i, l = "info") => {
    const o = Date.now() + Math.random();
    n(c => [...c, { id: o, type: l, message: i }]);
    setTimeout(() => {
      n(c => c.filter(u => u.id !== o));
    }, 4500);
  }, []);
  const s = m.useCallback(i => n(l => l.filter(o => o.id !== i)), []);
  const toastCtx = m.useMemo(() => ({ show: r }), [r]);
  """ + gtm_part + """
  return a.jsxs(zp.Provider, {
    value: toastCtx,
    children: [
      e,
      a.jsx("div", {
        className: "fixed top-4 inset-x-3 sm:inset-x-auto sm:right-6 sm:w-96 z-[9999] flex flex-col gap-2.5 pointer-events-none transition-all duration-300",
        children: t.map(i => {
          const meta = getToastMeta(i);
          return a.jsxs("div", {
            className: "pointer-events-auto bg-white rounded-2xl shadow-xl shadow-slate-900/10 border border-slate-100/90 p-4 flex items-start gap-3.5 animate-slide-in relative overflow-hidden transition-all duration-200 hover:shadow-2xl",
            children: [
              a.jsx("div", { className: "absolute left-0 top-0 bottom-0 w-1.5 " + meta.styles.bar }),
              a.jsx("div", {
                className: "w-9 h-9 rounded-xl flex items-center justify-center shrink-0 " + meta.styles.badge + " font-bold shadow-sm",
                children: meta.category === "success"
                  ? a.jsx(Nt, { className: "w-5 h-5" })
                  : (meta.category === "warning" || meta.isSecurity)
                  ? a.jsx(Zw, { className: "w-5 h-5 text-amber-700" })
                  : meta.category === "error"
                  ? a.jsx(cn, { className: "w-5 h-5" })
                  : a.jsx(Iw, { className: "w-5 h-5" })
              }),
              a.jsxs("div", {
                className: "flex-1 min-w-0 pr-1",
                children: [
                  a.jsx("h4", { className: "text-[11px] font-bold uppercase tracking-wider mb-0.5 " + meta.styles.title, children: meta.title }),
                  a.jsx("p", { className: "text-xs font-semibold text-slate-700 leading-snug break-words", children: i.message })
                ]
              }),
              a.jsx("button", {
                onClick: () => s(i.id),
                className: "text-slate-400 hover:text-slate-600 transition-colors p-1 rounded-lg hover:bg-slate-100 cursor-pointer shrink-0 -mr-1 -mt-1",
                "aria-label": "Dismiss notification",
                children: a.jsx(Un, { className: "w-4 h-4" })
              })
            ]
          }, i.id);
        })
      })
    ]
  });
}
"""
    code = code[:idx_r1] + r1_new + code[idx_he:]
    print("ToastProvider (r1) replaced successfully!")

# 2. Replace bw (AuthProvider)
idx_bw = code.find("function bw({children:e}){")
idx_ae = code.find("function Ae(){", idx_bw)
if idx_bw != -1 and idx_ae != -1:
    print("Found bw from", idx_bw, "to", idx_ae)
    
    bw_new = """function bw({children:e}){
  const [t, n] = m.useState(null),
        [r, s] = m.useState(null),
        [i, l] = m.useState(null),
        [o, c] = m.useState(!0);

  const userRef = m.useRef(null);
  userRef.current = t;
  const profileRef = m.useRef(null);
  profileRef.current = i;

  const u = m.useCallback(async (w) => {
    if (!w) return;
    try {
      let p_data = null;
      try {
        const cached = localStorage.getItem("mlb_saved_profile_" + w);
        if (cached) p_data = JSON.parse(cached);
      } catch(e) {}
      try {
        const { data: j, error: f } = await L.from("profiles").select("*").eq("id", w).maybeSingle();
        if (!f && j) { p_data = { ...(p_data || {}), ...j }; }
      } catch(err) {}
      try {
        const { data: u_auth } = await L.auth.getUser();
        const u_email = u_auth?.user?.email;
        const u_name = u_auth?.user?.user_metadata?.name || u_email?.split('@')[0] || 'User';
        const isAdminUser = isUserAdmin({ email: u_email });
        if (!p_data) {
          p_data = { id: w, email: u_email, name: u_name, role: isAdminUser ? 'super_admin' : 'user', account_status: 'active', status: 'active', is_pro: isAdminUser, pro_status: isAdminUser ? 'active' : 'inactive', created_at: new Date().toISOString() };
          try { await L.from('profiles').upsert(p_data); } catch(err) {}
        } else {
          if (isAdminUser) {
            p_data = { ...p_data, role: 'super_admin', is_pro: !0, pro_status: 'active' };
            try { await L.from('profiles').update({ role: 'super_admin', is_pro: !0, pro_status: 'active' }).eq('id', w); } catch(err) {}
          }
        }
      } catch(e) {}
      if (p_data) {
        try {
          const statusOverrides = JSON.parse(localStorage.getItem("admin_status_overrides") || "{}");
          const sOverride = statusOverrides[p_data.id] || (p_data.email && (statusOverrides[p_data.email] || statusOverrides[p_data.email.toLowerCase().trim()]));
          if (sOverride) {
            p_data.account_status = sOverride;
            p_data.status = sOverride;
          }
          const proOverrides = JSON.parse(localStorage.getItem("admin_pro_overrides") || "{}");
          const pOverride = proOverrides[p_data.id] || (p_data.email && (proOverrides[p_data.email] || proOverrides[p_data.email.toLowerCase().trim()]));
          if (pOverride) {
            if (pOverride.is_pro !== undefined) p_data.is_pro = pOverride.is_pro;
            if (pOverride.pro_status !== undefined) p_data.pro_status = pOverride.pro_status;
            if (pOverride.pro_expires_at) p_data.pro_expires_at = pOverride.pro_expires_at;
            if (pOverride.approved_expiry_date) p_data.approved_expiry_date = pOverride.approved_expiry_date;
          }
        } catch(err) {}
        try { localStorage.setItem("mlb_saved_profile_" + w, JSON.stringify(p_data)); } catch(err) {}
      }
      l(prev => areDataEqual(prev, p_data) ? prev : p_data);
    } catch(err) {
      console.warn('Profile fetch failure:', err);
    }
  }, []);

  const d = m.useCallback(async () => {
    if (userRef.current) await u(userRef.current.id);
  }, [u]);

  m.useEffect(() => {
    let isMounted = true;
    const safetyTimer = setTimeout(() => {
      if (isMounted) c(false);
    }, 1200);

    try {
      L.auth.getSession().then(({ data: j }) => {
        if (!isMounted) return;
        var f, g;
        s(j.session);
        const newUser = ((f = j.session) == null ? void 0 : f.user) ?? null;
        n(prev => areDataEqual(prev, newUser) ? prev : newUser);
        if ((g = j.session) != null && g.user) {
          u(j.session.user.id).catch(() => {}).finally(() => { if (isMounted) c(false); });
        } else {
          if (isMounted) c(false);
        }
      }).catch(err => {
        console.warn('getSession error:', err);
        if (isMounted) c(false);
      });
    } catch(err) {
      if (isMounted) c(false);
    }

    let unsub = null;
    try {
      const { data: w } = L.auth.onAuthStateChange((j, f) => {
        if (!isMounted) return;
        s(f);
        const newUser = (f == null ? void 0 : f.user) ?? null;
        n(prev => areDataEqual(prev, newUser) ? prev : newUser);
        if (f != null && f.user) {
          u(f.user.id).catch(() => {});
        } else {
          l(null);
        }
      });
      unsub = w?.subscription?.unsubscribe;
    } catch(err) {}

    const handleProfileSync = () => {
      if (userRef.current) u(userRef.current.id).catch(() => {});
    };
    window.addEventListener("user_profile_updated", handleProfileSync);
    window.addEventListener("user_status_changed", handleProfileSync);
    window.addEventListener("recharge_status_updated", handleProfileSync);

    return () => {
      isMounted = false;
      clearTimeout(safetyTimer);
      if (unsub) unsub();
      window.removeEventListener("user_profile_updated", handleProfileSync);
      window.removeEventListener("user_status_changed", handleProfileSync);
      window.removeEventListener("recharge_status_updated", handleProfileSync);
    };
  }, [u]);

  const h = m.useCallback(async (w, j, f) => {
    try {
      const { error: g } = await L.auth.signUp({ email: w, password: j, options: { data: { name: f } } });
      return { error: (g == null ? void 0 : g.message) ?? null };
    } catch(e) {
      return { error: e.message || 'Sign up failed' };
    }
  }, []);

  const p = m.useCallback(async (w, j) => {
    try {
      const { error: f } = await L.auth.signInWithPassword({ email: w, password: j });
      return { error: (f == null ? void 0 : f.message) ?? null };
    } catch(e) {
      return { error: e.message || 'Sign in failed' };
    }
  }, []);

  const v = m.useCallback(async () => {
    try { await L.auth.signOut(); } catch(e) {}
    l(null);
  }, []);

  const x = m.useCallback(async (w) => {
    try {
      const { error: j } = await L.auth.resetPasswordForEmail(w);
      return { error: (j == null ? void 0 : j.message) ?? null };
    } catch(e) {
      return { error: e.message || 'Reset failed' };
    }
  }, []);

  const userId = t?.id;
  const proExpiry = i?.pro_expires_at || i?.approved_expiry_date;
  m.useEffect(function() {
    if (userId && profileRef.current) {
      try { checkProExpiryNotifications(userRef.current, profileRef.current); } catch(e) {}
      const interval = setInterval(function() {
        if (typeof document !== "undefined" && document.hidden) return;
        try { checkProExpiryNotifications(userRef.current, profileRef.current); } catch(e) {}
      }, 3600000);
      return function() { clearInterval(interval); };
    }
  }, [userId, proExpiry]);

  const authCtxVal = m.useMemo(() => ({
    user: t,
    session: r,
    profile: i,
    loading: o,
    signUp: h,
    signIn: p,
    signOut: v,
    resetPassword: x,
    refreshProfile: d
  }), [t, r, i, o, h, p, v, x, d]);

  return a.jsx(Tp.Provider, { value: authCtxVal, children: e });
}
"""
    code = code[:idx_bw] + bw_new + code[idx_ae:]
    print("AuthProvider (bw) replaced successfully!")

# 3. Optimize Home (W1)
idx_w1 = code.find("function W1(){")
idx_w1_end = code.find("function Z1(", idx_w1)
if idx_w1_end == -1:
    idx_w1_end = code.find("function lj(", idx_w1)
if idx_w1 != -1 and idx_w1_end != -1:
    w1_old = code[idx_w1:idx_w1_end]
    print("Found W1 from", idx_w1, "to", idx_w1_end)
    
    # Let find the JSX portion in w1_old
    idx_return = w1_old.find("return a.jsxs(\"div\",{className:\"min-h-screen")
    jsx_portion = w1_old[idx_return:]
    
    w1_new = """function W1(){
  const { user: e, profile: t } = Ae(),
        n = he(),
        r = ke(),
        [s, i] = m.useState(""),
        [l, o] = m.useState(""),
        [c, u] = m.useState("All Locations"),
        [d, h] = m.useState(""),
        [p, v] = m.useState([]),
        [x, w] = m.useState([]),
        [j, f] = m.useState([]),
        [g, y] = m.useState([]),
        [_, k] = m.useState(!0),
        [S, b] = m.useState(!1),
        [N, I] = m.useState(new Set),
        [P, K] = m.useState(0),
        [ls, setLs] = m.useState("");

  const toastRef = m.useRef(n);
  toastRef.current = n;

  // Debounced search
  m.useEffect(() => {
    const E = setTimeout(() => o(s), 300);
    return () => clearTimeout(E);
  }, [s]);

  // Categories, locations, banners (Fetch only once on mount)
  m.useEffect(() => {
    Ac().then(res => w(prev => areDataEqual(prev, res) ? prev : res)).catch(() => {});
    $c().then(res => f(prev => areDataEqual(prev, res) ? prev : res)).catch(() => {});
    p1().then(res => y(prev => areDataEqual(prev, res) ? prev : res)).catch(() => {});
  }, []);

  const hasLoadedListingsRef = m.useRef(false);
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
  }, [userId, O]);

  m.useEffect(() => {
    if (g.length > 1) {
      const E = setInterval(() => {
        if (typeof document !== "undefined" && document.hidden) return;
        K(z => (z + 1) % g.length);
      }, 5000);
      return () => clearInterval(E);
    }
  }, [g.length]);

  const M = async (E) => {
    if (!e) {
      n.show("Please sign in to save favorites", "info");
      r("/auth");
      return;
    }
    const z = N.has(E);
    I(fe => {
      const C = new Set(fe);
      z ? C.delete(E) : C.add(E);
      return C;
    });
    try {
      z ? await Ea(e.id, E) : await Ca(e.id, E);
    } catch {
      n.show("Failed to update favorite", "error");
      I(fe => {
        const C = new Set(fe);
        z ? C.add(E) : C.delete(E);
        return C;
      });
    }
  };

  const G = m.useMemo(() => p.filter(E => E.is_featured).slice(0, 4), [p]);
  const recentOnly = m.useMemo(() => {
    if (l) return p;
    return p.filter(E => !E.is_featured);
  }, [p, l]);

  """ + jsx_portion + "\n"

    code = code[:idx_w1] + w1_new + code[idx_w1_end:]
    print("W1 Home component replaced successfully!")

with open("bundle.js", "w") as f:
    f.write(code)

print("Saved updated bundle.js")
