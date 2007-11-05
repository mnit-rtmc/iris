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
  * The PixCharacter interface defines all the attributes of a font character.
  *
  * @author Douglas Lau
  */
public interface PixCharacter extends TMSObject {

	/** Get the character index.
	 * @return Character index number */
	public int getIndex() throws RemoteException;

	/** Set the character width.
	 * @param w Character width */
	public void setWidth( int w ) throws TMSException, RemoteException;

	/** Get the character width.
	 * @return Character width */
	public int getWidth() throws RemoteException;

	/** Set the character bitmap.
	 * @param b A bitmap which defines the pixels within a rectangular
	 * region as either on or off.  The most significant bit corresponds
	 * with the pixel in the upper left corner of the character.  From
	 * there, the character is defined in rows, left to right, then top to
	 * bottom. */
	public void setBitmap( byte[] b ) throws TMSException,
		RemoteException;

	/** Get the character bitmap.
	 * @return A bitmap which defines the pixels within a rectangular
	 * region as either on or off.  The most significant bit corresponds
	 * with the pixel in the upper left corner of the character.  From
	 * there, the character is defined in rows, left to right, then top to
	 * bottom. */
	public byte[] getBitmap() throws RemoteException;
}
