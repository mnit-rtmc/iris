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

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.SwingUtilities;
import org.gstreamer.Bus;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.Structure;
import org.gstreamer.elements.RGBDataSink;
import org.gstreamer.swing.VideoComponent;
import us.mn.state.dot.tms.Camera;

/**
 * A GstManager is responsible for managing video streams using the
 * GStreamer-java library.
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class GstPanel extends StreamPanel {

	/** Initialize gstreamer when loaded */
	static {
		Gst.init();
	}

	/** The last element in the pipe which connects to the sink */
	static protected final String DECODER = "decoder";

	protected Pipeline pipe = null;

	/** Listener for gstreamer Bus errors */
	protected final Bus.ERROR error_listener = new Bus.ERROR() {
		public void errorMessage(GstObject src, int code, String msg) {
			System.err.println("gstreamer error: " + msg);
		}
	};

	/** Create a new gstreamer stream panel */
	protected GstPanel(Dimension sz) {
		super(sz);
	}

	private String createPipeString(URL url) {
		StringBuilder sb = new StringBuilder();
		if(url.toString().startsWith("rtsp")) {
			sb.append("rtspsrc ");
			sb.append("location=" + url + " ");
			sb.append("latency=0 ! decodebin ! ffmpegcolorspace ");
		} else if(url.toString().startsWith("http")) {
			sb.append("souphttpsrc ");
			sb.append("location=" + url + " ");
			sb.append("timeout=5 ! jpegdec ");
		}
		sb.append("name=" + DECODER);
		return sb.toString();
	}

	/** This method should only be called on the swing thread */
	private void connect() {
		VideoComponent screen = new VideoComponent();
		screenPanel.add(screen);
		screen.setPreferredSize(screenPanel.getPreferredSize());
		screen.doLayout();
		pipe.add(screen.getElement());
		pipe.getElementByName(DECODER).link(screen.getElement());
		pipe.setState(State.PAUSED);
		pipe.play();
		pipe.setState(State.PLAYING);
	}

	/** This method should only be called on the swing thread */
	private void disconnect() {
		Pipeline p = pipe;
		if(p != null) {
			p.stop();
			p.setState(State.NULL);
			pipe = null;
		}
		screenPanel.removeAll();
		screenPanel.repaint();
		streamLabel.setText(null);
	}

	/** Request a new video stream */
	protected void requestStream(final VideoRequest req, final Camera c) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					doRequestStream(req, c);
				}
				catch(MalformedURLException e) {
					streamLabel.setText(e.getMessage());
				}
			}
		});
		super.requestStream(req, c);
	}

	/** Request a new video stream */
	protected void doRequestStream(VideoRequest req, Camera c)
		throws MalformedURLException
	{
		disconnect();
		pipe = Pipeline.launch(createPipeString(req.getUrl(c)));
		pipe.getBus().connect(error_listener);
		connect();
		statusPanel.doLayout();
		screenPanel.doLayout();
		streamLabel.setText(getStreamStatus());
	}

	/** Check if the stream is playing */
	protected boolean isPlaying() {
		Pipeline p = pipe;
		return p != null && p.isPlaying();
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
				return MJPEG;
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
			enc = MPEG4;
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

	protected void clearStream() {
		super.clearStream();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				disconnect();
				streamLabel.setText("Stopped");
				statusPanel.doLayout();
			}
		});
	}
}
