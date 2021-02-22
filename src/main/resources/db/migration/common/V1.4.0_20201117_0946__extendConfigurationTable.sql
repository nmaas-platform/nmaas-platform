alter table configuration add column app_instance_failure_emails varchar(255) NOT NULL DEFAULT '';
alter table configuration add column send_app_instance_failure_emails boolean NOT NULL DEFAULT FALSE;