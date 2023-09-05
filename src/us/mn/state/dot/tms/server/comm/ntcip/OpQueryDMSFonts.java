/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2023  Minnesota Department of Transportation
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

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;
import us.mn.state.dot.tms.utils.DevelCfg;

/**
 * Operation to query all fonts on a DMS controller.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSFonts extends OpDMS {

	/** Make a font status object */
	static private ASN1Enum<FontStatus> makeStatus(int row) {
		return new ASN1Enum<FontStatus>(FontStatus.class,
			fontStatus.node, row);
	}

	/** Directory to store font files */
	static private final String FONT_FILE_DIR = 
		DevelCfg.get("font.output.dir", "/var/lib/iris/fonts/");

	/** Create a writer for a font file */
	private PrintWriter createWriter(String f) throws IOException {
		File file = new File(dir, f);
		return new PrintWriter(file);
	}

	/** Number of fonts supported */
	private final ASN1Integer num_fonts = numFonts.makeInt();

	/** Maximum number of characters in a font */
	private final ASN1Integer max_characters = maxFontCharacters.makeInt();

	/** Maximum character size */
	private final ASN1Integer max_char_sz = fontMaxCharacterSize.makeInt();

	/** Directory to write font files */
	private final File dir;

	/** Flag for version 2 or later (with support for fontStatus) */
	private boolean version2;

	/** Create a new operation to query fonts from a DMS */
	public OpQueryDMSFonts(DMSImpl d) {
		super(PriorityLevel.POLL_LOW, d);
		dir = new File(FONT_FILE_DIR, d.getName());
		if (!dir.exists())
			dir.mkdirs();
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new Query1203Version();
	}

	/** Phase to determine if v2 or greater */
	private class Query1203Version extends Phase {

		/** Query the maximum character size (v2 only) */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(max_char_sz);
			try {
				mess.queryProps();
				logQuery(max_char_sz);
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
			writeLimits();
			return nextFont(0);
		}
	}

	/** Write the font limits */
	private void writeLimits() throws IOException {
		PrintWriter writer = createWriter("limits.txt");
		writer.println("numFonts: " + num_fonts);
		writer.println("maxFontCharacters: " + max_characters);
		writer.println("fontMaxCharacterSize: " + max_char_sz);
		writer.flush();
		writer.close();
	}

	/** Get phase to query the next font */
	private Phase nextFont(int r) throws IOException {
		return (r < num_fonts.getInteger())
		      ? new QueryFont(r + 1)
		      : null;
	}

	/** Phase to query one row of font table */
	private class QueryFont extends Phase {

		/** Row to query */
		private final int row;

		private final ASN1Integer number;
		private final DisplayString name;
		private final ASN1Integer height;
		private final ASN1Integer char_spacing;
		private final ASN1Integer line_spacing;
		private final ASN1Integer version;
		private final ASN1Enum<FontStatus> status;

		/** Create a query font phase */
		private QueryFont(int r) throws IOException {
			row = r;
			number = fontNumber.makeInt(row);
			name = new DisplayString(fontName.node, row);
			height = fontHeight.makeInt(row);
			char_spacing = fontCharSpacing.makeInt(row);
			line_spacing = fontLineSpacing.makeInt(row);
			version = fontVersionID.makeInt(row);
			status = makeStatus(row);
			status.setEnum(FontStatus.unmanaged);
		}

		/** Query one row in font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(number);
			mess.add(name);
			mess.add(height);
			mess.add(char_spacing);
			mess.add(line_spacing);
			mess.add(version);
			if (version2)
				mess.add(status);
			try {
				mess.queryProps();
				logQuery(number);
				logQuery(name);
				logQuery(height);
				logQuery(char_spacing);
				logQuery(line_spacing);
				logQuery(version);
				logQuery(status);
			}
			catch (NoSuchName e) {
				// Note: some vendors respond with NoSuchName
				//       if the font is not valid
				return nextFont(row);
			}
			return isValid() ? writeHeader() : nextFont(row);
		}

		/** Check if font is valid */
		private boolean isValid() {
			return status.getEnum().isValid()
			    && height.getInteger() > 0;
		}

		/** Create font file name */
		private String fileName() {
			if (status.getEnum() == FontStatus.permanent)
				return "f" + number + "-perm.ifnt";
			else
				return "f" + number + ".ifnt";
		}

		/** Write the font header */
		private Phase writeHeader() throws IOException {
			PrintWriter writer = createWriter(fileName());
			writer.println("name: " + name);
			writer.println("font_number: " + number);
			writer.println("height: " + height);
			writer.println("width: 0");
			writer.println("line_spacing: " + line_spacing);
			writer.println("char_spacing: " + char_spacing);
			return nextCharacter(writer, height.getInteger(),
				row, 0);
		}
	}

	/** Get phase to query the next character in a font */
	private Phase nextCharacter(PrintWriter writer, int height, int r,
		int cr) throws IOException
	{
		if (cr < max_characters.getInteger())
			return new QueryCharacter(writer, height, r, cr + 1);
		else {
			writer.flush();
			writer.close();
			return nextFont(r);
		}
	}

	/** Phase to query one character */
	private class QueryCharacter extends Phase {
		private final PrintWriter writer;
		private final int height;
		private final int row;
		private final int crow;
		private final ASN1Integer char_width;
		private final ASN1OctetString char_bitmap;

		/** Create a new add character phase */
		public QueryCharacter(PrintWriter w, int h, int r, int cr) {
			writer = w;
			height = h;
			row = r;
			crow = cr;
			char_width = characterWidth.makeInt(row,
				crow);
			char_bitmap = new ASN1OctetString(
				characterBitmap.node, row, crow);
		}

		/** Add a character to the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(char_width);
			mess.add(char_bitmap);
			mess.queryProps();
			logQuery(char_width);
			logQuery(char_bitmap);
			if (char_width.getInteger() > 0)
				writeChar();
			return nextCharacter(writer, height, row, crow);
		}

		/** Write character data */
		private void writeChar() throws IOException {
			writer.println();
			writer.println("codepoint: " + crow + ' ' +
				(char) crow);
			int width = char_width.getInteger();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (isPixelLit(x, y))
						writer.print('X');
					else
						writer.print('.');
				}
				writer.println();
			}
		}

		/** Check if a pixel is lit */
		private boolean isPixelLit(int x, int y) {
			int pos = y * char_width.getInteger() + x;
			int off = pos / 8;
			int bit = 7 - (pos & 7); // 0b0111
			byte[] bitmap = char_bitmap.getByteValue();
			return (bitmap[off] >> bit & 1) != 0;
		}
	}
}
