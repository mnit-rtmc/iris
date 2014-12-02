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
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ITable;

/**
 * A proxy table panel is a UI for displaying a table of proxy objects.
 *
 * @author Douglas Lau
 */
public class ProxyTablePanel<T extends SonarObject> extends IPanel {

	/** Table model */
	private ProxyTableModel2<T> model;

	/** Proxy table */
	private final ITable table;

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
		table = createTable(m);
		show_props.setEnabled(false);
		add_txt.setEnabled(false);
		add_proxy.setEnabled(false);
		del_proxy.setEnabled(false);
	}

	/** Initialise the panel */
	@Override
	public void initialize() {
		super.initialize();
		model.initialize();
		createJobs();
		add(table, Stretch.FULL);
		add(buildButtonPanel(), Stretch.FULL);
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		model.dispose();
		super.dispose();
	}

	/** Set the model */
	public void setModel(ProxyTableModel2<T> m) {
		ProxyTableModel2<T> om = model;
		m.initialize();
		model = m;
		table.setModel(m);
		om.dispose();
	}

	/** Create the table */
	private ITable createTable(ProxyTableModel2<T> m) {
		ITable t = new ITable(m);
		t.setAutoCreateColumnsFromModel(false);
		t.setColumnModel(m.createColumnModel());
		t.setModel(m);
		t.setRowHeight(UI.scaled(m.getRowHeight()));
		t.setVisibleRowCount(m.getVisibleRowCount());
		return t;
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
		if (model.hasCreateDelete()) {
			add_proxy.setEnabled(model.canAdd());
			add_txt.setAction(add_proxy);
		}
	}

	/** Build the button panel */
	private JPanel buildButtonPanel() {
		JPanel pnl = new JPanel();
		GroupLayout gl = new GroupLayout(pnl);
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup vg = gl.createBaselineGroup(false,
			false);
		if (model.hasProperties()) {
			hg.addComponent(prop_btn);
			vg.addComponent(prop_btn);
			hg.addGap(UI.hgap);
		}
		if (model.hasCreateDelete()) {
			if (model.hasName()) {
				hg.addComponent(add_txt);
				vg.addComponent(add_txt);
				hg.addGap(UI.hgap);
			}
			JButton add_btn = new JButton(add_proxy);
			hg.addComponent(add_btn);
			vg.addComponent(add_btn);
			hg.addGap(2 * UI.hgap);
			JButton del_btn = new JButton(del_proxy);
			hg.addComponent(del_btn);
			vg.addComponent(del_btn);
		}
		gl.setHorizontalGroup(hg);
		gl.setVerticalGroup(vg);
		pnl.setLayout(gl);
		return pnl;
	}

	/** Get the currently selected proxy */
	public T getSelectedProxy() {
		return model.getRowProxy(table.getSelectedRow());
	}

	/** Select a new proxy */
	protected void selectProxy() {
		T proxy = getSelectedProxy();
		show_props.setEnabled(proxy != null);
		del_proxy.setEnabled(canRemove(proxy));
	}

	/** Select a new proxy */
	public void selectProxy(T proxy) {
		int row = model.getIndex(proxy);
		if (row >= 0) {
			ListSelectionModel s = table.getSelectionModel();
			s.setSelectionInterval(row, row);
			table.scrollRectToVisible(
				table.getCellRect(row, 0, true));
		}
	}

	/** Check if a proxy can be removed */
	public boolean canRemove(T proxy) {
		return model.canRemove(proxy);
	}
}
