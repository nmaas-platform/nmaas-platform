alter table app_deployment add column descriptive_deployment_id bytea;

update app_deployment set descriptive_deployment_id = deployment_id where descriptive_deployment_id is null;