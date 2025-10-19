-- Safely drop streak_count column if it exists. This migration is idempotent.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='habit_logs' AND column_name='streak_count'
    ) THEN
        -- Backfill any NULLs to 0 before dropping to be safe
        EXECUTE 'UPDATE habit_logs SET streak_count = 0 WHERE streak_count IS NULL';
        EXECUTE 'ALTER TABLE habit_logs DROP COLUMN streak_count';
    END IF;
END$$;