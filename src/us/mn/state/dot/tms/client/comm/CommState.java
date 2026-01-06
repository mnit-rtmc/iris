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
 * Comm state enumeration.
 *
 * @author Douglas Lau
 */
public enum CommState {
	INACTIVE   (ProxyTheme.COLOR_INACTIVE),
	OK         (ProxyTheme.COLOR_AVAILABLE),
	OFFLINE    (ProxyTheme.COLOR_OFFLINE),
	POLLINATOR (ProxyTheme.COLOR_POLLINATOR);

	/** Color for the comm state */
	public final Color color;

	/** Create a comm state */
	private CommState(Color c) {
		color = c;
	}

	/** Get values with null as first */
	static public CommState[] values_with_null() {
		return new CommState[] {
			null,
			INACTIVE,
			OK,
			OFFLINE,
			POLLINATOR,
		};
	}
}
