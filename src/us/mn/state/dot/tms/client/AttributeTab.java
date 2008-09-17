/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.TrafficDeviceAttribute;
import us.mn.state.dot.tms.client.toast.TrafficDeviceForm;

/**
 * This is a tab for viewing and editing attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class AttributeTab extends JPanel {

	/** tab name */
	protected final String TAB_NAME = "Attributes";

	/** Attribute table model */
	protected TrafficDeviceAttributeTableModel attribute_model;

	/** traffic device attribute table */
	protected final JTable attribute_table = new JTable();

	/** Button to delete the selected role */
	protected final JButton del_attrib_btn = new JButton("Delete Attribute");

	/** form this tab is displayed on */
	protected final TrafficDeviceForm m_tdf;

	/** Sonar state */
	protected final SonarState m_state;

	/** device id */
	protected final String m_id;

	/** user is an admin */
	protected final boolean m_admin;	//FIXME: what if user changes to admin? This isn't updated

	/** create the attribute editor tab */
	public AttributeTab(boolean admin, TrafficDeviceForm tdf, 
		SonarState state, String id)
	{
		m_admin = admin;
		m_tdf = tdf;
		m_state = state;
		m_id = id;

		// arg checks
		boolean bogus=(tdf==null || state==null || id==null);
		assert !bogus : "bogus args";
		if( bogus )
			return;

		// create table model
		attribute_model = new TrafficDeviceAttributeTableModel(m_id, 
			state.getTrafficDeviceAttributes(),admin);
		System.err.println("DMSProperties.DMSProperties(): attribute_model="+attribute_model.toString());

		createControls();
	}

	/** return the text name of the tab */
	public String getName() {
		return TAB_NAME;
	}

	/** create the attribute editor tab */
	protected void createControls() {

		this.setLayout(new GridBagLayout());
		GridBagConstraints bag = new GridBagConstraints();
		bag.anchor = GridBagConstraints.CENTER;
		bag.gridwidth = 1;
		bag.gridheight = 1;
		bag.insets.top = 5;
		bag.insets.left = 5;
		bag.insets.right = 5;
		bag.insets.bottom = 5;

		// attribute table
		initAttributeTable();
		bag.fill = GridBagConstraints.HORIZONTAL;
		JScrollPane scroll = new JScrollPane(attribute_table);
		bag.gridx = 0;
		bag.gridy = 0;
		bag.weightx = 1;
		bag.weighty = 1;
		this.add(scroll, bag);

		// delete attribute button
		bag.weightx = 1;
		bag.weighty = 1;
		bag.fill = GridBagConstraints.NONE;
		bag.gridx = 0;
		bag.gridy = GridBagConstraints.RELATIVE;
		this.add(del_attrib_btn, bag);
		del_attrib_btn.setEnabled(false); //FIXME: delete isn't working yet
		//del_attrib_btn.setEnabled(m_admin);
		new ActionJob(this, del_attrib_btn) {
			public void perform() throws Exception {
				final ListSelectionModel s = 
					attribute_table.getSelectionModel();
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					attribute_model.deleteRow(row);
			}
		};
	}

	/** Initialize the attribute table */
	protected void initAttributeTable() {
		final ListSelectionModel s = attribute_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				// FIXME
				//if(!event.getValueIsAdjusting())
				//	selectSignText();
			}
		};
		attribute_table.setAutoCreateColumnsFromModel(false);
		attribute_table.setColumnModel(
			TrafficDeviceAttributeTableModel.createColumnModel());
		attribute_table.setModel(attribute_model);
		attribute_table.setPreferredScrollableViewportSize(
			new Dimension(280, 200));
	}

	/** cleanup */
	public void dispose() {
		if(attribute_model != null)
			attribute_model.dispose();
	}
}

