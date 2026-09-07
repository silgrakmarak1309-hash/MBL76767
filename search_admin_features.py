with open("bundle.js", "r") as f:
    code = f.read()

import re

def search(name, pattern, max_len=300):
    print(f"=== SEARCH: {name} ===")
    matches = list(re.finditer(pattern, code))
    print(f"Found {len(matches)} matches")
    for m in matches[:5]:
        start = max(0, m.start() - 50)
        end = min(len(code), m.end() + max_len)
        print(f"[{m.start()}]:", code[start:end])
        print("-" * 40)

search("SYS_RECHARGE", r'SYS_RECHARGE')
search("SYS_TOP_PRO", r'SYS_TOP_PRO')
search("SYS_APP_CONFIG", r'SYS_APP_CONFIG')
search("Recharge Requests component/handler", r'RechargesView|recharge_requests|RechargeRequests')
search("Settings save handler", r'handleSaveSettings|saveSettings|admin_settings|app_config')
search("Admin Users View", r'UsersView|AdminUsers|UserManagement')

