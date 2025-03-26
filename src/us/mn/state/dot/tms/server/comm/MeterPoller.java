/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.RampMeterImpl;

/**
 * MeterPoller is an interface for pollers which can send messages to ramp meter
 * devices.
 *
 * @author Douglas Lau
 */
public interface MeterPoller {

	/** Get the system meter green time (tenths of a second) */
	static int getGreenTime() {
		float g = SystemAttrEnum.METER_GREEN_SECS.getFloat();
		return Math.round(g * 10);
	}

	/** Get the system meter yellow time (tenths of a second) */
	static int getYellowTime() {
		float y = SystemAttrEnum.METER_YELLOW_SECS.getFloat();
		return Math.round(y * 10);
	}

	/** Startup green time (tenths of a second) */
	int STARTUP_GREEN = 80;

	/** Startup yellow time (tenths of a second) */
	int STARTUP_YELLOW = 50;

	/** Send a device request */
	void sendRequest(RampMeterImpl meter, DeviceRequest r);

	/** Send a new release rate (vehicles per hour) */
	void sendReleaseRate(RampMeterImpl meter, Integer rate);
}
