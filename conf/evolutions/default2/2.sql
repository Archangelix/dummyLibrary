# Books schema

# --- !Ups

INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('ONE', 'SONE', 1901);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('TWO', 'STWO', 1902);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('THREE', 'STHREE', 1903);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('FOUR', 'SFOUR', 1904);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('FIVE', 'SFIVE', 1905);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('SIX', 'SSIX', 1906);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('SEVEN', 'SSEVEN', 1907);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('EIGHT', 'SEIGHT', 1908);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('NINE', 'SNINE', 1909);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('TEN', 'STEN', 1910);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('ELEVEN', 'SELEVEN', 1911);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('TWELVE', 'STWELVE', 1912);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('THIRTEEN', 'STHIRTEEN', 1913);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('FOURTEEN', 'SFOURTEEN', 1914);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('FIFTEEN', 'SFIFTEEN', 1915);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('SIXTEEN', 'SSIXTEEN', 1916);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('SEVENTEEN', 'SSEVENTEEN', 1917);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('EIGHTEEN', 'SEIGHTEEN', 1918);
INSERT INTO BOOK (TITLE, AUTHOR, PUBLISHEDYEAR) VALUES ('NINETEEN', 'SNINETEEN', 1919);

INSERT INTO USERS (USERID, PASSWORD, NAME, ADDRESS, DOB) VALUES ('admin', 'admin', 'ADMIN', 'admin''s address', PARSEDATETIME('12-01-1980','dd-mm-yyyy'));
INSERT INTO USERS (USERID, PASSWORD, NAME, ADDRESS, DOB) VALUES ('user', 'user', 'USER', 'user''s address', PARSEDATETIME('28-02-1983','dd-mm-yyyy'));

# --- !Downs

DELETE FROM BOOK;
DELETE FROM USERS;
