CREATE TABLE NUMBER(ID INT, REFERP INT, REFERQ INT, TYPE_LETTER VARCHAR(10) );

INSERT INTO NUMBER (ID, REFERP, REFERQ, TYPE_LETTER)
              VALUES ('221','1', null, 'p' );
              
INSERT INTO NUMBER (ID, REFERP, REFERQ, TYPE_LETTER)
              VALUES ('222', '2', null, 'p');
              
INSERT INTO NUMBER (ID, REFERP, REFERQ, TYPE_LETTER)
			VALUES ('223', null, '3', 'q');

INSERT INTO NUMBER (ID, REFERP, REFERQ, TYPE_LETTER)
              VALUES ('223', null, '4', 'q');