"""Add technician worker bank accounts for payouts

Revision ID: 0004_technician_bank_accounts
Revises: 0003_bank_transfer_and_payouts
Create Date: 2026-09-02 09:30:00.000000

"""
from alembic import op
import sqlalchemy as sa

revision = '0004_technician_bank_accounts'
down_revision = '0003_bank_transfer_and_payouts'
branch_labels = None
depends_on = None

def upgrade() -> None:
    # Create worker_bank_accounts table in PostgreSQL
    op.create_table(
        'worker_bank_accounts',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('worker_id', sa.String(length=36), nullable=False),
        sa.Column('bank_name', sa.String(length=100), nullable=False),
        sa.Column('account_holder', sa.String(length=150), nullable=False),
        sa.Column('account_type', sa.String(length=50), nullable=False),
        sa.Column('account_number', sa.String(length=50), nullable=False),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['worker_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('worker_id')
    )
    op.create_index('ix_worker_bank_accounts_worker_id', 'worker_bank_accounts', ['worker_id'], unique=True)

def downgrade() -> None:
    op.drop_index('ix_worker_bank_accounts_worker_id', table_name='worker_bank_accounts')
    op.drop_table('worker_bank_accounts')
