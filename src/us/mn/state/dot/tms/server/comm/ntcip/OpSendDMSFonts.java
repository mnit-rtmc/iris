/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.server.comm.snmp.GenError;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;
import us.mn.state.dot.tms.utils.Base64;

/**
 * Operation to send a set of fonts to a DMS controller.
 *
 * @author Douglas Lau
 */
public class OpSendDMSFonts extends OpDMS {

	/** Make a font status object */
	static private ASN1Enum<FontStatus> makeStatus(int row) {
		return new ASN1Enum<FontStatus>(FontStatus.class,
			fontStatus.node, row);
	}

	/** Number of fonts supported */
	private final ASN1Integer num_fonts = numFonts.makeInt();

	/** Maximum number of characters in a font */
	private final ASN1Integer max_characters = maxFontCharacters.makeInt();

	/** Mapping of font numbers to row in font table */
	private final TreeMap<Integer, Integer> num_2_row =
		new TreeMap<Integer, Integer>();

	/** Set of open rows in the font table */
	private final TreeSet<Integer> open_rows = new TreeSet<Integer>();

	/** Iterator of fonts to be sent to the sign */
	private final Iterator<Font> font_iterator;

	/** Current font */
	private Font font;

	/** Current row in font table */
	private int row;

	/** Flag for version 2 controller (with support for fontStatus) */
	private boolean version2;

	/** Create a new operation to send fonts to a DMS */
	public OpSendDMSFonts(DMSImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
		FontFinder ff = new FontFinder(d);
		LinkedList<Font> fonts = ff.getFonts();
		for (Font f: fonts)
			num_2_row.put(f.getNumber(), null);
		font_iterator = fonts.iterator();
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new Query1203Version();
	}

	/** Phase to determine the version of NTCIP 1203 (1 or 2) */
	protected class Query1203Version extends Phase {

		/** Query the maximum character size (v2 only) */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer max_char = fontMaxCharacterSize.makeInt();
			mess.add(max_char);
			try {
				mess.queryProps();
				logQuery(max_char);
				version2 = true;
			}
			catch (NoSuchName e) {
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
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(num_fonts);
			mess.add(max_characters);
			mess.queryProps();
			logQuery(num_fonts);
			logQuery(max_characters);
			for (row = 1; row <= num_fonts.getInteger(); row++)
				open_rows.add(row);
			row = 1;
			return new QueryFontNumbers();
		}
	}

	/** Phase to query all font numbers */
	protected class QueryFontNumbers extends Phase {

		/** Query the font number for one font */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer number = fontNumber.makeInt(row);
			ASN1Enum<FontStatus> status = makeStatus(row);
			mess.add(number);
			if (version2)
				mess.add(status);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Note: some vendors respond with NoSuchName
				//       if the font is not valid
				populateNum2Row();
				return nextFontPhase();
			}
			logQuery(number);
			if (version2)
				logQuery(status);
			else
				status.setEnum(FontStatus.unmanaged);
			updateRow(number.getInteger(), status.getEnum());
			if (row < num_fonts.getInteger()) {
				row++;
				return this;
			} else {
				populateNum2Row();
				return nextFontPhase();
			}
		}
	}

	/** Update one row of the font table */
	private void updateRow(Integer f_num, FontStatus status) {
		if (num_2_row.containsKey(f_num)) {
			if (isUpdatable(status))
				num_2_row.put(f_num, row);
			open_rows.remove(row);
		}
	}

	/** Check if the font can be updated */
	private boolean isUpdatable(FontStatus status) {
		switch (status) {
		case notUsed:
		case modifying:
		case readyForUse:
		case unmanaged:
			return true;
		default:
			return false;
		}
	}

	/** Populate the num_2_row mapping */
	private void populateNum2Row() {
		// The f_nums linked list is needed to avoid a
		// ConcurrentModificationException on num_2_row TreeMap
		LinkedList<Integer> f_nums = new LinkedList<Integer>();
		f_nums.addAll(num_2_row.keySet());
		for (Integer f_num: f_nums)
			populateNum2Row(f_num);
	}

	/** Populate one font number in mapping */
	private void populateNum2Row(Integer f_num) {
		if (num_2_row.get(f_num) == null) {
			Integer r = open_rows.pollLast();
			if (r != null)
				num_2_row.put(f_num, r);
			else
				num_2_row.remove(f_num);
		}
	}

	/** Get the first phase of the next font */
	protected Phase nextFontPhase() {
		while (font_iterator.hasNext()) {
			font = font_iterator.next();
			Integer f_num = font.getNumber();
			if (num_2_row.containsKey(f_num)) {
				row = num_2_row.get(f_num);
				return new VerifyFont();
			}
			abortUpload("Table full");
		}
		return null;
	}

	/** Abort upload of the current font */
	private void abortUpload(String msg) {
		Font f = font;
		if (f != null) {
			String s = "Font " + f.getNumber() + " aborted -- "+msg;
			setErrorStatus(s);
		}
	}

	/** Phase to verify a font */
	protected class VerifyFont extends Phase {

		/** Verify a font */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer version = fontVersionID.makeInt(row);
			mess.add(version);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Note: some vendors respond with NoSuchName
				//       if the font is not valid
				version.setInteger(-1);
			}
			int v = version.getInteger();
			logQuery(version);
			if (isVersionIDCorrect(v)) {
				logError("Font is valid");
				if (font == dms.getDefaultFont())
					return new SetDefaultFont();
				else
					return nextFontPhase();
			} else {
				if (version2)
					return new QueryInitialStatus();
				else
					return new InvalidateFont();
			}
		}
	}

	/** Compare the font version ID */
	private boolean isVersionIDCorrect(int v) throws IOException {
		return isManualVersionIDCorrect(v) || isAutoVersionIDCorrect(v);
	}

	/** Check if a font version ID matches the manually specified ID */
	private boolean isManualVersionIDCorrect(int v) {
		int fvid = font.getVersionID();
		return fvid != 0 && v == fvid;
	}

	/** Check if a font version ID matches the automatic ID */
	private boolean isAutoVersionIDCorrect(int v) throws IOException {
		FontVersionByteStream fv = new FontVersionByteStream(font);
		return v == fv.getCrcSwapped();
	}

	/** Phase to query the initial font status */
	protected class QueryInitialStatus extends Phase {

		/** Query the initial font status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			switch (status.getEnum()) {
			case notUsed:
				return new RequestStatusModify();
			case modifying:
			case calculatingID:
			case readyForUse:
			case unmanaged:
				return new RequestStatusNotUsed();
			default:
				abortUpload("Initial status: " +
					status.getEnum());
				return nextFontPhase();
			}
		}
	}

	/** Phase to request the font status be "notUsed" */
	protected class RequestStatusNotUsed extends Phase {

		/** Request the font status be "notUsed" */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(row);
			status.setEnum(FontStatus.notUsedReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyStatusNotUsed();
		}
	}

	/** Phase to verify the font status is "notUsed" */
	protected class VerifyStatusNotUsed extends Phase {

		/** Verify the font status is "notUsed" */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() != FontStatus.notUsed) {
				abortUpload("Expected notUsed, was "
					+ status.getEnum());
				return nextFontPhase();
			}
			return new RequestStatusModify();
		}
	}

	/** Phase to request the font status to "modifying" */
	protected class RequestStatusModify extends Phase {

		/** Set the font status to modifying */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(row);
			status.setEnum(FontStatus.modifyReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyStatusModifying();
		}
	}

	/** Phase to verify the font status is modifying */
	protected class VerifyStatusModifying extends Phase {

		/** Verify the font status is modifying */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() == FontStatus.modifyReq) {
				// Daktronics DMS returns modifyReq
				// instead of modifyting here ...
				logError("invalid state: modifyReq");
				return nextFontPhase();
			}
			if (status.getEnum() != FontStatus.modifying) {
				abortUpload("Expected modifying, was " +
					status.getEnum());
				return nextFontPhase();
			}
			return new InvalidateFont();
		}
	}

	/** Invalidate the font */
	protected class InvalidateFont extends Phase {

		/** Invalidate a font entry in the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer height = fontHeight.makeInt(row);
			mess.add(height);
			logStore(height);
			try {
				mess.storeProps();
			}
			catch (GenError e) {
				// Some vendors (Skyline) respond with GenError
				// if the font is not currently valid
			}
			return new CreateFont();
		}
	}

	/** Create the font */
	protected class CreateFont extends Phase {

		/** Create a new font in the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer number = fontNumber.makeInt(row);
			ASN1String name = new ASN1String(fontName.node, row);
			ASN1Integer height = fontHeight.makeInt(row);
			ASN1Integer char_spacing = fontCharSpacing.makeInt(row);
			ASN1Integer line_spacing = fontLineSpacing.makeInt(row);
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
			logStore(number);
			logStore(name);
			logStore(height);
			logStore(char_spacing);
			logStore(line_spacing);
			mess.storeProps();
			Collection<Glyph> glyphs =FontHelper.lookupGlyphs(font);
			if (glyphs.isEmpty()) {
				if (version2)
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
			if (chars.hasNext())
				glyph = chars.next();
		}

		/** Add a character to the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			int code_point = glyph.getCodePoint();
			Graphic graphic = glyph.getGraphic();
			byte[] pixels = Base64.decode(graphic.getPixels());
			ASN1Integer char_width = characterWidth.makeInt(row,
				code_point);
			ASN1OctetString char_bitmap = new ASN1OctetString(
				characterBitmap.node, row, code_point);
			char_width.setInteger(graphic.getWidth());
			char_bitmap.setOctetString(pixels);
			mess.add(char_width);
			mess.add(char_bitmap);
			logStore(char_width);
			logStore(char_bitmap);
			mess.storeProps();
			count++;
			if (count % 20 == 0 && !controller.isFailed())
				setSuccess(true);
			if (chars.hasNext()) {
				glyph = chars.next();
				return this;
			} else {
				if (version2)
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
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer height = fontHeight.makeInt(row);
			height.setInteger(font.getHeight());
			mess.add(height);
			logStore(height);
			mess.storeProps();
			if (font == dms.getDefaultFont())
				return new SetDefaultFont();
			else
				return nextFontPhase();
		}
	}

	/** Validate the font on a 1203 version 2 sign. */
	protected class ValidateFontV2 extends Phase {

		/** Validate a font entry in the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(row);
			status.setEnum(FontStatus.readyForUseReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyStatusReadyForUse();
		}
	}

	/** Phase to verify the font status is ready for use */
	protected class VerifyStatusReadyForUse extends Phase {

		/** Time in seconds to allow for calculating font ID */
		static private final int CALCULATING_ID_SECS = 15;

		/** Time to stop checking if the font is ready for use */
		private final long expire = TimeSteward.currentTimeMillis() + 
			CALCULATING_ID_SECS * 1000;

		/** Verify the font status is ready for use */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			switch (status.getEnum()) {
			case readyForUse:
				if (font == dms.getDefaultFont())
					return new SetDefaultFont();
				else
					return nextFontPhase();
			case calculatingID:
				if (TimeSteward.currentTimeMillis() > expire) {
					abortUpload("Still calculatingID, " +
						CALCULATING_ID_SECS +" seconds"+
						" after readyForUseReq");
					return nextFontPhase();
				} else
					return this;
			default:
				abortUpload("Invalid state readyForUseReq -> " +
					status.getEnum());
				return nextFontPhase();
			}
		}
	}

	/** Set the default font number for message text */
	protected class SetDefaultFont extends Phase {

		/** Set the default font numbmer */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer dfont = defaultFont.makeInt();
			dfont.setInteger(font.getNumber());
			mess.add(dfont);
			logStore(dfont);
			mess.storeProps();
			return nextFontPhase();
		}
	}
}
