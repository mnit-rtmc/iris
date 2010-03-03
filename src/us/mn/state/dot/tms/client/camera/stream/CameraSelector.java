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

import java.util.Collection;
import java.util.TreeMap;

import javax.swing.JList;

public class CameraSelector extends JList {


	public CameraSelector(){
		this.setVisibleRowCount(10);
	}
	
	public void setCameras(Collection<Camera> cams){
		this.removeAll();
		TreeMap<Camera, String> map = new TreeMap<Camera, String>(new CameraComparator());
		for(Camera c : cams){
			map.put(c, c.getId());
		}
		this.setListData(map.keySet().toArray());
	}

}
