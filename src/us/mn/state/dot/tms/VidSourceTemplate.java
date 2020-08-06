/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  SRF Consulting Group
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

/** A VidSourceTemplate contains a template form
 *  of information needed to open a GStreamer
 *  or MJPEG connection to an RTSP or
 *  UDP-multicast source.
 *
 *  Additional information needed to complete
 *  the template comes from the camera record,
 *  the client.properties table, and/or the
 *  system attribute table.
 *
 *  The many-to-many links between camera-template(s)
 *  and vid-source-template(s) are saved in the
 *  camera_source_order table.
 *
 * @author John L. Stanley - SRF Consulting
 */
public interface VidSourceTemplate extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "vid_src_template";

	/** Get the stream-type label shown in the video window.
	 * @return the label
	 */
	String getLabel();

	/** Set the stream-type label shown in the video window.
	 * @param label the label to set
	 */
	void setLabel(String label);

	/** Get the raw config string from the template */
	String getConfig();

	/** Set the raw config string in the template */
	void setConfig(String c);

	/**
	 * @return the defaultPort (or null)
	 */
	Integer getDefaultPort();

	/**
	 * @param defaultPort the defaultPort to set (or null)
	 */
	void setDefaultPort(Integer defaultPort);

	/** Get string containing a comma separated list of subnets.
	 * @return the subnets (or null)
	 */
	String getSubnets();

	/** Set string containing a comma separated list of subnets.
	 * @param subnets the subnets to set (or null)
	 */
	void setSubnets(String subnets);

	/** Get the latency
	 * @return the latency (or null)
	 */
	Integer getLatency();

	/** Set the latency.
	 * @param latency the latency to set (or null)
	 */
	void setLatency(Integer latency);

	/** Get the encoder-type string.
	 * @return the encoder type (or null)
	 */
	String getEncoder();

	/** Set the encoder-type string.
	 * @param encoder the encoder type to set (or null)
	 */
	void setEncoder(String encoder);

	/** Get the transport scheme.
	 * @return the scheme (or null)
	 */
	String getScheme();

	/** Set the transport scheme.
	 * @param scheme the scheme to set (or null)
	 */
	void setScheme(String scheme);

	/** Get the codec name.
	 * @return the codec name (or null)
	 */
	String getCodec();

	/** Set the codec name.
	 * @param codec the codec to set (or null)
	 */
	void setCodec(String codec);

	/** Get the native pixel width of the stream.
	 * @return the rezWidth (or null)
	 */
	Integer getRezWidth();

	/** Set the native pixel width of the stream.
	 * @param rezWidth the rezWidth to set (or null)
	 */
	void setRezWidth(Integer rezWidth);

	/** Get the native pixel height of the stream.
	 * @return the rezHeight (or null)
	 */
	Integer getRezHeight();

	/** Set the native pixel height of the stream.
	 * @param rezHeight the rezHeight to set (or null)
	 */
	void setRezHeight(Integer rezHeight);

	/** Get multicast-stream flag.
	 * @return the multicast flag (or null)
	 */
	Boolean getMulticast();

	/** Set multicast-stream flag.
	 * @param multicast the multicast flag to set (or null)
	 */
	void setMulticast(Boolean multicast);

	/** Get the notes for this template.
	 * @return the notes string (or null)
	 */
	String getNotes();

	/** Set the notes for this template.
	 * @param notes the notes string (or null)
	 */
	void setNotes(String notes);
}
