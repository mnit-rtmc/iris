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
INSERT INTO sign_group (name, local) VALUES ('125_Pix_Wide', false);
INSERT INTO sign_group (name, local) VALUES ('10_Char_Wide', false);
INSERT INTO sign_group (name, local) VALUES ('12_Char_Wide', false);

DROP VIEW dms_message_view;
DROP TABLE dms_message;
