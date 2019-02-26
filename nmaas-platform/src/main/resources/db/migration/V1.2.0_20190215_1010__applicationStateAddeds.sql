alter table application drop column deleted;
alter table application add column state varchar(255) not null;
alter table application add column owner varchar(255) not null;
alter table application alter column version varchar(255) not null;