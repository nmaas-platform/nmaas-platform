create table app_instance_members (
        app_instance_id bigint not null,
        members_id bigint not null,
        primary key (app_instance_id, members_id)
    );

alter table app_instance_members
       add constraint FKkjsy18fygbc7xckm5xjp850ws
       foreign key (members_id)
       references users;

alter table app_instance_members
       add constraint FKl7r33losltsy0tyga2w22r0vw
       foreign key (app_instance_id)
       references app_instance;