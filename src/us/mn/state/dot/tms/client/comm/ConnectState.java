/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2026  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.Color;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Connect state enumeration.
 *
 * @author Douglas Lau
 */
public enum ConnectState {
	INACTIVE   (ProxyTheme.COLOR_INACTIVE),
	OK         (ProxyTheme.COLOR_AVAILABLE),
	OFFLINE    (ProxyTheme.COLOR_OFFLINE),
	POLLINATOR (ProxyTheme.COLOR_POLLINATOR);

	/** Color for the connect state */
	public final Color color;

	/** Create a connect state */
	private ConnectState(Color c) {
		color = c;
	}

	/** Get values with null as first */
	static public ConnectState[] values_with_null() {
		return new ConnectState[] {
			null,
			INACTIVE,
			OK,
			OFFLINE,
			POLLINATOR,
		};
	}
}
