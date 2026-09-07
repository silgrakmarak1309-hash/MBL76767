with open("bundle.js", "r") as f:
    code = f.read()

import re

# Search for form submission / creation of listing
matches = [m.start() for m in re.finditer(r'title|category_id|price|images|phone|condition', code)]
# Let's search for "create_listing" or "add_listing" or where listings.insert happens
print("--- All .insert( calls ---")
for m in re.finditer(r'\.insert\(', code):
    snippet = code[max(0, m.start()-100):min(len(code), m.end()+300)]
    print(snippet)
    print("="*40)

