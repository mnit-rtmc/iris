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
 * Implementation of Device interface for testing.
 *
 * @author    <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version   $Revision: 1.4 $ $Date: 2003/12/08 16:58:23 $
 */
public class TestDevice extends TestTmsObject implements Device {

	private boolean active = true;
	private short crossDir = 0;
	private short freeDir = 0;
	private String notes = "";


	/** Creates new TestDevice */
	public TestDevice() { }


	/**
	 * Sets the active attribute of the TestDevice object
	 *
	 * @param a                                     The new active value
	 * @exception TMSException                      Description of Exception
	 * @exception RemoteException                   Description of Exception
	 */
	public void setActive( boolean a ) throws TMSException, RemoteException {
		active = a;
	}


	/**
	 * Sets the crossDir attribute of the TestDevice object
	 *
	 * @param param                                 The new crossDir value
	 * @exception TMSException                      Description of Exception
	 * @exception RemoteException                   Description of Exception
	 */
	public void setCrossDir( short param ) throws TMSException, RemoteException {
		crossDir = param;
	}


	/**
	 * Sets the crossStreet attribute of the TestDevice object
	 *
	 * @param crossStreet                           The new crossStreet value
	 * @exception TMSException                      Description of Exception
	 * @exception RemoteException                   Description of Exception
	 */
	public void setCrossStreet( String crossStreet ) throws TMSException,
			RemoteException {
	}


	/**
	 * Sets the freeDir attribute of the TestDevice object
	 *
	 * @param direction                             The new freeDir value
	 * @exception TMSException                      Description of Exception
	 * @exception RemoteException                   Description of Exception
	 */
	public void setFreeDir( short direction ) throws TMSException,
			RemoteException {
		freeDir = direction;
	}


	/**
	 * Sets the freeway attribute of the TestDevice object
	 *
	 * @param name                                  The new freeway value
	 * @exception TMSException                      Description of Exception
	 * @exception RemoteException                   Description of Exception
	 */
	public void setFreeway( String name ) throws TMSException, RemoteException {
	}


	/**
	 * Gets the controller attribute of the TestDevice object
	 *
	 * @return                              The controller value
	 * @exception RemoteException           Description of Exception
	 */
	public Controller getController() throws RemoteException {
		return null;
	}


	/**
	 * Gets the crossDir attribute of the TestDevice object
	 *
	 * @return                              The crossDir value
	 * @exception RemoteException           Description of Exception
	 */
	public short getCrossDir() throws RemoteException {
		return crossDir;
	}


	/**
	 * Gets the crossStreet attribute of the TestDevice object
	 *
	 * @return                              The crossStreet value
	 * @exception RemoteException           Description of Exception
	 */
	public Roadway getCrossStreet() throws RemoteException {
		return null;
	}


	/**
	 * Gets the freeDir attribute of the TestDevice object
	 *
	 * @return                              The freeDir value
	 * @exception RemoteException           Description of Exception
	 */
	public short getFreeDir() throws RemoteException {
		return freeDir;
	}


	/**
	 * Gets the freeway attribute of the TestDevice object
	 *
	 * @return                              The freeway value
	 * @exception RemoteException           Description of Exception
	 */
	public Roadway getFreeway() throws RemoteException {
		return null;
	}


	/**
	 * Gets the active attribute of the TestDevice object
	 *
	 * @return                              The active value
	 * @exception RemoteException           Description of Exception
	 */
	public boolean isActive() throws RemoteException {
		return active;
	}


	/**
	 * Gets the failed attribute of the TestDevice object
	 *
	 * @return                              The failed value
	 * @exception RemoteException           Description of Exception
	 */
	public boolean isFailed() throws RemoteException {
		return false;
	}
	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Device#getNotes()
	 */
	public String getNotes() throws RemoteException {
		return notes;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Device#setNotes(java.lang.String)
	 */
	public void setNotes(String n) throws TMSException, RemoteException {
		notes = n;
	}

}
