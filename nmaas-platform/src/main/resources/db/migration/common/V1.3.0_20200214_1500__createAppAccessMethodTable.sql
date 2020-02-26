create table app_deployment_spec_access_methods (
                                                           app_deployment_spec_id bigint not null,
                                                           name varchar(255),
                                                           type varchar(255),
                                                           tag varchar(255)
);

alter table app_deployment_spec_access_methods
    add constraint FKj2eolmo5vws87iandhroavkd
        foreign key (app_deployment_spec_id)
            references app_deployment_spec;

insert into app_deployment_spec_access_methods(app_deployment_spec_id, name, type)
select id, 'Default', 'DEFAULT' from app_deployment_spec;