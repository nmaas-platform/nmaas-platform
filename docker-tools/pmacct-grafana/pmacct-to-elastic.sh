#!/bin/bash

sleep 1
/usr/local/bin/pmacct -l -p /data/pmacct/plugin1.pipe -s -O json -e | /usr/local/bin/pmacct-to-elasticsearch plugin1 &> /opt/result.txt
