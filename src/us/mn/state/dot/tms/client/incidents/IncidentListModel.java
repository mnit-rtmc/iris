/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incidents;

import java.util.LinkedList;
import javax.swing.AbstractListModel;
import us.mn.state.dot.tdxml.Incident;
import us.mn.state.dot.tdxml.IncidentListener;

/**
 * IncidentListModel is a <code>ListModel</code> that can be registered with
 * an <code>IncidentClient</code> to obtain incident data.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class IncidentListModel extends AbstractListModel implements
	IncidentListener
{
	/** Temporary incident list */
	protected final LinkedList<Incident> list = new LinkedList<Incident>();

	/** Current incident data */
	protected Incident[] incidents = new Incident[0];

	/** Create a new incident list model */
	public IncidentListModel() {
	}

	public int getSize() {
		return incidents.length;
	}

	public Object getElementAt(int index) {
		return incidents[index];
	}

	protected void finishUpdate() {
		int before = incidents.length;
		incidents = (Incident[])list.toArray(incidents);
		int after = incidents.length;
		if(before < after)
			fireIntervalAdded(this, before, after);
		if(before > after)
			fireIntervalRemoved(this, after, before);
		fireContentsChanged(this, 0, Math.min(before, after));
	}

	public void update(boolean finish) {
		if(finish)
			finishUpdate();
		list.clear();
	}

	public void update(Incident i) {
		list.add(i);
	}
}
