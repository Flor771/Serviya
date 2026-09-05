from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from backend.config import settings
from backend.database import engine, Base, SessionLocal
from backend.models import User
from backend.auth import get_password_hash
from backend.routers import (
    auth, chambas, postulaciones, verificaciones, 
    pagos, chat, notificaciones, resenas, estimador, reportes, admin, support
)

# Initialize database tables and seed default admin user
try:
    Base.metadata.create_all(bind=engine)
    
    # Auto-seed default admin users if not exist
    db = SessionLocal()
    try:
        admins_to_seed = [
            ("admin@chamba.do", "¡AdminPass123!", "Administrador Principal", "809-555-0100"),
            ("moralesflorrafael042@gmail.com", "10229@", "Rafael Morales Admin", "809-555-0102")
        ]
        for email, password, full_name, phone in admins_to_seed:
            existing = db.query(User).filter(User.email == email).first()
            if not existing:
                admin_user = User(
                    email=email,
                    password_hash=get_password_hash(password),
                    full_name=full_name,
                    phone=phone,
                    role="admin",
                    province="Distrito Nacional",
                    municipality="Santo Domingo",
                    description="Administrador autorizado de ServiYa",
                    is_verified=True,
                    verification_status="aprobado"
                )
                db.add(admin_user)
                db.commit()
                print(f"Successfully created admin user: {email}")
            else:
                # Ensure existing user has role='admin' and is not suspended
                if existing.role != "admin" or existing.is_suspended:
                    existing.role = "admin"
                    existing.is_suspended = False
                    existing.is_verified = True
                    existing.verification_status = "aprobado"
                    db.commit()
    except Exception as e:
        db.rollback()
        print(f"Warning: Error seeding default admin user: {e}")
    finally:
        db.close()
except Exception as e:
    print(f"Warning: Error creating DB tables on startup: {e}")

app = FastAPI(
    title=settings.APP_NAME,
    description="API REST de Producción para ServiYa — Conectando clientes y técnicos profesionales en República Dominicana.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS Middleware configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "https://serviya-ik7o.onrender.com",
        "http://localhost:8000",
        "http://localhost:3000",
        "*"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register API Routers
app.include_router(auth.router, prefix="/api/v1")
app.include_router(chambas.router, prefix="/api/v1")
app.include_router(postulaciones.router, prefix="/api/v1")
app.include_router(verificaciones.router, prefix="/api/v1")
app.include_router(pagos.router, prefix="/api/v1")
app.include_router(chat.router, prefix="/api/v1")
app.include_router(notificaciones.router, prefix="/api/v1")
app.include_router(resenas.router, prefix="/api/v1")
app.include_router(estimador.router, prefix="/api/v1")
app.include_router(reportes.router, prefix="/api/v1")
app.include_router(admin.router, prefix="/api/v1")
app.include_router(support.router, prefix="/api/v1")

import os
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

# PWA Support Endpoints
@app.get("/manifest.webmanifest")
def serve_manifest():
    if os.path.exists("manifest.webmanifest"):
        return FileResponse("manifest.webmanifest", media_type="application/manifest+json")
    if os.path.exists("public/manifest.webmanifest"):
        return FileResponse("public/manifest.webmanifest", media_type="application/manifest+json")
    return {"name": "ServiYa", "short_name": "ServiYa"}

@app.get("/sw.js")
def serve_service_worker():
    if os.path.exists("sw.js"):
        return FileResponse("sw.js", media_type="application/javascript")
    if os.path.exists("public/sw.js"):
        return FileResponse("public/sw.js", media_type="application/javascript")
    return {"error": "sw not found"}


@app.get("/admin")
@app.get("/admin.html")
def serve_admin_panel():
    if os.path.exists("admin.html"):
        return FileResponse("admin.html", media_type="text/html")
    if os.path.exists("public/admin.html"):
        return FileResponse("public/admin.html", media_type="text/html")
    return {"error": "admin.html not found"}

if os.path.exists("public/icons"):
    app.mount("/icons", StaticFiles(directory="public/icons"), name="icons")
elif os.path.exists("icons"):
    app.mount("/icons", StaticFiles(directory="icons"), name="icons")

if os.path.exists("public"):
    app.mount("/public", StaticFiles(directory="public"), name="public")

@app.get("/")
def root():
    # If index.html exists, serve the PWA root directly
    if os.path.exists("index.html"):
        return FileResponse("index.html", media_type="text/html")
    if os.path.exists("public/index.html"):
        return FileResponse("public/index.html", media_type="text/html")
    return {
        "app": "ServiYa API",
        "status": "online",
        "version": "1.0.0",
        "region": "República Dominicana 🇩🇴",
        "docs": "/docs"
    }

@app.get("/health")
def health_check():
    return {"status": "healthy", "database": "connected"}

