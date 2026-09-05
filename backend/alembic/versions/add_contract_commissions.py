"""Add contract commissions

Revision ID: add_contract_commissions
Revises: 0005_postulacion_booleans
Create Date: 2026-09-03 12:00:01.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = '0006_contract_commissions'
down_revision: Union[str, None] = '0005_postulacion_booleans'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

def upgrade() -> None:
    op.add_column('contracts', sa.Column('commission_rate', sa.Float(), server_default='0.10', nullable=False))
    op.add_column('contracts', sa.Column('commission_amount_rd', sa.Float(), server_default='0.0', nullable=False))
    op.add_column('contracts', sa.Column('worker_payout_rd', sa.Float(), server_default='0.0', nullable=False))
    op.add_column('contracts', sa.Column('total_client_amount_rd', sa.Float(), server_default='0.0', nullable=False))

def downgrade() -> None:
    op.drop_column('contracts', 'total_client_amount_rd')
    op.drop_column('contracts', 'worker_payout_rd')
    op.drop_column('contracts', 'commission_amount_rd')
    op.drop_column('contracts', 'commission_rate')
