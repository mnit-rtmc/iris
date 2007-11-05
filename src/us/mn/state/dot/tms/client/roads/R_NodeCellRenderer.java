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
package us.mn.state.dot.tms.client.roads;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A list cell renderer for roadway nodes
 *
 * @author Douglas Lau
 */
public class R_NodeCellRenderer implements ListCellRenderer {

	/** Return a renderer component for the list cell */
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		R_NodeRenderer r = (R_NodeRenderer)value;
		r.setSelected(isSelected);
		return r;
	}
}
