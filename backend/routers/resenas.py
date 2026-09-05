from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import func

from backend.database import get_db
from backend.models import Review, Chamba, User, Notification
from backend.schemas import ReviewCreateRequest, ReviewResponse
from backend.auth import get_current_user

router = APIRouter(prefix="/resenas", tags=["Calificaciones & Reseñas"])

@router.post("/", response_model=ReviewResponse, status_code=status.HTTP_201_CREATED)
def submit_review(
    request: ReviewCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    chamba = db.query(Chamba).filter(Chamba.id == request.chamba_id).first()
    if not chamba:
        raise HTTPException(status_code=404, detail="Chamba no encontrada")
        
    # Check if user was part of this chamba
    if current_user.id != chamba.client_id and current_user.id != chamba.worker_id:
        raise HTTPException(status_code=403, detail="Solo los participantes de este trabajo pueden dejar una reseña.")
        
    # Prevent duplicate reviews by same author on same chamba
    existing = db.query(Review).filter(
        Review.chamba_id == request.chamba_id,
        Review.author_id == current_user.id
    ).first()
    if existing:
        raise HTTPException(status_code=400, detail="Ya has calificado esta chamba.")
        
    new_review = Review(
        chamba_id=request.chamba_id,
        author_id=current_user.id,
        recipient_id=request.recipient_id,
        rating=request.rating,
        comment=request.comment or ""
    )
    db.add(new_review)
    
    # Recalculate recipient rating average
    recipient = db.query(User).filter(User.id == request.recipient_id).first()
    if recipient:
        all_ratings = db.query(Review.rating).filter(Review.recipient_id == recipient.id).all()
        ratings_list = [r[0] for r in all_ratings] + [request.rating]
        recipient.rating_average = round(sum(ratings_list) / len(ratings_list), 2)
        recipient.total_ratings = len(ratings_list)
        
        # Notify recipient
        notif = Notification(
            user_id=recipient.id,
            title="⭐ Nueva Calificación Recibida",
            message=f"{current_user.full_name} te ha calificado con {request.rating:.1f} estrellas.",
            type="review",
            chamba_id=chamba.id
        )
        db.add(notif)
        
    db.commit()
    db.refresh(new_review)
    
    resp = ReviewResponse.model_validate(new_review)
    resp.author_name = current_user.full_name
    resp.author_photo = current_user.profile_photo_url
    return resp

@router.get("/user/{user_id}", response_model=List[ReviewResponse])
def get_user_reviews(user_id: str, db: Session = Depends(get_db)):
    reviews = db.query(Review).filter(
        Review.recipient_id == user_id
    ).order_by(Review.created_at.desc()).all()
    
    results = []
    for r in reviews:
        resp = ReviewResponse.model_validate(r)
        if r.author:
            resp.author_name = r.author.full_name
            resp.author_photo = r.author.profile_photo_url
        results.append(resp)
    return results
