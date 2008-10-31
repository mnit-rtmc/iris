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
package us.mn.state.dot.tms.client.system;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.Box;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.client.SonarState;

/**
 * This is a tab for viewing and editing system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SystemAttributeTab extends JPanel {

	/** tab name */
	protected final String TAB_NAME = "System Attributes";

	/** table model */
	protected SystemAttributeTableModel m_tableModel;

	/** traffic device attribute table */
	protected final JTable m_table = new JTable();

	/** Button to delete the selected attribute */
	protected final JButton del_attrib_btn = 
		new JButton("Delete");

	/** form this tab is displayed on */
	protected final PolicyForm m_form;

	/** Sonar state */
	TypeCache<SystemAttribute> m_systemAttributes;

	/** user is an admin */
//FIXME: what if user changes to admin? This isn't updated
//FIXME: use system_attribute permissions, not admin?
	protected final boolean m_admin;	

	/** Create the attribute editor tab.
	 * @param admin True if user is an admin.
	 * @param form Form this tab is placed onto.
	 * @param sa Type cache for system attributres.
	 */
	public SystemAttributeTab(boolean admin, PolicyForm form, 
		TypeCache<SystemAttribute> sa)
	{
		m_admin = admin;
		m_form = form;
		m_systemAttributes = sa;

		// arg checks
		boolean bogus=(form==null || sa==null);
		assert !bogus : "bogus args";
		if( bogus )
			return;

		m_tableModel = new SystemAttributeTableModel(admin,sa);
		createControls();
	}

	/** return the text name of the tab */
	public String getName() {
		return TAB_NAME;
	}

	/** create the attribute editor tab */
	protected void createControls() {
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		this.add(Box.createRigidArea(new Dimension(0,10)));

		// table
		initTable();
		JScrollPane scroll = new JScrollPane(m_table);
		this.add(scroll);

		this.add(Box.createVerticalGlue());
		this.add(Box.createRigidArea(new Dimension(0,20)));

		// delete button
		del_attrib_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(del_attrib_btn);
		del_attrib_btn.setEnabled(m_admin);

		this.add(Box.createRigidArea(new Dimension(0,20)));

		new ActionJob(this, del_attrib_btn) {
			public void perform() throws Exception {
				final ListSelectionModel s = 
					m_table.getSelectionModel();
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					m_tableModel.deleteRow(row);
			}
		};
	}

	/** Initialize the table */
	protected void initTable() {
		final ListSelectionModel s = m_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				// do nothing
			}
		};
		m_table.setAutoCreateColumnsFromModel(false);
		m_table.setColumnModel(
			SystemAttributeTableModel.createColumnModel());
		m_table.setModel(m_tableModel);
		m_table.setPreferredScrollableViewportSize(
			new Dimension(280, 200));
	}

	/** cleanup */
	public void dispose() {
		if(m_tableModel != null)
			m_tableModel.dispose();
	}
}

