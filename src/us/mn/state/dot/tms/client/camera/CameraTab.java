/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.SonarState;

/**
 * Provides a GUI for the camera tab on the operator interface for IRIS.
 *
 * @author Douglas Lau
 */
public class CameraTab extends MapTab {

	/** Message logger */
	protected final Logger logger;

	/** Create a new camera tab for the IRIS client */
	public CameraTab(CameraManager manager, List<LayerState> lstates,
		Properties props, Logger l, SonarState st, User user)
	{
		super("Camera", "Camera summary");
		logger = l;
		for(LayerState ls: lstates) {
			map_model.addLayer(ls);
			String name = ls.getLayer().getName();
			if(name.equals(manager.getProxyType()))
				map_model.setHomeLayer(ls);
		}
		add(new CameraViewer(manager, props, logger, st, user),
			BorderLayout.NORTH);
		add(manager.createStyleSummary(), BorderLayout.CENTER);
	}

	/** Get the tab number */
	public int getNumber() {
		return 4;
	}
}
