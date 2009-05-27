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
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.IrisTab;
import us.mn.state.dot.tms.client.SonarState;

/**
 * Provides a GUI for the camera tab on the operator interface for IRIS.
 *
 * @author Douglas Lau
 */
public class CameraTab extends IrisTab {

	/** Tab panel */
	protected final JPanel tabPanel;

	/** Message logger */
	protected final Logger logger;

	/** Create a new camera tab for the IRIS client */
	public CameraTab(CameraManager manager, Properties p, Logger l,
		SonarState st, User user)
	{
		super("Camera", "Camera summary");
		tabPanel = createSideBar(manager, p, st, user);
		logger = l;
	}

	/** Create the side bar component */
	protected JPanel createSideBar(CameraManager manager, Properties props,
		SonarState st, User user)
	{
		JPanel p = new JPanel(new BorderLayout());
		p.add(new CameraViewer(manager, props, logger, st, user),
			BorderLayout.NORTH);
		p.add(manager.createStyleSummary(), BorderLayout.CENTER);
		return p;
	}

	/** Get the tab number */
	public int getNumber() {
		return 4;
	}

	/** Get the tab panel */
	public JPanel getTabPanel() {
		return tabPanel;
	}

	/** Get the main panel */
	public JPanel getMainPanel() {
		return null;
	}
}
