from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from backend.database import get_db
from backend.models import User
from backend.schemas import (
    UserRegisterRequest, UserLoginRequest, TokenResponse,
    UserResponse, UserUpdateRequest, ChangePasswordRequest
)
from backend.auth import (
    get_password_hash, verify_password, create_access_token, 
    get_current_user
)

router = APIRouter(prefix="/auth", tags=["Autenticación"])

@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
def register_user(request: UserRegisterRequest, db: Session = Depends(get_db)):
    # Explicit security check against role escalation
    requested_role = (request.role or "").strip().lower()
    if requested_role in ["admin", "administrador", "superadmin", "moderador", "root"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Operación no permitida: El rol de administrador no puede ser creado mediante el registro público."
        )
    if requested_role not in ["cliente", "tecnico", "trabajador"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Rol no permitido para registro público. Debe ser 'cliente' o 'tecnico'."
        )
    
    valid_role = "trabajador" if requested_role in ["trabajador", "tecnico"] else "cliente"
    
    # Check if email exists
    existing = db.query(User).filter(User.email == request.email.lower().strip()).first()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ya existe una cuenta registrada con este correo electrónico."
        )
    
    new_user = User(
        email=request.email.lower().strip(),
        password_hash=get_password_hash(request.password),
        full_name=request.full_name.strip(),
        phone=request.phone,
        role=valid_role,
        province=request.province or "Santo Domingo",
        municipality=request.municipality or "Distrito Nacional",
        description=request.description or "",
        experience_years=request.experience_years or 1
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    
    token = create_access_token(data={"sub": new_user.id, "role": new_user.role})
    return TokenResponse(access_token=token, user=UserResponse.model_validate(new_user))

@router.post("/login", response_model=TokenResponse)
def login_user(request: UserLoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == request.email.lower().strip()).first()
    if not user or not verify_password(request.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Correo o contraseña incorrectos."
        )
    
    if user.is_suspended:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Tu cuenta se encuentra suspendida por infracción de normas."
        )
    
    token = create_access_token(data={"sub": user.id, "role": user.role})
    return TokenResponse(access_token=token, user=UserResponse.model_validate(user))

@router.get("/me", response_model=UserResponse)
def get_my_profile(current_user: User = Depends(get_current_user)):
    return UserResponse.model_validate(current_user)

@router.put("/profile", response_model=UserResponse)
def update_profile(
    request: UserUpdateRequest, 
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if request.full_name is not None:
        current_user.full_name = request.full_name
    if request.phone is not None:
        current_user.phone = request.phone
    if request.province is not None:
        current_user.province = request.province
    if request.municipality is not None:
        current_user.municipality = request.municipality
    if request.description is not None:
        current_user.description = request.description
    if request.experience_years is not None:
        current_user.experience_years = request.experience_years
    if request.hourly_rate_rd is not None:
        current_user.hourly_rate_rd = request.hourly_rate_rd
    if request.availability is not None:
        current_user.availability = request.availability
    if request.profile_photo_url is not None:
        current_user.profile_photo_url = request.profile_photo_url
    if request.portfolio_urls is not None:
        current_user.portfolio_urls = request.portfolio_urls
    if request.categories is not None:
        current_user.categories = request.categories
        
    db.commit()
    db.refresh(current_user)
    return UserResponse.model_validate(current_user)

@router.put("/change-password", response_model=dict)
def change_password(
    request: ChangePasswordRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if not verify_password(request.old_password, current_user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="La contraseña actual suministrada no es correcta."
        )
    
    if len(request.new_password.strip()) < 6:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="La nueva contraseña debe contener al menos 6 caracteres."
        )
        
    current_user.password_hash = get_password_hash(request.new_password)
    current_user.must_change_password = False
    db.commit()
    
    return {
        "message": "Contraseña actualizada exitosamente.",
        "must_change_password": False
    }

