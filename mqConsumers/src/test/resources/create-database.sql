create table t_brevstatus(
	brevreferanse varchar(32) not null,
	returkoe longvarchar not null,
	bestillerbrukerid varchar(18),
	brevmal varchar(255) not null,
	timestamp timestamp not null,
	systemid varchar(4) not null,
	status varchar(8) not null,
	format varchar(32),-- not null,
	skrivertype varchar(16),
	skriver varchar(32),
	arkiver varchar(3),
	skuff varchar(32),
	knappstatus integer
);

create table t_brevsystilgang(
	systemid varchar(4) not null,
	systempassord varchar(32) not null,
	eldok_id varchar(20),
	eldok_pwd varchar(20)
);

create table t_brevtilgang(
	brevreferanse varchar(32),
	token varchar(64),
	timestamp timestamp,
	systemid varchar(4),
	id integer identity
);

create table t_brevlager5(
	blobid int primary key auto_increment,
	brevreferanse varchar(32) not null,
	status varchar(8) not null,
	timestamp timestamp not null,
	brukerid varchar(18),
	systemid varchar(4) not null,
	brevdata blob not null,
	contenttype varchar(64),
	vasket char,
	partkey smallint
);

create sequence t_brevlager_historikk_seq start with 1 increment by 1;
create table t_brevlager_historikk(
	brevlager_historik_id integer not null,
	brevreferanse varchar(32) not null,
	status varchar(8) not null,
	timestamp timestamp not null,
	brukerid varchar(18),
	systemid varchar(4) not null,
	brevdata blob not null,
	contenttype varchar(64),
	vasket char not null,
	partkey smallint		
);

