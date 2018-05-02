/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.proxy.ProxyAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Selects the specified incident.
 *
 * @author Douglas Lau
 */
public class IncidentSelectAction extends ProxyAction<Incident> {

	/** Convert an event type to a string */
	static private String eventTypeToString(int t) {
		EventType et = EventType.fromId(t);
		String v = (et != null) ? et.toString() : "";
		// NOTE: strip off the "INCIDENT_" prefix
		int i = v.indexOf('_');
		return (i >= 0) ? v.substring(i + 1) : v;
	}

	/** Get action name for an incident */
	static private String getName(Incident inc) {
		return (inc != null)
		      ? eventTypeToString(inc.getEventType()).toLowerCase()
		      : I18N.get("incident.none");
	}

	/** Incident manager */
	private final IncidentManager manager;

	/** Create a new action to select an incident */
	public IncidentSelectAction(Incident inc, IncidentManager man) {
		super("incident.select", inc);
		manager = man;
		putValue(Action.NAME, getName(inc));
		putValue(Action.SMALL_ICON, manager.getIcon(inc));
		setEnabled(inc != null);
	}

	/** Actually perform the action */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		if (proxy != null)
			manager.getSelectionModel().setSelected(proxy);
	}
}
