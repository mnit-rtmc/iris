/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2021  Minnesota Department of Transportation
 * Copyright (C) 2012-2021  Iteris Inc.
 * Copyright (C) 2015-2023  SRF Consulting Group
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

import java.util.Arrays;
import java.util.Comparator;

/**
 * Communication protocol enumeration.  The ordinal values correspond to the
 * records in the iris.comm_protocol look-up table.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author John L. Stanley - SRF Consulting
 */
public enum CommProtocol {

	/** NTCIP Class B (0) */
	NTCIP_B("NTCIP Class B"),

	/** MnDOT 170 4-bit (1) */
	MNDOT_4("MnDOT 170 (4-bit)"),

	/** MnDOT 170 5-bit (2) */
	MNDOT_5("MnDOT 170 (5-bit)"),

	/** Wavetronix SmartSensor 105 (3) */
	SS_105("SmartSensor 105"),

	/** Canoga (4) */
	CANOGA("Canoga"),

	/** Pelco P (5) */
	PELCO_P("Pelco P"),

	/** Pelco D camera control (6) */
	PELCO_D_PTZ("Pelco D PTZ"),

	/** NTCIP Class C (7) */
	NTCIP_C("NTCIP Class C", false),

	/** Manchester camera control (8) */
	MANCHESTER_PTZ("Manchester PTZ"),

	/** DMS XML (9) */
	DMSXML("DMS XML"),

	/** Msg Feed (10) */
	MSG_FEED("MSG_FEED", false),

	/** NTCIP Class A (11) */
	NTCIP_A("NTCIP Class A", false),

	/** Banner DXM (12) */
	BANNER_DXM("Banner DXM", false),

	/** Vicon camera control (13) */
	VICON_PTZ("Vicon PTZ"),

	/** Wavetronix SmartSensor 125 HD (14) */
	SS_125("SmartSensor 125 HD"),

	/** Optical Scientific ORG-815 Precipitation Sensor (15) */
	ORG_815("OSi ORG-815", false),

	/** Infinova wrapping Pelco D camera control (16) */
	INFINOVA_D_PTZ("Infinova D PTZ"),

	/** RTMS G4 (17) */
	RTMS_G4("RTMS G4"),

	/** RTMS G4 Vehicle logging (18) */
	RTMS_G4_VLOG("RTMS G4 vlog"),

	/** Wavetronix SmartSensor 125 Vehicle logging (19) */
	SS_125_VLOG("SmartSensor 125 vlog"),

	/** Natch (20) */
	NATCH("Natch"),

	/** PeMS (21) */
	@Deprecated
	PEMS("PeMS", false),

	/** SSI -- removed (22) */
	@Deprecated
	SSI("SSI", false),

	/** CHP Incidents (23) */
	@Deprecated
	CHP_INCIDENTS("CHP Incidents", false),

	/** Nebraska (NDOT) Beacon (24) */
	NDOT_BEACON("NDOT Beacon", false),

	/** Digital Loggers Inc DIN Relay (25) */
	DIN_RELAY("DLI DIN Relay", false),

	/** Axis 292 Video Decoder (26) */
	@Deprecated
	AXIS_292("Axis 292"),

	/** Axis PTS (27) */
	AXIS_PTZ("Axis PTZ", false),

	/** HySecurity STC gate arm (28) */
	HYSECURITY_STC("HySecurity STC"),

	/** Cohu PTZ (29) */
	COHU_PTZ("Cohu PTZ"),

	/** DR-500 doppler radar (30) */
	DR_500("DR-500", false),

	/** ADDCO NodeComm sign control -- removed (31) */
	@Deprecated
	ADDCO("ADDCO"),

	/** TransCore E6 tag reader (32) */
	TRANSCORE_E6("TransCore E6", false),

	/** Control By Web (33) */
	CBW("CBW", false),

	/** Incident feed (34) */
	INC_FEED("Incident Feed", false),

	/** MonStream video switching (35) */
	MON_STREAM("MonStream", false),

	/** (Nebraska Department of Roads) NDOR GateArm v5 (36) */
	GATE_NDOR5("GATE NDORv5"),

	/** GPS using TAIP protocol (37) */
	GPS_TAIP("GPS TAIP"),

	/** Sierra Wireless GX modem (38) */
	SIERRA_GX("SierraGX"),

	/** GPS using RedLion AT+BMDIAG command (39) */
	GPS_REDLION("GPS RedLion"),
	
	/** Cohu Helois PTZ (40) */
	COHU_HELIOS_PTZ("Cohu Helios PTZ"),

	/** Streambed (41) */
	STREAMBED("Streambed", false),
	
	/** CAP feed (such as IPAWS-OPEN) (42) */
	CAP("CAP Feed", false),

	/** ClearGuide (43) */
	CLEARGUIDE("ClearGuide", false),

	/** GPS using Digi WR-series modem (44) */
	GPS_DIGI_WR("GPS Digi WR", false),

	/** ONVIF PTZ (45) */
	ONVIF_PTZ("ONVIF PTZ"),
	
	/** Sierra SSH GPS (46) */
	SIERRA_SSH_GPS("Sierra SSH GPS");
	
	/** Create a new comm protocol value */
	private CommProtocol(String d) {
		this(d, true);
	}

	/** Create a new comm protocol value */
	private CommProtocol(String d, boolean md) {
		description = d;
		multidrop = md;
	}

	/** Protocol description */
	public final String description;

	/** Flag for multidrop */
	public final boolean multidrop;

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Test if a comm protocol supports gate arm control */
	public boolean isGateArm() {
		return this == CommProtocol.HYSECURITY_STC ||
		       this == CommProtocol.GATE_NDOR5;
	}

	/** Values array */
	static private final CommProtocol[] VALUES = values();

	/** Get a comm protocol from an ordinal value */
	static public CommProtocol fromOrdinal(short o) {
		if (o >= 0 && o < VALUES.length)
			return VALUES[o];
		else
			return null;
	}

	/** Get protocol values sorted by name */
	static public CommProtocol[] valuesSorted() {
		CommProtocol[] v = values();
		Arrays.sort(v, new Comparator<CommProtocol>() {
			public int compare(CommProtocol cp0, CommProtocol cp1) {
				return cp0.description.compareTo(
				       cp1.description);
			}
		});
		return v;
	}
}
