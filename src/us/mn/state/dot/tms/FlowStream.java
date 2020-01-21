/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * A video flow stream.
 *
 * @author Douglas Lau
 */
public interface FlowStream extends ControllerIO {

	/** SONAR type name */
	String SONAR_TYPE = "flow_stream";

	/** Set flag to restrict publishing camera images */
	void setRestricted(boolean r);

	/** Get flag to restrict publishing camera images */
	boolean getRestricted();

	/** Set flag to enable location overlay text */
	void setLocOverlay(boolean lo);

	/** Get flag to enable location overlay text */
	boolean getLocOverlay();

	/** Set the encoding quality ordinal */
	void setQuality(int q);

	/** Get the encoding quality ordinal */
	int getQuality();

	/** Set the source camera */
	void setCamera(Camera c);

	/** Get the source camera */
	Camera getCamera();

	/** Set the source monitor number */
	void setMonNum(Integer mn);

	/** Get the source monitor number */
	Integer getMonNum();

	/** Set the sink address */
	void setAddress(String addr);

	/** Get the sink address */
	String getAddress();

	/** Set the sink port */
	void setPort(Integer p);

	/** Get the sink port */
	Integer getPort();

	/** Get the flow stream status ordinal */
	int getStatus();
}
