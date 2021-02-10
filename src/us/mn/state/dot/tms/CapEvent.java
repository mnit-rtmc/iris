/*
 * IRIS -- Intelligent Roadway Information System
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

/**
 * Common Alerting Protocol (CAP) event code enum.
 *
 * The values correspond to the records in the cap.event look-up table.
 *
 * @author Douglas Lau
 */
public enum CapEvent {
	ADR("Administrative Message"),
	AVA("Avalanche Watch"),
	AVW("Avalanche Warning"),
	BLU("Blue Alert"),
	BZW("Blizzard Warning"),
	BWY("Brisk Wind Advisory"),
	CAE("Child Abduction Emergency"),
	CDW("Civil Danger Warning"),
	CEM("Civil Emergency Message"),
	CFA("Coastal Flood Watch"),
	CFW("Coastal Flood Warning"),
	DMO("Practice/Demo Warning"),
	DSW("Dust Storm Warning"),
	EAN("Emergency Action Notification"),
	EAT("Emergency Action Termination"),
	EQW("Earthquake Warning"),
	EVI("Evacuation Immediate"),
	EWW("Extreme Wind Warning"),
	FFA("Flash Flood Watch"),
	FFS("Flash Flood Statement"),
	FFW("Flash Flood Warning"),
	FGY("Dense Fog Advisory"),
	FLA("Flood Watch"),
	FLS("Flood Statement"),
	FLW("Flood Warning"),
	FLY("Flood Advisory"),
	FRW("Fire Warning"),
	FSW("Flash Freeze Warning"),
	FZW("Freeze Warning"),
	GLA("Gale Watch"),
	GLW("Gale Warning"),
	HLS("Hurricane Statement"),
	HMW("Hazardous Materials Warning"),
	HUA("Hurricane Watch"),
	HUW("Hurricane Warning"),
	HWA("High Wind Watch"),
	HWW("High Wind Warning"),
	LAE("Local Area Emergency"),
	LEW("Law Enforcement Warning"),
	MWS("Marine Weather Statement"),
	NAT("National Audible Test"),
	NIC("National Information Center"),
	NMN("Network Message Notification"),
	NPT("National Periodic Test"),
	NUW("Nuclear Power Plant Warning"),
	NST("National Silent Test"),
	RHW("Radiological Hazard Warning"),
	RMT("Required Monthly Test"),
	RPS("Rip Current Statement"),
	RWT("Required Weekly Test"),
	SCY("Small Craft Advisory"),
	SMW("Special Marine Warning"),
	SPS("Special Weather Statement"),
	SPW("Shelter in Place Warning"),
	SQW("Snowsquall Warning"),
	SSA("Storm Surge Watch"),
	SSW("Storm Surge Warning"),
	SUW("High Surf Warning"),
	SUY("High Surf Advisory"),
	SVA("Severe Thunderstorm Watch"),
	SVR("Severe Thunderstorm Warning"),
	SVS("Severe Weather Statement"),
	TOA("Tornado Watch"),
	TOE("911 Telephone Outage Emergency"),
	TOR("Tornado Warning"),
	TRA("Tropical Storm Watch"),
	TRW("Tropical Storm Warning"),
	TSA("Tsunami Watch"),
	TSW("Tsunami Warning"),
	VOW("Volcano Warning"),
	WCY("Wind Chill Advisory"),
	WIY("Wind Advisory"),
	WSA("Winter Storm Watch"),
	WSW("Winter Storm Warning"),
	WWY("Winter Weather Advisory");

	/** Event description */
	public final String description;

	/** Create a CAP event */
	private CapEvent(String d) {
		description = d;
	}

	/** Values array */
	static private final CapEvent[] VALUES = values();

	/** Get a CapEvent from a code */
	static public CapEvent fromCode(String c) {
		for (CapEvent e: VALUES) {
			if (e.name().equalsIgnoreCase(c))
				return e;
		}
		return null;
	}

	/** Get the CapEvent from an event description */
	static public CapEvent fromDescription(String d) {
		for (CapEvent e: VALUES) {
			if (e.description.equalsIgnoreCase(d))
				return e;
		}
		return null;
	}
}
