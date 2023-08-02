alter table app_access_method add column condition_type varchar(255);
alter table app_access_method add column condition varchar(255);

alter table service_access_method add column condition varchar(255);
alter table service_access_method add column enabled boolean default true;