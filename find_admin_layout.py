with open("bundle.js", "r") as f:
    code = f.read()

idx = code.find("setPostPendingCount")
print("Found at", idx)
idx_fn = code.rfind("function ", 0, idx)
print("Function starts at", idx_fn)
print(code[idx_fn:idx_fn+2500])

