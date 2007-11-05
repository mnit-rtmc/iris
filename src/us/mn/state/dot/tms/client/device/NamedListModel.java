/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.device;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.event.ListDataListener;

/**
 * Extends javax.swing.DefaultListModel adding a name field.
 *
 * @author Erik Engstrom
 * @author Douglas lau
 */
public class NamedListModel extends DefaultListModel {

	/** Status code */
	protected final int status;

	/** Model name */
	protected final String name;

	/** Legend icon */
	protected final Icon legend;

	/** Creates new named list model */
	public NamedListModel(String displayName) {
		status = 0;
		name = displayName;
		legend = null;
	}

	/** Create a new named list model */
	public NamedListModel(int s, String n, Icon l) {
		status = s;
		name = n;
		legend = l;
	}

	public int getStatus() {
		return status;
	}

	public String getName() {
		return name;
	}

	public Icon getLegend() {
		return legend;
	}

	public String toString() {
		return name;
	}

	/** Dispose of the list model */
	public void dispose() {
		ListDataListener[] l = getListDataListeners();
		for(int i = 0; i < l.length; i++)
			removeListDataListener(l[i]);
	}
}
