with open("bundle.js", "r") as f:
    code = f.read()

print("Original code length:", len(code))

# Let's inspect where j1, _1, wd, k1, S1, v1, Xp, xj, wj, pj, TopProRequestsView are located
for name in ["async function j1(", "async function _1(", "async function wd(", "async function k1(", "async function S1(", "async function v1(", "function Xp(", "function xj(", "function wj(", "function pj(", "function TopProRequestsView("]:
    idx = code.find(name)
    print(f"{name} index: {idx}")

