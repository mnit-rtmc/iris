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
import us.mn.state.dot.tms.RptGenEnum;

/**
 * Contains info for a single report generator for a report request.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RptGenItem {

	private final RptGenEnum rptGenEnum;

	public RptGenItem(RptGenEnum e) {
		rptGenEnum = e;
	}

	//-------------------------------------------
	
	/** Is report-generator visible in GUI? */
	protected boolean visible = true;

	/** Set visible flag */
	public void setVisible(boolean flag) {
		visible = flag;
	}

	/** Get visible flag */
	public boolean getVisible() {
		return visible;
	}

	//-------------------------------------------
	
	/** Is report-generator selected in GUI? */
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
	
	/** Get report-generator name to be displayed in GUI checklist.
	 * - Must be implemented by each child generator class. */
	public String getGuiName() {
		return rptGenEnum.getGuiName();
	};

	//-------------------------------------------
	//TODO: Rewrite this section when we add more device classes
	
//	/** set of device-classes this generator works on */
//	protected HashSet<RptDeviceClassEnum> devClasses = new HashSet<RptDeviceClassEnum>();
//	
//	/** add a device-class to the devClasses set */
//	protected void includesDeviceClass(RptDeviceClassEnum e) {
//		devClasses.add(e);
//	}
//	
//	/** Scans rptRequest's deviceclass list and updates
//	 *  generator's visible flag as needed.  (A generator
//	 *  can, in theory, work on more than one device type.) 
//	 *  Returns true if generator's visible flag changed.
//	 */
//	public boolean updateVisible(RptRequest request) {
//		RptDeviceClassItem item;
//		RptDeviceClassEnum e;
//		boolean willBeVisible = false;
//
//		// Are any of the device-classes this generator
//		// works on "selected" in the request?
//		Iterator<RptDeviceClassItem> it = request.iterateDeviceClasses();
//		while (it.hasNext()) {
//			item = it.next();
//			if (item == null)
//				continue;
//			if (item.getSelected() == false)
//				continue;
//			e = item.getDevClassEnum();
//			if (devClasses.contains(e)) {
//				willBeVisible = true; // Yes!
//				break;
//			}
//		}
//		
//		if (willBeVisible == getVisible())
//			return false; // visible flag didn't change
//
//		setVisible(willBeVisible);
//		return true;  // visible flag changed
//	}

	//-------------------------------------------
	
	/** Adds a RptGenItem for each available report to the list */
	public static void initList(ArrayList<RptGenItem> list) {
		list.clear();
		for (RptGenEnum e: RptGenEnum.values()) {
			list.add(new RptGenItem(e));
		}
	}

}
