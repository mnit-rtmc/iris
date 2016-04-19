/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import java.awt.Color;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Camera theme provides styles for cameras.
 *
 * @author Douglas Lau
 */
public class CameraTheme extends ProxyTheme<Camera> {

	/** Color for active camera style */
	static private final Color COLOR_ACTIVE = new Color(0, 192, 255);

	/** Unpublished style */
	static public final Style UNPUBLISHED = new Style(
		ItemStyle.UNPUBLISHED.toString(), OUTLINE, COLOR_UNAVAILABLE);

	/** Inactive style */
	static public final Style INACTIVE = new Style(
		ItemStyle.INACTIVE.toString(), OUTLINE_INACTIVE,COLOR_INACTIVE);

	/** Playlist style */
	static public final Style PLAYLIST = new Style(
		ItemStyle.PLAYLIST.toString(), OUTLINE, COLOR_DEPLOYED);

	/** Active style */
	static public final Style ACTIVE = new Style(
		ItemStyle.ACTIVE.toString(), OUTLINE, COLOR_ACTIVE);

	/** All style */
	static public final Style ALL = new Style(ItemStyle.ALL.toString(),
		OUTLINE, null);

	/** Create a new camera theme */
	public CameraTheme(CameraManager man) {
		super(man, new CameraMarker());
		addStyle(UNPUBLISHED);
		addStyle(INACTIVE);
		addStyle(PLAYLIST);
		addStyle(ACTIVE);
		addStyle(ALL);
	}
}
