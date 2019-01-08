alter table application drop column additional_parameters_template_id;
alter table application drop column additional_mandatory_template_id;
alter table application add column configuration_update_template_id bigint;
alter table application add constraint FK9761cddsjbnr4lcmjgmm8ylc4 foreign key (configuration_update_template_id) references config_template;

