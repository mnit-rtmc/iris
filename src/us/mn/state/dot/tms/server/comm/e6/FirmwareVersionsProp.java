/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
 * Firmware versions property.
 *
 * @author Douglas Lau
 */
public class FirmwareVersionsProp extends E6Property {

	/** System information command */
	static private final Command CMD =new Command(CommandGroup.SYSTEM_INFO);

	/** Query command code */
	static private final int QUERY = 0x0021;

	/** Create a new firmware versions property */
	public FirmwareVersionsProp() { }

	/** CPU boot firmware version */
	private int cpu_boot;

	/** CPU application firmware version */
	private int cpu_app;

	/** Digital board FPGA 1 firmware version */
	private int fpga1;

	/** Daughter board FPGA 2 firmware version */
	private int fpga2;

	/** RF transciever FPGA firmware version */
	private int rf_fpga;

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
		if (d.length != 24)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		cpu_boot = parse32(d, 4);
		cpu_app = parse32(d, 8);
		fpga1 = parse32(d, 12);
		fpga2 = parse32(d, 16);
		rf_fpga = parse32(d, 20);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "cpu_boot: " + Integer.toHexString(cpu_boot) +
		       ", cpu_app: " + Integer.toHexString(cpu_app) +
		       ", fpga1: " + Integer.toHexString(fpga1) +
		       ", fpga2: " + Integer.toHexString(fpga2) +
		       ", rf_fpga: " + Integer.toHexString(rf_fpga);
	}
}
