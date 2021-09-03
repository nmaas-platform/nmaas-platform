-- set admin as default owner after migration
alter table application_base add column owner varchar(255) not null default 'admin';

alter table application drop column owner;