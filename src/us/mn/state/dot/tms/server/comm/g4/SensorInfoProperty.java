/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Sensor information property contains firmware version, serial no., etc.
 *
 * @author Douglas Lau
 */
public class SensorInfoProperty extends G4Property {

	/** Byte offsets from beginning of information data */
	static private final int OFF_MCU_REV = 0;
	static private final int OFF_MCU_BUILD = 1;
	static private final int OFF_MCU_CHK = 2;
	static private final int OFF_DSP_CHK = 3;
	static private final int OFF_FPGA_REL = 4;
	static private final int OFF_MODEL = 5;
	static private final int OFF_SERIAL_NO = 6;
	static private final int OFF_HW_CONFIG = 8;
	static private final int OFF_MODULES = 10;
	static private final int OFF_PROC_REV = 11;
	static private final int OFF_CPLD_VER = 12;
	static private final int OFF_MISC = 13;
	static private final int OFF_RESERVED = 14;

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[0];
		os.write(formatRequest(QualCode.INFO_QUERY, c.getDrop(), data));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Parse the data from one frame.
	 * @param qual Qualifier code.
	 * @param data Data packet. */
	@Override
	protected void parseData(QualCode qual, byte[] data)
		throws IOException
	{
		switch (qual) {
		case INFORMATION:
			parseInformation(data);
			break;
		default:
			super.parseData(qual, data);
		}
	}

	/** MCU firmware revision */
	private int mcu_rev;

	/** MCU firmware build number */
	private int mcu_build;

	/** MCU firmware checksum */
	private int mcu_chk;

	/** DSP firmware checksum */
	private int dsp_chk;

	/** FPGA release */
	private int fpga_rel;

	/** RTMS model */
	private int model;

	/** RTMS serial number */
	private int serial_no;

	/** Hardware configuration */
	private int hw_config;

	/** Optional modules installed */
	private int modules;

	/** Processor board revision */
	private int proc_rev;

	/** CPLD expansion board version */
	private int cpld_ver;

	/** Miscellaneous (serial port source) */
	private int misc;

	/** Reserved data */
	private int reserved;

	/** Get the version (for controller version property). */
	public String getVersion() {
		return "mcu:" + mcu_rev + '-' + mcu_build +
		       " fpga:" + fpga_rel + " ser#:" + serial_no;
	}

	/** Parse sensor information data */
	private void parseInformation(byte[] data) throws ParsingException {
		// Old firmware uses 14 bytes; new 16
		if (data.length != 14 && data.length != 16)
			throw new ParsingException("INFO LEN: " + data.length);
		mcu_rev = parse8(data, OFF_MCU_REV);
		mcu_build = parse8(data, OFF_MCU_BUILD);
		mcu_chk = parse8(data, OFF_MCU_CHK);
		dsp_chk = parse8(data, OFF_DSP_CHK);
		fpga_rel = parse8(data, OFF_FPGA_REL);
		model = parse8(data, OFF_MODEL);
		serial_no = parse16(data, OFF_SERIAL_NO);
		hw_config = parse16(data, OFF_HW_CONFIG);
		modules = parse8(data, OFF_MODULES);
		proc_rev = parse8(data, OFF_PROC_REV);
		cpld_ver = parse8(data, OFF_CPLD_VER);
		misc = parse8(data, OFF_MISC);
		if (data.length >= 16)
			reserved = parse16(data, OFF_RESERVED);
	}

	/** Get a string representation of the statistical property */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("mcu_rev:");
		sb.append(mcu_rev);
		sb.append(" mcu_build:");
		sb.append(mcu_build);
		sb.append(" mcu_chk:");
		sb.append(mcu_chk);
		sb.append(" dsp_chk:");
		sb.append(dsp_chk);
		sb.append(" fpga_rel:");
		sb.append(fpga_rel);
		sb.append(" model:");
		sb.append(model);
		sb.append(" serial_no:");
		sb.append(serial_no);
		sb.append(" hw_config:");
		sb.append(hw_config);
		sb.append(" modules:");
		sb.append(modules);
		sb.append(" proc_rev:");
		sb.append(proc_rev);
		sb.append(" cpld_ver:");
		sb.append(cpld_ver);
		sb.append(" misc:");
		sb.append(misc);
		return sb.toString();
	}
}
