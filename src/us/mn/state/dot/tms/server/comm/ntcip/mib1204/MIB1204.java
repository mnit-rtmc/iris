/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
 * Copyright (C) 2017	    Iteris Inc.
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

package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for NTCIP 1204 -
 *  Object Definitions for Environmental Sensor Stations (ESS)
 *  
 * @author John L. Stanley - SRF Consulting
 * @author Michael Darter
 */

public enum MIB1204 {
	system					(new int[] {1,3,6,1,2,1,1}),
	  sysDescr				(system, 1),
	  sysObjectID				(system, 2),
	  sysUpTime				(system, 3),
	  sysContact				(system, 4),
	  sysName				(system, 5),
	  sysLocation				(system, 6),
	ess					(MIB1201.devices, 5),
	  essBufr				(ess, 1),
	    essBufrInstrumentation		(essBufr, 2),
	      essTypeofStation			(essBufrInstrumentation, 1),
	    essBufrLocationVertical		(essBufr, 7),
	      essAtmosphericPressure		(essBufrLocationVertical, 4),
	    essBufrWind				(essBufr, 11),
	      essAvgWindDirection		(essBufrWind, 1),
	      essAvgWindSpeed			(essBufrWind, 2),
	      essMaxWindGustSpeed		(essBufrWind, 41),
	      essMaxWindGustDir			(essBufrWind, 43),
	    essBufrPrecip			(essBufr, 13),
	      essRelativeHumidity		(essBufrPrecip, 3),
	      essPrecipRate			(essBufrPrecip, 14),
	      essSnowfallAccumRate		(essBufrPrecip, 15),
	      essPrecipitationOneHour		(essBufrPrecip, 19),
	      essPrecipitationThreeHours	(essBufrPrecip, 20),
	      essPrecipitationSixHours		(essBufrPrecip, 21),
	      essPrecipitationTwelveHours	(essBufrPrecip, 22),
	      essPrecipitation24Hours		(essBufrPrecip, 23),
	    essBufrRadiation			(essBufr, 14),
	  essNtcip				(ess, 2),
	    essNtcipIdentification		(essNtcip, 1),
	      essNtcipCategory			(essNtcipIdentification, 1),
	      essNtcipSiteDescription		(essNtcipIdentification, 2),
	    essNtcipLocation			(essNtcip, 2),
	      essLatitude			(essNtcipLocation, 1),
	      essLongitude			(essNtcipLocation, 2),
	      essVehicleSpeed			(essNtcipLocation, 3),
	      essVehicleBearing			(essNtcipLocation, 4),
	      essOdometer			(essNtcipLocation, 5),
	    essNtcipHeight			(essNtcip, 3),
	      essReferenceHeight		(essNtcipHeight, 1),
	      essPressureHeight			(essNtcipHeight, 2),
	      essWindSensorHeight		(essNtcipHeight, 3),
	    essNtcipWind			(essNtcip, 4),
	      essSpotWindSpeed			(essNtcipWind, 2),
	      essWindSituation			(essNtcipWind, 3),
	      windSensorAvgDirectionV2		(essNtcipWind, 4),
	      windSensorSpotDirectionV2		(essNtcipWind, 5),
	      windSensorGustDirectionV2		(essNtcipWind, 6),
	    essNtcipTemperature			(essNtcip, 5),
	      essNumTemperatureSensors		(essNtcipTemperature, 1),
	      essTemperatureSensorTable		(essNtcipTemperature, 2),
	        essTemperatureSensorEntry	(essTemperatureSensorTable, 1),
	          essTemperatureSensorIndex	(essTemperatureSensorEntry, 1),
	          essTemperatureSensorHeight	(essTemperatureSensorEntry, 2),
	          essAirTemperature		(essTemperatureSensorEntry, 3),
	      essWetbulbTemp			(essNtcipTemperature, 3),
	      essDewpointTemp			(essNtcipTemperature, 4),
	      essMaxTemp			(essNtcipTemperature, 5),
	      essMinTemp			(essNtcipTemperature, 6),
	    essNtcipPrecip			(essNtcip, 6),
	    essNtcipRadiation			(essNtcip, 7),
	    essNtcipVisibility			(essNtcip, 8),
	      essVisibility			(essNtcipVisibility, 1),
	      essVisibilitySituation		(essNtcipVisibility, 3),
	    essNtcipPavement			(essNtcip, 9),
	    essNtcipMobile			(essNtcip, 10),
	    essNtcipInstrumentation		(essNtcip, 15),
	      essDoorStatus			(essNtcipInstrumentation, 1),
	      essBatteryStatus			(essNtcipInstrumentation, 2),
	      essLineVolts			(essNtcipInstrumentation, 3);

	/** MIB node */
	public final MIBNode node;

	/** Create a root node */
	private MIB1204(int[] n) {
		node = MIBNode.root(n, toString());
	}

	/** Create a node with MIB1201 parent */
	private MIB1204(MIB1201 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Create a new MIB1204 node */
	private MIB1204(MIB1204 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Make an integer */
	public ASN1Integer makeInt() {
		return new ASN1Integer(node);
	}

	/** Make an integer */
	public ASN1Integer makeInt(int r) {
		return new ASN1Integer(node, r);
	}

	/** Make an integer */
	public ASN1Integer makeInt(int r, int s) {
		return new ASN1Integer(node, r, s);
	}
}
