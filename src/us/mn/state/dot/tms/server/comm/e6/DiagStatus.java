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
package us.mn.state.dot.tms.server.comm.e6;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Diagnostic status property.
 *
 * @author Douglas Lau
 */
public class DiagStatus extends E6Property {

	/** Diagnostic command */
	static private final Command CMD = new Command(CommandGroup.DIAGNOSTIC,
		false, false);

	/** Command code */
	static private final int code = 0x0001;

	/** Diagnostic status codes */
	private final byte[] stat = new byte[8];

	/** Get the query command */
	@Override
	public Command queryCmd() {
		return CMD;
	}

	/** Get the packet data */
	@Override
	public byte[] data() {
		byte[] d = new byte[2];
		d[0] = (byte) (code >> 8);
		d[1] = (byte) (code >> 0);
		return d;
	}

	/** Parse a received packet */
	public void parse(byte[] d) throws IOException {
		if (d.length != 12)
			throw new ParsingException("DATA LEN: " + d.length);
		if (d[2] != 0 || d[3] != 1)
			throw new ParsingException("BAD DIAG STAT");
		System.arraycopy(d, 4, stat, 0, stat.length);
	}

	/** Test status bit for an error */
	private boolean testBit(int b) {
		int i = (63 - b) / 8;
		int bit = 1 << (b % 8);
		return (stat[i] & bit) == bit;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (testBit(58))
			sb.append("FRAM test, ");
		if (testBit(57))
			sb.append("FRAM data, ");
		if (testBit(50))
			sb.append("FPGA1, ");
		if (testBit(49))
			sb.append("FPGA2, ");
		if (testBit(48))
			sb.append("Power supply, ");
		if (testBit(47))
			sb.append("Digital overvoltage, ");
		if (testBit(46))
			sb.append("Digital undervoltage, ");
		if (testBit(43))
			sb.append("RF ADC > max, ");
		if (testBit(42))
			sb.append("RF ADC < min, ");
		if (testBit(41))
			sb.append("RF ATTN DAC1 > max, ");
		if (testBit(40))
			sb.append("RF ATTN DAC1 < min, ");
		if (testBit(39))
			sb.append("RF ATTN DAC2 > max, ");
		if (testBit(38))
			sb.append("RF ATTN DAC2 < min, ");
		if (testBit(37))
			sb.append("RF DOM DAC > max, ");
		if (testBit(36))
			sb.append("RF DOM DAC < min, ");
		if (testBit(35))
			sb.append("RF XCVR source1 PLL unlocked, ");
		if (testBit(34))
			sb.append("RF XCVR source2 PLL unlocked, ");
		if (testBit(33))
			sb.append("RF XCVR uncalibrated, ");
		if (testBit(32))
			sb.append("RF 5VDC overvoltage, ");
		if (testBit(31))
			sb.append("RF 5VDC undervoltage, ");
		if (testBit(22))
			sb.append("GPS T-RAIM alarm, ");
		if (testBit(21))
			sb.append("GPS self-test fault, ");
		if (testBit(20))
			sb.append("GPS power-on fault, ");
		if (testBit(19))
			sb.append("TDM two-masters, ");
		if (testBit(18))
			sb.append("TDM master-slave, ");
		if (testBit(17))
			sb.append("TDM clock, ");
		if (testBit(16))
			sb.append("GPS window, ");
		if (testBit(15))
			sb.append("GPS one-PPS, ");
		if (testBit(14))
			sb.append("GPS comm-link, ");
		if (testBit(13))
			sb.append("serial comm-link, ");
		if (testBit(12))
			sb.append("UDP/IP comm-link, ");
		if (testBit(11))
			sb.append("serial debug comm-link, ");
		if (testBit(10))
			sb.append("RF XCVR comm-link, ");
		if (testBit(3))
			sb.append("CPU firmware, ");
		if (testBit(1))
			sb.append("Buffered transactions, ");
		if (testBit(0))
			sb.append("Error log entries, ");
		if (sb.length() > 0)
			sb.setLength(sb.length() - 2);
		return sb.toString();
	}
}
