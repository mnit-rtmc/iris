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

import java.util.Calendar;

/**
 * @author Timothy A. Johnson
 *
 */
public abstract class VideoThread extends Thread{

	private final Calendar startTime = Calendar.getInstance();
	protected boolean done = false;

	/** Default timeout for direct URL Connections */
	public final static int TIMEOUT_DIRECT = 5 * 1000;

	/** Default timeout for proxied URL Connections */
	public final static int TIMEOUT_PROXY = 2 * 1000;

	/** Default timeout for HttpUrlConnection connect */
	public final static int TIMEOUT_CONNECT = 5 * 1000;

	/** Default timeout for HttpUrlConnection read */
	public final static int TIMEOUT_READ = 5 * 1000;

	public VideoThread() {
	}

	public final Calendar getStartTime(){
		return startTime;
	}

	public final void halt(){
		done = true;
	}

	/** Get the age of this thread (in seconds) */
	public int getAge(){
		Calendar now = Calendar.getInstance();
		return (int)(now.getTimeInMillis()-getStartTime().getTimeInMillis()/1000);
	}
}
