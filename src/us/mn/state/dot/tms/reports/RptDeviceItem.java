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
import java.util.Comparator;
import java.util.Iterator;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;

/**
 * Contains info for a single device for a report request.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RptDeviceItem {
	
	public RptDeviceItem(String devname, RptDeviceClassEnum e) {
		name = devname;
		nameLower = devname.toLowerCase();
		devClass = e;
	}

	//-------------------------------------------
	
	/** Is device class selected? */
	protected boolean selectedClass = false;

	/** Set selected device-class flag */
	public boolean setSelectedClass(boolean flag) {
		boolean changed = (selectedClass != flag);
		selectedClass = flag;
		return changed;
	}

	/** Is device name visible? */
	protected boolean visibleName = true;

	/** Set visible name-device flag.  
	 * Returns true if flag changed. */
	public boolean setVisibleName(boolean flag) {
		boolean changed = (visibleName != flag);
		visibleName = flag;
		return changed;
	}

	/** Get composite visible flag.  Device is only 
	 * visible if both (the device-class it belongs
	 * to is selected) -and- (the device name is
	 * visible (as specified by the name filter)). */
	public boolean getVisible() {
		return selectedClass & visibleName;
	}

	//-------------------------------------------
	
	/** Is device selected in GUI? */
	protected boolean selected = false;

	/** Set selected flag */
	public void setSelected(boolean flag) {
		selected = flag;
	}

	/** Get selected flag */
	public boolean getSelected() {
		return selected;
	}
	
	//-------------------------------------------
	
	/** device name */
	protected String name;
	protected String nameLower;

	/** Set device name */
	public void setName(String str) {
		name = (str != null) ? str : "";
		nameLower = name.toLowerCase();
	}

	/** Get device name */
	public String getName() {
		return name;
	}

	//-------------------------------------------

	/** class of this device */
	RptDeviceClassEnum devClass;

	public void setDevClass(RptDeviceClassEnum e) {
		devClass = e;
	}

	public RptDeviceClassEnum getDevClass() {
		return devClass;
	}

	//-------------------------------------------

	/** adds all supported devices to an ArrayList of RptDeviceItem */
	public static void initList(ArrayList<RptDeviceItem> list) {
		RptDeviceItem item;
		
		// add signs
		Iterator<DMS> itDms = DMSHelper.iterator();
		DMS sign;
		while (itDms.hasNext()) {
			sign = itDms.next();
			if (sign != null) {
				item = new RptDeviceItem(
						sign.getName(),
						RptDeviceClassEnum.DMS);
				list.add(item);
			}
		}
		
		//FIXME: add code to add other devices
		
		// sort list by device name
		list.sort(nameComparator);
	}
	
	//-------------------------------------------
	
	/** Sets visibleName flag based on partial name match.
	 *  filter string must not be null and must be lower case. 
	 *  Returns true if the visibleName flag changed. */
	public boolean setVisibleName(String filter) {
		if (filter.isEmpty())
			return setVisibleName(true);
		else
			return setVisibleName(nameLower.contains(filter));
	}

	//-------------------------------------------

	/** Comparator to sort by device name */
	public static Comparator<RptDeviceItem> nameComparator =
		new Comparator<RptDeviceItem>() {         
			public int compare(RptDeviceItem di1, RptDeviceItem di2) {             
				return (int) (di1.getName().compareTo(di2.getName()));         
			}
		};

}
