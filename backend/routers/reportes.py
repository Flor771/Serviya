from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from backend.database import get_db
from backend.models import Report, Dispute, User, Notification
from backend.schemas import ReportCreateRequest, DisputeCreateRequest, ReportResponse, DisputeResponse, DisputeResolveRequest
from backend.auth import get_current_user, require_admin

router = APIRouter(prefix="/reportes", tags=["Reportes & Disputas"])

@router.post("/crear", response_model=dict, status_code=status.HTTP_201_CREATED)
def create_report(
    request: ReportCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    new_report = Report(
        reporter_id=current_user.id,
        reported_user_id=request.reported_user_id,
        chamba_id=request.chamba_id or "",
        reason=request.reason,
        description=request.description,
        evidence_url=request.evidence_url or "",
        status="pendiente"
    )
    db.add(new_report)
    db.commit()
    db.refresh(new_report)
    return {"message": "Reporte enviado exitosamente. El equipo de administración revisará el caso.", "report_id": new_report.id}

@router.post("/disputa", response_model=dict, status_code=status.HTTP_201_CREATED)
def open_dispute(
    request: DisputeCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    new_dispute = Dispute(
        chamba_id=request.chamba_id,
        creator_id=current_user.id,
        reason=request.reason,
        description=request.description,
        evidence_url=request.evidence_url or "",
        status="abierta"
    )
    db.add(new_dispute)
    db.commit()
    db.refresh(new_dispute)
    return {"message": "Disputa abierta. El administrador intervendrá como mediador.", "dispute_id": new_dispute.id}

@router.get("/reportes", response_model=List[ReportResponse])
def get_reports(admin_user: User = Depends(require_admin), db: Session = Depends(get_db)):
    return db.query(Report).all()

@router.get("/disputas", response_model=List[DisputeResponse])
def get_disputes(admin_user: User = Depends(require_admin), db: Session = Depends(get_db)):
    return db.query(Dispute).order_by(Dispute.created_at.desc()).all()

@router.post("/disputas/{dispute_id}/resolver", response_model=DisputeResponse)
def resolve_dispute(
    dispute_id: str,
    request: DisputeResolveRequest,
    admin_user: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    from backend.models import Chamba
    dispute = db.query(Dispute).filter(Dispute.id == dispute_id).first()
    if not dispute:
        raise HTTPException(status_code=404, detail="Disputa no encontrada")
    
    dispute.status = "resuelta"
    dispute.resolution_notes = request.resolution_notes
    
    # Notificar a los involucrados
    chamba = db.query(Chamba).filter(Chamba.id == dispute.chamba_id).first()
    if chamba:
        chamba.status = "cancelada" # O "completada" dependiendo de la lógica, forzamos cierre preventivo
        
        db.add(Notification(
            user_id=chamba.client_id,
            title="⚖️ Disputa Resuelta",
            message=f"La disputa para la chamba '{chamba.title}' ha sido resuelta por el Administrador. Notas: {request.resolution_notes}",
            type="disputa",
            chamba_id=chamba.id
        ))
        if chamba.worker_id:
            db.add(Notification(
                user_id=chamba.worker_id,
                title="⚖️ Disputa Resuelta",
                message=f"La disputa para la chamba '{chamba.title}' ha sido resuelta por el Administrador. Notas: {request.resolution_notes}",
                type="disputa",
                chamba_id=chamba.id
            ))
            
    db.commit()
    db.refresh(dispute)
    return dispute
