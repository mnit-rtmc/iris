/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2024  Minnesota Department of Transportation
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

import java.awt.Color;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.map.Marker;
import us.mn.state.dot.tms.client.map.Outline;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.map.Theme;
import us.mn.state.dot.tms.client.map.VectorSymbol;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Theme for SONAR proxy objects on map
 *
 * @author Douglas Lau
 */
public class ProxyTheme<T extends SonarObject> extends Theme {

	/** Outline color */
	static protected final Color OUTLINE_COLOR = new Color(0, 0, 0, 128);

	/** Outline for stroking traffic devices */
	static public final Outline OUTLINE = Outline.createSolid(
		OUTLINE_COLOR, 1);

	/** Outline for stroking inactive traffic devices */
	static public final Outline OUTLINE_INACTIVE = Outline.createDashed(
		OUTLINE_COLOR, 1);

	/** Outline for stroking locked traffic devices */
	static public final Outline OUTLINE_LOCKED = Outline.createSolid(
		Color.RED, 2);

	/** Color to display inactive devices */
	static public final Color COLOR_INACTIVE = new Color(0, 0, 0, 32);

	/** Color to display "no controller" devices */
	static public final Color COLOR_NO_CONTROLLER =
		new Color(255, 255, 255, 64);

	/** Color to display offline devices */
	static public final Color COLOR_OFFLINE = Color.GRAY;

	/** Color to display fault devices */
	static public final Color COLOR_FAULT = Color.BLACK;

	/** Color to display available devices */
	static public final Color COLOR_AVAILABLE = new Color(96, 96, 255);

	/** Color to display deployed devices */
	static public final Color COLOR_DEPLOYED = Color.YELLOW;

	/** Color to display scheduled devices */
	static public final Color COLOR_SCHEDULED = new Color(240, 128, 0);

	/** Color to display external controlled devices */
	static public final Color COLOR_EXTERNAL = new Color(176, 240, 0);

	/** Color for dedicated purpose devices */
	static public final Color COLOR_PURPOSE = new Color(192, 16, 192);

	/** Color to display moving gate devices */
	static public final Color COLOR_MOVING = new Color(240, 128, 0);

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Create a new SONAR proxy theme */
	public ProxyTheme(ProxyManager<T> m, Marker mkr) {
		super(I18N.get(m.getSonarType()), new VectorSymbol(mkr),
			new Style(ItemStyle.ALL.toString(), OUTLINE, null));
		manager = m;
	}

	/** Add a style to the theme.
	 * @param is Item style.
	 * @param o Outline style.
	 * @param f Fill color. */
	public void addStyle(ItemStyle is, Outline o, Color f) {
		addStyle(new Style(is.toString(), o, f));
	}

	/** Add a style to the theme */
	public void addStyle(ItemStyle is, Color f) {
		addStyle(is, OUTLINE, f);
	}

	/** Add a style to the theme */
	public void addStyle(ItemStyle is) {
		addStyle(is, null);
	}

	/** Get an appropriate style for the given map object */
	@Override
	public Style getStyle(MapObject mo) {
		T proxy = manager.findProxy(mo);
		return (proxy != null) ? getStyle(proxy) : getDefaultStyle();
	}

	/** Get an appropriate style for the given proxy object */
	public Style getStyle(T proxy) {
		if (proxy != null) {
			for (Style sty: getStyles()) {
				ItemStyle is = ItemStyle.lookupStyle(
					sty.toString());
				if (is != null && manager.checkStyle(is, proxy))
					return sty;
			}
		}
		return getDefaultStyle();
	}
	
	/** Get the Style for the the given ItemStyle */
	public Style getStyle(ItemStyle is) {
		if (is != null) {
			for (Style sty: getStyles()) {
				ItemStyle s = ItemStyle.lookupStyle(sty.toString());
				if (is.equals(s))
					return sty;
			}
		}
		return getDefaultStyle();
	}

	/** Get tooltip text for the given map object */
	@Override
	public String getTip(MapObject o) {
		T proxy = manager.findProxy(o);
		if (proxy != null)
			return manager.getDescription(proxy);
		else
			return null;
	}
}
