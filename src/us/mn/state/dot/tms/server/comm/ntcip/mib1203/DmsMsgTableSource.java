/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * Ntcip DmsMsgTableSource object
 *
 * @author Douglas Lau
 */
public class DmsMsgTableSource extends MessageIDCode {

	/** Create a new DmsMsgTableSource object */
	public DmsMsgTableSource() {
		super(MIB1203.signControl.create(new int[] {5, 0}));
	}

	/** Test if the message table source is valid */
	public boolean isValid() {
		switch(getMemoryType()) {
		case permanent:
		case _volatile:
		case changeable:
		case blank:
			return true;
		default:
			return false;
		}
	}
}
