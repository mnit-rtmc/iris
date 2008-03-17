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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * SwitcherComponentForm is a Swing dialog for entering and editing
 * switcher component records
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
abstract public class SwitcherComponentForm extends TMSObjectForm {

	/** Tabbed pane */
	protected final JTabbedPane tab = new JTabbedPane( JTabbedPane.TOP );

	/** Controller button */
	protected final JButton controller = new JButton("Controller");

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 20);

	/** Apply button */
	protected final JButton apply = new JButton( "Apply Changes" );

	/** Create a new switcher component form */
	protected SwitcherComponentForm(String t, TmsConnection tc) {
		super(t, tc);
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		super.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(tab);
		if(admin) {
			add(Box.createVerticalStrut(VGAP));
			new ActionJob(this, apply) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
			add(apply);
			apply.setEnabled(admin);
		}
		add(Box.createVerticalStrut(VGAP));
	}

	/** Update the form with the current state of the object */
	protected void doUpdate() throws RemoteException {
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
//		device.setNotes(notes.getText());
	}
}
