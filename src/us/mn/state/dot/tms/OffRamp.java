/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001  Minnesota Department of Transportation
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

/**
 * Segment representing an off-ramp
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.9 $ $Date: 2001/10/24 17:37:26 $
 */
public interface OffRamp extends Ramp {

	/** Get the to-CD flag */
	public boolean isToCd() throws RemoteException;

	/** Get the from-CD flag */
	public boolean isFromCd() throws RemoteException;

	/** Get the number of ramp lanes */
	public int getRampLanes() throws RemoteException;
		
}
