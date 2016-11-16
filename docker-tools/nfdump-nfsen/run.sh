#!/bin/bash

# process config and install
php /opt/generate_nfsen_config_script.php > /opt/nfsen-final.conf
cd /opt/nfsen-1.3.7.tar.gz/nfsen-1.3.7
echo | ./install.pl /opt/nfsen-final.conf

# start nfsen
/usr/local/nfsen/bin/nfsen start

# start httpd
/etc/init.d/lighttpd start

# setup web
cd /opt/nfsen-1.3.7.tar.gz/nfsen-1.3.7/html
ln -s nfsen.php /var/www/nfsen/index.php
rm -rf /var/www/html
ln -s /var/www/nfsen /var/www/html

# block
sleep infinity
