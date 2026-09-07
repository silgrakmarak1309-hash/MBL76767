with open("bundle.js", "r") as f:
    code = f.read()

import re
# Find all occurrences of from("listings")
for m in re.finditer(r'from\("listings"\)', code):
    start = max(0, m.start() - 200)
    end = min(len(code), m.end() + 300)
    print("=== Match at", m.start(), "===")
    print(code[start:end])
    print("\n")
