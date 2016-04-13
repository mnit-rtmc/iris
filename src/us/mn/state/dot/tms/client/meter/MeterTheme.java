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
package us.mn.state.dot.tms.client.meter;

import java.awt.Color;
import java.util.HashMap;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.map.VectorSymbol;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Meter theme provides styles for ramp meters.
 *
 * @author Douglas Lau
 */
public class MeterTheme extends ProxyTheme<RampMeter> {

	/** Ramp meter map object marker */
	static private final MeterMarker MARKER = new MeterMarker();

	/** Color to display available meters */
	static private final Color COLOR_AVAILABLE = new Color(64, 64, 192);

	/** Color to display warning meters */
	static private final Color COLOR_WARNING = new Color(192, 96, 0);

	/** Color to display deployed meters */
	static private final Color COLOR_DEPLOYED = new Color(192, 192, 0);

	/** Color to display metering meters */
	static public final Color COLOR_METERING = new Color(0, 192, 0);

	/** Create a locked style */
	static private Style locked(ItemStyle sty, Color c) {
		return new Style(ItemStyle.LOCKED + " " + sty, OUTLINE_LOCKED,
			c);
	}

	/** Locked available style */
	static private final Style LCK_AVAILABLE = locked(ItemStyle.AVAILABLE,
		COLOR_AVAILABLE);

	/** Locked queue full style */
	static private final Style LCK_QUEUE_FULL = locked(ItemStyle.QUEUE_FULL,
		COLOR_WARNING);

	/** Locked queue exists style */
	static private final Style LCK_QUEUE_EXISTS = locked(
		ItemStyle.QUEUE_EXISTS, COLOR_DEPLOYED);

	/** Locked metering style */
	static private final Style LCK_METERING = locked(ItemStyle.METERING,
		COLOR_METERING);

	/** Locked maintenance style */
	static private final Style LCK_MAINTENANCE = locked(
		ItemStyle.MAINTENANCE, COLOR_UNAVAILABLE);

	/** Extra symbols */
	private final HashMap<String, Symbol> extra_syms =
		new HashMap<String, Symbol>();

	/** Create a new meter theme */
	public MeterTheme(MeterManager man) {
		super(man, MARKER);
		addStyle(ItemStyle.AVAILABLE, COLOR_AVAILABLE);
		addStyle(ItemStyle.QUEUE_FULL, COLOR_WARNING);
		addStyle(ItemStyle.QUEUE_EXISTS, COLOR_DEPLOYED);
		addStyle(ItemStyle.METERING, COLOR_METERING);
		addStyle(ItemStyle.LOCKED, null, ProxyTheme.OUTLINE_LOCKED);
		addStyle(ItemStyle.MAINTENANCE, ProxyTheme.COLOR_UNAVAILABLE);
		addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		addStyle(ItemStyle.INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		addStyle(ItemStyle.ALL);
		storeSymbol(LCK_AVAILABLE);
		storeSymbol(LCK_QUEUE_FULL);
		storeSymbol(LCK_QUEUE_EXISTS);
		storeSymbol(LCK_METERING);
		storeSymbol(LCK_MAINTENANCE);
	}

	/** Store one symbol */
	private void storeSymbol(Style sty) {
		extra_syms.put(sty.toString(), createSymbol(sty));
	}

	/** Create a symbol */
	private Symbol createSymbol(Style sty) {
		return new VectorSymbol(sty, MARKER, UI.scaled(22));
	}

	/** Get an appropriate style */
	@Override
	public Style getStyle(RampMeter rm) {
		if (manager.checkStyle(ItemStyle.LOCKED, rm)) {
			if (manager.checkStyle(ItemStyle.AVAILABLE, rm))
				return LCK_AVAILABLE;
			if (manager.checkStyle(ItemStyle.QUEUE_FULL, rm))
				return LCK_QUEUE_FULL;
			if (manager.checkStyle(ItemStyle.QUEUE_EXISTS, rm))
				return LCK_QUEUE_EXISTS;
			if (manager.checkStyle(ItemStyle.METERING, rm))
				return LCK_METERING;
			if (manager.checkStyle(ItemStyle.MAINTENANCE, rm))
				return LCK_MAINTENANCE;
		}
		return super.getStyle(rm);
	}

	/** Get a symbol by label */
	@Override
	public Symbol getSymbol(String label) {
		Symbol sym = extra_syms.get(label);
		return (sym != null) ? sym : super.getSymbol(label);
	}
}
