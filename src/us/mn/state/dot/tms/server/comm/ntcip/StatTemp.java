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
  * Ntcip StatTemp node
  *
  * @author Douglas Lau
  */
abstract class StatTemp extends DmsStatus {

	/** Create a new StatTemp object
	  * @param n additional nodes in object identifier */
	protected StatTemp( int i ) {
		super( 3 );
		oid[ node++ ] = 9;
		oid[ node++ ] = i;
		oid[ node++ ] = 0;
	}
}
