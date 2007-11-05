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
 * A WarningSign is a traffic device can display one fixed message. It can
 * only be turned on or off.
 *
 * @author Douglas Lau
 */
public interface WarningSign extends TrafficDevice {

	/** Set the verification camera */
	public void setCamera(String id) throws TMSException, RemoteException;

	/** Get the verification camera */
	public TrafficDevice getCamera() throws RemoteException;

	/** Get the message text */
	public String getText() throws RemoteException;

	/** Set the message text */
	public void setText(String t) throws TMSException, RemoteException;

	/** Check if the warning sign is deployed */
	public boolean isDeployed() throws RemoteException;

	/** Set the deployed status of the sign */
	public void setDeployed(boolean d) throws RemoteException;
}
