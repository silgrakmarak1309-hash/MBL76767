with open("bundle.js", "r") as f:
    code = f.read()

idx_pj = code.find("function pj(")
idx_pj_return = code.find("return a.jsxs(\"div\", {", idx_pj)
print("=== pj handlers code ===")
print(code[idx_pj:idx_pj_return])

