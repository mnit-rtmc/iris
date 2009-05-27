/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

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
	protected final ZTable m_table = new ZTable();

	/** Button to delete the selected video monitor */
	protected final JButton del_monitor = new JButton("Delete");

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
		new ActionJob(this, del_monitor) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					m_model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		panel.addRow(m_table);
		panel.addRow(del_monitor);
		del_monitor.setEnabled(false);
		return panel;
	}

	/** Change the selected monitor */
	protected void selectMonitor() {
		int row = m_table.getSelectedRow();
		del_monitor.setEnabled(row >= 0 && !m_model.isLastRow(row));
	}
}
