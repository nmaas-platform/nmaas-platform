alter table kubernetes_template add column helm_chart_repository_name varchar(14) default 'nmaas' not null;
alter table kubernetes_template add column helm_chart_repository_url varchar(255) default 'https://artifactory.software.geant.org/artifactory/nmaas-helm' not null;

insert into kubernetes_template (helm_chart_repository_name, helm_chart_repository_url) VALUES ('nmaas', 'https://artifactory.software.geant.org/artifactory/nmaas-helm');