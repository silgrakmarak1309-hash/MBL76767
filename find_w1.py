with open("bundle.js", "r") as f:
    code = f.read()

import re

for m in re.finditer(r'function\s+W1\s*\(', code):
    idx = m.start()
    print("Found W1 at index:", idx)
    print(code[idx:idx+1500])

