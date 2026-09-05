from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from backend.database import get_db
from backend.models import Postulacion, Chamba, User, Notification, Contract
from backend.schemas import PostulacionCreateRequest, PostulacionResponse
from backend.auth import get_current_user, require_role
from backend.routers.pagos import get_platform_commission_rate

router = APIRouter(prefix="/postulaciones", tags=["Postulaciones"])

@router.post("/", response_model=PostulacionResponse, status_code=status.HTTP_201_CREATED)
def apply_to_chamba(
    request: PostulacionCreateRequest,
    current_user: User = Depends(require_role(["trabajador", "admin"])),
    db: Session = Depends(get_db)
):
    # Verify chamba existence & status
    chamba = db.query(Chamba).filter(Chamba.id == request.chamba_id).first()
    if not chamba:
        raise HTTPException(status_code=404, detail="La chamba solicitada no existe.")
        
    if chamba.status not in ["publicada", "recibiendo_postulaciones"]:
        raise HTTPException(
            status_code=400, 
            detail="Esta chamba ya no está aceptando postulaciones."
        )
        
    if chamba.client_id == current_user.id:
        raise HTTPException(
            status_code=400, 
            detail="No puedes postularte a una chamba que tú mismo publicaste."
        )
        
    # Prevent duplicate applications
    existing = db.query(Postulacion).filter(
        Postulacion.chamba_id == request.chamba_id,
        Postulacion.worker_id == current_user.id
    ).first()
    
    if existing:
        if existing.status == "retirada":
            # Allow reactivation if previously withdrawn
            existing.status = "pendiente"
            existing.proposed_price_rd = request.proposed_price_rd
            existing.proposal_message = request.proposal_message or ""
            existing.can_perform = request.can_perform
            existing.has_tools = request.has_tools
            existing.available_on_date = request.available_on_date
            existing.needs_client_supplies = request.needs_client_supplies
            existing.confirm_details_read = request.confirm_details_read
            db.commit()
            db.refresh(existing)
            return build_postulacion_response(existing)
        raise HTTPException(
            status_code=400, 
            detail="Ya te has postulado previamente a esta chamba."
        )
        
    new_postulacion = Postulacion(
        chamba_id=request.chamba_id,
        worker_id=current_user.id,
        proposal_message=request.proposal_message or "",
        proposed_price_rd=request.proposed_price_rd,
        can_perform=request.can_perform,
        has_tools=request.has_tools,
        available_on_date=request.available_on_date,
        needs_client_supplies=request.needs_client_supplies,
        confirm_details_read=request.confirm_details_read,
        status="pendiente"
    )
    db.add(new_postulacion)
    
    # Update chamba state
    if chamba.status == "publicada":
        chamba.status = "recibiendo_postulaciones"
        
    # Notify client
    client_notif = Notification(
        user_id=chamba.client_id,
        title="Nueva postulación recibida",
        message=f"{current_user.full_name} se ha postulado a tu chamba '{chamba.title}' por RD${request.proposed_price_rd:,.0f}.",
        type="postulacion",
        chamba_id=chamba.id
    )
    db.add(client_notif)
    
    db.commit()
    db.refresh(new_postulacion)
    return build_postulacion_response(new_postulacion)

@router.get("/chamba/{chamba_id}", response_model=List[PostulacionResponse])
def get_chamba_postulaciones(
    chamba_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    chamba = db.query(Chamba).filter(Chamba.id == chamba_id).first()
    if not chamba:
        raise HTTPException(status_code=404, detail="Chamba no encontrada")
        
    # Only client or admin can see all applications
    if chamba.client_id != current_user.id and current_user.role != "admin":
        # Workers can only see their own
        posts = db.query(Postulacion).filter(
            Postulacion.chamba_id == chamba_id,
            Postulacion.worker_id == current_user.id
        ).all()
        return [build_postulacion_response(p) for p in posts]
        
    posts = db.query(Postulacion).filter(Postulacion.chamba_id == chamba_id).all()
    return [build_postulacion_response(p) for p in posts]

@router.get("/my-applications", response_model=List[PostulacionResponse])
def get_my_postulaciones(
    current_user: User = Depends(require_role(["trabajador", "admin"])),
    db: Session = Depends(get_db)
):
    posts = db.query(Postulacion).filter(
        Postulacion.worker_id == current_user.id
    ).order_by(Postulacion.created_at.desc()).all()
    return [build_postulacion_response(p) for p in posts]

@router.post("/{postulacion_id}/select", response_model=PostulacionResponse)
def select_worker(
    postulacion_id: str,
    current_user: User = Depends(require_role(["cliente", "admin"])),
    db: Session = Depends(get_db)
):
    postulacion = db.query(Postulacion).filter(Postulacion.id == postulacion_id).first()
    if not postulacion:
        raise HTTPException(status_code=404, detail="Postulación no encontrada")
        
    chamba = postulacion.chamba
    if chamba.client_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Solo el creador de la chamba puede seleccionar un técnico.")
        
    # Accept this application
    postulacion.status = "aceptada"
    
    # Reject all other applications for this chamba
    other_posts = db.query(Postulacion).filter(
        Postulacion.chamba_id == chamba.id,
        Postulacion.id != postulacion.id
    ).all()
    for other in other_posts:
        other.status = "rechazada"
        notif_rej = Notification(
            user_id=other.worker_id,
            title="Postulación no seleccionada",
            message=f"El cliente ha seleccionado a otro técnico para la chamba '{chamba.title}'.",
            type="seleccion",
            chamba_id=chamba.id
        )
        db.add(notif_rej)
        
    # Assign worker to chamba and update status
    chamba.worker_id = postulacion.worker_id
    chamba.status = "trabajador_seleccionado"
    
    # Calculate commission details based on the agreed price
    commission_rate = get_platform_commission_rate(db)
    precio_trabajo = postulacion.proposed_price_rd
    commission_amount = round(precio_trabajo * commission_rate, 2)
    total_cliente = round(precio_trabajo + commission_amount, 2)
    worker_payout = round(precio_trabajo, 2)

    # Create Contract record
    contract = Contract(
        chamba_id=chamba.id,
        client_id=chamba.client_id,
        worker_id=postulacion.worker_id,
        agreed_price_rd=precio_trabajo,
        commission_rate=commission_rate,
        commission_amount_rd=commission_amount,
        worker_payout_rd=worker_payout,
        total_client_amount_rd=total_cliente,
        status="activo"
    )
    db.add(contract)
    
    # Notify selected worker
    notif_worker = Notification(
        user_id=postulacion.worker_id,
        title="🎉 ¡Has sido seleccionado!",
        message=f"¡Felicidades! Fuiste seleccionado para la chamba '{chamba.title}'. Puedes iniciar el chat con el cliente.",
        type="seleccion",
        chamba_id=chamba.id
    )
    db.add(notif_worker)
    
    db.commit()
    db.refresh(postulacion)
    return build_postulacion_response(postulacion)

def build_postulacion_response(p: Postulacion) -> PostulacionResponse:
    resp = PostulacionResponse.model_validate(p)
    if p.chamba:
        resp.chamba_title = p.chamba.title
    if p.worker:
        resp.worker_name = p.worker.full_name
        resp.worker_photo = p.worker.profile_photo_url
        resp.worker_rating = p.worker.rating_average
        resp.worker_jobs = p.worker.completed_jobs
        resp.worker_verified = p.worker.is_verified
    return resp
