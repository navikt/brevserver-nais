FROM ghcr.io/navikt/baseimages/temurin:17-appdynamics
ENV APPD_ENABLED=true

COPY app/target/app.jar /app/app.jar
COPY export-vault-secrets.sh /init-scripts/10-export-vault-secrets.sh
COPY brevserver-java-opts.sh /init-scripts/11-brevserver-java-opts.sh