with open("bundle.js", "r") as f:
    code = f.read()

idx_j1 = code.find("async function j1(")
idx_xd = code.find("async function xd(", idx_j1)
print(f"j1 bounds: {idx_j1} to {idx_xd}, length: {idx_xd - idx_j1}")

new_j1_block = """async function j1(e, optListingId, optFeatured){
  let reqObj = null;
  try {
    const list = await Jp();
    reqObj = list.find(r => r && (r.id === e || r.utr === e));
  } catch(err) {}
  const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
  const durationDays = reqObj?.plan?.duration_days || (Number(reqObj?.amount) >= 300 ? 365 : Number(reqObj?.amount) >= 180 ? 180 : Number(reqObj?.amount) >= 115 ? 90 : 30);
  const expDate = new Date(Date.now() + durationDays * 86400000).toISOString();
  
  try {
    if (isUUID(e)) {
      await L.from("recharge_requests").update({
        status: "approved",
        approved_expiry_date: expDate,
        rejection_reason: null,
        reviewed_at: new Date().toISOString()
      }).eq("id", e);
    }
  } catch(err) {}

  try {
    let tUser = null;
    try { const { data: t } = await L.auth.getUser(); if (t && t.user) tUser = t.user; } catch(err) {}
    const adminUid = (tUser?.id && isUUID(tUser.id)) ? tUser.id : (reqObj?.user_id && isUUID(reqObj.user_id) ? reqObj.user_id : "54d69b2e-76f7-410d-84fc-af00f7101786");
    const statusPayload = {
      req_id: e,
      utr: reqObj?.utr || "",
      status: "approved",
      approved_expiry_date: expDate,
      rejection_reason: null,
      user_id: reqObj?.user_id || "",
      user_email: reqObj?.user_email || "",
      is_top_pro: Boolean(reqObj?.is_top_pro || optFeatured),
      listing_id: reqObj?.listing_id || optListingId || "",
      reviewed_at: new Date().toISOString()
    };
    await L.from("listings").insert({
      user_id: adminUid,
      title: "[SYS_RECHARGE_STATUS]",
      category_id: "3ed03846-ea53-4f52-9db5-17550b75f3f2",
      location_id: "02ef9e15-c49f-459e-916c-2432e90dd230",
      price: 0,
      condition: "new",
      description: JSON.stringify(statusPayload),
      phone: "9876543210",
      whatsapp: "9876543210",
      images: [],
      status: "active",
      is_featured: false
    });
  } catch(err) {
    console.warn("Cloud status insert error:", err);
  }

  try {
    const overrides = JSON.parse(localStorage.getItem("recharge_status_overrides") || "{}");
    overrides[e] = { status: "approved", approved_expiry_date: expDate, rejection_reason: null };
    if (reqObj && reqObj.utr) overrides[reqObj.utr] = { status: "approved", approved_expiry_date: expDate, rejection_reason: null };
    localStorage.setItem("recharge_status_overrides", JSON.stringify(overrides));
  } catch(err) {}

  try {
    const local = JSON.parse(localStorage.getItem("all_recharge_requests") || "[]");
    local.forEach(r => {
      if (r && (r.id === e || (reqObj && reqObj.utr && r.utr === reqObj.utr))) {
        r.status = "approved";
        r.approved_expiry_date = expDate;
        r.rejection_reason = null;
        if (!reqObj) reqObj = r;
      }
    });
    localStorage.setItem("all_recharge_requests", JSON.stringify(local));
  } catch(err) {}

  try {
    const customLocal = JSON.parse(localStorage.getItem("custom_recharge_requests") || "[]");
    customLocal.forEach(r => {
      if (r && (r.id === e || (reqObj && reqObj.utr && r.utr === reqObj.utr))) {
        r.status = "approved";
        r.approved_expiry_date = expDate;
        r.rejection_reason = null;
        if (!reqObj) reqObj = r;
      }
    });
    localStorage.setItem("custom_recharge_requests", JSON.stringify(customLocal));
  } catch(err) {}

  // Handle Listing Feature / Top PRO if applicable
  try {
    const targetListingId = optListingId || reqObj?.listing_id;
    if (targetListingId) {
      await xd(targetListingId, "active", !0);
    } else if (reqObj?.listing_title) {
      try {
        const { data: matched } = await L.from("listings").select("id").ilike("title", reqObj.listing_title);
        if (matched && matched.length > 0) {
          for (const ml of matched) {
            await xd(ml.id, "active", !0);
          }
        }
      } catch(err2) {}
    }
  } catch(err) {}

  // Handle User PRO activation
  const uId = reqObj?.user_id;
  const uEmail = reqObj?.user_email || reqObj?.user?.email;
  const pData = {
    is_pro: true,
    pro_status: "active",
    pro_expires_at: expDate,
    pro_expiry_at: expDate,
    approved_expiry_date: expDate
  };

  try {
    if (uId && isUUID(uId)) {
      await L.from("profiles").update(pData).eq("id", uId);
    }
    if (uEmail) {
      await L.from("profiles").update(pData).eq("email", uEmail);
    }
  } catch(err) {}

  try {
    const localProList = JSON.parse(localStorage.getItem("admin_pro_overrides") || "{}");
    if (uId) localProList[uId] = pData;
    if (uEmail) {
      localProList[uEmail] = pData;
      localProList[uEmail.toLowerCase().trim()] = pData;
    }
    localStorage.setItem("admin_pro_overrides", JSON.stringify(localProList));
    localStorage.setItem("pro_status_overrides", JSON.stringify(localProList));
  } catch(err) {}

  try {
    const pMap = {};
    if (uId) pMap[uId] = pData;
    if (uEmail) {
      pMap[uEmail] = pData;
      pMap[uEmail.toLowerCase().trim()] = pData;
    }
    await syncCloudConfig({ user_pro_overrides: pMap });
  } catch(err) {}

  try {
    const isTopPro = Boolean(reqObj?.is_top_pro || reqObj?.plan_id === "plan_single_top_pro" || Number(reqObj?.amount) === 30 || Number(reqObj?.amount) === 10 || Number(reqObj?.amount) === 20 || optFeatured || reqObj?.listing_title || reqObj?.listing_id);
    const txItem = {
      id: "tx_" + (reqObj?.id || Date.now()),
      user_id: reqObj?.user_id || "user",
      user_name: reqObj?.user_name || reqObj?.user?.name || (reqObj?.user_email ? reqObj.user_email.split("@")[0] : "User"),
      user_email: reqObj?.user_email || reqObj?.user?.email || "",
      amount: reqObj?.amount || (isTopPro ? 30 : 112.5),
      type: isTopPro ? "top_pro_boost" : "pro_membership",
      description: isTopPro ? ("⭐ Top PRO Boost: " + (reqObj?.listing_title || "Ad Post") + " (UTR: " + (reqObj?.utr || "Verified") + ")") : ("👑 Monthly PRO Plan (UTR: " + (reqObj?.utr || "Verified") + ")"),
      status: "completed",
      created_at: reqObj?.submitted_at || new Date().toISOString()
    };
    const txList = JSON.parse(localStorage.getItem("user_transactions") || "[]");
    txList.unshift(txItem);
    localStorage.setItem("user_transactions", JSON.stringify(txList));
    if (reqObj?.user_id) {
      sendNotification(reqObj.user_id, isTopPro ? "⭐ Top PRO Boost Approved!" : "👑 Monthly PRO Plan Activated!", isTopPro ? ("Aapka listing '" + (reqObj.listing_title || "Ad") + "' Top PRO me feature ho gaya hai!") : "Aapka Monthly PRO plan approve ho gaya hai aur badge active hai.", isTopPro ? "boost_approved" : "recharge_approved");
    }
  } catch(err) {}

  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "approved" } }));
      window.dispatchEvent(new CustomEvent("user_profile_updated", { detail: { id: uId, email: uEmail, is_pro: !0, pro_status: "active", pro_expires_at: expDate } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}

async function _1(e, t){
  let reqObj = null;
  try {
    const list = await Jp();
    reqObj = list.find(r => r && (r.id === e || r.utr === e));
  } catch(err) {}
  const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
  try{await L.rpc("reject_recharge",{p_request_id:e,p_reason:t});}catch(err){}
  try{if(isUUID(e)) await L.from("recharge_requests").update({status:"rejected",rejection_reason:t,approved_expiry_date:null}).eq("id",e);}catch(err){}
  
  try {
    let tUser = null;
    try { const { data: usr } = await L.auth.getUser(); if (usr && usr.user) tUser = usr.user; } catch(err) {}
    const adminUid = (tUser?.id && isUUID(tUser.id)) ? tUser.id : (reqObj?.user_id && isUUID(reqObj.user_id) ? reqObj.user_id : "54d69b2e-76f7-410d-84fc-af00f7101786");
    const statusPayload = {
      req_id: e,
      utr: reqObj?.utr || "",
      status: "rejected",
      rejection_reason: t,
      approved_expiry_date: null,
      user_id: reqObj?.user_id || "",
      user_email: reqObj?.user_email || "",
      reviewed_at: new Date().toISOString()
    };
    await L.from("listings").insert({
      user_id: adminUid,
      title: "[SYS_RECHARGE_STATUS]",
      category_id: "3ed03846-ea53-4f52-9db5-17550b75f3f2",
      location_id: "02ef9e15-c49f-459e-916c-2432e90dd230",
      price: 0,
      condition: "new",
      description: JSON.stringify(statusPayload),
      phone: "9876543210",
      whatsapp: "9876543210",
      images: [],
      status: "active",
      is_featured: false
    });
  } catch(err) {
    console.warn("Cloud status reject insert error:", err);
  }

  try{
    const overrides=JSON.parse(localStorage.getItem("recharge_status_overrides")||"{}");
    overrides[e]={status:"rejected",rejection_reason:t,approved_expiry_date:null};
    if(reqObj&&reqObj.utr) overrides[reqObj.utr]={status:"rejected",rejection_reason:t,approved_expiry_date:null};
    localStorage.setItem("recharge_status_overrides",JSON.stringify(overrides));
  }catch(err){}

  try{
    const local=JSON.parse(localStorage.getItem("all_recharge_requests")||"[]");
    local.forEach(r=>{if(r&&(r.id===e||(reqObj&&reqObj.utr&&r.utr===reqObj.utr))){r.status="rejected";r.rejection_reason=t;r.approved_expiry_date=null;}});
    localStorage.setItem("all_recharge_requests",JSON.stringify(local));
  }catch(err){}

  try{
    const customLocal=JSON.parse(localStorage.getItem("custom_recharge_requests")||"[]");
    customLocal.forEach(r=>{if(r&&(r.id===e||(reqObj&&reqObj.utr&&r.utr===reqObj.utr))){r.status="rejected";r.rejection_reason=t;r.approved_expiry_date=null;}});
    localStorage.setItem("custom_recharge_requests",JSON.stringify(customLocal));
  }catch(err){}

  // If Top PRO, remove featured
  try {
    const targetListingId = reqObj?.listing_id;
    if (targetListingId) {
      await xd(targetListingId, "active", !1);
    }
  } catch(err) {}

  // Deactivate PRO for user if it was a monthly plan
  const uId = reqObj?.user_id;
  const uEmail = reqObj?.user_email || reqObj?.user?.email;
  const pData = {
    is_pro: false,
    pro_status: "inactive",
    pro_expires_at: null,
    pro_expiry_at: null,
    approved_expiry_date: null
  };

  try {
    if (uId && isUUID(uId)) {
      await L.from("profiles").update(pData).eq("id", uId);
    }
    if (uEmail) {
      await L.from("profiles").update(pData).eq("email", uEmail);
    }
  } catch(err) {}

  try {
    const localProList = JSON.parse(localStorage.getItem("admin_pro_overrides") || "{}");
    if (uId) localProList[uId] = pData;
    if (uEmail) {
      localProList[uEmail] = pData;
      localProList[uEmail.toLowerCase().trim()] = pData;
    }
    localStorage.setItem("admin_pro_overrides", JSON.stringify(localProList));
    localStorage.setItem("pro_status_overrides", JSON.stringify(localProList));
  } catch(err) {}

  try {
    const pMap = {};
    if (uId) pMap[uId] = pData;
    if (uEmail) {
      pMap[uEmail] = pData;
      pMap[uEmail.toLowerCase().trim()] = pData;
    }
    await syncCloudConfig({ user_pro_overrides: pMap });
  } catch(err) {}

  try {
    if (reqObj?.user_id) {
      sendNotification(reqObj.user_id, "❌ Request Rejected", "Aapka request reject ho gaya hai: " + (t || "Verification failed"), "recharge_rejected");
    }
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "rejected", reason: t } }));
      window.dispatchEvent(new CustomEvent("user_profile_updated", { detail: { id: uId, email: uEmail, is_pro: !1, pro_status: "inactive" } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}

async function unapproveRecharge(e) {
  let reqObj = null;
  try {
    const list = await Jp();
    reqObj = list.find(r => r && (r.id === e || r.utr === e));
  } catch(err) {}
  const isUUID = str => typeof str === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
  
  try {
    if (isUUID(e)) {
      await L.from("recharge_requests").update({
        status: "pending",
        approved_expiry_date: null,
        rejection_reason: null,
        reviewed_at: new Date().toISOString()
      }).eq("id", e);
    }
  } catch(err) {}

  try {
    let tUser = null;
    try { const { data: usr } = await L.auth.getUser(); if (usr && usr.user) tUser = usr.user; } catch(err) {}
    const adminUid = (tUser?.id && isUUID(tUser.id)) ? tUser.id : (reqObj?.user_id && isUUID(reqObj.user_id) ? reqObj.user_id : "54d69b2e-76f7-410d-84fc-af00f7101786");
    const statusPayload = {
      req_id: e,
      utr: reqObj?.utr || "",
      status: "pending",
      approved_expiry_date: null,
      rejection_reason: null,
      user_id: reqObj?.user_id || "",
      user_email: reqObj?.user_email || "",
      reviewed_at: new Date().toISOString()
    };
    await L.from("listings").insert({
      user_id: adminUid,
      title: "[SYS_RECHARGE_STATUS]",
      category_id: "3ed03846-ea53-4f52-9db5-17550b75f3f2",
      location_id: "02ef9e15-c49f-459e-916c-2432e90dd230",
      price: 0,
      condition: "new",
      description: JSON.stringify(statusPayload),
      phone: "9876543210",
      whatsapp: "9876543210",
      images: [],
      status: "active",
      is_featured: false
    });
  } catch(err) {}

  try {
    const overrides = JSON.parse(localStorage.getItem("recharge_status_overrides") || "{}");
    overrides[e] = { status: "pending", approved_expiry_date: null, rejection_reason: null };
    if (reqObj && reqObj.utr) overrides[reqObj.utr] = { status: "pending", approved_expiry_date: null, rejection_reason: null };
    localStorage.setItem("recharge_status_overrides", JSON.stringify(overrides));
  } catch(err) {}

  try {
    const local = JSON.parse(localStorage.getItem("all_recharge_requests") || "[]");
    local.forEach(r => {
      if (r && (r.id === e || (reqObj && reqObj.utr && r.utr === reqObj.utr))) {
        r.status = "pending";
        r.approved_expiry_date = null;
        r.rejection_reason = null;
      }
    });
    localStorage.setItem("all_recharge_requests", JSON.stringify(local));
  } catch(err) {}

  try {
    const customLocal = JSON.parse(localStorage.getItem("custom_recharge_requests") || "[]");
    customLocal.forEach(r => {
      if (r && (r.id === e || (reqObj && reqObj.utr && r.utr === reqObj.utr))) {
        r.status = "pending";
        r.approved_expiry_date = null;
        r.rejection_reason = null;
      }
    });
    localStorage.setItem("custom_recharge_requests", JSON.stringify(customLocal));
  } catch(err) {}

  // Remove featured from listing if Top PRO
  try {
    const targetListingId = reqObj?.listing_id;
    if (targetListingId) {
      await xd(targetListingId, "active", !1);
    }
  } catch(err) {}

  // Deactivate PRO
  const uId = reqObj?.user_id;
  const uEmail = reqObj?.user_email || reqObj?.user?.email;
  const pData = {
    is_pro: false,
    pro_status: "inactive",
    pro_expires_at: null,
    pro_expiry_at: null,
    approved_expiry_date: null
  };

  try {
    if (uId && isUUID(uId)) {
      await L.from("profiles").update(pData).eq("id", uId);
    }
    if (uEmail) {
      await L.from("profiles").update(pData).eq("email", uEmail);
    }
  } catch(err) {}

  try {
    const localProList = JSON.parse(localStorage.getItem("admin_pro_overrides") || "{}");
    if (uId) localProList[uId] = pData;
    if (uEmail) {
      localProList[uEmail] = pData;
      localProList[uEmail.toLowerCase().trim()] = pData;
    }
    localStorage.setItem("admin_pro_overrides", JSON.stringify(localProList));
    localStorage.setItem("pro_status_overrides", JSON.stringify(localProList));
  } catch(err) {}

  try {
    const pMap = {};
    if (uId) pMap[uId] = pData;
    if (uEmail) {
      pMap[uEmail] = pData;
      pMap[uEmail.toLowerCase().trim()] = pData;
    }
    await syncCloudConfig({ user_pro_overrides: pMap });
  } catch(err) {}

  try {
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("recharge_status_updated", { detail: { id: e, status: "pending" } }));
      window.dispatchEvent(new CustomEvent("user_profile_updated", { detail: { id: uId, email: uEmail, is_pro: !1, pro_status: "inactive" } }));
      window.dispatchEvent(new Event("storage"));
    }
  } catch(err) {}
}
"""

code = code[:idx_j1] + new_j1_block + code[idx_xd:]

with open("bundle.js", "w") as f:
    f.write(code)

print("j1 and _1 and unapproveRecharge updated successfully!")
