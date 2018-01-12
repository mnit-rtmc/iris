/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2017  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2015-2018  SRF Consulting Group
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

import java.net.URI;
import java.util.Properties;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.Encoding;
import static us.mn.state.dot.tms.utils.URIUtil.create;
import static us.mn.state.dot.tms.utils.URIUtil.HTTP;
import static us.mn.state.dot.tms.utils.URIUtil.RTSP;

/**
 * The video stream request parameter wrapper.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 * @author Travis Swanston
 * @author John L. Stanley - SRF Consulting
 */
public class VideoRequest {

	/** MnDOT Servlet type enum */
	static public enum ServletType {
		STREAM("stream"), STILL("image");

		private final String servlet;
		private ServletType(String srv) {
			servlet = srv;
		}
	}

	/** Video stream image-size enum */
	static public enum Size {
		THUMBNAIL(132, 90, 's'),	// Thumbnail
		SMALL(176, 120, 's'),		// Quarter SIF
		MEDIUM(352, 240, 'm'),		// Full SIF
		LARGE(704, 480, 'l');		// 4x SIF
		private Size(int w, int h, char c) {
			width = w;
			height = h;
			code = c;
		}
		public final int width;
		public final int height;
		public final char code;
		/* For Axis Q1602, if the resolution is not between 160x90 and
		 * 768x576 we will get HTTP error 400 (Bad Request) */
		private int getWidthReq() {
			return Math.max(160, Math.min(width, 768));
		}
		private int getHeightReq() {
			return Math.max(90, Math.min(height, 576));
		}
		public String getResolution() {
			return "" + getWidthReq() + 'x' + getHeightReq();
		}
	}

	/** Client property name: software used for the video proxy */
	static private final String VIDEO_PROXY = "video.proxy";

	/** Client property name: video host (dns name or IP address) */
	static private final String VIDEO_HOST = "video.host";

	/** Client property name: video port */
	static private final String VIDEO_PORT = "video.port";

	/** Create a url for connecting to the video server.
	 * @param p Properties */
	private String createBaseUrl(Properties p) {
		String host = p.getProperty(VIDEO_HOST);
		String port = p.getProperty(VIDEO_PORT);
		return (host != null) ? createBaseUrl(host, port) : null;
	}

	/** Create a url for connecting to the video server */
	private String createBaseUrl(String host, String port) {
		return (port != null) ? (host + ":" + port) : host;
	}

	/** Sonar session identifier for authenticating to the video system */
	private long sonarSessionId = -1;

	/** Get the SONAR session ID */
	public long getSonarSessionId() {
		return sonarSessionId;
	}

	/** Set the SONAR session ID */
	public void setSonarSessionId(long ssid) {
		sonarSessionId = ssid;
	}

	/** Stream size */
	private final Size size;

	/** Get the stream size */
	public Size getSize() {
		return size;
	}

	/** Name of the software used for the video proxy */
	private final String video_proxy;
	private static final String SERVLET_MNDOT = "Servlet_MnDOT";
	private static final String LIVE555_SRF   = "Live555_SRF";

	/** The base URL of the video proxy */
	private final String base_url;

	/** District ID */
	private final String district;

	/** Servlet type */
	private final ServletType servlet_type = ServletType.STREAM;

	/** Create a new video request */
	public VideoRequest(Properties p, Size sz) {
		base_url = createBaseUrl(p);
		district = p.getProperty("district", "tms");
		video_proxy = p.getProperty(VIDEO_PROXY, SERVLET_MNDOT);
		size = sz;
	}

	/** Create a URI for a stream */
	public URI getUri(Camera c) {
		return useProxy() ? getProxyUri(c) : getCameraUri(c);
	}

	/** Test if a proxy is being used */
	private boolean useProxy() {
		return base_url != null;
	}

	/** Get camera name modified for use in
	  * a live555 videoProxy rtsp uri string */
	private String getLive555CamName(Camera cam) {
		String camName = cam.getName();
		int len = camName.length();
		StringBuilder newCamName = new StringBuilder(len);
		char ch;
		for (int i = 0; (i < len); ++i) {
			ch = camName.charAt(i);
			if (ch >= 127) // replace any non-ASCII character
				newCamName.append('_');
			else if (Character.isLetterOrDigit(ch)
			      || (ch == '.')
			      || (ch == '-')
			      || (ch == '_')
			      || (ch == '~'))
				newCamName.append(ch);
			else
				newCamName.append('_');
		}
		return newCamName.toString();
	}
	
	/** Create a video proxy URI */
	private URI getProxyUri(Camera cam) {
		// Using a Live555_SRF video proxy?
		if (video_proxy.equalsIgnoreCase(LIVE555_SRF)) {
			return create(RTSP, base_url +
		                        "/" + getLive555CamName(cam));
		}
		// No, we're using a Servlet_MnDOT video proxy...
		return create(HTTP, base_url +
		                    "/video/" + servlet_type.servlet +
		                    "/" + district +
		                    "/" + cam.getName() +
		                    "?size=" + size.code +
		                    "&ssid=" + sonarSessionId);
	}

	/** Create a camera encoder URI */
	public URI getCameraUri(Camera cam) {
		return CameraHelper.encoderUri(cam, getQuery(cam));
	}

	/** Create camera encoder URI query part */
	private String getQuery(Camera cam) {
		/* NOTE: query only required for older Axis encoders */
		if (isAxisEncoder(cam)) {
			/* NOTE: showlength needed to force ancient (2401)
			 *       servers to provide Content-Length headers */
			return "?camera=" + cam.getEncoderChannel()
			     + "&resolution=" + size.getResolution()
			     + "&showlength=1";
		} else
			return "";
	}

	/** Check if encoder type is AXIS */
	private boolean isAxisEncoder(Camera cam) {
		EncoderType et = cam.getEncoderType();
		return (et != null)
		      ? et.getName().toUpperCase().contains("AXIS")
		      : false;
	}

	/** Check if encoding is MJPEG.
	 * @param c Camera.
	 * @return true if encoding is motion JPEG. */
	public boolean hasMJPEG(Camera c) {
		return (c != null) && (getEncoding(c) == Encoding.MJPEG);
	}

	/** Get the encoding for a camera */
	private Encoding getEncoding(Camera c) {
		Encoding enc = getEncoding(c.getEncoderType());
		if (enc != Encoding.UNKNOWN && useProxy())
			return Encoding.MJPEG;
		else
			return enc;
	}

	/** Get the encoding for an encoder type */
	private Encoding getEncoding(EncoderType et) {
		return (et != null)
		      ? Encoding.fromOrdinal(et.getEncoding())
		      : Encoding.UNKNOWN;
	}
}
