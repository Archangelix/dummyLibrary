# Books schema

# --- !Ups

CREATE SEQUENCE book_id_seq;

CREATE TABLE BOOK (
	id integer not null default nextval('book_id_seq'),
	title varchar2(100),
	author varchar2(50),
	publishedYear integer
);

CREATE SEQUENCE user_id_seq;

CREATE TABLE USERS (
	seqno integer not null default nextval('user_id_seq'),
	userid varchar2 (15),
	password varchar2 (30),
	name varchar2 (100),
	address varchar2 (100),
	dob date
);

# --- !Downs
DROP TABLE BOOK;
DROP SEQUENCE book_id_seq;

DROP TABLE USERS;
DROP SEQUENCE user_id_seq;
