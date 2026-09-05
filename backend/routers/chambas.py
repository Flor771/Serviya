from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import or_, and_

from backend.database import get_db
from backend.models import Chamba, User, Postulacion
from backend.schemas import (
    ChambaCreateRequest, ChambaUpdateRequest, ChambaResponse
)
from backend.auth import get_current_user, require_role

router = APIRouter(prefix="/chambas", tags=["Chambas"])

@router.post("/", response_model=ChambaResponse, status_code=status.HTTP_201_CREATED)
def create_chamba(
    request: ChambaCreateRequest,
    current_user: User = Depends(require_role(["cliente", "admin"])),
    db: Session = Depends(get_db)
):
    new_chamba = Chamba(
        title=request.title.strip(),
        description=request.description.strip(),
        category_id=request.category_id,
        category_name=request.category_name or "General",
        client_id=current_user.id,
        province=request.province or "Santo Domingo",
        municipality=request.municipality or "Distrito Nacional",
        budget_rd=request.budget_rd,
        scheduled_date=request.scheduled_date or "",
        photos=request.photos or [],
        status="publicada"
    )
    db.add(new_chamba)
    db.commit()
    db.refresh(new_chamba)
    
    resp = ChambaResponse.model_validate(new_chamba)
    resp.client_name = current_user.full_name
    return resp

@router.get("/", response_model=List[ChambaResponse])
def list_chambas(
    category: Optional[str] = None,
    province: Optional[str] = None,
    municipality: Optional[str] = None,
    search: Optional[str] = None,
    min_budget: Optional[float] = None,
    max_budget: Optional[float] = None,
    status_filter: Optional[str] = Query("disponibles", description="disponibles, todas, o estado específico"),
    limit: int = 50,
    offset: int = 0,
    db: Session = Depends(get_db)
):
    query = db.query(Chamba)
    
    if status_filter == "disponibles":
        query = query.filter(Chamba.status.in_(["publicada", "recibiendo_postulaciones"]))
    elif status_filter != "todas":
        query = query.filter(Chamba.status == status_filter)
        
    if category and category != "Todas":
        query = query.filter(Chamba.category_name.ilike(f"%{category}%"))
    if province and province != "Todas":
        query = query.filter(Chamba.province.ilike(f"%{province}%"))
    if municipality:
        query = query.filter(Chamba.municipality.ilike(f"%{municipality}%"))
    if min_budget is not None:
        query = query.filter(Chamba.budget_rd >= min_budget)
    if max_budget is not None:
        query = query.filter(Chamba.budget_rd <= max_budget)
    if search:
        query = query.filter(
            or_(
                Chamba.title.ilike(f"%{search}%"),
                Chamba.description.ilike(f"%{search}%")
            )
        )
        
    chambas = query.order_by(Chamba.created_at.desc()).offset(offset).limit(limit).all()
    
    results = []
    for c in chambas:
        resp = ChambaResponse.model_validate(c)
        if c.client:
            resp.client_name = c.client.full_name
        if c.worker:
            resp.worker_name = c.worker.full_name
        resp.postulaciones_count = len(c.postulaciones)
        results.append(resp)
        
    return results

@router.get("/{chamba_id}", response_model=ChambaResponse)
def get_chamba_detail(chamba_id: str, db: Session = Depends(get_db)):
    chamba = db.query(Chamba).filter(Chamba.id == chamba_id).first()
    if not chamba:
        raise HTTPException(status_code=404, detail="Chamba no encontrada")
        
    resp = ChambaResponse.model_validate(chamba)
    if chamba.client:
        resp.client_name = chamba.client.full_name
    if chamba.worker:
        resp.worker_name = chamba.worker.full_name
    resp.postulaciones_count = len(chamba.postulaciones)
    return resp

@router.get("/user/my-chambas", response_model=List[ChambaResponse])
def get_my_chambas(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if current_user.role == "cliente":
        chambas = db.query(Chamba).filter(Chamba.client_id == current_user.id).order_by(Chamba.created_at.desc()).all()
    else:
        chambas = db.query(Chamba).filter(Chamba.worker_id == current_user.id).order_by(Chamba.created_at.desc()).all()
        
    results = []
    for c in chambas:
        resp = ChambaResponse.model_validate(c)
        if c.client:
            resp.client_name = c.client.full_name
        if c.worker:
            resp.worker_name = c.worker.full_name
        resp.postulaciones_count = len(c.postulaciones)
        results.append(resp)
    return results

@router.put("/{chamba_id}/cancel", response_model=ChambaResponse)
def cancel_chamba(
    chamba_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    chamba = db.query(Chamba).filter(Chamba.id == chamba_id).first()
    if not chamba:
        raise HTTPException(status_code=404, detail="Chamba no encontrada")
        
    if chamba.client_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="No tienes permiso para cancelar esta chamba")
        
    if chamba.status in ["completada", "en_progreso"]:
        raise HTTPException(status_code=400, detail="No se puede cancelar una chamba ya en progreso o completada sin abrir una disputa.")
        
    chamba.status = "cancelada"
    db.commit()
    db.refresh(chamba)
    return ChambaResponse.model_validate(chamba)
