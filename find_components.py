with open("bundle.js", "r") as f:
    code = f.read()

import re

# Find components with intervals or listeners or state loops
print("=== Checking all components in bundle.js ===")
patterns = [
    r'function ([A-Za-z0-9_$]+)\s*\([^)]*\)\s*\{[^}]*setInterval',
    r'function ([A-Za-z0-9_$]+)\s*\([^)]*\)\s*\{[^}]*addEventListener',
]

for p in patterns:
    for m in re.finditer(p, code):
        print("Match:", m.group(1))

