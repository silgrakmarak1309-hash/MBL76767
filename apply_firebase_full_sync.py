import os

def patch_bundle():
    bundle_paths = ['public/bundle.js', 'bundle.js']
    
    for path in bundle_paths:
        if not os.path.exists(path):
            continue
        with open(path, 'r', encoding='utf-8') as f:
            code = f.read()

        # 1. Enhance c1 (Listing Creation) to save to FirebaseDB
        target_c1 = 'window.dispatchEvent(new CustomEvent("listing_created", { detail: created }));'
        c1_replacement = '''if (typeof window !== "undefined" && window.FirebaseDB && window.FirebaseDB.saveListing) {
          try { window.FirebaseDB.saveListing(created); } catch(err) { console.warn("[Firebase] Error saving listing:", err); }
        }
        window.dispatchEvent(new CustomEvent("listing_created", { detail: created }));'''
        if target_c1 in code and '[Firebase] Error saving listing' not in code:
            code = code.replace(target_c1, c1_replacement, 1)

        # 2. Enhance fetchAllListings to merge FirebaseDB listings
        target_fetch = 'if (postReqs && postReqs.length > 0) {'
        fetch_replacement = '''if (typeof window !== "undefined" && window.FirebaseDB && window.FirebaseDB.getListings) {
    try {
      const fbListings = await window.FirebaseDB.getListings();
      if (fbListings && fbListings.length > 0) {
        fbListings.forEach(function(fbItem) {
          if (!fbItem || !fbItem.id) return;
          const exIdx = list.findIndex(function(it) { return it && it.id === fbItem.id; });
          if (exIdx >= 0) {
            list[exIdx] = Object.assign({}, list[exIdx], fbItem);
          } else {
            list.unshift(fbItem);
          }
        });
      }
    } catch(err) {}
  }
  if (postReqs && postReqs.length > 0) {'''
        if target_fetch in code and 'window.FirebaseDB.getListings' not in code:
            code = code.replace(target_fetch, fetch_replacement, 1)

        # 3. Enhance Jp (Recharge & Top PRO Requests) to merge FirebaseDB requests
        target_jp = 'const mergedMap = new Map();'
        jp_replacement = '''if (typeof window !== "undefined" && window.FirebaseDB && window.FirebaseDB.getRechargeRequests) {
    try {
      const fbReqs = await window.FirebaseDB.getRechargeRequests();
      if (fbReqs && fbReqs.length > 0) {
        fbReqs.forEach(function(r) {
          if (r && (r.id || r.utr)) localList.unshift(r);
        });
      }
    } catch(err) {}
  }
  const mergedMap = new Map();'''
        if target_jp in code and 'window.FirebaseDB.getRechargeRequests' not in code:
            code = code.replace(target_jp, jp_replacement, 1)

        # 4. Enhance A1 (Save Settings) to write to FirebaseDB
        target_a1 = 'saved[e] = { key: e, value: cleanVal, is_public: n, updated_at: new Date().toISOString() };'
        a1_replacement = '''saved[e] = { key: e, value: cleanVal, is_public: n, updated_at: new Date().toISOString() };
    if (typeof window !== "undefined" && window.FirebaseDB && window.FirebaseDB.saveSettings) {
      try { window.FirebaseDB.saveSettings({ [e]: cleanVal }); } catch(err) {}
    }'''
        if target_a1 in code and 'window.FirebaseDB.saveSettings' not in code:
            code = code.replace(target_a1, a1_replacement, 1)

        # 5. Enhance x1 (Load Settings) to include FirebaseDB
        target_x1 = 'return Object.values(result);'
        x1_replacement = '''if (typeof window !== "undefined" && window.FirebaseDB && window.FirebaseDB.getSettings) {
    try {
      const fbSettings = await window.FirebaseDB.getSettings();
      if (fbSettings) {
        Object.keys(fbSettings).forEach(function(k) {
          if (fbSettings[k] !== undefined && fbSettings[k] !== null) {
            result[k] = { key: k, value: String(fbSettings[k]), is_public: true };
          }
        });
      }
    } catch(err) {}
  }
  return Object.values(result);'''
        if target_x1 in code and 'window.FirebaseDB.getSettings' not in code:
            code = code.replace(target_x1, x1_replacement, 1)

        # 6. Enhance j1 (Approve Request) to update FirebaseDB
        target_j1_end = 'window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "approved" } }));'
        j1_replacement = '''if (typeof window !== "undefined" && window.FirebaseDB) {
    try {
      if (window.FirebaseDB.updateRechargeStatus) {
        window.FirebaseDB.updateRechargeStatus(e, "approved", {
          approved_expiry_date: expDate,
          is_top_pro: isTopPro,
          plan_id: reqObj?.plan_id,
          plan_name: reqObj?.plan_name,
          user_id: uId,
          user_email: uEmail,
          amount: Number(reqObj?.amount) || (isTopPro ? 30 : 112.5)
        });
      }
      if (isTopPro && targetListingId && window.FirebaseDB.updateListing) {
        window.FirebaseDB.updateListing(targetListingId, { is_top_pro: true, is_featured: true });
      }
      if (window.FirebaseDB.recordTransaction) {
        window.FirebaseDB.recordTransaction({
          user_id: uId,
          user_email: uEmail,
          amount: Number(reqObj?.amount) || (isTopPro ? 30 : 112.5),
          type: isTopPro ? "top_pro_boost" : "pro_membership",
          description: isTopPro ? "Top PRO Boost (₹30) - Approved" : ("PRO Membership (" + durationDays + " Days) - Approved"),
          status: "approved",
          date: new Date().toISOString(),
          expiry_date: expDate,
          plan_name: isTopPro ? "Top PRO Boost" : (reqObj?.plan_name || (durationDays + " Days Plan"))
        });
      }
    } catch(fbErr) {
      console.warn("[Firebase] Error in j1 approval:", fbErr);
    }
  }
  window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "approved" } }));'''
        if target_j1_end in code and 'window.FirebaseDB.updateRechargeStatus' not in code:
            code = code.replace(target_j1_end, j1_replacement, 1)

        # 7. Enhance _1 (Reject Request) to update FirebaseDB
        target_1_end = 'window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "rejected", reason: t } }));'
        rej_replacement = '''if (typeof window !== "undefined" && window.FirebaseDB && window.FirebaseDB.updateRechargeStatus) {
    try {
      window.FirebaseDB.updateRechargeStatus(e, "rejected", {
        rejection_reason: t,
        user_id: reqObj?.user_id,
        user_email: reqObj?.user_email
      });
    } catch(fbErr) {}
  }
  window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "rejected", reason: t } }));'''
        if target_1_end in code and 'window.FirebaseDB.updateRechargeStatus(e, "rejected"' not in code:
            code = code.replace(target_1_end, rej_replacement, 1)

        # 8. Enhance user status change / k1
        target_k1 = 'window.dispatchEvent(new CustomEvent("user_profile_updated", { detail: { id: e, is_pro: !0, pro_status: "active" } }));'
        k1_replacement = '''if (typeof window !== "undefined" && window.FirebaseDB && window.FirebaseDB.updateUserStatus) {
    try {
      window.FirebaseDB.updateUserStatus(e, "active", "pro", {
        subscription_expiry: expiry,
        is_pro: true,
        pro_status: "active"
      });
    } catch(err) {}
  }
  window.dispatchEvent(new CustomEvent("user_profile_updated", { detail: { id: e, is_pro: !0, pro_status: "active" } }));'''
        if target_k1 in code and 'window.FirebaseDB.updateUserStatus' not in code:
            code = code.replace(target_k1, k1_replacement, 1)

        with open(path, 'w', encoding='utf-8') as f:
            f.write(code)
        print(f'Successfully patched {path} for Firebase Firestore!')

if __name__ == '__main__':
    patch_bundle()
