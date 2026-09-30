-- ==============================================================================
-- 008_routine_commutes_and_cancellation.sql
-- 1. Tambah cancellation_reason di tabel bookings
-- 2. Buat tabel routine_commutes untuk otomasi tebengan rutin pengguna
-- ==============================================================================

-- 1. Tambah cancellation_reason ke tabel bookings jika belum ada
ALTER TABLE IF EXISTS bookings 
ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;

-- 2. Buat tabel routine_commutes
CREATE TABLE IF NOT EXISTS routine_commutes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    origin_name TEXT NOT NULL,
    destination_name TEXT NOT NULL,
    origin_lat DOUBLE PRECISION DEFAULT 0.0,
    origin_lng DOUBLE PRECISION DEFAULT 0.0,
    destination_lat DOUBLE PRECISION DEFAULT 0.0,
    destination_lng DOUBLE PRECISION DEFAULT 0.0,
    departure_time TEXT NOT NULL,
    active_days TEXT NOT NULL,
    vehicle_type TEXT DEFAULT 'car' CHECK (vehicle_type IN ('car', 'motorcycle', 'all')),
    is_enabled BOOLEAN DEFAULT TRUE,
    auto_book BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indeks performa
CREATE INDEX IF NOT EXISTS routine_commutes_user_id_idx ON routine_commutes (user_id);
CREATE INDEX IF NOT EXISTS routine_commutes_is_enabled_idx ON routine_commutes (is_enabled);

-- Row Level Security
ALTER TABLE routine_commutes ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'routine_commutes' AND policyname = 'routine_commutes: allow all'
    ) THEN
        CREATE POLICY "routine_commutes: allow all" ON routine_commutes FOR ALL USING (true) WITH CHECK (true);
    END IF;
END $$;
