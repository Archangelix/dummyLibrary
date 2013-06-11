# Books schema

# --- !Ups

CREATE SEQUENCE book_id_seq;

CREATE TABLE BOOK (
	id integer not null default nextval('book_id_seq'),
	title varchar2(100),
	author varchar2(50),
	publishedYear integer
);

# --- !Downs
DROP TABLE BOOK;
DROP SEQUENCE book_id_seq;
