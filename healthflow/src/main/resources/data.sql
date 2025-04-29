-- If any doctors have null department_id, update them with department 1
UPDATE doctors SET department_id = 1 WHERE department_id IS NULL;