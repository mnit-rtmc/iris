/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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

	/** Set flag to connect direct to camera */
	void setDirect(boolean d);

	/** Get flag to connect directo to camera */
	boolean getDirect();

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
}
