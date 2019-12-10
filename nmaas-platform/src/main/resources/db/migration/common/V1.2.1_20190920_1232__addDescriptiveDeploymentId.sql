alter table app_deployment add column instance_id bigint;
alter table nm_service_info add column descriptive_deployment_id bytea;

update nm_service_info set descriptive_deployment_id = deployment_id where descriptive_deployment_id is null;