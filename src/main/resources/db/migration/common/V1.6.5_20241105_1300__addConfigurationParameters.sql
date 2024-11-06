ALTER TABLE configuration ADD COLUMN bulk_deployment_job_cron varchar(255) DEFAULT '0 */1 * * * ?' not null ;
ALTER TABLE configuration ADD COLUMN parallel_deployments_limit integer DEFAULT 2 not null ;
