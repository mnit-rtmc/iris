/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2006  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;

/**
 * A roadway segment which may contain a ramp meter
 *
 * @author Douglas Lau
 */
abstract public class MeterableImpl extends SegmentImpl implements Meterable {

	/** ObjectVault table name */
	static public final String tableName = "meterable";

	/** HOV bypass ramp */
	protected final boolean hovBypass;

	/** Does this segment have an HOV bypass? */
	public boolean hasHovBypass() { return hovBypass; }

	/**
	 * Create a new meterable roadway segment
	 * @param left flag for left-side ramps
	 * @param delta change in number of mainline lanes
	 * @param cdDelta change in number of collector-distributor lanes
	 * @param hovBypass flag for ramps with HOV bypasses
	 */
	public MeterableImpl(boolean left, int delta, int cdDelta,
		boolean hovBypass) throws RemoteException
	{
		super(left, delta, cdDelta);
		if(delta < 0) throw new
			IllegalArgumentException("Delta cannot be negative");
		this.hovBypass = hovBypass;
	}

	/** Create a meterable roadway segment from an ObjectVault field map */
	protected MeterableImpl(FieldMap fields) throws RemoteException {
		super( fields );
		hovBypass = fields.getBoolean("hovBypass");
	}

	protected boolean validateDetector( DetectorImpl det ) {
		return det.isOnRamp();
	}
}
