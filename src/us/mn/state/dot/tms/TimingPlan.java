/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2004  Minnesota Department of Transportation
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
 * Timing plan
 *
 * @author Douglas Lau
 */
public interface TimingPlan extends TMSObject {

	/** Number of minutes in a day */
	public int MINUTES_PER_DAY = 24 * 60;

	/** AM period plan */
	public int AM = 0;

	/** PM period plan */
	public int PM = 1;

	/** Get the plan type */
	public String getPlanType() throws RemoteException;

	/** Get the start time (minute of day) */
	public int getStartTime() throws RemoteException;

	/** Set the start time (minute of day) */
	public void setStartTime(int t) throws TMSException, RemoteException;

	/** Get the stop time (minute of day) */
	public int getStopTime() throws RemoteException;

	/** Set the stop time (minute of day) */
	public void setStopTime(int t) throws TMSException, RemoteException;

	/** Get the active status */
	public boolean isActive() throws RemoteException;

	/** Set the active status */
	public void setActive(boolean a) throws TMSException, RemoteException;

	/** Get the testing status */
	public boolean isTesting() throws RemoteException;

	/** Set the testing status */
	public void setTesting(boolean t) throws RemoteException;
}
