import os
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from backend.config import settings

# Handle Render's postgres:// -> postgresql:// URL compatibility
database_url = settings.DATABASE_URL
if database_url.startswith("postgres://"):
    database_url = database_url.replace("postgres://", "postgresql://", 1)

# Configure connection arguments and pooling based on database type
connect_args = {}
engine_kwargs = {"pool_pre_ping": True}

if database_url.startswith("sqlite"):
    connect_args["check_same_thread"] = False
else:
    engine_kwargs["pool_size"] = 10
    engine_kwargs["max_overflow"] = 20
    if "render.com" in database_url or "aws.neon.tech" in database_url or "supabase.co" in database_url or "dpg-" in database_url:
        connect_args["sslmode"] = "require"

engine = create_engine(
    database_url,
    connect_args=connect_args,
    **engine_kwargs
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
