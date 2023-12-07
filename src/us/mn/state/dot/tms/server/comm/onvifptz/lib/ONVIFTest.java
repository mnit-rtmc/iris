/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.onvifptz.lib;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;

/**
 * Test class for ONVIF Standard implementation
 *
 * @author Ethan Beauclaire
 */
public class ONVIFTest {
	public static void main(String[] args) throws Exception {
		Console c = System.console();
		String username = "", password = "";
		if (c != null) {
			username = c.readLine("Username: ");
			password = new String(c.readPassword("Password: "));
		}

		if ("".equals(username)) password = "";


		DeviceService deviceService = DeviceService.getDeviceService(
			"http://192.168.1.150/onvif/device_service", username, password);
		MediaService mediaService = MediaService.getMediaService(
			"http://192.168.1.150:80/onvif/media", username, password);
		PTZService ptzService = PTZService.getPTZService(
			"http://192.168.1.150:80/onvif/ptz", username, password);
		ImagingService imagingService = ImagingService.getImagingService(
			"http://192.168.1.150:80/onvif/imaging", username, password);

		//System.out.println("\nServices: \n" + deviceService.getServices() + "\n");
		//System.out.println("\nServiceCapabilities: \n" + deviceService.getServiceCapabilities() + "\n");
		System.out.println("\nCapabilities: \n" + deviceService.getCapabilities("All") + "\n");

		//System.out.println("\nMedia Profiles: \n" + mediaService.getProfiles() + "\n");
		System.out.println("\nVideo sources:\n" + mediaService.getVideoSources() + "\n");

		//System.out.println("\nConfigurations: \n" + ptzService.getConfigurations() + "\n");
		//System.out.println("\nConfigurationOptions: \n" + ptzService.getConfigurationOptions("HD") + "\n");

		System.out.println("\nImaging options:\n" + imagingService.getOptions("Visible Camera") + "\n");
		System.out.println("\nImaging settings:\n" + imagingService.getImagingSettings("Visible Camera") + "\n");

		String userCommand = "";
		String resp = "";
		while (!"exit".equals(userCommand = c.readLine("Enter a command <exit|con x y z|rel x y z|stop|configurefocus s|focus f|getfocus|moveoptions|iris f|scopes|isettings|ptzconfigs|ptzconfigoptions|wiper On|Off|setpreset i|gotopreset i|getpresets>: "))) {
			String[] cmd = userCommand.split(" ");
			if (cmd.length == 0) continue;
			switch (cmd[0]) {
				case "ptz":
				case "con":
					if (cmd.length == 4)
						resp = ptzService.continuousMove(Float.parseFloat(cmd[1]), Float.parseFloat(cmd[2]), Float.parseFloat(cmd[3]));
					else continue;
					break;
				case "rel":
					if (cmd.length == 4)
						resp = ptzService.relativeMove(Float.parseFloat(cmd[1]), Float.parseFloat(cmd[2]), Float.parseFloat(cmd[3]));
					else continue;
					break;
				case "stop":
					resp = ptzService.stop();
					break;
				case "getpresets":
					resp = ptzService.getPresets();
					break;
				case "gotopreset":
					if (cmd.length == 2)
						resp = ptzService.gotoPreset(cmd[1]);
					else continue;
					break;
				case "setpreset":
					if (cmd.length == 2)
						resp = ptzService.setPreset(cmd[1]);
					else continue;
					break;
				case "iris":
					if (cmd.length == 2)
						resp = imagingService.setIris("Visible Camera", cmd[1]);
					else continue;
					break;
				case "configurefocus":
					if (cmd.length == 2)
						imagingService.setFocus("Visible Camera", cmd[1]);
					else continue;
					break;
				case "focus":
					if (cmd.length == 2)
						imagingService.moveFocus("Visible Camera", Float.parseFloat(cmd[1]));
					else continue;
					break;
				case "getfocusoptions":
				case "moveoptions":
					resp = imagingService.getMoveOptions("Visible Camera");
					break;
				case "getfocus":
					resp = imagingService.getStatus("Visible Camera");
					break;
				case "nodes":
					resp = ptzService.getNodes();
					break;
				case "node":
					if (cmd.length >= 2)
						resp = ptzService.getNode(Arrays.asList(cmd).stream().skip(1).collect(Collectors.joining(" ")));
					else continue;
					break;
				case "scopes":
					resp = deviceService.getScopes();
					break;
				case "isettings":
					resp = imagingService.getImagingSettings("Visible Camera");
					break;
				case "ptzconfigs":
					resp = ptzService.getConfigurations();
					break;
				case "ptzconfigoptions":
					resp = ptzService.getConfigurationOptions("HD");
					break;
				case "wiper":
					if (cmd.length == 2)
						resp = ptzService.setWiper(cmd[1]);
					break;
			}
			System.out.println("\n" + userCommand + " response:\n" + resp + "\n");
		}
		System.out.println("Stopping camera...");
		imagingService.stop("Visible Camera");
		ptzService.stop();
		System.out.println("Exiting...");
	}
}

