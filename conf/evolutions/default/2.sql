# Books schema

# --- !Ups

CREATE SEQUENCE book_id_seq;

CREATE TABLE BOOK (
	id integer not null default nextval('book_id_seq'),
	title varchar2(100),
	author varchar2(50),
	publishedYear integer
);

INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('ONE', 'SONE', 1901);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('TWO', 'STWO', 1902);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('THREE', 'STHREE', 1903);

# --- !Downs
DROP TABLE BOOK;
DROP SEQUENCE book_id_seq;
