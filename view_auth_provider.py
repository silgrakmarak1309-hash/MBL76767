with open("bundle.js", "r") as f:
    code = f.read()

import re

# Find AuthProvider or where user profile is loaded
idx = code.find("function AuthProvider(")
if idx == -1:
    idx = code.find("function _p(") # let's search for createContext Tp or profile state
    if idx == -1:
        # search for where Tp.Provider is used
        idx = code.find(".Provider")

print("=== Auth Provider / context around", idx, "===")
print(code[idx:idx+2500])

