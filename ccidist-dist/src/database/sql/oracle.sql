create table organization(
    id varchar2(255 char) not null,
    version number(19, 0) not null,
    name varchar2(255),
    created date ,
    updated date ,
    primary key(id)
);

create table publication (
    id varchar2(255 char) not null,
    version number(19, 0) not null,
    name varchar2(255),
    organization_id varchar2(255 char),
    created date ,
    updated date ,
    primary key (id),
    FOREIGN KEY (organization_id) REFERENCES organization (id)  on delete cascade
);

create table rxml_zip_file (
    id number(19, 0) not null,
    version number(19, 0) not null,
    file_name varchar2(255),
    issue_name varchar2(255),
    issue_date date,
    design_name varchar2(255),
    publication_id varchar2(255),
    created date,
    updated date,
    primary key (id),
    foreign key (publication_id) references publication(id) on delete cascade
);

create table rxml_binary_file (
  id number(19, 0) not null,
  version number(19, 0) not null,
  file_content blob,
  rxml_zip_file_id number(19, 0),
  primary key (id),
  FOREIGN KEY (rxml_zip_file_id) REFERENCES rxml_zip_file (id) on delete cascade
 );
alter table rxml_binary_file modify lob(file_content) (cache);

create table events (
	id number(19, 0) not null,
	version number(19, 0) not null,
	issue_id number(19, 0),
	path varchar2(255),
	category number(2, 0),
	created date,
	primary key(id)
);

create table role (
    id number(19, 0) not null,
    version number(19, 0) not null,
    name varchar2(255),
    primary key(id)
);

create table user_privilege (
    id number(19, 0) not null,
    version number(19, 0) not null,
    organization_id varchar2(255),
    role_id number(3, 0),
    primary key(id),
    foreign key (organization_id) references organization(id) on delete cascade,
    foreign key (role_id) references role(id)
);

create table users (
    id number(19, 0) not null,
    version number(19, 0) not null,
    name varchar2(255),
    login_name varchar2(255),
    password varchar2(255),
    created date,
    updated date,
    user_privilege_id number(19, 0),
    primary key(id),
    constraint login_name_constraint unique(login_name),
    foreign key (user_privilege_id) references user_privilege(id) on delete cascade
);

create table design_to_epub_mapper (
    id number(19, 0) not null,
    version number(19, 0) not null,
    design_name varchar2(255),
    epub_name varchar2(255),
    constraint epub_name_constraint unique(epub_name),
    primary key(id)
);

create table driver_info (
    id number(19, 0) not null,
    version number(19, 0) not null,
    publication_id varchar2(255),
    design_to_epub_mapper_id number(19, 0),
    pre_generate number(1, 0),
    os varchar2(20),
    os_version varchar2(10),
    reader varchar2(10),
    device_name varchar2(10),
    start_date date,
    end_date date,
    created date,
    updated date,
    internal number(1, 0) not null,
    primary key(id),
    foreign key (design_to_epub_mapper_id) references design_to_epub_mapper(id),
    foreign key (publication_id) references publication(id)  on delete cascade
);

create table matching_rules (
    id number(19, 0) not null,
    version number(19, 0) not null,
    design_to_epub_mapper_id number(19, 0),
    publication_id varchar2(255),
    width number(19, 0),
    height number(19, 0),
    os varchar2(255),
    osv varchar2(255),
    reader_version varchar2(255),
    device_name varchar2(255),
    created date,
    updated date,
    primary key(id),
    foreign key (design_to_epub_mapper_id) references design_to_epub_mapper(id),
    foreign key (publication_id) references publication(id)  on delete cascade
);

create table issue (
    id number(19, 0) not null,
    version number(19, 0) not null,
    name varchar2(255),
    publication_id varchar2(255 char) ,
    zip_file_id number(19, 0) not null,
    driver_info_id number(19, 0) not null,
    status number(2, 0),
    stale number(2, 0) not null,
    created date,
    updated date,
    primary key (id),
    foreign key (publication_id) references publication(id) on delete cascade,
    foreign key (zip_file_id) references rxml_zip_file (id) on delete cascade,
    foreign key (driver_info_id) references driver_info (id) on delete cascade
);

create table epub_file (
    id number(19, 0) not null,
    version number(19, 0) not null,
    file_content blob,
    issue_id number(19, 0),
    primary key (id),
    FOREIGN KEY (issue_id) REFERENCES Issue (id)  on delete cascade
);
alter table epub_file modify lob(file_content) (cache);


create sequence ccidist_seq_issue start with 1 increment by 1;
create sequence ccidist_seq_epub_file start with 1 increment by 1;
create sequence ccidist_seq_rxml_zip_file start with 1 increment by 1;
create sequence ccidist_seq_matching_rules start with 1 increment by 1;
create sequence ccidist_seq_driver_info start with 1 increment by 1;
create sequence ccidist_seq_epub_mapper start with 1 increment by 1;
create sequence ccidist_seq_users start with 1 increment by 1;
create sequence ccidist_seq_user_privilege start with 1 increment by 1;
create sequence ccidist_seq_role start with 1 increment by 1;
create sequence ccidist_seq_events start with 1 increment by 1;
create sequence ccidist_seq_rxml_binary_file start with 1 increment by 1;


insert into users (id , name, login_name, password, created, updated, user_privilege_id, version) values (ccidist_seq_users.nextval,'Administrative User', 'admin', '2d8cc94a8c8b5ca7400969c5b2e572c1', sysdate, sysdate, null, 0);
insert into role (id, name, version) values (ccidist_seq_role.nextval, 'Portal', 0);
insert into role (id, name, version) values (ccidist_seq_role.nextval, 'Ingester', 0);
