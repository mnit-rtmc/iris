/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.User;

/**
 * A video monitor device.
 *
 * @author Douglas Lau
 */
public interface VideoMonitor extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "video_monitor";

	/** Set the monitor number */
	void setMonNum(int mn);

	/** Get the monitor number */
	int getMonNum();

	/** Set flag to restrict publishing camera images */
	void setRestricted(boolean r);

	/** Get flag to restrict publishing camera images */
	boolean getRestricted();

	/** Set the monitor style */
	void setMonitorStyle(MonitorStyle ms);

	/** Get the monitor style */
	MonitorStyle getMonitorStyle();

	/** Set the camera displayed on the monitor */
	void setCamera(Camera c);

	/** Get the camera displayed on the monitor */
	Camera getCamera();

	/** Set the play list.
	 * This will start the given play list from the beginning. */
	void setPlayList(PlayList pl);
}
