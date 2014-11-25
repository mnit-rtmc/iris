/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * Camera properties preset panel.
 *
 * @author Douglas Lau
 */
public class PropPreset extends IPanel {

	/** Preset model */
	private final PresetModel preset_mdl;

	/** Create a new camera properties preset panel */
	public PropPreset(Session s, Camera c) {
		preset_mdl = new PresetModel(s, c);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		preset_mdl.initialize();
		ZTable table = new ZTable();
		table.setAutoCreateColumnsFromModel(false);
		table.setModel(preset_mdl);
		table.setColumnModel(preset_mdl.createColumnModel());
		table.setVisibleRowCount(CameraPreset.MAX_PRESET + 1);
		add(table, Stretch.FULL);
	}

	/** Dispose of the preset panel */
	@Override
	public void dispose() {
		preset_mdl.dispose();
		super.dispose();
	}
}
