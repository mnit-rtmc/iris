/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Joystick handling for linux. This is needed because java does not allow
 * opening a device node as a file. So, instead we create a Python script in
 * the /tmp directory and pipe the output of that script.
 *
 * @author Douglas Lau
 */
public class JoyLinux {

	/** Location of temporary python script */
	protected final File script;

	/** Create a new linux joystick script */
	public JoyLinux() throws IOException {
		script = File.createTempFile("joy", ".py");
		script.deleteOnExit();
		FileWriter fw = new FileWriter(script);
		fw.write("from sys import stdout\n");
		fw.write("from struct import unpack\n");
		fw.write("joystick = open('/dev/input/js0')\n");
		fw.write("while True:\n");
		fw.write("\tv = unpack('ihBB', joystick.read(8))\n");
		fw.write("\tif v[2] & 0x01:\n");
		fw.write("\t\tprint 'button:%d,value:%d' % (v[3], v[1])\n");
		fw.write("\tif v[2] & 0x02:\n");
		fw.write("\t\tvalue = v[1] / 32767.0\n");
		fw.write("\t\tprint 'axis:%d,value:%f' % (v[3], value)\n");
		fw.write("\tstdout.flush()\n");
		fw.close();
	}

	/** Location of joystick polling script on Linux */
	public String getCommand() {
		return "/usr/bin/env python " + script.toString();
	}
}
