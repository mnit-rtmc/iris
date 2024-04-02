/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.ToolTipBuilder;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.map.Symbol;
import us.mn.state.dot.tms.client.map.VectorSymbol;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Theme for DMS objects on the map.
 *
 * @author Douglas Lau
 */
public class DmsTheme extends ProxyTheme<DMS> {

	/** Style for drawing purpose markers */
	static private final Style PURPOSE_STYLE = new Style("Purpose", null,
		Color.BLACK);

	/** Wayfinding symbol */
	static private final Symbol WAYFINDING_SYMBOL = new VectorSymbol(
		new WayfindingMarker());

	/** Tolling symbol */
	static private final Symbol TOLLING_SYMBOL = new VectorSymbol(
		new TollingMarker());

	/** Parking symbol */
	static private final Symbol PARKING_SYMBOL = new VectorSymbol(
		new ParkingMarker());

	/** Create a new DMS theme */
	public DmsTheme(DMSManager man) {
		super(man, new DmsMarker());
		// NOTE: the ordering of styles controls which color is used
		//       to render the sign icon on the map
		addStyle(ItemStyle.AVAILABLE, COLOR_AVAILABLE);
		addStyle(ItemStyle.DEPLOYED, COLOR_DEPLOYED);
		addStyle(ItemStyle.SCHEDULED, COLOR_SCHEDULED);
		addStyle(ItemStyle.EXTERNAL, COLOR_EXTERNAL);
		addStyle(ItemStyle.MAINTENANCE, COLOR_UNAVAILABLE);
		addStyle(ItemStyle.FAILED, COLOR_FAILED);
		addStyle(ItemStyle.PURPOSE, COLOR_PURPOSE);
		addStyle(ItemStyle.ALL);
	}

	/** Set the map scale */
	@Override
	public void setScale(float scale) {
		super.setScale(scale);
		float p = scale * UI.scaled(1) * 0.45f;
		WAYFINDING_SYMBOL.setScale(p);
		TOLLING_SYMBOL.setScale(p);
		PARKING_SYMBOL.setScale(p);
	}

	/** Draw the specified map object */
	@Override
	public void draw(Graphics2D g, MapObject mo) {
		AffineTransform t = g.getTransform();
		super.draw(g, mo);
		Symbol ps = getPurposeSymbol(mo);
		if (ps != null) {
			g.setTransform(t); // reset transform
			ps.draw(g, mo, PURPOSE_STYLE);
		}
	}

	/** Get the purpose symbol */
	private Symbol getPurposeSymbol(MapObject mo) {
		DMS proxy = manager.findProxy(mo);
		if (proxy != null) {
			if (DMSHelper.hasHashtag(proxy, "#Wayfinding"))
				return WAYFINDING_SYMBOL;
			if (DMSHelper.hasHashtag(proxy, "#Tolling"))
				return TOLLING_SYMBOL;
			if (DMSHelper.hasHashtag(proxy, "#Parking"))
				return PARKING_SYMBOL;
		}
		return null;
	}

	/** Get tooltip text for the given map object.
	 * @return String or null for none */
	@Override
	public String getTip(MapObject o) {
		DMS p = manager.findProxy(o);
		if(p == null)
			return null;
		ToolTipBuilder ttb = new ToolTipBuilder();
		ttb.addLine(manager.getDescription(p));
		if (SystemAttrEnum.DMS_MESSAGE_TOOLTIP_ENABLE.getBoolean()) {
			// TODO move tip text to i18n
			SignMessage m = p.getMsgCurrent();
			ttb.addLine("Message", m.getMulti());
			ttb.setLast();
			ttb.addLine("Owner", m.getMsgOwner());
		}
		return ttb.get();
	}
}
