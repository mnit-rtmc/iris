INSERT INTO iris.system_attribute (name, value) VALUES ('dict_allowed_scheme', '0');
INSERT INTO iris.system_attribute (name, value) VALUES ('dict_banned_scheme', '0');
insert into iris.privilege values ('prv_dic1', 'policy_admin', 'word(/.*)?',true,true,true,true);
insert into iris.privilege values ('prv_dic2', 'dms_tab', 'word(/.*)?',true,false,false,false);
CREATE TABLE iris.word (
	name VARCHAR(24) PRIMARY KEY,
	abbr VARCHAR(12),
	allowed boolean DEFAULT false NOT NULL
);
