alter table gitlab_project add column webhook_id varchar(50);
alter table gitlab_project add column webhook_token varchar(50);
alter table gitlab_project add column clone_url varchar(255);