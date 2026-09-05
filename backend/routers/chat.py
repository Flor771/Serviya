from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from backend.database import get_db
from backend.models import Message, Chamba, User, Notification
from backend.schemas import MessageCreateRequest, MessageResponse
from backend.auth import get_current_user

router = APIRouter(prefix="/chat", tags=["Mensajería"])

@router.post("/", response_model=MessageResponse, status_code=status.HTTP_201_CREATED)
def send_message(
    request: MessageCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    chamba = db.query(Chamba).filter(Chamba.id == request.chamba_id).first()
    if not chamba:
        raise HTTPException(status_code=404, detail="Chamba no encontrada")
        
    # Check that current user is part of the chamba (client or worker)
    if current_user.id != chamba.client_id and current_user.id != chamba.worker_id and current_user.role != "admin":
        raise HTTPException(
            status_code=403, 
            detail="No tienes permiso para enviar mensajes en esta conversación."
        )
        
    new_msg = Message(
        chamba_id=request.chamba_id,
        sender_id=current_user.id,
        receiver_id=request.receiver_id,
        content=request.content.strip()
    )
    db.add(new_msg)
    
    # Send notification to receiver
    notif = Notification(
        user_id=request.receiver_id,
        title=f"Nuevo mensaje de {current_user.full_name}",
        message=request.content[:100],
        type="chat",
        chamba_id=chamba.id
    )
    db.add(notif)
    
    db.commit()
    db.refresh(new_msg)
    
    resp = MessageResponse.model_validate(new_msg)
    resp.sender_name = current_user.full_name
    return resp

@router.get("/chamba/{chamba_id}", response_model=List[MessageResponse])
def get_conversation(
    chamba_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    chamba = db.query(Chamba).filter(Chamba.id == chamba_id).first()
    if not chamba:
        raise HTTPException(status_code=404, detail="Chamba no encontrada")
        
    if current_user.id != chamba.client_id and current_user.id != chamba.worker_id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Acceso denegado a esta conversación.")
        
    messages = db.query(Message).filter(
        Message.chamba_id == chamba_id
    ).order_by(Message.created_at.asc()).all()
    
    # Mark messages as read for recipient
    for m in messages:
        if m.receiver_id == current_user.id and not m.is_read:
            m.is_read = True
    db.commit()
    
    results = []
    for m in messages:
        resp = MessageResponse.model_validate(m)
        if m.sender:
            resp.sender_name = m.sender.full_name
        results.append(resp)
    return results
