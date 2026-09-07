with open("bundle.js", "r") as f:
    code = f.read()

idx_j1 = code.find("async function j1(")
print("j1 at:", idx_j1)
print(code[idx_j1:idx_j1+1800])

