with open("bundle.js", "r") as f:
    code = f.read()

import re

# Find components that show full-screen or section loaders when loading state is true
# and check how they set loading state
components = [
    ("Home/Marketplace", 938000, 945000),
    ("Admin Normal Posts", 1088000, 1092000),
    ("Admin Top PRO Recharges", 1117000, 1121000),
    ("Admin Dashboard Layout", 1132000, 1136000),
    ("Admin Overview Stats", 1154000, 1157000),
    ("Admin Users Table", 1157500, 1162000),
    ("Admin Listings Table", 1177000, 1181000),
    ("Admin Monthly Plan Requests", 1200000, 1204000),
    ("Admin Transactions", 1227000, 1230000),
    ("Admin Banners", 1234000, 1237000),
    ("Admin Categories", 1240000, 1243000),
    ("Admin Locations", 1244000, 1247000),
    ("Admin Plans", 1250000, 1253000),
    ("Admin Settings", 1254000, 1257000),
]

for name, start, end in components:
    print(f"\n=== {name} ===")
    snippet = code[start:end]
    # Check intervals, listeners, and loading state toggles
    for line in snippet.split("\n")[:10]:
        print(line[:120])

