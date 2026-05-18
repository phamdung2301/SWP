import os
import re

src_dir = os.path.join("src", "main", "java", "com", "liteflow", "modules")

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

def replacer(match):
    # e.g., com.liteflow.model.auth
    layer = match.group(1)
    subpkg = match.group(2)
    if layer in ["controller", "service", "dao", "model", "dto", "mapper", "validator", "security", "filter", "listener", "job", "util"]:
        if subpkg and subpkg[0].islower():
            module = get_module(subpkg)
            return f"com.liteflow.modules.{module}.{layer}.{subpkg}"
        else:
            return f"com.liteflow.modules.core.{layer}.{subpkg}"
    return match.group(0)

pattern = re.compile(r'com\.liteflow\.([a-z]+)\.([a-zA-Z0-9_]+)')

count = 0
for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # fix imports like com.liteflow.model.auth.*
            new_content = pattern.sub(replacer, content)
            
            # fix any leftovers like com.liteflow.model.* (no subpackage)
            new_content = re.sub(r'com\.liteflow\.([a-z]+)\.\*', r'com.liteflow.modules.core.\1.*', new_content)
            
            # clean up any duplicate modules prefixes
            for mod, mod_val in mapping.items():
                new_content = re.sub(rf'com\.liteflow\.modules\.{mod_val}\.([a-z]+)\.{mod}\.', rf'com.liteflow.modules.{mod_val}.\1.', new_content)
                new_content = re.sub(rf'com\.liteflow\.modules\.{mod_val}\.([a-z]+)\.{mod}\*', rf'com.liteflow.modules.{mod_val}.\1.*', new_content)
            
            # some subpackages are just classes, but eplacer might have changed com.liteflow.model.User 
            # to com.liteflow.modules.core.model.User which is correct, since we moved it to core.
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                count += 1

print(f"Fixed imports in {count} files.")
