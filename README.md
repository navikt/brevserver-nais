#Brevserver-nais

> [!WARNING]
> Dette repoet kan ikke settes til public da det ligger lisensfil og fonter under [core/src/main/resources/aspose](core/src/main/resources/aspose).
> Disse filene kan vi ikke publisere. De må skrubbes fra git treet og verifiseres at er permanent slettet.

* [Funksjonelle Krav](#1-funksjonelle-krav)
* [Distribusjon av tjenesten (deployment)](#2-distribusjon-av-tjenesten-deployment)
* [Utviklingsmiljø](#3-utviklingsmilj)
* [Drift og støtte](#4-drift-og-sttte)

## Funksjonelle krav
Brevserver-nais består av gamle tjenester for å bestille og redigere brev fra fagsystemene. Brukes av pesys, bisys, økonomi og skatt (++ ?). 

For mer informasjon (confluencen for gamle brevserver; overordnet fungerer brevserver-nais på samme måte): [confluence](https://confluence.adeo.no/display/BOA/Brevserver+-+Komponenter+og+programmer)

Brevserver består av mq-tjenester for å bestille/arkivere brev og web-tjenester for å redigere disse.

Typisk ser løpet slik ut:

    1. Fagsystem sender en bestilling over mq bestående av en header på 350 tegn med metadata før resten av meldingen er brev-xml'en.
    2. Bestillingen blir plukket opp av bestillBrev som lagrer metadataen og sender hele meldingen videre til exstream (eies av #team-ccm) over mq for å opprette selve brevet.
    3. Exstream sender svar over mq som blir plukket opp av arkiverBrev som igjen arkiverer dokumentet. Dette kan enten være en kladd eller ferdig pdf.
    4. ArkiverBrev sender så en kvitteringsmelding tilbake til fagsystemet på input-køen de selv definerte i JMSReplyTo headeren i den originale bestillingen.
    5. Fagsystemet plukker opp kvitteringen og ser at det nå finnes et brev som kan redigeres
    6. Saksbehanderen åpner brevet i brevklient som åpnes som et word-dokument hvor saksbehandler kan redigere og gjøre endringer
    7. Saksbehandlere velger "ferdigstill" i brevklient som oppretter en PDF fra word-dokumentet og låser det for videre redigering. Sender kvittering tilbake til fagsystemet på at brevet er "ferdig"

Skatt / økonomi / bidrag har også løp hvor de kun bruker brevserver til å arkivere ferdige pdf'er, uten å redigere disse. Det blir da lagt en melding til arkiverBrev som vanlig.

## Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort av Jenkins:
[brevserver-nais CI / CD](https://dok-jenkins.adeo.no/job/Brevserver-Github/)
Push/merge til masterbranch vil teste, bygge og deploye til produksjonsmiljø og testmiljø.


## Utviklingsmiljø
### Forutsetninger
* Java 17
* Kubectl
* Maven

### Kjøre prosjektet lokalt
For å kjøre opp applikasjonen lokal, bruk profile `nais` og systemvariabler hentet fra vault: [System variabler](https://vault.adeo.no/ui/vault/secrets/secret/list/dokument/brevserver/q1/) 

### Bygge app.jar og kjøre tester
Alt kan bygges bortsett fra brevserver-klient-applett. 
`mvn clean package`/`mvn clean install`


## Drift og støtte
### Logging
Loggene til tjenesten kan leses på to måter:

### Kibana
For [dev-fss](https://logs.adeo.no/goto/b531033b1d84bd755427a31475915378)

For [prod-fss](https://logs.adeo.no/goto/d279261eecd001bcc1795f9734b5b9ab)

### Kubectl
For dev-fss:
```shell script
kubectl config use-context dev-fss
kubectl get pods -n=teamdokumenthandtering -l app=brevserver-nais
kubectl logs -f brevserver-nais-<POD-ID> -n teamdokumenthandtering -c brevserver-nais
```

For prod-fss:
```shell script
kubectl config use-context prod-fss
kubectl get pods -l app=brevserver-nais -n=teamdokumenthandtering
kubectl logs -f brevserver-nais-<POD-ID> -n teamdokumenthandtering -c brevserver-nais
```

### Henvendelser
Spørsmål til koden eller prosjektet kan rettes til Team Dokumentløsninger på:
* [\#Team Dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)



