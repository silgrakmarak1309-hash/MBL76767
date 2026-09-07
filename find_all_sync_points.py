with open("bundle.js", "r") as f:
    code = f.read()

import re

print("1. Functions calling xd:")
for m in re.finditer(r'\bxd\(', code):
    pos = m.start()
    print(f"xd at {pos}:", code[max(0, pos-50):min(len(code), pos+150)])

print("\n2. Functions calling j1:")
for m in re.finditer(r'\bj1\(', code):
    pos = m.start()
    print(f"j1 at {pos}:", code[max(0, pos-50):min(len(code), pos+150)])

print("\n3. Functions calling wd:")
for m in re.finditer(r'\bwd\(', code):
    pos = m.start()
    print(f"wd at {pos}:", code[max(0, pos-50):min(len(code), pos+150)])

print("\n4. Functions calling k1 or S1:")
for m in re.finditer(r'\b(k1|S1)\(', code):
    pos = m.start()
    print(f"k1/S1 at {pos}:", code[max(0, pos-50):min(len(code), pos+150)])

