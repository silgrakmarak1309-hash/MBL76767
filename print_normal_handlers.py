with open("bundle.js", "r") as f:
    code = f.read()

import re

idx = code.find("function NormalPostRequestsView(")
idx_end = code.find("function TopProRequestsView(", idx)
normal_code = code[idx:idx_end]

for m in re.finditer(r'(?:handle[A-Za-z0-9_]+|publish|reject|approve|unpublish)', normal_code):
    print("Match:", m.group(0))
    start = max(0, m.start() - 40)
    end = min(len(normal_code), m.end() + 200)
    print(normal_code[start:end])
    print("-" * 50)

