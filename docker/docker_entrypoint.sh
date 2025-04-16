#!/bin/sh

export HELM_HOME=/home/nmaas/.helm

mkdir -p $HELM_HOME
helm repo add nmaas https://artifactory.software.geant.org/artifactory/nmaas-helm
helm repo add influxdata https://helm.influxdata.com/
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add jenkins https://charts.jenkins.io

cat >> /home/nmaas/.profile <<EOF
export HELM_HOME=$HELM_HOME
export HELM_HOST=$HELM_HOST
export TILLER_NAMESPACE=$TILLER_NAMESPACE
export KUBERNETES_SERVICE_HOST=kubernetes.default
export KUBERNETES_SERVICE_PORT=443
EOF

DIR=/nmaas/platform
FILE=$(ls $DIR | grep .jar)
cd $DIR

echo "Waiting for database to become ready"
until PGPASSWORD=${POSTGRESQL_PASSWORD} psql -h "${POSTGRESQL_HOST}" -p ${POSTGRESQL_PORT} -U "${POSTGRESQL_USERNAME}" -d "postgres" -c '\l'; do
  sleep 1s
done

if PGPASSWORD=${POSTGRESQL_PASSWORD} psql -h "${POSTGRESQL_HOST}" -p ${POSTGRESQL_PORT} -U "${POSTGRESQL_USERNAME}" -lqt | cut -d \| -f 1 | grep -qw ${POSTGRESQL_DBNAME}; then
  echo "Database is already exists"
else
  echo "Database needs to be created. Creating now ..."
  PGPASSWORD=${POSTGRESQL_PASSWORD} createdb ${POSTGRESQL_DBNAME} -h "${POSTGRESQL_HOST}" -p ${POSTGRESQL_PORT} -U "${POSTGRESQL_USERNAME}" ${POSTGRESQL_DBNAME}
fi

echo "Running nmaas-platform ..."
java -Djava.security.egd=file:/dev/./urandom -Dlogging.config=/nmaas/platform/config/logback.xml -jar $FILE