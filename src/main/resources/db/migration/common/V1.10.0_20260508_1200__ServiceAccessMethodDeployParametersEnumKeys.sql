alter table service_access_method_deploy_parameters alter column deploy_parameters_key type varchar(255);

update service_access_method_deploy_parameters set deploy_parameters_key = 'INGRESS_HOSTS' where deploy_parameters_key = '0';
update service_access_method_deploy_parameters set deploy_parameters_key = 'INGRESS_TLS_ENABLED' where deploy_parameters_key = '1';
update service_access_method_deploy_parameters set deploy_parameters_key = 'INGRESS_CLASS' where deploy_parameters_key = '2';
update service_access_method_deploy_parameters set deploy_parameters_key = 'INGRESS_LETSENCRYPT' where deploy_parameters_key = '3';
update service_access_method_deploy_parameters set deploy_parameters_key = 'INGRESS_WILDCARD_OR_ISSUER' where deploy_parameters_key = '4';
update service_access_method_deploy_parameters set deploy_parameters_key = 'INGRESS_ENABLED' where deploy_parameters_key = '5';
update service_access_method_deploy_parameters set deploy_parameters_key = 'INGRESS_TLS_HOSTS' where deploy_parameters_key = '6';
update service_access_method_deploy_parameters set deploy_parameters_key = 'K8S_SERVICE_SUFFIX' where deploy_parameters_key = '7';
update service_access_method_deploy_parameters set deploy_parameters_key = 'K8S_SERVICE_PORT' where deploy_parameters_key = '8';
update service_access_method_deploy_parameters set deploy_parameters_key = 'ACCESS_USER' where deploy_parameters_key = '9';
update service_access_method_deploy_parameters set deploy_parameters_key = 'EXTERNAL_SERVICE_SUFFIX' where deploy_parameters_key = '10';
