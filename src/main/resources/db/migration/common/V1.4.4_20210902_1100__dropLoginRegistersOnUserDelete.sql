alter table user_login_register drop constraint FKbsf876ctwu2lon29tqyj52xuh;

alter table user_login_register
    add constraint FKbsf876ctwu2lon29tqyj52xuh
        foreign key (user_id)
            references users on delete cascade;