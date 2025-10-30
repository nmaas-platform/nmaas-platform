alter table webhooks add column domain_id bigint;

alter table webhooks add constraint fk_webhooks_domain foreign key (domain_id) references domain(id);