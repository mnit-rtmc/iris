/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.User;

/**
 * Gate Arm Array interface.
 *
 * @author Douglas Lau
 */
public interface GateArmArray extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "gate_arm_array";

	/** Maximum number of gate arms in array */
	int MAX_ARMS = 8;

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set prerequisite gate arm array */
	void setPrereq(String pr);

	/** Get prerequisite gate arm array */
	String getPrereq();

	/** Set verification camera */
	void setCamera(Camera c);

	/** Get verification camera */
	Camera getCamera();

	/** Set approach camera */
	void setApproach(Camera c);

	/** Get approach camera */
	Camera getApproach();

	/** Set warning DMS */
	void setDms(DMS d);

	/** Get warning DMS */
	DMS getDms();

	/** Set the OPEN quick message */
	void setOpenMsg(QuickMessage m);

	/** Get the OPEN quick message */
	QuickMessage getOpenMsg();

	/** Set the CLOSED quick message */
	void setClosedMsg(QuickMessage m);

	/** Get the CLOSED quick message */
	QuickMessage getClosedMsg();

	/** Set the next state owner */
	void setOwnerNext(User o);

	/** Set the next arm state (request change) */
	void setArmStateNext(int gas);

	/** Get the (aggregate) arm state */
	int getArmState();

	/** Get the interlock state */
	int getInterlock();
}
