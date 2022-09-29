#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvbrevserver-nais/username;
then
    echo "Setting SERVICEUSER_USERNAME"
    export FAGARKIV_SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvbrevserver-nais/username)
fi

if test -f /var/run/secrets/nais.io/srvbrevserver-nais/password;
then
    echo "Setting SERVICEUSER_PASSWORD"
    export FAGARKIV_SERVICEUSER_PASSWORD=$(cat /var/run/secrets/nais.io/srvbrevserver-nais/password)
fi

if test -f /var/run/secrets/nais.io/brevserverDS/password;
then
    echo "Setting SPRING_DATASOURCE_PASSWORD"
    export  SPRING_DATASOURCE_PASSWORD=$(cat /var/run/secrets/nais.io/brevserverDS/password)
fi

echo "Exporting appdynamics environment variables"
if test -f /var/run/secrets/nais.io/appdynamics/appdynamics.env;
then
    export $(cat /var/run/secrets/nais.io/appdynamics/appdynamics.env)
    export APPDYNAMICS_AGENT_BASE_DIR=/tmp/appdynamics
    echo "Appdynamics environment variables exported"
else
    echo "No such file or directory found at /var/run/secrets/nais.io/appdynamics/appdynamics.env"
fi

if test -f /var/run/secrets/nais.io/certificate/keystore
then
    echo "Setting BREVSERVERCERT_KEYSTORE"
    CERT_PATH='/var/run/secrets/nais.io/certificate/keystore-extracted'
    openssl base64 -d -A -in /var/run/secrets/nais.io/certificate/keystore -out $CERT_PATH
    export BREVSERVERCERT_KEYSTORE=$CERT_PATH
fi

if test -f /var/run/secrets/nais.io/certificate/keystorepassword
then
    echo "Setting BREVSERVERCERT_PASSWORD"
    export BREVSERVERCERT_PASSWORD=$(cat /var/run/secrets/nais.io/certificate/keystorepassword)
fi