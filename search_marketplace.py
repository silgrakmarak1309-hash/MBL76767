with open("bundle.js", "r") as f:
    code = f.read()

import re

# Let us find where getActiveListings or similar functions exist, or all functions in the app that fetch listings (like Wp, Zp, etc.)
# Let us search for ".from(\"listings\")" or functions returning listings
funcs = re.findall(r'async function [A-Za-z0-9_]+\([^)]*\)\s*\{[^}]*listings[^}]*\}', code)
print("Found listing functions count:", len(funcs))
for f in funcs[:10]:
    print("Function:", f[:200], "\n---")

