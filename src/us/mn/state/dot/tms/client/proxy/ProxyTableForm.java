/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
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

	/** Action to display the proxy properties */
	private final IAction show_props = new IAction("device.properties") {
		protected void doActionPerformed(ActionEvent e) {
			T proxy = getSelectedProxy();
			if(proxy != null)
				model.showPropertiesForm(proxy);
		}
	};

	/** Button to display the proxy properties */
	private final JButton prop_btn = new JButton(show_props);

	/** Action to delete the selected proxy */
	private final IAction del_obj = new IAction("device.delete") {
		protected void doActionPerformed(ActionEvent e) {
			T proxy = getSelectedProxy();
			if(proxy != null)
				proxy.destroy();
		}
	};

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
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectProxy();
			}
		});
		if(model.hasProperties()) {
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2)
						prop_btn.doClick();
				}
			});
		}
	}

	/** Create the panel for the form */
	private JPanel createPanel() {
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setModel(model);
		table.setRowHeight(UI.scaled(getRowHeight()));
		table.setVisibleRowCount(getVisibleRowCount());
		show_props.setEnabled(false);
		del_obj.setEnabled(false);
		IPanel p = new IPanel();
		addTable(p);
		p.add(buildButtonBox(), Stretch.RIGHT);
		return p;
	}

	/** Build the button box */
	private Box buildButtonBox() {
		Box box = Box.createHorizontalBox();
		if(model.hasProperties())
			box.add(prop_btn);
		if(model.hasDelete()) {
			box.add(Box.createHorizontalStrut(UI.hgap));
			box.add(new JButton(del_obj));
		}
		return box;
	}

	/** Add the table to the panel */
	protected void addTable(IPanel p) {
		p.add(table, Stretch.FULL);
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
		show_props.setEnabled(proxy != null);
		del_obj.setEnabled(model.canRemove(proxy));
	}
}
