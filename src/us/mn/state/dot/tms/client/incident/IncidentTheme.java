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
package us.mn.state.dot.tms.client.incident;

import java.awt.Color;
import java.util.HashMap;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.map.VectorSymbol;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Theme for incident objects on the map.
 *
 * @author Douglas Lau
 */
public class IncidentTheme extends ProxyTheme<Incident> {

	/** Incident marker */
	static private final IncidentMarker MARKER = new IncidentMarker();

	/** Outline for unconfirmed styles */
	static private final Outline UN_OUTLINE = Outline.createSolid(
		Color.WHITE, 1);

	/** Color for CRASH incidents */
	static private final Color CRASH_COLOR = new Color(255, 128, 128);

	/** Color for STALL incidents */
	static private final Color STALL_COLOR = new Color(255, 128, 255);

	/** Color for ROADWORK incidents */
	static private final Color ROADWORK_COLOR = new Color(255, 208, 128);

	/** Color for HAZARD incidents */
	static private final Color HAZARD_COLOR = new Color(255, 255, 128);

	/** Color for CLEARED incidents */
	static private final Color CLEARED_COLOR = new Color(128, 255, 128);

	/** Create a cleared style */
	static private Style cleared(ItemStyle sty) {
		return new Style(ItemStyle.CLEARED + " " + sty, OUTLINE,
			CLEARED_COLOR);
	}

	/** Cleared crash style */
	static private final Style CLR_CRASH = cleared(ItemStyle.CRASH);

	/** Cleared stall style */
	static private final Style CLR_STALL = cleared(ItemStyle.STALL);

	/** Cleared roadwork style */
	static private final Style CLR_ROADWORK = cleared(ItemStyle.ROADWORK);

	/** Cleared hazard style */
	static private final Style CLR_HAZARD = cleared(ItemStyle.HAZARD);

	/** Create an unconfirmed color */
	static private Color unconfirmedColor(Color c) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
	}

	/** Create an unconfirmed style */
	static private Style unconfirmed(ItemStyle sty, Color c) {
		return new Style(ItemStyle.UNCONFIRMED + " " + sty, UN_OUTLINE,
			unconfirmedColor(c));
	}

	/** Unconfirmed crash style */
	static private final Style UN_CRASH = unconfirmed(ItemStyle.CRASH,
		CRASH_COLOR);

	/** Unconfirmed stall style */
	static private final Style UN_STALL = unconfirmed(ItemStyle.STALL,
		STALL_COLOR);

	/** Unconfirmed roadwork style */
	static private final Style UN_ROADWORK = unconfirmed(ItemStyle.ROADWORK,
		ROADWORK_COLOR);

	/** Unconfirmed hazard style */
	static private final Style UN_HAZARD = unconfirmed(ItemStyle.HAZARD,
		HAZARD_COLOR);

	/** Extra symbols */
	private final HashMap<String, Symbol> extra_syms =
		new HashMap<String, Symbol>();

	/** Create a new incident theme */
	public IncidentTheme(IncidentManager man) {
		super(man, MARKER);
		addStyle(ItemStyle.CRASH, CRASH_COLOR);
		addStyle(ItemStyle.STALL, STALL_COLOR);
		addStyle(ItemStyle.ROADWORK, ROADWORK_COLOR);
		addStyle(ItemStyle.HAZARD, HAZARD_COLOR);
		addStyle(ItemStyle.CLEARED, CLEARED_COLOR);
		addStyle(ItemStyle.UNCONFIRMED, unconfirmedColor(Color.WHITE),
			IncidentTheme.UN_OUTLINE);
		addStyle(ItemStyle.ALL);
		storeSymbol(CLR_CRASH);
		storeSymbol(CLR_STALL);
		storeSymbol(CLR_ROADWORK);
		storeSymbol(CLR_HAZARD);
		storeSymbol(UN_CRASH);
		storeSymbol(UN_STALL);
		storeSymbol(UN_ROADWORK);
		storeSymbol(UN_HAZARD);
	}

	/** Store one symbol */
	private void storeSymbol(Style sty) {
		extra_syms.put(sty.toString(), createSymbol(sty));
	}

	/** Create a symbol */
	private Symbol createSymbol(Style sty) {
		return new VectorSymbol(sty, MARKER, UI.scaled(22));
	}

	/** Get an appropriate style for the given map object */
	@Override
	public Style getStyle(MapObject mo) {
		if (mo instanceof IncidentGeoLoc) {
			IncidentGeoLoc loc = (IncidentGeoLoc) mo;
			return getStyle(loc.getIncident());
		}
		return dstyle;
	}

	/** Get an appropriate style for the given proxy object */
	@Override
	public Style getStyle(Incident inc) {
		if (manager.checkStyle(ItemStyle.CLEARED, inc)) {
			if (manager.checkStyle(ItemStyle.CRASH, inc))
				return CLR_CRASH;
			if (manager.checkStyle(ItemStyle.STALL, inc))
				return CLR_STALL;
			if (manager.checkStyle(ItemStyle.ROADWORK, inc))
				return CLR_ROADWORK;
			if (manager.checkStyle(ItemStyle.HAZARD, inc))
				return CLR_HAZARD;
		}
		if (manager.checkStyle(ItemStyle.UNCONFIRMED, inc)) {
			if (manager.checkStyle(ItemStyle.CRASH, inc))
				return UN_CRASH;
			if (manager.checkStyle(ItemStyle.STALL, inc))
				return UN_STALL;
			if (manager.checkStyle(ItemStyle.ROADWORK, inc))
				return UN_ROADWORK;
			if (manager.checkStyle(ItemStyle.HAZARD, inc))
				return UN_HAZARD;
		}
		return super.getStyle(inc);
	}

	/** Get a symbol by label */
	@Override
	public Symbol getSymbol(String label) {
		Symbol sym = extra_syms.get(label);
		return (sym != null) ? sym : super.getSymbol(label);
	}
}
