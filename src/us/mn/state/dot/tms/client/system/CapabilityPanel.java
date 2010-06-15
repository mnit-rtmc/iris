/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.TmsForm;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing capabilities and privileges.
 *
 * @author Douglas Lau
 */
public class CapabilityPanel extends JPanel {

	/** Table model for capabilities */
	protected final CapabilityModel cap_model;

	/** Table model for privileges */
	protected PrivilegeModel p_model;

	/** Table to hold the capability list */
	protected final ZTable cap_table = new ZTable();

	/** Table to hold the privilege list */
	protected final ZTable p_table = new ZTable();

	/** Button to delete the selected capability */
	protected final JButton del_capability =
		new JButton("Delete Capability");

	/** Button to delete the selected privilege */
	protected final JButton del_privilege = new JButton("Delete Privilege");

	/** User session */
	protected final Session session;

	/** Create a new capability panel */
	public CapabilityPanel(Session s) {
		super(new GridBagLayout());
		session = s;
		cap_model = new CapabilityModel(s);
		p_model = new PrivilegeModel(session, null);
		setBorder(TmsForm.BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = 4;
		bag.insets.right = 4;
		bag.insets.top = 4;
		bag.insets.bottom = 4;
		cap_table.setModel(cap_model);
		cap_table.setAutoCreateColumnsFromModel(false);
		cap_table.setColumnModel(cap_model.createColumnModel());
		cap_table.setVisibleRowCount(16);
		JScrollPane pane = new JScrollPane(cap_table);
		add(pane, bag);
		p_table.setModel(p_model);
		p_table.setAutoCreateColumnsFromModel(false);
		p_table.setColumnModel(p_model.createColumnModel());
		p_table.setVisibleRowCount(16);
		pane = new JScrollPane(p_table);
		add(pane, bag);
		del_capability.setEnabled(false);
		bag.gridx = 0;
		bag.gridy = 1;
		add(del_capability, bag);
		del_privilege.setEnabled(false);
		bag.gridx = 1;
		add(del_privilege, bag);
	}

	/** Initializze the panel */
	protected void initialize() {
		cap_model.initialize();
		p_model.initialize();
		final ListSelectionModel s = cap_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting())
					selectCapability();
			}
		});
		new ActionJob(this, del_capability) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					cap_model.deleteRow(row);
			}
		};
		final ListSelectionModel sp = p_table.getSelectionModel();
		sp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sp.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting())
					selectPrivilege();
			}
		});
		new ActionJob(this, del_privilege) {
			public void perform() throws Exception {
				int row = sp.getMinSelectionIndex();
				if(row >= 0)
					p_model.deleteRow(row);
			}
		};
	}

	/** Dispose of the panel */
	protected void dispose() {
		cap_model.dispose();
		p_model.dispose();
	}

	/** Change the selected capability */
	protected void selectCapability() {
		ListSelectionModel s = cap_table.getSelectionModel();
		Capability c = cap_model.getProxy(s.getMinSelectionIndex());
		del_capability.setEnabled(cap_model.canRemove(c));
		p_table.clearSelection();
		final PrivilegeModel pm = p_model;
		p_model = new PrivilegeModel(session, c);
		p_model.initialize();
		p_table.setModel(p_model);
		pm.dispose();
	}

	/** Select a privilege */
	protected void selectPrivilege() {
		Privilege p = getSelectedPrivilege();
		del_privilege.setEnabled(p_model.canRemove(p));
	}

	/** Get the selected privilege */
	protected Privilege getSelectedPrivilege() {
		final PrivilegeModel pm = p_model;	// Avoid race
		ListSelectionModel s = p_table.getSelectionModel();
		return pm.getProxy(s.getMinSelectionIndex());
	}
}
