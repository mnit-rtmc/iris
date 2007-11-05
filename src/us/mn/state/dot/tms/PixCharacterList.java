/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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
 */
package us.mn.state.dot.tms;

import java.util.Iterator;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * PixCharacterList is the class which maintains a list of characters for
 * dynamic message sign fonts.
 *
 * @author Douglas Lau
 */
final class PixCharacterList extends IndexedListImpl {

	/** ObjectVault table name */
	static public final String tableName = "character_list";

	/** Create a new character list */
	public PixCharacterList() throws RemoteException {
		super( true );
	}

	/** Create a character list from an ObjectVault field map */
	protected PixCharacterList( FieldMap fields ) throws RemoteException {
		super( fields );
	}

	/** Append a character to the list */
	public synchronized TMSObject append() throws TMSException,
		RemoteException
	{
		int index = list.size();
		PixCharacterImpl c = new PixCharacterImpl( index + 1 );
		try { vault.add( list, index, c, getUserName() ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		list.add( c );
		notifyAdd( index, c.toString() );
		return c;
	}

	/** Get the font width (0 for proportional) */
	synchronized int getWidth() {
		boolean first = true;
		int w = 0;
		Iterator it = list.iterator();
		while(it.hasNext()) {
			PixCharacterImpl c = (PixCharacterImpl)it.next();
			if(first) {
				w = c.getWidth();
				first = false;
			}
			if(w != c.getWidth())
				return 0;
		}
		return w;
	}
}
