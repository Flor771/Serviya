from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from datetime import datetime

from backend.database import get_db
from backend.models import User, Notification
from backend.schemas import VerificationSubmitRequest, UserResponse
from backend.auth import get_current_user, require_role, require_admin

router = APIRouter(prefix="/verificaciones", tags=["Verificación de Técnicos"])

@router.post("/solicitar", response_model=UserResponse)
def submit_verification(
    request: VerificationSubmitRequest,
    current_user: User = Depends(require_role(["trabajador", "admin"])),
    db: Session = Depends(get_db)
):
    cedula_clean = request.id_card_number.strip()
    if len(cedula_clean) < 11:
        raise HTTPException(
            status_code=400, 
            detail="Por favor ingresa un número de Cédula Dominicana válido (11 dígitos)."
        )
        
    current_user.id_card_number = cedula_clean
    current_user.infotep_course_name = request.infotep_course_name or ""
    current_user.infotep_doc_url = request.infotep_doc_url or ""
    current_user.verification_status = "pendiente"
    # IMPORTANT: Never auto-verify without admin review
    current_user.is_verified = False
    current_user.is_id_card_verified = False
    current_user.has_infotep_certificate = False
    
    db.commit()
    db.refresh(current_user)
    return UserResponse.model_validate(current_user)

@router.get("/pendientes", response_model=List[UserResponse])
def get_pending_verifications(
    admin_user: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    pending = db.query(User).filter(
        User.role == "trabajador",
        User.verification_status == "pendiente"
    ).all()
    return [UserResponse.model_validate(u) for u in pending]

@router.post("/{worker_id}/aprobar", response_model=UserResponse)
def approve_verification(
    worker_id: str,
    admin_user: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    worker = db.query(User).filter(User.id == worker_id).first()
    if not worker:
        raise HTTPException(status_code=404, detail="Técnico no encontrado")
        
    worker.verification_status = "aprobado"
    worker.is_verified = True
    worker.is_id_card_verified = True
    if worker.infotep_course_name:
        worker.has_infotep_certificate = True
        
    # Send congratulatory notification
    notif = Notification(
        user_id=worker.id,
        title="🛡️ ¡Verificación Oficial Aprobada!",
        message="Tu Cédula y certificación técnica han sido validadas. Ya cuentas con la insignia oficial ✓ TÉCNICO VERIFICADO.",
        type="verificacion"
    )
    db.add(notif)
    
    db.commit()
    db.refresh(worker)
    return UserResponse.model_validate(worker)

@router.post("/{worker_id}/rechazar", response_model=UserResponse)
def reject_verification(
    worker_id: str,
    motivo: str = "Los documentos no coinciden o no son legibles",
    admin_user: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    worker = db.query(User).filter(User.id == worker_id).first()
    if not worker:
        raise HTTPException(status_code=404, detail="Técnico no encontrado")
        
    worker.verification_status = "rechazado"
    worker.is_verified = False
    worker.is_id_card_verified = False
    worker.has_infotep_certificate = False
    
    notif = Notification(
        user_id=worker.id,
        title="Solicitud de Verificación Rechazada",
        message=f"Tu solicitud no pudo ser aprobada: {motivo}. Puedes actualizar tus datos e intentar nuevamente.",
        type="verificacion"
    )
    db.add(notif)
    
    db.commit()
    db.refresh(worker)
    return UserResponse.model_validate(worker)
