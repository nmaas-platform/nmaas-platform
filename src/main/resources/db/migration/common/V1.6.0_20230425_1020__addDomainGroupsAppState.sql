create table domain_group_application_state_per_domain (
   domain_group_id bigint not null,
   application_base_id bigint,
   enabled boolean not null,
   pv_storage_size_limit bigint not null
);

alter table domain_group_application_state_per_domain
   add constraint FK4rbow14ni84p8bwqotvgq3k0n
   foreign key (application_base_id)
   references application_base
   on delete cascade;

alter table domain_group_application_state_per_domain
   add constraint FKbws55oybs107xffds9i8j4t7v
   foreign key (domain_group_id)
   references domain_group;