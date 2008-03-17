/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.switcher;

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
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * A form for displaying and editing video monitors
 *
 * @author Douglas Lau
 */
public class VideoMonitorForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Video Monitors";

	/** Table model for video monitors */
	protected VideoMonitorModel m_model;

	/** Table to hold the video monitor list */
	protected final JTable m_table = new JTable();

	/** Button to delete the selected video monitor */
	protected final JButton del_monitor = new JButton("Delete Monitor");

	/** Video monitor type cache */
	protected final TypeCache<VideoMonitor> cache;

	/** Create a new video monitor form */
	public VideoMonitorForm(TypeCache<VideoMonitor> c) {
		super(TITLE);
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		m_model = new VideoMonitorModel(cache);
		add(createMonitorPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		m_model.dispose();
	}

	/** Create monitor panel */
	protected JPanel createMonitorPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = HGAP;
		bag.insets.right = HGAP;
		bag.insets.top = VGAP;
		bag.insets.bottom = VGAP;
		final ListSelectionModel s = m_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectMonitor();
			}
		};
		m_table.setModel(m_model);
		m_table.setAutoCreateColumnsFromModel(false);
		m_table.setColumnModel(m_model.createColumnModel());
		JScrollPane pane = new JScrollPane(m_table);
		panel.add(pane, bag);
		del_monitor.setEnabled(false);
		panel.add(del_monitor, bag);
		new ActionJob(this, del_monitor) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					m_model.deleteRow(row);
			}
		};
		return panel;
	}

	/** Change the selected monitor */
	protected void selectMonitor() {
		int row = m_table.getSelectedRow();
		del_monitor.setEnabled(row >= 0 && !m_model.isLastRow(row));
	}
}
