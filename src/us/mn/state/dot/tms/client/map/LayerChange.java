/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

/**
 * A LayerChange is an enum of changes to a map layer.
 *
 * @author Douglas Lau
 */
public enum LayerChange {
	model,		// map model change
	extent,		// extent change (pan / zoom)
	geometry,	// geometry change (new tile, etc)
	status,		// status change (device status)
	visibility,	// visibility change (layer hidden / shown)
	theme,		// theme change
	selection;	// selection change
}
