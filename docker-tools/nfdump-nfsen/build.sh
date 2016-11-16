#!/bin/bash
set -e

DEBIAN_FRONTEND=noninteractive

useradd -c NFSEN -G www-data netflow

mkdir -p /var/www
ln -s /var/www/nfsen /var/www/html

apt-get -y install build-essential flex bison rrdtool librrds-perl libmailtools-perl libsocket6-perl librrd-dev libbz2-1.0 libbz2-dev libbz2-ocaml libbz2-ocaml-dev  lighttpd php5-cgi php5-cli

lighttpd-enable-mod fastcgi-php

# building nfdump
cd /opt/nfdump-1.6.15.tar.gz/nfdump-1.6.15
./configure --enable-nfprofile
make && make install
ldconfig

# configure authentication (override default lighttpd configuration)
cp /opt/lighttpd.conf /etc/lighttpd/lighttpd.conf

# creating directory to mount external storage
mkdir /data
