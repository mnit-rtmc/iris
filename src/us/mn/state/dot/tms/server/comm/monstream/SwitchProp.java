/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.CtrlCondition;
import us.mn.state.dot.tms.EncoderStream;
import us.mn.state.dot.tms.Encoding;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * A property to switch a camera.
 *
 * @author Douglas Lau
 */
public class SwitchProp extends MonProp {

	/** Controller pin */
	private final int pin;

	/** Camera to display */
	private final CameraImpl camera;

	/** Create a new switch property */
	public SwitchProp(int p, CameraImpl c) {
		pin = p;
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
		EncoderStream es = getEncoderStream();
		StringBuilder sb = new StringBuilder();
		sb.append("play");
		sb.append(UNIT_SEP);
		sb.append(pin - 1);
		sb.append(UNIT_SEP);
		sb.append(getCamNum());
		sb.append(UNIT_SEP);
		sb.append(getUri());
		sb.append(UNIT_SEP);
		sb.append(getEncoding(es));
		sb.append(UNIT_SEP);
		sb.append(getDescription());
		sb.append(UNIT_SEP);
		sb.append(getLatency(es));
		sb.append(RECORD_SEP);
		return sb.toString();
	}

	/** Get the best encoder stream */
	private EncoderStream getEncoderStream() {
		return isStreaming() ? CameraHelper.getStream(camera) : null;
	}

	/** Check if camera is streaming */
	private boolean isStreaming() {
		return CameraHelper.isActive(camera)
		   && !CameraHelper.isBlank(camera);
	}

	/** Get camera number */
	private String getCamNum() {
		if (CameraHelper.isBlank(camera))
			return "";
		else {
			assert camera != null;
			Integer cn = camera.getCamNum();
			return (cn != null) ? cn.toString() : camera.getName();
		}
	}

	/** Get the stream URI */
	private String getUri() {
		return CameraHelper.getUri(camera);
	}

	/** Get the encoding */
	private String getEncoding(EncoderStream es) {
		if (es != null) {
			Encoding enc = Encoding.fromOrdinal(es.getEncoding());
			if (enc != Encoding.UNKNOWN)
				return enc.toString();
		}
		return "PNG";
	}

	/** Get the stream description */
	private String getDescription() {
		return CameraHelper.isBlank(camera)
		      ? ""
		      : GeoLocHelper.getLocation(camera.getGeoLoc());
	}

	/** Get the stream latency (ms) */
	private int getLatency(EncoderStream es) {
		return (es != null)
		      ? es.getLatency()
		      : EncoderStream.DEFAULT_LATENCY_MS;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "switch: " + camera;
	}
}
