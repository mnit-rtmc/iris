/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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
import javax.swing.Action;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.widget.WebBrowser;

/**
 * An action to launch a web browser for TESLA error logging.
 *
 * @author Douglas Lau
 */
public class TeslaAction<T extends SonarObject> extends ProxyAction {

	/** Check if the TESLA system attribute is configured */
	static public boolean isConfigured() {
		return SystemAttrEnum.TESLA_HOST.getString() != null;
	}

	/** Create a new TESLA action */
	public TeslaAction(T p) {
		super("device.log", p);
		Object v = getValue(Action.NAME);
		if(v instanceof String) {
			String s = (String)v;
			putValue(Action.NAME, s + " " + p.getName());
		}
	}

	/** Actually perform the action */
	protected void doActionPerformed(ActionEvent e) throws IOException {
		String host = SystemAttrEnum.TESLA_HOST.getString();
		if(host != null && host.trim().length() > 0) {
			WebBrowser.open(new URL("http://" + host +
				"/tesla/user/Ticket.do?" +
				"command=form" +
				"&deviceType=" + proxy.getTypeName() +
				"&deviceName=" + proxy.getName()));
		}
	}
}
