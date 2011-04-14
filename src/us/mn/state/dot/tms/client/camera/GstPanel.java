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

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.gstreamer.Element;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.swing.VideoComponent;

import us.mn.state.dot.tms.Camera;

/**
 * A GstManager is responsible for managing video streams using the GStreamer-java library.
 *
 * @author Tim Johnson
 */

final public class GstPanel extends StreamPanel {

	static boolean gstInitialized = false;
	static VideoComponent screen = null;
	static Pipeline pipe = null;

	protected GstPanel(){
		if(!gstInitialized){
			String[] args = {};
			Gst.init("IRIS", args);
		}
	}
	
	private String createPipeString(Camera cam){
		String encoder = cam.getEncoder();
		if(encoder == null) return null;
		encoder = encoder.substring(0,encoder.indexOf(':'));
		StringBuilder sb = new StringBuilder();
		sb.append("rtspsrc ");
		sb.append("location=rtsp://" + encoder + ":554/mpeg4/1/media.amp ");
		sb.append("latency=0 name=rtspsrc_1 ! decodebin name=db1 ! ffmpegcolorspace name=" + cam.getName());
		return sb.toString();
	}

	private Element getSink(){
		return screen.getElement();
	}
	
	private synchronized void connect(Camera cam, Element sink, Pipeline pipe){
		pipe.add(sink);
		System.out.println("Adding initial sink.");
		pipe.getElementByName(cam.getName()).link(sink);
		pipe.setState(State.PAUSED);
		pipe.play();
		pipe.setState(State.PLAYING);
		System.out.println("\t" + cam.getName() + " ---> " + sink.getName());
	}

	public void requestStream(final VideoRequest req, final Camera cam){
		final JPanel screenPanel = this.screenPanel;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println("Streaming camera " + cam.getName());
				if(pipe != null){
					pipe.stop();
					pipe.setState(State.NULL);
				}
				screenPanel.removeAll();
				screenPanel.repaint();
				screen = new VideoComponent();
				screen.setPreferredSize(screenPanel.getPreferredSize());
				screenPanel.add(screen);
				screenPanel.doLayout();
				pipe = Pipeline.launch(createPipeString(cam));
				connect(cam, screen.getElement(), pipe);
			}
		});
	}

	public void clearStream(){
		final JPanel screenPanel = this.screenPanel;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(pipe != null){
					System.out.println("Stopping stream");
					pipe.stop();
					pipe.setState(State.NULL);
				}
				screenPanel.removeAll();
				screenPanel.repaint();
			}
		});
	}
}
