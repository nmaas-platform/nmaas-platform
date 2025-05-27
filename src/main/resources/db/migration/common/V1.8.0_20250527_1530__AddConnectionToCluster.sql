ALTER TABLE kubernetes_nm_service_info
ADD COLUMN k_cluster_id integer;

ALTER TABLE kubernetes_nm_service_info
ADD CONSTRAINT fk_kubernetes_nm_service_info_kcluster
FOREIGN KEY (k_cluster_id)
REFERENCES k_cluster (id)
ON DELETE SET NULL;

ALTER TABLE app_deployment
ADD COLUMN remote_cluster_id integer;