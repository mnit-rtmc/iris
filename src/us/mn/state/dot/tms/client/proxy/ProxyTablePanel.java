/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A proxy table panel is a UI for displaying a table of proxy objects.
 *
 * @author Douglas Lau
 */
public class ProxyTablePanel<T extends SonarObject> extends IPanel {

	/** Table model */
	private final ProxyTableModel2<T> model;

	/** Proxy table */
	private final ZTable table;

	/** Action to display the proxy properties */
	private final IAction show_props = new IAction("device.properties") {
		protected void doActionPerformed(ActionEvent e) {
			T proxy = getSelectedProxy();
			if (proxy != null)
				model.showPropertiesForm(proxy);
		}
	};

	/** Button to display the proxy properties */
	private final JButton prop_btn = new JButton(show_props);

	/** Text field to add a proxy */
	private final JTextField add_txt = new JTextField(16);

	/** Action to create a proxy */
	private final IAction add_proxy = new IAction("device.create") {
		protected void doActionPerformed(ActionEvent e) {
			String name = add_txt.getText();
			add_txt.setText("");
			model.createObject(name);
		}
	};

	/** Action to delete the selected proxy */
	private final IAction del_proxy = new IAction("device.delete") {
		protected void doActionPerformed(ActionEvent e) {
			T proxy = getSelectedProxy();
			if (proxy != null)
				proxy.destroy();
		}
	};

	/** Mouse listener for table */
	private final MouseAdapter mouser = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2)
				prop_btn.doClick();
		}
	};

	/** Create a new proxy table panel */
	public ProxyTablePanel(ProxyTableModel2<T> m) {
		model = m;
		table = createTable();
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setModel(model);
		table.setRowHeight(UI.scaled(getRowHeight()));
		table.setVisibleRowCount(model.getVisibleRowCount());
		show_props.setEnabled(false);
		add_txt.setEnabled(false);
		add_proxy.setEnabled(false);
		del_proxy.setEnabled(false);
	}

	/** Initialise the panel */
	public void initialize() {
		model.initialize();
		createJobs();
		addTable();
		add(buildButtonBox(), Stretch.FULL);
	}

	/** Dispose of the panel */
	public void dispose() {
		removeAll();
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
		if (model.hasProperties())
			table.addMouseListener(mouser);
		if (model.canCreate()) {
			add_proxy.setEnabled(model.canAdd());
			add_txt.setAction(add_proxy);
		}
	}

	/** Build the button box */
	private Box buildButtonBox() {
		Box box = Box.createHorizontalBox();
		if (model.hasProperties()) {
			box.add(prop_btn);
			box.add(Box.createHorizontalStrut(UI.hgap));
		}
		if (model.canCreate()) {
			box.add(add_txt);
			box.add(Box.createHorizontalStrut(UI.hgap));
			box.add(new JButton(add_proxy));
			box.add(Box.createHorizontalStrut(UI.hgap));
		}
		if (model.canDelete()) {
			box.add(Box.createHorizontalGlue());
			box.add(Box.createHorizontalStrut(UI.hgap));
			box.add(new JButton(del_proxy));
		}
		return box;
	}

	/** Add the table to the panel */
	protected void addTable() {
		add(table, Stretch.FULL);
	}

	/** Get the row height */
	protected int getRowHeight() {
		return 18;
	}

	/** Get the currently selected proxy */
	protected T getSelectedProxy() {
		return model.getRowProxy(table.getSelectedRow());
	}

	/** Select a new proxy */
	protected void selectProxy() {
		T proxy = getSelectedProxy();
		show_props.setEnabled(proxy != null);
		del_proxy.setEnabled(model.canRemove(proxy));
	}
}
