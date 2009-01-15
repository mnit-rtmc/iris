/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.LCSList;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AddForm;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.SortedListForm;

/**
 * LcsListForm
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public class LcsListForm extends SortedListForm {

	/** Frame title */
	static private final String TITLE = "Lane Control Signal";

	/** Add title */
	static private final String ADD_TITLE = "Add Lane Control Signal";

	/** Create a new LcsListForm */
	public LcsListForm(TmsConnection tc) {
		super(TITLE, tc, tc.getProxy().getLCSList(),
			Icons.getIcon("lcs"));
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		add(createListPanel());
		super.initialize();
	}

	/** Add an item to the list */
	protected void addItem() throws Exception {
		connection.getDesktop().show(new AddLcsForm());
		updateButtons();
	}

	/** Edit an item in the list */
	protected void editItem() throws RemoteException {
		String id = getSelectedItem();
		if(id != null) {
			connection.getDesktop().show(
				new LcsProperties(connection, id));
		}
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "C694W55";
	}

	/** Form to add a lane control signal to the list */
	protected class AddLcsForm extends AddForm {

		/** Field for entering the number of lanes */
		protected JTextField lanes = new JTextField();

		/** Create a lane control signal add form */
		public AddLcsForm() {
			super(ADD_TITLE, sList);
		}

		/** Initialize the widgets on the form */
		protected void initialize() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(Box.createVerticalGlue());
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(new JLabel("Name"));
			box.add(Box.createHorizontalStrut(HGAP));
			box.add(name);
			box.add(Box.createHorizontalGlue());
			add(box);
			add(Box.createVerticalStrut(VGAP));
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(new JLabel("Number of lanes:"));
			box.add(Box.createHorizontalStrut(HGAP));
			box.add(lanes);
			box.add(Box.createHorizontalGlue());
			add(box);
			add(Box.createVerticalStrut(VGAP));
			add(apply);
			add(Box.createVerticalGlue());
			new ActionJob(this, apply) {
				public void perform() throws Exception {
					addItem();
				}
			};
		}

		/** Add an item to the list */
		protected String addItem() throws Exception {
			String id = name.getText().trim();
			if(id.equals(""))
				return null;
			int n = Integer.parseInt(lanes.getText());
			if(n > 0)
				((LCSList)rList).add(id, n);
			else {
				throw new ChangeVetoException(
					"Lane must be greater than 0");
			}
			name.setText("");
			lanes.setText("");
			return id;
		}
	}
}
