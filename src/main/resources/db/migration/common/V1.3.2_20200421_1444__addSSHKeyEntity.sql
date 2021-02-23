create table sshkey_entity (
        id bigint generated by default as identity,
        fingerprint varchar(255),
        key text,
        name varchar(255),
        owner_id bigint,
        primary key (id)
    );

alter table sshkey_entity
       add constraint FKhx652dtyvl6yljedf0dofy2gc
       foreign key (owner_id)
       references users
       on delete cascade;