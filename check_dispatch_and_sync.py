with open("bundle.js", "r") as f:
    code = f.read()

import re

print("=== dispatchEvent calls ===")
for m in re.finditer(r'dispatchEvent\s*\(', code):
    start = max(0, m.start() - 100)
    end = min(len(code), m.end() + 100)
    print(m.start(), ":", code[start:end])
    print("-" * 50)

print("=== syncCloudConfig calls ===")
for m in re.finditer(r'syncCloudConfig\s*\(', code):
    start = max(0, m.start() - 100)
    end = min(len(code), m.end() + 100)
    print(m.start(), ":", code[start:end])
    print("-" * 50)

