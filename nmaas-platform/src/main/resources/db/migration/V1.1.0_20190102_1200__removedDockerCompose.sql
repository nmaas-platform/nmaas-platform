alter table k_cluster_attach_point add column vlan_number integer;
alter table k_cluster_attach_point add column subnet varchar(255);
alter table k_cluster_attach_point add column gateway varchar(255);

alter table app_deployment_spec drop column docker_compose_file_template_id;

drop table docker_compose_file;
drop table docker_compose_file_template;
drop table docker_compose_file_template_dcn_attached_containers;
drop table docker_compose_service;
drop table docker_compose_service_component;
drop table docker_compose_service_service_components;
drop table docker_host;
drop table docker_host_attach_point;
drop table docker_host_network;
drop table docker_host_state;
drop table docker_host_state_address_assignments;
drop table docker_host_state_port_assignments;
drop table docker_host_state_vlan_assignments;
drop table docker_network_ipam_spec;
drop table docker_compose_nm_service_info;
drop table docker_host_network_assigned_addresses;