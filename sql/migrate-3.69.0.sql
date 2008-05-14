SET SESSION AUTHORIZATION 'tms';

CREATE TABLE sign_group (
	name VARCHAR(16) PRIMARY KEY,
	local BOOLEAN NOT NULL
);

CREATE TABLE dms_sign_group (
	name VARCHAR(24) PRIMARY KEY,
	dms text NOT NULL REFERENCES dms(id),
	sign_group VARCHAR(16) NOT NULL REFERENCES sign_group
);

CREATE TABLE sign_text (
	name VARCHAR(20) PRIMARY KEY,
	sign_group VARCHAR(16) NOT NULL REFERENCES sign_group,
	line smallint NOT NULL,
	message VARCHAR(24) NOT NULL,
	priority smallint NOT NULL,
	CONSTRAINT sign_text_line CHECK ((line >= 1) AND (line <= 12)),
	CONSTRAINT sign_text_priority CHECK
		((priority >= 1) AND (priority <= 99))
);

-- do not leave this in migrate script
INSERT INTO sign_group (name, local) VALUES ('PIXEL 125 WIDE', false);
INSERT INTO sign_group (name, local) VALUES ('CHAR 10 WIDE', false);
INSERT INTO sign_group (name, local) VALUES ('CHAR 12 WIDE', false);

CREATE TEMP SEQUENCE __a_seq MINVALUE 0;
CREATE TEMP SEQUENCE __b_seq MINVALUE 0;
CREATE TEMP SEQUENCE __c_seq MINVALUE 0;

INSERT INTO sign_text (name, sign_group, line, message, priority)
	(SELECT 'PIXEL 125 WIDE_' || trim(both FROM to_char(nextval('__a_seq'),
	'999')), 'PIXEL 125 WIDE', line, message, priority FROM dms_message
	WHERE dms IS NULL AND char_length(message) > 0 AND line < 4);

INSERT INTO sign_text (name, sign_group, line, message, priority)
	(SELECT 'CHAR 12 WIDE_' || trim(both FROM to_char(nextval('__b_seq'),
	'999')), 'CHAR 12 WIDE', line, abbrev, priority FROM dms_message
	WHERE dms IS NULL AND char_length(abbrev) > 0);

INSERT INTO sign_text (name, sign_group, line, message, priority)
	(SELECT 'CHAR 12 WIDE_' || trim(both FROM to_char(nextval('__b_seq'),
	'999')), 'CHAR 12 WIDE', line, message, priority FROM dms_message
	WHERE dms IS NULL AND char_length(message) > 0 AND
	char_length(message) <= 12);

INSERT INTO sign_text (name, sign_group, line, message, priority)
	(SELECT 'CHAR 10 WIDE_' || trim(both FROM to_char(nextval('__c_seq'),
	'999')), 'CHAR 10 WIDE', line, abbrev, priority FROM dms_message
	WHERE dms IS NULL AND char_length(abbrev) > 0 AND
	char_length(abbrev) <= 10);

INSERT INTO sign_text (name, sign_group, line, message, priority)
	(SELECT 'CHAR 10 WIDE_' || trim(both FROM to_char(nextval('__c_seq'),
	'999')), 'CHAR 10 WIDE', line, message, priority FROM dms_message
	WHERE dms IS NULL AND char_length(message) > 0 AND
	char_length(message) <= 10);

CREATE FUNCTION copy_sign_text(TEXT) RETURNS bool AS '
	DECLARE dms_id ALIAS FOR $1;
		b INTEGER;
	BEGIN
		INSERT INTO sign_group (name, local) VALUES (dms_id, true);
		INSERT INTO dms_sign_group (name, dms, sign_group)
		VALUES (dms_id || ''_'' || dms_id, dms_id, dms_id);
		INSERT INTO dms_sign_group (name, dms, sign_group)
		VALUES (''PIXEL 125 WIDE_'' || dms_id, dms_id,
		''PIXEL 125 WIDE'');
		CREATE TEMP SEQUENCE __d_seq MINVALUE 0;
		INSERT INTO sign_text (name,sign_group, line, message, priority)
		(SELECT dms_id || ''_'' || trim(both FROM
		to_char(nextval(''__d_seq''), ''999'')), dms_id, line, message,
		priority
		FROM dms_message
		WHERE dms = dms_id AND char_length(message) > 0);
		DROP SEQUENCE __d_seq;
		RETURN true;
	END;'
LANGUAGE PLPGSQL;

SELECT count(copy_sign_text(id)) AS Local_Sign_Groups FROM dms;

DROP FUNCTION copy_sign_text(TEXT);

DROP VIEW dms_message_view;
DROP TABLE dms_message;

CREATE VIEW sign_text_view AS
	SELECT dms, local, line, message, priority
	FROM dms_sign_group
	JOIN sign_group ON dms_sign_group.sign_group = sign_group.name
	JOIN sign_text ON sign_group.name = sign_text.sign_group;

GRANT SELECT ON sign_text_view TO PUBLIC;
