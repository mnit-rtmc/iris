/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2002  Minnesota Department of Transportation
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
 * Root MIBObject
 *
 * @author Douglas Lau
 */
abstract class MIBObject {

	/** Get the object identifier */
	abstract public int[] getOID();

	/** Get the object name */
	abstract protected String getName();

	/** Get the object value */
	abstract protected String getValue();

	/** Create an object description */
	public String toString() {
		return getName() + ": " + getValue();
	}
}
