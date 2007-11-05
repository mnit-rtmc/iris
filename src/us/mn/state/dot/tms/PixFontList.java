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
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * PixFontList is a collection which stores an indexed list of dynamic
 * message sign fonts.
 *
 * @author Douglas Lau
 */
public class PixFontList extends IndexedListImpl {

	/** Create a new pixel font list */
	public PixFontList() throws RemoteException {
		super( false );
	}

	/** Append a new pixel font to the list */
	public synchronized TMSObject append() throws TMSException,
		RemoteException
	{
		int index = list.size();
		PixFontImpl font = new PixFontImpl( index + 1 );
		try { vault.save( font, getUserName() ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		list.add( font );
		notifyAdd( index, font.toString() );
		return font;
	}

	/** Get the first matching font */
	public synchronized PixFontImpl getFont(int h, int w, int ls) {
		Iterator it = list.iterator();
		while(it.hasNext()) {
			PixFontImpl font = (PixFontImpl)it.next();
			if(font.matches(h, w, ls))
				return font;
		}
		return null;
	}
}
