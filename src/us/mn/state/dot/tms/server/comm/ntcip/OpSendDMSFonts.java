/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontFinder;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
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
	protected final Iterator<Font> font_iterator;

	/** Current font */
	protected Font font;

	/** Current row in font table */
	protected int row;

	/** Flag for version 2 controller (with support for fontStatus) */
	protected boolean version2;

	/** Create a new operation to send fonts to a DMS */
	public OpSendDMSFonts(DMSImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
		FontFinder ff = new FontFinder(d);
		LinkedList<Font> fonts = ff.getFonts();
		for(Font f: fonts)
			num_2_row.put(f.getNumber(), null);
		font_iterator = fonts.iterator();
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new Query1203Version();
	}

	/** Phase to determine the version of NTCIP 1203 (1 or 2) */
	protected class Query1203Version extends Phase {

		/** Query the maximum character size (v2 only) */
		protected Phase poll(CommMessage mess) throws IOException {
			FontMaxCharacterSize max_char =
				new FontMaxCharacterSize();
			mess.add(max_char);
			try {
				mess.queryProps();
				DMS_LOG.log(dms.getName() + ": " + max_char);
				version2 = true;
			}
			catch(SNMP.Message.NoSuchName e) {
				// Note: if this object doesn't exist, then the
				//       sign must not support v2.
				version2 = false;
			}
			return new QueryNumFonts();
		}
	}

	/** Phase to query the number of supported fonts */
	protected class QueryNumFonts extends Phase {

		/** Query the number of supported fonts */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(num_fonts);
			mess.add(max_characters);
			mess.queryProps();
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
		protected Phase poll(CommMessage mess) throws IOException {
			FontNumber number = new FontNumber(row);
			FontStatus status = new FontStatus(row);
			mess.add(number);
			if(version2)
				mess.add(status);
			try {
				mess.queryProps();
			}
			catch(SNMP.Message.NoSuchName e) {
				// Note: some vendors respond with NoSuchName
				//       if the font is not valid
				return populateNum2Row();
			}
			DMS_LOG.log(dms.getName() + ": " + number);
			if(version2)
				DMS_LOG.log(dms.getName() + ": " + status);
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
		// The f_nums linked list is needed to avoid a
		// ConcurrentModificationException on num_2_row TreeMap
		LinkedList<Integer> f_nums = new LinkedList<Integer>();
		f_nums.addAll(num_2_row.keySet());
		for(Integer f_num: f_nums)
			populateNum2Row(f_num);
		return nextFontPhase();
	}

	/** Populate one font number in mapping */
	private void populateNum2Row(Integer f_num) {
		if(num_2_row.get(f_num) == null) {
			Integer r = open_rows.pollLast();
			if(r != null)
				num_2_row.put(f_num, r);
			else
				num_2_row.remove(f_num);
		}
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
		protected Phase poll(CommMessage mess) throws IOException {
			FontVersionID version = new FontVersionID(row);
			mess.add(version);
			try {
				mess.queryProps();
			}
			catch(SNMP.Message.NoSuchName e) {
				// Note: some vendors respond with NoSuchName
				//       if the font is not valid
				version.setInteger(-1);
			}
			int v = version.getInteger();
			DMS_LOG.log(dms.getName() + ": " + version);
			if(isVersionIDCorrect(v)) {
				DMS_LOG.log(dms.getName() + ": Font is valid");
				if(font == dms.getDefaultFont())
					return new SetDefaultFont();
				else
					return nextFontPhase();
			} else {
				if(version2)
					return new QueryInitialStatus();
				else
					return new InvalidateFont();
			}
		}
	}

	/** Compare the font version ID */
	protected boolean isVersionIDCorrect(int v) throws IOException {
		FontVersionByteStream fv = new FontVersionByteStream(font);
		int crc = fv.getCrc() ^ CRC16.INITIAL_CRC;
		int vid = ((crc & 0xFF) << 8) | ((crc >> 8) & 0xFF);
		if(v == vid)
			return true;
		else
			return v == font.getVersionID();
	}

	/** Phase to query the initial font status */
	protected class QueryInitialStatus extends Phase {

		/** Query the initial font status */
		protected Phase poll(CommMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			mess.add(status);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + status);
			switch(status.getEnum()) {
			case notUsed:
				return new RequestStatusModify();
			case modifying:
			case calculatingID:
			case readyForUse:
			case unmanaged:
				return new RequestStatusNotUsed();
			default:
				DMS_LOG.log(dms.getName() + ": font aborted");
				return nextFontPhase();
			}
		}
	}

	/** Phase to request the font status be "notUsed" */
	protected class RequestStatusNotUsed extends Phase {

		/** Request the font status be "notUsed" */
		protected Phase poll(CommMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			status.setEnum(FontStatus.Enum.notUsedReq);
			mess.add(status);
			DMS_LOG.log(dms.getName() + ":= " + status);
			mess.storeProps();
			return new VerifyStatusNotUsed();
		}
	}

	/** Phase to verify the font status is "notUsed" */
	protected class VerifyStatusNotUsed extends Phase {

		/** Verify the font status is "notUsed" */
		protected Phase poll(CommMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			mess.add(status);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + status);
			if(status.getEnum() != FontStatus.Enum.notUsed) {
				DMS_LOG.log(dms.getName() + ": font aborted");
				return nextFontPhase();
			}
			return new RequestStatusModify();
		}
	}

	/** Phase to request the font status to "modifying" */
	protected class RequestStatusModify extends Phase {

		/** Set the font status to modifying */
		protected Phase poll(CommMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			status.setEnum(FontStatus.Enum.modifyReq);
			mess.add(status);
			DMS_LOG.log(dms.getName() + ":= " + status);
			mess.storeProps();
			return new VerifyStatusModifying();
		}
	}

	/** Phase to verify the font status is modifying */
	protected class VerifyStatusModifying extends Phase {

		/** Verify the font status is modifying */
		protected Phase poll(CommMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			mess.add(status);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + status);
			if(status.getEnum() != FontStatus.Enum.modifying) {
				DMS_LOG.log(dms.getName() + ": font aborted");
				return nextFontPhase();
			}
			return new InvalidateFont();
		}
	}

	/** Invalidate the font */
	protected class InvalidateFont extends Phase {

		/** Invalidate a font entry in the font table */
		protected Phase poll(CommMessage mess) throws IOException {
			FontHeight height = new FontHeight(row);
			mess.add(height);
			DMS_LOG.log(dms.getName() + ":= " + height);
			try {
				mess.storeProps();
			}
			catch(SNMP.Message.GenError e) {
				// Some vendors (Skyline) respond with GenError
				// if the font is not currently valid
			}
			return new CreateFont();
		}
	}

	/** Create the font */
	protected class CreateFont extends Phase {

		/** Create a new font in the font table */
		protected Phase poll(CommMessage mess) throws IOException {
			FontNumber number = new FontNumber(row);
			FontName name = new FontName(row);
			FontHeight height = new FontHeight(row);
			FontCharSpacing char_spacing = new FontCharSpacing(row);
			FontLineSpacing line_spacing = new FontLineSpacing(row);
			number.setInteger(font.getNumber());
			name.setString(font.getName());
			height.setInteger(font.getHeight());
			char_spacing.setInteger(font.getCharSpacing());
			line_spacing.setInteger(font.getLineSpacing());
			mess.add(number);
			mess.add(name);
			mess.add(height);
			mess.add(char_spacing);
			mess.add(line_spacing);
			DMS_LOG.log(dms.getName() + ":= " + number);
			DMS_LOG.log(dms.getName() + ":= " + name);
			DMS_LOG.log(dms.getName() + ":= " + height);
			DMS_LOG.log(dms.getName() + ":= " + char_spacing);
			DMS_LOG.log(dms.getName() + ":= " + line_spacing);
			mess.storeProps();
			Collection<Glyph> glyphs =FontHelper.lookupGlyphs(font);
			if(glyphs.isEmpty()) {
				if(version2)
					return new ValidateFontV2();
				else
					return new ValidateFontV1();
			} else
				return new AddCharacter(glyphs);
		}
	}

	/** Add a character to the font table */
	protected class AddCharacter extends Phase {

		/** Iterator for remaining glyphs */
		protected final Iterator<Glyph> chars;

		/** Current glyph */
		protected Glyph glyph;

		/** Count of characters added */
		protected int count = 0;

		/** Create a new add character phase */
		public AddCharacter(Collection<Glyph> c) {
			chars = c.iterator();
			if(chars.hasNext())
				glyph = chars.next();
		}

		/** Add a character to the font table */
		protected Phase poll(CommMessage mess) throws IOException {
			int code_point = glyph.getCodePoint();
			Graphic graphic = glyph.getGraphic();
			byte[] pixels = Base64.decode(graphic.getPixels());
			CharacterWidth char_width = new CharacterWidth(row,
				code_point);
			CharacterBitmap char_bitmap = new CharacterBitmap(row,
				code_point);
			char_width.setInteger(graphic.getWidth());
			char_bitmap.setOctetString(pixels);
			mess.add(char_width);
			mess.add(char_bitmap);
			DMS_LOG.log(dms.getName() + ":= " + char_width);
			DMS_LOG.log(dms.getName() + ":= " + char_bitmap);
			mess.storeProps();
			count++;
			if(count % 20 == 0 && !controller.isFailed())
				errorCounter = 0;
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
		protected Phase poll(CommMessage mess) throws IOException {
			FontHeight height = new FontHeight(row);
			height.setInteger(font.getHeight());
			mess.add(height);
			DMS_LOG.log(dms.getName() + ":= " + height);
			mess.storeProps();
			if(font == dms.getDefaultFont())
				return new SetDefaultFont();
			else
				return nextFontPhase();
		}
	}

	/** Validate the font on a 1203 version 2 sign. */
	protected class ValidateFontV2 extends Phase {

		/** Validate a font entry in the font table */
		protected Phase poll(CommMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			status.setEnum(FontStatus.Enum.readyForUseReq);
			mess.add(status);
			DMS_LOG.log(dms.getName() + ":= " + status);
			mess.storeProps();
			return new VerifyStatusReadyForUse();
		}
	}

	/** Phase to verify the font status is ready for use */
	protected class VerifyStatusReadyForUse extends Phase {

		/** Time to stop checking if the font is ready for use */
		protected final long expire = TimeSteward.currentTimeMillis() + 
			15 * 1000;

		/** Verify the font status is ready for use */
		protected Phase poll(CommMessage mess) throws IOException {
			FontStatus status = new FontStatus(row);
			mess.add(status);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + status);
			switch(status.getEnum()) {
			case readyForUse:
				if(font == dms.getDefaultFont())
					return new SetDefaultFont();
				else
					return nextFontPhase();
			case calculatingID:
				if(TimeSteward.currentTimeMillis() > expire) {
					DMS_LOG.log(dms.getName() + ": font " +
					"status timeout expired -- aborted");
					return nextFontPhase();
				} else
					return this;
			default:
				DMS_LOG.log(dms.getName() + ": font status " +
					"unexpected -- aborted");
				return nextFontPhase();
			}
		}
	}

	/** Set the default font number for message text */
	protected class SetDefaultFont extends Phase {

		/** Set the default font numbmer */
		protected Phase poll(CommMessage mess) throws IOException {
			DefaultFont dfont = new DefaultFont();
			dfont.setInteger(font.getNumber());
			mess.add(dfont);
			DMS_LOG.log(dms.getName() + ":= " + dfont);
			mess.storeProps();
			return nextFontPhase();
		}
	}
}
