/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.Base64;

/**
 * Operation to query all fonts on a DMS controller.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSFonts extends OpDMS {

	/** Directory to store font files */
	static private final String FONT_FILE_DIR = "/var/log/iris/";

	/** Maximum character size */
	private final ASN1Integer max_char_sz = fontMaxCharacterSize.makeInt();

	/** Number of fonts supported */
	private final ASN1Integer num_fonts = numFonts.makeInt();

	/** Maximum number of characters in a font */
	private final ASN1Integer max_characters = maxFontCharacters.makeInt();

	/** Writer for font file */
	private final PrintWriter writer;

	/** Create a new operation to query fonts from a DMS */
	public OpQueryDMSFonts(DMSImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
		writer = createWriter();
	}

	/** Create a writer for the font file */
	private PrintWriter createWriter() {
		String f = "fonts-" + dms.getName() + ".sql";
		File file = new File(FONT_FILE_DIR, f);
		try {
			return new PrintWriter(file);
		}
		catch (IOException e) {
			logError("createWriter: " + e.getMessage());
			return null;
		}
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return (writer != null) ? new Query1203Version() : null;
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
			}
			catch (NoSuchName e) {
				// Note: if this object doesn't exist, then the
				//       sign must not support v2.
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
			write_header();
			return nextFont(0);
		}
	}

	/** Write the font file header */
	private void write_header() {
		writer.println("\\set ON_ERROR_STOP");
		writer.println("SET SESSION AUTHORIZATION 'tms';");
		writer.println("BEGIN;");
		writer.println();
		writer.println("-- " + max_char_sz);
		writer.println("-- " + max_characters);
		writer.println("-- " + num_fonts);
		writer.println();
	}

	/** Get phase to query the next font */
	private Phase nextFont(int r) {
		if (r < num_fonts.getInteger())
			return new QueryFont(r + 1);
		else
			return null;
	}

	/** Phase to query one row of font table */
	private class QueryFont extends Phase {

		/** Row to query */
		private final int row;

		/** Create a query font phase */
		private QueryFont(int r) {
			row = r;
		}

		/** Query the font number for one row in font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer number = fontNumber.makeInt(row);
			DisplayString name = new DisplayString(fontName.node,
				row);
			ASN1Integer height = fontHeight.makeInt(row);
			ASN1Integer char_spacing = fontCharSpacing.makeInt(row);
			ASN1Integer line_spacing = fontLineSpacing.makeInt(row);
			ASN1Integer version = fontVersionID.makeInt(row);
			mess.add(number);
			mess.add(name);
			mess.add(height);
			mess.add(char_spacing);
			mess.add(line_spacing);
			mess.add(version);
			try {
				mess.queryProps();
				logQuery(number);
				logQuery(name);
				logQuery(height);
				logQuery(char_spacing);
				logQuery(line_spacing);
				logQuery(version);
			}
			catch (NoSuchName e) {
				// Note: some vendors respond with NoSuchName
				//       if the font is not valid
				return nextFont(row);
			}
			if (height.getInteger() > 0) {
				write_font(name.getValue(), number.getInteger(),
				           height.getInteger(),
				           line_spacing.getInteger(),
				           char_spacing.getInteger(),
				           version.getInteger());
				return nextCharacter(name.getValue(), row, 0);
			} else
				return nextFont(row);
		}
	}

	/** Write the font data */
	private void write_font(String name, int number, int height,
		int line_spacing, int char_spacing, int version)
	{
		writer.println("INSERT INTO iris.font (name, f_number, " +
			"height, width, line_spacing,");
		writer.println("char_spacing, version_id) VALUES ('" +
		               name + "', " +
		               number + ", " +
		               height + ", " +
		               "0, " + // font width
		               line_spacing + ", " +
		               char_spacing + ", " +
		               version + ");");
		writer.println();
		writer.println("COPY iris.glyph (name, font, code_point, " +
			"width, pixels) FROM stdin;");
	}

	/** Get phase to query the next character in a font */
	private Phase nextCharacter(String name, int r, int cr) {
		if (cr < max_characters.getInteger())
			return new QueryCharacter(name, r, cr + 1);
		else {
			write_font_done();
			return nextFont(r);
		}
	}

	/** Write end font */
	private void write_font_done() {
		writer.println("\\.");
		writer.println();
	}

	/** Phase to query one character */
	private class QueryCharacter extends Phase {

		/** Font name */
		private final String name;

		/** Font row */
		private final int row;

		/** Character row */
		private final int crow;

		/** Create a new add character phase */
		public QueryCharacter(String n, int r, int cr) {
			name = n;
			row = r;
			crow = cr;
		}

		/** Add a character to the font table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer char_width = characterWidth.makeInt(row,
				crow);
			ASN1OctetString char_bitmap = new ASN1OctetString(
				characterBitmap.node, row, crow);
			mess.add(char_width);
			mess.add(char_bitmap);
			mess.queryProps();
			logQuery(char_width);
			logQuery(char_bitmap);
			if (char_width.getInteger() > 0) {
				write_char(name, crow, char_width.getInteger(),
				           char_bitmap);
			}
			return nextCharacter(name, row, crow);
		}
	}

	/** Write character data */
	private void write_char(String name, int crow, int width,
		ASN1OctetString bitmap)
	{
		String bmap = Base64.encode(bitmap.getByteValue());
		String cname = name + "_" + crow;
		writer.println(cname + '\t' +
		               name + '\t' +
		               crow + '\t' +
		               width + '\t' +
		               bmap.replace("\n", "\\n"));
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		super.cleanup();
		if (writer != null) {
			writer.println("COMMIT;");
			writer.flush();
			writer.close();
		}
	}
}
