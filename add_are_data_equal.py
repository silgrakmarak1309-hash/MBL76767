with open("bundle.js", "r") as f:
    code = f.read()

helper = """function areDataEqual(a, b) {
  if (a === b) return true;
  if (!a || !b) return false;
  if (Array.isArray(a) && Array.isArray(b)) {
    if (a.length !== b.length) return false;
    try {
      return JSON.stringify(a) === JSON.stringify(b);
    } catch {
      return false;
    }
  }
  try {
    return JSON.stringify(a) === JSON.stringify(b);
  } catch {
    return false;
  }
}
"""

if "function areDataEqual" not in code:
    idx = code.find("function W1(){")
    if idx != -1:
        code = code[:idx] + helper + "\n" + code[idx:]
        print("Added areDataEqual helper before function W1")
    else:
        print("function W1 not found!")
else:
    print("function areDataEqual already exists in code")

with open("bundle.js", "w") as f:
    f.write(code)

with open("./public/bundle.js", "w") as f:
    f.write(code)

with open("./dist/bundle.js", "w") as f:
    f.write(code)

