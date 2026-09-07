with open("bundle.js", "r") as f:
    code = f.read()

import re

# Find Admin components/tabs
matches = re.findall(r'function ([A-Za-z0-9_]+)\([^\)]*\)\s*\{[^\}]*(?:Recharge Requests|User Management|All Listings|Manage Listings)', code)
print("Matches with tab titles:", matches)

# Find where "Approve" button or recharge approval is rendered
for m in re.finditer(r'Approve', code):
    pos = m.start()
    if pos < 700000: continue
    print(f"=== 'Approve' at {pos} ===")
    print(code[max(0, pos-150):min(len(code), pos+250)])
    print("-" * 50)

