/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toolbar;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A panel for use on the toolbar.
 *
 * @author Douglas Lau
 */
abstract public class ToolPanel extends JPanel {

	/** Create a new tool panel */
	public ToolPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	/** Dispose of the tool panel */
	public void dispose() { }
}
