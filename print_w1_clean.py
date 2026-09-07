with open("bundle.js", "r") as f:
    code = f.read()

idx = code.find("function W1(){")
idx_end = code.find("function V1(){", idx)
print(f"W1 length: {idx_end - idx}")
print(code[idx:idx_end])

