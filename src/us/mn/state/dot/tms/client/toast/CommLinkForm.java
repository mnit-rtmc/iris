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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * A form for displaying and editing comm links
 *
 * @author Douglas Lau
 */
public class CommLinkForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Comm Links";

	/** Table model for comm links */
	protected CommLinkModel model;

	/** Table to hold the comm link list */
	protected final JTable table = new JTable();

	/** Button to delete the selected comm link */
	protected final JButton del_button = new JButton("Delete Comm Link");

	/** Comm Link type cache */
	protected final TypeCache<CommLink> cache;

	/** Create a new comm link form */
	public CommLinkForm(TypeCache<CommLink> c) {
		super(TITLE);
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model = new CommLinkModel(cache);
		add(createCommLinkPanel());
		Dimension d = new Dimension(table.getPreferredSize().width,
			table.getPreferredScrollableViewportSize().height / 3);
		table.setPreferredScrollableViewportSize(d);
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create comm link panel */
	protected JPanel createCommLinkPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = HGAP;
		bag.insets.right = HGAP;
		bag.insets.top = VGAP;
		bag.insets.bottom = VGAP;
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
		JScrollPane pane = new JScrollPane(table);
		panel.add(pane, bag);
		del_button.setEnabled(false);
		panel.add(del_button, bag);
		new ActionJob(this, del_button) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		return panel;
	}

	/** Change the selected comm link */
	protected void selectCommLink() {
		int row = table.getSelectedRow();
		del_button.setEnabled(row >= 0 && !model.isLastRow(row));
	}
}
