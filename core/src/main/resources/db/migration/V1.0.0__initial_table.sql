create table t_brevstatus
(
    brevreferanse     varchar2(32) not null,
    returkoe          varchar2(100) not null,
    bestillerbrukerid varchar2(18),
    brevmal           varchar2(255) not null,
    systemid          varchar2(4) not null,
    status            varchar2(8) not null,
    format            varchar2(32) not null,
    skrivertype       varchar2(16),
    skriver           varchar2(32),
    arkiver           varchar2(3),
    skuff             varchar2(32),
    knappstatus       integer,
    dato_endret date not null,
    endret_av varchar2(100) not null,
    dato_opprettet date not null,
    opprettet_av varchar2(100) not null
);

create table t_brevsystilgang
(
    systemid      varchar2(4) not null,
    systempassord varchar2(32) not null,
    eldok_id      varchar2(20),
    eldok_pwd     varchar2(20)
);

create table t_brevtilgang
(
    id        number(19,0) PRIMARY KEY,
    brevreferanse   varchar2(32),
    token     varchar2(64),
    systemid  varchar2(4),
    dato_endret date not null,
    endret_av varchar2(100) not null,
    dato_opprettet date not null,
    opprettet_av varchar2(100) not null
);

create table t_brevlager
(
    id        NUMBER(19,0) PRIMARY KEY,
    brevreferanse varchar2(32) not null,
    status        varchar2(8) not null,
    brukerid      varchar2(18),
    systemid      varchar2(4) not null,
    brevdata      blob      not null,
    contenttype   varchar2(64),
    vasket        char,
    partkey       smallint,
    dato_endret date not null,
    endret_av varchar2(100) not null,
    dato_opprettet date not null,
    opprettet_av varchar2(100) not null
);

create sequence t_brevlager_historikk_seq start with 1 increment by 1;
create table t_brevlager_historikk
(
    brevlager_historik_id integer   not null,
    brevreferanse         varchar2(32) not null,
    status                varchar2(8) not null,
    brukerid              varchar2(18),
    systemid              varchar2(4) not null,
    brevdata              blob      not null,
    contenttype           varchar2(64),
    vasket                char      not null,
    partkey               smallint,
    dato_endret date not null,
    endret_av varchar2(100) not null,
    dato_opprettet date not null,
    opprettet_av varchar2(100) not null,
);

