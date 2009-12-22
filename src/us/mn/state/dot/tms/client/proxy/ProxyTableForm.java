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
package us.mn.state.dot.tms.client.proxy;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A proxy table form is a simple widget for displaying a table of proxy
 * objects.
 *
 * @author Douglas Lau
 */
public class ProxyTableForm<T extends SonarObject> extends AbstractForm {

	/** Table model */
	protected final ProxyTableModel<T> model;

	/** Proxy table */
	protected final ZTable table;

	/** Button to display the proxy properties */
	protected final JButton prop_btn = new JButton("Properties");

	/** Button to delete the selected proxy */
	protected final JButton del_btn = new JButton("Delete");

	/** Create a new proxy table form */
	public ProxyTableForm(String t, ProxyTableModel<T> m) {
		super(t);
		model = m;
		table = createTable();
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		model.initialize();
		createJobs();
		add(createPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create the table */
	protected ZTable createTable() {
		return new ZTable();
	}

	/** Create Gui jobs */
	protected void createJobs() {
		ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				selectProxy();
			}
		};
		new ActionJob(this, del_btn) {
			public void perform() throws Exception {
				T proxy = getSelectedProxy();
				if(proxy != null)
					proxy.destroy();
			}
		};
		if(model.hasProperties()) {
			new ActionJob(this, prop_btn) {
				public void perform() throws Exception {
					T proxy = getSelectedProxy();
					if(proxy != null)
						model.showPropertiesForm(proxy);
				}
			};
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2)
						prop_btn.doClick();
				}
			});
		}
	}

	/** Create the panel for the form */
	protected JPanel createPanel() {
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setModel(model);
		table.setRowHeight(getRowHeight());
		table.setVisibleRowCount(getVisibleRowCount());
		FormPanel panel = new FormPanel(true);
		panel.addRow(table);
		if(model.hasProperties())
			panel.add(prop_btn);
		panel.addRow(del_btn);
		prop_btn.setEnabled(false);
		del_btn.setEnabled(false);
		return panel;
	}

	/** Get the row height */
	protected int getRowHeight() {
		return 18;
	}

	/** Get the visible row count */
	protected int getVisibleRowCount() {
		return 16;
	}

	/** Get the currently selected proxy */
	protected T getSelectedProxy() {
		return model.getProxy(table.getSelectedRow());
	}

	/** Select a new proxy */
	protected void selectProxy() {
		T proxy = getSelectedProxy();
		prop_btn.setEnabled(proxy != null);
		del_btn.setEnabled(model.canRemove(proxy));
	}
}
