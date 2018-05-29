/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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

/**
 * Amenities enumeration for parking areas.  The ordinal values correspond to
 * the bits in the iris.parking_area_amenities look-up table.
 *
 * @author Douglas Lau
 */
public enum ParkingAreaAmenities {
	flush_toilet,                   // 0
	assisted_restroom,              // 1
	drinking_fountain,              // 2
	shower,                         // 3
	picnic_table,                   // 4
	picnic_shelter,                 // 5
	pay_phone,                      // 6
	tty_pay_phone,                  // 7
	wireless_internet,              // 8
	atm,                            // 9
	vending_machine,                // 10
	shop,                           // 11
	play_area,                      // 12
	pet_exercise_area,              // 13
	interpretive_information;       // 14

	/** I18n name */
	public String i18n() {
		return "parking_area.amenity." + toString();
	};

	/** Number of amenities in enum */
	static public int SIZE = values().length;
}
