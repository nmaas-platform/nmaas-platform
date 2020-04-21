-- migration from old default_storage_space column
insert into app_storage_volume(id, type, default_storage_space)
  select ads.id, 'MAIN', ads.default_storage_space from app_deployment_spec ads;

insert into app_storage_volume_deploy_parameters(app_storage_volume_id, deploy_parameters, deploy_parameters_key)
  select asv.id, 'persistence.enabled', 0 from app_storage_volume asv
  union
  select asv.id, 'persistence.name', 1 from app_storage_volume asv
  union
  select asv.id, 'persistence.storageClass', 2 from app_storage_volume asv
  union
  select asv.id, 'persistence.size', 3 from app_storage_volume asv;

insert into app_deployment_spec_storage_volumes(app_deployment_spec_id, storage_volumes_id)
  select ads.id, ads.id from app_deployment_spec ads;

-- migration from old storage_space column
insert into service_storage_volume(id, type, size)
  select nsi.id, 'MAIN', nsi.storage_space from nm_service_info nsi;

insert into service_storage_volume_deploy_parameters(service_storage_volume_id, deploy_parameters, deploy_parameters_key)
  select ssv.id, 'persistence.enabled', 0 from service_storage_volume ssv
  union
  select ssv.id, 'persistence.name', 1 from service_storage_volume ssv
  union
  select ssv.id, 'persistence.storageClass', 2 from service_storage_volume ssv
  union
  select ssv.id, 'persistence.size', 3 from service_storage_volume ssv;

insert into kubernetes_nm_service_info_storage_volumes(kubernetes_nm_service_info_id, storage_volumes_id)
  select nsi.id, nsi.id from nm_service_info nsi;

alter table app_deployment_spec drop column default_storage_space;
alter table nm_service_info drop column storage_space;