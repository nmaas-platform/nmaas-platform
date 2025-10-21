alter table app_deployment_spec add column consumed_pods integer;
alter table app_deployment_spec add column consumed_cpu integer;
alter table app_deployment_spec add column consumed_memory integer;