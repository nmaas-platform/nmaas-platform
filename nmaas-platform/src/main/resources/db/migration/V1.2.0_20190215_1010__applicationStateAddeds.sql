alter table application drop column deleted;
alter table application add column state varchar(255) not null;