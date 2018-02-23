CREATE TABLE event.action_plan_event (
	event_id integer PRIMARY KEY DEFAULT nextval('event.event_id_seq'),
	event_date timestamp with time zone NOT NULL,
	event_desc_id integer NOT NULL
		REFERENCES event.event_description(event_desc_id),
	action_plan VARCHAR(16),
	iris_user VARCHAR(15)
);

insert into event.event_description (event_desc_id,description)
	VALUES (900,'Action Plan ACTIVATED');
insert into event.event_description (event_desc_id,description)
	VALUES (901,'Action Plan DEACTIVATED');
insert into iris.system_attribute (name,value)
	VALUES ('action_plan_event_purge_days', '90');
