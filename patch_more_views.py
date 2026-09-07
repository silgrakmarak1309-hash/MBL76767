with open("bundle.js", "r") as f:
    code = f.read()

# Patch MyAds (find "Failed to load your ads")
idx_myads = code.find("Failed to load your ads")
if idx_myads != -1:
    idx_start = code.rfind("function ", 0, idx_myads)
    idx_end = code.find("function ", idx_myads)
    myads_code = code[idx_start:idx_end]
    myads_code = myads_code.replace("s(await qp(e.id))", "const myAds = await qp(e.id); s(prev => areDataEqual(prev, myAds) ? prev : myAds)")
    code = code[:idx_start] + myads_code + code[idx_end:]
    print("MyAds patched!")

# Patch Notifications (cj)
idx_notif = code.find("function cj(){")
if idx_notif != -1:
    idx_notif_end = code.find("function ", idx_notif + 10)
    notif_code = code[idx_notif:idx_notif_end]
    notif_code = notif_code.replace("s(await w1(e.id));", "const myN = await w1(e.id); s(prev => areDataEqual(prev, myN) ? prev : myN);")
    code = code[:idx_notif] + notif_code + code[idx_notif_end:]
    print("User Notifications patched!")

# Patch Categories view (Admin)
idx_cat = code.find("function AdminCategories(")
if idx_cat == -1:
    idx_cat = code.find("Category updated")
if idx_cat != -1:
    idx_start = code.rfind("function ", 0, idx_cat)
    idx_end = code.find("function ", idx_cat)
    cat_code = code[idx_start:idx_end]
    cat_code = cat_code.replace("n(await g1())", "const cList = await g1(); n(prev => areDataEqual(prev, cList) ? prev : cList)")
    code = code[:idx_start] + cat_code + code[idx_end:]
    print("AdminCategories patched!")

with open("bundle.js", "w") as f:
    f.write(code)

print("Additional views patched!")

