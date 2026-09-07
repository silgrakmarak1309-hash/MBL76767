with open("bundle.js", "r") as f:
    code = f.read()

import re

# Let's inspect pj approve & reject
idx_pj = code.find("function pj(")
idx_pj_end = code.find("function mj(", idx_pj)
print("=== pj (Recharges) ===")
print(code[idx_pj:idx_pj_end])

# Let's inspect TopProRequestsView approve & reject
idx_tp = code.find("function TopProRequestsView(")
idx_tp_end = code.find("function nj(", idx_tp)
print("=== TopProRequestsView ===")
print(code[idx_tp:idx_tp_end])

