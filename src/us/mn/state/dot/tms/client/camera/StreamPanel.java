/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

/**
 * A JPanel that can display a video stream.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
abstract public class StreamPanel extends JPanel {

	/** Size of a quarter SIF */
	static protected final Dimension SIF_QUARTER = new Dimension(176, 120);

	/** Size of a full SIF */
	static protected final Dimension SIF_FULL = new Dimension(352, 240);

	/** Size of 4 x SIF */
	static protected final Dimension SIF_4X = new Dimension(704, 480);

	/** Progress bar for duration */
	protected final JProgressBar progress = new JProgressBar(0, 100);

	/** Size of video image */
	protected Dimension imageSize = new Dimension(SIF_FULL);

	/** Create a new stream panel */
	public StreamPanel() {
		super(new BorderLayout());
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
//		p.add(screen, c);
		p.add(progress, c);
		add(p);
		setVideoSize(imageSize);
//		screen.setBorder(BorderFactory.createBevelBorder(
//			BevelBorder.LOWERED));
//		thread.start();
	}

	static public StreamPanel getInstance(){
		try{
			Class.forName("org.gstreamer.Gst");
			return new GstManager();
		}catch(ClassNotFoundException cnfe){
			return new JavaManager();
		}
	}
	
	abstract void requestStream(VideoRequest req, String camId);

	abstract void clearStream();

	/** Set the dimensions of the video stream */
	protected void setVideoSize(Dimension d) {
		imageSize = d;
//		screen.setPreferredSize(d);
//		screen.setMinimumSize(d);
	}

}
