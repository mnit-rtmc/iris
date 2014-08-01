/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Joystick script handling. This is needed because java does not allow
 * opening a device node as a file. So, instead we create a temp Python script 
 * and pipe the output of that script. On Linux, the script opens the device
 * node directly, but on other operating systems, PyGame is used.
 *
 * @author Douglas Lau
 */
public class JoyScript {

	/** Possible locations of Pythn */
	static protected final String[] PYTHON_PATHS = {
		"/usr/bin/python",
		"C:\\Python27\\python.exe",
		"C:\\Python26\\python.exe",
		"C:\\Python33\\python.exe",
		"C:\\Python32\\python.exe",
		"C:\\Python31\\python.exe",
		"C:\\Python30\\python.exe",
	};

	/** Locate a Python interpreter */
	static protected File locatePython() throws IOException {
		for(String path: PYTHON_PATHS) {
			File f = new File(path);
			if(f.canExecute())
				return f;
		}
		throw new FileNotFoundException("No Python interpreter");
	}

	/** Location of python interpreter */
	protected final File interpreter;

	/** Location of temporary python script */
	protected final File script;

	/** Create a new joystick script */
	public JoyScript() throws IOException {
		interpreter = locatePython();
		script = File.createTempFile("joy", ".py");
		script.deleteOnExit();
		FileWriter fw = new FileWriter(script);
		try {
			if(isLinux())
				createLinuxScript(fw);
			else
				createPyGameScript(fw);
		}
		finally {
			fw.close();
		}
	}

	/** Check if the OS is Linux */
	protected boolean isLinux() {
		return System.getProperty("os.name").equals("Linux");
	}

	/** Create the joystick script for use with Linux */
	protected void createLinuxScript(FileWriter fw) throws IOException {
		fw.write("from sys import stdout\n");
		fw.write("from struct import unpack\n");
		fw.write("joystick = open('/dev/input/js0', 'rb')\n");
		fw.write("while True:\n");
		fw.write("\tv = unpack('ihBB', joystick.read(8))\n");
		fw.write("\tif v[2] & 0x01:\n");
		fw.write("\t\tprint ('button:%d,value:%d' % (v[3], v[1]))\n");
		fw.write("\tif v[2] & 0x02:\n");
		fw.write("\t\tvalue = v[1] / 32767.0\n");
		fw.write("\t\tprint ('axis:%d,value:%f' % (v[3], value))\n");
		fw.write("\tstdout.flush()\n");
	}

	/** Create the joystick script for use with PyGame */
	protected void createPyGameScript(FileWriter fw) throws IOException {
		fw.write("import pygame\n");
		fw.write("from sys import stdout\n");
		fw.write("pygame.joystick.init()\n");
		fw.write("joystick = pygame.joystick.Joystick(0)\n");
		fw.write("joystick.init()\n");
		fw.write("pygame.init()\n");
		fw.write("while True:\n");
		fw.write("\tev = pygame.event.wait()\n");
		fw.write("\tif ev.type == pygame.JOYAXISMOTION:\n");
		fw.write("\t\tprint ('axis:%d,value:%f' % (ev.axis,ev.value))\n");
		fw.write("\tif ev.type == pygame.JOYBUTTONUP:\n");
		fw.write("\t\tprint ('button:%d,value:0' % (ev.button))\n");
		fw.write("\tif ev.type == pygame.JOYBUTTONDOWN:\n");
		fw.write("\t\tprint ('button:%d,value:1' % (ev.button))\n");
		fw.write("\tstdout.flush()\n");
	}

	/** Create the joystick polling process */
	public Process createProcess() throws IOException {
		final Process process = Runtime.getRuntime().exec(getCommand());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				process.destroy();
			}
		});
		return process;
	}

	/** Get the command to execute the python script */
	protected String getCommand() {
		return interpreter.toString() + " " + script.toString();
	}
}
