with open("bundle.js", "r") as f:
    code = f.read()

# Let's find pj (RechargesView)
idx_pj = code.find("function pj(")
idx_mj = code.find("function mj(", idx_pj)
print("=== pj (Recharges View) ===")
print(code[idx_pj:idx_pj+2500])

# Let's find hj (Users View)
idx_hj = code.find("function hj(")
idx_fj = code.find("function fj(", idx_hj)
print("=== hj (Users View) ===")
print(code[idx_hj:idx_hj+2500])

# Let's find wj (Settings View)
idx_wj = code.find("function wj(")
print("=== wj (Settings View) ===")
print(code[idx_wj:idx_wj+2500])

