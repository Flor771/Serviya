"""Add admin security columns to users table

Revision ID: 0002_admin_security
Revises: 0001_initial_schema
Create Date: 2026-09-02 06:45:00.000000

"""
from alembic import op
import sqlalchemy as sa

revision = '0002_admin_security'
down_revision = '0001_initial_schema'
branch_labels = None
depends_on = None

def upgrade() -> None:
    op.add_column('users', sa.Column('must_change_password', sa.Boolean(), server_default='false', nullable=False))
    op.add_column('users', sa.Column('created_by_admin_id', sa.String(length=36), nullable=True))

def downgrade() -> None:
    op.drop_column('users', 'created_by_admin_id')
    op.drop_column('users', 'must_change_password')
