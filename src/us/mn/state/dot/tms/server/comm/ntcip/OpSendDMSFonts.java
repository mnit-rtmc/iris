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
import java.util.Map;
import java.util.TreeMap;
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

	/** Font row values */
	static private class FontRow {
		private final int row;
		private final int f_num;
		private final Font font;

		/** Create a new font row */
		private FontRow(int r, int fn, Font f) {
			row = r;
			f_num = fn;
			font = f;
		}

		/** Create a new font row */
		private FontRow(Font f) {
			row = 0;
			f_num = f.getNumber();
			font = f;
		}

		/** Check if the row is valid */
		private boolean isValid() {
			return font != null;
		}
	}

	/** Make a font status object */
	static private ASN1Enum<FontStatus> makeStatus(int row) {
		return new ASN1Enum<FontStatus>(FontStatus.class,
			fontStatus.node, row);
	}

	/** Time in seconds to allow for verifying font status */
	static private final int VERIFY_STATUS_SECS = 5;

	/** Time in seconds to allow for calculating font ID */
	static private final int CALCULATING_ID_SECS = 15;

	/** Number of fonts supported */
	private final ASN1Integer num_fonts = numFonts.makeInt();

	/** Maximum number of characters in a font */
	private final ASN1Integer max_characters = maxFontCharacters.makeInt();

	/** List of matching fonts */
	private final LinkedList<Font> fonts;

	/** Mapping of rows to font values in font table */
	private final TreeMap<Integer, FontRow> rows =
		new TreeMap<Integer, FontRow>();

	/** Flag for version 2 controller (with support for fontStatus) */
	private boolean version2;

	/** Create a new operation to send fonts to a DMS */
	public OpSendDMSFonts(DMSImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
		FontFinder ff = new FontFinder(d);
		fonts = ff.getFonts();
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new Query1203Version();
	}

	/** Phase to determine the version of NTCIP 1203 (1 or 2) */
	private class Query1203Version extends Phase {

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
	private class QueryNumFonts extends Phase {

		/** Query the number of supported fonts */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(num_fonts);
			mess.add(max_characters);
			mess.queryProps();
			logQuery(num_fonts);
			logQuery(max_characters);
			return new QueryFontNumbers();
		}
	}

	/** Phase to query all font numbers */
	private class QueryFontNumbers extends Phase {

		/** Row to query */
		private int row = 1;

		/** Query the font number for one row in font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer number = fontNumber.makeInt(row);
			mess.add(number);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Note: some vendors respond with NoSuchName
				//       if the font is not valid
				return firstFontPhase();
			}
			logQuery(number);
			addRow(row, number.getInteger());
			if (row < num_fonts.getInteger()) {
				row++;
				return this;
			} else
				return firstFontPhase();
		}
	}

	/** Add a row to font rows mapping.
	 * @param row Row number in font table.
	 * @param f_num Font number in font table. */
	private void addRow(int row, int f_num) {
		rows.put(row, new FontRow(row, f_num, findFont(f_num)));
	}

	/** Find and remove matching font */
	private Font findFont(int f_num) {
		Iterator<Font> it = fonts.iterator();
		while (it.hasNext()) {
			Font f = it.next();
			if (f.getNumber() == f_num) {
				it.remove();
				return f;
			}
		}
		return null;
	}

	/** Get the first phase of the first font */
	private Phase firstFontPhase() {
		populateRows();
		warnTableFull();
		return nextFontPhase();
	}

	/** Populate the rows mapping */
	private void populateRows() {
		// Start at the last row in the table -- some old firmwares
		// treat the first row or two as special permanent fonts.
		for (int row = num_fonts.getInteger(); row > 0; row--) {
			if (fonts.size() < 1)
				break;
			if (rows.containsKey(row))
				rows.put(row, populateRow(rows.remove(row)));
		}
	}

	/** Populate a FontRow with an unassigned font */
	private FontRow populateRow(FontRow fr) {
		assert fonts.size() > 0;
		if (fr.font != null)
			return fr;
		else {
			Font f = fonts.pollFirst();
			return new FontRow(fr.row, fontNum(fr,f), f);
		}
	}

	/** Get the font number for a specified row and font */
	private int fontNum(FontRow fr, Font f) {
		return isAddco() ? fr.f_num : f.getNumber();
	}

	/** Check if DMS make is ADDCO.  Some ADDCO signs flake out if
	 * the font *number* is greater than numFonts (typically 4). */
	private boolean isAddco() {
		String make = dms.getMake();
		return make != null && make.startsWith("ADDCO");
	}

	/** Print warning if unable to send fonts */
	private void warnTableFull() {
		for (Font f : fonts)
			abortUpload(new FontRow(f), "Table full");
	}

	/** Get the first phase of the next font */
	private Phase nextFontPhase() {
		while (true) {
			Map.Entry<Integer, FontRow> ent = rows.pollFirstEntry();
			if (ent != null) {
				FontRow fr = ent.getValue();
				if (fr.isValid())
					return new VerifyFont(fr);
			} else
				break;
		}
		return null;
	}

	/** Abort upload of the current font */
	private void abortUpload(FontRow frow, String msg) {
		setErrorStatus("Font " + frow.font.getName() + " aborted -- " +
		               msg);
	}

	/** Phase to verify a font */
	protected class VerifyFont extends Phase {
		private final FontRow frow;
		private VerifyFont(FontRow fr) {
			frow = fr;
		}

		/** Verify a font */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer version = fontVersionID.makeInt(frow.row);
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
				if (dms.getDefaultFont() == frow.font)
					return new SetDefaultFont(frow);
				else
					return nextFontPhase();
			} else {
				if (version2)
					return new QueryInitialStatus(frow);
				else
					return new InvalidateFont(frow);
			}
		}

		/** Compare the font version ID */
		private boolean isVersionIDCorrect(int v) throws IOException {
			return isManualVersionIDCorrect(v) ||
			       isAutoVersionIDCorrect(v);
		}

		/** Check if font version ID matches manually specified ID */
		private boolean isManualVersionIDCorrect(int v) {
			int fvid = frow.font.getVersionID();
			return fvid != 0 && v == fvid;
		}

		/** Check if font version ID matches the automatic ID */
		private boolean isAutoVersionIDCorrect(int v)
			throws IOException
		{
			FontVersionByteStream fv = new FontVersionByteStream(
				frow.font, frow.f_num);
			return v == fv.getCrcSwapped();
		}
	}

	/** Phase to query the initial font status */
	private class QueryInitialStatus extends Phase {
		private final FontRow frow;
		private QueryInitialStatus(FontRow fr) {
			frow = fr;
		}

		/** Query the initial font status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(frow.row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			switch (status.getEnum()) {
			case notUsed:
				return new RequestStatusModify(frow);
			case modifying:
			case calculatingID:
			case readyForUse:
			case unmanaged:
				return new RequestStatusNotUsed(frow);
			default:
				abortUpload(frow, "Initial status: " +
					status.getEnum());
				return nextFontPhase();
			}
		}
	}

	/** Phase to request the font status be "notUsed" */
	private class RequestStatusNotUsed extends Phase {
		private final FontRow frow;
		private RequestStatusNotUsed(FontRow fr) {
			frow = fr;
		}

		/** Request the font status be "notUsed" */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(frow.row);
			status.setEnum(FontStatus.notUsedReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyStatusNotUsed(frow);
		}
	}

	/** Phase to verify the font status is "notUsed" */
	private class VerifyStatusNotUsed extends Phase {
		private final FontRow frow;
		private VerifyStatusNotUsed(FontRow fr) {
			frow = fr;
		}

		/** Time to stop checking if the status has updated */
		private final long expire = TimeSteward.currentTimeMillis() +
			VERIFY_STATUS_SECS * 1000;

		/** Verify the font status is "notUsed" */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(frow.row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() == FontStatus.notUsedReq) {
				// Daktronics DMS return notUsedReq instead of
				// notUsed for a short time; try again
				if (TimeSteward.currentTimeMillis() < expire)
					return this;
			}
			if (status.getEnum() != FontStatus.notUsed) {
				abortUpload(frow, "Expected notUsed, was "
					+ status.getEnum());
				return nextFontPhase();
			}
			return new RequestStatusModify(frow);
		}
	}

	/** Phase to request the font status to "modifying" */
	private class RequestStatusModify extends Phase {
		private final FontRow frow;
		private RequestStatusModify(FontRow fr) {
			frow = fr;
		}

		/** Set the font status to modifying */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(frow.row);
			status.setEnum(FontStatus.modifyReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyStatusModifying(frow);
		}
	}

	/** Phase to verify the font status is modifying */
	private class VerifyStatusModifying extends Phase {
		private final FontRow frow;
		private VerifyStatusModifying(FontRow fr) {
			frow = fr;
		}

		/** Time to stop checking if the status has updated */
		private final long expire = TimeSteward.currentTimeMillis() +
			VERIFY_STATUS_SECS * 1000;

		/** Verify the font status is modifying */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(frow.row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() == FontStatus.modifyReq) {
				// Daktronics DMS return modifyReq instead of
				// modifying for a short time; try again
				if (TimeSteward.currentTimeMillis() < expire)
					return this;
			}
			if (status.getEnum() != FontStatus.modifying) {
				abortUpload(frow, "Expected modifying, was " +
					status.getEnum());
				return nextFontPhase();
			}
			return new InvalidateFont(frow);
		}
	}

	/** Invalidate the font */
	private class InvalidateFont extends Phase {
		private final FontRow frow;
		private InvalidateFont(FontRow fr) {
			frow = fr;
		}

		/** Invalidate a font entry in the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer height = fontHeight.makeInt(frow.row);
			mess.add(height);
			logStore(height);
			try {
				mess.storeProps();
			}
			catch (GenError e) {
				// Some vendors (Skyline) respond with GenError
				// if the font is not currently valid
			}
			return new CreateFont(frow);
		}
	}

	/** Create the font */
	protected class CreateFont extends Phase {
		private final FontRow frow;
		private CreateFont(FontRow fr) {
			frow = fr;
		}

		/** Create a new font in the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			int row = frow.row;
			ASN1Integer number = fontNumber.makeInt(row);
			ASN1String name = new ASN1String(fontName.node, row);
			ASN1Integer height = fontHeight.makeInt(row);
			ASN1Integer char_spacing = fontCharSpacing.makeInt(row);
			ASN1Integer line_spacing = fontLineSpacing.makeInt(row);
			number.setInteger(frow.f_num);
			name.setString(frow.font.getName());
			height.setInteger(frow.font.getHeight());
			char_spacing.setInteger(frow.font.getCharSpacing());
			line_spacing.setInteger(frow.font.getLineSpacing());
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
			Collection<Glyph> glyphs =
				FontHelper.lookupGlyphs(frow.font);
			if (glyphs.isEmpty()) {
				if (version2)
					return new ValidateFontV2(frow);
				else
					return new ValidateFontV1(frow);
			} else
				return new AddCharacter(frow, glyphs);
		}
	}

	/** Add a character to the font table */
	private class AddCharacter extends Phase {

		private final FontRow frow;

		/** Iterator for remaining glyphs */
		private final Iterator<Glyph> chars;

		/** Current glyph */
		private Glyph glyph;

		/** Count of characters added */
		private int count = 0;

		/** Create a new add character phase */
		public AddCharacter(FontRow fr, Collection<Glyph> c) {
			frow = fr;
			chars = c.iterator();
			if (chars.hasNext())
				glyph = chars.next();
		}

		/** Add a character to the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			int row = frow.row;
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
					return new ValidateFontV2(frow);
				else
					return new ValidateFontV1(frow);
			}
		}
	}

	/** Validate the font. This forces a fontVersionID update on some signs
	 * which implement 1203 version 1 (LedStar). */
	private class ValidateFontV1 extends Phase {
		private final FontRow frow;
		private ValidateFontV1(FontRow fr) {
			frow = fr;
		}

		/** Validate a font entry in the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer height = fontHeight.makeInt(frow.row);
			height.setInteger(frow.font.getHeight());
			mess.add(height);
			logStore(height);
			mess.storeProps();
			if (dms.getDefaultFont() == frow.font)
				return new SetDefaultFont(frow);
			else
				return nextFontPhase();
		}
	}

	/** Validate the font on a 1203 version 2 sign. */
	private class ValidateFontV2 extends Phase {
		private final FontRow frow;
		private ValidateFontV2(FontRow fr) {
			frow = fr;
		}

		/** Validate a font entry in the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(frow.row);
			status.setEnum(FontStatus.readyForUseReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyStatusReadyForUse(frow);
		}
	}

	/** Phase to verify the font status is ready for use */
	private class VerifyStatusReadyForUse extends Phase {
		private final FontRow frow;
		private VerifyStatusReadyForUse(FontRow fr) {
			frow = fr;
		}

		/** Time to stop checking if the font is ready for use */
		private final long expire = TimeSteward.currentTimeMillis() +
			CALCULATING_ID_SECS * 1000;

		/** Verify the font status is ready for use */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<FontStatus> status = makeStatus(frow.row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			switch (status.getEnum()) {
			case readyForUse:
				if (dms.getDefaultFont() == frow.font)
					return new SetDefaultFont(frow);
				else
					return nextFontPhase();
			case readyForUseReq:
				// Daktronics DMS return readyForUseReq instead
				// of calculatingID for a short time; try again
			case calculatingID:
				if (TimeSteward.currentTimeMillis() > expire) {
					abortUpload(frow, "calculatingID, " +
						CALCULATING_ID_SECS +" seconds"+
						" after readyForUseReq");
					return nextFontPhase();
				} else
					return this;
			default:
				abortUpload(frow, "Invalid state " +
					"readyForUseReq -> " +status.getEnum());
				return nextFontPhase();
			}
		}
	}

	/** Set the default font number for message text */
	protected class SetDefaultFont extends Phase {
		private final FontRow frow;
		private SetDefaultFont(FontRow fr) {
			frow = fr;
		}

		/** Set the default font numbmer */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer dfont = defaultFont.makeInt();
			dfont.setInteger(frow.f_num);
			mess.add(dfont);
			logStore(dfont);
			mess.storeProps();
			return nextFontPhase();
		}
	}
}
