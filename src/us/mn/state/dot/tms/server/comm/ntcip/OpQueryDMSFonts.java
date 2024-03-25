/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.HexString;

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
		DevelCfg.get("font.output.dir", "/var/lib/iris/web/tfon");

	/** Symbols for all ASCII + Latin 1 characters */
	static private final String[] SYMBOL = new String[] {
	    "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL", "BS", "HT", "LF",
	    "VT", "FF", "CR", "SO", "SI", "DLE", "DC1", "DC2", "DC3", "DC4", "NAK",
	    "SYN", "ETB", "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US", "SP", "!",
	    "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", "0",
	    "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?",
	    "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
	    "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]",
	    "^", "_", "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
	    "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{",
	    "|", "}", "~", "DEL", "PAD", "HOP", "BPH", "NBH", "IND", "NEL", "SSA",
	    "ESA", "HTS", "HTJ", "LTS", "PLD", "PLU", "RI", "SS2", "SS3", "DCS", "PU1",
	    "PU2", "STS", "CCH", "MW", "SPA", "EPA", "SOS", "SGCI", "SCI", "CSI", "ST",
	    "OSC", "PM", "APC", "NBSP", "¡", "¢", "£", "¤", "¥", "¦", "§", "¨", "©",
	    "ª", "«", "¬", "SHY", "®", "¯", "°", "±", "²", "³", "´", "µ", "¶", "·",
	    "¸", "¹", "º", "»", "¼", "½", "¾", "¿", "À", "Á", "Â", "Ã", "Ä", "Å", "Æ",
	    "Ç", "È", "É", "Ê", "Ë", "Ì", "Í", "Î", "Ï", "Ð", "Ñ", "Ò", "Ó", "Ô", "Õ",
	    "Ö", "×", "Ø", "Ù", "Ú", "Û", "Ü", "Ý", "Þ", "ß", "à", "á", "â", "ã", "ä",
	    "å", "æ", "ç", "è", "é", "ê", "ë", "ì", "í", "î", "ï", "ð", "ñ", "ò", "ó",
	    "ô", "õ", "ö", "÷", "ø", "ù", "ú", "û", "ü", "ý", "þ", "ÿ"
	};

	/** Get a symbol for a character */
	static private String symbol(int crow) {
		return (crow >= 0 && crow < 256) ? SYMBOL[crow] : "";
	}

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
		writer.println(num_fonts);
		writer.println(max_characters);
		writer.println(max_char_sz);
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
		private final ASN1Integer version_id;
		private final ASN1Enum<FontStatus> status;

		/** Create a query font phase */
		private QueryFont(int r) throws IOException {
			row = r;
			number = fontNumber.makeInt(row);
			name = new DisplayString(fontName.node, row);
			height = fontHeight.makeInt(row);
			char_spacing = fontCharSpacing.makeInt(row);
			line_spacing = fontLineSpacing.makeInt(row);
			version_id = fontVersionID.makeInt(row);
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
			mess.add(version_id);
			if (version2)
				mess.add(status);
			try {
				mess.queryProps();
				logQuery(number);
				logQuery(name);
				logQuery(height);
				logQuery(char_spacing);
				logQuery(line_spacing);
				logQuery(version_id);
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
			int num = number.getInteger();
			StringBuilder nm = new StringBuilder();
			nm.append('F');
			if (num < 10)
				nm.append('0');
			nm.append(num);
			nm.append('-');
			nm.append(HexString.format(version_id.getInteger(), 4));
			if (status.getEnum() == FontStatus.permanent)
				nm.append("-perm");
			nm.append(".tfon");
			return nm.toString();
		}

		/** Write the font header */
		private Phase writeHeader() throws IOException {
			PrintWriter writer = createWriter(fileName());
			writer.println("font_name: " + name.getValue());
			writer.println("font_number: " + number.getInteger());
			writer.println("char_spacing: " +
				char_spacing.getInteger());
			writer.println("line_spacing: " +
				line_spacing.getInteger());
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
			writer.println("ch: " + crow + ' ' + symbol(crow));
			int width = char_width.getInteger();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (isPixelLit(x, y))
						writer.print('@');
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
