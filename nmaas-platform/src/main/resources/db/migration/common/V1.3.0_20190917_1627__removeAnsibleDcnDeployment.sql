alter table dcn_info drop constraint if exists FK6tnvn4d10w4gamkiwyettj0xn;
alter table dcn_info drop constraint if exists FKa60cx3nls8rc7omsglkb2gbxh;
alter table dcn_info drop constraint if exists FKm88vjmyolbu15ccg5s58xp3a9;

alter table dcn_info drop column cloud_endpoint_details_id;
alter table dcn_info drop column playbook_for_client_side_router_id;
alter table dcn_info drop column playbook_for_cloud_side_router_id;
alter table k_cluster drop column attach_point_id;

drop table dcn_cloud_endpoint_details;
drop table ansible_playbook_vpn_config;
drop table k_cluster_attach_point;