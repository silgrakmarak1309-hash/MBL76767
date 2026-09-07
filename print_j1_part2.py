with open("bundle.js", "r") as f:
    code = f.read()

idx_j1 = code.find("async function j1(")
idx_w1 = code.find("async function w1(", idx_j1)
print(code[idx_j1:idx_w1])

