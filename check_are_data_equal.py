with open("bundle.js", "r") as f:
    code = f.read()

import re
matches = [m.start() for m in re.finditer(r'\bareDataEqual\b', code)]
print(f"Total occurrences of areDataEqual: {len(matches)}")
for pos in matches[:10]:
    print(f"Pos {pos}: {code[max(0, pos-40):min(len(code), pos+60)]}")

