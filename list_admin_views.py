with open("bundle.js", "r") as f:
    code = f.read()

import re

names = ["dj", "hj", "fj", "pj", "mj", "gj", "yj", "vj", "xj", "wj", "TopProRequestsView", "NormalPostRequestsView"]

for n in names:
    idx = code.find(f"function {n}(")
    if idx != -1:
        # find some text / headers inside
        sample = code[idx:idx+800]
        # find text like "Plans", "Users", "Banners", etc.
        print(f"=== {n} at {idx} ===")
        # search for titles or h1/h2/h3
        titles = re.findall(r'children:\s*["\']([^"\']{3,40})["\']', sample)
        print("Titles in view:", titles[:5])

