/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.tms.SignMessage;

/**
 * List cell renderer for deployed DMS message list on main tab
 *
 * @author Erik Engstrom
 */
public class DmsMessageRenderer extends DefaultListCellRenderer
	implements ListCellRenderer
{
	/** Create a new DmsMessageRenderer */
	public DmsMessageRenderer() {
		super();
	}

	/** Get a component to render a cell in the list */
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		super.getListCellRendererComponent(list, value, index,
			isSelected, cellHasFocus);
		DMSProxy proxy = (DMSProxy)value;
		SignMessage message = proxy.getMessage();
		setText(proxy.getId() + " : " + message.getMulti());
		return this;
	}
}
