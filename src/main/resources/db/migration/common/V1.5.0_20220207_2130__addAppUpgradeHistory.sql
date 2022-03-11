create table app_upgrade_history (
  id bigint generated by default as identity,
  deployment_id bytea not null,
  timestamp timestamp not null,
  previous_application_id bytea not null,
  target_application_id bytea not null,
  mode varchar(255),
  status varchar(255),
  primary key (id));