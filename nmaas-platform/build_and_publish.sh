#!/bin/bash

TAG=1.2.0
PACKAGE=nmaas-platform
REPOSITORY=artifactory.geant.net/nmaas-docker-local
sudo docker build --rm -t $REPOSITORY/$PACKAGE:$TAG -f ./Dockerfile ..
sudo docker push $REPOSITORY/$PACKAGE:$TAG
