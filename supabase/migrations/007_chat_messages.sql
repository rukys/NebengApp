-- ==============================================================================
-- 007_chat_messages.sql
-- Tabel pesan percakapan antar driver dan penumpang berbasis booking
-- ==============================================================================

CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id TEXT NOT NULL,
    sender_id TEXT NOT NULL,
    sender_name TEXT,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS chat_messages_booking_id_idx ON chat_messages (booking_id);
CREATE INDEX IF NOT EXISTS chat_messages_created_at_idx ON chat_messages (created_at);

ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;
CREATE POLICY "chat_messages: allow all" ON chat_messages FOR ALL USING (true) WITH CHECK (true);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'chat_messages'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE chat_messages;
    END IF;
EXCEPTION WHEN OTHERS THEN
    NULL;
END $$;
