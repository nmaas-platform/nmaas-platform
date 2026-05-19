alter table app_deployment_history
    add column user_id bigint;

alter table app_deployment_history
    add constraint fk_app_deployment_history_user
        foreign key (user_id)
            references users(id);