/*
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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

import java.util.Calendar;

/** The VideoClip class represents an archive video clip from the NVR system. */ 
public class VideoClip {

	protected Calendar start = Calendar.getInstance();
	protected int duration = 0;
	protected String cameraId = null;
	
	public VideoClip(){
	}

	public String getCameraId() {
		return cameraId;
	}

	public void setCameraId(String id) {
		cameraId = id;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Calendar getStart() {
		return start;
	}

	public void setStart(Calendar start) {
		this.start = start;
	}

	public String getName(){
		String s = cameraId + "_" +
			Constants.DATE_FORMAT.format(start.getTime()) +
			".mpg";
		s = s.replace("-", "");
		s = s.replace(":", "");
		return s;
	}

	public Calendar getEnd(){
		Calendar c = (Calendar)start.clone();
		c.add(Calendar.SECOND, duration);
		return c;
	}
}
