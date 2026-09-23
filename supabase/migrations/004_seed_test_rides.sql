-- ==============================================================================
-- MIGRATION: 004_seed_test_rides.sql
-- Seed sample drivers and rides for Jakarta commuting (Tebet, SCBD, Kuningan)
-- ==============================================================================

DO $$
DECLARE
    v_driver1_id UUID;
    v_driver2_id UUID;
    v_driver3_id UUID;
    v_driver4_id UUID;
BEGIN
    -- 1. Upsert Sample Drivers
    INSERT INTO users (id, firebase_uid, phone_number, full_name, role, average_rating, total_trips, bio)
    VALUES 
        (gen_random_uuid(), 'seed-driver-andi', '+6281100000001', 'Andi Pratama', 'driver', 4.9, 128, 'Komuter SCBD tiap jam 7 pagi'),
        (gen_random_uuid(), 'seed-driver-reza', '+6281100000002', 'Reza Hendra', 'driver', 4.8, 95, 'Rider NMAX Tebet - Rasuna Said'),
        (gen_random_uuid(), 'seed-driver-siti', '+6281100000003', 'Siti Rahma', 'driver', 5.0, 64, 'Karyawan swasta Senayan'),
        (gen_random_uuid(), 'seed-driver-budi', '+6281100000004', 'Budi Santoso', 'driver', 4.8, 42, 'Santai & tepat waktu')
    ON CONFLICT (phone_number) DO UPDATE SET full_name = EXCLUDED.full_name;

    SELECT id INTO v_driver1_id FROM users WHERE phone_number = '+6281100000001' LIMIT 1;
    SELECT id INTO v_driver2_id FROM users WHERE phone_number = '+6281100000002' LIMIT 1;
    SELECT id INTO v_driver3_id FROM users WHERE phone_number = '+6281100000003' LIMIT 1;
    SELECT id INTO v_driver4_id FROM users WHERE phone_number = '+6281100000004' LIMIT 1;

    -- 2. Clean old seed rides
    DELETE FROM rides WHERE driver_id IN (v_driver1_id, v_driver2_id, v_driver3_id, v_driver4_id);

    -- 3. Insert Sample Rides (Tebet & sekitarnya)
    INSERT INTO rides (
        driver_id, vehicle_brand, vehicle_model, vehicle_plate, vehicle_type,
        max_passengers, available_seats, pickup_address, pickup_location,
        dropoff_address, dropoff_location, departure_time, status, notes
    ) VALUES
    (
        v_driver1_id, 'Toyota', 'Avanza Silver', 'B 1234 ABC', 'car',
        4, 2, 'Stasiun Tebet (Pintu Barat)', ST_MakePoint(106.8580, -6.2297)::geography,
        'SCBD Sudirman (Lot 8 & Pasific)', ST_MakePoint(106.8080, -6.2274)::geography,
        NOW() + INTERVAL '1 hour', 'available', 'Non-smoking, AC Dingin, Kantor SCBD'
    ),
    (
        v_driver2_id, 'Yamaha', 'NMAX Hitam', 'B 5678 XYZ', 'motorcycle',
        1, 1, 'Stasiun Tebet', ST_MakePoint(106.8582, -6.2295)::geography,
        'Kuningan City Mall', ST_MakePoint(106.8290, -6.2240)::geography,
        NOW() + INTERVAL '30 minutes', 'available', 'Helm driver ada, Jas hujan ready'
    ),
    (
        v_driver3_id, 'Honda', 'HR-V Putih', 'B 9999 SBD', 'car',
        3, 2, 'Pancoran Riverside', ST_MakePoint(106.8600, -6.2400)::geography,
        'Menara Mandiri Senayan', ST_MakePoint(106.8050, -6.2230)::geography,
        NOW() + INTERVAL '2 hours', 'available', 'Khusus Wanita, Musik santai'
    ),
    (
        v_driver4_id, 'Honda', 'PCX Abu-abu', 'B 3456 DEF', 'motorcycle',
        1, 1, 'Stasiun Cawang', ST_MakePoint(106.8640, -6.2420)::geography,
        'Plaza Indonesia Sudirman', ST_MakePoint(106.8220, -6.1920)::geography,
        NOW() + INTERVAL '45 minutes', 'available', 'Lewat jalur cepat, aman'
    );
END $$;
