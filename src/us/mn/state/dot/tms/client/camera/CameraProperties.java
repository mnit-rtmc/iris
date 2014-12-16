/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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

import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a camera.
 *
 * @author Douglas Lau
 */
public class CameraProperties extends SonarObjectForm<Camera> {

	/** Location panel */
	private final PropLocation location_pnl;

	/** Setup panel */
	private final PropSetup setup_pnl;

	/** Preset panel */
	private final PropPreset preset_pnl;

	/** Create a new camera properties form */
	public CameraProperties(Session s, Camera c) {
		super(I18N.get("camera") + ": ", s, c);
		location_pnl = new PropLocation(s, c);
		setup_pnl = new PropSetup(s, c);
		preset_pnl = new PropPreset(s, c);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<Camera> getTypeCache() {
		return state.getCamCache().getCameras();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		location_pnl.initialize();
		setup_pnl.initialize();
		preset_pnl.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), location_pnl);
		tab.add(I18N.get("device.setup"), setup_pnl);
		tab.add(I18N.get("camera.preset"), preset_pnl);
		add(tab);
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		location_pnl.dispose();
		setup_pnl.dispose();
		preset_pnl.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		location_pnl.updateEditMode();
		setup_pnl.updateEditMode();
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		location_pnl.updateAttribute(a);
		setup_pnl.updateAttribute(a);
	}
}
