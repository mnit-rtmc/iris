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
public class DiagStatusProp extends E6Property {

	/** Diagnostic command */
	static private final Command CMD = new Command(CommandGroup.DIAGNOSTIC);

	/** Query command code */
	static private final int QUERY = 0x0001;

	/** Diagnostic status codes */
	private final byte[] stat = new byte[8];

	/** Get the command */
	@Override
	public Command command() {
		return CMD;
	}

	/** Get the query packet data */
	@Override
	public byte[] queryData() {
		byte[] d = new byte[2];
		format16(d, 0, QUERY);
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length != 12)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		System.arraycopy(d, 4, stat, 0, stat.length);
	}

	/** Diag error bits */
	public enum ErrorBit {
		FRAM_test		(58, true),
		FRAM_data		(57, true),
		FPGA1			(50, true),
		FPGA2			(49, true),
		Power_supply		(48, false),
		DIO_overvoltage		(47, true),
		DIO_undervoltage	(46, true),
		RF_ADC_gt_max		(43, false),
		RF_ADC_lt_min		(42, false),
		RF_ATTN_DAC1_gt_max	(41, false),
		RF_ATTN_DAC1_lt_min	(40, false),
		RF_ATTN_DAC2_gt_max	(39, false),
		RF_ATTN_DAC2_lt_min	(38, false),
		RF_DOM_DAC_gt_max	(37, false),
		RF_DOM_DAC_lt_min	(36, false),
		RF_XCVR_src1_PLL_unlocked(35, true),
		RF_XCVR_src2_PLL_unlocked(34, true),
		RF_XCVR_uncalibrated	(33, true),
		RF_5VDC_overvoltage	(32, false),
		RF_5VDC_undervoltage	(31, false),
		GPS_T_RAIM_alarm	(22, true),
		GPS_self_test_fault	(21, true),
		GPS_power_on_fault	(20, true),
		TDM_two_masters		(19, true),
		TDM_master_slave	(18, true),
		TDM_clock		(17, true),
		GPS_window		(16, true),
		GPS_one_PPS		(15, true),
		GPS_comm_link		(14, true),
		serial_comm_link	(13, false),
		UDP_IP_comm_link	(12, false),
		serial_debug_comm_link	(11, false),
		RF_XCVR_comm_link	(10, true),
		CPU_firmware		(3, true),
		Buffered_transactions	(1, false),
		Error_log_entries	(0, false);

		/** Create a new diag error bit */
		private ErrorBit(int b, boolean e) {
			bit = b;
			err = e;
		}

		/** Bit number */
		public final int bit;

		/** Error / maintenance flag */
		public final boolean err;
	};

	/** Test status bit for an error */
	private boolean testBit(int b) {
		int i = (63 - b) / 8;
		int bit = 1 << (b % 8);
		return (stat[i] & bit) == bit;
	}

	/** Format maintenance status */
	public String formatMaint() {
		return formatStatus(false);
	}

	/** Format error status */
	public String formatErrors() {
		return formatStatus(true);
	}

	/** Format status */
	private String formatStatus(boolean err) {
		StringBuilder sb = new StringBuilder();
		for (ErrorBit eb: ErrorBit.values()) {
			if ((eb.err == err) && testBit(eb.bit)) {
				sb.append(eb);
				sb.append(',');
			}
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/** Get a string representation */
	@Override
	public String toString() {
		String errs = formatErrors();
		String maint = formatMaint();
		if (errs.length() > 0 && maint.length() > 0)
			return errs + ", " + maint;
		else
			return errs + maint;
	}
}
