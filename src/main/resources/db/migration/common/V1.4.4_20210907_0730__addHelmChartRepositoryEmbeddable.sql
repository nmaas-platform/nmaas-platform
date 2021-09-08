alter table kubernetes_template add column helm_chart_repository_name varchar(14);
alter table kubernetes_template add column helm_chart_repository_url varchar(255);