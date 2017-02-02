/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.monstream;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.StreamType;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * A property to switch a camera.
 *
 * @author Douglas Lau
 */
public class SwitchProp extends ControllerProp {

	/** ASCII unit separator */
	static private final char UNIT_SEP = 31;

	/** Camera to display */
	private final CameraImpl camera;

	/** Create a new switch property */
	public SwitchProp(CameraImpl c) {
		camera = c;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		tx_buf.put(formatReq().getBytes("UTF8"));
	}

	/** Format a switch request */
	private String formatReq() {
		return (camera != null) ? formatPlay() : formatStop();
	}

	/** Format a play request */
	private String formatPlay() {
		assert camera != null;
		StringBuilder sb = new StringBuilder();
		sb.append("play");
		sb.append(UNIT_SEP);
		sb.append(camera.getName());
		sb.append(UNIT_SEP);
		sb.append(CameraHelper.encoderUri(camera, getAuth(),
		          "").toString());
		sb.append(UNIT_SEP);
		sb.append(StreamType.fromOrdinal(camera.getStreamType()));
		sb.append(UNIT_SEP);
		sb.append(GeoLocHelper.getDescription(camera.getGeoLoc()));
		return sb.toString();
	}

	/** Get camera encoder auth string */
	private String getAuth() {
		assert camera != null;
		Controller c = camera.getController();
		if (c instanceof ControllerImpl) {
			String pwd = ((ControllerImpl) c).getPassword();
			return (pwd.length() > 0) ? "//" + pwd + '@' : "";
		} else
			return "";
	}

	/** Format a stop request */
	private String formatStop() {
		StringBuilder sb = new StringBuilder();
		sb.append("stop");
		sb.append(UNIT_SEP);
		sb.append(UNIT_SEP);
		sb.append(UNIT_SEP);
		sb.append(UNIT_SEP);
		return sb.toString();
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "switch: " + camera;
	}
}
