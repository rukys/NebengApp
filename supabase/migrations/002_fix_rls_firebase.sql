-- ==============================================================================
-- MIGRATION: 002_fix_rls_firebase.sql
-- Fix Row Level Security (RLS) untuk arsitektur Firebase Auth + Supabase PostgREST
-- Karena autentikasi ditangani oleh Firebase (bukan Supabase GoTrue), request dari app
-- menggunakan anon key sehingga auth.uid() bernilai NULL di Supabase.
-- ==============================================================================

-- 1. USERS: Izinkan insert saat register dan update profil
DROP POLICY IF EXISTS "Users: insert own on register" ON users;
CREATE POLICY "Users: insert own on register" ON users
    FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "Users: update own" ON users;
CREATE POLICY "Users: update own" ON users
    FOR UPDATE USING (true);

DROP POLICY IF EXISTS "Users: read own" ON users;
DROP POLICY IF EXISTS "Users: read public profile" ON users;
CREATE POLICY "Users: allow read all" ON users
    FOR SELECT USING (true);

-- 2. NOTIFICATIONS: Izinkan read & update mark read
DROP POLICY IF EXISTS "Notifications: read own" ON notifications;
CREATE POLICY "Notifications: read own" ON notifications
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "Notifications: update own (mark read)" ON notifications;
CREATE POLICY "Notifications: update own (mark read)" ON notifications
    FOR UPDATE USING (true);

-- 3. BOOKINGS: Izinkan booking seat & update tip
DROP POLICY IF EXISTS "Bookings: passenger insert" ON bookings;
CREATE POLICY "Bookings: passenger insert" ON bookings
    FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "Bookings: passenger read own" ON bookings;
DROP POLICY IF EXISTS "Bookings: driver read bookings on own rides" ON bookings;
CREATE POLICY "Bookings: allow read" ON bookings
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "Bookings: passenger update has_tipped" ON bookings;
CREATE POLICY "Bookings: passenger update has_tipped" ON bookings
    FOR UPDATE USING (true);

-- 4. RIDES: Izinkan CRUD
DROP POLICY IF EXISTS "Rides: anyone read available" ON rides;
DROP POLICY IF EXISTS "Rides: driver CRUD own" ON rides;
CREATE POLICY "Rides: allow all" ON rides
    FOR ALL USING (true);

-- 5. VEHICLE REGISTRATIONS
DROP POLICY IF EXISTS "Vehicle: driver CRUD own" ON vehicle_registrations;
DROP POLICY IF EXISTS "Vehicle: anyone read verified" ON vehicle_registrations;
CREATE POLICY "Vehicle: allow all" ON vehicle_registrations
    FOR ALL USING (true);
