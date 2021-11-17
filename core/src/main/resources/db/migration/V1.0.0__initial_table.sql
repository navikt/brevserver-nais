create table t_brevstatus
(
    brevreferanse     varchar2(32 CHAR) not null,
    returkoe          varchar2(100 CHAR) not null,
    bestillerbrukerid varchar2(18 CHAR),
    brevmal           varchar2(255 CHAR) not null,
    systemid          varchar2(4 CHAR) not null,
    status            varchar2(8 CHAR) not null,
    format            varchar2(32 CHAR) not null,
    skrivertype       varchar2(16 CHAR),
    skriver           varchar2(32 CHAR),
    arkiver           varchar2(3 CHAR),
    skuff             varchar2(32 CHAR),
    knappstatus       integer,
    dato_endret date not null,
    endret_av varchar2(100 CHAR) not null,
    dato_opprettet date not null,
    opprettet_av varchar2(100 CHAR) not null
);

create table t_brevsystilgang
(
    systemid      varchar2(4 CHAR) not null,
    systempassord varchar2(32 CHAR) not null,
    eldok_id      varchar2(20 CHAR),
    eldok_pwd     varchar2(20 CHAR)
);

create table t_brevtilgang
(
    id        number(19,0) PRIMARY KEY,
    brevreferanse   varchar2(32 CHAR),
    token     varchar2(64 CHAR),
    systemid  varchar2(4 CHAR),
    dato_endret date not null,
    endret_av varchar2(100 CHAR) not null,
    dato_opprettet date not null,
    opprettet_av varchar2(100 CHAR) not null
);

create table t_brevlager
(
    id        NUMBER(19,0) PRIMARY KEY,
    brevreferanse varchar2(32 CHAR) not null,
    status        varchar2(8 CHAR) not null,
    brukerid      varchar2(18 CHAR),
    systemid      varchar2(4 CHAR) not null,
    brevdata      blob      not null,
    contenttype   varchar2(64 CHAR),
    vasket        char,
    partkey       smallint,
    dato_endret date not null,
    endret_av varchar2(100 CHAR) not null,
    dato_opprettet date not null,
    opprettet_av varchar2(100 CHAR) not null
);

create sequence t_brevlager_historikk_seq start with 1 increment by 1;
create table t_brevlager_historikk
(
    brevlager_historik_id integer   not null,
    brevreferanse         varchar2(32 CHAR) not null,
    status                varchar2(8 CHAR) not null,
    brukerid              varchar2(18 CHAR),
    systemid              varchar2(4 CHAR) not null,
    brevdata              blob      not null,
    contenttype           varchar2(64 CHAR),
    vasket                char      not null,
    partkey               smallint,
    dato_endret date not null,
    endret_av varchar2(100 CHAR) not null,
    dato_opprettet date not null,
    opprettet_av varchar2(100 CHAR) not null
);

