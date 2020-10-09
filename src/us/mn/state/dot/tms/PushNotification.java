/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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

import java.util.Date;

import us.mn.state.dot.sonar.SonarObject;

/** 
 * SonarObject for sending push notifications out to clients. PushNotification
 * objects contain a reference to another SonarObject that requires a user's
 * attention, along with the notification time and a title/description.
 *
 * @author Gordon Parikh
 */
public interface PushNotification extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "push_notification";
	
	/** Set the type of the object that is being referenced */
	void setRefObjectType(String rt);
	
	/** Get the type of the object that is being referenced */
	String getRefObjectType();

	/** Set the SONAR name of the object that is being referenced */
	void setRefObjectName(String rn);
	
	/** Get the SONAR name of the object that is being referenced */
	String getRefObjectName();

	/** Set whether users must be able to write this type of objects to see
	 *  this notification.
	 */
	void setNeedsWrite(boolean nw);

	/** Get whether users must be able to write this type of objects to see
	 *  this notification.
	 */
	boolean getNeedsWrite();
	
	/** Set the time the notification was generated/sent */
	void setSentTime(Date st);
	
	/** Get the time the notification was generated/sent */
	Date getSentTime();
	
	/** Set the notification title */
	void setTitle(String t);
	
	/** Get the notification title */
	String getTitle();
	
	/** Set the notification description */
	void setDescription(String d);
	
	/** Get the notification description */
	String getDescription();
	
	/** Set the name of the user who addressed this notification */
	void setAddressedBy(String u);
	
	/** Get the name of the user who addressed this notification */
	String getAddressedBy();
	
	/** Set the time at which this notification was addressed */
	void setAddressedTime(Date at);
	
	/** Get the time at which this notification was addressed */
	Date getAddressedTime();
}
