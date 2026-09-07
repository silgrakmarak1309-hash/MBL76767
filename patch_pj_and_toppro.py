with open("bundle.js", "r") as f:
    code = f.read()

# 1. Patch TopProRequestsView
idx_tp = code.find("function TopProRequestsView(")
idx_pj = code.find("function pj(", idx_tp)
tp_code = code[idx_tp:idx_pj]

# Find handleReject in tp_code
idx_rej = tp_code.find("const handleReject = async () => {")
idx_rej_end = tp_code.find("if (r) return a.jsx(", idx_rej)

tp_handlers_addition = """const handleReject = async () => {
    if (!o) return;
    p(!0);
    try {
      const reason = u.trim() || "Top PRO request declined by admin";
      await _1(o, reason);
      try {
        const overrides = JSON.parse(localStorage.getItem("recharge_status_overrides") || "{}");
        overrides[o] = { status: "rejected", rejection_reason: reason };
        const activeReq = proReqs.find(f => f.id === o);
        if (activeReq && activeReq.utr) overrides[activeReq.utr] = { status: "rejected", rejection_reason: reason };
        localStorage.setItem("recharge_status_overrides", JSON.stringify(overrides));
      } catch(e) {}
      e.show("Top PRO request rejected", "success");
      c(null);
      d("");
      await v();
      onRefresh && onRefresh();
    } catch(err) {
      e.show(err instanceof Error ? err.message : "Failed", "error");
    } finally {
      p(!1);
    }
  };
  const handleUnapprove = async (req) => {
    p(!0);
    try {
      await unapproveRecharge(req.id);
      if (req.listing_id) {
        await xd(req.listing_id, "active", !1);
      }
      e.show("Top PRO request unapproved & listing unfeatured", "success");
      await v();
      onRefresh && onRefresh();
    } catch(err) {
      e.show(err instanceof Error ? err.message : "Failed to unapprove", "error");
    } finally {
      p(!1);
    }
  };
  const handleResetToPending = async (req) => {
    p(!0);
    try {
      await unapproveRecharge(req.id);
      e.show("Top PRO request reset to Pending", "success");
      await v();
      onRefresh && onRefresh();
    } catch(err) {
      e.show(err instanceof Error ? err.message : "Failed to reset", "error");
    } finally {
      p(!1);
    }
  };
  """

tp_code = tp_code[:idx_rej] + tp_handlers_addition + tp_code[idx_rej_end:]

# Replace approved & rejected render in TopProRequestsView
old_tp_approved = 'req.status === "approved" && a.jsx("div", { className: "p-2 rounded-lg bg-green-50 text-green-700 text-xs font-bold text-center border border-green-200", children: "✓ Approved & Top PRO Active on Marketplace" })'
new_tp_approved = '''req.status === "approved" && a.jsxs("div", { className: "space-y-2 pt-1 border-t border-gray-100", children: [
          a.jsx("div", { className: "p-2 rounded-lg bg-green-50 text-green-700 text-xs font-bold text-center border border-green-200", children: "✓ Approved & Top PRO Active on Marketplace" }),
          a.jsxs("div", { className: "flex gap-2", children: [
            a.jsxs("button", { onClick: () => handleUnapprove(req), disabled: h, className: "btn-outline text-xs flex-1 py-1.5 font-bold flex items-center justify-center gap-1 border-amber-300 text-amber-800 hover:bg-amber-50", children: [a.jsx("span", { children: "↺" }), " Unapprove / Remove Top PRO"] }),
            a.jsxs("button", { onClick: () => { c(req.id); d(""); }, disabled: h, className: "btn-danger text-xs px-3 py-1.5 font-bold flex items-center justify-center gap-1", children: [a.jsx("span", { children: "✕" }), " Reject"] })
          ] })
        ] })'''

old_tp_rejected = 'req.status === "rejected" && a.jsxs("div", { className: "p-2 rounded-lg bg-red-50 text-red-700 text-xs text-center border border-red-200", children: [a.jsx("span", { className: "font-bold", children: "Rejected: " }), req.rejection_reason || "Declined by Admin"] })'
new_tp_rejected = '''req.status === "rejected" && a.jsxs("div", { className: "space-y-2 pt-1 border-t border-gray-100", children: [
          a.jsxs("div", { className: "p-2 rounded-lg bg-red-50 text-red-700 text-xs text-center border border-red-200", children: [a.jsx("span", { className: "font-bold", children: "Rejected: " }), req.rejection_reason || "Declined by Admin"] }),
          a.jsxs("div", { className: "flex gap-2", children: [
            a.jsxs("button", { onClick: () => handleApprove(req), disabled: h, className: "btn-primary text-xs flex-1 py-1.5 font-bold bg-emerald-600 hover:bg-emerald-700 text-white flex items-center justify-center gap-1", children: [a.jsx("span", { children: "✓" }), " Approve & Set Top PRO"] }),
            a.jsxs("button", { onClick: () => handleResetToPending(req), disabled: h, className: "btn-outline text-xs px-3 py-1.5 font-bold flex items-center justify-center gap-1", children: [a.jsx("span", { children: "↺" }), " Reset to Pending"] })
          ] })
        ] })'''

tp_code = tp_code.replace(old_tp_approved, new_tp_approved)
tp_code = tp_code.replace(old_tp_rejected, new_tp_rejected)

code = code[:idx_tp] + tp_code + code[idx_pj:]

print("TopProRequestsView patched!")

# 2. Patch pj (Monthly Plan Payment Requests)
idx_pj = code.find("function pj(")
idx_vj = code.find("function vj(", idx_pj)
pj_code = code[idx_pj:idx_vj]

# Find handleReject in pj_code
idx_rej_pj = pj_code.find("const handleReject = async () => {")
idx_rej_pj_end = pj_code.find("const copyUtr =", idx_rej_pj)

pj_handlers_addition = """const handleReject = async () => {
    if (!rejectTargetId) return;
    setIsProcessing(!0);
    try {
      const reason = rejectReason.trim() || "Monthly plan payment verification failed / invalid UTR";
      await _1(rejectTargetId, reason);
      try {
        const overrides = JSON.parse(localStorage.getItem("recharge_status_overrides") || "{}");
        overrides[rejectTargetId] = { status: "rejected", rejection_reason: reason };
        const activeReq = monthlyReqs.find(f => f.id === rejectTargetId);
        if (activeReq && activeReq.utr) overrides[activeReq.utr] = { status: "rejected", rejection_reason: reason };
        localStorage.setItem("recharge_status_overrides", JSON.stringify(overrides));
      } catch(e) {}
      toast.show("Monthly plan request rejected", "success");
      setRejectTargetId(null);
      setRejectReason("");
      await loadData(!1);
      onRefresh && onRefresh();
    } catch(err) {
      toast.show(err instanceof Error ? err.message : "Failed to reject request", "error");
    } finally {
      setIsProcessing(!1);
    }
  };
  const handleUnapprove = async (req) => {
    setIsProcessing(!0);
    try {
      await unapproveRecharge(req.id);
      toast.show("Plan unapproved & reset to Pending", "success");
      await loadData(!1);
      onRefresh && onRefresh();
    } catch(err) {
      toast.show(err instanceof Error ? err.message : "Failed to unapprove request", "error");
    } finally {
      setIsProcessing(!1);
    }
  };
  const handleResetToPending = async (req) => {
    setIsProcessing(!0);
    try {
      await unapproveRecharge(req.id);
      toast.show("Request reset to Pending", "success");
      await loadData(!1);
      onRefresh && onRefresh();
    } catch(err) {
      toast.show(err instanceof Error ? err.message : "Failed to reset request", "error");
    } finally {
      setIsProcessing(!1);
    }
  };
  """

pj_code = pj_code[:idx_rej_pj] + pj_handlers_addition + pj_code[idx_rej_pj_end:]

# Replace isApproved & isRejected render in pj
old_pj_approved = '''isApproved && a.jsxs("div", {
                className: "p-2 rounded-lg bg-emerald-50 text-emerald-800 text-xs font-semibold text-center border border-emerald-200 flex items-center justify-center gap-1",
                children: [
                  a.jsx("span", { children: "👑" }),
                  " Approved & PRO Member Badge Active",
                  req.approved_expiry_date ? " (Valid until " + fr(req.approved_expiry_date) + ")" : ""
                ]
              })'''

new_pj_approved = '''isApproved && a.jsxs("div", {
                className: "space-y-2 pt-1 border-t border-gray-100",
                children: [
                  a.jsxs("div", {
                    className: "p-2 rounded-lg bg-emerald-50 text-emerald-800 text-xs font-semibold text-center border border-emerald-200 flex items-center justify-center gap-1",
                    children: [
                      a.jsx("span", { children: "👑" }),
                      " Approved & PRO Member Badge Active",
                      req.approved_expiry_date ? " (Valid until " + fr(req.approved_expiry_date) + ")" : ""
                    ]
                  }),
                  a.jsxs("div", {
                    className: "flex gap-2",
                    children: [
                      a.jsxs("button", {
                        onClick: () => handleUnapprove(req),
                        disabled: isProcessing,
                        className: "btn-outline text-xs flex-1 py-1.5 font-bold flex items-center justify-center gap-1 border-amber-300 text-amber-800 hover:bg-amber-50",
                        children: [a.jsx("span", { children: "↺" }), " Unapprove (Set Pending)"]
                      }),
                      a.jsxs("button", {
                        onClick: () => { setRejectTargetId(req.id); setRejectReason(""); },
                        disabled: isProcessing,
                        className: "btn-danger text-xs px-3 py-1.5 font-bold flex items-center justify-center gap-1",
                        children: [a.jsx("span", { children: "✕" }), " Revoke & Reject"]
                      })
                    ]
                  })
                ]
              })'''

old_pj_rejected = '''isRejected && a.jsxs("div", {
                className: "p-2 rounded-lg bg-red-50 text-red-700 text-xs text-center border border-red-200",
                children: [
                  a.jsx("span", { className: "font-bold", children: "Rejected: " }),
                  req.rejection_reason || "Declined by Admin"
                ]
              })'''

new_pj_rejected = '''isRejected && a.jsxs("div", {
                className: "space-y-2 pt-1 border-t border-gray-100",
                children: [
                  a.jsxs("div", {
                    className: "p-2 rounded-lg bg-red-50 text-red-700 text-xs text-center border border-red-200",
                    children: [
                      a.jsx("span", { className: "font-bold", children: "Rejected: " }),
                      req.rejection_reason || "Declined by Admin"
                    ]
                  }),
                  a.jsxs("div", {
                    className: "flex gap-2",
                    children: [
                      a.jsxs("button", {
                        onClick: () => handleApprove(req),
                        disabled: isProcessing,
                        className: "btn-primary text-xs flex-1 py-1.5 font-bold bg-emerald-600 hover:bg-emerald-700 text-white flex items-center justify-center gap-1",
                        children: [a.jsx("span", { children: "✓" }), " Approve & Activate PRO"]
                      }),
                      a.jsxs("button", {
                        onClick: () => handleResetToPending(req),
                        disabled: isProcessing,
                        className: "btn-outline text-xs px-3 py-1.5 font-bold flex items-center justify-center gap-1",
                        children: [a.jsx("span", { children: "↺" }), " Reset to Pending"]
                      })
                    ]
                  })
                ]
              })'''

pj_code = pj_code.replace(old_pj_approved, new_pj_approved)
pj_code = pj_code.replace(old_pj_rejected, new_pj_rejected)

code = code[:idx_pj] + pj_code + code[idx_vj:]

print("pj patched!")

with open("bundle.js", "w") as f:
    f.write(code)

print("All patched and written successfully!")

