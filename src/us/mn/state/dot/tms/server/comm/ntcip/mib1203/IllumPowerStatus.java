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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.server.comm.ntcip.ASN1OctetStr;

/**
 * Skyline IllumPowerStatus object
 *
 * @author Douglas Lau
 */
public class IllumPowerStatus extends ASN1OctetStr {

	/** Enumeration of power status */
	static public enum Enum {
		unavailable, low, marginallyLow, ok, marginallyHigh, high;

		/** Get power status from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return unavailable;
		}
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.skylineDmsStatus.createOID(new int[] {2, 0});
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < value.length; i++) {
			if(b.length() > 0)
				b.append(", ");
			b.append("#");
			b.append(i + 1);
			b.append(": ");
			b.append(Enum.fromOrdinal(value[i]));
		}
		if(b.length() == 0)
			b.append("None");
		return b.toString();
	}

	/** Simple interface to test a power supply for something */
	static public interface PowerTester {
		boolean check(Enum p);
	}

	/** Get power status bitmaps */
	public String[] getBitmaps() throws IOException {
		String[] rows = new String[3];
		rows[DMS.FAIL_BITMAP] = createBase64(new PowerTester() {
			public boolean check(Enum p) {
				return p == Enum.low || p == Enum.high;
			}
		});
		rows[DMS.VOLTAGE_BITMAP] = createBase64(new PowerTester() {
			public boolean check(Enum p) {
				return p != Enum.ok && p != Enum.unavailable;
			}
		});
		rows[DMS.CURRENT_BITMAP] = createBase64(new PowerTester() {
			public boolean check(Enum p) {
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
			for(int i = 0; i < value.length; ) {
				int v = 0;
				for(int b = 0; b < 8; b++) {
					Enum p = Enum.fromOrdinal(value[i]);
					if(tester.check(p))
						v |= 1 << b;
					i++;
					if(i >= value.length)
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
