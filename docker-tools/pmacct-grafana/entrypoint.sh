#!/bin/bash

set -e

mkdir /var/spool/pmacct
nfacctd -f /etc/pmacct/nfacctd.conf -D
crontab /opt/crontab
rsyslogd
cron

service elasticsearch start

service grafana-server start
sleep 10
curl -H "Content-Type: application/json" --data @/opt/grafana-datasource.json http://admin:admin@localhost:3000/api/datasources
curl -H "Content-Type: application/json" --data @/opt/grafana-dashboard.json http://admin:admin@localhost:3000/api/dashboards/db
curl -H "Content-Type: application/json" --data @/opt/grafana-user.json http://admin:admin@localhost:3000/api/admin/users

# block
sleep infinity
