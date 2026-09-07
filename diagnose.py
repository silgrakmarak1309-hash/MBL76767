with open("bundle.js", "r") as f:
    code = f.read()

import re

print("Bundle length:", len(code))

# 1. Check Jp() - how recharge requests are fetched
idx_jp = code.find("async function Jp()")
idx_jp_end = code.find("async function", idx_jp + 20)
print("\n=== Jp() ===")
print(code[idx_jp:idx_jp_end][:1200])

# 2. Check how user submits recharge request (recharge form)
idx_recharge_submit = code.find("title: isTopPro ? \"[SYS_TOP_PRO_REQUEST]\"")
if idx_recharge_submit != -1:
    print("\n=== Recharge submit snippet ===")
    print(code[idx_recharge_submit-100:idx_recharge_submit+800])

# 3. Check j1() - how admin approves recharge request & activates PRO
idx_j1 = code.find("async function j1(")
idx_j1_end = code.find("async function", idx_j1 + 20)
print("\n=== j1() ===")
print(code[idx_j1:idx_j1_end][:1200])

# 4. Check user profile sync in AuthProvider (bw) and Profile page
idx_bw = code.find("function bw({children")
idx_bw_end = code.find("function Ae()", idx_bw)
print("\n=== bw (AuthProvider) ===")
print(code[idx_bw:idx_bw_end][:1200])

