#!/bin/bash

TAG=1.2.0-SNAPSHOT
WHAT=portal
sudo docker build --rm -t artifactory.geant.net/nmaas-docker-local/nmaas-$WHAT:$TAG .
sudo docker push artifactory.geant.net/nmaas-docker-local/nmaas-$WHAT:$TAG
