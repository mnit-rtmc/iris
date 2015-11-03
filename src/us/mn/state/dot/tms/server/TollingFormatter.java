/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MultiParser;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.TollZoneHelper;

/**
 * Tolling Formatter
 *
 * @author Douglas Lau
 */
public class TollingFormatter {

	/** Number formatter */
	private final NumberFormat formatter = NumberFormat.getNumberInstance();

	/** Create a tolling formatter */
	public TollingFormatter() {
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

	/** MultiString for replacing tolling tags */
	private class MultiCallback extends MultiString {

		protected boolean valid = true;

		/** Add a tolling message */
		@Override
		public void addTolling(String mode, String[] zones) {
			if ("p".equals(mode)) {
				String p = formatPrice(zones);
				if (p != null)
					addSpan(p);
				else
					valid = false;
			}
		}
	}

	/** Format the price for tolling zones */
	private String formatPrice(String[] zones) {
		float price = 0;
		for (String zid: zones) {
			Float p = lookupPrice(zid);
			if (p != null)
				price += p;
			else
				return null;
		}
		// FIXME: limit within toll_min_price / toll_max_price
		return formatter.format(price);
	}

	/** Lookup the current price for a toll zone */
	private Float lookupPrice(String zid) {
		TollZone z = TollZoneHelper.lookup(zid);
		if (z instanceof TollZoneImpl) {
			TollZoneImpl tz = (TollZoneImpl) z;
			return tz.getPrice();
		} else
			return null;
	}
}
