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

import javax.swing.Icon;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;

/**
 * ListCellRenderer for ProxyJList cells.
 *
 * @author Douglas Lau
 */
public class ProxyCellRenderer<T extends SonarObject>
	extends IListCellRenderer<T>
{
	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Create a new proxy cell renderer */
	public ProxyCellRenderer(ProxyManager<T> m) {
		manager = m;
	}

	/** Convert proxy to a string */
	@Override
	protected String valueToString(T proxy) {
		return manager.getDescription(proxy);
	}

	/** Get an icon for a proxy */
	@Override
	protected Icon getIcon(T proxy) {
		return manager.getIcon(proxy);
	}
}
