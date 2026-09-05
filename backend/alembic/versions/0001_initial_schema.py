"""Initial migration for CHAMBA RD

Revision ID: 0001_initial_schema
Revises: 
Create Date: 2026-09-02 06:30:00.000000

"""
from alembic import op
import sqlalchemy as sa

revision = '0001_initial_schema'
down_revision = None
branch_labels = None
depends_on = None

def upgrade() -> None:
    # Users Table
    op.create_table(
        'users',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('email', sa.String(length=120), nullable=False, unique=True, index=True),
        sa.Column('password_hash', sa.String(length=255), nullable=False),
        sa.Column('full_name', sa.String(length=100), nullable=False),
        sa.Column('phone', sa.String(length=30), nullable=True),
        sa.Column('role', sa.String(length=20), nullable=False, server_default='cliente'),
        sa.Column('province', sa.String(length=100), server_default='Santo Domingo'),
        sa.Column('municipality', sa.String(length=100), server_default='Distrito Nacional'),
        sa.Column('description', sa.Text(), server_default=''),
        sa.Column('experience_years', sa.Integer(), server_default='1'),
        sa.Column('hourly_rate_rd', sa.Float(), server_default='0.0'),
        sa.Column('availability', sa.String(length=50), server_default='Disponible'),
        sa.Column('profile_photo_url', sa.Text(), server_default=''),
        sa.Column('portfolio_urls', sa.JSON(), server_default='[]'),
        sa.Column('categories', sa.JSON(), server_default='[]'),
        sa.Column('is_verified', sa.Boolean(), server_default='false'),
        sa.Column('verification_status', sa.String(length=20), server_default='sin_solicitar'),
        sa.Column('id_card_number', sa.String(length=30), server_default=''),
        sa.Column('is_id_card_verified', sa.Boolean(), server_default='false'),
        sa.Column('has_infotep_certificate', sa.Boolean(), server_default='false'),
        sa.Column('infotep_course_name', sa.String(length=150), server_default=''),
        sa.Column('infotep_doc_url', sa.Text(), server_default=''),
        sa.Column('rating_average', sa.Float(), server_default='5.0'),
        sa.Column('total_ratings', sa.Integer(), server_default='0'),
        sa.Column('completed_jobs', sa.Integer(), server_default='0'),
        sa.Column('is_active', sa.Boolean(), server_default='true'),
        sa.Column('is_suspended', sa.Boolean(), server_default='false'),
        sa.Column('must_change_password', sa.Boolean(), server_default='false'),
        sa.Column('created_by_admin_id', sa.String(length=36), nullable=True),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now()),
        sa.Column('updated_at', sa.DateTime(), server_default=sa.func.now(), onupdate=sa.func.now())
    )

    # Categories Table
    op.create_table(
        'categories',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('name', sa.String(length=80), nullable=False, unique=True),
        sa.Column('icon_name', sa.String(length=50), server_default='build'),
        sa.Column('description', sa.Text(), server_default=''),
        sa.Column('min_price_rd', sa.Float(), server_default='500.0'),
        sa.Column('max_price_rd', sa.Float(), server_default='5000.0'),
        sa.Column('is_active', sa.Boolean(), server_default='true')
    )

    # Chambas Table
    op.create_table(
        'chambas',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('title', sa.String(length=150), nullable=False, index=True),
        sa.Column('description', sa.Text(), nullable=False),
        sa.Column('category_id', sa.String(length=36), sa.ForeignKey('categories.id'), nullable=True),
        sa.Column('category_name', sa.String(length=80), server_default='General'),
        sa.Column('client_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('worker_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('province', sa.String(length=100), server_default='Santo Domingo'),
        sa.Column('municipality', sa.String(length=100), server_default='Distrito Nacional'),
        sa.Column('budget_rd', sa.Float(), server_default='0.0'),
        sa.Column('status', sa.String(length=30), server_default='publicada', index=True),
        sa.Column('photos', sa.JSON(), server_default='[]'),
        sa.Column('scheduled_date', sa.String(length=50), server_default=''),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now()),
        sa.Column('updated_at', sa.DateTime(), server_default=sa.func.now(), onupdate=sa.func.now())
    )

    # Postulaciones Table
    op.create_table(
        'postulaciones',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('chamba_id', sa.String(length=36), sa.ForeignKey('chambas.id'), nullable=False, index=True),
        sa.Column('worker_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False, index=True),
        sa.Column('proposal_message', sa.Text(), server_default=''),
        sa.Column('proposed_price_rd', sa.Float(), nullable=False),
        sa.Column('status', sa.String(length=20), server_default='pendiente'),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now()),
        sa.Column('updated_at', sa.DateTime(), server_default=sa.func.now(), onupdate=sa.func.now())
    )

    # Contracts Table
    op.create_table(
        'contracts',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('chamba_id', sa.String(length=36), sa.ForeignKey('chambas.id'), unique=True, nullable=False),
        sa.Column('client_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('worker_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('agreed_price_rd', sa.Float(), nullable=False),
        sa.Column('status', sa.String(length=30), server_default='activo'),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now()),
        sa.Column('completed_at', sa.DateTime(), nullable=True)
    )

    # Payments Table
    op.create_table(
        'payments',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('chamba_id', sa.String(length=36), sa.ForeignKey('chambas.id'), unique=True, nullable=False),
        sa.Column('client_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('worker_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('total_amount_rd', sa.Float(), nullable=False),
        sa.Column('commission_rate', sa.Float(), server_default='0.10'),
        sa.Column('commission_amount_rd', sa.Float(), server_default='0.0'),
        sa.Column('worker_payout_rd', sa.Float(), server_default='0.0'),
        sa.Column('status', sa.String(length=30), server_default='retenido'),
        sa.Column('transaction_ref', sa.String(length=100), server_default=''),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now()),
        sa.Column('released_at', sa.DateTime(), nullable=True)
    )

    # Messages Table
    op.create_table(
        'messages',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('chamba_id', sa.String(length=36), sa.ForeignKey('chambas.id'), nullable=False, index=True),
        sa.Column('sender_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('receiver_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('content', sa.Text(), nullable=False),
        sa.Column('is_read', sa.Boolean(), server_default='false'),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now())
    )

    # Notifications Table
    op.create_table(
        'notifications',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('user_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False, index=True),
        sa.Column('title', sa.String(length=150), nullable=False),
        sa.Column('message', sa.Text(), nullable=False),
        sa.Column('type', sa.String(length=30), server_default='info'),
        sa.Column('chamba_id', sa.String(length=36), server_default=''),
        sa.Column('is_read', sa.Boolean(), server_default='false'),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now())
    )

    # Reviews Table
    op.create_table(
        'reviews',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('chamba_id', sa.String(length=36), sa.ForeignKey('chambas.id'), nullable=False),
        sa.Column('author_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('recipient_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False, index=True),
        sa.Column('rating', sa.Float(), nullable=False),
        sa.Column('comment', sa.Text(), server_default=''),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now())
    )

    # Reports Table
    op.create_table(
        'reports',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('reporter_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('reported_user_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('chamba_id', sa.String(length=36), server_default=''),
        sa.Column('reason', sa.String(length=80), nullable=False),
        sa.Column('description', sa.Text(), server_default=''),
        sa.Column('evidence_url', sa.Text(), server_default=''),
        sa.Column('status', sa.String(length=20), server_default='pendiente'),
        sa.Column('admin_resolution', sa.Text(), server_default=''),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now())
    )

    # Disputes Table
    op.create_table(
        'disputes',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('chamba_id', sa.String(length=36), sa.ForeignKey('chambas.id'), nullable=False),
        sa.Column('creator_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('reason', sa.String(length=100), nullable=False),
        sa.Column('description', sa.Text(), nullable=False),
        sa.Column('evidence_url', sa.Text(), server_default=''),
        sa.Column('status', sa.String(length=20), server_default='abierta'),
        sa.Column('resolution', sa.Text(), server_default=''),
        sa.Column('created_at', sa.DateTime(), server_default=sa.func.now())
    )

    # Price References Table
    op.create_table(
        'price_references',
        sa.Column('id', sa.String(length=36), primary_key=True),
        sa.Column('category', sa.String(length=80), nullable=False, index=True),
        sa.Column('service_name', sa.String(length=120), nullable=False),
        sa.Column('unit_type', sa.String(length=50), server_default='unidad'),
        sa.Column('min_price_rd', sa.Float(), nullable=False),
        sa.Column('max_price_rd', sa.Float(), nullable=False),
        sa.Column('estimated_time', sa.String(length=60), server_default='1-3 horas'),
        sa.Column('includes_materials', sa.Boolean(), server_default='false'),
        sa.Column('notes', sa.Text(), server_default='')
    )

    # Platform Config Table
    op.create_table(
        'platform_configs',
        sa.Column('key', sa.String(length=80), primary_key=True),
        sa.Column('value', sa.String(length=255), nullable=False),
        sa.Column('description', sa.Text(), server_default=''),
        sa.Column('updated_at', sa.DateTime(), server_default=sa.func.now())
    )

def downgrade() -> None:
    op.drop_table('platform_configs')
    op.drop_table('price_references')
    op.drop_table('disputes')
    op.drop_table('reports')
    op.drop_table('reviews')
    op.drop_table('notifications')
    op.drop_table('messages')
    op.drop_table('payments')
    op.drop_table('contracts')
    op.drop_table('postulaciones')
    op.drop_table('chambas')
    op.drop_table('categories')
    op.drop_table('users')
