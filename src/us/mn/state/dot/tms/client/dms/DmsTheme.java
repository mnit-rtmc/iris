/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DevicePurpose;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.ItemStyle;
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

	/** Tolling symbol */
	static private final Symbol TOLLING_SYMBOL = new VectorSymbol(
		new TollingMarker());

	/** Create a new DMS theme */
	public DmsTheme(DMSManager man) {
		super(man, new DmsMarker());
		// NOTE: the ordering of styles controls which color is used
		//       to render the sign icon on the map
		addStyle(ItemStyle.AVAILABLE, ProxyTheme.COLOR_AVAILABLE);
		addStyle(ItemStyle.DEPLOYED, ProxyTheme.COLOR_DEPLOYED);
		addStyle(ItemStyle.SCHEDULED, ProxyTheme.COLOR_SCHEDULED);
		addStyle(ItemStyle.MAINTENANCE, ProxyTheme.COLOR_UNAVAILABLE);
		addStyle(ItemStyle.PURPOSE, ProxyTheme.COLOR_PURPOSE);
		addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		addStyle(ItemStyle.ALL);
	}

	/** Set the map scale */
	@Override
	public void setScale(float scale) {
		super.setScale(scale);
		float p = scale * UI.scaled(1) * 0.45f;
		TOLLING_SYMBOL.setScale(p);
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
			int p = proxy.getPurpose();
			DevicePurpose dp = DevicePurpose.fromOrdinal(p);
			switch (dp) {
				case TOLLING: return TOLLING_SYMBOL;
				default: return null;
			}
		}
		return null;
	}
}
