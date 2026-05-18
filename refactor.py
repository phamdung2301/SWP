import os
import re
import shutil

src_dir = os.path.join("src", "main", "java", "com", "liteflow")
layers = ["controller", "service", "dao", "model", "dto", "mapper", "validator"]

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

class_mapping = {} # old_full_name -> new_full_name
file_moves = [] # (old_path, new_path)
package_updates = {} # old_path -> new_package

def get_module(subpackage):
    return mapping.get(subpackage, "core")

for layer in layers:
    layer_dir = os.path.join(src_dir, layer)
    if not os.path.exists(layer_dir):
        continue
    for root, dirs, files in os.walk(layer_dir):
        for file in files:
            if file.endswith(".java"):
                old_path = os.path.join(root, file)
                rel_path = os.path.relpath(old_path, layer_dir)
                parts = rel_path.replace("\\", "/").split("/")
                
                if len(parts) > 1:
                    subpkg = parts[0]
                    module = get_module(subpkg)
                    # new package: com.liteflow.modules.{module}.{layer}
                    # Wait, if there are sub-sub packages, we might want to keep them.
                    # For simplicity, flatten to module.layer for now, or module.layer.subpkg
                    # Actually, keeping it as module.layer.subpkg might be better, or just module.layer.
                    new_pkg = f"com.liteflow.modules.{module}.{layer}"
                    if len(parts) > 2:
                        extra_pkgs = ".".join(parts[1:-1])
                        new_pkg += "." + extra_pkgs
                    
                    old_pkg = f"com.liteflow.{layer}.{subpkg}"
                    if len(parts) > 2:
                        old_pkg += "." + ".".join(parts[1:-1])
                        
                    new_dir = os.path.join(src_dir, "modules", module, layer, *parts[1:-1])
                else:
                    # directly under layer (e.g. com.liteflow.service.SomeService)
                    module = "core"
                    new_pkg = f"com.liteflow.modules.{module}.{layer}"
                    old_pkg = f"com.liteflow.{layer}"
                    new_dir = os.path.join(src_dir, "modules", module, layer)

                new_path = os.path.join(new_dir, file)
                
                class_name = file.replace(".java", "")
                old_full_name = f"{old_pkg}.{class_name}"
                new_full_name = f"{new_pkg}.{class_name}"
                
                class_mapping[old_full_name] = new_full_name
                file_moves.append((old_path, new_path))
                package_updates[old_path] = new_pkg

# Now we also need to update JSPs, XMLs, etc.
# But first, let's execute the move and rewrite Java files

# First, read all java files and update contents
for old_path, new_path in file_moves:
    with open(old_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # update package
    old_pkg_match = re.search(r'package\s+([a-zA-Z0-9_\.]+)\s*;', content)
    if old_pkg_match:
        content = re.sub(r'package\s+([a-zA-Z0-9_\.]+)\s*;', f"package {package_updates[old_path]};", content)
    
    # update imports
    for old_full, new_full in class_mapping.items():
        # import com.liteflow.model.auth.User; -> import com.liteflow.modules.auth.model.User;
        content = content.replace(f"import {old_full};", f"import {new_full};")
        # Also handle fully qualified names in code
        content = content.replace(old_full, new_full)
        
    os.makedirs(os.path.dirname(new_path), exist_ok=True)
    with open(new_path, 'w', encoding='utf-8') as f:
        f.write(content)

# We also need to remove the old files, but let's just leave them or delete the old layer dirs
for layer in layers:
    layer_dir = os.path.join(src_dir, layer)
    if os.path.exists(layer_dir):
        shutil.rmtree(layer_dir)

# Now update references in all other files (JSP, XML)
# webapp_dir = os.path.join("src", "main", "webapp")
# We will do this in a second step if needed.

print(f"Moved {len(file_moves)} files to modular structure.")
