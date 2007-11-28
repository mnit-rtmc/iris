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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.FontImpl;
import us.mn.state.dot.tms.GlyphImpl;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to send a font to a DMS controller
 *
 * @author Douglas Lau
 */
public class DMSFontDownload extends DMSOperation {

	/** Version ID returned by Skyline controllers for NORMAL font */
	static protected final int SKYLINE_VERSION_ID_NORMAL = 288;

	/** Version ID returned by Skyline controllers for HINTED font */
	static protected final int SKYLINE_VERSION_ID_HINTED = 64723;

	/** Font to download to the sign */
	protected final FontImpl font;

	/** Font index */
	protected final int index;

	/** Glyph mapping */
	protected final SortedMap<Integer, GlyphImpl> glyphs;

	/** Create a new DMS font download operation */
	public DMSFontDownload(DMSImpl d, FontImpl f) {
		super(DOWNLOAD, d);
		font = f;
		// On ADDCO signs, font 1 is read-only (apparently)
		if(d.getMake().contains("ADDCO"))
			index = 2;
		else
			index = 1;
		glyphs = f.getGlyphs();
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new CheckVersionID();
	}

	/** Check if the font version ID matches */
	protected boolean fontMatches(int v) {
		if(v == font.getVersionID())
			return true;
		if(index == 1 && v == SKYLINE_VERSION_ID_NORMAL)
			return true;
		if(index == 2 && v == SKYLINE_VERSION_ID_HINTED)
			return true;
		return false;
	}

	/** Check version ID */
	protected class CheckVersionID extends Phase {

		/** Check the font version ID */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontVersionID version = new FontVersionID(index);
			mess.add(version);
			try { mess.getRequest(); }
			catch(SNMP.Message.NoSuchName e) {
				return new CreateFont();
			}
			int v = version.getInteger();
			DMS_LOG.log(dms.getId() + " Font #" + index +
				" versionID:" + v);
			if(fontMatches(v))
				return null;
			else
				return new InvalidateFont();
		}
	}

	/** Invalidate the font */
	protected class InvalidateFont extends Phase {

		/** Invalidate a font entry in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new FontHeight(index, 0));
			mess.setRequest();
			return new CreateFont();
		}
	}

	/** Create the font */
	protected class CreateFont extends Phase {

		/** Create a new font in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new FontNumber(index, index));
			mess.add(new FontName(index, font.getName()));
			mess.add(new FontHeight(index, font.getHeight()));
			mess.add(new FontCharSpacing(index,
				font.getCharSpacing()));
			mess.add(new FontLineSpacing(index,
				font.getLineSpacing()));
			mess.setRequest();
			if(!glyphs.isEmpty())
				return new AddCharacter(glyphs.values());
			else
				return new ValidateFont();
		}
	}

	/** Add a character to the font table */
	protected class AddCharacter extends Phase {

		/** Iterator for remaining glyphs */
		protected final Iterator<GlyphImpl> chars;

		/** Current glyph */
		protected GlyphImpl glyph;

		/** Count of characters added */
		protected int count = 0;

		/** Create a new add character phase */
		public AddCharacter(Collection<GlyphImpl> c) {
			chars = c.iterator();
			if(chars.hasNext())
				glyph = chars.next();
		}

		/** Add a character to the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int code_point = glyph.getCodePoint();
			Graphic graphic = glyph.getGraphic();
			byte[] pixels = Base64.decode(graphic.getPixels());
			mess.add(new CharacterWidth(index, code_point,
				graphic.getWidth()));
			mess.add(new CharacterBitmap(index, code_point,
				pixels));
			mess.setRequest();
			count++;
			if(count >= 20 && !controller.isFailed())
				controller.resetErrorCounter(id);
			if(chars.hasNext()) {
				glyph = chars.next();
				return this;
			} else
				return new ValidateFont();
		}
	}

	/** Validate the font. This forces a LedStar fontVersionID update. */
	protected class ValidateFont extends Phase {

		/** Validate a font entry in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new FontHeight(index, font.getHeight()));
			mess.setRequest();
			return new SetDefaultFont();
		}
	}

	/** Set the default font number for message text */
	protected class SetDefaultFont extends Phase {

		/** Set the default font numbmer */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new DefaultFont(index));
			mess.setRequest();
			return null;
		}
	}
}
