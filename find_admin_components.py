with open("bundle.js", "r") as f:
    code = f.read()

idx_tabs = code.find('r === "dashboard"')
print("Admin tab router area:")
print(code[idx_tabs-100:idx_tabs+1200])

