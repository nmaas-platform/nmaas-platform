#!/bin/bash

DEBIAN_FRONTEND=noninteractive

apt-get -y install libjansson-dev libpcap-dev make pkg-config 
apt-get -y install wget openjdk-7-jre
apt-get -y install git
apt-get -y install adduser libfontconfig
apt-get -y install rsyslog
apt-get -y install curl

cd /opt/pmacct-1.6.0.tar.gz/pmacct-1.6.0/
./configure --enable-ipv6 --enable-jansson
make
make check
make install

mkdir /etc/pmacct
cp /opt/nfacctd.conf /etc/pmacct/nfacctd.conf

#elasticsearch
cd /opt/
wget https://download.elastic.co/elasticsearch/release/org/elasticsearch/distribution/deb/elasticsearch/2.4.1/elasticsearch-2.4.1.deb
dpkg -i elasticsearch-2.4.1.deb
echo "script.engine.groovy.inline.aggs: on" >> /etc/elasticsearch/elasticsearch.yml
echo "script.engine.groovy.inline.search: on" >> /etc/elasticsearch/elasticsearch.yml
echo "script.engine.groovy.inline.update: on" >> /etc/elasticsearch/elasticsearch.yml

#pmacct-to-elasticsearch
cd /opt/
git clone https://github.com/pierky/pmacct-to-elasticsearch.git
cd pmacct-to-elasticsearch/
./install -F
cp /opt/plugin1.conf /etc/p2es/plugin1.conf

#grafana
cd /opt/
wget https://grafanarel.s3.amazonaws.com/builds/grafana_3.1.1-1470047149_amd64.deb
dpkg -i grafana_3.1.1-1470047149_amd64.deb
