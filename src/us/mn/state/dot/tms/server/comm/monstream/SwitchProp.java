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
import us.mn.state.dot.tms.CtrlCondition;
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

	/** ASCII record separator */
	static private final char RECORD_SEP = 30;

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
		int pin = op.getDevice().getPin();
		tx_buf.put(formatReq(pin).getBytes("UTF8"));
	}

	/** Format a switch request */
	private String formatReq(int pin) {
		StringBuilder sb = new StringBuilder();
		sb.append("play");
		sb.append(UNIT_SEP);
		sb.append(pin - 1);
		sb.append(UNIT_SEP);
		sb.append(getCamNum());
		sb.append(UNIT_SEP);
		sb.append(getUri());
		sb.append(UNIT_SEP);
		sb.append(getStreamType());
		sb.append(UNIT_SEP);
		sb.append(getDescription());
		sb.append(RECORD_SEP);
		return sb.toString();
	}

	/** Get camera number */
	private String getCamNum() {
		if (camera != null) {
			Integer cam_num = camera.getCamNum();
			return (cam_num != null)
			      ? cam_num.toString()
			      : camera.getName();
		}
		return "";
	}

	/** Get the stream URI */
	private String getUri() {
		if (camera != null) {
			String cond = getConditionUri();
			if (cond != null)
				return cond;
			return CameraHelper.encoderUri(camera, getAuth(),
				"").toString();
		} else
			return "";
	}

	/** Get the condition URI */
	private String getConditionUri() {
		switch (getCondition()) {
		case CONSTRUCTION:
			// FIXME
			return "http://tms-iris/monstream/construction.png";
		case PLANNED:
		case REMOVED:
			// FIXME
			return "http://tms-iris/monstream/service.png";
		default:
			return null;
		}
	}

	/** Get the camera condition */
	private CtrlCondition getCondition() {
		if (camera != null) {
			Controller c = camera.getController();
			if (c instanceof ControllerImpl) {
				return CtrlCondition.fromOrdinal(
					c.getCondition());
			}
		}
		return CtrlCondition.REMOVED;
	}

	/** Get camera encoder auth string */
	private String getAuth() {
		assert camera != null;
		Controller c = camera.getController();
		if (c instanceof ControllerImpl) {
			String pwd = ((ControllerImpl) c).getPassword();
			return (pwd != null && pwd.length() > 0)
			      ? "//" + pwd + '@'
			      : "";
		} else
			return "";
	}

	/** Get the stream type */
	private String getStreamType() {
		if (CtrlCondition.ACTIVE == getCondition()) {
			if (camera != null) {
				return StreamType.fromOrdinal(
					camera.getStreamType()).toString();
			}
		}
		return "PNG";
	}

	/** Get the stream description */
	private String getDescription() {
		return (camera != null)
		      ? GeoLocHelper.getDescription(camera.getGeoLoc())
		      : "";
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "switch: " + camera;
	}
}
