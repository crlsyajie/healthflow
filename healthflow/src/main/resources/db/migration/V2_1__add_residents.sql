-- Add profile_image and is_resident fields to doctors table
ALTER TABLE doctors 
ADD COLUMN profile_image VARCHAR(255) DEFAULT NULL,
ADD COLUMN is_resident BOOLEAN NOT NULL DEFAULT false;

-- Create residents table
CREATE TABLE residents (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  doctor_id bigint(20) NOT NULL,
  program_name varchar(255) NOT NULL,
  program_start_date date DEFAULT NULL,
  program_end_date date DEFAULT NULL,
  residency_year int(11) DEFAULT NULL,
  supervisor_id bigint(20) DEFAULT NULL,
  supervisor_name varchar(255) DEFAULT NULL,
  notes varchar(1000) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY doctor_id (doctor_id),
  CONSTRAINT residents_doctor_fk FOREIGN KEY (doctor_id) REFERENCES doctors (id) ON DELETE CASCADE,
  CONSTRAINT residents_supervisor_fk FOREIGN KEY (supervisor_id) REFERENCES doctors (id) ON DELETE SET NULL
); 