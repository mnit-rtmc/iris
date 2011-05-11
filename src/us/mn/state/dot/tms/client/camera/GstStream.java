/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import java.io.IOException;
import javax.swing.JComponent;
import org.gstreamer.Bus;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.Structure;
import org.gstreamer.elements.RGBDataSink;
import org.gstreamer.swing.VideoComponent;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.EncoderType;

/**
 * A GstStream is a video stream which can read streams handled by the
 * GStreamer-java library.
 *
 * @author Douglas Lau
 * @author Tim Johnson
 */
public class GstStream implements VideoStream {

	/** Initialize gstreamer when loaded */
	static {
		Gst.init();
	}

	/** Component to display video stream */
	private final VideoComponent screen = new VideoComponent();

	/** Gstreamer pipeline */
	private final Pipeline pipe;

	/** Listener for Gstreamer bus errors */
	protected final Bus.ERROR error_listener = new Bus.ERROR() {
		public void errorMessage(GstObject src, int code, String msg) {
			// Only record first error on stream
			if(error_msg == null)
				error_msg = msg;
			pipe.stop();
		}
	};

	/** Stream status */
	private final String status;

	/** Stream error message */
	private String error_msg = null;

	/** Create a new gstreamer stream */
	public GstStream(VideoRequest req, Camera c) {
		pipe = createPipe(createSource(req, c));
		pipe.getBus().connect(error_listener);
		pipe.setState(State.PLAYING);
		status = getStreamStatus();
	}

	/** Create the pipeline */
	private Pipeline createPipe(String src) {
		Pipeline p = Pipeline.launch(src + " ! decodebin2 ! " +
			"ffmpegcolorspace name=tail");
		Element filter = ElementFactory.make("capsfilter", "filter");
		// NOTE: Ideally, this filter should not be necessary, but the
		//       red_mask and blue_mask need to be specified in order to
		//       work around a bug in gstreamer-java VideoComponent
		filter.setCaps(Caps.fromString("video/x-raw-rgb, bpp=32, " +
			"depth=24, red_mask=0xFF00, green_mask=0xFF0000, " +
			"blue_mask=0xFF000000")); 
		p.add(filter);
		p.getElementByName("tail").link(filter);
		Element sink = screen.getElement();
		p.add(sink);
		filter.link(sink);
		return p;
	}

	/** Create a source element */
	private String createSource(VideoRequest req, Camera c) {
		switch(EncoderType.fromOrdinal(c.getEncoderType()).stream_type){
		case MJPEG:
			return "souphttpsrc location=" + req.getUrl(c) +
				" timeout=5";
		case MPEG4:
			return "rtspsrc location=" + req.getUrl(c) +
				" latency=0";
		default:
			return "fakesrc";
		}
	}

	/** Get the stream status */
	private String getStreamStatus() {
		String enc = "";
		int w = 0;
		int h = 0;
		for(Element e: pipe.getElements()) {
			if(e instanceof RGBDataSink)
				continue; // no useful info from sink
			if(e.getName().startsWith("souphttp"))
				return VideoStream.MJPEG;
			for(Pad p: e.getSrcPads()) {
				Caps c = p.getCaps();
				if(c.size() > 0) {
					String n = c.getStructure(0).getName();
					if(n.startsWith("application"))
						enc = getEncoding(c, enc);
					if(n.startsWith("video")) {
						w = getWidth(c, w);
						h = getHeight(c, h);
					}
				}
			}
		}
		if(enc.startsWith("MP4V"))
			enc = VideoStream.MPEG4;
		return enc + " (" + w + "x" + h + ")";
	}

	/** Get width from Caps */
	static protected int getWidth(Caps c, int w) {
		for(int i = 0; i < c.size(); i++) {
			Structure s = c.getStructure(i);
			if(s.hasIntField("width"))
				return s.getInteger("width");
		}
		return w;
	}

	/** Get height from Caps */
	static protected int getHeight(Caps c, int h) {
		for(int i = 0; i < c.size(); i++) {
			Structure s = c.getStructure(i);
			if(s.hasIntField("height"))
				return s.getInteger("height");
		}
		return h;
	}

	/** Get encoding from Caps */
	static protected String getEncoding(Caps c, String enc) {
		for(int i = 0; i < c.size(); i++) {
			Structure s = c.getStructure(i);
			if(s.hasField("encoding-name"))
				return s.getString("encoding-name");
		}
		return enc;
	}

	/** Get a component for displaying the video stream */
	public JComponent getComponent() {
		return screen;
	}

	/** Get the status of the stream */
	public String getStatus() {
		String e = error_msg;
		if(e != null)
			return e;
		else
			return status;
	}

	/** Test if the video is playing */
	public boolean isPlaying() {
		return pipe.getState() == State.PLAYING;
	}

	/** Dispose of the video stream */
	public void dispose() {
		pipe.stop();
		pipe.setState(State.NULL);
		pipe.getBus().disconnect(error_listener);
	}
}
