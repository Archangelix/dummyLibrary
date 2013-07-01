# Books schema

# --- !Ups

CREATE SEQUENCE book_id_seq;

CREATE TABLE BOOK (
	id integer not null default nextval('book_id_seq'),
	title varchar(100),
	author varchar(50),
	publishedYear integer
);

CREATE SEQUENCE user_id_seq;

CREATE TABLE USERS (
	seqno integer not null default nextval('user_id_seq'),
	userid varchar (15),
	password varchar (30),
	name varchar (100),
	address varchar (100),
	dob date
);

# --- !Downs
DROP TABLE BOOK;
DROP SEQUENCE book_id_seq;

DROP TABLE USERS;
DROP SEQUENCE user_id_seq;
