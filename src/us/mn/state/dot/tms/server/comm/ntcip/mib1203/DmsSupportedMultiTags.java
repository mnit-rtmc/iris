/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2015  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;

/**
 * Bitmap of supported MULTI tags.  This object was added in 1203v2.
 *
 * @author Douglas Lau
 */
public class DmsSupportedMultiTags extends ASN1OctetString {

	/** Enumeration of MULTI tags */
	static public enum Enum {
		cb, cf, fl, fo, g, hc, jl, jp, ms, mvt, nl, np, pt, sc, f1, f2,
		f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, tr, cr, pb;

		/** Get MULTI tag from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for (Enum e: values()) {
				if (e.ordinal() == o)
					return e;
			}
			return null;
		}
	}

	/** Create a new DmsSupportedMultiTags object */
	public DmsSupportedMultiTags() {
		super(MIB1203.dmsSupportedMultiTags.node);
	}

	/** Get the object value */
	@Override
	public String getValue() {
		boolean[] tags = tagArray();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < tags.length; i++) {
			if (tags[i]) {
				Enum tag = Enum.fromOrdinal(i);
				if (tag != null) {
					b.append(tag.toString());
					b.append(',');
				}
			}
		}
		if (b.length() == 0)
			return "None";
		else {
			// remove trailing comma
			b.setLength(b.length() - 1);
			return b.toString();
		}
	}

	/** Get the tag array.
	 * @return Array of 32 tag flags. */
	private boolean[] tagArray() {
		byte[] bits = bitmapArray();
		boolean[] tags = new boolean[32];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 8; j++) {
				int k = i * 8 + j;
				int bit = 1 << j;
				tags[k] = ((bits[i] & bit) != 0);
			}
		}
		return tags;
	}

	/** Get the bitmap array.
	 * @return Array of 4 bytes */
	private byte[] bitmapArray() {
		byte[] val = getByteValue();
		if (val.length == 4)
			return val;
		else
			return new byte[4];
	}
}
