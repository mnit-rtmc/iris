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
public class TestTmsObject implements TMSObject {
	
	/** Creates new TestTmsObject */
	public TestTmsObject() {
	}
	
	public void addObserver(us.mn.state.dot.tms.RemoteObserver remoteObserver) throws java.rmi.RemoteException {
	}
	
	public void deleteObserver(us.mn.state.dot.tms.RemoteObserver remoteObserver) throws java.rmi.RemoteException {
	}
	
	public boolean isDeletable() throws us.mn.state.dot.tms.TMSException, java.rmi.RemoteException {
		return true;
	}
	
	public void notifyUpdate() throws java.rmi.RemoteException {
	}
	
}
