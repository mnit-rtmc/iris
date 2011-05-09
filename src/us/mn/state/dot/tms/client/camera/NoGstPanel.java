/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2011  Minnesota Department of Transportation
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
import java.io.IOException;
import us.mn.state.dot.tms.Camera;

/**
 * A NoGstPanel is responsible for managing video streams without using the
 * gstreamer-java library.
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class NoGstPanel extends StreamPanel {

	/** Current video stream */
	private VideoStream stream = null;

	/** Create a new stream panel */
	protected NoGstPanel(Dimension sz) {
		super(sz);
	}

	/** Request a new video stream */
	protected void requestStream(VideoRequest req, Camera cam) {
		try {
			stream = new MJPEGStream(req, cam);
			screenPanel.add(stream.getComponent());
			super.requestStream(req, cam);
		}
		catch(IOException e) {
			streamLabel.setText(e.getMessage());
		}
	}

	/** Clear the video stream */
	protected void clearStream() {
		super.clearStream();
		VideoStream vs = stream;
		if(vs != null) {
			vs.dispose();
			stream = null;
			streamLabel.setText(null);
		}
	}
}
