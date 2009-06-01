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
package us.mn.state.dot.tms.server.comm.ntcip.mibledstar;

import us.mn.state.dot.tms.server.comm.ntcip.MIBNode;
import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;

/**
 * MIB nodes for Ledstar NTCIP signs.
 *
 * @author Douglas Lau
 */
class MIB extends MIBNode {

	private MIB() {
		super(null, null);
		assert false;
	}

	static public final MIBNode ledstar = MIB1201._private.create(16);
	static public final MIBNode ledstarDMS = ledstar.create(1);
	static public final MIBNode ledstarSignControl = ledstarDMS.create(1);
	static public final MIBNode ledstarDiagnostics = ledstarDMS.create(2);
}
