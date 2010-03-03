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

import java.util.TreeSet;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * ThreadMonitor is a thread used to monitor other threads when debugging 
 * multi-threaded apps.  It will periodically report on the status 
 * of each registered thread.
 * @author Timothy Johnson
 *
 */
public class ThreadMonitor extends Thread {

	private Logger logger = null;
	private final TreeSet<VideoThread> threads =
		new TreeSet<VideoThread>(new VideoThreadComparator());
	private final String name;
	private int interval = 20 * 1000;
	
	public ThreadMonitor(String name, int interval, Logger l){
		this.name = name;
		if(interval>0){
			this.interval = interval;
		}
		logger = l;
		logger.fine("Initialized thread monitor.");
		start();
	}
		
	public synchronized void addThread(VideoThread t){
		threads.add(t);
	}
	
	public void run(){
		try{
			while(true){
				printThreads();
				Thread.sleep(interval);
			}
		}catch(InterruptedException ie){
		}finally{
			logger.warning(name + " is no longer monitoring.");
		}
	}

	private synchronized void printThreads(){
		for (Iterator i = threads.iterator(); i.hasNext();) {
			if (!((VideoThread) i.next()).isAlive()) {
				i.remove();
			} else {
				logger.fine((VideoThread) i.next() + " " + ((VideoThread) i.next()).getStatus());
			}

		}
	}
}
