alter table k_cluster ADD COLUMN state varchar(255) DEFAULT 'UNKNOWN';
alter table k_cluster ADD COLUMN current_state_since timestamp(6) with time zone not null;

ALTER TABLE configuration ADD COLUMN health_check_job_cron varchar(255);


