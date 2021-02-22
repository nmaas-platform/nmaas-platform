create table user_login_register (
                                     date timestamp with time zone not null,
                                     host varchar(255),
                                     remote_address varchar(255),
                                     type varchar(255),
                                     user_agent varchar(255),
                                     user_id bigint not null,
                                     primary key (date, user_id)
);

alter table user_login_register
    add constraint FKbsf876ctwu2lon29tqyj52xuh
        foreign key (user_id)
            references users;