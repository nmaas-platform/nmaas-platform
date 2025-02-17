ALTER TABLE  monitor_entry DROP COLUMN service_name;
ALTER TABLE monitor_entry ADD COLUMN service_name varchar(255) not null;

ALTER TABLE  monitor_entry DROP COLUMN status;
ALTER TABLE monitor_entry ADD COLUMN status varchar(255) not null;