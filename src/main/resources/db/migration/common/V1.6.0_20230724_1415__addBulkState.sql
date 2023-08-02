alter table bulk_deployment_entry add column state varchar(255);
alter table bulk_deployment_entry rename column type to bulk_type;
alter table bulk_deployment_entry alter column bulk_type type varchar(255);
alter table bulk_deployment_entry delete column successful;

alter table bulk_deployment alter column state type varchar(255);
alter table bulk_deployment rename column type to bulk_type;
alter table bulk_deployment alter column bulk_type type varchar(255);