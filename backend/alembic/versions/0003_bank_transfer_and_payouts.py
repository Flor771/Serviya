"""Add bank transfer configuration, audit, receipt fields and technician payouts

Revision ID: 0003_bank_transfer_and_payouts
Revises: 0002_admin_security
Create Date: 2026-09-02 08:30:00.000000

"""
from alembic import op
import sqlalchemy as sa

revision = '0003_bank_transfer_and_payouts'
down_revision = '0002_admin_security'
branch_labels = None
depends_on = None

def upgrade() -> None:
    # 1. Create bank_account_configs table
    op.create_table(
        'bank_account_configs',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('bank_name', sa.String(length=100), nullable=False),
        sa.Column('account_holder', sa.String(length=150), nullable=False),
        sa.Column('account_type', sa.String(length=50), nullable=False),
        sa.Column('account_number', sa.String(length=50), nullable=False),
        sa.Column('rnc_or_cedula', sa.String(length=50), server_default='', nullable=True),
        sa.Column('is_active', sa.Boolean(), server_default='true', nullable=False),
        sa.Column('notes', sa.Text(), server_default='', nullable=True),
        sa.Column('created_by_admin_id', sa.String(length=36), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )

    # 2. Create bank_account_audits table
    op.create_table(
        'bank_account_audits',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('admin_id', sa.String(length=36), nullable=False),
        sa.Column('action', sa.String(length=50), nullable=False),
        sa.Column('account_id', sa.String(length=36), nullable=True),
        sa.Column('old_data', sa.JSON(), nullable=True),
        sa.Column('new_data', sa.JSON(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['admin_id'], ['users.id'], ),
        sa.PrimaryKeyConstraint('id')
    )

    # 3. Add columns to payments table
    op.add_column('payments', sa.Column('receipt_url', sa.Text(), server_default='', nullable=True))
    op.add_column('payments', sa.Column('receipt_notes', sa.Text(), server_default='', nullable=True))
    op.add_column('payments', sa.Column('receipt_uploaded_at', sa.DateTime(), nullable=True))
    op.add_column('payments', sa.Column('verified_by_admin_id', sa.String(length=36), nullable=True))
    op.add_column('payments', sa.Column('verified_at', sa.DateTime(), nullable=True))
    op.add_column('payments', sa.Column('rejection_reason', sa.Text(), server_default='', nullable=True))
    op.add_column('payments', sa.Column('bank_account_used_id', sa.String(length=36), nullable=True))
    op.add_column('payments', sa.Column('bank_name_used', sa.String(length=100), server_default='', nullable=True))
    op.add_column('payments', sa.Column('account_number_used', sa.String(length=50), server_default='', nullable=True))

    # 4. Create technician_payouts table
    op.create_table(
        'technician_payouts',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('payment_id', sa.String(length=36), nullable=False),
        sa.Column('chamba_id', sa.String(length=36), nullable=False),
        sa.Column('worker_id', sa.String(length=36), nullable=False),
        sa.Column('gross_amount_rd', sa.Float(), nullable=False),
        sa.Column('commission_rate', sa.Float(), server_default='0.10', nullable=False),
        sa.Column('commission_amount_rd', sa.Float(), server_default='0.0', nullable=False),
        sa.Column('net_payout_rd', sa.Float(), nullable=False),
        sa.Column('status', sa.String(length=30), server_default='pendiente', nullable=False),
        sa.Column('paid_at', sa.DateTime(), nullable=True),
        sa.Column('payment_method', sa.String(length=50), server_default='Transferencia Bancaria', nullable=True),
        sa.Column('transfer_reference', sa.String(length=100), server_default='', nullable=True),
        sa.Column('processed_by_admin_id', sa.String(length=36), nullable=True),
        sa.Column('payout_receipt_url', sa.Text(), server_default='', nullable=True),
        sa.Column('notes', sa.Text(), server_default='', nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['chamba_id'], ['chambas.id'], ),
        sa.ForeignKeyConstraint(['payment_id'], ['payments.id'], ),
        sa.ForeignKeyConstraint(['worker_id'], ['users.id'], ),
        sa.PrimaryKeyConstraint('id')
    )

def downgrade() -> None:
    op.drop_table('technician_payouts')
    op.drop_column('payments', 'account_number_used')
    op.drop_column('payments', 'bank_name_used')
    op.drop_column('payments', 'bank_account_used_id')
    op.drop_column('payments', 'rejection_reason')
    op.drop_column('payments', 'verified_at')
    op.drop_column('payments', 'verified_by_admin_id')
    op.drop_column('payments', 'receipt_uploaded_at')
    op.drop_column('payments', 'receipt_notes')
    op.drop_column('payments', 'receipt_url')
    op.drop_table('bank_account_audits')
    op.drop_table('bank_account_configs')
