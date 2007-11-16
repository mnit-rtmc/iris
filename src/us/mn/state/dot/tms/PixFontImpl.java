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

import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;

/**
 * The PixFontImpl class defines all the attributes of a pixel font. These
 * fonts are used for VMS messages, and are downloaded to NTCIP sign
 * controllers.
 *
 * @author Douglas Lau
 */
public class PixFontImpl extends TMSObjectImpl implements PixFont, Storable {

	/** ObjectVault table name */
	static public final String tableName = "font";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new pixel font
	 * @param i Font index */
	public PixFontImpl( int i ) throws RemoteException {
		super();
		index = i;
		characters = new PixCharacterList();
	}

	/** Create a pixel font from an ObjectVault field map */
	protected PixFontImpl( FieldMap fields ) throws RemoteException {
		super();
		index = fields.getInt( "index" );
		characters = (PixCharacterList)fields.get( "characters" );
	}

	/** Get a string representation of the font
	 * @return Description of font */
	public String toString() {
		StringBuffer buffer = new StringBuffer().append( index );
		if( name != null ) buffer.append( "  " ).append( name );
		return buffer.toString();
	}

	/** Font index (used for both fontIndex and fontNumber NTCIP objects)
	 */
	protected final int index;

	/** Get the font index. This is used for both the fontIndex and the
	 * fontNumber NTCIP objects.
	 * @return Font index number */
	public int getIndex() { return index; }

	/** Font name */
	protected String name;

	/** Set the font name
	 * @param s Font name */
	public void setName( String n ) throws TMSException {
		if( n.equals( name ) ) return;
		validateText(n);
		store.update(this, "name", n);
		name = n;
	}

	/** Get the font name
	 * @return Font name */
	public String getName() { return name; }

	/** Font height (in pixels) */
	protected int height = 7;

	/** Set the font height
	 * @param h Height of the font (in pixels)
	 * @exception ChangeVetoException thrown if the height is invalid or
	 * if there are any characters already defined for the font */
	public void setHeight( int h ) throws TMSException {
		if( h == height ) return;
		if( h < 4 || h > 24 ) throw new
			ChangeVetoException( "Invalid height" );
		if( characters.size() > 0 )
			throw new ChangeVetoException( "Characters exist" );
		store.update(this, "height", h);
		height = h;
	}

	/** Get the font height
	 * @return Height of the font (in pixels) */
	public int getHeight() { return height; }

	/** Default horizontal spacing between characters (in pixels) */
	protected int characterSpacing = 1;

	/** Set the default horizontal spacing between characters
	 * @param s Default spacing between characters (in pixels)
	 * @exception ChangeVetoException thrown if spacing is invalid */
	public void setCharacterSpacing( int s ) throws TMSException {
		if( s == characterSpacing ) return;
		if( s < 0 || s > 9 ) throw new
			ChangeVetoException( "Invalid spacing" );
		store.update(this, "characterSpacing", s);
		characterSpacing = s;
	}

	/** Get the default horizontal spacing between characters
	 * @return Default spacing between characters (in pixels) */
	public int getCharacterSpacing() { return characterSpacing; }

	/** Default vetical spacing between lines (in pixels) */
	protected int lineSpacing = 0;

	/** Set the default vertical spacing between lines.
	 * This attribute only applies to full matrix signs.
	 * @param s Default spacing between lines (in pixels)
	 * @exception ChangeVetoException thrown if spacing is invalid */
	public void setLineSpacing( int s ) throws TMSException {
		if( s == lineSpacing ) return;
		if( s < 0 || s > 9 ) throw new
			ChangeVetoException( "Invalid spacing" );
		store.update(this, "lineSpacing", s);
		lineSpacing = s;
	}

	/** Get the default vertical spacing between lines.
	 * This attribute only applies to full matrix signs.
	 * @return Default spacing between lines (in pixels) */
	public int getLineSpacing() { return lineSpacing; }

	/** Character list */
	protected final PixCharacterList characters;

	/** Get the character list */
	public IndexedList getCharacterList() { return characters; }

	/** Font version ID */
	protected int versionID = 0;

	/** Set the font version ID.
	 * @param v Font version ID */
	public void setVersionID( int v ) throws TMSException {
		if( v == versionID ) return;
		store.update(this, "versionID", v);
		versionID = v;
	}

	/** Get the font version ID.
	 * @return Font version ID */
	public int getVersionID() { return versionID; }

	/** Render text onto a bitmap graphic.
	 * @param g Bitmap to render onto
	 * @param x X pixel to start
	 * @param y Y pixel to start
	 * @param t Text to render
	 * @throws IndexOufOfBoundsException If text has invalid characters. */
	public void renderOn(BitmapGraphic g, int x, int y, String t) {
		for(int i = 0; i < t.length(); i++) {
			int j = t.charAt(i);
			PixCharacterImpl c =
				(PixCharacterImpl)characters.getElement(j);
			c.renderOn(g, x, y, height);
			x += c.getWidth() + characterSpacing;
		}
	}

	/** Calculate the width (in pixels) of a line of text.
	 * @param t Single line of text
	 * @return Number of pixels width
	 * @throws IndexOufOfBoundsException If text has invalid characters. */
	public int calculateWidth(String t) {
		int width = 0;
		for(int i = 0; i < t.length(); i++) {
			if(i > 0)
				width += characterSpacing;
			int j = t.charAt(i);
			PixCharacterImpl c = (PixCharacterImpl)
				characters.getElement(j);
			width += c.getWidth();
		}
		return width;
	}

	/** Is this font deletable? */
	public boolean isDeletable() throws TMSException {
		if(characters.size() > 0) return false;
		return super.isDeletable();
	}

	/** Get the character width (0 for proportional) */
	protected int getWidth() {
		if(characterSpacing > 0)
			return 0;
		return characters.getWidth();
	}

	/** Test if the font matches a specified character height/width */
	protected boolean matches(int h, int w) {
		if(h == 0)
			return w == getWidth();
		else
			return (h == height) && (w == getWidth());
	}

	/** Test if the font matches the specified parameters */
	public boolean matches(int h, int w, int ls) {
		if(ls == 0 || ls == lineSpacing)
			return matches(h, w);
		else
			return false;
	}
}
