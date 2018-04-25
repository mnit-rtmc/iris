/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MonitorStyle;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * A property to setup a monitor.
 *
 * @author Douglas Lau
 */
public class MonitorProp extends MonProp {

	/** Get a monitor style */
	static private MonitorStyle monitorStyle(VideoMonitorImpl mon) {
		return (mon != null)
		      ? mon.getMonitorStyle()
		      : null;
	}

	/** Controller pin */
	private final int pin;

	/** Video monitor */
	private final VideoMonitorImpl mon;

	/** Extra monitor numbers (full-screen) */
	private final String extra;

	/** Create a new monitor prop */
	public MonitorProp(int p, VideoMonitorImpl vm, String ex) {
		pin = p;
		mon = vm;
		extra = ex;
	}

	/** Create a new monitor prop */
	public MonitorProp(int p, VideoMonitorImpl vm) {
		this(p, vm, "");
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		tx_buf.put(formatReq().getBytes("UTF8"));
	}

	/** Format a config request */
	private String formatReq() {
		StringBuilder sb = new StringBuilder();
		sb.append("monitor");
		sb.append(UNIT_SEP);
		sb.append(pin - 1);
		sb.append(UNIT_SEP);
		sb.append(getMonLabel());
		sb.append(UNIT_SEP);
		sb.append(getAccent());
		sb.append(UNIT_SEP);
		sb.append(getForceAspect());
		sb.append(UNIT_SEP);
		sb.append(Integer.toString(getFontSz()));
		sb.append(UNIT_SEP);
		sb.append("AAAA");	// FIXME
		sb.append(UNIT_SEP);
		sb.append(Integer.toString(getHGap()));
		sb.append(UNIT_SEP);
		sb.append(Integer.toString(getVGap()));
		sb.append(UNIT_SEP);
		sb.append(extra);
		sb.append(RECORD_SEP);
		return sb.toString();
	}

	/** Get monitor label as a string */
	private String getMonLabel() {
		if (getTitleBar()) {
			assert mon != null;
			int n = mon.getMonNum();
			if (n > 0)
				return Integer.toString(n);
			else
				return mon.getName();
		} else
			return "";
	}

	/** Get the monitor title bar */
	private boolean getTitleBar() {
		MonitorStyle ms = monitorStyle(mon);
		return (ms != null) && ms.getTitleBar();
	}

	/** Get monitor accent color */
	private String getAccent() {
		MonitorStyle ms = monitorStyle(mon);
		return (ms != null)
		      ? ms.getAccent()
		      : MonitorStyle.DEFAULT_ACCENT;
	}

	/** Get force-aspect as a string */
	private String getForceAspect() {
		MonitorStyle ms = monitorStyle(mon);
		return (ms != null && ms.getForceAspect()) ? "1" : "0";
	}

	/** Get the monitor font size */
	private int getFontSz() {
		MonitorStyle ms = monitorStyle(mon);
		return (ms != null)
		      ? ms.getFontSz()
		      : MonitorStyle.DEFAULT_FONT_SZ;
	}

	/** Get the horizontal gap */
	private int getHGap() {
		MonitorStyle ms = monitorStyle(mon);
		return (ms != null) ? ms.getHGap() : 0;
	}

	/** Get the vertical gap */
	private int getVGap() {
		MonitorStyle ms = monitorStyle(mon);
		return (ms != null) ? ms.getVGap() : 0;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "monitor: " + pin;
	}
}
