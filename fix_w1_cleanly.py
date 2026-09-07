with open("bundle.js", "r") as f:
    code = f.read()

# Locate W1 function
idx_start = code.find("function W1(){")
idx_end = code.find("function V1(){", idx_start)

old_w1 = code[idx_start:idx_end]

new_w1 = """function W1(){
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

  const hasLoadedRef = m.useRef(false);
  const O = m.useCallback(async (forceSkeleton = false) => {
    if (!hasLoadedRef.current || forceSkeleton) {
      k(true);
    }
    try {
      const E = await Vp({ search: l || void 0, locationId: d || void 0, limit: 50 });
      hasLoadedRef.current = true;
      v(prev => areDataEqual(prev, E) ? prev : E);
    } catch {
      if (toastRef.current && !hasLoadedRef.current) {
        toastRef.current.show("Failed to load listings", "error");
      }
    } finally {
      k(false);
    }
  }, [l, d]);

  // Initial load and search/filter change
  const prevFilterRef = m.useRef({ l: null, d: null });
  m.useEffect(() => {
    const filterChanged = prevFilterRef.current.l !== null && (prevFilterRef.current.l !== l || prevFilterRef.current.d !== d);
    prevFilterRef.current = { l, d };
    O(filterChanged);
  }, [O, l, d]);

  // User favorites & sync
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
  }, [userId]);

  // Banner carousel auto rotate
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

  return a.jsxs("div", {
    className: "min-h-screen pb-20 md:pb-8",
    children: [
      a.jsx(Wp, { search: s, setSearch: i, selectedLocation: c, onLocationClick: () => b(!0) }),
      a.jsxs("div", {
        className: "max-w-7xl mx-auto px-4 py-4",
        children: [
          g.length > 0 && a.jsxs("div", {
            className: "relative rounded-2xl overflow-hidden mb-4 bg-gradient-to-r from-slate-900 via-primary-950 to-indigo-950 aspect-[16/7] sm:aspect-[16/6] shadow-sm",
            children: [
              g.map((E, z) => a.jsxs("div", {
                className: `absolute inset-0 transition-opacity duration-500 ${z === P ? "opacity-100" : "opacity-0 pointer-events-none"}`,
                children: [
                  a.jsx("img", {
                    src: E.image_url || "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=1200&q=80",
                    alt: "",
                    onError: evt => { evt.currentTarget.src = "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=1200&q=80"; },
                    className: "w-full h-full object-cover"
                  }),
                  (E.title || E.description) && a.jsxs("div", {
                    className: "absolute inset-0 bg-gradient-to-r from-black/75 via-black/40 to-transparent flex flex-col justify-center px-6 sm:px-10",
                    children: [
                      E.tag && a.jsx("span", { className: "inline-block w-fit px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-primary-500/90 text-white mb-2 shadow-sm backdrop-blur-sm", children: E.tag }),
                      E.title && a.jsx("h2", { className: "text-white font-bold text-lg sm:text-2xl mb-1 drop-shadow-sm", children: E.title }),
                      (E.description || E.subtitle) && a.jsx("p", { className: "text-white/90 text-xs sm:text-sm max-w-md drop-shadow-sm leading-relaxed", children: E.description || E.subtitle })
                    ]
                  })
                ]
              }, E.id || z)),
              g.length > 1 && a.jsx("div", {
                className: "absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-1.5 z-10",
                children: g.map((E, z) => a.jsx("button", { onClick: () => K(z), className: `h-1.5 rounded-full transition-all ${z === P ? "w-6 bg-white shadow" : "w-1.5 bg-white/50"}` }, z))
              })
            ]
          }),
          a.jsxs("div", {
            className: "mb-6",
            children: [
              a.jsx("h2", { className: "text-sm font-semibold text-gray-800 mb-3", children: "Browse Categories" }),
              a.jsx("div", {
                className: "flex gap-3 overflow-x-auto no-scrollbar pb-2",
                children: x.map(E => {
                  const dsg = getCategoryDesign(E);
                  return a.jsxs("button", {
                    onClick: () => r(`/search?category=${E.id}`),
                    className: "flex flex-col items-center gap-2 shrink-0 w-[84px] group transition-all",
                    children: [
                      a.jsx("div", {
                        className: `w-14 h-14 rounded-2xl ${dsg.bg} ${dsg.border} border shadow-sm flex items-center justify-center ${dsg.iconColor} group-hover:shadow-md group-hover:scale-105 group-active:scale-95 transition-all duration-200 relative`,
                        children: renderCategoryIcon(E.icon || E.name, "w-7 h-7", "text-3xl")
                      }),
                      a.jsx("span", {
                        className: "text-xs font-semibold text-gray-700 text-center leading-tight line-clamp-1 group-hover:text-primary-600 transition-colors",
                        children: E.name
                      })
                    ]
                  }, E.id);
                })
              })
            ]
          }),
          a.jsx(RewardedProAdSection, { user: e, profile: t, showToast: n, navigate: r }),
          G.length > 0 && !l && a.jsxs("div", {
            className: "mb-6",
            children: [
              a.jsxs("div", {
                className: "flex items-center justify-between mb-3",
                children: [
                  a.jsx("h2", { className: "text-sm font-semibold text-gray-800", children: "⭐ Top PRO Listings" }),
                  a.jsxs("button", {
                    type: "button",
                    onClick: () => {
                      const el = document.getElementById("recent-listings-section");
                      if (el) el.scrollIntoView({ behavior: "smooth" });
                      else r("/search");
                    },
                    className: "text-xs font-semibold text-primary-700 bg-primary-50 hover:bg-primary-100 active:scale-95 px-2.5 py-1 rounded-full border border-primary-200 flex items-center gap-1 shadow-2xs transition-all cursor-pointer",
                    children: ["Recent Listings", a.jsx("span", { className: "text-xs leading-none", children: "↓" })]
                  })
                ]
              }),
              a.jsx("div", {
                className: "grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-4",
                children: G.map(E => a.jsx(ta, { listing: E, seller: E.seller, isFavorited: N.has(E.id), onFavoriteToggle: M }, E.id))
              })
            ]
          }),
          a.jsxs("div", {
            id: "recent-listings-section",
            className: "scroll-mt-4",
            children: [
              a.jsxs("div", {
                className: "flex items-center justify-between mb-3",
                children: [
                  a.jsx("h2", { className: "text-sm font-semibold text-gray-800", children: l ? `Results for "${l}"` : "Recent Listings" }),
                  a.jsxs("button", { onClick: () => r("/search"), className: "text-xs text-secondary-600 hover:underline flex items-center gap-0.5", children: ["See all ", a.jsx(Rc, { className: "w-3 h-3" })] })
                ]
              }),
              (_ && p.length === 0) ? a.jsx(Oc, {}) : recentOnly.length === 0 ? a.jsx(Te, { icon: a.jsx(Re, { className: "w-7 h-7" }), title: "No listings found", message: "Try adjusting your search or location filter." }) : a.jsx("div", { className: "grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3 md:gap-4", children: recentOnly.map(E => a.jsx(ta, { listing: E, seller: E.seller, isFavorited: N.has(E.id), onFavoriteToggle: M }, E.id)) })
            ]
          })
        ]
      }),
      a.jsx(ze, {
        open: S,
        onClose: () => { b(!1); setLs(""); },
        title: "Select Location",
        children: a.jsxs("div", {
          className: "p-4 space-y-3",
          children: [
            a.jsxs("div", {
              className: "relative",
              children: [
                a.jsx(Rs, { className: "w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" }),
                a.jsx("input", { type: "text", value: ls, onChange: E => setLs(E.target.value), placeholder: "Search city or location...", className: "input pl-9 pr-8 text-sm w-full py-2.5 bg-gray-50 focus:bg-white" }),
                ls && a.jsx("button", { type: "button", onClick: () => setLs(""), className: "absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 p-1", children: a.jsx(cn, { className: "w-4 h-4" }) })
              ]
            }),
            a.jsxs("div", {
              className: "max-h-[60vh] overflow-y-auto space-y-1 pr-1",
              children: [
                (!ls || "all locations".includes(ls.toLowerCase())) && a.jsxs("button", {
                  onClick: () => { u("All Locations"); h(""); b(!1); setLs(""); },
                  className: `flex items-center gap-3 w-full px-4 py-3 rounded-xl text-sm font-medium transition-colors ${d === "" ? "bg-primary-50 text-primary-700 font-semibold" : "hover:bg-gray-50 text-gray-700"}`,
                  children: [a.jsx(yt, { className: `w-4 h-4 ${d === "" ? "text-primary-600" : "text-gray-400"}` }), " All Locations"]
                }),
                j.filter(E => !ls.trim() || E.name.toLowerCase().includes(ls.trim().toLowerCase()) || (E.state && E.state.toLowerCase().includes(ls.trim().toLowerCase()))).sort((A, B) => A.name.localeCompare(B.name)).map(E => a.jsxs("button", {
                  onClick: () => { u(E.name); h(E.id); b(!1); setLs(""); },
                  className: `flex items-center justify-between w-full px-4 py-3 rounded-xl text-sm font-medium transition-colors ${d === E.id ? "bg-primary-50 text-primary-700 font-semibold" : "hover:bg-gray-50 text-gray-700"}`,
                  children: [
                    a.jsxs("div", { className: "flex items-center gap-3", children: [a.jsx(yt, { className: `w-4 h-4 ${d === E.id ? "text-primary-600" : "text-gray-400"}` }), a.jsx("span", { children: E.name })] }),
                    E.state && a.jsx("span", { className: "text-xs text-gray-400 font-normal", children: E.state })
                  ]
                }, E.id)),
                j.filter(E => !ls.trim() || E.name.toLowerCase().includes(ls.trim().toLowerCase()) || (E.state && E.state.toLowerCase().includes(ls.trim().toLowerCase()))).length === 0 && (!ls || !"all locations".includes(ls.toLowerCase())) && a.jsxs("div", {
                  className: "py-8 text-center text-gray-500",
                  children: [
                    a.jsx(yt, { className: "w-8 h-8 mx-auto mb-2 text-gray-300" }),
                    a.jsx("p", { className: "text-sm font-medium", children: "No locations found" }),
                    a.jsx("p", { className: "text-xs text-gray-400 mt-0.5", children: `No match for "${ls}"` })
                  ]
                })
              ]
            })
          ]
        })
      })
    ]
  });
}"""

code = code[:idx_start] + new_w1 + "\n" + code[idx_end:]

# Update V1 search page condition
code = code.replace("M?a.jsx(Oc,{}):b.length===0?", "(M&&b.length===0)?a.jsx(Oc,{}):b.length===0?")
# Update rj favorites page condition
code = code.replace("i?a.jsx(Oc,{}):r.length===0?", "(i&&r.length===0)?a.jsx(Oc,{}):r.length===0?")

with open("bundle.js", "w") as f:
    f.write(code)

with open("./public/bundle.js", "w") as f:
    f.write(code)

if os.path.exists("./dist/bundle.js"):
    with open("./dist/bundle.js", "w") as f:
        f.write(code)

print("Replacement done!")
