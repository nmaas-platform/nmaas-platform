ALTER TABLE configuration ADD COLUMN bulk_deployment_job_cron varchar(255) not null;
ALTER TABLE configuration ADD COLUMN parallel_deployments_limit integer not null;
