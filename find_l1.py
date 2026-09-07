with open("bundle.js", "r") as f:
    code = f.read()

import re
for m in re.finditer(r'function L1\(', code):
    print("Found L1 at", m.start())
