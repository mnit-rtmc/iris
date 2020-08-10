/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2020  Minnesota Department of Transportation
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
import java.awt.Frame;
import java.util.HashMap;
import java.util.Properties;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.UserProperty;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * Layout of stream frames.
 *
 * @author Douglas Lau
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class StreamLayout {

	static private final String TITLE = "Stream Panel";

	/** Number of streams */
	public final int num_streams;

	/** Mapping of property names to values */
	private final HashMap<String, String> props;

	/** Create a new stream layout */
	public StreamLayout(Properties properties, String name) {
		props = UserProperty.getCameraFrames(properties, name);
		if (props.get(UserProperty.NUM_STREAM.name) != null) {
			num_streams = Integer.parseInt(
				props.get(UserProperty.NUM_STREAM.name));
		} else
			num_streams = 0;
	}

	/** Restore the stream frames */
	public void restoreFrames(SmartDesktop desktop) {
		HashMap<String, Frame> vidFrames = getOpenFrames();
		for (int i = 0; i < num_streams; i++) {
			String cam_name = props.get(
				UserProperty.STREAM_CCTV.name + "." + i);
			Camera cam = CameraHelper.lookup(cam_name);
			if (cam != null) {
				String title = VidWindow.getWindowTitle(cam);
				if (vidFrames.containsKey(title)) {
					Frame f = vidFrames.get(title);
					f.setVisible(true);
					f.toFront();
				} else
					openStreamFrame(desktop, cam, i);
			}
		}
	}

	/** Get a mapping of open streaming frames */
	private HashMap<String, Frame> getOpenFrames() {
		Frame[] frames = IrisClient.getFrames();
		HashMap<String, Frame> vidFrames = new HashMap<String, Frame>();
		for (Frame f: frames) {
			if (f.getTitle().contains(TITLE) && f.isVisible())
				vidFrames.put(f.getTitle(), f);
		}
		return vidFrames;
	}

	/** Open a camera stream frame */
	private void openStreamFrame(SmartDesktop desktop, Camera cam, int i) {
		int strm_num = Integer.parseInt(props.get(
			UserProperty.STREAM_SRC.name + "." + i));
		int w = Integer.parseInt(props.get(
			UserProperty.STREAM_WIDTH.name + "." + i));
		int h = Integer.parseInt(props.get(
			UserProperty.STREAM_HEIGHT.name + "." + i));
		Dimension d = new Dimension(w, h);
		VidWindow window = new VidWindow(cam, true, d, strm_num);
		int x = Integer.parseInt(props.get(
			UserProperty.STREAM_X.name + "." + i));
		int y = Integer.parseInt(props.get(
			UserProperty.STREAM_Y.name + "." + i));
		desktop.showExtFrame(window, x, y);
	}
}
