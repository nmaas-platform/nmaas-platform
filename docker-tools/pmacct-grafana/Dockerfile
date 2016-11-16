FROM ubuntu:14.04

MAINTAINER Lukasz Lopatowski <llopat@man.poznan.pl>

RUN DEBIANFRONTEND=noninteractive apt-get -qq update

ADD pmacct-1.6.0.tar.gz /opt/pmacct-1.6.0.tar.gz
COPY configure.sh /opt/configure.sh
COPY entrypoint.sh /opt/entrypoint.sh
COPY nfacctd.conf /opt/nfacctd.conf
COPY plugin1.conf /opt/plugin1.conf
COPY pmacct-to-elastic.sh /opt/pmacct-to-elastic.sh
COPY crontab /opt/crontab
COPY grafana-datasource.json /opt/grafana-datasource.json
COPY grafana-dashboard.json /opt/grafana-dashboard.json
COPY grafana-user.json /opt/grafana-user.json
RUN chmod +x /opt/configure.sh /opt/entrypoint.sh /opt/pmacct-to-elastic.sh ; sync ; sleep 1;
RUN /opt/configure.sh

ENTRYPOINT ["/opt/entrypoint.sh"]
