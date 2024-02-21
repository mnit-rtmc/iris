/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.server.comm.ntcip.CRCStream;
import us.mn.state.dot.tms.utils.Base64;

/**
 * FontVersionByteStream is used to calculate fontVersionID.  It is encoded
 * using OER (NTCIP 1102).
 *
 * @author Douglas Lau
 */
public class FontVersionByteStream extends CRCStream {

	/** Create a new FontVersionByteStream */
	public FontVersionByteStream(Font font, int f_num, boolean full_matrix)
		throws IOException
	{
		Collection<Glyph> glyphs =
			FontHelper.lookupGlyphs(font).values();
		DataOutputStream dos = new DataOutputStream(this);
		dos.writeByte(f_num);
		dos.writeByte(font.getHeight());
		dos.writeByte(font.getCharSpacing());
		// char- or line-matrix signs "shall ignore" line spacing,
		// except when they're buggy and we must work around it
		dos.writeByte(full_matrix
			? font.getLineSpacing()
			: 0
		);
		int size = glyphs.size();
		if (size < 256) {
			dos.writeByte(1); // length prefix
			dos.writeByte(size);
		} else {
			dos.writeByte(2); // length prefix
			dos.writeShort(size);
		}
		for (Glyph glyph: glyphs) {
			byte[] bitmap = Base64.decode(glyph.getPixels());
			dos.writeShort(glyph.getCodePoint());
			dos.writeByte(glyph.getWidth());
			int len = bitmap.length;
			if (len <= 0x7F) {
				dos.writeByte(bitmap.length);
			} else if (len <= 0xFF) {
				dos.writeByte(0x81); // one length octet
				dos.writeByte(bitmap.length);
			} else {
				dos.writeByte(0x82); // two length octets
				dos.writeShort(bitmap.length);
			}
			dos.write(bitmap);
		}
	}
}
