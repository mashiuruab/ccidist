create database cciService character set utf8;
use cciService;

create table organization(
	id varchar(255) not null,
	version int not null,
	name varchar(255),
	created datetime,
	updated datetime,
	primary key(id)
) ENGINE=InnoDB;

create table publication (
	id varchar(255) not null,
	version int not null,
	name varchar(255),
	organization_id varchar(255),
	created datetime,
	updated datetime,
	primary key (id),
	FOREIGN KEY (organization_id) REFERENCES organization (id) on delete cascade
) ENGINE=InnoDB;

create table rxml_zip_file (
	id int not null auto_increment,
	version int not null,
	file_name varchar(255),
	design_name varchar(255),
	issue_name varchar(255),
	issue_date datetime,
	publication_id varchar(255),
	created datetime,
	updated datetime,
	primary key (id),
	foreign key (publication_id) references publication(id) on delete cascade
) ENGINE=InnoDB;

create table rxml_binary_file (
  id int not null auto_increment,
  version int not null,
  file_content longblob,
  rxml_zip_file_id int,
  primary key (id),
  FOREIGN KEY (rxml_zip_file_id) REFERENCES rxml_zip_file (id) on delete cascade
 ) ENGINE=InnoDB;

create table events (
	id int not null auto_increment,
	version int not null,
	issue_id int,
	path varchar(255),
	category int,
	created datetime,
	primary key(id)
) ENGINE=InnoDB;

create table role (
	id int not null auto_increment,
	version int not null,
	name varchar(255),
	primary key(id)
) ENGINE=InnoDB;

create table user_privilege (
	id int not null auto_increment,
	version int not null,
	organization_id varchar(255),
	role_id int,
	primary key(id),
	foreign key (organization_id) references organization(id) on delete cascade,
	foreign key (role_id) references role(id)
) ENGINE=InnoDB;

create table users (
	id int not null auto_increment,
	version int not null,
	name varchar(255),
	login_name varchar(255),
	password varchar(255),
	created datetime,
	updated datetime,
	user_privilege_id int,
	primary key(id),
	constraint login_name_constraint unique(login_name),
	foreign key (user_privilege_id) references user_privilege(id) on delete cascade
) ENGINE=InnoDB;

create table design_to_epub_mapper (
	id int not null auto_increment,
	version int not null,
	design_name varchar(255),
	epub_name varchar(255),
	constraint epub_name_constraint unique(epub_name),
	primary key(id)
) ENGINE=InnoDB;

create table driver_info (
	id int not null auto_increment,
	version int not null,
	publication_id varchar(255),
	design_to_epub_mapper_id int,
	pre_generate tinyint,
	os varchar(20),
	os_version varchar(10),
	reader varchar(10),
	device_name varchar(100),
	start_date datetime,
	end_date datetime,
	created datetime,
	updated datetime,
	internal tinyint not null,
	primary key(id),
	foreign key (design_to_epub_mapper_id) references design_to_epub_mapper(id),
	foreign key (publication_id) references publication(id) on delete cascade
) ENGINE=InnoDB;

create table matching_rules (
	id int not null auto_increment,
	version int not null,
	design_to_epub_mapper_id int,
    publication_id varchar(255),
	width int,
	height int,
	os varchar(255),
	osv varchar(255),
	reader_version varchar(255),
    device_name varchar(255),
	created datetime,
	updated datetime,
	primary key(id),
	foreign key (design_to_epub_mapper_id) references design_to_epub_mapper(id),
	foreign key (publication_id) references publication(id) on delete cascade
) ENGINE=InnoDB;

create table issue (
    id int not null auto_increment,
	version int not null,
	name varchar(255),
	publication_id varchar(255) ,
	zip_file_id int not null,
	driver_info_id int not null,
	status tinyint,
    stale tinyint not null,
	created datetime,
	updated datetime,
	primary key (id),
	foreign key (publication_id) references publication(id) on delete cascade,
	foreign key (driver_info_id) references driver_info(id) on delete cascade,
	foreign key (zip_file_id) references rxml_zip_file (id) on delete cascade
) ENGINE=InnoDB;

create table epub_file (
    id int not null auto_increment,
    version int not null,
    file_content longblob,
    issue_id int,
    primary key (id),
    FOREIGN KEY (issue_id) REFERENCES issue (id) on delete cascade
) ENGINE=InnoDB;

insert into users (id, name, login_name, password, created, updated, user_privilege_id, version) values (1, 'Administrative User', 'admin', '2d8cc94a8c8b5ca7400969c5b2e572c1', current_timestamp, current_timestamp, null, 0);
insert into role (id, name, version) values (1, 'Portal', 0);
insert into role (id, name, version) values (2, 'Ingester', 0);
