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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.server.BaseObjectImpl;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.FontImpl;
import us.mn.state.dot.tms.server.GlyphImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to send a set of fonts to a DMS controller.
 *
 * @author Douglas Lau
 */
public class OpSendDMSFonts extends OpDMS {

	/** Number of fonts supported */
	protected final NumFonts num_fonts = new NumFonts();

	/** Maximum number of characters in a font */
	protected final MaxFontCharacters max_characters =
		new MaxFontCharacters();

	/** Mapping of font numbers to row in font table */
	protected final TreeMap<Integer, Integer> num_2_row =
		new TreeMap<Integer, Integer>();

	/** Set of open rows in the font table */
	protected final TreeSet<Integer> open_rows = new TreeSet<Integer>();

	/** Iterator of fonts to be sent to the sign */
	protected final Iterator<FontImpl> font_iterator;

	/** Current font */
	protected FontImpl font;

	/** Current row in font table */
	protected int row;

	/** Flag for determining the default font */
	protected boolean first = true;

	/** Flag for version 2 controller (with support for fontStatus) */
	protected boolean version2 = true;

	/** Create a new operation to send fonts to a DMS */
	public OpSendDMSFonts(DMSImpl d) {
		super(DOWNLOAD, d);
		final LinkedList<FontImpl> fonts = new LinkedList<FontImpl>();
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
		for(FontImpl f: fonts)
			num_2_row.put(f.getNumber(), null);
		font_iterator = fonts.iterator();
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
			mess.add(max_characters);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + num_fonts);
			DMS_LOG.log(dms.getName() + ": " + max_characters);
			for(row = 1; row <= num_fonts.getInteger(); row++)
				open_rows.add(row);
			row = 1;
			return new QueryFontNumbers();
		}
	}

	/** Phase to query all font numbers */
	protected class QueryFontNumbers extends Phase {

		/** Query the font number for one font */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontNumber number = new FontNumber(row);
			mess.add(number);
			try {
				mess.getRequest();
			}
			catch(SNMP.Message.NoSuchName e) {
				return populateNum2Row();
			}
			DMS_LOG.log(dms.getName() + ": " + number);
			Integer f_num = number.getInteger();
			if(num_2_row.containsKey(f_num)) {
				num_2_row.put(f_num, row);
				open_rows.remove(row);
			}
			if(row < num_fonts.getInteger()) {
				row++;
				return this;
			} else
				return populateNum2Row();
		}
	}

	/** Populate the num_2_row mapping */
	protected Phase populateNum2Row() {
		for(Integer f_num: num_2_row.keySet()) {
			if(num_2_row.get(f_num) == null) {
				Integer r = open_rows.pollLast();
				if(r != null)
					num_2_row.put(f_num, r);
				else
					num_2_row.remove(f_num);
			}
		}
		return nextFontPhase();
	}

	/** Get the first phase of the next font */
	protected Phase nextFontPhase() {
		while(font_iterator.hasNext()) {
			font = font_iterator.next();
			Integer f_num = font.getNumber();
			if(num_2_row.containsKey(f_num)) {
				row = num_2_row.get(f_num);
				return new VerifyFont();
			}
			DMS_LOG.log(dms.getName() + ": Skipping font " + f_num);
		}
		return null;
	}

	/** Phase to verify a font */
	protected class VerifyFont extends Phase {

		/** Verify a font */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DMS_LOG.log(dms.getName() + " Font #" + row +
				", name: " + font.getName() + ", number: " +
				 font.getNumber());
			FontVersionID version = new FontVersionID(row);
			mess.add(version);
			try {
				mess.getRequest();
			}
			catch(SNMP.Message.NoSuchName e) {
				// Note: some vendors respond with NoSuchName
				//       if the font is not valid
				version.setInteger(-1);
			}
			int v = version.getInteger();
			DMS_LOG.log(dms.getName() + ": " + version);
			if(v == font.getVersionID()) {
				DMS_LOG.log(dms.getName() + ": Font is valid");
				return nextFontPhase();
			} else {
				if(version2)
					return new QueryInitialStatus();
				else
					return new InvalidateFontV1();
			}
		}
	}

	/** Phase to query the initial font status */
	protected class QueryInitialStatus extends Phase {

		/** Query the initial font status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			mess.add(status);
			try {
				mess.getRequest();
			}
			catch(SNMP.Message.NoSuchName e) {
				version2 = false;
				return new InvalidateFontV1();
			}
			DMS_LOG.log(dms.getName() + ": " + status);
			switch(status.getEnum()) {
			case modifying:
				return new CreateFont();
			case permanent:
				return nextFontPhase();
			case unmanaged:
				return new InvalidateFontV2();
			case inUse:
				DMS_LOG.log(dms.getName() +
					": font send aborted");
				return null;
			default:
				return new SetStatusModifying();
			}
		}
	}

	/** Invalidate the font (v1) */
	protected class InvalidateFontV1 extends Phase {

		/** Invalidate a font entry in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontHeight height = new FontHeight(row, 0);
			mess.add(height);
			mess.setRequest();
			DMS_LOG.log(dms.getName() + ": " + height);
			return new CreateFont();
		}
	}

	/** Invalidate the font (v2) */
	protected class InvalidateFontV2 extends Phase {

		/** Invalidate the font entry in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			status.setInteger(FontStatus.Enum.notUsedReq.ordinal());
			mess.add(status);
			mess.setRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			return new SetStatusModifying();
		}
	}

	/** Phase to set the font status to modifying */
	protected class SetStatusModifying extends Phase {

		/** Set the font status to modifying */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			status.setInteger(FontStatus.Enum.modifyReq.ordinal());
			mess.add(status);
			mess.setRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			return new VerifyStatusModifying();
		}
	}

	/** Phase to verify the font status is modifying */
	protected class VerifyStatusModifying extends Phase {

		/** Verify the font status is modifying */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			mess.add(status);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			if(status.getEnum() != FontStatus.Enum.modifying) {
				DMS_LOG.log(dms.getName() +
					": font send aborted");
				return null;
			}
			return new CreateFont();
		}
	}

	/** Create the font */
	protected class CreateFont extends Phase {

		/** Create a new font in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new FontNumber(row, font.getNumber()));
			mess.add(new FontName(row, font.getName()));
			mess.add(new FontHeight(row, font.getHeight()));
			mess.add(new FontCharSpacing(row,
				font.getCharSpacing()));
			mess.add(new FontLineSpacing(row,
				font.getLineSpacing()));
			mess.setRequest();
			DMS_LOG.log(dms.getName() + ": create font #" + row);
			SortedMap<Integer, GlyphImpl> glyphs = font.getGlyphs();
			if(glyphs.isEmpty()) {
				if(version2)
					return new ValidateFontV2();
				else
					return new ValidateFontV1();
			} else
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
			mess.add(new CharacterWidth(row, code_point,
				graphic.getWidth()));
			mess.add(new CharacterBitmap(row, code_point,
				pixels));
			mess.setRequest();
			count++;
			if(count % 20 == 0 && !controller.isFailed())
				controller.resetErrorCounter(id);
			if(chars.hasNext()) {
				glyph = chars.next();
				return this;
			} else {
				if(version2)
					return new ValidateFontV2();
				else
					return new ValidateFontV1();
			}
		}
	}

	/** Validate the font. This forces a fontVersionID update on some signs
	 * which implement 1203 version 1 (LedStar). */
	protected class ValidateFontV1 extends Phase {

		/** Validate a font entry in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new FontHeight(row, font.getHeight()));
			mess.setRequest();
			if(first)
				return new SetDefaultFont();
			else
				return nextFontPhase();
		}
	}

	/** Validate the font on a 1203 version 2 sign. */
	protected class ValidateFontV2 extends Phase {

		/** Validate a font entry in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			status.setInteger(
				FontStatus.Enum.readyForUseReq.ordinal());
			mess.add(status);
			mess.setRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			return new VerifyStatusReadyForUse();
		}
	}

	/** Phase to verify the font status is ready for use */
	protected class VerifyStatusReadyForUse extends Phase {

		/** Time to stop checking if the font is ready for use */
		protected final long expire = System.currentTimeMillis() + 
			10 * 1000;

		/** Verify the font status is ready for use */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			mess.add(status);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			if(status.getEnum() == FontStatus.Enum.readyForUse) {
				if(first)
					return new SetDefaultFont();
				else
					return nextFontPhase();
			}
			if(status.getEnum() != FontStatus.Enum.calculatingID) {
				DMS_LOG.log(dms.getName() + ": font status " +
					"unexpected -- aborted");
				return null;
			}
			if(System.currentTimeMillis() > expire) {
				DMS_LOG.log(dms.getName() + ": font status " +
					"timeout expired -- aborted");
				return null;
			} else
				return this;
		}
	}

	/** Set the default font number for message text */
	protected class SetDefaultFont extends Phase {

		/** Set the default font numbmer */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DefaultFont dfont = new DefaultFont(font.getNumber());
			DMS_LOG.log(dms.getName() + ": " + dfont);
			mess.add(dfont);
			mess.setRequest();
			first = false;
			return nextFontPhase();
		}
	}
}
