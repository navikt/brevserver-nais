# ⚠️  Les dette før du opensourcer dette repoet: ⚠️

# ⚠️ Pull-Request-historikken i dette repoet inneholder lisensbelagte filer! ⚠️

Selv om selve git-historikken i dette repoet er "vasket" nå, inneholder noen av PR-ene i dette repoet fortsatt
lisensbelagte filer. Det er to måter å håndtere det om dette repoet skal opensources:

1. kontakt github kundeservice, og få dem til å slette PR-ene i dette repoet
2. Lag et nytt, åpent github-repo, men bruk samme git-repo. Slett/arkiver det opprinnelige github-repoet.

# Brevserver-nais

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

### Kjøre prosjektet lokalt
For å kjøre opp applikasjonen lokal, bruk profile `nais` og systemvariabler hentet fra vault: [System variabler](https://vault.adeo.no/ui/vault/secrets/secret/list/dokument/brevserver/q1/) 

### Bygge app.jar og kjøre tester
`mvn clean package`/`mvn clean install`

### Henvendelser
Spørsmål om koden eller prosjektet kan rettes til [Slack-kanalen for \#Team Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ).
