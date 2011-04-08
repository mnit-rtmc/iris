/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import java.util.Hashtable;

import javax.swing.JPanel;

/**
 * This is the base class for managing video streams. This class is responsible
 * for instantiating the appropriate StreamManager based on available libraries.
 *
 * @author Tim Johnson
 */
abstract class StreamManager {

	private static StreamManager manager = null;
	final Hashtable<String, String> sources = new Hashtable<String, String>();
	final Hashtable<String, String> uris = new Hashtable<String, String>();
	
	public final String toString(){
		return this.getClass().getSimpleName();
	}

	static public StreamManager getInstance(){
		if(manager != null) return manager;
		try{
			Class.forName("org.gstreamer.Gst");
			return new GstManager();
		}catch(ClassNotFoundException cnfe){
			return new JavaManager();
		}
	}
	
	abstract void requestStream(VideoRequest req, String camId, JPanel displayPanel);

	abstract void clearStream(JPanel displayPanel);

}
