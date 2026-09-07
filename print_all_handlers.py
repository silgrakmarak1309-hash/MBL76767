with open("bundle.js", "r") as f:
    code = f.read()

import re

def show_section(query, length=2000):
    idx = code.find(query)
    if idx != -1:
        print(f"=== Found '{query}' at {idx} ===")
        print(code[idx:idx+length])
    else:
        print(f"NOT FOUND: '{query}'")

show_section("async function Jp(")
show_section("async function g1(")
show_section("async function y1(")
show_section("async function v1(")
show_section("function TopProRequestsView(")
show_section("function NormalPostRequestsView(")

