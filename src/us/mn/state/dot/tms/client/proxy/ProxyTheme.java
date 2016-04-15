/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
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
	static public final Outline OUTLINE_INACTIVE = Outline.createSolid(
		OUTLINE_COLOR, 1);

	/** Outline for stroking locked traffic devices */
	static public final Outline OUTLINE_LOCKED = Outline.createSolid(
		Color.RED, 2);

	/** Color to display inactive devices */
	static public final Color COLOR_INACTIVE = new Color(0, 0, 0, 32);

	/** Color to display "no controller" devices */
	static public final Color COLOR_NO_CONTROLLER =
		new Color(255, 255, 255, 64);

	/** Color to display failed devices */
	static public final Color COLOR_FAILED = Color.GRAY;

	/** Color to display unavailable devices */
	static public final Color COLOR_UNAVAILABLE = Color.BLACK;

	/** Color to display available devices */
	static public final Color COLOR_AVAILABLE = new Color(96, 96, 255);

	/** Color to display deployed devices */
	static public final Color COLOR_DEPLOYED = Color.YELLOW;

	/** Color to display scheduled devices */
	static public final Color COLOR_SCHEDULED = new Color(240, 128, 0);

	/** Size of legend icons */
	static protected final int lsize = UI.scaled(22);

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Create a new SONAR proxy theme */
	public ProxyTheme(ProxyManager<T> m, Marker mkr) {
		super(I18N.get(m.getSonarType()), new VectorSymbol(mkr, lsize),
			new Style(ItemStyle.ALL.toString(), null));
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
	public Style getStyle(MapObject o) {
		T proxy = manager.findProxy(o);
		if (proxy != null)
			return getStyle(proxy);
		else
			return def_style;
	}

	/** Get an appropriate style for the given proxy object */
	public Style getStyle(T proxy) {
		for (Style st: getStyles()) {
			ItemStyle is = ItemStyle.lookupStyle(st.toString());
			if (is != null && manager.checkStyle(is, proxy))
				return st;
		}
		return def_style;
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
