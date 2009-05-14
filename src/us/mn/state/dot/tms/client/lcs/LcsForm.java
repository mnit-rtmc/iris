/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.toast.ZTable;

/**
 * A form for displaying a table of LCS arrays.
 *
 * @author Douglas Lau
 */
public class LcsForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "LCS Arrays";

	/** Table model for LCS arrays */
	protected LCSArrayModel model;

	/** Table to hold the LCS array list */
	protected final ZTable table = new ZTable();

	/** Button to display the properties */
	protected final JButton propertiesBtn = new JButton("Properties");

	/** Button to delete the selected proxy */
	protected final JButton deleteBtn = new JButton("Delete");

	/** TMS connection */
	protected final TmsConnection connection;

	/** Type cache */
	protected final TypeCache<LCSArray> cache;

	/** Create a new LCS form */
	public LcsForm(TmsConnection tc, TypeCache<LCSArray> c) {
		super(TITLE);
		connection = tc;
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model = new LCSArrayModel(cache);
		add(createLCSArrayPanel());
		table.setVisibleRowCount(16);
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create LCS array panel */
	protected JPanel createLCSArrayPanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectProxy();
			}
		};
		new ActionJob(this, propertiesBtn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0) {
					LCSArray proxy = model.getProxy(row);
					if(proxy != null)
						showPropertiesForm(proxy);
				}
			}
		};
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					propertiesBtn.doClick();
			}
		});
		new ActionJob(this, deleteBtn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		panel.addRow(table);
		panel.add(propertiesBtn);
		panel.addRow(deleteBtn);
		propertiesBtn.setEnabled(false);
		deleteBtn.setEnabled(false);
		return panel;
	}

	/** Change the selected proxy */
	protected void selectProxy() {
		int row = table.getSelectedRow();
		propertiesBtn.setEnabled(row >= 0 && !model.isLastRow(row));
		deleteBtn.setEnabled(row >= 0 && !model.isLastRow(row));
	}

	/** Show the properties form */
	protected void showPropertiesForm(LCSArray proxy) {
		SmartDesktop desktop = connection.getDesktop();
		desktop.show(new LCSArrayProperties(connection, proxy));
	}
}
