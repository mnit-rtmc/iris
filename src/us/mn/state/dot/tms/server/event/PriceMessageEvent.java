/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2024  Minnesota Department of Transportation
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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.SString;

/**
 * This is a class for logging price message events to a database.
 *
 * @author Douglas Lau
 */
public class PriceMessageEvent extends BaseEvent {

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

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "price_message_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.price_message_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("event_desc_id", event_type.id);
		map.put("device_id", device_id);
		map.put("toll_zone", toll_zone);
		map.put("detector", detector);
		map.put("price", price);
		return map;
	}
}
