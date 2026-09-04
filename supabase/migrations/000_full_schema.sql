-- ==============================================================================
-- NEBENG DATABASE MIGRATION - MASTER FULL SCHEMA (FREE COMMUNITY RIDESHARING)
-- Platform: Supabase PostgreSQL with PostGIS
-- Last Updated: 2026-09-04
-- ==============================================================================

-- 0. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ==============================================================================
-- 1. TABLES
-- ==============================================================================

-- 1.1 USERS
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    phone_number TEXT UNIQUE NOT NULL,
    email TEXT,
    avatar_url TEXT,
    whatsapp_number TEXT,
    office_address TEXT,
    office_location GEOGRAPHY(POINT, 4326),
    bio TEXT,
    qris_image_url TEXT,                     -- Foto QRIS driver untuk tampung tip (opsional)
    ktp_verified BOOLEAN DEFAULT FALSE,
    office_verified BOOLEAN DEFAULT FALSE,
    average_rating DECIMAL(2,1) DEFAULT 0.0,
    total_trips INTEGER DEFAULT 0,
    role TEXT DEFAULT 'passenger' CHECK (role IN ('passenger', 'driver', 'both')),
    fcm_token TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 1.2 VEHICLE REGISTRATIONS
CREATE TABLE IF NOT EXISTS vehicle_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    brand TEXT NOT NULL,
    model TEXT NOT NULL,
    plate TEXT UNIQUE NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('car', 'motorcycle')),
    color TEXT NOT NULL,
    year INTEGER NOT NULL,
    stnk_url TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 1.3 RIDES
CREATE TABLE IF NOT EXISTS rides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_id UUID REFERENCES vehicle_registrations(id) ON DELETE SET NULL,
    vehicle_brand TEXT NOT NULL,
    vehicle_model TEXT NOT NULL,
    vehicle_plate TEXT NOT NULL,
    vehicle_type TEXT NOT NULL CHECK (vehicle_type IN ('car', 'motorcycle')),
    max_passengers INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    pickup_address TEXT NOT NULL,
    pickup_location GEOGRAPHY(POINT, 4326) NOT NULL,
    dropoff_address TEXT NOT NULL,
    dropoff_location GEOGRAPHY(POINT, 4326) NOT NULL,
    route_polyline TEXT,
    departure_time TIMESTAMPTZ NOT NULL,
    -- TIDAK ADA price_per_seat -- Nebeng 100% gratis
    status TEXT DEFAULT 'available' CHECK (status IN ('available', 'full', 'ongoing', 'done', 'cancelled')),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 1.4 BOOKINGS
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_id UUID NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    passenger_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seat_position TEXT NOT NULL CHECK (seat_position IN ('front_left', 'rear_left', 'rear_center', 'rear_right', 'pillion')),
    pickup_pin TEXT NOT NULL,               -- 6-digit PIN konfirmasi jemput
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'picked_up', 'done', 'cancelled')),
    has_tipped BOOLEAN DEFAULT FALSE,       -- true jika user memilih kasih tip (jumlah tidak disimpan)
    passenger_rating INTEGER CHECK (passenger_rating BETWEEN 1 AND 5),
    driver_rating INTEGER CHECK (driver_rating BETWEEN 1 AND 5),
    passenger_review TEXT,
    driver_review TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 1.5 NOTIFICATIONS
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category TEXT NOT NULL CHECK (category IN ('trip', 'review', 'system')),
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    action_url TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 1.6 TRIP LOCATIONS (Realtime tracking)
CREATE TABLE IF NOT EXISTS trip_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- 2. INDEXES
-- ==============================================================================

-- rides: geo-query & lookup
CREATE INDEX IF NOT EXISTS rides_pickup_location_idx ON rides USING GIST (pickup_location);
CREATE INDEX IF NOT EXISTS rides_dropoff_location_idx ON rides USING GIST (dropoff_location);
CREATE INDEX IF NOT EXISTS rides_departure_time_idx ON rides (departure_time);
CREATE INDEX IF NOT EXISTS rides_status_idx ON rides (status);
CREATE INDEX IF NOT EXISTS rides_driver_id_idx ON rides (driver_id);

-- bookings: query per user & per ride
CREATE INDEX IF NOT EXISTS bookings_passenger_id_idx ON bookings (passenger_id);
CREATE INDEX IF NOT EXISTS bookings_ride_id_idx ON bookings (ride_id);
CREATE INDEX IF NOT EXISTS bookings_status_idx ON bookings (status);

-- notifications: query per user (unread)
CREATE INDEX IF NOT EXISTS notifications_user_id_idx ON notifications (user_id);
CREATE INDEX IF NOT EXISTS notifications_is_read_idx ON notifications (is_read) WHERE is_read = false;

-- trip_locations: realtime lookup
CREATE UNIQUE INDEX IF NOT EXISTS trip_locations_booking_id_idx ON trip_locations (booking_id);

-- ==============================================================================
-- 3. ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE vehicle_registrations ENABLE ROW LEVEL SECURITY;
ALTER TABLE rides ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE trip_locations ENABLE ROW LEVEL SECURITY;

-- 3.1 USERS
CREATE POLICY "Users: read own" ON users
    FOR SELECT USING (firebase_uid = auth.uid()::text);

CREATE POLICY "Users: update own" ON users
    FOR UPDATE USING (firebase_uid = auth.uid()::text);

CREATE POLICY "Users: insert own on register" ON users
    FOR INSERT WITH CHECK (firebase_uid = auth.uid()::text);

CREATE POLICY "Users: read public profile" ON users
    FOR SELECT USING (true);

-- 3.2 VEHICLE REGISTRATIONS
CREATE POLICY "Vehicle: driver CRUD own" ON vehicle_registrations
    FOR ALL USING (
        driver_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
    );

CREATE POLICY "Vehicle: anyone read verified" ON vehicle_registrations
    FOR SELECT USING (is_verified = true);

-- 3.3 RIDES
CREATE POLICY "Rides: anyone read available" ON rides
    FOR SELECT USING (status = 'available');

CREATE POLICY "Rides: driver CRUD own" ON rides
    FOR ALL USING (
        driver_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
    );

-- 3.4 BOOKINGS
CREATE POLICY "Bookings: passenger read own" ON bookings
    FOR SELECT USING (
        passenger_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
    );

CREATE POLICY "Bookings: driver read bookings on own rides" ON bookings
    FOR SELECT USING (
        ride_id IN (
            SELECT id FROM rides
            WHERE driver_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
        )
    );

CREATE POLICY "Bookings: passenger insert" ON bookings
    FOR INSERT WITH CHECK (
        passenger_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
    );

CREATE POLICY "Bookings: passenger update has_tipped" ON bookings
    FOR UPDATE USING (
        passenger_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
    );

-- 3.5 NOTIFICATIONS
CREATE POLICY "Notifications: read own" ON notifications
    FOR SELECT USING (
        user_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
    );

CREATE POLICY "Notifications: update own (mark read)" ON notifications
    FOR UPDATE USING (
        user_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
    );

-- 3.6 TRIP LOCATIONS
CREATE POLICY "TripLocations: read if involved" ON trip_locations
    FOR SELECT USING (
        booking_id IN (
            SELECT id FROM bookings
            WHERE passenger_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
            OR ride_id IN (
                SELECT id FROM rides
                WHERE driver_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1)
            )
        )
    );

-- ==============================================================================
-- 4. RPC / STORED FUNCTIONS
-- ==============================================================================

-- 4.1 search_nearby_rides()
CREATE OR REPLACE FUNCTION search_nearby_rides(
    user_lat DOUBLE PRECISION,
    user_lng DOUBLE PRECISION,
    radius_meters INTEGER DEFAULT 500,
    p_vehicle_type TEXT DEFAULT NULL,
    departure_date DATE DEFAULT CURRENT_DATE
)
RETURNS SETOF rides AS $$
    SELECT r.*
    FROM rides r
    WHERE
        ST_DWithin(
            r.pickup_location,
            ST_MakePoint(user_lng, user_lat)::geography,
            radius_meters
        )
        AND (p_vehicle_type IS NULL OR r.vehicle_type = p_vehicle_type)
        AND DATE(r.departure_time AT TIME ZONE 'Asia/Jakarta') = departure_date
        AND r.status = 'available'
        AND r.available_seats > 0
    ORDER BY
        ST_Distance(r.pickup_location, ST_MakePoint(user_lng, user_lat)::geography) ASC,
        r.departure_time ASC;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- 4.2 book_seat() (Atomic concurrency row-lock)
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
    -- Lock row untuk prevent race condition
    SELECT available_seats INTO v_current_seats
    FROM rides WHERE id = p_ride_id FOR UPDATE;

    IF v_current_seats <= 0 THEN
        RAISE EXCEPTION 'Kursi sudah penuh';
    END IF;

    -- Check seat position belum ditempati
    IF EXISTS (
        SELECT 1 FROM bookings
        WHERE ride_id = p_ride_id
        AND seat_position = p_seat_position
        AND status NOT IN ('cancelled')
    ) THEN
        RAISE EXCEPTION 'Kursi sudah dipesan';
    END IF;

    -- Generate PIN 6 digit
    v_pickup_pin := LPAD(FLOOR(RANDOM() * 1000000)::TEXT, 6, '0');

    -- Insert booking (gratis, tanpa kolom harga)
    INSERT INTO bookings (
        ride_id, passenger_id, seat_position, pickup_pin, status
    ) VALUES (
        p_ride_id, p_passenger_id, p_seat_position, v_pickup_pin, 'pending'
    ) RETURNING id INTO v_booking_id;

    -- Decrement sisa kursi
    UPDATE rides
    SET available_seats = available_seats - 1,
        status = CASE WHEN available_seats - 1 = 0 THEN 'full' ELSE status END
    WHERE id = p_ride_id;

    RETURN v_booking_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 4.3 update_user_rating()
CREATE OR REPLACE FUNCTION update_user_rating(p_user_id UUID)
RETURNS void AS $$
    UPDATE users SET
        average_rating = (
            SELECT ROUND(AVG(driver_rating)::numeric, 1)
            FROM bookings
            WHERE ride_id IN (SELECT id FROM rides WHERE driver_id = p_user_id)
            AND driver_rating IS NOT NULL
        ),
        total_trips = (
            SELECT COUNT(*) FROM bookings
            WHERE ride_id IN (SELECT id FROM rides WHERE driver_id = p_user_id)
            AND status = 'done'
        )
    WHERE id = p_user_id;
$$ LANGUAGE sql SECURITY DEFINER;

-- 4.4 mark_tip() — Set has_tipped = true
CREATE OR REPLACE FUNCTION mark_tip(p_booking_id UUID)
RETURNS void AS $$
    UPDATE bookings SET has_tipped = true
    WHERE id = p_booking_id
    AND passenger_id = (SELECT id FROM users WHERE firebase_uid = auth.uid()::text LIMIT 1);
$$ LANGUAGE sql SECURITY DEFINER;

-- ==============================================================================
-- 5. TRIGGERS
-- ==============================================================================

-- 5.1 Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS users_updated_at ON users;
CREATE TRIGGER users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

DROP TRIGGER IF EXISTS bookings_updated_at ON bookings;
CREATE TRIGGER bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- 5.2 Auto-update rides status ke 'full' jika available_seats = 0
CREATE OR REPLACE FUNCTION check_ride_availability()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.available_seats = 0 THEN
        NEW.status = 'full';
    ELSIF NEW.available_seats > 0 AND OLD.status = 'full' THEN
        NEW.status = 'available';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS rides_check_availability ON rides;
CREATE TRIGGER rides_check_availability
    BEFORE UPDATE OF available_seats ON rides
    FOR EACH ROW EXECUTE FUNCTION check_ride_availability();

-- ==============================================================================
-- 6. STORAGE BUCKETS
-- ==============================================================================
INSERT INTO storage.buckets (id, name, public)
VALUES 
    ('avatars', 'avatars', true),
    ('ktp-documents', 'ktp-documents', false),
    ('stnk-documents', 'stnk-documents', false),
    ('qris-images', 'qris-images', true)
ON CONFLICT (id) DO NOTHING;

-- Storage RLS
DROP POLICY IF EXISTS "Public avatars are viewable by everyone" ON storage.objects;
CREATE POLICY "Public avatars are viewable by everyone" ON storage.objects
    FOR SELECT USING (bucket_id = 'avatars');

DROP POLICY IF EXISTS "Users can upload own avatar" ON storage.objects;
CREATE POLICY "Users can upload own avatar" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'avatars' 
        AND auth.role() = 'authenticated'
    );

DROP POLICY IF EXISTS "Public QRIS images viewable" ON storage.objects;
CREATE POLICY "Public QRIS images viewable" ON storage.objects
    FOR SELECT USING (bucket_id = 'qris-images');

DROP POLICY IF EXISTS "Drivers can upload own QRIS image" ON storage.objects;
CREATE POLICY "Drivers can upload own QRIS image" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'qris-images' 
        AND auth.role() = 'authenticated'
    );
