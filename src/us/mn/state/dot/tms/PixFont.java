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

import java.rmi.RemoteException;

/**
 * The PixFont interface defines all the attributes of a pixel font.  These
 * fonts are used for VMS messages, and are downloaded to NTCIP sign
 * controllers.
 *
 * @author Douglas Lau
 */
public interface PixFont extends TMSObject {

	/** Get the font index. This is used for both the fontIndex and the
	  * fontNumber NTCIP objects.
	  * @return Font index number */
	public int getIndex() throws RemoteException;

	/** Set the font name
	  * @param s Font name */
	public void setName( String n ) throws TMSException, RemoteException;

	/** Get the font name
	  * @return Font name */
	public String getName() throws RemoteException;

	/** Set the font height
	  * @param h Height of the font (in pixels)
	  * @exception ChangeVetoException thrown if the height is invalid or
	  * if there are any characters already defined for the font */
	public void setHeight( int h ) throws TMSException, RemoteException;

	/** Get the font height
	  * @return Height of the font (in pixels) */
	public int getHeight() throws RemoteException;

	/** Set the default horizontal spacing between characters
	  * @param s Default spacing between characters (in pixels)
	  * @exception ChangeVetoException thrown if spacing is invalid */
	public void setCharacterSpacing( int s ) throws TMSException,
		RemoteException;

	/** Get the default horizontal spacing between characters
	  * @return Default spacing between characters (in pixels) */
	public int getCharacterSpacing() throws RemoteException;

	/** Set the default vertical spacing between lines.
	  * This attribute only applies to full matrix signs.
	  * @param s Default spacing between lines (in pixels)
	  * @exception ChangeVetoException thrown if spacing is invalid */
	public void setLineSpacing( int s ) throws TMSException,
		RemoteException;

	/** Get the default vertical spacing between lines.
	  * This attribute only applies to full matrix signs.
	  * @return Default spacing between lines (in pixels) */
	public int getLineSpacing() throws RemoteException;

	/** Get the character list */
	public IndexedList getCharacterList() throws RemoteException;

	/** Set the font version ID.
	  * @param v Font version ID */
	public void setVersionID( int v ) throws TMSException,
		RemoteException;

	/** Get the font version ID.
	  * @return Font version ID */
	public int getVersionID() throws RemoteException;
}
