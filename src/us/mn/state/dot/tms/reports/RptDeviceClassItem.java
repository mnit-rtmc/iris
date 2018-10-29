/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.reports;

import java.util.ArrayList;

/**
 * Contains info for a single device-class for a report request.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RptDeviceClassItem {
	
	public RptDeviceClassItem(RptDeviceClassEnum e) {
		devClassEnum = e;
		name = e.toString();
		selected = false;
	}

	//-------------------------------------------
	
	/** Is device class selected in GUI? */
	protected boolean selected;

	/** Set selected flag */
	public boolean setSelected(boolean flag) {
		boolean changed = (selected != flag); 
		selected = flag;
		return changed;
	}

	/** Get selected flag */
	public boolean getSelected() {
		return selected;
	}
	
	//-------------------------------------------
	
	/** device-class name */
	protected String name;

	/** Set device-class name */
	public void setName(String str) {
		name = str;
	}

	/** Get device-class name */
	public String getName() {
		return name;
	}

	//-------------------------------------------
	
	/** device-class enumeration */
	protected RptDeviceClassEnum devClassEnum;

	/** Set device-class enum */
	public void setDevClassEnum(RptDeviceClassEnum e) {
		devClassEnum = e;
	}

	/** Get device-class enum */
	public RptDeviceClassEnum getDevClassEnum() {
		return devClassEnum;
	}

	//-------------------------------------------

	public static void initList(ArrayList<RptDeviceClassItem> list) {
		list.clear();
		list.add(new RptDeviceClassItem(RptDeviceClassEnum.DMS));
	}

}
