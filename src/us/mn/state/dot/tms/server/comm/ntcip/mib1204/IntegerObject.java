/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.utils.Json;

/**
 * Integer object.
 *
 * @author Douglas Lau
 */
public class IntegerObject {

	/** Json key name */
	protected final String key;

	/** Integer MIB node */
	public final ASN1Integer node;

	/** Create an integer object */
	public IntegerObject(String k, ASN1Integer n) {
		key = k;
		node = n;
		// `missing` value is always 1 greater than max
		node.setInteger(maxValue() + 1);
	}

	/** Get the minimum valid value */
	protected int minValue() {
		return 0;
	}

	/** Get the maximum valid value */
	protected int maxValue() {
		return 65534;
	}

	/** Get object value.
	 * @return Value or null if missing */
	public Integer getValue() {
		int p = node.getInteger();
		return (p >= minValue() && p <= maxValue())
		      ? Integer.valueOf(p)
		      : null;
	}

	/** Get JSON representation */
	public String toJson() {
		return Json.num(key, getValue());
	}
}
