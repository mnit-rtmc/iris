/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Permission;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for displaying a table of permissions (for one role).
 *
 * @author Douglas Lau
 */
public class PermissionPanel extends ProxyTablePanel<Permission> {

	/** Base resource types */
	static public final String[] BASE_RES = {
		"action_plan",
		"alert_config",
		"beacon",
		"camera",
		"controller",
		"detector",
		"dms",
		"gate_arm",
		"incident",
		"lcs",
		"parking_area",
		"permission",
		"ramp_meter",
		"system_attribute",
		"toll_zone",
		"video_monitor",
		"weather_sensor"
	};

	/** Base resource label */
	private final ILabel resource_lbl =
		new ILabel("permission.base.resource");

	/** Base resource combo box */
	private final JComboBox<String> resource_cbx =
		new JComboBox<String>();

	/** Base resource model */
	private final DefaultComboBoxModel<String> resource_mdl;

	/** Create a new permission panel */
	public PermissionPanel(PermissionModel mdl) {
		super(mdl);
		resource_mdl = new DefaultComboBoxModel<String>(BASE_RES);
	}

	/** Initialise the panel */
	@Override
	public void initialize() {
		super.initialize();
		resource_cbx.setEnabled(false);
		resource_cbx.setModel(resource_mdl);
		resource_lbl.setEnabled(false);
	}

	/** Add create/delete widgets to the button panel */
	@Override
	protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(resource_lbl);
		vg.addComponent(resource_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(resource_cbx);
		vg.addComponent(resource_cbx);
		hg.addGap(UI.hgap);
		super.addCreateDeleteWidgets(hg, vg);
	}

	/** Update the button panel */
	@Override
	public void updateButtonPanel() {
		resource_lbl.setEnabled(model.canAdd());
		resource_cbx.setEnabled(model.canAdd());
		super.updateButtonPanel();
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		if (model instanceof PermissionModel) {
			PermissionModel mdl = (PermissionModel) model;
			Object res = resource_mdl.getSelectedItem();
			if (res instanceof String)
				mdl.createObject((String) res);
		}
	}
}
