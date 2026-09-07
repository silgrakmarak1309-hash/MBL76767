with open("bundle.js", "r") as f:
    code = f.read()

import re

# Let's find Gp definition and usage
idx = code.find("async function Gp(")
if idx != -1:
    print("Gp found at", idx)
    print(code[idx:idx+1500])

# Let's search where listings are fetched for home/dashboard (e.g. status === "active" or getActiveListings)
print("\n--- Searches for Gp ---")
for m in re.finditer(r'Gp\(', code):
    print("Gp call at", m.start(), ":", code[max(0, m.start()-50):min(len(code), m.end()+100)])

