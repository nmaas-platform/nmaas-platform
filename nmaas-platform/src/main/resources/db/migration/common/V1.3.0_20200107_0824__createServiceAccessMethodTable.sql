create table kubernetes_nm_service_info_access_methods (
                                                           kubernetes_nm_service_info_id bigint not null,
                                                           name varchar(255),
                                                           type varchar(255),
                                                           url varchar(255)
);

alter table kubernetes_nm_service_info_access_methods
    add constraint FKj2eolmo5vws87eicirpfvvkd
        foreign key (kubernetes_nm_service_info_id)
            references kubernetes_nm_service_info;

insert into kubernetes_nm_service_info_access_methods
select id, 'Default', 'DEFAULT', service_external_url from nmaas.public.kubernetes_nm_service_info;

alter table kubernetes_nm_service_info drop column service_external_url;