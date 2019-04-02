alter table domain add column dcn_deployment_type varchar(255);
alter table dcn_info add column dcn_deployment_type varchar(255) not null default 'NONE';