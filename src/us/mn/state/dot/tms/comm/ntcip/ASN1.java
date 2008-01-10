/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Abstract Syntax Notation One (ASN.1)
 *
 * @author Douglas Lau
 */
abstract public class ASN1 {

	/** Identifier tag */
	static public class Tag {

		/** Universal tag class */
		static public final byte UNIVERSAL = 0x00;

		/** Application tag class */
		static public final byte APPLICATION = 0x40;

		/** Context-specific tag class */
		static public final byte CONTEXT = (byte)0x80;

		/** Private tag class */
		static public final byte PRIVATE = (byte)0xC0;

		/** Tag class mask */
		static public final byte CLASS_MASK = (byte)0xC0;

		/** Identifier bit mask for constructed encoding */
		static public final byte CONSTRUCTED = 0x20;

		/** Tag numbers equal or greater than ONE_OCTET are encoded
		 * with more than one octet */
		static public final int ONE_OCTET = 0x1F;

		/** Tag class */
		public final byte clazz;

		/** Constructed tag flag */
		public final boolean constructed;

		/** Tag number */
		public final int number;

		/** Create a new identifier tag */
		protected Tag(byte c, boolean co, int n) {
			clazz = c;
			constructed = co;
			number = n;
		}

		/** Compare this tag with another for equality */
		public boolean equals(Object obj) {
			if(!(obj instanceof Tag)) return false;
			Tag o = (Tag)obj;
			if(clazz != o.clazz) return false;
			if(constructed != o.constructed) return false;
			if(number != o.number) return false;
			return true;
		}

		/** Calculate a hash code for the tag */
		public int hashCode() {
			return number ^ ((int)clazz << 24);
		}

		/** Boolean tag */
		static public final Tag BOOLEAN = new Tag(UNIVERSAL, false, 1);

		/** Integer tag */
		static public final Tag INTEGER = new Tag(UNIVERSAL, false, 2);

		/** Bit-string tag */
		static public final Tag BIT_STRING =
			new Tag(UNIVERSAL, false, 3);

		/** Octet-string tag */
		static public final Tag OCTET_STRING =
			new Tag(UNIVERSAL, false, 4);

		/** Null tag */
		static public final Tag NULL = new Tag(UNIVERSAL, false, 5);

		/** Object identifier tag */
		static public final Tag OBJECT_IDENTIFIER =
			new Tag(UNIVERSAL, false, 6);

		/** Sequence (or sequence-of) tag */
		static public final Tag SEQUENCE =
			new Tag(UNIVERSAL, true, 16);
	}

	/** Get the tag with matching parameters */
	protected Tag getTag(byte clazz, boolean constructed, int number) {
		Tag tag = new Tag(clazz, constructed, number);
		if(tag.equals(Tag.BOOLEAN)) return Tag.BOOLEAN;
		if(tag.equals(Tag.INTEGER)) return Tag.INTEGER;
		if(tag.equals(Tag.BIT_STRING)) return Tag.BIT_STRING;
		if(tag.equals(Tag.OCTET_STRING)) return Tag.OCTET_STRING;
		if(tag.equals(Tag.NULL)) return Tag.NULL;
		if(tag.equals(Tag.OBJECT_IDENTIFIER))
			return Tag.OBJECT_IDENTIFIER;
		if(tag.equals(Tag.SEQUENCE)) return Tag.SEQUENCE;
		return null;
	}

	/** Byte array output stream used to encode messages */
	protected final ByteArrayOutputStream encoder =
		new ByteArrayOutputStream();

	/** Encode an ASN.1 identifier (tag) */
	abstract protected void encodeIdentifier(Tag tag) throws IOException;

	/** Encode an ASN.1 length */
	abstract protected void encodeLength(int length) throws IOException;

	/** Get encoded data as byte array (and reset the encoder) */
	protected byte[] getEncodedData() {
		byte[] buffer = encoder.toByteArray();
		encoder.reset();
		return buffer;
	}

	/** Decode an ASN.1 identifier (tag) */
	abstract protected Tag decodeIdentifier(InputStream is)
		throws IOException;

	/** Decode an ASN.1 length */
	abstract protected int decodeLength(InputStream is) throws IOException;
}
