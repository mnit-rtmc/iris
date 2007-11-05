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

import java.rmi.*;

/**
 * Remote interface for the list of LaneControlSignals
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @created   May 24, 2002
 * @version   $Revision: 1.4 $ $Date: 2003/12/01 23:48:50 $
 */
public interface LCSList extends SortedList {

	/**
	 * Add an element to the list
	 *
	 * @param key                  The value to use as the key in the list
	 * @param lanes                The number of lanes
	 * @return                     The object that was added
	 * @exception TMSException    If there is a problem accessing
	 * the database
	 * @exception RemoteException  If there is an RMI error
	 */
	public TMSObject add( String key, int lanes ) throws TMSException,
			RemoteException;
}
