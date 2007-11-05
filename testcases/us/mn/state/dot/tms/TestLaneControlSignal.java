/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000  Minnesota Department of Transportation
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
 * Implementation of a LaneControlSignal for testing purposes.
 *
 * @author    <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version   $Revision: 1.4 $ $Date: 2003/05/12 15:46:18 $
 */
public class TestLaneControlSignal extends TestTrafficDevice implements
		LaneControlSignal {

	private int[] signals = new int[]{0, 1, 2, 3, 4};

	private int statusCode = 2;

	private int lanes  = 2;

	/** Creates new TestLaneControlSignal */
	public TestLaneControlSignal() { }


	/**
	 * Sets the camera attribute of the TestLaneControlSignal object
	 *
	 * @param str                                   The new camera value
	 * @exception us.mn.state.dot.tms.TMSException  Description of Exception
	 * @exception java.rmi.RemoteException          Description of Exception
	 */
	public void setCamera( String str ) throws TMSException,
			java.rmi.RemoteException {
	}


	/**
	 * Sets the signals attribute of the TestLaneControlSignal object
	 *
	 * @param values                                The new signals value
	 * @exception us.mn.state.dot.tms.TMSException  Description of Exception
	 * @exception java.rmi.RemoteException          Description of Exception
	 */
	public void setSignals( int[] values ) throws
			us.mn.state.dot.tms.TMSException, java.rmi.RemoteException {
		signals = values;
	}

	public void setLanes( int laneCount ) {
		lanes = laneCount;
	}

	public int getLanes() {
		return lanes;
	}

	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Gets the camera attribute of the TestLaneControlSignal object
	 *
	 * @return                              The camera value
	 * @exception java.rmi.RemoteException  Description of Exception
	 */
	public us.mn.state.dot.tms.TrafficDevice getCamera()
			throws java.rmi.RemoteException {
		return null;
	}


	/**
	 * Gets the signals attribute of the TestLaneControlSignal object
	 *
	 * @return                              The signals value
	 * @exception java.rmi.RemoteException  Description of Exception
	 */
	public int[] getSignals() throws java.rmi.RemoteException {
		return signals;
	}

	/** Get the individual modules */
	public LCSModule[] getModules() throws java.rmi.RemoteException {
		return new LCSModule[lanes];
	}
	
	/**
	 * @see us.mn.state.dot.tms.LaneControlSignal#setSignals(int[], String)
	 */
	public void setSignals(int[] arg0, String arg1)
		throws TMSException, RemoteException {
	}

}
