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
 * Ntcip DmsSignTechnology object
 *
 * @author Douglas Lau
 */
public class DmsSignTechnology extends DmsSignCfg implements ASN1Integer {

	/** Other technology */
	static public final int OTHER = 1 << 0;

	/** LED technology */
	static public final int LED = 1 << 1;

	/** Flip disk technology */
	static public final int FLIP_DISK = 1 << 2;

	/** Fiber optic technology */
	static public final int FIBER_OPTIC = 1 << 3;

	/** Shuttered technology */
	static public final int SHUTTERED = 1 << 4;

	/** Lamp technology */
	static public final int LAMP = 1 << 5;

	/** Drum technology */
	static public final int DRUM = 1 << 6;

	/** Create a new DmsSignTechnology object */
	public DmsSignTechnology() {
		super(9);
	}

	/** Get the object name */
	protected String getName() { return "dmsSignTechnology"; }

	/** Sign technology bitfield */
	protected int technology;

	/** Set the integer value */
	public void setInteger(int value) { technology = value; }

	/** Get the integer value */
	public int getInteger() { return technology; }

	/** Get the object value */
	public String getValue() {
		StringBuffer buffer = new StringBuffer();
		if((technology & DRUM) > 0) buffer.append("Drum");
		if((technology & LAMP) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("Lamp");
		}
		if((technology & SHUTTERED) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("Shuttered");
		}
		if((technology & FIBER_OPTIC) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("Fiber Optics");
		}
		if((technology & FLIP_DISK) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("Flip Disk");
		}
		if((technology & LED) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("LED");
		}
		if((technology & OTHER) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("Other");
		}
		if(buffer.length() == 0) buffer.append("None");
		return buffer.toString();
	}
}
