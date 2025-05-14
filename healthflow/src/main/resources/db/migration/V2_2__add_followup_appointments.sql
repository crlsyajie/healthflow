-- Add follow-up appointment fields to appointments table
ALTER TABLE appointments 
ADD COLUMN parent_appointment_id bigint(20) DEFAULT NULL,
ADD COLUMN is_follow_up BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN follow_up_interval_days INT DEFAULT NULL,
ADD CONSTRAINT appointments_parent_fk FOREIGN KEY (parent_appointment_id) REFERENCES appointments (id) ON DELETE SET NULL; 