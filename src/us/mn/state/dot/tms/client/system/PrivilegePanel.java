/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.system;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for displaying a table of privileges.
 *
 * @author Douglas Lau
 */
public class PrivilegePanel extends ProxyTablePanel<Privilege> {

	/** All sonar type names (matches iris.sonar_types DB table) */
	static public final String[] ALL_TYPES = {
		"action_plan",
		"alarm",
		"beacon",
		"beacon_action",
		"cabinet",
		"cabinet_style",
		"camera",
		"camera_preset",
		"capability",
		"comm_link",
		"connection",
		"controller",
		"day_matcher",
		"day_plan",
		"detector",
		"dms",
		"dms_action",
		"dms_sign_group",
		"encoder_type",
		"font",
		"gate_arm",
		"gate_arm_array",
		"geo_loc",
		"glyph",
		"graphic",
		"inc_advice",
		"inc_descriptor",
		"incident",
		"incident_detail",
		"inc_locator",
		"lane_action",
		"lane_marking",
		"lane_use_multi",
		"lcs",
		"lcs_array",
		"lcs_indication",
		"map_extent",
		"meter_action",
		"modem",
		"monitor_style",
		"plan_phase",
		"play_list",
		"privilege",
		"quick_message",
		"ramp_meter",
		"r_node",
		"road",
		"role",
		"sign_group",
		"sign_message",
		"sign_text",
		"station",
		"system_attribute",
		"tag_reader",
		"time_action",
		"toll_zone",
		"user",
		"video_monitor",
		"weather_sensor",
		"word"
	};

	/** Privilege type name label */
	private final ILabel type_lbl = new ILabel("privilege.type");

	/** Privilege type name combo box */
	private final JComboBox<String> type_cbx =
		new JComboBox<String>();

	/** Privilege type model */
	private final DefaultComboBoxModel<String> type_mdl;

	/** Create a new privilege panel */
	public PrivilegePanel(PrivilegeModel mdl) {
		super(mdl);
		type_mdl = new DefaultComboBoxModel<String>(ALL_TYPES);
	}

	/** Initialise the panel */
	@Override
	public void initialize() {
		super.initialize();
		type_cbx.setEnabled(false);
		type_cbx.setModel(type_mdl);
		type_lbl.setEnabled(false);
	}

	/** Add create/delete widgets to the button panel */
	@Override
	protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(type_lbl);
		vg.addComponent(type_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(type_cbx);
		vg.addComponent(type_cbx);
		hg.addGap(UI.hgap);
		super.addCreateDeleteWidgets(hg, vg);
	}

	/** Update the button panel */
	@Override
	public void updateButtonPanel() {
		type_lbl.setEnabled(model.canAdd());
		type_cbx.setEnabled(model.canAdd());
		super.updateButtonPanel();
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		PrivilegeModel mdl = getPrivilegeModel();
		if (mdl != null) {
			Object tn = type_mdl.getSelectedItem();
			if (tn instanceof String)
				mdl.createObject((String) tn);
		}
	}

	/** Get the privilege model */
	private PrivilegeModel getPrivilegeModel() {
		ProxyTableModel<Privilege> mdl = model;
		return (mdl instanceof PrivilegeModel)
		     ? (PrivilegeModel) mdl
		     : null;
	}
}
