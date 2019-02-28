alter table application drop column deleted;
alter table application add column state varchar(255) not null default 'NEW';
alter table application add column owner varchar(255) not null default 'admin';
alter table application alter column version varchar(255) not null;