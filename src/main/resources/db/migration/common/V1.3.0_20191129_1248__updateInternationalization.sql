-- drop table internationalization if exists;

create table internationalization_simple (id bigint generated by default as identity, enabled boolean not null, language varchar(255) not null, primary key (id));
create table internationalization_simple_language_nodes (
    internationalization_simple_id bigint not null,
    content varchar(1024),
    node_key varchar(255)
);

alter table internationalization_simple add constraint UK_4vac9n7iv51f1o3u39n4nmgfe unique (language);
alter table internationalization_simple_language_nodes add constraint FKhkdvumx2has2syatom23tkkas foreign key (internationalization_simple_id) references internationalization_simple;
