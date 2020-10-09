/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2019  Minnesota Department of Transportation
 * Copyright (C) 2018  Iteris Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server;

import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.event.ActionPlanEvent;
import us.mn.state.dot.tms.server.event.AlarmEvent;
import us.mn.state.dot.tms.server.event.BeaconEvent;
import us.mn.state.dot.tms.server.event.CameraSwitchEvent;
import us.mn.state.dot.tms.server.event.CameraVideoEvent;
import us.mn.state.dot.tms.server.event.ClientEvent;
import us.mn.state.dot.tms.server.event.CommEvent;
import us.mn.state.dot.tms.server.event.DetAutoFailEvent;
import us.mn.state.dot.tms.server.event.GateArmEvent;
import us.mn.state.dot.tms.server.event.MeterEvent;
import us.mn.state.dot.tms.server.event.PriceMessageEvent;
import us.mn.state.dot.tms.server.event.SignEvent;
import us.mn.state.dot.tms.server.event.TagReadEvent;
import us.mn.state.dot.tms.server.event.TravelTimeEvent;

/**
 * Job to periodically purge database event records.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class EventPurgeJob extends Job {

	/** Create a new job to purge database events */
	public EventPurgeJob() {
		super(Calendar.DATE, 1, Calendar.HOUR, 2);
	}

	/** Perform the event purge job */
	public void perform() throws TMSException {
		ActionPlanEvent.purgeRecords();
		AlarmEvent.purgeRecords();
		BeaconEvent.purgeRecords();
		CameraSwitchEvent.purgeRecords();
		CameraVideoEvent.purgeRecords();
		ClientEvent.purgeRecords();
		CommEvent.purgeRecords();
		DetAutoFailEvent.purgeRecords();
		GateArmEvent.purgeRecords();
		IpawsAlertImpl.purgeRecords();  // IPAWS Alerts are stored as events
		MeterEvent.purgeRecords();
		PriceMessageEvent.purgeRecords();
		SignEvent.purgeRecords();
		TagReadEvent.purgeRecords();
		TravelTimeEvent.purgeRecords();
	}
}
