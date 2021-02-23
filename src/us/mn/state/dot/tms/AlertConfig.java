/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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

/**
 * Alert Configuration object.
 *
 * Contains parameters ("event", "response type", "urgency", "severity",
 * "certainty") to filter alerts which can trigger AlertMessage deployments.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public interface AlertConfig extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "alert_config";

	/** Set the alert event code */
	void setEvent(String ev);

	/** Get the alert event code */
	String getEvent();

	/** Set the response shelter flag */
	void setResponseShelter(boolean fl);

	/** Get the response shelter flag */
	boolean getResponseShelter();

	/** Set the response evacuate flag */
	void setResponseEvacuate(boolean fl);

	/** Get the response evacuate flag */
	boolean getResponseEvacuate();

	/** Set the response prepare flag */
	void setResponsePrepare(boolean fl);

	/** Get the response prepare flag */
	boolean getResponsePrepare();

	/** Set the response execute flag */
	void setResponseExecute(boolean fl);

	/** Get the response execute flag */
	boolean getResponseExecute();

	/** Set the response avoid flag */
	void setResponseAvoid(boolean fl);

	/** Get the response avoid flag */
	boolean getResponseAvoid();

	/** Set the response monitor flag */
	void setResponseMonitor(boolean fl);

	/** Get the response monitor flag */
	boolean getResponseMonitor();

	/** Set the response all clear flag */
	void setResponseAllClear(boolean fl);

	/** Get the response all clear flag */
	boolean getResponseAllClear();

	/** Set the response none flag */
	void setResponseNone(boolean fl);

	/** Get the response none flag */
	boolean getResponseNone();

	/** Set the urgency unknown flag */
	void setUrgencyUnknown(boolean fl);

	/** Get the urgency unknown flag */
	boolean getUrgencyUnknown();

	/** Set the urgency past flag */
	void setUrgencyPast(boolean fl);

	/** Get the urgency past flag */
	boolean getUrgencyPast();

	/** Set the urgency future flag */
	void setUrgencyFuture(boolean fl);

	/** Get the urgency future flag */
	boolean getUrgencyFuture();

	/** Set the urgency expected flag */
	void setUrgencyExpected(boolean fl);

	/** Get the urgency expected flag */
	boolean getUrgencyExpected();

	/** Set the urgency immediate flag */
	void setUrgencyImmediate(boolean fl);

	/** Get the urgency immediate flag */
	boolean getUrgencyImmediate();

	/** Set the severity unknown flag */
	void setSeverityUnknown(boolean fl);

	/** Get the severity unknown flag */
	boolean getSeverityUnknown();

	/** Set the severity minor flag */
	void setSeverityMinor(boolean fl);

	/** Get the severity minor flag */
	boolean getSeverityMinor();

	/** Set the severity moderate flag */
	void setSeverityModerate(boolean fl);

	/** Get the severity moderate flag */
	boolean getSeverityModerate();

	/** Set the severity severe flag */
	void setSeveritySevere(boolean fl);

	/** Get the severity severe flag */
	boolean getSeveritySevere();

	/** Set the severity extreme flag */
	void setSeverityExtreme(boolean fl);

	/** Get the severity extreme flag */
	boolean getSeverityExtreme();

	/** Set the certainty unknown flag */
	void setCertaintyUnknown(boolean fl);

	/** Get the certainty unknown flag */
	boolean getCertaintyUnknown();

	/** Set the certainty unlikely flag */
	void setCertaintyUnlikely(boolean fl);

	/** Get the certainty unlikely flag */
	boolean getCertaintyUnlikely();

	/** Set the certainty possible flag */
	void setCertaintyPossible(boolean fl);

	/** Get the certainty possible flag */
	boolean getCertaintyPossible();

	/** Set the certainty likely flag */
	void setCertaintyLikely(boolean fl);

	/** Get the certainty likely flag */
	boolean getCertaintyLikely();

	/** Set the certainty observed flag */
	void setCertaintyObserved(boolean fl);

	/** Get the certainty observed flag */
	boolean getCertaintyObserved();

	/** Enable/disable auto deploy */
	void setAutoDeploy(boolean ad);

	/** Get auto deploy enabled state */
	boolean getAutoDeploy();

	/** Set the duration in hours for the "before" alert period */
	void setBeforePeriodHours(int hours);

	/** Get the duration in hours for the "before" alert period */
	int getBeforePeriodHours();

	/** Set the duration in hours for the "after" alert period */
	void setAfterPeriodHours(int hours);

	/** Get the duration in hours for the "after" alert period */
	int getAfterPeriodHours();
}
