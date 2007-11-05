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
import java.rmi.server.*;

/**
 * LoginImpl
 *
 * @author Douglas Lau
 */
final class LoginImpl extends UnicastRemoteObject implements Login {

	/** TMS object */
	protected final TMS tms;

	/** Create a new login server object */
	LoginImpl( TMS t ) throws RemoteException {
		super();
		tms = t;
	}

	/** Log in a user */
	public TMS login( String userName ) {
		try { TMSObjectImpl.loginUser( userName ); }
		catch( ServerNotActiveException e ) {
			return null;
		}
		return tms;
	}
}
