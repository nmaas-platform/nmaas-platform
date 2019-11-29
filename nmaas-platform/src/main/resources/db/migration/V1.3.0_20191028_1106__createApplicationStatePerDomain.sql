
--
CREATE TABLE domain_application_state_per_domain
(
    domain_id bigint NOT NULL,
    application_base_id bigint,
    enabled boolean NOT NULL,

    CONSTRAINT fk5nrhw0d2a813xeiwg2tldnpeh FOREIGN KEY (application_base_id)
        REFERENCES application_base (id)
        ON DELETE CASCADE,
        --ON UPDATE NO ACTION
        --ON DELETE NO ACTION,
    CONSTRAINT fk743vfxcj9oeddf3xwmjekmldc FOREIGN KEY (domain_id)
        REFERENCES domain (id),
        --ON UPDATE NO ACTION
        --ON DELETE NO ACTION,
    UNIQUE (domain_id, application_base_id)
);

-- initial insert
insert into domain_application_state_per_domain(domain_id, application_base_id, enabled)
 select d.id, a.id, TRUE from domain d join application_base a on true;