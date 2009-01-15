/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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

import java.io.IOException;
import us.mn.state.dot.tms.Base64;

/**
 * Skyline IllumPowerStatus object
 *
 * @author Douglas Lau
 */
public class IllumPowerStatus extends SkylineDmsStatus
	implements ASN1OctetString
{
	/** Power status codes */
	static public final int UNAVAILABLE = 0;
	static public final int LOW = 1;
	static public final int MARGINALLY_LOW = 2;
	static public final int OK = 3;
	static public final int MARGINALLY_HIGH = 4;
	static public final int HIGH = 5;

	/** Status descriptions */
	static protected final String[] STATUS = {
		"???", "low", "marginally low", "OK", "marginally high", "high"
	};

	/** Create a new IllumPowerStatus object */
	public IllumPowerStatus() {
		super(2);
		oid[node++] = 2;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() {
		return "illumPowerStatus";
	}

	/** Power status */
	protected byte[] power = new byte[0];

	/** Set the octet string value */
	public void setOctetString(byte[] value) {
		power = value;
		for(int i = 0; i < power.length; i++) {
			if(power[i] < 0 || power[i] >= STATUS.length)
				power[i] = UNAVAILABLE;
		}
	}

	/** Get the octet string value */
	public byte[] getOctetString() {
		return power;
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < power.length; i++) {
			if(b.length() > 0)
				b.append(", ");
			b.append("#");
			b.append(i + 1);
			b.append(": ");
			b.append(STATUS[power[i]]);
		}
		if(b.length() == 0)
			b.append("None");
		return b.toString();
	}

	/** Simple interface to test a power supply for something */
	static public interface PowerTester {
		boolean check(int p);
	}

	/** Get power status bitmaps */
	public String[] getBitmaps() throws IOException {
		String[] rows = new String[3];
		rows[DMS.FAIL_BITMAP] = createBase64(new PowerTester() {
			public boolean check(int p) {
				return p == LOW || p == HIGH;
			}
		});
		rows[DMS.VOLTAGE_BITMAP] = createBase64(new PowerTester() {
			public boolean check(int p) {
				return p != OK && p != UNAVAILABLE;
			}
		});
		rows[DMS.CURRENT_BITMAP] = createBase64(new PowerTester() {
			public boolean check(int p) {
				return false;
			}
		});
		return rows;
	}

	/** Create a power status bitmap encoded in Base64 */
	protected String createBase64(PowerTester tester) throws IOException {
		return Base64.encode(createBitmap(tester));
	}

	/** Create a power status bitmap */
	protected byte[] createBitmap(PowerTester tester) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			for(int i = 0; i < power.length; ) {
				int v = 0;
				for(int b = 0; b < 8; b++) {
					if(tester.check(power[i]))
						v |= 1 << b;
					i++;
					if(i >= power.length)
						break;
				}
				dos.writeByte(v);
			}
			return bos.toByteArray();
		}
		finally {
			bos.close();
		}
	}
}
