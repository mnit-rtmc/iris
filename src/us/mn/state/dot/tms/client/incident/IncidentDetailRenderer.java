/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import us.mn.state.dot.tms.IncidentDetail;

/**
 * Cell renderer used for incident detail.
 *
 * @author Douglas Lau
 */
public class IncidentDetailRenderer extends DefaultListCellRenderer {

	/** Configure the renderer component for incident detail */
	public Component getListCellRendererComponent(JList list,
		Object value, int index, boolean isSelected,
		boolean cellHasFocus)
	{
		String v = "";
		if(value instanceof IncidentDetail) {
			IncidentDetail dtl = (IncidentDetail)value;
			v = dtl.getDescription();
		}
		return super.getListCellRendererComponent(list, v, index,
			isSelected, cellHasFocus);
	}
}
