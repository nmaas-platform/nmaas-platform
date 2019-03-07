#!/bin/bash

TAG=LATEST
PACKAGE=nmaas-platform
ARTIFACTORY=artifactory.geant.net/nmaas-docker-local
sudo docker build --rm -t $ARTIFACTORY/$PACKAGE:$TAG -f ./Dockerfile ..
sudo docker push $ARTIFACTORY/$PACKAGE:$TAG
