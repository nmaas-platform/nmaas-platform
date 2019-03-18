#!/bin/sh
DIR=/nmaas/platform
FILE=$(ls $DIR | grep .jar)
cd $DIR

if [[ -z "${PLATFORM_PORT}" ]]; then
  cp $DIR/config/nmaas-platform.properties.default $DIR/nmaas-platform.properties
else
  envsubst < $DIR/config/nmaas-platform.properties.template > $DIR/nmaas-platform.properties
fi

mkdir -p /root/.ssh
cp /nmaas/.ssh/id_rsa /root/.ssh/id_rsa
chmod 600 /root/.ssh/id_rsa
mkdir -p /nmaas/files/upload
mkdir -p /nmaas/files/log

until PGPASSWORD=${POSTGRESQL_PASSWORD} psql -h "${POSTGRESQL_HOST}" -U "${POSTGRESQL_USERNAME}" -d "postgres" -c '\l'; do
  sleep 1s
done

if PGPASSWORD=${POSTGRESQL_PASSWORD} psql -h "${POSTGRESQL_HOST}" -U "${POSTGRESQL_USERNAME}" -lqt | cut -d \| -f 1 | grep -qw ${POSTGRESQL_DBNAME}; then
  echo "Database is already setup"
else
  echo "Database is being created..."
  PGPASSWORD=${POSTGRESQL_PASSWORD} createdb ${POSTGRESQL_DBNAME} -h "${POSTGRESQL_HOST}" -U "${POSTGRESQL_USERNAME}" ${POSTGRESQL_DBNAME}
fi

java -Djava.security.egd=file:/dev/./urandom -Dlog4j.configurationFile=config/log4j2-spring.json -jar $FILE --spring.config.name=nmaas-platform
