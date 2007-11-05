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
 * Ntcip DmsMessageTable node
 *
 * @author Douglas Lau
 */
abstract class DmsMessageTable extends DmsMessage {

	/** Memory type */
	protected final int memoryType;

	/** Message number */
	protected final int number;

	/** Create a new DmsMessageTable item
	  * @param m Memory type
	  * @param n Message number */
	protected DmsMessageTable(int m, int n) {
		super(5);
		memoryType = m;
		number = n;
		oid[node++] = 8;
		oid[node++] = 1;
		oid[node++] = getTableItem();
		oid[node++] = memoryType;
		oid[node++] = number;
	}

	/** Get the message table item (defined in subclasses) */
	abstract protected int getTableItem();

	/** Create an object description */
	public final String toString() {
		return getName() + "." +
			DmsMessageMemoryType.DESCRIPTION[memoryType] + "." +
			number + ": " + getValue();
	}
}
