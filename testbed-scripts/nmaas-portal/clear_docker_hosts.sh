#!/bin/bash

DIR=/home/mgmt/scripts
REMOVE_CONTAINERS_SCRIPT=remove_all_container.sh
REMOVE_NETWORKS_SCRIPT=remove_all_macvlan_networks.sh

for DHOST in "$@"
do
ssh mgmt@$DHOST << EOF
echo "Cleanup on $DHOST"
$DIR/$REMOVE_CONTAINERS_SCRIPT
$DIR/$REMOVE_NETWORKS_SCRIPT
EOF
done
