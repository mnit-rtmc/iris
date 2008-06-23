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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.DMSList;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AddForm;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.SortedListForm;
import us.mn.state.dot.tms.client.toast.TmsForm;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * DMSListForm
 *
 * @author Douglas Lau
 */
public class DMSListForm extends SortedListForm {

	/** Frame title */
	static protected String TITLE = I18NMessages.get("DMSListForm.Title")+": ";

	/** Add title */
	static protected final String ADD_TITLE = "Add DMS";

	/** Remote sign list interface */
	protected final DMSList signList;

	/** Alert line 1 message field */
	protected final JTextField line1 = new JTextField();

	/** Alert line 2 message field */
	protected final JTextField line2 = new JTextField();

	/** Alert line 3 message field */
	protected final JTextField line3 = new JTextField();

	/** Create a new DMSListForm */
	public DMSListForm(TmsConnection tc) {
		super(TITLE, tc, tc.getProxy().getDMSListModel(),
			Icons.getIcon("drum-inactive"));
		signList = (DMSList)obj;
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		JTabbedPane tab = new JTabbedPane();
		tab.add("List", createListPanel());
		if(connection.isAlert())
			tab.add("Alert", createAlertPanel());
		add(tab);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create the alert panel */
	protected JPanel createAlertPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridwidth = 2;
		JLabel label = new JLabel("WARNING: Alert will be sent to");
		label.setForeground(TmsForm.ERROR);
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("all active "+I18NMessages.get("DMSListForm.AlertText")+".");
		label.setForeground(TmsForm.ERROR);
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("* Double check your spelling *");
		label.setForeground(TmsForm.ERROR);
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("* Don't use any naughty words *");
		label.setForeground(TmsForm.ERROR);
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridwidth = 1;
		bag.anchor = GridBagConstraints.EAST;
		bag.insets.right = 2;
		label = new JLabel("Line 1");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Line 2");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Line 3");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = 1;
		bag.gridy = 4;
		bag.insets.right = 0;
		bag.insets.top = 4;
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		lay.setConstraints(line1, bag);
		panel.add(line1);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(line2, bag);
		panel.add(line2);
		lay.setConstraints(line3, bag);
		panel.add(line3);
		bag.gridx = 0;
		bag.gridwidth = 2;
		bag.weightx = 0;
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.NONE;
		final JButton send = new JButton(I18NMessages.get("DMSListForm.SendButton"));
		lay.setConstraints(send, bag);
		panel.add(send);
		final JButton clear = new JButton(I18NMessages.get("DMSListForm.ClearButton"));
		lay.setConstraints(clear, bag);
		panel.add(clear);
		new ActionJob(this, send) {
			public void perform() throws Exception {
				sendPressed(send);
			}
		};
		new ActionJob(this, clear) {
			public void perform() throws Exception {
				clearPressed(clear);
			}
		};
		return panel;
	}

	/** Send an alert when the send button is pressed */
	protected void sendPressed(JButton send) throws Exception {
		String[] text = new String[3];
		text[0] = line1.getText().trim().toUpperCase();
		text[1] = line2.getText().trim().toUpperCase();
		text[2] = line3.getText().trim().toUpperCase();
		signList.sendGroup(null, "Alert", text);
	}

	/** Clear an alert when the clear button is pressed */
	protected void clearPressed(JButton clear) throws Exception {
		line1.setText("");
		line2.setText("");
		line3.setText("");
		signList.clearGroup(null, "USER FIXME");
	}

	/** Add an item to the list */
	protected void addItem() throws Exception {
		connection.getDesktop().show(new AddForm(ADD_TITLE, sList));
		updateButtons();
	}

	/** Edit an item in the list */
	protected void editItem() throws RemoteException {
		String id = getSelectedItem();
		if(id != null) {
			connection.getDesktop().show(
				new DMSProperties(connection, id));
		}
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "V394W02";
	}
}
