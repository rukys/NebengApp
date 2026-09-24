-- ==============================================================================
-- 006_push_notifications_trigger.sql
-- Trigger otomatis penambahan notifikasi pada tabel notifications dan push trigger
-- ==============================================================================

-- 1. Trigger saat booking baru diajukan (Notify Driver)
CREATE OR REPLACE FUNCTION notify_on_booking_created()
RETURNS TRIGGER AS $$
DECLARE
    v_driver_id UUID;
    v_passenger_name TEXT;
    v_pickup_address TEXT;
BEGIN
    -- Dapatkan driver_id dan rute
    SELECT driver_id, pickup_address INTO v_driver_id, v_pickup_address
    FROM rides WHERE id = NEW.ride_id LIMIT 1;

    -- Dapatkan nama penumpang
    SELECT full_name INTO v_passenger_name
    FROM users WHERE id = NEW.passenger_id LIMIT 1;

    IF v_driver_id IS NOT NULL THEN
        INSERT INTO notifications (
            user_id,
            category,
            title,
            body,
            action_url,
            is_read,
            created_at
        ) VALUES (
            v_driver_id,
            'trip',
            'Permintaan Tebengan Masuk',
            COALESCE(v_passenger_name, 'Penumpang') || ' ingin nebeng menuju ' || COALESCE(v_pickup_address, 'tujuan') || '. Ketuk untuk tinjau.',
            'nebeng://activity',
            FALSE,
            NOW()
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_booking_created ON bookings;
CREATE TRIGGER trg_booking_created
    AFTER INSERT ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION notify_on_booking_created();

-- 2. Trigger saat status booking diperbarui (Notify Passenger on confirm / cancel)
CREATE OR REPLACE FUNCTION notify_on_booking_status_changed()
RETURNS TRIGGER AS $$
DECLARE
    v_driver_name TEXT;
    v_vehicle_info TEXT;
BEGIN
    IF OLD.status != NEW.status THEN
        -- Status berubah jadi 'confirmed'
        IF NEW.status = 'confirmed' THEN
            SELECT u.full_name, r.vehicle_model INTO v_driver_name, v_vehicle_info
            FROM rides r
            JOIN users u ON u.id = r.driver_id
            WHERE r.id = NEW.ride_id LIMIT 1;

            INSERT INTO notifications (
                user_id,
                category,
                title,
                body,
                action_url,
                is_read,
                created_at
            ) VALUES (
                NEW.passenger_id,
                'trip',
                'Tebengan Dikonfirmasi! 🎉',
                COALESCE(v_driver_name, 'Driver') || ' telah menyetujui tebenganmu. Pantau posisi pengemudi di Live Tracking.',
                'nebeng://trip/' || NEW.id || '/tracking',
                FALSE,
                NOW()
            );

        -- Status berubah jadi 'cancelled'
        ELSIF NEW.status = 'cancelled' THEN
            INSERT INTO notifications (
                user_id,
                category,
                title,
                body,
                action_url,
                is_read,
                created_at
            ) VALUES (
                NEW.passenger_id,
                'trip',
                'Tebengan Dibatalkan',
                'Permintaan tebengan tidak dapat dipenuhi oleh pengemudi.',
                'nebeng://activity',
                FALSE,
                NOW()
            );

        -- Status berubah jadi 'completed'
        ELSIF NEW.status = 'completed' THEN
            INSERT INTO notifications (
                user_id,
                category,
                title,
                body,
                action_url,
                is_read,
                created_at
            ) VALUES (
                NEW.passenger_id,
                'review',
                'Perjalanan Selesai: Beri Penilaian',
                'Bagaimana perjalanan tebenganmu? Beri rating bintang untuk pengemudi!',
                'nebeng://trip/' || NEW.id || '/done',
                FALSE,
                NOW()
            );
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_booking_status ON bookings;
CREATE TRIGGER trg_booking_status
    AFTER UPDATE OF status ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION notify_on_booking_status_changed();
