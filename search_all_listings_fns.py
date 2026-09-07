with open("bundle.js", "r") as f:
    code = f.read()

import re
# Let us inspect the lines around Gp() definition
idx = code.find("async function Gp(")
print("--- Gp full function: ---")
print(code[idx:idx+3500])

