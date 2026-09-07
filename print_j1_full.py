with open("bundle.js", "r") as f:
    code = f.read()

idx_j1 = code.find("async function j1(")
idx_next = code.find("async function _1(", idx_j1)
print("=== j1 ===")
print(code[idx_j1:idx_next])

idx_after = code.find("async function w1(", idx_next)
print("=== _1 ===")
print(code[idx_next:idx_after])

