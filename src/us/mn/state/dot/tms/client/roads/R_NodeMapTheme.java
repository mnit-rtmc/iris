/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Theme for roadway nodes on map
 *
 * @author Douglas Lau
 */
public class R_NodeMapTheme extends ProxyTheme<R_Node> {

	/** Create a new roadway node map theme */
	public R_NodeMapTheme(R_NodeManager m) {
		super(m, new R_NodeMarker());
	}
}
