-- Drop foreign key constraint if it exists
SET FOREIGN_KEY_CHECKS=0;

-- Alter doctors table to make department_id nullable
ALTER TABLE doctors MODIFY COLUMN department_id BIGINT DEFAULT NULL;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS=1;