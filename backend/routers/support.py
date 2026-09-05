import re
from datetime import datetime
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from backend.database import get_db
from backend.models import User, PlatformConfig
from backend.schemas import SupportConfigRequest, SupportConfigResponse
from backend.auth import require_admin

router = APIRouter(prefix="/support", tags=["Atención al Cliente"])

DEFAULT_SUPPORT_PHONE = "829-837-0908"
DEFAULT_SUPPORT_WHATSAPP = "18298370908"
DEFAULT_SUPPORT_HOURS = "Lunes a Viernes: 8:00 AM - 6:00 PM | Sábados: 9:00 AM - 1:00 PM"
DEFAULT_SUPPORT_EMAIL = "soporte@chambard.com"
DEFAULT_SUPPORT_MSG = "Hola CHAMBA RD, necesito asistencia con la plataforma."

def _get_config_value(db: Session, key: str, default: str) -> str:
    cfg = db.query(PlatformConfig).filter(PlatformConfig.key == key).first()
    return cfg.value if cfg else default

def _set_config_value(db: Session, key: str, value: str, description: str = "") -> None:
    cfg = db.query(PlatformConfig).filter(PlatformConfig.key == key).first()
    if cfg:
        cfg.value = value
        cfg.updated_at = datetime.utcnow()
        if description:
            cfg.description = description
    else:
        cfg = PlatformConfig(key=key, value=value, description=description, updated_at=datetime.utcnow())
        db.add(cfg)

@router.get("/config", response_model=SupportConfigResponse)
def get_customer_support_config(db: Session = Depends(get_db)):
    """
    Retorna la configuración actual de canales de atención al cliente de CHAMBA RD.
    Accesible para todos los usuarios (clientes, técnicos y visitantes) para consultar
    el teléfono oficial, enlace directo de WhatsApp, horarios y correo de soporte.
    """
    phone = _get_config_value(db, "support_phone", DEFAULT_SUPPORT_PHONE)
    whatsapp = _get_config_value(db, "support_whatsapp", DEFAULT_SUPPORT_WHATSAPP)
    hours = _get_config_value(db, "support_hours", DEFAULT_SUPPORT_HOURS)
    email = _get_config_value(db, "support_email", DEFAULT_SUPPORT_EMAIL)
    msg = _get_config_value(db, "support_whatsapp_message", DEFAULT_SUPPORT_MSG)
    last_updated_by = _get_config_value(db, "support_last_updated_by", "administrador_sistema")
    
    # Obtener fecha de última modificación
    last_cfg = db.query(PlatformConfig).filter(PlatformConfig.key.in_([
        "support_phone", "support_whatsapp", "support_hours", "support_email"
    ])).order_by(PlatformConfig.updated_at.desc()).first()
    updated_at = last_cfg.updated_at if last_cfg else datetime.utcnow()

    clean_phone = re.sub(r"[^0-9+]", "", phone)
    clean_wa_digits = re.sub(r"[^0-9]", "", whatsapp)
    clean_wa = f"1{clean_wa_digits}" if (len(clean_wa_digits) == 10 and not clean_wa_digits.startswith("1")) else clean_wa_digits

    return SupportConfigResponse(
        phone=phone,
        whatsapp=whatsapp,
        business_hours=hours,
        email=email,
        whatsapp_welcome_message=msg,
        clean_phone_for_dial=clean_phone or "8095550150",
        clean_whatsapp_for_link=clean_wa or "18095550150",
        updated_at=updated_at,
        last_updated_by=last_updated_by
    )

@router.put("/config", response_model=SupportConfigResponse)
def update_customer_support_config(
    request: SupportConfigRequest,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    """
    Actualiza los canales oficiales de atención al cliente de CHAMBA RD.
    SEGURIDAD: Restringido estrictamente a usuarios autenticados con 'role = admin'.
    Cualquier modificación se persiste en la base de datos sin requerir cambios de código.
    """
    clean_phone = request.phone.strip()
    clean_whatsapp = request.whatsapp.strip()
    clean_hours = request.business_hours.strip()
    clean_email = (request.email or "").strip()
    clean_msg = (request.whatsapp_welcome_message or "").strip()

    if not clean_phone:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El número de teléfono de atención es obligatorio."
        )
    if not clean_whatsapp:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El número de WhatsApp de atención es obligatorio."
        )
    if not clean_hours:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El horario de atención es obligatorio."
        )

    _set_config_value(db, "support_phone", clean_phone, "Número telefónico oficial de atención")
    _set_config_value(db, "support_whatsapp", clean_whatsapp, "Número de WhatsApp oficial para soporte")
    _set_config_value(db, "support_hours", clean_hours, "Horario oficial de atención al cliente")
    _set_config_value(db, "support_email", clean_email or "soporte@chambard.com", "Correo de atención oficial")
    _set_config_value(db, "support_whatsapp_message", clean_msg or "Hola CHAMBA RD, necesito asistencia.", "Mensaje predeterminado WhatsApp")
    _set_config_value(db, "support_last_updated_by", admin.email, "Admin que realizó la última actualización")

    db.commit()

    return get_customer_support_config(db)
