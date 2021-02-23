/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Helper class for alert configuration messages.
 *
 * @author Douglas Lau
 */
public class AlertMessageHelper extends BaseHelper {

	/** Name creator */
	static private final UniqueNameCreator UNC = new UniqueNameCreator(
		"alert_msg_%d", 20, (n)->lookup(n));

	/** Create a unique record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}

	/** Don't instantiate */
	private AlertMessageHelper() {
		assert false;
	}

	/** Lookup the alert config with the specified name */
	static public AlertMessage lookup(String name) {
		return (AlertMessage) namespace.lookupObject(
			AlertMessage.SONAR_TYPE, name);
	}

	/** Get an alert config object iterator */
	static public Iterator<AlertMessage> iterator() {
		return new IteratorWrapper<AlertMessage>(namespace.iterator(
			AlertMessage.SONAR_TYPE));
	}

	/** Get the set of all messages for an alert configuration */
	static public Set<AlertMessage> getAllMessages(AlertConfig cfg) {
		TreeSet<AlertMessage> msgs = new TreeSet<AlertMessage>();
		Iterator<AlertMessage> it = iterator();
		while (it.hasNext()) {
			AlertMessage msg = it.next();
			if (msg.getAlertConfig() == cfg)
				msgs.add(msg);
		}
		return msgs;
	}

	/** Get the set of valid messages for an alert configuration */
	static public Set<AlertMessage> getValidMessages(AlertConfig cfg) {
		TreeSet<AlertMessage> msgs = new TreeSet<AlertMessage>();
		Iterator<AlertMessage> it = iterator();
		while (it.hasNext()) {
			AlertMessage msg = it.next();
			if (msg.getAlertConfig() == cfg &&
			    msg.getSignGroup() != null &&
			    msg.getQuickMessage() != null)
				msgs.add(msg);
		}
		return msgs;
	}
}
