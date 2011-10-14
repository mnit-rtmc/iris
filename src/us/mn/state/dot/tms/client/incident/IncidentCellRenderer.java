/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.SwingConstants;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Incident;

/**
 * ListCellRenderer for incident cells.
 *
 * @author Douglas Lau
 */
public class IncidentCellRenderer extends DefaultListCellRenderer {

	/** Incident manager */
	protected final IncidentManager manager;

	/** Create a new incident cell renderer */
	public IncidentCellRenderer(IncidentManager m) {
		manager = m;
	}

	/** Get a list cell renderer component */
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		Component c = super.getListCellRendererComponent(list,
			value, index, isSelected, cellHasFocus);
		if(c instanceof JLabel) {
			JLabel lbl = (JLabel)c;
			lbl.setHorizontalTextPosition(SwingConstants.TRAILING);
			lbl.setText("");
			lbl.setIcon(null);
			if(value instanceof Incident) {
				Incident inc = (Incident)value;
				String dsc = manager.getDescription(inc);
				lbl.setText(dsc + getCamera(inc));
				lbl.setIcon(lookupIcon(inc));
			}
		}
		return c;
	}

	/** Lookup the icon to use for an incident */
	protected Icon lookupIcon(Incident inc) {
		if(inc.getCleared())
			return manager.getIcon(null);
		else
			return manager.getIcon(inc);
	}

	/** Get the incident camera */
	protected String getCamera(Incident inc) {
		Camera cam = inc.getCamera();
		if(cam != null) 
			return " -- " + cam.getName();
		else
			return "";
	}
}
