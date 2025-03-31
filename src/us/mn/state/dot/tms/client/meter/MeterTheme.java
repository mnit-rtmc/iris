/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Meter theme provides styles for ramp meters.
 *
 * @author Douglas Lau
 */
public class MeterTheme extends ProxyTheme<RampMeter> {

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
			c, false);
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

	/** Locked fault style */
	static private final Style LCK_FAULT = locked(
		ItemStyle.FAULT, COLOR_FAULT);

	/** Create a new meter theme */
	public MeterTheme(MeterManager man) {
		super(man, new MeterMarker());
		addStyle(ItemStyle.AVAILABLE, COLOR_AVAILABLE);
		addStyle(ItemStyle.QUEUE_FULL, COLOR_WARNING);
		addStyle(ItemStyle.QUEUE_EXISTS, COLOR_DEPLOYED);
		addStyle(ItemStyle.METERING, COLOR_METERING);
		addStyle(ItemStyle.LOCKED, ProxyTheme.OUTLINE_LOCKED, null);
		addStyle(ItemStyle.FAULT, ProxyTheme.COLOR_FAULT);
		addStyle(ItemStyle.OFFLINE, ProxyTheme.COLOR_OFFLINE);
		addStyle(ItemStyle.ALL);
		addStyle(LCK_AVAILABLE);
		addStyle(LCK_QUEUE_FULL);
		addStyle(LCK_QUEUE_EXISTS);
		addStyle(LCK_METERING);
		addStyle(LCK_FAULT);
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
			if (manager.checkStyle(ItemStyle.FAULT, rm))
				return LCK_FAULT;
		}
		return super.getStyle(rm);
	}
}
