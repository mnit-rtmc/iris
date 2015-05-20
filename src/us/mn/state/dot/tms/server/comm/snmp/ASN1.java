/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Abstract Syntax Notation One (ASN.1)
 *
 * @author Douglas Lau
 */
abstract public class ASN1 {

	/** Get the tag with matching parameters */
	protected Tag getTag(byte clazz, boolean constructed, int number) {
		return ASN1Tag.fromValues(clazz, constructed, number);
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
