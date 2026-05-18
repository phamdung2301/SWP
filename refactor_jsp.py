import os
import re

mapping = {
    "auth": "auth",
    "inventory": "inventory",
    "procurement": "procurement",
    "employee": "hr",
    "payroll": "hr",
    "timesheet": "hr",
    "schedule": "hr",
    "notice": "hr",
    "alert": "core",
    "ai": "analytics",
    "analytics": "analytics",
    "dashboard": "analytics",
    "report": "analytics",
    "cashier": "reservation",
    "payment": "reservation",
    "sales": "reservation",
    "admin": "core",
    "api": "core",
    "external": "core",
}

def get_module(subpackage):
    return mapping.get(subpackage, "core")

webapp_dir = os.path.join("src", "main", "webapp")
files_to_check = []

for root, dirs, files in os.walk(webapp_dir):
    for file in files:
        if file.endswith(('.jsp', '.html', '.xml')):
            files_to_check.append(os.path.join(root, file))

# We will also check src/main/resources if there are xmls
resources_dir = os.path.join("src", "main", "resources")
if os.path.exists(resources_dir):
    for root, dirs, files in os.walk(resources_dir):
        for file in files:
            if file.endswith('.xml'):
                files_to_check.append(os.path.join(root, file))

# And pom.xml
files_to_check.append("pom.xml")

def replacer(match):
    # e.g., com.liteflow.model.auth.User
    layer = match.group(1)
    subpkg = match.group(2)
    # wait, sometimes it's com.liteflow.model.User (no subpkg)
    if layer in ["controller", "service", "dao", "model", "dto", "mapper", "validator", "security", "filter", "listener", "job", "util"]:
        if subpkg and subpkg[0].islower():
            module = get_module(subpkg)
            return f"com.liteflow.modules.{module}.{layer}.{subpkg}"
        else:
            # it's a class name directly under layer: com.liteflow.model.User
            return f"com.liteflow.modules.core.{layer}.{subpkg}"
    return match.group(0)

# The pattern looks for com.liteflow.<layer>.<subpkg_or_class>
pattern = re.compile(r'com\.liteflow\.([a-z]+)\.([a-zA-Z0-9_]+)')

for filepath in files_to_check:
    if not os.path.exists(filepath): continue
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        new_content = pattern.sub(replacer, content)
        
        # apply a second pass for class names that were in subpackages
        # e.g. com.liteflow.modules.auth.model.auth.User -> we want com.liteflow.modules.auth.model.User
        # actually, our previous refactor script didn't keep the subpkg name if it was moved to module root.
        # Let's fix that pattern specifically:
        # com.liteflow.modules.auth.model.auth.User -> com.liteflow.modules.auth.model.User
        for mod, mod_val in mapping.items():
            new_content = re.sub(rf'com\.liteflow\.modules\.{mod_val}\.([a-z]+)\.{mod}\.', rf'com.liteflow.modules.{mod_val}.\1.', new_content)
        
        if new_content != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Updated {filepath}")
    except Exception as e:
        pass

print("JSP/XML update complete.")
