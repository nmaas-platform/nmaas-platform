alter table app_configuration_spec add terms_acceptance_required boolean not null default false;
alter table app_deployment add terms_acceptance_required boolean not null default false;