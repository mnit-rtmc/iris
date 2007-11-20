/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.util.Arrays;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;

/**
 * The PixCharacterImpl class defines all the attributes of a font character.
 *
 * @author Douglas Lau
 */
final class PixCharacterImpl extends TMSObjectImpl implements PixCharacter,
	Storable
{
	/** ObjectVault table name */
	static public final String tableName = "character";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new pixel font character
	 * @param i Character index */
	public PixCharacterImpl( int i ) throws RemoteException {
		super();
		index = i;
		width = 5;
		bitmap = new byte[ 5 ];
	}

	/** Create a pixel font character from an ObjectVault field map */
	protected PixCharacterImpl( FieldMap fields ) throws RemoteException {
		super();
		index = fields.getInt( "index" );
	}

	/** Get a string representation of the character */
	public String toString() {
		StringBuffer buffer = new StringBuffer().append( index );
		while( buffer.length() < 3 ) buffer.insert( 0, ' ' );
		if( index > 31 ) {
			buffer.append( "  (" );
			buffer.append( (char)( index ) );
			buffer.append( ")" );
		}
		return buffer.toString();
	}

	/** Character index */
	private final int index;

	/** Get the character index.
	 * @return Character index number */
	public int getIndex() { return index; }

	/** Character width */
	private int width;

	/** Set the character width.
	 * @param w Character width */
	public synchronized void setWidth(int w) throws TMSException {
		if(w == width)
			return;
		store.update(this, "width", w);
		width = w;
	}

	/** Get the character width.
	 * @return Character width */
	public int getWidth() { return width; }

	/** Character bitmap */
	private byte[] bitmap;

	/** Set the character bitmap.
	 * @param b A bitmap which defines the pixels within a rectangular
	 * region as either on or off.  The most significant bit corresponds
	 * with the pixel in the upper left corner of the character.  From
	 * there, the character is defined in rows, left to right, then top to
	 * bottom. */
	public synchronized void setBitmap(byte[] b) throws TMSException {
		if(Arrays.equals(b, bitmap))
			return;
		store.update(this, "bitmap", b);
		bitmap = b;
	}

	/** Get the character bitmap.
	 * @return A bitmap which defines the pixels within a rectangular
	 * region as either on or off.  The most significant bit corresponds
	 * with the pixel in the upper left corner of the character.  From
	 * there, the character is defined in rows, left to right, then top to
	 * bottom. */
	public byte[] getBitmap() { return bitmap; }

	/** Render the character onto a bitmap graphic */
	public void renderOn(BitmapGraphic g, int x, int y, int h) {
		BitmapGraphic c = new BitmapGraphic(width, h);
		c.setBitmap(bitmap);
		for(int yy = 0; yy < h; yy++) {
			for(int xx = 0; xx < width; xx++) {
				int p = c.getPixel(xx, yy);
				g.setPixel(x + xx, y + yy, p);
			}
		}
	}
}
