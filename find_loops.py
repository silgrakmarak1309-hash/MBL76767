with open("bundle.js", "r") as f:
    code = f.read()

import re
print("=== setInterval calls ===")
for m in re.finditer(r'setInterval\(', code):
    start = max(0, m.start() - 60)
    end = min(len(code), m.start() + 150)
    print("--- at", m.start(), "---")
    print(code[start:end])

print("\n=== addEventListener for storage/custom events ===")
for m in re.finditer(r'addEventListener\(["\'](storage|[a-zA-Z0-9_-]+)', code):
    start = max(0, m.start() - 40)
    end = min(len(code), m.start() + 120)
    print("--- at", m.start(), "---")
    print(code[start:end])

print("\n=== dispatchEvent calls ===")
for m in re.finditer(r'dispatchEvent\(', code):
    start = max(0, m.start() - 40)
    end = min(len(code), m.start() + 120)
    print("--- at", m.start(), "---")
    print(code[start:end])

