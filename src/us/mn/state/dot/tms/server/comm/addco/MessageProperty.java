/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.addco;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.BitmapGraphic;
import static us.mn.state.dot.tms.DmsColor.AMBER;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.CRC;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Addco Message Property.
 *
 * @author Douglas Lau
 */
public class MessageProperty extends AddcoProperty {

	/** Length of query request (bytes) */
	static private final int QUERY_REQ_LEN = 11;

	/** CRC-16 algorithm */
	static private final CRC crc16 = new CRC(16, 0x8005, 0xFFFF, true);

	/** Calculate the stride of a bitmap */
	static private int bitmapStride(int width) {
		// Stride is always a multiple of 4 bytes
		return (((width - 1) / 32) + 1) * 4;
	}

	/** Calculate the number of bytes in a bitmap */
	static private int bitmapBytes(int width, int height) {
		return height * bitmapStride(width);
	}

	/** DMS for message */
	private final DMSImpl dms;

	/** All pages of the message */
	private MessagePage[] pages = new MessagePage[0];

	/** Current parsing position */
	private int pos;

	/** Create a new message property */
	public MessageProperty(DMSImpl d) {
		dms = d;
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(formatQuery());
	}

	/** Format a QUERY request */
	private byte[] formatQuery() throws IOException {
		byte[] buf = new byte[QUERY_REQ_LEN];
		format8(buf, 0, MsgCode.NORMAL.code);
		format16le(buf, 1, QUERY_REQ_LEN + 2);	// + 2 FCS bytes
		format16le(buf, 3, ADDR_ANY);
		buf[5] = 'G';
		buf[6] = 'M';
		format8(buf, 7, -1);	// ???
		format8(buf, 8, 1);	// ???
		format8(buf, 9, 0);	// ???
		format8(buf, 10, 32);	// ???
		return buf;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		int len = decodeHead(is, MsgCode.NORMAL);
		if (len < 10)
			throw new ParsingException("MSG LEN: " + len);
		parseQuery(decodeBody(is, len));
	}

	/** Parse a query response */
	private void parseQuery(byte[] body) throws IOException {
		checkCommand(body, "RM");
		int n_pages = parse8(body, 4);
		if (n_pages < 0 || n_pages > 4)
			throw new ParsingException("BAD PAGE COUNT: " +n_pages);
		pos = 5;
		pages = new MessagePage[n_pages];
		for (int p = 0; p < n_pages; p++)
			pages[p] = parsePage(body, p, n_pages);
	}

	/** Parse one page of a message */
	private MessagePage parsePage(byte[] body, int p, int n_pages)
		throws IOException
	{
		parseCheck2(body, "UNKNOWN0", 8, 8);
		int seq = parse8(body, pos);
		pos++;
		parseCheck2(body, "PAGE #", p + 1, p + 1);
		parseCheck2(body, "PAGES", n_pages, n_pages);
		int p_type = parse8(body, pos);
		pos++;
		int p_on = parse8(body, pos);
		pos++;
		int p_off = parse8(body, pos);
		pos++;
		if (p_type == 0)
			return parseBitmapPage(body, p_on, p_off);
		else if (p_type == 16)
			return parseTextPage(body, p_on, p_off);
		else
			throw new ParsingException("PTYPE: " + p_type);
	}

	/** Parse a text page of a message */
	private MessagePage parseTextPage(byte[] body, int p_on, int p_off)
		throws IOException
	{
		final int i_pos = pos;
		int n_len = parseCheck2(body, "NLEN", 0, 64);
		String name = parseAscii(body, pos, n_len);
		pos += n_len;
		int t_len = parseCheck2(body, "TLEN", 0, 64);
		String text = parseAscii(body, pos, t_len);
		pos += t_len;
		parseCheck2(body, "ZERO", 0, 0);
		parseCheckCrc(body, i_pos);
		String multi = MultiString.replacePageTime(text, p_on, p_off);
		return new MessagePage(dms, multi);
	}

	/** Parse a CRC-16 and check it */
	private void parseCheckCrc(byte[] body, int i_pos)
		throws ChecksumException
	{
		int crc = calculateCrc(body, i_pos);
		int rc = parse16le(body, pos);
		pos += 2;
		if (rc != crc)
			throw new ChecksumException(body);
	}

	/** Calculate a CRC-16 */
	private int calculateCrc(byte[] body, int i_pos) {
		int crc = crc16.seed;
		for (int i = i_pos; i < pos; i++)
			crc = crc16.step(crc, body[i]);
		return crc16.result(crc);
	}

	/** Parse a bitmap page of a message */
	private MessagePage parseBitmapPage(byte[] body, int p_on, int p_off)
		throws IOException
	{
		final int i_pos = pos;
		int n_len = parseCheck2(body, "NLEN", 0, 64);
		String name = parseAscii(body, pos, n_len);
		pos += n_len;
		int b_len = parseCheck2(body, "BLEN", 0, 4096);
		String bm = parseAscii(body, pos, 2);
		pos += 2;
		if (!bm.equals("BM"))
			throw new ParsingException("BM: " + bm);
		parseCheck2(body, "BLEN2", b_len, b_len);
		parseCheck2(body, "Z0", 0, 0);
		parseCheck4(body, "Z1", 0, 0);
		parseCheck4(body, "EXTRA", 62, 62);
		parseCheck4(body, "UNKNOWN1", 40, 40);
		int width = parseCheck4(body, "WIDTH", 0, 256);
		int height = parseCheck4(body, "HEIGHT", 0, 256);
		parseCheck2(body, "UNKNOWN2", 1, 1);
		parseCheck2(body, "UNKNOWN3", 1, 1);
		parseCheck4(body, "UNKNOWN4", 0, 0);
		int n_bytes = bitmapBytes(width, height);
		parseCheck4(body, "BBYTES", n_bytes, n_bytes);
		parse4(body);	// ???
		parse4(body);	// ???
		parse4(body);	// ???
		parse4(body);	// ???
		parseCheck4(body, "Z2", 0, 0);
		parseCheck2(body, "UNKNOWN5", -1, -1);
		parseCheck2(body, "UNKNOWN6", 255, 255);
		BitmapGraphic bmap = parseBitmap(body, width, height);
		parseCheck2(body, "Z3", 0, 0);
		parseCheckCrc(body, i_pos);
		String multi = MultiString.replacePageTime(name, p_on, p_off);
		return new MessagePage(multi, bmap);
	}

	/** Parse a bitmap graphic */
	private BitmapGraphic parseBitmap(byte[] body, int width, int height)
		throws ParsingException
	{
		int stride = bitmapStride(width);
		BitmapGraphic bmap = DMSHelper.createBitmapGraphic(dms);
		// FIXME: check dimensions
		for (int y = 0; y < height; y++) {
			int iy = height - y - 1;
			int off = 0;
			int bit = 7;
			for (int x = 0; x < width; x++) {
				if ((body[pos + off] & (1 << bit)) != 0)
					bmap.setPixel(x, iy, AMBER);
				if (bit > 0)
					bit--;
				else {
					off++;
					bit = 7;
				}
			}
			pos += stride;
		}
		return bmap;
	}

	/** Parse a 2-byte value and check it's within range */
	private int parseCheck2(byte[] body, String vname, int mn, int mx)
		throws ParsingException
	{
		int val = parse16le(body, pos);
		pos += 2;
		if (val < mn || val > mx)
			throw new ParsingException(vname + ": " + val);
		return val;
	}

	/** Parse a 4-byte value and check it's within range */
	private int parseCheck4(byte[] body, String vname, int mn, int mx)
		throws ParsingException
	{
		int val = parse32le(body, pos);
		pos += 4;
		if (val < mn || val > mx)
			throw new ParsingException(vname + ": " + val);
		return val;
	}

	/** Parse a 4-byte value */
	private int parse4(byte[] body) {
		int val = parse32le(body, pos);
		pos += 4;
		return val;
	}

	/** Get the message MULTI string */
	public String getMulti() {
		MultiString ms = new MultiString();
		for (int i = 0; i < pages.length; i++) {
			if (i > 0)
				ms.addPage();
			ms.addSpan(pages[i].getMulti());
		}
		return ms.toString();
	}

	/** Get the bitmaps for all pages */
	public BitmapGraphic[] getBitmaps() {
		BitmapGraphic[] bmaps = new BitmapGraphic[pages.length];
		for (int i = 0; i < bmaps.length; i++)
			bmaps[i] = pages[i].getBitmap();
		return bmaps;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return getMulti();
	}
}
