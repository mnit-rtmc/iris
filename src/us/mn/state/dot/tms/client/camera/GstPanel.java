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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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

/**
 * A GstManager is responsible for managing video streams using the GStreamer-java library.
 *
 * @author Tim Johnson
 */

final public class GstPanel extends StreamPanel {

	static boolean gstInitialized = false;
	static VideoComponent screen = null;
	static Pipeline pipe = null;

	/** The name of the last element in the pipe which connects to the sink. */
	static final String DECODER = "decoder";

	Timer timer = null;
	
	/** Seconds of video elapsed */
	int seconds = 0;
	/** Milliseconds between updates to the progress */
	int delay = 1000;
	ActionListener taskPerformer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			progress.setValue(++seconds);
			if(seconds > progress.getMaximum()){
				disconnect();
			}
		}
	};

	protected GstPanel(){
		if(!gstInitialized){
			String[] args = {};
			Gst.init("IRIS", args);
		}
	}
	
	private String createPipeString(String urlString){
		if(urlString == null) return null;
		StringBuilder sb = new StringBuilder();
		if(urlString.startsWith("rtsp")){
			sb.append("rtspsrc ");
			sb.append("location=" + urlString + " ");
			sb.append("latency=0 ! decodebin ! ffmpegcolorspace ");
		}else if(urlString.startsWith("http")){
			sb.append("souphttpsrc ");
			sb.append("location=" + urlString + " ");
			sb.append("timeout=5 ! jpegdec ");
		}
		sb.append("name=" + DECODER);
		return sb.toString();
	}

	/** This method should only be called on the swing thread. */
	private synchronized void connect(){
		screen = new VideoComponent();
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

	/** This method should only be called on the swing thread. */
	private void disconnect(){
		if(pipe != null){
			pipe.stop();
			pipe.setState(State.NULL);
		}
		screenPanel.removeAll();
		screenPanel.repaint();
		if(timer != null) timer.stop();
		seconds = 0;
		progress.setValue(seconds);
		streamLabel.setText(null);
	}

	public void requestStream(final VideoRequest req){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				disconnect();
				String urlString = req.getUrlString(MPEG4);
				if(urlString != null) pipe = Pipeline.launch(createPipeString(urlString));
				connect();
				if(!pipe.isPlaying()){
					disconnect();
					urlString = req.getUrlString(MJPEG);
					if(urlString != null) pipe = Pipeline.launch(createPipeString(urlString));
					connect();
				}
				statusPanel.doLayout();
				screenPanel.doLayout();
				updateStatus();
			}
		});
	}

	private void updateStatus(){
		int w = 0;
		int h = 0;
		String encoding = "";
		int fps = 0;
		List<Element> elements = pipe.getElements();
		for(Element e : elements){
			if(e instanceof RGBDataSink) continue; //no useful info from sink
			if(e.getName().startsWith("souphttp")){
				streamLabel.setText(MJPEG);
				return;
			}
			List<Pad> pads = e.getSrcPads();
			for(Pad p : pads){
				Caps c = p.getCaps();
				if(c.size() > 0){
					String capDesc = c.getStructure(0).getName();
					try{
						for(int i=0; i<c.size(); i++){
							if(capDesc.startsWith("video")){
								w = c.getStructure(i).getInteger("width");
								h = c.getStructure(i).getInteger("height");
							}else if(capDesc.startsWith("application")){
								encoding = c.getStructure(i).getString("encoding-name");
								fps = (Float.valueOf(c.getStructure(i).getString("a-framerate"))).intValue();
							}
						}
					}catch(Exception ex){}
				}
			}
		}
		if(encoding.startsWith("MP4V")) encoding = MPEG4;
		streamLabel.setText(encoding + " (" + w + "x" + h + ")");
	}
	
	public void clearStream(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				disconnect();
				streamLabel.setText("Stopped");
				statusPanel.doLayout();
			}
		});
	}
}
