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
 * Roadway segment class for off-ramp segments
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class OffRampImpl extends SegmentImpl implements OffRamp {

	/** ObjectVault table name */
	static public final String tableName = "off_ramp";

	/** To-CD flag */
	protected final boolean toCd;

	/** Get the to-CD flag */
	public boolean isToCd() { return toCd; }

	/** From-CD flag */
	protected final boolean fromCd;

	/** Get the from-CD flag */
	public boolean isFromCd() { return fromCd; }

	/** Number of ramp lanes */
	protected final int rampLanes;

	/** Get the number of ramp lanes */
	public int getRampLanes() { return rampLanes; }

        /**
         * Create a new off-ramp
         * @param left flag for left-side ramps
         * @param delta change in number of mainline lanes
         * @param cdDelta change in number of collector-distributor lanes
	 * @param toCd flag to determine if the ramp is to a CD
	 * @param fromCd flag to determine if the ramp is from a CD
         * @param rampLanes number of lanes on the ramp
         */
	public OffRampImpl(boolean left, int delta, int cdDelta, boolean toCd,
		boolean fromCd, int rampLanes) throws RemoteException
	{
		super(left, delta, cdDelta);
		if(delta > 0) throw new
			IllegalArgumentException("Delta cannot be > 0");
		if(toCd && fromCd) throw new
			IllegalArgumentException("Cannot be toCd and fromCd");
		this.toCd = toCd;
		this.fromCd = fromCd;
		this.rampLanes = rampLanes;
	}

	/** Create an off-ramp from an ObjectVault field map */
	protected OffRampImpl(FieldMap fields) throws RemoteException {
		super(fields);
		toCd = fields.getBoolean("toCd");
		fromCd = fields.getBoolean("fromCd");
		rampLanes = fields.getInt("rampLanes");
	}

	/** Test if a detector is valid for an off-ramp segment */
	protected boolean validateDetector(DetectorImpl det) {
		return det.isOffRamp();
	}

	/** Validate the segment from the previous segment's values */
	public boolean validate(int lanes, int shift, int cd) {
		boolean result = true;
		if(cd < 1 && fromCd)
			result = false;
		if(cd < 1 && cdDelta < 1 && toCd)
			result = false;
		return super.validate(lanes, shift, cd) && result;
	}
}
