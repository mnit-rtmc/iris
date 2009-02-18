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
import java.util.LinkedList;
import java.util.SortedMap;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.BaseObjectImpl;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontImpl;
import us.mn.state.dot.tms.GlyphImpl;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to send a set of fonts to a DMS controller.
 *
 * @author Douglas Lau
 */
public class DMSFontDownload extends DMSOperation {

	/** Default font index */
	static protected final int DEFAULT_FONT = 1;

	/** Number of fonts supported */
	protected final NumFonts num_fonts = new NumFonts();

	/** List of fonts to be sent to the sign */
	protected final LinkedList<FontImpl> fonts = new LinkedList<FontImpl>();

	/** Font index (starts at 1, not 0).
	 * On some signs (ADDCO), font index 1 is read-only (apparently). */
	protected int index = DEFAULT_FONT;

	/** Create a new DMS font download operation */
	public DMSFontDownload(DMSImpl d) {
		super(DOWNLOAD, d);
		Integer w = dms.getWidthPixels();
		Integer h = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		if(w != null && h != null && cw != null && ch != null) {
			PixelMapBuilder builder = new PixelMapBuilder(
				BaseObjectImpl.namespace, w, h, cw, ch);
			builder.findFonts(new Checker<Font>() {
				public boolean check(Font font) {
					fonts.add((FontImpl)font);
					return false;
				}
			});
		}
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryNumFonts();
	}

	/** Phase to query the number of supported fonts */
	protected class QueryNumFonts extends Phase {

		/** Query the number of supported fonts */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(num_fonts);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + num_fonts);
			return currentFontPhase();
		}
	}

	/** Get the current font */
	protected FontImpl currentFont() {
		return fonts.get(index - 1);
	}

	/** Get the first phase of the current font */
	protected Phase currentFontPhase() {
		if(index < 1 || index > fonts.size())
			return null;
		if(index > num_fonts.getInteger()) {
			DMS_LOG.log(dms.getName() + ": Too many fonts");
			for(int i = index - 1; i < fonts.size(); i++) {
				DMS_LOG.log(dms.getName() + ": Skipping font " +
					fonts.get(i).getName());
			}
			return null;
		}
		return new CheckVersionID();
	}

	/** Move to the first phase of the next font */
	protected Phase nextFontPhase() {
		index++;
		return currentFontPhase();
	}

	/** Check version ID */
	protected class CheckVersionID extends Phase {

		/** Check the font version ID */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontImpl font = currentFont();
			DMS_LOG.log(dms.getName() + " Font #" + index +
				", name: " + font.getName() + ", number: " +
				 font.getNumber());
			FontVersionID version = new FontVersionID(index);
			mess.add(version);
			try {
				mess.getRequest();
			}
			// Note: some vendors respond with NoSuchName if the
			//       font is not valid
			catch(SNMP.Message.NoSuchName e) {
				return new CreateFont();
			}
			int v = version.getInteger();
			DMS_LOG.log(dms.getName() + " Font #" + index +
				" versionID:" + v);
			if(v == font.getVersionID()) {
				DMS_LOG.log(dms.getName() + " Font #" + index +
					" is valid");
				return nextFontPhase();
			} else
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
			FontImpl font = currentFont();
			mess.add(new FontNumber(index, font.getNumber()));
			mess.add(new FontName(index, font.getName()));
			mess.add(new FontHeight(index, font.getHeight()));
			mess.add(new FontCharSpacing(index,
				font.getCharSpacing()));
			mess.add(new FontLineSpacing(index,
				font.getLineSpacing()));
			mess.setRequest();

			SortedMap<Integer, GlyphImpl> glyphs = font.getGlyphs();
			if(glyphs.isEmpty())
				return new ValidateFont();
			else
				return new AddCharacter(glyphs.values());
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
			FontImpl font = currentFont();
			mess.add(new FontHeight(index, font.getHeight()));
			mess.setRequest();
			if(index == DEFAULT_FONT)
				return new SetDefaultFont();
			else
				return nextFontPhase();
		}
	}

	/** Set the default font number for message text */
	protected class SetDefaultFont extends Phase {

		/** Set the default font numbmer */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DefaultFont dfont = new DefaultFont(index);
			DMS_LOG.log(dms.getName() + ": " + dfont);
			mess.add(dfont);
			mess.setRequest();
			return nextFontPhase();
		}
	}
}
