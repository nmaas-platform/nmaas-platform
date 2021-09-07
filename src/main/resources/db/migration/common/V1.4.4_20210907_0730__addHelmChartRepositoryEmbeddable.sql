alter table kubernetes_template add column helm_chart_repository_name varchar(14) not null default 'nmaas';
alter table kubernetes_template add column helm_chart_repository_url varchar(255) not null default 'https://artifactory.software.geant.org/artifactory/nmaas-helm';

insert into kubernetes_template (helm_chart_repository_name, helm_chart_repository_url) VALUES ('nmaas', 'https://artifactory.software.geant.org/artifactory/nmaas-helm');