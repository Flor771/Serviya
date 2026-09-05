with open("backend/main.py", "r") as f:
    content = f.read()

admin_route = '''
@app.get("/admin")
@app.get("/admin.html")
def serve_admin_panel():
    if os.path.exists("admin.html"):
        return FileResponse("admin.html", media_type="text/html")
    if os.path.exists("public/admin.html"):
        return FileResponse("public/admin.html", media_type="text/html")
    return {"error": "admin.html not found"}
'''

if "/admin.html" not in content:
    # Insert before if os.path.exists("public/icons"):
    target = 'if os.path.exists("public/icons"):'
    new_content = content.replace(target, admin_route + "\n" + target)
    with open("backend/main.py", "w") as f:
        f.write(new_content)
    print("Updated main.py with /admin route")
else:
    print("Main.py already has /admin route")
