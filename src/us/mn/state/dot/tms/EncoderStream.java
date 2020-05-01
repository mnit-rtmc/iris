/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.SonarObject;

/**
 * Video encoder stream.
 *
 * @author Douglas Lau
 */
public interface EncoderStream extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "encoder_stream";

	/** Default buffering latency */
	int DEFAULT_LATENCY_MS = 50;

	/** Get the encoder type */
	EncoderType getEncoderType();

	/** Set the view number */
	void setViewNum(Integer n);

	/** Get the view number */
	Integer getViewNum();

	/** Set the flow stream flag */
	void setFlowStream(boolean fs);

	/** Get the flow stream flag */
	boolean getFlowStream();

	/** Set the encoding ordinal */
	void setEncoding(int e);

	/** Get the encoding ordinal */
	int getEncoding();

	/** Set the encoding quality ordinal */
	void setQuality(int q);

	/** Get the encoding quality ordinal */
	int getQuality();

	/** Set the URI scheme */
	void setUriScheme(String s);

	/** Get the URI scheme */
	String getUriScheme();

	/** Set the URI path */
	void setUriPath(String p);

	/** Get the URI path*/
	String getUriPath();

	/** Set the multicast port */
	void setMcastPort(Integer p);

	/** Get the multicast port */
	Integer getMcastPort();

	/** Set stream latency (ms) */
	void setLatency(int l);

	/** Get stream latency (ms) */
	int getLatency();
}
