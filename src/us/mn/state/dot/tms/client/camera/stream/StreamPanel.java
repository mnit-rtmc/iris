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
public class StreamPanel extends JPanel implements DataSink {

	static protected final Dimension SIF_QUARTER = new Dimension(176, 120);
	static protected final Dimension SIF_FULL = new Dimension(352, 240);
	static protected final Dimension SIF_4X = new Dimension(704, 480);

	private DataSource source = null;
	private int imagesRendered = 0;
	private final JLabel screen = new JLabel();
	private JProgressBar progress = new JProgressBar(0, 100);
	private int imagesRequested = 0;
	private Dimension imageSize = new Dimension(SIF_FULL);

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
	}

	public synchronized void setVideoSize(Dimension d) {
		imageSize = d;
		screen.setPreferredSize(d);
		screen.setMinimumSize(d);
	}

	public void setDataSource(DataSource src, int totalFrames) {
		if(source != null)
			source.disconnectSink(this);
		imagesRendered = 0;
		imagesRequested = totalFrames;
		if(src != null) {
			src.connectSink(this);
			try {
				((Thread)src).start();
			}
			catch(IllegalThreadStateException its) {
				// do nothing... it's already been started.
			}
		}
		source = src;
		progress.setMaximum(imagesRequested);
		progress.setValue(0);
	}

	public void flush(byte[] i) {
		ImageIcon icon = new ImageIcon(i);
		setImage(icon);
		progress.setValue(imagesRendered);
		imagesRendered++;
		if(imagesRendered >= imagesRequested) {
			//FIXME: This is a thread safety violation since this
			//call to a synchronized method disconnectSink is
			//called from another synchronized method notifySinks.
			//Both method calls are running in different threads
			//and operating on the same ArrayList.

			//Note: We actually prefer continuous video so the fix
			//for us is to remove the following call.

			//source.disconnectSink(this);
			clear();
		}
	}

	/** Set the image to be displayed on the panel
	 * @param image  The image to display. */
	private synchronized void setImage(ImageIcon icon) {
		Image i = icon.getImage().getScaledInstance(
			imageSize.width, imageSize.height,
				Image.SCALE_FAST);
		screen.setIcon(new ImageIcon(i));
		repaint();
	}

	private void clear() {
		progress.setMaximum(imagesRequested);
		progress.setValue(0);
	}
}
