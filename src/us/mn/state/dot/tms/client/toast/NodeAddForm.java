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
package us.mn.state.dot.tms.client.toast;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.tms.NodeGroup;
import us.mn.state.dot.tms.utils.ActionJob;

/**
 * NodeAddForm is a Swing dialog for adding a node
 *
 * @author Sandy Dinh
 * @author Douglas Lau
 */
public class NodeAddForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Node: ";

	/** Form panel */
	protected final FormPanel panel = new FormPanel(true);

	/** NodeGroup */
	protected final NodeGroup group;

	/** JSpinner number model */
	protected final SpinnerNumberModel model =
		new SpinnerNumberModel(1, 1, 9, 1);

	/** JSpinner for node ID */
	protected final JSpinner spinner = new JSpinner(model);

	/** Add button */
	protected final JButton add = new JButton("Add");

	/** Create a new commline adding form */
	public NodeAddForm(NodeGroup g, int index) {
		super(TITLE + index);
		group = g;
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		add(panel);
		panel.addRow("Node ID", spinner);
		panel.addRow(add);
		new ActionJob(this, add) {
			public void perform() throws Exception {
				addPressed();
			}
		};
	}

	/** This is called when the 'add' button is pressed */
	protected void addPressed() throws Exception {
		Object id = model.getValue();
		group.insertNode((id.toString()).charAt(0));
		group.notifyUpdate();
	}
}
