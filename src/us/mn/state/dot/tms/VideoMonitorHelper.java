/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.Iterator;

/**
 * Video monitor helper methods.
 *
 * @author Douglas Lau
 */
public class VideoMonitorHelper extends BaseHelper {

	/** Disallow instantiation */
	private VideoMonitorHelper() {
		assert false;
	}

	/** Lookup the monitor with the specified name */
	static public VideoMonitor lookup(String name) {
		return (VideoMonitor) namespace.lookupObject(
			VideoMonitor.SONAR_TYPE, name);
	}

	/** Get a video monitor iterator */
	static public Iterator<VideoMonitor> iterator() {
		return new IteratorWrapper<VideoMonitor>(namespace.iterator(
			VideoMonitor.SONAR_TYPE));
	}

	/** Find a video monitor with the specific UID */
	static public VideoMonitor findUID(final int uid) {
		Iterator<VideoMonitor> it = iterator();
		while (it.hasNext()) {
			VideoMonitor vm = it.next();
			if (vm.getMonNum() == uid)
				return vm;
		}
		return null;
	}

	/** Find a video monitor with the specific UID */
	static public VideoMonitor findUID(String uid) {
		Integer id = CameraHelper.parseUID(uid);
		return (id != null) ? findUID(id) : null;
	}

	/** Find previous valid video monitor number */
	static public int findPrev(int uid) {
		int pid = 0;
		Iterator<VideoMonitor> it = iterator();
		while (it.hasNext()) {
			int mid = it.next().getMonNum();
			if ((mid < uid) && (0 == pid || pid < mid))
				pid = mid;
		}
		return pid;
	}

	/** Find next valid video monitor number */
	static public int findNext(int uid) {
		int nid = 0;
		Iterator<VideoMonitor> it = iterator();
		while (it.hasNext()) {
			int mid = it.next().getMonNum();
			if ((mid > uid) && (0 == nid || nid > mid))
				nid = mid;
		}
		return nid;
	}
}
