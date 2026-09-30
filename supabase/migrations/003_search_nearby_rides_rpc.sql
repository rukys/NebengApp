-- ==============================================================================
-- MIGRATION: 003_search_nearby_rides_rpc.sql
-- Enhanced search_nearby_rides RPC with driver join & coordinates projection
-- ==============================================================================

DROP FUNCTION IF EXISTS search_nearby_rides(DOUBLE PRECISION, DOUBLE PRECISION, INTEGER, TEXT, DATE);

CREATE OR REPLACE FUNCTION search_nearby_rides(
    user_lat DOUBLE PRECISION,
    user_lng DOUBLE PRECISION,
    radius_meters INTEGER DEFAULT 10000,
    p_vehicle_type TEXT DEFAULT NULL,
    departure_date DATE DEFAULT NULL
)
RETURNS TABLE (
    id UUID,
    driver_id UUID,
    driver_name TEXT,
    driver_gender TEXT,
    driver_avatar TEXT,
    driver_rating NUMERIC,
    driver_total_trips INTEGER,
    vehicle_brand TEXT,
    vehicle_model TEXT,
    vehicle_plate TEXT,
    vehicle_type TEXT,
    max_passengers INTEGER,
    available_seats INTEGER,
    pickup_address TEXT,
    pickup_lat DOUBLE PRECISION,
    pickup_lng DOUBLE PRECISION,
    dropoff_address TEXT,
    dropoff_lat DOUBLE PRECISION,
    dropoff_lng DOUBLE PRECISION,
    departure_time TIMESTAMPTZ,
    notes TEXT,
    distance_meters DOUBLE PRECISION
) AS $$
    SELECT 
        r.id,
        r.driver_id,
        COALESCE(u.full_name, 'Pengemudi Nebeng')::TEXT AS driver_name,
        'male'::TEXT AS driver_gender,
        u.avatar_url::TEXT AS driver_avatar,
        COALESCE(u.average_rating, 5.0)::NUMERIC AS driver_rating,
        COALESCE(u.total_trips, 0)::INTEGER AS driver_total_trips,
        r.vehicle_brand::TEXT,
        r.vehicle_model::TEXT,
        r.vehicle_plate::TEXT,
        r.vehicle_type::TEXT,
        r.max_passengers,
        r.available_seats,
        r.pickup_address::TEXT,
        ST_Y(r.pickup_location::geometry)::DOUBLE PRECISION AS pickup_lat,
        ST_X(r.pickup_location::geometry)::DOUBLE PRECISION AS pickup_lng,
        r.dropoff_address::TEXT,
        ST_Y(r.dropoff_location::geometry)::DOUBLE PRECISION AS dropoff_lat,
        ST_X(r.dropoff_location::geometry)::DOUBLE PRECISION AS dropoff_lng,
        r.departure_time,
        r.notes::TEXT,
        ST_Distance(r.pickup_location, ST_MakePoint(user_lng, user_lat)::geography)::DOUBLE PRECISION AS distance_meters
    FROM rides r
    LEFT JOIN users u ON u.id = r.driver_id
    WHERE
        ST_DWithin(
            r.pickup_location,
            ST_MakePoint(user_lng, user_lat)::geography,
            radius_meters
        )
        AND (p_vehicle_type IS NULL OR r.vehicle_type = p_vehicle_type)
        AND (departure_date IS NULL OR DATE(r.departure_time AT TIME ZONE 'Asia/Jakarta') = departure_date)
        AND r.status = 'available'
        AND r.available_seats > 0
    ORDER BY
        distance_meters ASC,
        r.departure_time ASC;
$$ LANGUAGE sql STABLE SECURITY DEFINER;
