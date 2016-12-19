/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.WebBrowser;

/**
 * An action to launch a web browser for work request logging.
 *
 * @author Douglas Lau
 */
public class WorkRequestAction<T extends SonarObject> extends IAction {

	/** Get the work request URL */
	static private String getUrl() {
		return SystemAttrEnum.WORK_REQUEST_URL.getString();
	}

	/** Check if the work request url system attribute is configured */
	static public boolean isConfigured() {
		String url = getUrl();
		return url != null && url.trim().length() > 0;
	}

	/** Sonar proxy */
	private final T proxy;

	/** Proxy location */
	private final GeoLoc loc;

	/** Create a new work request action */
	public WorkRequestAction(T p, GeoLoc l) {
		super("device.work.request", p.getName());
		proxy = p;
		loc = l;
		setEnabled(p != null);
	}

	/** Actually perform the action */
	@Override
	protected void doActionPerformed(ActionEvent e) throws IOException {
		if (isConfigured()) {
			StringBuilder sb = new StringBuilder();
			sb.append("http://");
			sb.append(getUrl());
			sb.append("/georilla/forms/tams/");
			sb.append("?asset-type=720");
			sb.append("&inventory-id-field=its_device_name");
			sb.append("&inventory-id=");
			sb.append(proxy.getName());
			if (loc != null) {
				Double lon = loc.getLon();
				Double lat = loc.getLat();
				if (lon != null && lon != 0) {
					sb.append("&longitude=");
					sb.append(lon);
				}
				if (lat != null && lat != 0) {
					sb.append("&latitude=");
					sb.append(lat);
				}
			}
			WebBrowser.open(new URL(sb.toString()));
		}
	}
}
