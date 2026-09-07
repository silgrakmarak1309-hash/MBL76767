with open("bundle.js", "r") as f:
    code = f.read()

idx = code.find("function W1(){")
print(code[idx:idx+3500])

