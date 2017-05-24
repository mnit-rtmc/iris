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
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.Encoding;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.utils.URIUtil;

/**
 * A property to switch a camera.
 *
 * @author Douglas Lau
 */
public class SwitchProp extends ControllerProp {

	/** Get the blank URL */
	static private String getBlankUrl() {
		return SystemAttrEnum.CAMERA_BLANK_URL.getString();
	}

	/** Get the construction URL */
	static private String getConstructionUrl() {
		return SystemAttrEnum.CAMERA_CONSTRUCTION_URL.getString();
	}

	/** Get the out-of-service URL */
	static private String getOutOfServiceUrl() {
		return SystemAttrEnum.CAMERA_OUT_OF_SERVICE_URL.getString();
	}

	/** Get the "blank" camera number */
	static private int cameraNumBlank() {
		return SystemAttrEnum.CAMERA_NUM_BLANK.getInt();
	}

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
		sb.append(getEncoding());
		sb.append(UNIT_SEP);
		sb.append(getDescription());
		sb.append(UNIT_SEP);
		sb.append(getLatency());
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

	/** Check if the camera is blank */
	private boolean isCameraBlank() {
		if (camera != null) {
			Integer cn = camera.getCamNum();
			return (cn != null) && (cn == cameraNumBlank());
		} else
			return true;
	}

	/** Get the stream URI */
	private String getUri() {
		if (isCameraBlank())
			return getBlankUrl();
		if (camera != null) {
			String cond = getConditionUri();
			if (cond != null)
				return cond;
			String mcast = getMulticastUri();
			if (mcast != null)
				return mcast;
			return CameraHelper.encoderUri(camera, "").toString();
		} else
			return "";
	}

	/** Get the condition URI */
	private String getConditionUri() {
		switch (getCondition()) {
		case CONSTRUCTION:
			return getConstructionUrl();
		case PLANNED:
		case REMOVED:
			return getOutOfServiceUrl();
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

	/** Get camera multicast URI */
	private String getMulticastUri() {
		assert camera != null;
		String mcast = camera.getEncMulticast();
		if (mcast != null && mcast.length() > 0)
			return URIUtil.create(URIUtil.UDP, mcast).toString();
		else
			return null;
	}

	/** Get the encoding */
	private String getEncoding() {
		if (CtrlCondition.ACTIVE == getCondition()) {
			assert camera != null;
			EncoderType et = camera.getEncoderType();
			if (et != null) {
				return Encoding.fromOrdinal(et.getEncoding())
				               .toString();
			}
		}
		return "PNG";
	}

	/** Get the stream description */
	private String getDescription() {
		return isCameraBlank()
		      ? ""
		      : GeoLocHelper.getDescription(camera.getGeoLoc());
	}

	/** Get the stream latency (ms) */
	private int getLatency() {
		if (camera != null) {
			EncoderType et = camera.getEncoderType();
			if (et != null)
				return et.getLatency();
		}
		return EncoderType.DEFAULT_LATENCY_MS;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "switch: " + camera;
	}
}
