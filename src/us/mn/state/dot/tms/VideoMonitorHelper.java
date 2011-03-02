/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.util.Collection;
import java.util.LinkedList;
import us.mn.state.dot.sonar.Checker;

/**
 * Video monitor helper methods.
 *
 * @author Douglas Lau
 */
public class VideoMonitorHelper extends BaseHelper {

	/** Disallow instantiation */
	protected VideoMonitorHelper() {
		assert false;
	}

	/** Find video monitors using a Checker */
	static public VideoMonitor find(final Checker<VideoMonitor> checker) {
		return (VideoMonitor)namespace.findObject(
			VideoMonitor.SONAR_TYPE, checker);
	}

	/** Find restricted video montiros displaying a given camera */
	static public Collection<VideoMonitor> findRestricted(final Camera cam){
		final LinkedList<VideoMonitor> restricted =
			new LinkedList<VideoMonitor>();
		find(new Checker<VideoMonitor>() {
			public boolean check(VideoMonitor m) {
				if(m.getRestricted()) {
					Camera c = m.getCamera();
					if(c == cam || c == null)
						restricted.add(m);
				}
				return false;
			}
		});
		return restricted;
	}
}
