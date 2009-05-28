/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

/**
 * Ntcip DmsLegend object
 *
 * @author Douglas Lau
 */
public class DmsLegend extends DmsSignCfg implements ASN1Integer {

	/** Sign legends */
	static protected final String[] LEGEND = {
		"?", "other", "noLegend", "legendExists"
	};

	/** Create a new DMS legend object */
	public DmsLegend() {
		super(7);
	}

	/** Get the object name */
	protected String getName() { return "dmsLegend"; }

	/** Sign legend */
	protected int legend;

	/** Set the integer value */
	public void setInteger(int value) {
		legend = value;
		if(legend < 0 || legend >= LEGEND.length) legend = 0;
	}

	/** Get the integer value */
	public int getInteger() { return legend; }

	/** Get the object value */
	public String getValue() { return LEGEND[legend]; }
}
