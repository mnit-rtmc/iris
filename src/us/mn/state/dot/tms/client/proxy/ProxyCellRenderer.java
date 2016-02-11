/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.sonar.SonarObject;

/**
 * ListCellRenderer for ProxyJList cells.
 *
 * @author Douglas Lau
 */
public class ProxyCellRenderer<T extends SonarObject>
	implements ListCellRenderer<T>
{
	/** Default cell renderer */
	private final DefaultListCellRenderer renderer =
		new DefaultListCellRenderer();

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Create a new proxy cell renderer */
	public ProxyCellRenderer(ProxyManager<T> m) {
		manager = m;
	}

	/** Get a list cell renderer component */
	@Override
	public Component getListCellRendererComponent(JList<? extends T> list,
		T value, int index, boolean isSelected, boolean cellHasFocus)
	{
		String v = (value != null)
		         ? manager.getDescription(value)
		         : null;
		return renderer.getListCellRendererComponent(list, v, index,
			isSelected, cellHasFocus);
	}
}
