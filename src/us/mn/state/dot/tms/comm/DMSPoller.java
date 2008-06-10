/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.SignMessage;

/**
 * DMSPoller is an interface for MessagePoller classes which can poll DMS
 * sign devices.
 *
 * @author Douglas Lau
 */
public interface DMSPoller {

	/** Query the DMS configuration */
	void queryConfiguration(DMSImpl dms);

	/** Send a new message to the sign */
	void sendMessage(DMSImpl dms, SignMessage m)
		throws InvalidMessageException;

	/** Set the time remaining for the currently displayed message */
	void setMessageTimeRemaining(DMSImpl dms, SignMessage m);

	/** Set manual brightness level (null for photocell control) */
	void setBrightnessLevel(DMSImpl dms, Integer l);

	/** Activate a pixel test */
	void testPixels(DMSImpl dms);

	/** Activate a lamp test */
	void testLamps(DMSImpl dms);

	/** Activate a fan test */
	void testFans(DMSImpl dms);

	/** reset the sign */
	void reset(DMSImpl dms);

	/** get the sign message */
	void getSignMessage(DMSImpl dms);

	/** Set Ledstar pixel configuration */
	void setLedstarPixel(DMSImpl dms, int ldcPotBase, int pixelCurrentLow,
		int pixelCurentHigh, int badPixelLimit);
}
