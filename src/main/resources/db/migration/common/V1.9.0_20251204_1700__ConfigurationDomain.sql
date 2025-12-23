alter table configuration add column default_domain_for_sso_users bigint;

alter table configuration add constraint fk_configuration_domain foreign key (default_domain_for_sso_users) references domain(id) on delete set null;