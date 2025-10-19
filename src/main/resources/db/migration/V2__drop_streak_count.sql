-- Drop streak_count column if present (run after baseline)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='habit_logs' AND column_name='streak_count'
    ) THEN
        UPDATE habit_logs SET streak_count = 0 WHERE streak_count IS NULL;
        ALTER TABLE habit_logs DROP COLUMN streak_count;
    END IF;
END$$;