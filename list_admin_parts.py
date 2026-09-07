with open("bundle.js", "r") as f:
    code = f.read()

import re

# Find subcomponents before uj or within uj
idx_uj = code.find("function uj(")
# Search for functions defined between 1000000 and idx_uj+134000
for m in re.finditer(r'function\s+([A-Za-z0-9_]+)\s*\([^)]*\)\s*\{', code[1000000:idx_uj+135000]):
    fn_name = m.group(1)
    pos = 1000000 + m.start()
    print(f"Function {fn_name} at pos {pos}")

