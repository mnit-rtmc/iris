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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.NodeGroup;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * NodeGroupForm is a Swing dialog for entering and editing node groups
 *
 * @author Douglas Lau
 * @author Sandy Dinh
 */
class NodeGroupForm extends TMSObjectForm {

	/** Frame title */
	static protected final String TITLE = "Node Group ";

	/** Remote list object */
	protected IndexedList rList;

	/** Node group index */
	protected final int index;

	/** Remote node group object */
	protected NodeGroup group;

	/** Description text field */
	protected final JTextField desc = new JTextField(20);

	/** Apply changes button */
	protected final JButton apply = new JButton("Apply Changes");

	/** Create a new node group form */
	public NodeGroupForm(TmsConnection tc, int i) {
		super(TITLE + i, tc);
		index = i;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		rList = (IndexedList)tms.getGroups().getList();
		obj = rList.getElement(index);
		group = (NodeGroup)obj;
		super.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(createSetupPanel());
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

	/** Create the setup panel */
	protected JPanel createSetupPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BORDER);
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("Description"));
		box.add(Box.createHorizontalStrut(HGAP));
		desc.setEnabled(admin);
		box.add(desc);
		box.add(Box.createHorizontalGlue());
		panel.add(box);
		return panel;
	}

	/** Update the form with the current state */
	protected void doUpdate() throws RemoteException {
		desc.setText(group.getDescription());
	}

	/** Apple changed form entries to the remote node group */
	protected void applyPressed() throws Exception {
		group.setDescription(desc.getText());
		group.notifyUpdate();
		rList.update(index);
	}
}
