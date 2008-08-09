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
package us.mn.state.dot.tms.client.toast;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * A form for displaying and editing comm links
 *
 * @author Douglas Lau
 */
public class CommLinkForm extends AbstractForm {

	/** Comm link table row height */
	static protected final int ROW_HEIGHT = 24;

	/** Frame title */
	static protected final String TITLE = "Comm Links";

	/** Tabbed pane */
	protected final JTabbedPane tab = new JTabbedPane();

	/** Table model for comm links */
	protected CommLinkModel model;

	/** Table to hold the comm link list */
	protected final JTable table = new JTable();

	/** Table model for controllers */
	protected ControllerModel cmodel;

	/** Table to hold controllers */
	protected final JTable ctable = new JTable();

	/** Comm link status */
	protected final JLabel link_status = new JLabel();

	/** Button to delete the selected comm link */
	protected final JButton del_button = new JButton("Delete Comm Link");

	/** Table to hold failed controllers */
	protected final JTable ftable = new JTable();

	/** Failed controller table model */
	protected FailedControllerModel fmodel;

	/** Button to go to a failed controller */
	protected final JButton go_button = new JButton("Go");

	/** Comm Link type cache */
	protected final TypeCache<CommLink> cache;

	/** Controller type cache */
	protected final TypeCache<Controller> ccache;

	/** Create a new comm link form */
	public CommLinkForm(TypeCache<CommLink> c, TypeCache<Controller> cc) {
		super(TITLE);
		cache = c;
		ccache = cc;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model = new CommLinkModel(cache);
		fmodel = new FailedControllerModel(ccache);
		tab.add("All Links", createCommLinkPanel());
		tab.add("Failed Controllers", createFailedControllerPanel());
		add(tab);

		// FIXME: JPanels don't like to layout properly
		Dimension d = getPreferredSize();
		setMinimumSize(new Dimension(d.width, d.height + 30));
		setPreferredSize(new Dimension(d.width, d.height + 30));
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
		fmodel.dispose();
		if(cmodel != null)
			cmodel.dispose();
	}

	/** Create comm link panel */
	protected JPanel createCommLinkPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = HGAP;
		bag.insets.right = HGAP;
		bag.insets.top = VGAP;
		bag.insets.bottom = VGAP;
		bag.gridwidth = 3;
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectCommLink();
			}
		};
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(ROW_HEIGHT);
		table.setPreferredScrollableViewportSize(new Dimension(
			table.getPreferredSize().width, ROW_HEIGHT * 8));
		JScrollPane pane = new JScrollPane(table);
		panel.add(pane, bag);
		bag.gridwidth = 1;
		bag.gridx = 0;
		bag.gridy = 1;
		bag.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("Seleccted Comm Link:"), bag);
		bag.gridx = 1;
		panel.add(link_status, bag);
		bag.gridx = 2;
		bag.anchor = GridBagConstraints.EAST;
		del_button.setEnabled(false);
		panel.add(del_button, bag);
		new ActionJob(this, del_button) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 3;
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.BOTH;
		final ListSelectionModel cs = ctable.getSelectionModel();
		cs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, cs) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectController();
			}
		};
		ctable.setAutoCreateColumnsFromModel(false);
		ctable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		ctable.setRowHeight(ROW_HEIGHT);
		pane = new JScrollPane(ctable);
		panel.add(pane, bag);
		return panel;
	}

	/** Change the selected comm link */
	protected void selectCommLink() {
		int row = table.getSelectedRow();
		CommLink cl = model.getProxy(row);
		if(cl != null)
			link_status.setText(cl.getStatus());
		else
			link_status.setText("");
		del_button.setEnabled(row >= 0 && !model.isLastRow(row));
		ControllerModel old_model = cmodel;
		cmodel = new ControllerModel(ccache, cl);
		ctable.setModel(cmodel);
		ctable.setColumnModel(cmodel.createColumnModel());
		if(old_model != null)
			old_model.dispose();
	}

	/** Change the selected controller */
	protected void selectController() {
		// FIXME
	}

	/** Create the failed controller panel */
	protected JPanel createFailedControllerPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BORDER);
		final ListSelectionModel s = ftable.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectFailedController();
			}
		};
		ftable.setModel(fmodel);
		ftable.setAutoCreateColumnsFromModel(false);
		ftable.setColumnModel(fmodel.createColumnModel());
		ftable.setRowHeight(ROW_HEIGHT);
		JScrollPane pane = new JScrollPane(ftable);
		panel.add(pane);
		new ActionJob(this, go_button) {
			public void perform() throws Exception {
				goFailedController();
			}
		};
		ftable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() > 1)
					go_button.doClick();
			}
		});
		panel.add(go_button);
		return panel;
	}

	/** Change the selected failed controller */
	protected void selectFailedController() {
		// FIXME
	}

	/** Go to the failed controller (on the main tab) */
	protected void goFailedController() {
		int row = ftable.getSelectedRow();
		Controller c = fmodel.getProxy(row);
		if(c != null)
			goController(c);
	}

	/** Go to the specified controller (on the main tab) */
	protected void goController(Controller c) {
		CommLink l = c.getCommLink();
		int row = model.getRow(l);
		if(row >= 0) {
			ListSelectionModel s = table.getSelectionModel();
			s.setSelectionInterval(row, row);
			table.scrollRectToVisible(
				table.getCellRect(row, 0, true));
			tab.setSelectedIndex(0);
		}
	}
}
