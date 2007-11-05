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
import javax.swing.JTextField;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.utils.ActionJob;

/**
 * AddForm is a form for adding string-keyed records, like Roadway
 *
 * @author Douglas Lau
 */
public class AddForm extends AbstractForm {

	/** Remote sorted list */
	protected final SortedList rList;

	/** Form panel */
	protected final FormPanel panel = new FormPanel(true);

	/** Name to add to the list */
	protected final JTextField name = new JTextField(20);

	/** Apply button */
	protected final JButton apply = new JButton("Add");

	/** Create an add form */
	public AddForm(String t, SortedList r) {
		super(t);
		rList = r;
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		add(panel);
		panel.addRow("Name", name);
		panel.addRow(apply);
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
		rList.add(id);
		name.setText("");
		return id;
	}
}
