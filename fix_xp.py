with open("bundle.js", "r") as f:
    code = f.read()

idx_xp = code.find("function Xp(){")
if idx_xp != -1:
    code = code[:idx_xp] + "async " + code[idx_xp:]
    with open("bundle.js", "w") as f:
        f.write(code)
    print("Fixed Xp to async function Xp")
else:
    print("Xp is already async or not matched")

