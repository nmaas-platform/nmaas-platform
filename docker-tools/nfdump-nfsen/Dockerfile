FROM debian:jessie

MAINTAINER Lukasz Lopatowski <llopat@man.poznan.pl>

RUN DEBIANFRONTEND=noninteractive apt-get -qq update

ADD nfdump-1.6.15.tar.gz /opt/nfdump-1.6.15.tar.gz
ADD nfsen-1.3.7.tar.gz /opt/nfsen-1.3.7.tar.gz
COPY build.sh /opt/build.sh
COPY run.sh /opt/run.sh
COPY nfsen-generic.conf /opt/nfsen-generic.conf
COPY generate_nfsen_config_script.php /opt/generate_nfsen_config_script.php
COPY lighttpd.conf /opt/lighttpd.conf
COPY lighttpdpassword /opt/lighttpdpassword 

RUN chmod +x /opt/run.sh /opt/build.sh ; sync ; sleep 1; 
RUN /opt/build.sh

ENTRYPOINT ["/opt/run.sh"]
