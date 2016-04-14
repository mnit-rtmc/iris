/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

import javax.swing.JLabel;
import javax.swing.JMenu;

/**
 * JMenu for displaying the legend for a theme.
 *
 * @author Douglas Lau
 */
public class LegendMenu extends JMenu {

  	/** Create a new legend menu */
 	public LegendMenu(String name, Theme t) {
		super(name);
		setTheme(t);
	}

	/** Set the theme that this menu displays */
	public void setTheme(Theme t) {
		removeAll();
		for (Style sty: t.getStyles()) {
			if (sty.legend) {
				JLabel l = new JLabel(sty.toString());
				l.setIcon(t.getLegend(sty));
				add(l);
			}
		}
	}
}
