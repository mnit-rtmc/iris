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

/**
 * Contains a single report result-item for a RptResults.
 *
 * @author John L. Stanley - SRF Consulting
 */

public class RptResultItem {
	
	public RptResultItem(long dt, String devName, String un, String descr) {
		setDatetime(dt);
		setName(devName);
		setUsername(un);
		setDescription(descr);
	}
	
	public RptResultItem() {
		setDatetime("");
		setName("");
		setUsername("");
		setDescription("");
	}
	
	//-------------------------------------------
	
	/** epoc date-time value */
	protected Long datetime;
	
	/** set epoc datetime */
	public void setDatetime(long when) {
		datetime = when;
	}

	/** set epoc datetime */
	public void setDatetime(String when) {
		datetime = RptDateTime.getLong(when);
	}

	/** get epoc datetime */
	public String getDatetimeStr() {
		return RptDateTime.getString(datetime);
	}

	/** get epoc datetime */
	public Long getDatetimeLong() {
		return datetime;
	}

	//-------------------------------------------

	/** device name */
	protected String name;

	/** set device name */
	public void setName(String devname) {
		name = devname;
	}

	/** get device name */
	public String getName() {
		return name;
	}

	//-------------------------------------------

	/** username */
	protected String username;

	/** set username */
	public void setUsername(String xusername) {
		username = xusername;
	}

	/** get username */
	public String getUsername() {
		return username;
	}

	//-------------------------------------------

	/** description of event (report item) */
	protected String description;

	/** set description */
	public void setDescription(String descr) {
		description = descr;
	}

	/** get description */
	public String getDescription() {
		return description;
	}

	//-------------------------------------------
	
}
