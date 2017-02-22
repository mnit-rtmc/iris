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
package us.mn.state.dot.tms.client.camera;

import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing video monitors
 *
 * @author Douglas Lau
 */
public class VideoMonitorForm extends ProxyTableForm<VideoMonitor> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(VideoMonitor.SONAR_TYPE);
	}

	/** Create a new video monitor form */
	public VideoMonitorForm(Session s) {
		super(I18N.get("video.monitors"), new VideoMonitorPanel(s));
	}
}
