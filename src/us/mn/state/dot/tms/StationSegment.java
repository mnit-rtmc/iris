/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004  Minnesota Department of Transportation
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
 * RMI interface for a mainline station roadway segment
 *
 * @author Douglas Lau
 */
public interface StationSegment extends Segment {

	/** Get the station index */
	public Integer getIndex() throws RemoteException;

	/** Set the station index */
	public void setIndex(Integer i) throws TMSException, RemoteException;

	/** Get the station label */
	public String getLabel() throws RemoteException;

	/** Get the station speed limit */
	public int getSpeedLimit() throws RemoteException;

	/** Set the station speed limit */
	public void setSpeedLimit(int l) throws TMSException, RemoteException;

	/** Is this station active */
	public boolean isActive() throws RemoteException;

	/** Get the average station volume
	 * @deprecated */
	public float getVolume() throws RemoteException;

	/** Get the average station occupancy
	 * @deprecated */
	public float getOccupancy() throws RemoteException;

	/** Get the average station flow */
	public int getFlow() throws RemoteException;

	/** Get the average station speed */
	public int getSpeed() throws RemoteException;
}
