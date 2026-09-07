with open("bundle.js", "r") as f:
    code = f.read()

import re

# Find top level functions in bundle.js that use React hooks
function_defs = list(re.finditer(r'(?:function\s+([A-Za-z0-9_$]+)|const\s+([A-Za-z0-9_$]+)\s*=\s*(?:m\.forwardRef|m\.memo|\([^)]*\)\s*=>))\s*\{', code))
print(f"Total function definitions found: {len(function_defs)}")

# Let's inspect functions after index 700000 (app code)
app_funcs = []
for i, m in enumerate(function_defs):
    if m.start() > 700000:
        name = m.group(1) or m.group(2)
        end_pos = min(len(code), function_defs[i+1].start() if i+1 < len(function_defs) else len(code))
        body = code[m.start():end_pos]
        if "useEffect" in body or "useState" in body:
            app_funcs.append((name, m.start(), body))

print(f"App components found with state/effects: {len(app_funcs)}")
for name, pos, body in app_funcs:
    has_interval = "setInterval" in body
    has_listener = "addEventListener" in body
    has_dispatch = "dispatchEvent" in body
    print(f"\nComponent: {name} (at {pos}) | interval={has_interval} listener={has_listener} dispatch={has_dispatch}")
    # print all useEffect in this body
    effects = list(re.finditer(r'm\.useEffect\s*\([^;]+(?:\}\s*\,\s*\[[^\]]*\])?\s*\)', body))
    for ef in effects:
        print("  Effect snippet:", ef.group(0)[:120], "...")

