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
package us.mn.state.dot.tms.client.marking;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A form for displaying and editing lane markings
 *
 * @author Douglas Lau
 */
public class LaneMarkingForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Lane Markings";

	/** Table model for lane markings */
	protected final LaneMarkingModel m_model;

	/** Table to hold the lane markings */
	protected final ZTable m_table = new ZTable();

	/** Button to display the lane marking properties */
	protected final JButton properties = new JButton("Properties");

	/** Button to delete the selected lane marking */
	protected final JButton del_btn = new JButton("Delete");

	/** User session */
	protected final Session session;

	/** Create a new lane marking form */
	public LaneMarkingForm(Session s, TypeCache<LaneMarking> c) {
		super(TITLE);
		session = s;
		m_model = new LaneMarkingModel(c,
			session.getSonarState().getNamespace(),
			session.getUser());
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		m_model.initialize();
		add(createLaneMarkingPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		m_model.dispose();
	}

	/** Create lane marking panel */
	protected JPanel createLaneMarkingPanel() {
		final ListSelectionModel s = m_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectLaneMarking();
			}
		};
		m_table.setModel(m_model);
		m_table.setAutoCreateColumnsFromModel(false);
		m_table.setColumnModel(LaneMarkingModel.createColumnModel());
		m_table.setVisibleRowCount(12);
		new ActionJob(this, properties) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0) {
					LaneMarking lm = m_model.getProxy(row);
					if(lm != null)
						showPropertiesForm(lm);
				}
			}
		};
		m_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					properties.doClick();
			}
		});
		new ActionJob(this, del_btn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					m_model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		panel.addRow(m_table);
		panel.add(properties);
		panel.addRow(del_btn);
		properties.setEnabled(false);
		del_btn.setEnabled(false);
		return panel;
	}

	/** Change the selected lane marking */
	protected void selectLaneMarking() {
		LaneMarking lm = m_model.getProxy(m_table.getSelectedRow());
		properties.setEnabled(lm != null);
		del_btn.setEnabled(m_model.canRemove(lm));
	}

	/** Show the properties form for a lane marking */
	protected void showPropertiesForm(LaneMarking lm) throws Exception {
		session.getDesktop().show(
			new LaneMarkingProperties(session, lm));
	}
}
