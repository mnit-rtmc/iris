/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
 * Copyright (C) 2015  AHMCT, University of California
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

import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.HOURS;
import static us.mn.state.dot.tms.units.Interval.Units.MINUTES;

/**
 * A CommLink is a network connection for device communication.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public interface CommLink extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "comm_link";

	/** Valid polling periods */
	Interval[] VALID_PERIODS = {
		new Interval(5),
		new Interval(10),
		new Interval(15),
		new Interval(20),
		new Interval(30),
		new Interval(60),
		new Interval(90),
		new Interval(2, MINUTES),
		new Interval(4, MINUTES),
		new Interval(5, MINUTES),
		new Interval(10, MINUTES),
		new Interval(15, MINUTES),
		new Interval(20, MINUTES),
		new Interval(30, MINUTES),
		new Interval(60, MINUTES),
		new Interval(2, HOURS),
		new Interval(4, HOURS),
		new Interval(8, HOURS),
		new Interval(12, HOURS),
		new Interval(24, HOURS),
	};

	/** Set text description */
	void setDescription(String d);

	/** Get text description */
	String getDescription();

	/** Set the remote URI */
	void setUri(String u);

	/** Get the remote URI */
	String getUri();

	/** Set the communication protocol */
	void setProtocol(short p);

	/** Get the communication protocol */
	short getProtocol();

	/** Enable or disable polling */
	void setPollEnabled(boolean e);

	/** Get polling enabled/disabled flag */
	boolean getPollEnabled();

	/** Set poll period (seconds) */
	void setPollPeriod(int s);

	/** Get poll period (seconds) */
	int getPollPeriod();

	/** Maximum timeout (milliseconds) */
	int MAX_TIMEOUT_MS = 20 * 1000;

	/** Set the polling timeout (milliseconds) */
	void setTimeout(int t);

	/** Get the polling timeout (milliseconds) */
	int getTimeout();

	/** Get the communication port status */
	String getStatus();
}
