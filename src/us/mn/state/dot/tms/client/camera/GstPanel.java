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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.Gst;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
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

	/** The last element in the pipe which connects to the sink */
	static protected final String DECODER = "decoder";

	static boolean gstInitialized = false;

	protected Pipeline pipe = null;

	protected Timer timer = null;

	/** Seconds of video elapsed */
	int seconds = 0;

	/** Milliseconds between updates to the progress */
	int delay = 1000;

	ActionListener taskPerformer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			progress.setValue(++seconds);
			if(seconds > progress.getMaximum()) {
				disconnect();
			}
		}
	};

	/** Create a new gstreamer stream panel */
	protected GstPanel(Dimension sz) {
		super(sz);
		if(!gstInitialized) {
			String[] args = {};
			Gst.init("IRIS", args);
		}
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
		timer = new Timer(delay, taskPerformer);
		timer.start();
	}

	/** This method should only be called on the swing thread */
	private void disconnect() {
		Timer t = timer;
		if(t != null) {
			t.stop();
			timer = null;
		}
		Pipeline p = pipe;
		if(p != null) {
			p.stop();
			p.setState(State.NULL);
			pipe = null;
		}
		screenPanel.removeAll();
		screenPanel.repaint();
		seconds = 0;
		progress.setValue(seconds);
		streamLabel.setText(null);
	}

	/** Request a new video stream */
	public void requestStream(final VideoRequest req, final Camera c) {
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
	}

	/** Request a new video stream */
	protected void doRequestStream(VideoRequest req, Camera c)
		throws MalformedURLException
	{
		disconnect();
		pipe = Pipeline.launch(createPipeString(req.getUrl(c)));
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
		int w = 0;
		int h = 0;
		String encoding = "";
		int fps = 0;
		for(Element e: pipe.getElements()) {
			if(e instanceof RGBDataSink)
				continue; // no useful info from sink
			if(e.getName().startsWith("souphttp"))
				return MJPEG;
			for(Pad p: e.getSrcPads()) {
				Caps c = p.getCaps();
				if(c.size() > 0) {
					String capDesc = c.getStructure(0).getName();
					for(int i = 0; i < c.size(); i++) {
						if(capDesc.startsWith("video")) {
							w = c.getStructure(i).getInteger("width");
							h = c.getStructure(i).getInteger("height");
						} else if(capDesc.startsWith("application")) {
							encoding = c.getStructure(i).getString("encoding-name");
							fps = (Float.valueOf(c.getStructure(i).getString("a-framerate"))).intValue();
						}
					}
				}
			}
		}
		if(encoding.startsWith("MP4V"))
			encoding = MPEG4;
		return encoding + " (" + w + "x" + h + ")";
	}

	public void clearStream() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				disconnect();
				streamLabel.setText("Stopped");
				statusPanel.doLayout();
			}
		});
	}
}
