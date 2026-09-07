with open("bundle.js", "r") as f:
    code = f.read()

code = code.replace("return t}async \nfunction Z1", "return t;} \nasync function Z1")
code = code.replace("return t}async function Z1", "return t;} async function Z1")

with open("bundle.js", "w") as f:
    f.write(code)

