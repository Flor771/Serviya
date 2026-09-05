from typing import List, Optional
from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import func

from backend.database import get_db
from backend.models import (
    User, Chamba, Postulacion, Payment, Report, Dispute, 
    Category, PlatformConfig, BankAccountConfig, BankAccountAudit,
    TechnicianPayout, Notification, WorkerBankAccount
)
from backend.schemas import (
    UserResponse, ChambaResponse, AdminCreateRequest, PaymentResponse,
    PaymentVerificationActionRequest, BankAccountCreateRequest,
    BankAccountUpdateRequest, BankAccountResponse, BankAccountAuditResponse,
    TechnicianPayoutResponse, TechnicianPayoutMarkPaidRequest,
    FinancialSummaryResponse, CommissionUpdateRequest, WorkerBankAccountResponse
)
from backend.auth import require_admin, get_password_hash

router = APIRouter(prefix="/admin", tags=["Panel Administrativo"])

# --- STATS & RESUMEN FINANCIERO ---

@router.get("/stats", response_model=dict)
def get_admin_stats(
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    total_users = db.query(User).count()
    total_clients = db.query(User).filter(User.role == "cliente").count()
    total_workers = db.query(User).filter(User.role == "trabajador").count()
    verified_workers = db.query(User).filter(User.role == "trabajador", User.is_verified == True).count()
    pending_verifications = db.query(User).filter(User.role == "trabajador", User.verification_status == "pendiente").count()
    
    total_chambas = db.query(Chamba).count()
    active_chambas = db.query(Chamba).filter(Chamba.status.in_(["publicada", "recibiendo_postulaciones", "en_progreso"])).count()
    completed_chambas = db.query(Chamba).filter(Chamba.status == "completada").count()
    cancelled_chambas = db.query(Chamba).filter(Chamba.status == "cancelada").count()
    
    payments = db.query(Payment).all()
    total_volume_rd = sum(p.total_amount_rd for p in payments)
    platform_earnings_rd = sum(p.commission_amount_rd for p in payments if p.status in ["liberado", "confirmado", "retenido"])
    
    payouts = db.query(TechnicianPayout).all()
    payouts_paid_rd = sum(po.net_payout_rd for po in payouts if po.status == "pagado")
    payouts_pending_rd = sum(po.net_payout_rd for po in payouts if po.status == "pendiente")
    
    pending_receipts = db.query(Payment).filter(Payment.status.in_(["comprobante_subido", "en_revision"])).count()
    open_reports = db.query(Report).filter(Report.status == "pendiente").count()
    open_disputes = db.query(Dispute).filter(Dispute.status == "abierta").count()
    
    return {
        "usuarios": {
            "total": total_users,
            "clientes": total_clients,
            "trabajadores": total_workers,
            "verificados": verified_workers,
            "verificaciones_pendientes": pending_verifications
        },
        "chambas": {
            "total": total_chambas,
            "activas": active_chambas,
            "completadas": completed_chambas,
            "canceladas": cancelled_chambas
        },
        "finanzas": {
            "volumen_total_rd": total_volume_rd,
            "ganancias_comision_rd": platform_earnings_rd,
            "pagos_tecnicos_efectuados_rd": payouts_paid_rd,
            "pagos_tecnicos_pendientes_rd": payouts_pending_rd,
            "comprobantes_pendientes_revision": pending_receipts
        },
        "moderacion": {
            "reportes_pendientes": open_reports,
            "disputas_abiertas": open_disputes
        }
    }

@router.get("/finanzas/resumen", response_model=FinancialSummaryResponse)
def get_financial_summary(
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    payments = db.query(Payment).all()
    payouts = db.query(TechnicianPayout).all()
    
    total_recibido = sum(p.total_amount_rd for p in payments if p.status in ["confirmado", "retenido", "liberado"])
    comisiones_ganadas = sum(p.commission_amount_rd for p in payments if p.status in ["confirmado", "retenido", "liberado"])
    pagos_efectuados = sum(po.net_payout_rd for po in payouts if po.status == "pagado")
    pagos_pendientes = sum(po.net_payout_rd for po in payouts if po.status == "pendiente")
    
    commission_config = db.query(PlatformConfig).filter(PlatformConfig.key == "platform_commission_rate").first()
    current_commission = float(commission_config.value) if commission_config else 0.10
    
    active_bank = db.query(BankAccountConfig).filter(BankAccountConfig.is_active == True).order_by(BankAccountConfig.created_at.desc()).first()
    
    return FinancialSummaryResponse(
        total_recibido_rd=total_recibido,
        comisiones_ganadas_rd=comisiones_ganadas,
        pagos_tecnicos_efectuados_rd=pagos_efectuados,
        pagos_tecnicos_pendientes_rd=pagos_pendientes,
        total_transacciones=len(payments),
        comision_porcentaje_actual=current_commission,
        cuenta_bancaria_activa=BankAccountResponse.model_validate(active_bank) if active_bank else None
    )

# --- GESTIÓN DE PAGOS Y COMPROBANTES DE TRANSFERENCIA ---

@router.get("/pagos", response_model=List[PaymentResponse])
def get_all_payments_for_admin(
    status_filter: Optional[str] = None,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    """Permite al administrador ver todos los pagos y filtrar por comprobantes pendientes."""
    query = db.query(Payment)
    if status_filter:
        query = query.filter(Payment.status == status_filter)
    payments = query.order_by(Payment.created_at.desc()).all()
    
    results = []
    for p in payments:
        ch = db.query(Chamba).filter(Chamba.id == p.chamba_id).first()
        cl = db.query(User).filter(User.id == p.client_id).first()
        wk = db.query(User).filter(User.id == p.worker_id).first()
        results.append(PaymentResponse(
            id=p.id,
            chamba_id=p.chamba_id,
            chamba_title=ch.title if ch else "Chamba",
            client_id=p.client_id,
            client_name=cl.full_name if cl else "Cliente",
            worker_id=p.worker_id,
            worker_name=wk.full_name if wk else "Técnico",
            total_amount_rd=p.total_amount_rd,
            commission_rate=p.commission_rate,
            commission_amount_rd=p.commission_amount_rd,
            worker_payout_rd=p.worker_payout_rd,
            status=p.status,
            transaction_ref=p.transaction_ref,
            receipt_url=p.receipt_url or "",
            receipt_notes=p.receipt_notes or "",
            receipt_uploaded_at=p.receipt_uploaded_at,
            verified_by_admin_id=p.verified_by_admin_id,
            verified_at=p.verified_at,
            rejection_reason=p.rejection_reason or "",
            bank_account_used_id=p.bank_account_used_id,
            bank_name_used=p.bank_name_used or "",
            account_number_used=p.account_number_used or "",
            created_at=p.created_at,
            released_at=p.released_at
        ))
    return results

@router.post("/pagos/{payment_id}/confirmar", response_model=PaymentResponse)
def confirm_bank_transfer_payment(
    payment_id: str,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    """El Administrador valida y confirma el comprobante bancario. El pago queda retenido en custodia y la chamba pasa a en_progreso."""
    payment = db.query(Payment).filter(Payment.id == payment_id).first()
    if not payment:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
        
    payment.status = "retenido" # Confirmado y en custodia
    payment.verified_by_admin_id = admin.id
    payment.verified_at = datetime.utcnow()
    payment.rejection_reason = ""
    
    # Update Chamba to en_progreso
    chamba = db.query(Chamba).filter(Chamba.id == payment.chamba_id).first()
    if chamba:
        chamba.status = "en_progreso"
        
    # Notify Client
    db.add(Notification(
        user_id=payment.client_id,
        title="✅ Transferencia Confirmada",
        message=f"Tu transferencia por RD${payment.total_amount_rd:,.0f} ha sido verificada con éxito. Los fondos están en custodia segura.",
        type="pago",
        chamba_id=payment.chamba_id
    ))
    
    # Notify Worker
    db.add(Notification(
        user_id=payment.worker_id,
        title="🚀 ¡Chamba Confirmada! Puedes Iniciar",
        message=f"El pago para «{chamba.title if chamba else 'la chamba'}» ha sido verificado y está en custodia. Ya puedes realizar el trabajo.",
        type="pago",
        chamba_id=payment.chamba_id
    ))
    
    db.commit()
    db.refresh(payment)
    cl = db.query(User).filter(User.id == payment.client_id).first()
    wk = db.query(User).filter(User.id == payment.worker_id).first()
    return PaymentResponse(
        id=payment.id,
        chamba_id=payment.chamba_id,
        chamba_title=chamba.title if chamba else "Chamba",
        client_id=payment.client_id,
        client_name=cl.full_name if cl else "Cliente",
        worker_id=payment.worker_id,
        worker_name=wk.full_name if wk else "Técnico",
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

@router.post("/pagos/{payment_id}/rechazar", response_model=PaymentResponse)
def reject_bank_transfer_payment(
    payment_id: str,
    action_req: PaymentVerificationActionRequest,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    """El Administrador rechaza un comprobante inválido o ilegible con un motivo."""
    payment = db.query(Payment).filter(Payment.id == payment_id).first()
    if not payment:
        raise HTTPException(status_code=404, detail="Pago no encontrado")
        
    payment.status = "rechazado"
    payment.verified_by_admin_id = admin.id
    payment.verified_at = datetime.utcnow()
    payment.rejection_reason = action_req.rejection_reason or "Comprobante no válido o fondos no reflejados en cuenta bancaria."
    
    # Notify Client
    db.add(Notification(
        user_id=payment.client_id,
        title="⚠️ Comprobante Rechazado",
        message=f"El comprobante de transferencia fue rechazado: {payment.rejection_reason}. Por favor sube un comprobante válido.",
        type="pago",
        chamba_id=payment.chamba_id
    ))
    
    db.commit()
    db.refresh(payment)
    ch = db.query(Chamba).filter(Chamba.id == payment.chamba_id).first()
    cl = db.query(User).filter(User.id == payment.client_id).first()
    wk = db.query(User).filter(User.id == payment.worker_id).first()
    return PaymentResponse(
        id=payment.id,
        chamba_id=payment.chamba_id,
        chamba_title=ch.title if ch else "Chamba",
        client_id=payment.client_id,
        client_name=cl.full_name if cl else "Cliente",
        worker_id=payment.worker_id,
        worker_name=wk.full_name if wk else "Técnico",
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

# --- PAGOS A TÉCNICOS (TRANSFERENCIAS MANUALES EFECTUADAS POR ADMINISTRACIÓN) ---

@router.get("/payouts/technicians", response_model=List[TechnicianPayoutResponse])
def get_technician_payouts(
    status_filter: Optional[str] = None,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    query = db.query(TechnicianPayout)
    if status_filter:
        query = query.filter(TechnicianPayout.status == status_filter)
    payouts = query.order_by(TechnicianPayout.created_at.desc()).all()
    
    results = []
    for po in payouts:
        ch = db.query(Chamba).filter(Chamba.id == po.chamba_id).first()
        wk = db.query(User).filter(User.id == po.worker_id).first()
        # Query technician's bank account for manual transfer
        bank = db.query(WorkerBankAccount).filter(WorkerBankAccount.worker_id == po.worker_id).first()
        results.append(TechnicianPayoutResponse(
            id=po.id,
            payment_id=po.payment_id,
            chamba_id=po.chamba_id,
            chamba_title=ch.title if ch else "Chamba",
            worker_id=po.worker_id,
            worker_name=wk.full_name if wk else "Técnico",
            gross_amount_rd=po.gross_amount_rd,
            commission_rate=po.commission_rate,
            commission_amount_rd=po.commission_amount_rd,
            net_payout_rd=po.net_payout_rd,
            status=po.status,
            paid_at=po.paid_at,
            payment_method=po.payment_method,
            transfer_reference=po.transfer_reference,
            processed_by_admin_id=po.processed_by_admin_id,
            payout_receipt_url=po.payout_receipt_url,
            notes=po.notes,
            worker_bank_name=bank.bank_name if bank else None,
            worker_account_holder=bank.account_holder if bank else None,
            worker_account_type=bank.account_type if bank else None,
            worker_account_number=bank.account_number if bank else None,
            worker_has_bank_account=bank is not None,
            created_at=po.created_at
        ))
    return results

@router.get("/tecnicos/{worker_id}/datos-bancarios", response_model=WorkerBankAccountResponse)
def get_technician_bank_details_by_admin(
    worker_id: str,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    """Consulta los datos bancarios privados de un técnico específico para procesar un pago manual.
    Solo accesible para administradores autorizados.
    """
    bank = db.query(WorkerBankAccount).filter(WorkerBankAccount.worker_id == worker_id).first()
    if not bank:
        raise HTTPException(
            status_code=404,
            detail="El técnico no tiene datos bancarios registrados aún para recibir pagos."
        )
    return WorkerBankAccountResponse.model_validate(bank)

@router.post("/payouts/technicians/{payout_id}/marcar-pagado", response_model=TechnicianPayoutResponse)
def mark_technician_payout_as_paid(
    payout_id: str,
    request: TechnicianPayoutMarkPaidRequest,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    """Registra la transferencia bancaria realizada al técnico por parte de la administración."""
    payout = db.query(TechnicianPayout).filter(TechnicianPayout.id == payout_id).first()
    if not payout:
        raise HTTPException(status_code=404, detail="Registro de pago a técnico no encontrado.")
        
    payout.status = "pagado"
    payout.paid_at = datetime.utcnow()
    payout.payment_method = request.payment_method or "Transferencia Bancaria"
    payout.transfer_reference = request.transfer_reference
    payout.payout_receipt_url = request.payout_receipt_url or ""
    payout.notes = request.notes or ""
    payout.processed_by_admin_id = admin.id
    
    # Notify Worker
    ch = db.query(Chamba).filter(Chamba.id == payout.chamba_id).first()
    db.add(Notification(
        user_id=payout.worker_id,
        title="🎉 Pago Transferido a tu Cuenta",
        message=f"Se ha transferido RD${payout.net_payout_rd:,.0f} a tu cuenta. Ref: {payout.transfer_reference}.",
        type="pago",
        chamba_id=payout.chamba_id
    ))
    
    db.commit()
    db.refresh(payout)
    wk = db.query(User).filter(User.id == payout.worker_id).first()
    return TechnicianPayoutResponse(
        id=payout.id,
        payment_id=payout.payment_id,
        chamba_id=payout.chamba_id,
        chamba_title=ch.title if ch else "Chamba",
        worker_id=payout.worker_id,
        worker_name=wk.full_name if wk else "Técnico",
        gross_amount_rd=payout.gross_amount_rd,
        commission_rate=payout.commission_rate,
        commission_amount_rd=payout.commission_amount_rd,
        net_payout_rd=payout.net_payout_rd,
        status=payout.status,
        paid_at=payout.paid_at,
        payment_method=payout.payment_method,
        transfer_reference=payout.transfer_reference,
        processed_by_admin_id=payout.processed_by_admin_id,
        payout_receipt_url=payout.payout_receipt_url,
        notes=payout.notes,
        created_at=payout.created_at
    )

# --- CONFIGURACIÓN Y AUDITORÍA DE CUENTAS BANCARIAS ---

@router.get("/bank-accounts", response_model=List[BankAccountResponse])
def list_bank_accounts(
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    accounts = db.query(BankAccountConfig).order_by(BankAccountConfig.created_at.desc()).all()
    return [BankAccountResponse.model_validate(a) for a in accounts]

@router.post("/bank-accounts", response_model=BankAccountResponse, status_code=status.HTTP_201_CREATED)
def create_bank_account(
    request: BankAccountCreateRequest,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    if request.is_active:
        db.query(BankAccountConfig).update({BankAccountConfig.is_active: False})
        
    account = BankAccountConfig(
        bank_name=request.bank_name.strip(),
        account_holder=request.account_holder.strip(),
        account_type=request.account_type.strip(),
        account_number=request.account_number.strip(),
        rnc_or_cedula=request.rnc_or_cedula.strip() if request.rnc_or_cedula else "",
        is_active=request.is_active if request.is_active is not None else True,
        notes=request.notes.strip() if request.notes else "",
        created_by_admin_id=admin.id
    )
    db.add(account)
    db.commit()
    db.refresh(account)
    
    audit = BankAccountAudit(
        admin_id=admin.id,
        action="CREATE",
        account_id=account.id,
        old_data={},
        new_data={
            "bank_name": account.bank_name,
            "account_holder": account.account_holder,
            "account_number": account.account_number,
            "account_type": account.account_type,
            "is_active": account.is_active
        }
    )
    db.add(audit)
    db.commit()
    return BankAccountResponse.model_validate(account)

@router.put("/bank-accounts/{account_id}", response_model=BankAccountResponse)
def update_bank_account(
    account_id: str,
    request: BankAccountUpdateRequest,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    account = db.query(BankAccountConfig).filter(BankAccountConfig.id == account_id).first()
    if not account:
        raise HTTPException(status_code=404, detail="Cuenta bancaria no encontrada.")
        
    old_data = {
        "bank_name": account.bank_name,
        "account_holder": account.account_holder,
        "account_number": account.account_number,
        "account_type": account.account_type,
        "is_active": account.is_active
    }
    
    if request.bank_name is not None:
        account.bank_name = request.bank_name.strip()
    if request.account_holder is not None:
        account.account_holder = request.account_holder.strip()
    if request.account_type is not None:
        account.account_type = request.account_type.strip()
    if request.account_number is not None:
        account.account_number = request.account_number.strip()
    if request.rnc_or_cedula is not None:
        account.rnc_or_cedula = request.rnc_or_cedula.strip()
    if request.notes is not None:
        account.notes = request.notes.strip()
    if request.is_active is not None:
        if request.is_active:
            db.query(BankAccountConfig).filter(BankAccountConfig.id != account.id).update({BankAccountConfig.is_active: False})
        account.is_active = request.is_active
        
    account.updated_at = datetime.utcnow()
    
    new_data = {
        "bank_name": account.bank_name,
        "account_holder": account.account_holder,
        "account_number": account.account_number,
        "account_type": account.account_type,
        "is_active": account.is_active
    }
    
    audit = BankAccountAudit(
        admin_id=admin.id,
        action="UPDATE",
        account_id=account.id,
        old_data=old_data,
        new_data=new_data
    )
    db.add(audit)
    db.commit()
    db.refresh(account)
    return BankAccountResponse.model_validate(account)

@router.put("/bank-accounts/{account_id}/activate", response_model=BankAccountResponse)
def activate_bank_account(
    account_id: str,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    account = db.query(BankAccountConfig).filter(BankAccountConfig.id == account_id).first()
    if not account:
        raise HTTPException(status_code=404, detail="Cuenta bancaria no encontrada.")
        
    db.query(BankAccountConfig).update({BankAccountConfig.is_active: False})
    account.is_active = True
    account.updated_at = datetime.utcnow()
    
    audit = BankAccountAudit(
        admin_id=admin.id,
        action="ACTIVATE",
        account_id=account.id,
        old_data={"is_active": False},
        new_data={"is_active": True, "bank_name": account.bank_name, "account_number": account.account_number}
    )
    db.add(audit)
    db.commit()
    db.refresh(account)
    return BankAccountResponse.model_validate(account)

@router.get("/bank-accounts/audit", response_model=List[BankAccountAuditResponse])
def list_bank_account_audits(
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    audits = db.query(BankAccountAudit).order_by(BankAccountAudit.created_at.desc()).limit(100).all()
    return [BankAccountAuditResponse.model_validate(a) for a in audits]

# --- USERS & ADMINS MANAGEMENT ---

@router.get("/users", response_model=List[UserResponse])
def get_all_users(
    role: Optional[str] = None,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    query = db.query(User)
    if role:
        query = query.filter(User.role == role)
    users = query.order_by(User.created_at.desc()).all()
    return [UserResponse.model_validate(u) for u in users]

@router.put("/users/{user_id}/suspend", response_model=UserResponse)
def toggle_user_suspension(
    user_id: str,
    suspend: bool = True,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
        
    user.is_suspended = suspend
    user.is_active = not suspend
    db.commit()
    db.refresh(user)
    return UserResponse.model_validate(user)

@router.get("/config/commission", response_model=dict)
def get_commission_config(
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    config = db.query(PlatformConfig).filter(PlatformConfig.key == "platform_commission_rate").first()
    rate = float(config.value) if config else 0.10
    return {"platform_commission_rate": rate, "percentage": f"{rate * 100:.1f}%"}

@router.put("/config/commission", response_model=dict)
def update_commission_config(
    request: CommissionUpdateRequest,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    new_rate = request.commission_rate
    if new_rate < 0.0 or new_rate > 0.50:
        raise HTTPException(status_code=400, detail="La comisión debe ser un valor entre 0% (0.0) y 50% (0.50).")
        
    config = db.query(PlatformConfig).filter(PlatformConfig.key == "platform_commission_rate").first()
    if not config:
        config = PlatformConfig(
            key="platform_commission_rate",
            value=str(new_rate),
            description="Comisión porcentual retenida por CHAMBA RD en cada trabajo completado"
        )
        db.add(config)
    else:
        config.value = str(new_rate)
        
    db.commit()
    return {"message": f"Comisión actualizada a {new_rate * 100:.1f}%", "platform_commission_rate": new_rate}

@router.get("/administradores", response_model=List[UserResponse])
def list_administrators(
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    admins = db.query(User).filter(User.role == "admin").order_by(User.created_at.asc()).all()
    return [UserResponse.model_validate(a) for a in admins]

@router.post("/administradores", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def create_additional_administrator(
    request: AdminCreateRequest,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    normalized_email = request.email.lower().strip()
    existing = db.query(User).filter(User.email == normalized_email).first()
    
    if existing:
        if existing.role == "admin":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Este usuario ya cuenta con rol de Administrador."
            )
        existing.role = "admin"
        existing.must_change_password = True
        existing.created_by_admin_id = admin.id
        if request.password:
            existing.password_hash = get_password_hash(request.password)
        db.commit()
        db.refresh(existing)
        return UserResponse.model_validate(existing)

    if len(request.password.strip()) < 8:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="La contraseña inicial del administrador debe contener al menos 8 caracteres."
        )

    new_admin = User(
        email=normalized_email,
        password_hash=get_password_hash(request.password),
        full_name=request.full_name.strip(),
        phone=request.phone or "809-555-0100",
        role="admin",
        province="Distrito Nacional",
        municipality="Santo Domingo",
        description="Administrador autorizado de CHAMBA RD",
        is_verified=True,
        verification_status="aprobado",
        must_change_password=True,
        created_by_admin_id=admin.id
    )
    
    db.add(new_admin)
    db.commit()
    db.refresh(new_admin)
    return UserResponse.model_validate(new_admin)

@router.put("/administradores/{admin_id}/status", response_model=UserResponse)
def toggle_admin_status(
    admin_id: str,
    is_active: bool,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db)
):
    if admin_id == admin.id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No puedes desactivar tu propia cuenta de administrador en sesión."
        )
        
    target_admin = db.query(User).filter(User.id == admin_id, User.role == "admin").first()
    if not target_admin:
        raise HTTPException(status_code=404, detail="Administrador no encontrado.")
        
    target_admin.is_active = is_active
    target_admin.is_suspended = not is_active
    db.commit()
    db.refresh(target_admin)
    return UserResponse.model_validate(target_admin)

