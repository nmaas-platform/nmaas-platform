ALTER TABLE domain_application_state_per_domain
ADD COLUMN pv_storage_size_limit INTEGER;

UPDATE domain_application_state_per_domain
    SET pv_storage_size_limit =
 (
    select ads.default_storage_space as space
    from app_deployment_spec ads
        join application a on ads.id = a.app_deployment_spec_id
        join application_base ab on a.name = ab.name
    where ab.id = domain_application_state_per_domain.application_base_id
 )
WHERE exists (select * from app_deployment_spec ads
                                join application a on ads.id = a.app_deployment_spec_id
                                join application_base ab on a.name = ab.name
              where ab.id = domain_application_state_per_domain.application_base_id);

ALTER TABLE domain_application_state_per_domain
ALTER COLUMN pv_storage_size_limit SET NOT NULL;