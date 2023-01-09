/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
 * Copyright (C) 2023  SRF Consulting Group
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.UserProperty;
import static us.mn.state.dot.tms.client.UserProperty.*;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * Layout of stream frames.
 *
 * @author Douglas Lau
 * @author Michael Janson
 * @author Gordon Parikh
 * @author John L. Stanley
 */
public class StreamLayout {

	/** Number of streams */
	private final int num_streams;

	/** Mapping of property names to values */
	private final HashMap<String, String> props;

	/** Create a new stream layout */
	public StreamLayout(Properties properties, String name) {
		props = UserProperty.getCameraFrames(properties, name);
		if (props.get(NUM_STREAM.name) != null)
			num_streams = getPropI(NUM_STREAM.name);
		else
			num_streams = 0;
	}

	/** Get an integer property */
	private int getPropI(String name) {
		try {
			String prop = props.get(name);
			return (prop != null) ? Integer.parseInt(prop) : 0;
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	/** Restore the stream frames */
	public void restoreFrames(SmartDesktop desktop) {
		HashMap<String, Frame> frames = getOpenFramesMap();
		for (int i = 0; i < num_streams; i++) {
			String cam_name = props.get(STREAM_CCTV.name + "." + i);
			Camera cam = CameraHelper.lookup(cam_name);
			if (cam != null) {
				String title = VidWindow.getWindowTitle(cam);
				if (frames.containsKey(title)) {
					Frame f = frames.get(title);
					f.setVisible(true);
					f.toFront();
				} else
					openStreamFrame(desktop, cam, i);
			}
		}
	}

	/** Get a hashmap of open streaming frames */
	private HashMap<String, Frame> getOpenFramesMap() {
		Frame[] frames = IrisClient.getFrames();
		HashMap<String, Frame> vidFrames = new HashMap<String, Frame>();
		for (Frame f: frames) {
			if (VidWindow.isFrame(f))
				vidFrames.put(f.getTitle(), f);
		}
		return vidFrames;
	}

	/** Get an arraylist of open streaming frames */
	static ArrayList<Frame> getOpenFramesList() {
		Frame[] frames = IrisClient.getFrames();
		ArrayList<Frame> frameList = new ArrayList<>();
		for (Frame f: frames) {
			if (VidWindow.isFrame(f))
				frameList.add(f);
		}
		return frameList;
	}

	/** Report if one or more layout-video frames are open */
	static public boolean framesAreOpen() {
		ArrayList<Frame> frameList = getOpenFramesList();
		return !frameList.isEmpty();
	}
	
	/** Open a camera stream frame */
	private void openStreamFrame(SmartDesktop desktop, Camera cam, int i) {
		int src = getPropI(STREAM_SRC.name + "." + i);
		int w = getPropI(STREAM_WIDTH.name + "." + i);
		int h = getPropI(STREAM_HEIGHT.name + "." + i);
		if (w > 0 && h > 0) {
			Dimension d = new Dimension(w, h);
			VidWindow window = new VidWindow(cam, true, d, src);
			int x = getPropI(STREAM_X.name + "." + i);
			int y = getPropI(STREAM_Y.name + "." + i);
			desktop.showExtFrame(window, x, y);
		}
	}
}
