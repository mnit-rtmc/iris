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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import us.mn.state.dot.tms.PushNotification;
import us.mn.state.dot.tms.PushNotificationHelper;
import us.mn.state.dot.tms.TMSException;

/** 
 * SonarObject for sending push notifications out to clients - server-side
 * implementation. PushNotification objects contain a reference to another
 * SonarObject that requires a user's attention, along with the notification
 * time and a title/description.
 *
 * @author Gordon Parikh
 */
public class PushNotificationImpl extends BaseObjectImpl
	implements PushNotification {

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event." + SONAR_TYPE;
	}
	
	/** Load all the push notifications */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, PushNotificationImpl.class);
		store.query("SELECT name, ref_object_type, ref_object_name, " +
			"needs_write, sent_time, title, description, addressed_by, " +
			"addressed_time FROM event." + SONAR_TYPE +
			";", new ResultFactory()
		{
			@Override
			public void create(ResultSet row) throws Exception {
				try {
					namespace.addObject(new PushNotificationImpl(row));
				} catch (Exception e) {
					System.out.println("Error adding: " + row.getString(1));
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("ref_object_type", ref_object_type);
		map.put("ref_object_name", ref_object_name);
		map.put("needs_write", needs_write);
		map.put("sent_time", sent_time);
		map.put("title", title);
		map.put("description", description);
		map.put("addressed_by", addressed_by);
		map.put("addressed_time", addressed_time);
		return map;
	}
	
	private PushNotificationImpl(ResultSet row) throws SQLException {
		this(row.getString(1),			// name
			row.getString(2),			// ref_object_type
			row.getString(3),			// ref_object_name
			row.getBoolean(4),			// needs_write
			row.getTimestamp(5),		// sent_time
			row.getString(6),			// title
			row.getString(7),			// description
			row.getString(8),			// addressed_by
			row.getTimestamp(9)			// addressed_time
		);
	}
	
	public PushNotificationImpl(String n) {
		super(n);
	}

	/** Create a new PushNotification. Automatically generates a new unique
	 *  SONAR name and sets the sent time.
	 */
	public PushNotificationImpl(String rt, String rn, boolean nw,
			String t, String d) {
		this(PushNotificationHelper.createUniqueName(),
				rt, rn, nw, new Date(), t, d, null, null);
	}
	
	public PushNotificationImpl(String n, String rt, String rn, boolean nw,
			Date st, String t, String d, String u, Date at) {
		super(n);
		ref_object_type = rt;
		ref_object_name = rn;
		needs_write = nw;
		sent_time = st;
		title = t;
		description = d;
		addressed_by = u;
		addressed_time = at;
	}

	/** Type of object being referenced */
	private String ref_object_type;
	
	/** Set the type of the object that is being referenced */
	@Override
	public void setRefObjectType(String rt) {
		ref_object_type = rt;
	}

	/** Set the type of the object that is being referenced */
	public void doSetRefObjectType(String rt) throws TMSException {
		if (!objectEquals(rt, ref_object_type)) {
			store.update(this, "ref_object_type", rt);
			setRefObjectType(rt);
		}
	}
	
	/** Get the type of the object that is being referenced */
	@Override
	public String getRefObjectType() {
		return ref_object_type;
	}

	/** Name of the object being referenced */
	private String ref_object_name;
	
	/** Set the SONAR name of the object that is being referenced */
	@Override
	public void setRefObjectName(String rn) {
		ref_object_name = rn;
	}

	/** Set the SONAR name of the object that is being referenced */
	public void doSetRefObjectName(String rn) throws TMSException {
		if (!objectEquals(rn, ref_object_name)) {
			store.update(this, "ref_object_name", rn);
			setRefObjectName(rn);
		}
	}
	
	/** Get the SONAR name of the object that is being referenced */
	@Override
	public String getRefObjectName() {
		return ref_object_name;
	}

	/** Whether users must be able to write this type of objects to see this
	 *  notification (by necessity they must be able to read them).
	 */
	private boolean needs_write;
	
	/** Set whether users must be able to write this type of objects to see
	 *  this notification.
	 */
	@Override
	public void setNeedsWrite(boolean nw) {
		needs_write = nw;
	}
	
	/** Set whether users must be able to write this type of objects to see
	 *  this notification.
	 */
	public void doSetNeedsWrite(boolean nw) throws TMSException {
		if (nw != needs_write) {
			store.update(this, "needs_write", nw);
			setNeedsWrite(nw);
		}
	}
	
	/** Get whether users must be able to write this type of objects to see
	 *  this notification.
	 */
	@Override
	public boolean getNeedsWrite() {
		return needs_write;
	}
	
	/** Time the notification was generated/sent */
	private Date sent_time;
	
	/** Set the time the notification was generated/sent */
	@Override
	public void setSentTime(Date st) {
		sent_time = st;
	}

	/** Set the time the notification was generated/sent */
	public void doSetSentTime(Date st) throws TMSException {
		if (!objectEquals(st, sent_time)) {
			store.update(this, "sent_time", st);
			setSentTime(st);
		}
	}
	
	/** Get the time the notification was generated/sent */
	@Override
	public Date getSentTime() {
		return sent_time;
	}
	
	/** Notification title */
	private String title;

	/** Set the notification title */
	@Override
	public void setTitle(String t) {
		title = t;
	}

	/** Set the notification title */
	public void doSetTitle(String t) throws TMSException {
		if (!objectEquals(t, title)) {
			store.update(this, "title", t);
			setTitle(t);
		}
	}
	
	/** Get the notification title */
	@Override
	public String getTitle() {
		return title;
	}
	
	/** Notification description */
	private String description;

	/** Set the notification description */
	@Override
	public void setDescription(String d) {
		description = d;
	}

	/** Set the notification description */
	public void doSetDescription(String d) throws TMSException {
		if (!objectEquals(d, description)) {
			store.update(this, "description", d);
			setDescription(d);
		}
	}
	
	/** Set the notification description, notifying clients if it changes.*/
	public void setDescriptionNotify(String d) throws TMSException {
		if (!objectEquals(d, description)) {
			doSetDescription(d);
			notifyAttribute("description");
		}
	}
	
	/** Get the notification description */
	@Override
	public String getDescription() {
		return description;
	}

	/** Name of the user who addressed this notification */
	private String addressed_by;
	
	/** Set the name of the user who addressed this notification */
	@Override
	public void setAddressedBy(String u) {
		addressed_by = u;
	}
	
	/** Set the name of the user who addressed this notification */
	public void doSetAddressedBy(String u) throws TMSException {
		if (!objectEquals(u, addressed_by)) {
			store.update(this, "addressed_by", u);
			setAddressedBy(u);
		}
	}
	
	/** Set the name of the user who addressed this notification */
	public void setAddressedByNotify(String u) throws TMSException {
		if (!objectEquals(u, addressed_by)) {
			doSetAddressedBy(u);
			notifyAttribute("addressedBy");
		}
	}
	
	/** Get the name of the user who addressed this notification */
	@Override
	public String getAddressedBy() {
		return addressed_by;
	}

	/** Time at which this notification was addressed */
	private Date addressed_time;
	
	/** Set the time at which this notification was addressed */
	@Override
	public void setAddressedTime(Date at) {
		addressed_time = at;
	}

	/** Set the time at which this notification was addressed */
	public void doSetAddressedTime(Date at) throws TMSException {
		if (!objectEquals(at, addressed_time)) {
			store.update(this, "addressed_time", at);
			setAddressedTime(at);
		}
	}
	
	/** Set the time at which this notification was addressed */
	public void setAddressedTimeNotify(Date at) throws TMSException {
		if (!objectEquals(at, addressed_time)) {
			doSetAddressedTime(at);
			notifyAttribute("addressedTime");
		}
	}
	
	/** Get the time at which this notification was addressed */
	@Override
	public Date getAddressedTime() {
		return addressed_time;
	}
	
}
