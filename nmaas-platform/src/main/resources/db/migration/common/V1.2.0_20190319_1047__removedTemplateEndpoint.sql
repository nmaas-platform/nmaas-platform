alter table nm_service_configuration_template rename to config_file_template;
alter table config_template rename to config_wizard_template;
alter table application rename column config_template_id to config_wizard_template_id;
alter table application rename column configuration_update_template_id to config_update_wizard_template_id;

create table app_configuration_spec (
  id bigint generated by default as identity,
  config_file_repository_required boolean not null,
  primary key (id)
);

create table app_configuration_spec_templates (
  app_configuration_spec_id bigint not null,
  templates_id bigint not null
);

alter table application add column app_configuration_spec_id bigint;
alter table app_deployment_spec drop column config_file_repository_required;
alter table application add constraint FKrmr9itdpv66wygo72wifvowum foreign key (app_configuration_spec_id) references app_configuration_spec;
alter table app_configuration_spec_templates add constraint FK7h85i58a15c64ve4h7xocpw00 foreign key (templates_id) references config_file_template;
alter table app_configuration_spec_templates add constraint FKnfjlc8no2snwbe80d9475tme0 foreign key (app_configuration_spec_id) references app_configuration_spec;