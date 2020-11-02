create table sshkey_entity_aud
(
    id          bigint  not null,
    rev         integer not null,
    revtype     tinyint,
    fingerprint varchar(255),
    key         TEXT,
    name        varchar(255),
    owner_id    bigint,
    primary key (id, rev)
);

alter table sshkey_entity_aud
    add constraint FKm5islbxgw2jmifmve5yny93f5
        foreign key (rev)
            references revinfo;

create table user_sshkey_entity_aud
(
    rev      integer not null,
    owner_id bigint  not null,
    id       bigint  not null,
    revtype  tinyint,
    primary key (rev, owner_id, id)
);

alter table user_sshkey_entity_aud
    add constraint FKmdm4twg5xu7gm645q4fpl23o6
        foreign key (rev)
            references revinfo;