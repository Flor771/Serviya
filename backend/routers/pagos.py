from typing import List, Optional
from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from backend.database import get_db
from backend.models import Payment, Chamba, User, Notification, PlatformConfig, BankAccountConfig, TechnicianPayout, WorkerBankAccount, Contract
from backend.schemas import (
    PaymentCreateRequest, PaymentResponse, BankAccountResponse, 
    PaymentReceiptUploadRequest, TechnicianPayoutResponse,
    WorkerBankAccountCreateRequest, WorkerBankAccountResponse
)
from backend.auth import get_current_user, require_role, require_admin
from backend.config import settings

router = APIRouter(prefix="/pagos", tags=["Pagos & Comisiones"])

def get_platform_commission_rate(db: Session) -> float:
    config = db.query(PlatformConfig).filter(PlatformConfig.key == "platform_commission_rate").first()
    if config:
        try:
            return float(config.value)
        except ValueError:
            pass
    return settings.DEFAULT_COMMISSION_RATE

def get_active_bank_account(db: Session) -> Optional[BankAccountConfig]:
    account = db.query(BankAccountConfig).filter(BankAccountConfig.is_active == True).order_by(BankAccountConfig.created_at.desc()).first()
    if not account:
        # Fallback default seed bank account if none created yet
        account = BankAccountConfig(
            bank_name="Banco de Reservas (Banreservas)",
            account_holder="CHAMBA RD S.R.L.",
            account_type="Cuenta Corriente Empresarial",
            account_number="960-123456-7",
            rnc_or_cedula="1-32-45678-9",
            is_active=True,
            notes="Cuenta oficial para transferencias y depósitos directos de CHAMBA RD."
        )
        db.add(account)
        db.commit()
        db.refresh(account)
    return account

@router.get("/active-bank-account", response_model=BankAccountResponse)
def get_current_active_bank_account(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Retorna la cuenta bancaria activa configurada por el administrador para realizar pagos por transferencia."""
    account = get_active_bank_account(db)
    return BankAccountResponse.model_validate(account)

@router.post("/iniciar", response_model=PaymentResponse, status_code=status.HTTP_201_CREATED)
def initiate_payment(
    request: PaymentCreateRequest,
    current_user: User = Depends(require_role(["cliente", "admin"])),
    db: Session = Depends(get_db)
):
    """Inicia la orden de pago por transferencia bancaria y congela los datos bancarios y comisión actuales."""
    chamba = db.query(Chamba).filter(Chamba.id == request.chamba_id).first()
    if not chamba:
        raise HTTPException(status_code=404, detail="Chamba no encontrada")
        
    if chamba.client_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Solo el cliente puede iniciar el pago.")
        
    if not chamba.worker_id:
        raise HTTPException(status_code=400, detail="Debes seleccionar un técnico antes de realizar el pago.")
        
    contract = db.query(Contract).filter(Contract.chamba_id == chamba.id).first()
    if contract:
        commission_rate = contract.commission_rate
        precio_trabajo = contract.agreed_price_rd
        commission_amount = contract.commission_amount_rd
        total_cliente = contract.total_client_amount_rd
        worker_payout = contract.worker_payout_rd
    else:
        commission_rate = get_platform_commission_rate(db)
        precio_trabajo = float(chamba.budget_rd if (chamba.budget_rd and chamba.budget_rd > 0) else request.total_amount_rd)
        commission_amount = round(precio_trabajo * commission_rate, 2)
        total_cliente = round(precio_trabajo + commission_amount, 2)
        worker_payout = round(precio_trabajo, 2)
        
    bank_account = get_active_bank_account(db)
    
    # Check if payment already exists
    existing = db.query(Payment).filter(Payment.chamba_id == chamba.id).first()
    if existing:
        existing.total_amount_rd = total_cliente
        existing.commission_rate = commission_rate
        existing.commission_amount_rd = commission_amount
        existing.worker_payout_rd = worker_payout
        if existing.status in ["pendiente", "rechazado"]:
            existing.status = "pendiente"
        if bank_account:
            existing.bank_account_used_id = bank_account.id
            existing.bank_name_used = bank_account.bank_name
            existing.account_number_used = bank_account.account_number
        db.commit()
        db.refresh(existing)
        return _format_payment_response(existing, chamba, db)
        
    new_payment = Payment(
        chamba_id=chamba.id,
        client_id=chamba.client_id,
        worker_id=chamba.worker_id,
        total_amount_rd=total_cliente,
        commission_rate=commission_rate,
        commission_amount_rd=commission_amount,
        worker_payout_rd=worker_payout,
        status="pendiente",
        transaction_ref=f"RD-TRANS-{int(datetime.utcnow().timestamp()) % 1000000}",
        bank_account_used_id=bank_account.id if bank_account else None,
        bank_name_used=bank_account.bank_name if bank_account else "",
        account_number_used=bank_account.account_number if bank_account else ""
    )
    
    db.add(new_payment)
    db.commit()
    db.refresh(new_payment)
    return _format_payment_response(new_payment, chamba, db)

@router.post("/{payment_id}/subir-comprobante", response_model=PaymentResponse)
def upload_payment_receipt(
    payment_id: str,
    request: PaymentReceiptUploadRequest,
    current_user: User = Depends(require_role(["cliente", "admin"])),
    db: Session = Depends(get_db)
):
    """El cliente sube el comprobante de la transferencia realizada para revisión administrativa."""
    payment = db.query(Payment).filter(Payment.id == payment_id).first()
    if not payment:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
        
    if payment.client_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="No autorizado para subir comprobante en este pago.")
        
    if payment.status in ["retenido", "liberado", "pagado"]:
        raise HTTPException(status_code=400, detail="El pago ya ha sido verificado o procesado.")
        
    payment.receipt_url = request.receipt_url
    payment.receipt_notes = request.receipt_notes or ""
    if request.transaction_ref:
        payment.transaction_ref = request.transaction_ref
    payment.receipt_uploaded_at = datetime.utcnow()
    payment.status = "comprobante_subido" # En revisión por el administrador
    payment.rejection_reason = ""
    
    # Notify administrators
    admins = db.query(User).filter(User.role == "admin").all()
    for adm in admins:
        db.add(Notification(
            user_id=adm.id,
            title="📥 Nuevo comprobante de transferencia",
            message=f"El cliente ha subido un comprobante para la chamba por RD${payment.total_amount_rd:,.0f}. Pendiente de revisión.",
            type="pago",
            chamba_id=payment.chamba_id
        ))
        
    db.commit()
    db.refresh(payment)
    chamba = db.query(Chamba).filter(Chamba.id == payment.chamba_id).first()
    return _format_payment_response(payment, chamba, db)

@router.get("/{payment_id}", response_model=PaymentResponse)
def get_payment_details(
    payment_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    payment = db.query(Payment).filter(Payment.id == payment_id).first()
    if not payment:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
    if payment.client_id != current_user.id and payment.worker_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Acceso no autorizado a este pago.")
    chamba = db.query(Chamba).filter(Chamba.id == payment.chamba_id).first()
    return _format_payment_response(payment, chamba, db)

@router.get("/chamba/{chamba_id}", response_model=Optional[PaymentResponse])
def get_payment_by_chamba(
    chamba_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    payment = db.query(Payment).filter(Payment.chamba_id == chamba_id).first()
    if not payment:
        return None
    if payment.client_id != current_user.id and payment.worker_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Acceso no autorizado.")
    chamba = db.query(Chamba).filter(Chamba.id == chamba_id).first()
    return _format_payment_response(payment, chamba, db)

@router.post("/{payment_id}/liberar", response_model=PaymentResponse)
def release_payment(
    payment_id: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Al completar satisfactoriamente la chamba, se libera el pago en custodia y se registra el pago pendiente al técnico."""
    payment = db.query(Payment).filter(Payment.id == payment_id).first()
    if not payment:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
        
    if payment.client_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Solo el cliente o el administrador pueden liberar los fondos.")
        
    if payment.status != "retenido":
        raise HTTPException(status_code=400, detail="No se puede liberar un pago que no está en custodia ('retenido').")
        
    payment.status = "liberado"
    payment.released_at = datetime.utcnow()
    
    # Update Chamba state
    if payment.chamba:
        payment.chamba.status = "completada"
        
    # Update Worker completed jobs count
    worker = db.query(User).filter(User.id == payment.worker_id).first()
    if worker:
        worker.completed_jobs += 1
        
    # Create or update Technician Payout record in "pendiente" status
    existing_payout = db.query(TechnicianPayout).filter(TechnicianPayout.payment_id == payment.id).first()
    if not existing_payout:
        payout = TechnicianPayout(
            payment_id=payment.id,
            chamba_id=payment.chamba_id,
            worker_id=payment.worker_id,
            gross_amount_rd=payment.total_amount_rd,
            commission_rate=payment.commission_rate,
            commission_amount_rd=payment.commission_amount_rd,
            net_payout_rd=payment.worker_payout_rd,
            status="pendiente",
            notes="Pago pendiente de transferencia al técnico por parte de administración de CHAMBA RD."
        )
        db.add(payout)
        
    # Notification to Worker
    notif = Notification(
        user_id=payment.worker_id,
        title="💵 ¡Trabajo Confirmado y Pago Liberado!",
        message=f"El cliente ha confirmado la finalización. Ganancia neta: RD${payment.worker_payout_rd:,.0f}. Administración procesará tu transferencia.",
        type="pago",
        chamba_id=payment.chamba_id
    )
    db.add(notif)
    
    db.commit()
    db.refresh(payment)
    chamba = db.query(Chamba).filter(Chamba.id == payment.chamba_id).first()
    return _format_payment_response(payment, chamba, db)

@router.get("/cliente/mis-pagos", response_model=List[PaymentResponse])
def get_client_payments(
    current_user: User = Depends(require_role(["cliente", "admin"])),
    db: Session = Depends(get_db)
):
    payments = db.query(Payment).filter(Payment.client_id == current_user.id).order_by(Payment.created_at.desc()).all()
    results = []
    for p in payments:
        ch = db.query(Chamba).filter(Chamba.id == p.chamba_id).first()
        results.append(_format_payment_response(p, ch, db))
    return results

@router.get("/tecnico/mis-ingresos", response_model=dict)
def get_worker_income_summary(
    current_user: User = Depends(require_role(["trabajador", "admin"])),
    db: Session = Depends(get_db)
):
    payments = db.query(Payment).filter(Payment.worker_id == current_user.id).all()
    payouts = db.query(TechnicianPayout).filter(TechnicianPayout.worker_id == current_user.id).all()
    
    retenido = sum(p.worker_payout_rd for p in payments if p.status in ["comprobante_subido", "en_revision", "confirmado", "retenido"])
    pendiente_pago_admin = sum(po.net_payout_rd for po in payouts if po.status == "pendiente")
    pagado_transferido = sum(po.net_payout_rd for po in payouts if po.status == "pagado")
    total_comision = sum(p.commission_amount_rd for p in payments if p.status in ["liberado", "confirmado"])
    
    return {
        "en_custodia_rd": retenido,
        "pendiente_transferencia_admin_rd": pendiente_pago_admin,
        "pagado_transferido_rd": pagado_transferido,
        "comisiones_retenidas_rd": total_comision,
        "total_ganado_neto_rd": pagado_transferido + pendiente_pago_admin,
        "total_trabajos_completados": len([p for p in payments if p.status == "liberado"])
    }

def _format_payment_response(payment: Payment, chamba: Optional[Chamba], db: Session) -> PaymentResponse:
    client = db.query(User).filter(User.id == payment.client_id).first()
    worker = db.query(User).filter(User.id == payment.worker_id).first()
    return PaymentResponse(
        id=payment.id,
        chamba_id=payment.chamba_id,
        chamba_title=chamba.title if chamba else "Chamba",
        client_id=payment.client_id,
        client_name=client.full_name if client else "Cliente",
        worker_id=payment.worker_id,
        worker_name=worker.full_name if worker else "Técnico",
        total_amount_rd=payment.total_amount_rd,
        commission_rate=payment.commission_rate,
        commission_amount_rd=payment.commission_amount_rd,
        worker_payout_rd=payment.worker_payout_rd,
        status=payment.status,
        transaction_ref=payment.transaction_ref,
        receipt_url=payment.receipt_url or "",
        receipt_notes=payment.receipt_notes or "",
        receipt_uploaded_at=payment.receipt_uploaded_at,
        verified_by_admin_id=payment.verified_by_admin_id,
        verified_at=payment.verified_at,
        rejection_reason=payment.rejection_reason or "",
        bank_account_used_id=payment.bank_account_used_id,
        bank_name_used=payment.bank_name_used or "",
        account_number_used=payment.account_number_used or "",
        created_at=payment.created_at,
        released_at=payment.released_at
    )


# --- DATOS BANCARIOS DEL TÉCNICO PARA RECIBIR PAGOS ---

@router.get("/tecnico/datos-bancarios", response_model=WorkerBankAccountResponse)
def get_my_worker_bank_account(
    current_user: User = Depends(require_role(["trabajador", "admin"])),
    db: Session = Depends(get_db)
):
    """Obtiene los datos bancarios privados del técnico autenticado para recibir pagos en PostgreSQL."""
    bank = db.query(WorkerBankAccount).filter(WorkerBankAccount.worker_id == current_user.id).first()
    if not bank:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Aún no has registrado tus datos de pago para poder recibir tus ganancias."
        )
    return WorkerBankAccountResponse.model_validate(bank)


@router.post("/tecnico/datos-bancarios", response_model=WorkerBankAccountResponse)
def save_my_worker_bank_account(
    request: WorkerBankAccountCreateRequest,
    current_user: User = Depends(require_role(["trabajador", "admin"])),
    db: Session = Depends(get_db)
):
    """Registra o actualiza los datos bancarios del técnico autenticado para recibir pagos.
    Reglas aplicadas:
    - Validación de coincidencia de números de cuenta
    - Exclusivo para técnicos y administradores autorizados
    - Almacenado de forma segura en PostgreSQL
    - Nunca se solicitan ni almacenan PIN, contraseñas ni códigos de seguridad (CVV)
    """
    # Validation: reject any suspicious keywords or sensitive terms
    forbidden_terms = ["pin", "cvv", "password", "clave", "código de seguridad"]
    for term in forbidden_terms:
        if term in request.account_number.lower() or term in request.bank_name.lower():
            raise HTTPException(status_code=400, detail="Operación no permitida: No ingrese contraseñas, PIN ni CVV.")

    # Search existing
    bank = db.query(WorkerBankAccount).filter(WorkerBankAccount.worker_id == current_user.id).first()
    if bank:
        bank.bank_name = request.bank_name.strip()
        bank.account_holder = request.account_holder.strip()
        bank.account_type = request.account_type.strip()
        bank.account_number = request.account_number.strip()
        bank.updated_at = datetime.utcnow()
    else:
        bank = WorkerBankAccount(
            worker_id=current_user.id,
            bank_name=request.bank_name.strip(),
            account_holder=request.account_holder.strip(),
            account_type=request.account_type.strip(),
            account_number=request.account_number.strip(),
            created_at=datetime.utcnow(),
            updated_at=datetime.utcnow()
        )
        db.add(bank)

    db.commit()
    db.refresh(bank)
    return WorkerBankAccountResponse.model_validate(bank)

