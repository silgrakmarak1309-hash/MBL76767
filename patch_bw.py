with open('bundle.js', 'r') as f:
    code = f.read()

p_bw = code.find('function bw({children:e}){')
p_ae = code.find('function Ae()', p_bw)

new_bw = """function bw({children:e}){
  const [t,n] = m.useState(null),
        [r,s] = m.useState(null),
        [i,l] = m.useState(null),
        [o,c] = m.useState(!0),
        userRef = m.useRef(null),
        u = m.useCallback(async w => {
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
                p_data = {
                  id: w,
                  email: u_email,
                  name: u_name,
                  role: isAdminUser ? 'super_admin' : 'user',
                  account_status: 'active',
                  status: 'active',
                  is_pro: isAdminUser,
                  pro_status: isAdminUser ? 'active' : 'inactive',
                  created_at: new Date().toISOString()
                };
                try { await L.from('profiles').upsert(p_data); } catch(err) {}
              } else {
                if (isAdminUser) {
                  p_data = { ...p_data, role: 'super_admin', is_pro: !0, pro_status: 'active' };
                  try { await L.from('profiles').update({ role: 'super_admin', is_pro: !0, pro_status: 'active' }).eq('id', w); } catch(err) {}
                }
              }
            } catch(e) {}

            if (p_data) {
              // 1. Prioritize Cloud Sync as single source of truth
              try {
                const syncState = await getCloudSyncState(true);
                const userOverrides = syncState.userStatusOverrides || {};
                const cleanEmail = (p_data.email || "").trim().toLowerCase();
                const uCloud = userOverrides[p_data.id] || (cleanEmail ? userOverrides[cleanEmail] : null);
                if (uCloud) {
                  if (uCloud.account_status !== undefined) p_data.account_status = uCloud.account_status;
                  if (uCloud.status !== undefined) p_data.status = uCloud.status;
                  if (uCloud.is_pro !== undefined) p_data.is_pro = uCloud.is_pro;
                  if (uCloud.pro_status !== undefined) p_data.pro_status = uCloud.pro_status;
                  if (uCloud.pro_expires_at) p_data.pro_expires_at = uCloud.pro_expires_at;
                  if (uCloud.approved_expiry_date) p_data.approved_expiry_date = uCloud.approved_expiry_date;
                }
              } catch(err) {}

              try { localStorage.setItem("mlb_saved_profile_" + w, JSON.stringify(p_data)); } catch(err) {}
            }
            l(p_data);
          } catch(err) {
            console.warn('Profile fetch failure:', err);
          }
        }, []),
        d = m.useCallback(async () => {
          const currentUser = userRef.current;
          currentUser && await u(currentUser.id);
        }, [u]);

  m.useEffect(() => {
    let isMounted = true;
    const safetyTimer = setTimeout(() => {
      if (isMounted) c(false);
    }, 1500);

    try {
      L.auth.getSession().then(({ data: j }) => {
        if (!isMounted) return;
        var f, g;
        s(j.session);
        userRef.current = ((f = j.session) == null ? void 0 : f.user) ?? null;
        n(userRef.current);
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
        userRef.current = (f == null ? void 0 : f.user) ?? null;
        n(userRef.current);
        if (f != null && f.user) {
          u(f.user.id).catch(() => {});
        } else {
          l(null);
        }
      });
      unsub = w?.subscription?.unsubscribe;
    } catch(err) {}

    const handleProfileSync = () => {
      const currentUser = userRef.current;
      if (currentUser) u(currentUser.id).catch(() => {});
    };

    // Auto-poll user status from cloud every 6 seconds
    const pollInterval = setInterval(() => {
      if (typeof document !== "undefined" && document.hidden) return;
      const currentUser = userRef.current;
      if (currentUser) u(currentUser.id).catch(() => {});
    }, 6000);

    window.addEventListener("user_profile_updated", handleProfileSync);
    window.addEventListener("user_status_changed", handleProfileSync);
    window.addEventListener("user_status_updated", handleProfileSync);
    window.addEventListener("recharge_status_updated", handleProfileSync);
    window.addEventListener("storage", handleProfileSync);
    window.addEventListener("focus", handleProfileSync);
    document.addEventListener("visibilitychange", handleProfileSync);

    return () => {
      isMounted = false;
      clearTimeout(safetyTimer);
      clearInterval(pollInterval);
      if (unsub) unsub();
      window.removeEventListener("user_profile_updated", handleProfileSync);
      window.removeEventListener("user_status_changed", handleProfileSync);
      window.removeEventListener("user_status_updated", handleProfileSync);
      window.removeEventListener("recharge_status_updated", handleProfileSync);
      window.removeEventListener("storage", handleProfileSync);
      window.removeEventListener("focus", handleProfileSync);
      document.removeEventListener("visibilitychange", handleProfileSync);
    };
  }, [u]);

  const h = async (w, j, f) => {
    try {
      const { error: g } = await L.auth.signUp({ email: w, password: j, options: { data: { name: f } } });
      return { error: (g == null ? void 0 : g.message) ?? null };
    } catch(e) {
      return { error: e.message || 'Sign up failed' };
    }
  },
  p = async (w, j) => {
    try {
      const { error: f } = await L.auth.signInWithPassword({ email: w, password: j });
      return { error: (f == null ? void 0 : f.message) ?? null };
    } catch(e) {
      return { error: e.message || 'Sign in failed' };
    }
  },
  v = async () => {
    try { await L.auth.signOut(); } catch(e) {}
    l(null);
  },
  x = async w => {
    try {
      const { error: j } = await L.auth.resetPasswordForEmail(w);
      return { error: (j == null ? void 0 : j.message) ?? null };
    } catch(e) {
      return { error: e.message || 'Reset failed' };
    }
  };

  m.useEffect(function() {
    if (t && i) {
      try { checkProExpiryNotifications(t, i); } catch(e) {}
      const interval = setInterval(function() {
        try { checkProExpiryNotifications(t, i); } catch(e) {}
      }, 3600000);
      return function() { clearInterval(interval); };
    }
  }, [t, i]);

  return a.jsx(Tp.Provider, { value: { user: t, session: r, profile: i, loading: o, signUp: h, signIn: p, signOut: v, resetPassword: x, refreshProfile: d }, children: e });
}
"""

code = code[:p_bw] + new_bw + code[p_ae:]
with open('bundle.js', 'w') as f:
    f.write(code)
print("Updated AuthProvider bw successfully!")
