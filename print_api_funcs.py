with open("bundle.js", "r") as f:
    code = f.read()

import re

def print_func(func_name):
    print(f"==================== {func_name} ====================")
    # search async function func_name or function func_name
    match = re.search(r'(?:async\s+)?function\s+' + func_name + r'\s*\(', code)
    if match:
        start = match.start()
        # find end of function or next 2000 chars
        print(code[start:start+2500])
    else:
        print(f"Function {func_name} NOT FOUND")

for fn in ["Jp", "g1", "y1", "v1", "k1", "S1", "wd", "x1", "A1", "Zp"]:
    print_func(fn)

