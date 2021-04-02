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
	SAB("Avalanche Advisory"),
	AVW("Avalanche Warning"),
	AVA("Avalanche Watch"),
	BHS("Beach Hazard Statement"),
	BZW("Blizzard Warning"),
	DUY("Blowing Dust Advisory"),
	BLU("Blue Alert"),
	BWY("Brisk Wind Advisory"),
	CAE("Child Abduction Emergency"),
	CDW("Civil Danger Warning"),
	CEM("Civil Emergency Message"),
	CFW("Coastal Flood Warning"),
	CFA("Coastal Flood Watch"),
	CFY("Coastal Flood Advisory"),
	FGY("Dense Fog Advisory"),
	MFY("Dense Fog Advisory"),
	DSY("Dust Advisory"),
	DSW("Dust Storm Warning"),
	EQW("Earthquake Warning"),
	EAN("Emergency Action Notification"),
	EAT("Emergency Action Termination"),
	EVI("Evacuation Immediate"),
	EWW("Extreme Wind Warning"),
	FRW("Fire Warning"),
	FFS("Flash Flood Statement"),
	FFW("Flash Flood Warning"),
	FFA("Flash Flood Watch"),
	FSW("Flash Freeze Warning"),
	FAY("Flood Advisory"),
	FLY("Flood Advisory"),
	FLS("Flood Statement"),
	FAW("Flood Warning"),
	FLW("Flood Warning"),
	FAA("Flood Watch"),
	FLA("Flood Watch"),
	FZW("Freeze Warning"),
	FZA("Freeze Watch"),
	ZFY("Freezing Fog Advisory"),
	GLW("Gale Warning"),
	GLA("Gale Watch"),
	HZW("Hard Freeze Warning"),
	HZA("Hard Freeze Watch"),
	HMW("Hazardous Materials Warning"),
	SEW("Hazardous Seas Warning"),
	SEA("Hazardous Seas Watch"),
	UPY("Heavy Freezing Spray Advisory"),
	SUY("High Surf Advisory"),
	SUW("High Surf Warning"),
	HWW("High Wind Warning"),
	HWA("High Wind Watch"),
	HLS("Hurricane Statement"),
	HUW("Hurricane Warning"),
	HUA("Hurricane Watch"),
	ISW("Ice Storm Warning"),
	LEW("Law Enforcement Warning"),
	LWY("Lake Wind Advisory"),
	LAE("Local Area Emergency"),
	LOY("Low Water Advisory"),
	MWS("Marine Weather Statement"),
	NAT("National Audible Test"),
	NIC("National Information Center"),
	NPT("National Periodic Test"),
	NST("National Silent Test"),
	NMN("Network Message Notification"),
	NUW("Nuclear Power Plant Warning"),
	DMO("Practice/Demo Warning"),
	RHW("Radiological Hazard Warning"),
	FWW("Red Flag Warning"),
	RMT("Required Monthly Test"),
	RWT("Required Weekly Test"),
	RPS("Rip Current Statement"),
	SVW("Severe Thunderstorm Warning"),
	SVA("Severe Thunderstorm Watch"),
	SVS("Severe Weather Statement"),
	SPW("Shelter in Place Warning"),
	SCY("Small Craft Advisory"),
	SQW("Snowsquall Warning"),
	MAW("Special Marine Warning"),
	SPS("Special Weather Statement"),
	SRW("Storm Warning"),
	SSW("Storm Surge Warning"),
	SSA("Storm Surge Watch"),
	TOE("911 Telephone Outage Emergency"),
	TOW("Tornado Warning"),
	TOA("Tornado Watch"),
	TRW("Tropical Storm Warning"),
	TRA("Tropical Storm Watch"),
	TSW("Tsunami Warning"),
	TSA("Tsunami Watch"),
	VOW("Volcano Warning"),
	WIY("Wind Advisory"),
	WCY("Wind Chill Advisory"),
	WCW("Wind Chill Warning"),
	WCA("Wind Chill Watch"),
	WSW("Winter Storm Warning"),
	WSA("Winter Storm Watch"),
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
