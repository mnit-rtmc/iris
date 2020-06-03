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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import us.mn.state.dot.tms.reports.RptDeviceClassEnum;
import us.mn.state.dot.tms.reports.RptDeviceClassItem;
import us.mn.state.dot.tms.reports.RptDeviceItem;
import us.mn.state.dot.tms.reports.RptGenItem;

/**
 * Container for a report-request.
 *
 * @author John L. Stanley - SRF Consulting
 */

public class RptRequest {

	/** Filter applied to names in the device list */
	String devNameFilter = "";

	//-------------------------------------------

	/** List of device classes */
	ArrayList<RptDeviceClassItem> devClassList =
			new ArrayList<RptDeviceClassItem>();

	public void setDeviceClassList(ArrayList<RptDeviceClassItem> list) {
		devClassList = list;
	}
	
	public List<RptDeviceClassItem> getDeviceClassList() {
		return devClassList;
	}

	public Iterator<RptDeviceClassItem> iterateDeviceClasses() {
		return devClassList.iterator();
	}

	public RptDeviceClassItem getDeviceClass(RptDeviceClassEnum e) {
		Iterator<RptDeviceClassItem> iter = iterateDeviceClasses();
		RptDeviceClassItem it;
		while (iter.hasNext()) {
			it = iter.next();
			if (it.getDevClassEnum().equals(e))
				return it;
		}
		return null;
	}

	public RptDeviceClassItem getDeviceClass(String devclass) {
		Iterator<RptDeviceClassItem> iter = iterateDeviceClasses();
		RptDeviceClassItem it;
		while (iter.hasNext()) {
			it = iter.next();
			if (it.getName().equalsIgnoreCase(devclass))
				return it;
		}
		return null;
	}

	//-------------------------------------------

	/** List of devices */
	ArrayList<RptDeviceItem> devList =
			new ArrayList<RptDeviceItem>();

	public void setDeviceList(ArrayList<RptDeviceItem> list) {
		devList = list;
	}
	
	public List<RptDeviceItem> getDeviceList() {
		return devList;
	}

	public Iterator<RptDeviceItem> iterateDevices() {
		return devList.iterator();
	}

	public RptDeviceItem getDevice(String devname) {
		Iterator<RptDeviceItem> iter = iterateDevices();
		RptDeviceItem it;
		while (iter.hasNext()) {
			it = iter.next();
			if (it.getName().equalsIgnoreCase(devname))
				return it;
		}
		return null;
	}

	//-------------------------------------------
	
	/** List of RptGenItem(s) */
	ArrayList<RptGenItem> genList =
			new ArrayList<RptGenItem>();

	public void setGenItemList(ArrayList<RptGenItem> list) {
		genList = list;
	}
	
	public List<RptGenItem> getGenItemList() {
		return genList;
	}

	public Iterator<RptGenItem> iterateGenItems() {
		return genList.iterator();
	}

	public RptGenItem getGenItemFromGuiName(String guiname) {
		Iterator<RptGenItem> iter = iterateGenItems();
		RptGenItem it;
		while (iter.hasNext()) {
			it = iter.next();
			if (it.getGuiName().equalsIgnoreCase(guiname))
				return it;
		}
		return null;
	}

	//-------------------------------------------
	
	/** Start epoc time for time-related reports */
	long startDatetime;

	public void setStartDatetime(long datetime) {
		startDatetime = datetime;
	}
	
	public Long getStartDatetime() {
		return startDatetime;
	}
	
	//-------------------------------------------
	
	/** End epoc time for time-related reports */
	long endDatetime;
	
	public void setEndDatetime(long datetime) {
		endDatetime = datetime;
	}
	
	public Long getEndDatetime() {
		return endDatetime;
	}
	
	//===============================================================
	// More complex methods
	//===============================================================
	
	/** Populates all three lists,
	* clears device name filter,
	* sets startDateTime to one day ago,
	* and sets endDateTime to now. */
	public void init() {
		RptGenItem.initList(genList);
		RptDeviceClassItem.initList(devClassList);
		RptDeviceItem.initList(devList);
		devNameFilter = "";
		endDatetime = System.currentTimeMillis();
		startDatetime = endDatetime - (1000L * 60 * 60 * 24);
	}
	
	//-------------------------------------------

	/** Modify the selected flag on a deviceClassItem.
	 *  This also modifies the visible flags on the
	 *  deviceItem and generatorItem lists if necessary.
	 *  Returns true if visible status of any deviceItem
	 *  or generatorItem are changed. */
	public boolean setDeviceClassSelected(RptDeviceClassEnum e, boolean flag) {
		if (e == null)
			return false;  // no change

		// Modify class selected flag.
		RptDeviceClassItem dci = getDeviceClass(e);
		if ((dci == null)
		 || !dci.setSelected(flag))
			return false;  // no change

		// Modify the devices selectedClass flags
		// (Which can effect the visibility of the device...)
		boolean changed = false;
		Iterator<RptDeviceItem> devIt = iterateDevices();
		RptDeviceItem dev;
		while (devIt.hasNext()) {
			dev = devIt.next();
			if ((dev != null) && dev.getDevClass().equals(e))
				changed |= dev.setSelectedClass(flag);
		}

		//TODO: Rewrite this section when we add more device types
//		// Modify the generators visible flags
//		RptGenItem gen;
//		Iterator<RptGenItem> genIt = iterateGenerators();
//		while (genIt.hasNext()) {
//			gen = genIt.next();
//			if (gen == null)
//				continue;
//			changed |= gen.updateVisible(this);
//		}

		return changed;
	}
	
	//-------------------------------------------

	/** Modifies visibleName RptDeviceItem flags so
	 * only devices with names that match the string
	 * are visible.  If string is empty or null, all
	 * devices are visible.  String matching is
	 * caseless and matches if any part of the device
	 * name matches the filter string.  Returns true
	 * if any of the visibleName flags changed. */
	public boolean setDeviceNameFilter(String str) {
		str = (str != null) ? str.toLowerCase() : "";
		RptDeviceItem dev;
		boolean changed = false;
		Iterator<RptDeviceItem> devIt = iterateDevices();
		while (devIt.hasNext()) {
			dev = devIt.next();
			if (dev != null)
				changed |= dev.setVisibleName(str);
		}
		return changed;
	}
	
	//-------------------------------------------

	/** modifies the selected flag on the deviceItem. */
	public void setDeviceSelected(String devname, boolean flag) {
		RptDeviceItem dev = getDevice(devname);
		if (dev != null)
			dev.setSelected(flag);
	}

	//-------------------------------------------

	/** modifies the selected flag on the generator */
	public void setGeneratorSelected(String guiname, boolean flag) {
		RptGenItem gen = getGenItemFromGuiName(guiname);
		if (gen != null)
			gen.setSelected(flag);
	}
	
	//-------------------------------------------

	// serialization tags
	protected final String GENERATORS = "generators";
	protected final String DEVICES    = "devices";
	protected final String DATETIME   = "datetime";

	/** Convert request to a composite-request String.
	 * Only serializes items that are both selected &amp; visible. */
	public String toReqString() {
		
		RptStringSet rssGenerators = new RptStringSet(GENERATORS);
		RptStringSet rssDevices    = new RptStringSet(DEVICES);
		RptStringSet rssDatetime   = new RptStringSet(DATETIME);
		RptStringSetMap rssm = new RptStringSetMap();

		RptGenItem gen;
		Iterator<RptGenItem> iterGen = genList.iterator();
		while (iterGen.hasNext()) {
			gen = iterGen.next();
			if (gen.visible && gen.selected)
				rssGenerators.add(gen.getGuiName());
		}
		
		RptDeviceItem itDev;
		Iterator<RptDeviceItem> iterDev = iterateDevices();
		while (iterDev.hasNext()) {
			itDev = iterDev.next();
			if (itDev.getVisible() && itDev.getSelected())
				rssDevices.add(itDev.getName());
		}

		rssDatetime.add(""+getStartDatetime());
		rssDatetime.add(""+getEndDatetime());

		rssm.add(rssGenerators);
		rssm.add(rssDevices);
		rssm.add(rssDatetime);

		String sReq = rssm.toCompositeString();
		return sReq;
	}
	
	/** Initialize request from a composite-request String. */
	public void initFromReqString(String sReq) {
		init();

		try {
			RptStringSetMap rssm = new RptStringSetMap(sReq);
			RptStringSet rssGenerators = rssm.get(GENERATORS);
			RptStringSet rssDevices    = rssm.get(DEVICES);
			RptStringSet rssDatetime   = rssm.get(DATETIME);
			Iterator<String> it;
			String str;
	
			it = rssGenerators.iterator();
			while (it.hasNext()) {
				str = it.next();
				setGeneratorSelected(str, true);
			}
	
			it = rssDevices.iterator();
			while (it.hasNext()) {
				str = it.next();
				setDeviceSelected(str, true);
			}
	
			String [] datetimes = new String[2];
			datetimes = rssDatetime.toArray(datetimes);
			startDatetime = Long.parseLong(datetimes[0]);
			endDatetime   = Long.parseLong(datetimes[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
