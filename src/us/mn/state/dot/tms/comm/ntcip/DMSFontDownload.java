/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

	/** Font to download to the sign */
	protected final FontImpl font;

	/** Should this be the default font */
	protected final boolean _default;

	/** Font index.
	 * On some signs (ADDCO), font index 1 is read-only (apparently). */
	protected final int index;

	/** Glyph mapping */
	protected final SortedMap<Integer, GlyphImpl> glyphs;

	/** Create a new DMS font download operation */
	public DMSFontDownload(DMSImpl d, FontImpl f, int i, boolean df) {
		super(DOWNLOAD, d);
		font = f;
		index = i;
		_default = df;
		glyphs = f.getGlyphs();
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new CheckVersionID();
	}

	/** Check if the font version ID matches */
	protected boolean fontMatches(int v) {
		return v == font.getVersionID();
	}

	/** Check version ID */
	protected class CheckVersionID extends Phase {

		/** Check the font version ID */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontVersionID version = new FontVersionID(index);
			mess.add(version);
			try {
				mess.getRequest();
			}
			catch(SNMP.Message.NoSuchName e) {
				return new CreateFont();
			}
			int v = version.getInteger();
			DMS_LOG.log(dms.getName() + " Font #" + index +
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
			mess.add(new FontNumber(index, font.getNumber()));
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
			if(count % 20 == 0 && !controller.isFailed())
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
			if(_default)
				return new SetDefaultFont();
			else
				return null;
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
