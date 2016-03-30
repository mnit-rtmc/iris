/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.text.NumberFormat;
import java.util.HashMap;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.MultiParser;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.TollZoneHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.MultiBuilder;

/**
 * Tolling Formatter
 *
 * @author Douglas Lau
 */
public class TollingFormatter {

	/** Get minimum tolling price */
	static private float min_price() {
		return SystemAttrEnum.TOLL_MIN_PRICE.getFloat();
	}

	/** Get maximum tolling price */
	static private float max_price() {
		return SystemAttrEnum.TOLL_MAX_PRICE.getFloat();
	}

	/** Limit tolling price */
	static private float limit_price(float p) {
		return Math.min(Math.max(p, min_price()), max_price());
	}

	/** Tolling formatter label */
	private final String lbl;

	/** Origin location */
	private final GeoLoc origin;

	/** Number formatter */
	private final NumberFormat formatter = NumberFormat.getNumberInstance();

	/** Create a tolling formatter */
	public TollingFormatter(String l, GeoLoc o) {
		lbl = l;
		origin = o;
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(2);
	}

	/** Replace tolling tags in a MULTI string.
	 * @param multi MULTI string to parse.
	 * @return MULTI string with tolling tags replaced. */
	public String replaceTolling(String multi) {
		MultiCallback cb = new MultiCallback();
		MultiParser.parse(multi, cb);
		if (cb.valid)
			return cb.toString();
		else
			return null;
	}

	/** Calculate prices for tolling tags.
	 * @param multi MULTI string to parse.
	 * @return Hash map of toll zones and prices. */
	public HashMap<String, Float> calculatePrices(String multi) {
		MultiCallback cb = new MultiCallback();
		MultiParser.parse(multi, cb);
		if (cb.valid && cb.prices.size() > 0)
			return cb.prices;
		else
			return null;
	}

	/** MultiBuilder for replacing tolling tags */
	private class MultiCallback extends MultiBuilder {

		protected boolean valid = true;
		protected HashMap<String, Float> prices =
			new HashMap<String, Float>();

		/** Add a tolling message */
		@Override
		public void addTolling(String mode, String[] zones) {
			if (zones.length < 1) {
				valid = false;
				return;
			}
			String z = zones[zones.length - 1];
			if ("p".equals(mode)) {
				Float p = calculatePrice(zones);
				if (p != null) {
					prices.put(z, p);
					addSpan(formatter.format(p));
				} else
					valid = false;
			} else if ("o".equals(mode) || "c".equals(mode))
				prices.put(z, 0f);
			else
				valid = false;
		}
	}

	/** Calculate the price for tolling zones */
	private Float calculatePrice(String[] zones) {
		if (zones.length < 1)
			return null;
		float price = 0;
		for (String zid: zones) {
			Float p = lookupPrice(zid);
			if (p != null)
				price += p;
			else
				return null;
		}
		return limit_price(price);
	}

	/** Lookup the current price for a toll zone */
	private Float lookupPrice(String zid) {
		TollZone z = TollZoneHelper.lookup(zid);
		if (z instanceof TollZoneImpl) {
			TollZoneImpl tz = (TollZoneImpl) z;
			return tz.getPrice(lbl, origin);
		} else
			return null;
	}
}
