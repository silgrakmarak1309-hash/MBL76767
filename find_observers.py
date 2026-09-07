with open("bundle.js", "r") as f:
    code = f.read()

import re

for m in re.finditer(r'(IntersectionObserver|ResizeObserver|MutationObserver|scroll|onScroll)', code):
    pos = m.start()
    if pos > 600000: # skip react-dom internals
        print(f"=== Match at {pos}: {m.group(0)} ===")
        print(code[max(0, pos-100):min(len(code), pos+200)])

