/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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

import javax.swing.JTextArea;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.LocationPanel;

/**
 * R_NodeProperties is a form for viewing and editing roadway node parameters.
 *
 * @author Douglas Lau
 */
public class R_NodeLocationPanel extends LocationPanel {

	/** Component for editing notes */
	protected final JTextArea notes_txt = new JTextArea(3, 20);

	/** Node being edited */
	protected final R_Node node;

	/** Create a new roadway node location panel */
	public R_NodeLocationPanel(Session s, R_Node n) {
		super(s, n.getGeoLoc());
		node = n;
	}

	/** Initialize the widgets on the panel */
	public void initialize() {
		super.initialize();
		addRow("Notes", notes_txt);
		createJobs();
	}

	/** Create the jobs */
	protected void createJobs() {
		new FocusJob(notes_txt) {
			public void perform() {
				if(wasLost())
					node.setNotes(notes_txt.getText());
			}
		};
	}

	/** Update one attribute on the panel */
	public void doUpdateAttribute(String a) {
		if(a == null || a.equals("notes"))
			notes_txt.setText(node.getNotes());
	}
}
