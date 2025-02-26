#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvbrevserver/username;
then
    echo "Setting SERVICEUSER_USERNAME"
    export FAGARKIV_SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvbrevserver/username)
fi

if test -f /var/run/secrets/nais.io/srvbrevserver/password;
then
    echo "Setting SERVICEUSER_PASSWORD"
    export FAGARKIV_SERVICEUSER_PASSWORD=$(cat /var/run/secrets/nais.io/srvbrevserver/password)
fi

if test -f /var/run/secrets/nais.io/db_creds/username;
then
    echo "Setting SPRING_DATASOURCE_USERNAME"
    export  SPRING_DATASOURCE_USERNAME=$(cat /var/run/secrets/nais.io/db_creds/username)
fi
if test -f /var/run/secrets/nais.io/db_creds/password;
then
    echo "Setting SPRING_DATASOURCE_PASSWORD"
    export  SPRING_DATASOURCE_PASSWORD=$(cat /var/run/secrets/nais.io/db_creds/password)
fi

if test -f /var/run/secrets/nais.io/db_config/jdbc_url;
then
    export  SPRING_DATASOURCE_URL=$(cat /var/run/secrets/nais.io/db_config/jdbc_url)
    echo "Setting SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL"
fi
if test -f /var/run/secrets/nais.io/db_config/ons_host;
then
    export  DATABASE_ONSHOSTS=$(cat /var/run/secrets/nais.io/db_config/ons_host)
    echo "Setting DATABASE_ONSHOSTS=$DATABASE_ONSHOSTS"
fi