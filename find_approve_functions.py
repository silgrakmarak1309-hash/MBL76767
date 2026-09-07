with open("bundle.js", "r") as f:
    code = f.read()

import re

# Find pj
idx_pj = code.find("function pj(")
idx_pj_end = code.find("function mj(", idx_pj)
print("=== pj (length) ===", idx_pj_end - idx_pj)
print("pj start:", code[idx_pj:idx_pj+1500])

# Find approve handlers in pj
for m in re.finditer(r'(?:handleApprove|handleReject|approve|reject|approveRecharge)', code[idx_pj:idx_pj_end]):
    start = idx_pj + max(0, m.start() - 50)
    end = idx_pj + min(idx_pj_end - idx_pj, m.end() + 200)
    print("--- pj match ---")
    print(code[start:end])

