with open("bundle.js", "r") as f:
    code = f.read()

import re

for m in re.finditer(r'setInterval\(', code):
    pos = m.start()
    print(f"=== setInterval at {pos} ===")
    print(code[max(0, pos-80):min(len(code), pos+250)])

