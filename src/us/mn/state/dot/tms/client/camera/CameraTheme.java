/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
 * Copyright (C) 2024  SRF Consulting Group
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
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.ToolTipBuilder;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.units.Temperature;

/**
 * Camera theme provides styles for cameras.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class CameraTheme extends ProxyTheme<Camera> {

	/** Color for video loss camera style */
	static private final Color COLOR_VIDEO_LOSS = new Color(128, 64, 255);

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

	/** Video loss style */
	static public final Style VIDEO_LOSS = new Style(
		ItemStyle.VIDEO_LOSS.toString(), OUTLINE, COLOR_VIDEO_LOSS);

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
		addStyle(VIDEO_LOSS);
		addStyle(ACTIVE);
		addStyle(ALL);
	}

	/** Get tooltip text for the given map object */
	@Override
	public String getTip(MapObject o) {
		Camera p = manager.findProxy(o);
		if (p == null)
			return null;
		String desc = manager.getDescription(p);
		String ptzInfo = CameraHelper.getPtzInfo(p);
		if (ptzInfo == null)
			return desc;
		ToolTipBuilder ttb = new ToolTipBuilder();
		ttb.addLine(desc);
		ttb.setLast();
		ttb.addLine(ptzInfo);
		return ttb.get();
	}

}
