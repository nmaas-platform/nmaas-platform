ALTER TABLE  monitor_entry DROP COLUMN service_name;
ALTER TABLE monitor_entry ADD COLUMN service_name varchar(255) not null;