with open("bundle.js", "r") as f:
    code = f.read()

code = code.replace("async async function Xp()", "async function Xp()")

with open("bundle.js", "w") as f:
    f.write(code)

print("Fixed double async!")
