/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2008  Minnesota Department of Transportation
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.CommunicationLine;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.Node;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * CircuitAddForm is a form for adding circuits
 *
 * @author Douglas Lau
 */
public class CircuitAddForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Circuit @ ";

	/** TMS connection */
	protected final TmsConnection connection;

	/** Form panel */
	protected final FormPanel panel = new FormPanel(true);

	/** Remote list object */
	protected IndexedList rList;

	/** Remote node object */
	protected final Node node;

	/** Line lookup button */
	protected final JButton lineButton = new JButton("Line");

	/** Line combo box */
	protected final JComboBox lineBox = new JComboBox();

	/** Circuit text field */
	protected final JTextField circuitTextField = new JTextField(8);

	/** Add button */
	protected final JButton addButton = new JButton("Add");

	/** Create a new circuit add form */
	public CircuitAddForm(TmsConnection tc, Node n, String nid) {
		super(TITLE + "Node " + nid);
		connection = tc;
		node = n;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		rList = (IndexedList)tms.getLines().getList();
		lineBox.setModel(new WrapperComboBoxModel(
			tms.getLines().getModel()));

		add(panel);
		panel.addRow("Circuit ID", circuitTextField);
		panel.addRow(lineButton, lineBox);
		if(connection.isAdmin())
			panel.addRow(addButton);

		new ActionJob(this, lineButton) {
			public void perform() throws Exception {
				linePressed();
			}
		};
		new ActionJob(this, lineBox) {
			public void perform() {
				doUpdate();
			}
		};
		if(connection.isAdmin()) {
			new ActionJob(this, addButton) {
				public void perform() throws Exception {
					addPressed();
				}
			};
		}
	}

	/** Update the form with the current state */
	protected void doUpdate() {
		lineButton.setEnabled(lineBox.getSelectedIndex() >= 0);
	}

	/** Apple changed form entries to the remote node */
	protected void addPressed() throws Exception {
		int index = lineBox.getSelectedIndex();
		CommunicationLine line = null;
		if(index > 0)
			line = (CommunicationLine)rList.getElement(index);
		String text = circuitTextField.getText();
		if(line == null || text == null)
			return;
		node.insertCircuit(text, line);
		node.notifyUpdate();
	}

	/** Lookup the associated line */
	protected void linePressed() throws Exception {
		int index = lineBox.getSelectedIndex();
		if(index >= 0) {
			connection.getDesktop().show(
				new CommunicationLineForm(connection, index));
		}
	}
}
