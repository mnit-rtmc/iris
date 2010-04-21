/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.io.EOFException;
import java.util.Iterator;
import java.util.LinkedList;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * CanogaPoller is a java implementation of the 3M Canoga (tm) serial
 * communication protocol
 *
 * @author Douglas Lau
 */
public class CanogaPoller extends MessagePoller implements SamplePoller {

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
		throws EOFException
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
	public void download(ControllerImpl c, int p) {
		if(c.getActive()) {
			OpQueryConfig o = new OpQueryConfig(c);
			o.setPriority(p);
			o.start();
		}
	}

	/** List of all event data collectors on line */
	protected final LinkedList<OpQueryEventSamples> collectors =
		new LinkedList<OpQueryEventSamples>();

	/** Get a current event collector operation (if any) */
	protected OpQueryEventSamples getEventCollector(final ControllerImpl c){
		Iterator<OpQueryEventSamples> it = collectors.iterator();
		while(it.hasNext()) {
			OpQueryEventSamples qes = it.next();
			if(qes.isDone())
				it.remove();
			else if(qes.getController() == c)
				return qes;
		}
		return null;
	}

	/** Perform a controller reset */
	public void resetController(ControllerImpl c) {
		if(c.getActive())
			new OpQueryConfig(c).start();
	}

	/** Send sample settings to a controller */
	public void sendSettings(ControllerImpl c) {
		if(c.getActive())
			new OpQueryConfig(c).start();
	}

	/** Query sample data */
	public void querySamples(ControllerImpl c, int intvl, Completer comp) {
		if(c.hasActiveDetector()) {
			OpQueryEventSamples qes = getEventCollector(c);
			if(qes == null) {
				qes = new OpQueryEventSamples(c);
				collectors.add(qes);
				qes.start();
			} else
				qes.cleanup();
		}
		// FIXME: put logged data in bin
	}
}
