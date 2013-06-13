/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2013  Minnesota Department of Transportation
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

import java.awt.BorderLayout;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;

/**
 * Provides a GUI for the camera tab on the operator interface for IRIS.
 *
 * @author Douglas Lau
 */
public class CameraTab extends MapTab {

	/** Camera viewer */
	private final CameraViewer viewer;

	/** Create a new camera tab for the IRIS client */
	public CameraTab(Session session, CameraManager manager) {
		super("camera");
		viewer = new CameraViewer(session, manager);
		add(viewer, BorderLayout.NORTH);
		add(manager.createStyleSummary(), BorderLayout.CENTER);
	}

	/** Perform any clean up necessary */
	@Override public void dispose() {
		super.dispose();
		viewer.dispose();
	}
}
