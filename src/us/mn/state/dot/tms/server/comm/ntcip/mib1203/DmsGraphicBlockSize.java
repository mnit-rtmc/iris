/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * DmsGraphicBlockSize is the number of bytes in each block within a graphic
 * image on the sign.
 *
 * @author Douglas Lau
 */
public class DmsGraphicBlockSize extends ASN1Integer {

	/** Create a new DmsGraphicBlockSize object */
	public DmsGraphicBlockSize() {
		super(MIB1203.graphicDefinition.create(new int[] {5, 0}));
	}
}
