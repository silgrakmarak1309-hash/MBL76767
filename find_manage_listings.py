with open("bundle.js", "r") as f:
    code = f.read()

import re
pos = code.find("Manage Listings")
print("=== Manage Listings ===")
print(code[pos-400:pos+1500])

