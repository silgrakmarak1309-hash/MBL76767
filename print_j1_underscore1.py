with open("bundle.js", "r") as f:
    code = f.read()

import re

def print_fn(name):
    print(f"=== Function {name} ===")
    m = re.search(r'(?:async\s+)?function\s+' + name + r'\s*\(', code)
    if m:
        start = m.start()
        print(code[start:start+2500])
    else:
        print(f"{name} NOT FOUND")

print_fn("j1")
print_fn("_1")
print_fn("w1")

# Also let's check TopProRequestsView approve/reject
idx_tp = code.find("function TopProRequestsView(")
idx_tp_return = code.find("return a.jsxs(\"div\", {", idx_tp)
print("=== TopProRequestsView handlers ===")
print(code[idx_tp:idx_tp_return])

