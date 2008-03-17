/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.canoga;

import java.util.Iterator;
import java.util.LinkedList;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.MessengerException;

/**
 * CanogaPoller is a java implementation of the 3M Canoga (tm) serial
 * communication protocol
 *
 * @author Douglas Lau
 */
public class CanogaPoller extends MessagePoller {

	/** Maximum address allowed for backplane addressing */
	static protected final int ADDRESS_MAX_BACKPLANE = 15;

	/** Minimum address allowed for EEPROM programmable */
	static protected final int ADDRESS_MIN_EEPROM = 128;

	/** Wildcard address */
	static protected final int ADDRESS_WILDCARD = 255;

	/** Create a new Canoga poller */
	public CanogaPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified controller */
	public AddressedMessage createMessage(ControllerImpl c)
		throws MessengerException
	{
		return new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c), c.getDrop());
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return (drop >= 0 && drop <= ADDRESS_MAX_BACKPLANE) ||
		       (drop >= ADDRESS_MIN_EEPROM && drop <= ADDRESS_WILDCARD);
	}

	/** Perform a controller download */
	public void download(ControllerImpl c, boolean reset, int p) {
		if(c.isActive()) {
			InitializeCanoga o = new InitializeCanoga(c, reset);
			o.setPriority(p);
			o.start();
		}
	}

	/** List of all event data collectors on line */
	protected final LinkedList<CollectEventData> collectors =
		new LinkedList<CollectEventData>();

	/** Get a current event collector operation (if any) */
	protected CollectEventData getEventCollector(final ControllerImpl c) {
		Iterator<CollectEventData> it = collectors.iterator();
		while(it.hasNext()) {
			CollectEventData ced = it.next();
			if(ced.isDone())
				it.remove();
			else if(ced.getController() == c)
				return ced;
		}
		return null;
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		if(c.hasActiveDetector()) {
			CollectEventData ced = getEventCollector(c);
			if(ced == null) {
				ced = new CollectEventData(c);
				collectors.add(ced);
				ced.start();
			} else
				ced.cleanup();
		}
		// FIXME: put logged data in bin
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		// Nothing to do here
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		return null;
	}
}
