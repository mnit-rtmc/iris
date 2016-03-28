/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.IncDescriptor;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Panel for incident descriptors.
 *
 * @author Douglas Lau
 */
public class IncDescriptorPanel extends ProxyTablePanel<IncDescriptor> {

	/** Create the incident type combo box */
	static public JComboBox<EventType> createIncTypeCombo() {
		return new JComboBox<EventType>(new EventType[] {
			EventType.INCIDENT_CRASH,
			EventType.INCIDENT_STALL,
			EventType.INCIDENT_ROADWORK,
			EventType.INCIDENT_HAZARD
		});
	}

	/** Sign group label */
	private final ILabel sign_group_lbl = new ILabel("dms.group");

	/** Sign group field */
	private final JTextField sign_group_txt = new JTextField(16);

	/** Incident type label */
	private final ILabel event_type_lbl = new ILabel("incident.type");

	/** Incident type combo box */
	private final JComboBox<EventType> event_type_cbx =createIncTypeCombo();

	/** Incident descriptor model */
	private final IncDescriptorTableModel model;

	/** Create a new incident descriptor panel */
	public IncDescriptorPanel(IncDescriptorTableModel m) {
		super(m);
		model = m;
		event_type_cbx.setRenderer(new EventTypeRenderer());
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		sign_group_lbl.setEnabled(false);
		sign_group_txt.setEnabled(false);
		event_type_lbl.setEnabled(false);
		event_type_cbx.setEnabled(false);
	}

	/** Add create/delete widgets to the button panel */
	@Override
	protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(sign_group_lbl);
		vg.addComponent(sign_group_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(sign_group_txt);
		vg.addComponent(sign_group_txt);
		hg.addGap(UI.hgap);
		hg.addComponent(event_type_lbl);
		vg.addComponent(event_type_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(event_type_cbx);
		vg.addComponent(event_type_cbx);
		hg.addGap(UI.hgap);
		super.addCreateDeleteWidgets(hg, vg);
	}

	/** Update the button panel */
	@Override
	public void updateButtonPanel() {
		sign_group_lbl.setEnabled(model.canAdd());
		sign_group_txt.setEnabled(model.canAdd());
		event_type_lbl.setEnabled(model.canAdd());
		event_type_cbx.setEnabled(model.canAdd());
		super.updateButtonPanel();
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		String sgt = sign_group_txt.getText().trim();
		SignGroup sg = SignGroupHelper.lookup(sgt);
		EventType et = (EventType) event_type_cbx.getSelectedItem();
		if (sg != null)
			model.create(sg, et);
		sign_group_txt.setText("");
	}
}
