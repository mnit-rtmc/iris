/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2002  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.ntcip;

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
