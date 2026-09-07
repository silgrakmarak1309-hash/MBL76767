with open("bundle.js", "r") as f:
    code = f.read()

import re

# Find Oc component definition and usage
print("=== Searching for Oc in bundle.js ===")
for m in re.finditer(r'\bOc\b', code):
    start = max(0, m.start() - 60)
    end = min(len(code), m.start() + 100)
    print(f"at {m.start()}: {code[start:end]}")

# Find scroll listeners or IntersectionObserver
print("\n=== Searching for scroll / intersection in bundle.js ===")
for m in re.finditer(r'(scroll|IntersectionObserver|onScroll)', code):
    start = max(0, m.start() - 50)
    end = min(len(code), m.start() + 80)
    print(f"at {m.start()}: {code[start:end]}")

