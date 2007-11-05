/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.device.TrafficDeviceTheme;

/**
 * Theme for warning signs
 * 
 * @author Douglas Lau
 */
public class WarningSignTheme extends TrafficDeviceTheme {

	/** Create a new warning sign theme */
	public WarningSignTheme() {
		super(WarningSignProxy.PROXY_TYPE, new WarningSignMarker());
		addStyle(WarningSign.STATUS_AVAILABLE, "Available",
			COLOR_AVAILABLE);
		addStyle(WarningSign.STATUS_DEPLOYED, "Deployed",
			COLOR_DEPLOYED);
		addStyle(WarningSign.STATUS_FAILED, "Failed", COLOR_FAILED);
	}

	/** Get tooltip text for the given map object */
	public String getTip(MapObject o) {
		String t = super.getTip(o);
		WarningSignProxy p = (WarningSignProxy)o;
		return t + '\n' + p.getText();
	}
}
