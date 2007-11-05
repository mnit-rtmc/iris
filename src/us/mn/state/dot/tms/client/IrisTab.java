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
package us.mn.state.dot.tms.client;

import javax.swing.JPanel;

/**
 * Super class of all tabs used in the IrisClient.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class IrisTab {

	/** Name of tab */
	protected final String name;

	/** Get the name of the tab */
	public String getName() {
		return name;
	}

	/** Get the tab number */
	abstract public int getNumber();

	/** Tip for hovering */
	protected final String tip;

	/** Get the tip */
	public String getTip() {
		return tip;
	}

	/** Create a new Iris tab */
	public IrisTab(String n, String t) {
		name = n;
		tip = t;
	}

	/** Get the tab panel */
	abstract public JPanel getTabPanel();

	/** Get the main panel */
	abstract public JPanel getMainPanel();

	/** Perform any clean up necessary */
	public void dispose() { }
}
