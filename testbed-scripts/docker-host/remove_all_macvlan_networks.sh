#!/bin/bash
docker network rm $(docker network ls | awk '$3 == "macvlan" {print $1}')
