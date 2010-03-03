/*
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
package us.mn.state.dot.tms.client.camera.stream;

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
public class StreamPanel extends JPanel {

	static protected final Dimension SIF_QUARTER = new Dimension(176, 120);
	static protected final Dimension SIF_FULL = new Dimension(352, 240);
	static protected final Dimension SIF_4X = new Dimension(704, 480);

	private VideoStream stream = null;
	private int imagesRendered = 0;
	private final JLabel screen = new JLabel();
	private JProgressBar progress = new JProgressBar(0, 100);
	private int imagesRequested = 0;
	private Dimension imageSize = new Dimension(SIF_FULL);

	private final Thread thread = new Thread() {
		public void run() {
			while(true) {
				VideoStream vs = stream;
				if(vs != null)
					readStream(vs);
				synchronized(thread) {
					try {
						thread.wait();
					}
					catch(InterruptedException e) {
						// nothing to do
					}
				}
			}
		}
		protected void readStream(VideoStream vs) {
			try {
				while(vs == stream) {
					byte[] im = vs.getImage();
					if(im != null)
						flush(im);
					else
						break;
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			finally {
				vs.close();
			}
		}
	};

	/** Create a new stream panel */
	public StreamPanel() {
		super(new BorderLayout());
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		p.add(screen, c);
		p.add(progress, c);
		add(p);
		setVideoSize(imageSize);
		screen.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		thread.start();
	}

	/** Set the dimensions of the video stream */
	protected void setVideoSize(Dimension d) {
		imageSize = d;
		screen.setPreferredSize(d);
		screen.setMinimumSize(d);
	}

	public void setVideoStream(VideoStream vs, int f) {
		stream = vs;
		imagesRequested = f;
		progress.setMaximum(imagesRequested);
		progress.setValue(0);
		synchronized(thread) {
			thread.notify();
		}
	}

	protected void flush(byte[] i) {
		setImage(new ImageIcon(i));
		progress.setValue(imagesRendered);
		imagesRendered++;
		if(imagesRendered >= imagesRequested) {
			stream = null;
			clear();
		}
	}

	/** Set the image to be displayed on the panel
	 * @param image  The image to display. */
	private synchronized void setImage(ImageIcon icon) {
		Image i = icon.getImage().getScaledInstance(
			imageSize.width, imageSize.height, Image.SCALE_FAST);
		screen.setIcon(new ImageIcon(i));
		repaint();
	}

	private void clear() {
		progress.setMaximum(imagesRequested);
		progress.setValue(0);
	}
}
