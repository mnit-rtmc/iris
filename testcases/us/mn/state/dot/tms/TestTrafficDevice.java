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

/**
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.2 $ $Date: 2002/04/18 19:55:38 $
 */
public class TestTrafficDevice extends TestDevice implements TrafficDevice {
	
	private float xCoord = 0;
	private float xOffset = 0;
	private float yCoord = 0;
	private float yOffset =0;
	
	/** Creates new TestTrafficDevice */
	public TestTrafficDevice() {
	}
	
	public String getId() throws java.rmi.RemoteException {
		return "XXXXXX";
	}
	
	public String getOperation() throws java.rmi.RemoteException {
		return "None";
	}
	
	public String getStatus() throws java.rmi.RemoteException {
		return "Just fine";
	}
	
	public float getX() throws java.rmi.RemoteException {
		return xCoord;
	}
	
	public float getXOffset() throws java.rmi.RemoteException {
		return xOffset;
	}
	
	public float getY() throws java.rmi.RemoteException {
		return yCoord;
	}
	
	public float getYOffset() throws java.rmi.RemoteException {
		return yOffset;
	}
	
	public void setX(float param) throws us.mn.state.dot.tms.TMSException, java.rmi.RemoteException {
		xCoord = param;
	}
	
	public void setXOffset(float param) throws us.mn.state.dot.tms.TMSException, java.rmi.RemoteException {
		xOffset = param;
	}
	
	public void setY(float param) throws us.mn.state.dot.tms.TMSException, java.rmi.RemoteException {
		yCoord = param;
	}
	
	public void setYOffset(float param) throws us.mn.state.dot.tms.TMSException, java.rmi.RemoteException {
		yOffset = param;
	}
	
}
