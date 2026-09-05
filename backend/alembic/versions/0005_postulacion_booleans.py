"""Add booleans to postulacion

Revision ID: add_postulacion_booleans
Revises: 
Create Date: 2026-09-03 12:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = '0005_postulacion_booleans'
down_revision: Union[str, None] = '0004_technician_bank_accounts'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

def upgrade() -> None:
    op.add_column('postulaciones', sa.Column('can_perform', sa.Boolean(), server_default='true', nullable=False))
    op.add_column('postulaciones', sa.Column('has_tools', sa.Boolean(), server_default='true', nullable=False))
    op.add_column('postulaciones', sa.Column('available_on_date', sa.Boolean(), server_default='true', nullable=False))
    op.add_column('postulaciones', sa.Column('needs_client_supplies', sa.Boolean(), server_default='false', nullable=False))
    op.add_column('postulaciones', sa.Column('confirm_details_read', sa.Boolean(), server_default='true', nullable=False))

def downgrade() -> None:
    op.drop_column('postulaciones', 'confirm_details_read')
    op.drop_column('postulaciones', 'needs_client_supplies')
    op.drop_column('postulaciones', 'available_on_date')
    op.drop_column('postulaciones', 'has_tools')
    op.drop_column('postulaciones', 'can_perform')
