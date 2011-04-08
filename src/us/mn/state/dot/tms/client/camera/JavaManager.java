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

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A JavaManager is responsible for managing video streams using the built-in java libraries.
 *
 * @author Tim Johnson
 */
final public class JavaManager extends StreamManager {

	protected JavaManager(){}

	public JComponent createStreamRenderer(){
		return new StreamPanel();
	}

	public void requestStream(VideoRequest req, String camId, JPanel displayPanel){
		System.out.println(
				"Java implementation of starting a stream on camera " + camId);
	}
	
	public void clearStream(JPanel displayPanel){
		System.out.println("JAVA implementation of stopping a stream.");
	}
}
