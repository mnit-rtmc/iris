/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.rmi.RemoteException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.Node;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * NodeForm is a Swing dialog for entering and editing nodes
 *
 * @author Douglas Lau
 * @author Sandy Dinh
 */
final class NodeForm extends TMSObjectForm {

	/** Frame title */
	static protected final String TITLE = "Node ";

	/** Remote node object */
	protected final Node node;

	/** Remote location object */
	protected LocationPanel location;

	/** Notes text */
	protected final JTextArea notes = new JTextArea(3, 20);

	/** Apply changes button */
	protected final JButton apply = new JButton("Apply Changes");

	/** Create a new node form */
	public NodeForm(TmsConnection tc, String id, Node n) {
		super(TITLE + id, tc);
		node = n;
		obj = n;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		location = new LocationPanel(admin, node.getLocation(),
			connection.getSonarState());
		notes.setText(node.getNotes());
		super.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(createLocationPanel());
		if(admin) {
			add(Box.createVerticalStrut(VGAP));
			new ActionJob(this, apply) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
			add(apply);
		}
		add(Box.createVerticalStrut(VGAP));
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		location.setBorder(BORDER);
		location.addNote(notes);
		return location;
	}

	/** Update the form with the current state */
	protected void doUpdate() throws RemoteException {
		location.doUpdate();
		String n = node.getNotes();
		if(n != null)
			notes.setText(n);
		else
			notes.setText("");
	}

	/** Apple changed form entries to the remote node */
	protected void applyPressed() throws Exception {
		location.applyPressed();
		node.setNotes((String)notes.getText());
		node.notifyUpdate();
	}
}
