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

/**
 * A GstManager is responsible for managing video streams using the GStreamer-java library.
 *
 * @author Tim Johnson
 */

final public class GstManager extends StreamManager {

	static boolean gstInitialized = false;
	static VideoComponent vc = null;
	static Pipeline pipe = null;

	protected GstManager(){
		if(!gstInitialized){
			String[] args = {};
			Gst.init("IRIS", args);
		}
	}
	
	private String createPipeString(String camId){
		StringBuilder sb = new StringBuilder();
		sb.append("rtspsrc ");
		sb.append("location=rtsp://root:video@151.111.8.155:554/mpeg4/1/media.amp ");
		sb.append("latency=0 name=rtspsrc_1 ! decodebin name=db1 ! ffmpegcolorspace name=" + camId);
		return sb.toString();
	}

	protected Element getSink(){
		return vc.getElement();
	}
	
	private synchronized void connect(String camId, Element sink, Pipeline pipe){
		pipe.add(sink);
		System.out.println("Adding initial sink.");
		pipe.getElementByName(camId).link(sink);
		pipe.setState(State.PAUSED);
		pipe.play();
		pipe.setState(State.PLAYING);
		System.out.println("\t" + camId + " ---> " + sink.getName());
	}

	public void requestStream(final VideoRequest req, final String camId, final JPanel displayPanel){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println("Streaming camera " + camId);
				if(pipe != null){
					pipe.stop();
					pipe.setState(State.NULL);
				}
				displayPanel.removeAll();
				displayPanel.repaint();
				vc = new VideoComponent();
				vc.setPreferredSize(new Dimension(720, 576));
				displayPanel.add(vc);
				displayPanel.doLayout();
				pipe = Pipeline.launch(createPipeString(camId));
				connect(camId,vc.getElement(),pipe);
			}
		});
	}

	public void clearStream(final JPanel displayPanel){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(pipe != null){
					System.out.println("Stopping stream");
					pipe.stop();
					pipe.setState(State.NULL);
				}
				displayPanel.removeAll();
				displayPanel.repaint();
			}
		});
	}
}
