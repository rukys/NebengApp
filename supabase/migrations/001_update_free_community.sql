-- ==============================================================================
-- MIGRATION: 001_update_free_community.sql
-- Migrasi perubahan dari schema berbayar ke skema 100% gratis + tip QRIS
-- ==============================================================================

-- 1. Users: tambah qris_image_url
ALTER TABLE users ADD COLUMN IF NOT EXISTS qris_image_url TEXT;

-- 2. Rides: hapus price_per_seat
ALTER TABLE rides DROP COLUMN IF EXISTS price_per_seat;

-- 3. Bookings: hapus kolom biaya & voucher, tambah has_tipped
ALTER TABLE bookings DROP COLUMN IF EXISTS base_price;
ALTER TABLE bookings DROP COLUMN IF EXISTS discount_amount;
ALTER TABLE bookings DROP COLUMN IF EXISTS final_price;
ALTER TABLE bookings DROP COLUMN IF EXISTS voucher_code;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS has_tipped BOOLEAN DEFAULT FALSE;

-- 4. Hapus tabel payments & vouchers
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS vouchers CASCADE;

-- 5. Notifications: update kategori (hapus promo)
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_category_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_category_check CHECK (category IN ('trip', 'review', 'system'));

-- 6. Update function book_seat() tanpa parameter harga
CREATE OR REPLACE FUNCTION book_seat(
    p_ride_id UUID,
    p_passenger_id UUID,
    p_seat_position TEXT
)
RETURNS UUID AS $$
DECLARE
    v_booking_id UUID;
    v_current_seats INTEGER;
    v_pickup_pin TEXT;
BEGIN
    SELECT available_seats INTO v_current_seats
    FROM rides WHERE id = p_ride_id FOR UPDATE;

    IF v_current_seats <= 0 THEN
        RAISE EXCEPTION 'Kursi sudah penuh';
    END IF;

    IF EXISTS (
        SELECT 1 FROM bookings
        WHERE ride_id = p_ride_id
        AND seat_position = p_seat_position
        AND status NOT IN ('cancelled')
    ) THEN
        RAISE EXCEPTION 'Kursi sudah dipesan';
    END IF;

    v_pickup_pin := LPAD(FLOOR(RANDOM() * 1000000)::TEXT, 6, '0');

    INSERT INTO bookings (
        ride_id, passenger_id, seat_position, pickup_pin, status
    ) VALUES (
        p_ride_id, p_passenger_id, p_seat_position, v_pickup_pin, 'pending'
    ) RETURNING id INTO v_booking_id;

    UPDATE rides
    SET available_seats = available_seats - 1,
        status = CASE WHEN available_seats - 1 = 0 THEN 'full' ELSE status END
    WHERE id = p_ride_id;

    RETURN v_booking_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 7. Tambah function mark_tip()
CREATE OR REPLACE FUNCTION mark_tip(p_booking_id UUID)
RETURNS void AS $$
    UPDATE bookings SET has_tipped = true
    WHERE id = p_booking_id
    AND passenger_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1);
$$ LANGUAGE sql SECURITY DEFINER;

-- 8. Tambah storage bucket qris-images
INSERT INTO storage.buckets (id, name, public)
VALUES ('qris-images', 'qris-images', true)
ON CONFLICT (id) DO NOTHING;

DROP POLICY IF EXISTS "Public QRIS images viewable" ON storage.objects;
CREATE POLICY "Public QRIS images viewable" ON storage.objects
    FOR SELECT USING (bucket_id = 'qris-images');

DROP POLICY IF EXISTS "Drivers can upload own QRIS image" ON storage.objects;
CREATE POLICY "Drivers can upload own QRIS image" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'qris-images' 
        AND auth.role() = 'authenticated'
    );
