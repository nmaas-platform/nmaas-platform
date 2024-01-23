create table domain_group_managers
(
    domain_group_id bigint not null,
    user_id         bigint not null
)

alter table domain_group_managers
    add constraint FKkcd7k1w0xyty9ogqfrnk7em82
        foreign key (user_id)
            references users

alter table domain_group_managers
    add constraint FKbe4i8b8p6u3klyanhmw7i8ocn
        foreign key (domain_group_id)
            references domain_group