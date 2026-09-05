from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from backend.database import get_db
from backend.models import PriceReference
from backend.schemas import (
    PriceReferenceResponse, PriceEstimateCalculateRequest, PriceEstimateResult
)

router = APIRouter(prefix="/estimador", tags=["Estimador de Precios RD$"])

@router.get("/referencias", response_model=List[PriceReferenceResponse])
def get_price_references(category: Optional[str] = None, db: Session = Depends(get_db)):
    query = db.query(PriceReference)
    if category and category != "Todas":
        query = query.filter(PriceReference.category.ilike(f"%{category}%"))
    items = query.order_by(PriceReference.category.asc(), PriceReference.service_name.asc()).all()
    return [PriceReferenceResponse.model_validate(item) for item in items]

@router.post("/calcular", response_model=PriceEstimateResult)
def calculate_estimate(request: PriceEstimateCalculateRequest, db: Session = Depends(get_db)):
    ref = db.query(PriceReference).filter(PriceReference.id == request.service_id).first()
    if not ref:
        raise HTTPException(status_code=404, detail="Servicio de referencia no encontrado")
        
    qty = max(1.0, request.quantity)
    total_min = ref.min_price_rd * qty
    total_max = ref.max_price_rd * qty
    
    return PriceEstimateResult(
        service_name=ref.service_name,
        category=ref.category,
        quantity=qty,
        unit_type=ref.unit_type,
        total_min_rd=total_min,
        total_max_rd=total_max,
        disclaimer="Los precios son orientativos. El precio final puede variar según materiales, dificultad, ubicación, urgencia y acuerdo entre cliente y técnico."
    )
