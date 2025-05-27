ALTER TABLE kubernetes_nm_service_info
ADD COLUMN kcluster_id integer;

ALTER TABLE kubernetes_nm_service_info
ADD CONSTRAINT fk_kubernetes_nm_service_info_kcluster
FOREIGN KEY (kcluster_id)
REFERENCES kcluster (id)
ON DELETE SET NULL;

ALTER TABLE app_deployment
ADD COLUMN remote_cluster_id integer;