/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
 * Ntcip VmsPixelServiceFrequency object.  This really should be called
 * VmsPixelServicePeriod, since it defines the period, in seconds, for
 * servicing pixels.  Oh, well, it's part of NTCIP 1203.
 *
 * @author Douglas Lau
 */
public class VmsPixelServiceFrequency extends ASN1Integer {

	/** Create a new VmsPixelServiceFrequency object */
	public VmsPixelServiceFrequency() {
		super(MIB1203.signControl.create(new int[] {22, 0}));
	}
}
