create table app_deployment_spec_global_deploy_parameters (
    app_deployment_spec_id bigint not null,
    global_deploy_parameters varchar(255) not null,
    global_deploy_parameters_key varchar(255) not null,
    primary key (app_deployment_spec_id, global_deploy_parameters_key)
);