ALTER TABLE stock ADD COLUMN change_rate double precision NULL;
ALTER TABLE stock  ADD COLUMN volume BIGINT NULL;

ALTER TABLE stock ALTER COLUMN deleted SET DEFAULT false;
UPDATE stock SET deleted = false WHERE deleted IS NULL;

ALTER TABLE stock ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE stock ALTER COLUMN updated_at SET DEFAULT now();

