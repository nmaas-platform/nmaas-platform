alter table app_deployment_spec_deploy_parameters alter column deploy_parameters_key type varchar(255);

update app_deployment_spec_deploy_parameters set deploy_parameters_key = 'SMTP_HOSTNAME' where deploy_parameters_key = '0';
update app_deployment_spec_deploy_parameters set deploy_parameters_key = 'SMTP_PORT' where deploy_parameters_key = '1';
update app_deployment_spec_deploy_parameters set deploy_parameters_key = 'SMTP_USERNAME' where deploy_parameters_key = '2';
update app_deployment_spec_deploy_parameters set deploy_parameters_key = 'SMTP_PASSWORD' where deploy_parameters_key = '3';
update app_deployment_spec_deploy_parameters set deploy_parameters_key = 'DOMAIN_CODENAME' where deploy_parameters_key = '4';
update app_deployment_spec_deploy_parameters set deploy_parameters_key = 'BASE_URL' where deploy_parameters_key = '5';
update app_deployment_spec_deploy_parameters set deploy_parameters_key = 'RELEASE_NAME' where deploy_parameters_key = '6';