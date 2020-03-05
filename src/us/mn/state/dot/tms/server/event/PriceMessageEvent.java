/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.event;

import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.SString;

/**
 * This is a class for logging price message events to a database.
 *
 * @author Douglas Lau
 */
public class PriceMessageEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.price_message_event";

	/** Get price message event purge threshold (days) */
	static private int getPurgeDays() {
		return SystemAttrEnum.PRICE_MESSAGE_EVENT_PURGE_DAYS.getInt();
	}

	/** Purge old records */
	static public void purgeRecords() throws TMSException {
		int age = getPurgeDays();
		if (store != null && age > 0) {
			store.update("DELETE FROM " + TABLE +
				" WHERE event_date < now() - '" + age +
				" days'::interval;");
		}
	}

	/** Device ID */
	private final String device_id;

	/** Toll zone ID */
	private final String toll_zone;

	/** Detector name */
	private final String detector;

	/** Price on message */
	public final float price;

	/** Create a new price message event */
	public PriceMessageEvent(EventType et, String d, String tz, String det,
		float p)
	{
		super(et);
		assert et == EventType.PRICE_DEPLOYED ||
		       et == EventType.PRICE_VERIFIED;
		device_id = d;
		toll_zone = tz;
		detector = SString.truncate(det, 20);
		price = p;
	}

	/** Get a price message event with specified event type */
	public PriceMessageEvent withEventType(EventType net) {
		return (net == event_type) ? this : new PriceMessageEvent(net,
			device_id, toll_zone, detector, price);
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return TABLE;
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_date", event_date);
		map.put("event_desc_id", event_type.id);
		map.put("device_id", device_id);
		map.put("toll_zone", toll_zone);
		map.put("detector", detector);
		map.put("price", price);
		return map;
	}
}
