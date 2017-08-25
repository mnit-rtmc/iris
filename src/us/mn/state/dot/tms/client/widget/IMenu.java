/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import javax.swing.JMenu;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IMenu is a JMenu helper.
 *
 * @author Douglas Lau
 */
abstract public class IMenu extends JMenu {

	/** Create a new menu */
	protected IMenu(String n) {
		super(I18N.get(n));
	}

	/** Add a sub-menu */
	protected void addMenu(JMenu m) {
		if (m.getItemCount() > 0)
			add(m);
	}

	/** Add an action */
	protected void addItem(IAction a) {
		if (a != null)
			add(a);
	}
}
