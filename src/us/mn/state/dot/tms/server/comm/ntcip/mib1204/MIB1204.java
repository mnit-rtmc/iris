/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
 * Copyright (C) 2017-2021  Iteris Inc.
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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
 * MIB nodes for NTCIP 1204 - Environmental Sensor Stations (ESS)
 *
 * @author John L. Stanley - SRF Consulting
 * @author Michael Darter
 * @author Douglas Lau
 */
public enum MIB1204 {
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
	      essInstantaneousTerrestrialRadiation(essBufrRadiation, 17), // V2
	      essInstantaneousSolarRadiation	(essBufrRadiation, 18), // V2
	      essSolarRadiation			(essBufrRadiation, 24), // V1
	      essTotalRadiation			(essBufrRadiation, 25), // V2
	      essTotalSun			(essBufrRadiation, 31),
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
	      essSpotWindDirection		(essNtcipWind, 1),
	      essSpotWindSpeed			(essNtcipWind, 2),
	      essWindSituation			(essNtcipWind, 3),
	      windSensorTableNumSensors		(essNtcipWind, 7), // V2
	      windSensorTable			(essNtcipWind, 8), // V2
	        windSensorEntry			(windSensorTable, 1), // V2
	          windSensorIndex		(windSensorEntry, 1), // V2
	          windSensorHeight		(windSensorEntry, 2), // V2
	          windSensorLocation		(windSensorEntry, 3), // V2
	          windSensorAvgSpeed		(windSensorEntry, 4), // V2
	          windSensorAvgDirection	(windSensorEntry, 5), // V2
	          windSensorSpotSpeed		(windSensorEntry, 6), // V2
	          windSensorSpotDirection	(windSensorEntry, 7), // V2
	          windSensorGustSpeed		(windSensorEntry, 8), // V2
	          windSensorGustDirection	(windSensorEntry, 9), // V2
	          windSensorSituation		(windSensorEntry, 10), // V2
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
	      essWaterDepth			(essNtcipPrecip, 1),
	      essAdjacentSnowDepth		(essNtcipPrecip, 2),
	      essRoadwaySnowDepth		(essNtcipPrecip, 3),
	      essRoadwaySnowPackDepth		(essNtcipPrecip, 4),
	      essPrecipYesNo			(essNtcipPrecip, 5),
	      essPrecipSituation		(essNtcipPrecip, 6),
	      essIceThickness			(essNtcipPrecip, 7),
	      essPrecipitationStartTime		(essNtcipPrecip, 8),
	      essPrecipitationEndTime		(essNtcipPrecip, 9),
	      precipitationSensorModelInformation(essNtcipPrecip, 10),
	      waterLevelSensorTableNumSensors	(essNtcipPrecip, 11),
	    essNtcipRadiation			(essNtcip, 7),
	      essCloudSituation			(essNtcipRadiation, 1),
	      essTotalRadiationPeriod		(essNtcipRadiation, 2), // V2
	    essNtcipVisibility			(essNtcip, 8),
	      essVisibility			(essNtcipVisibility, 1),
	      essVisibilitySituation		(essNtcipVisibility, 3),
	    essNtcipPavement			(essNtcip, 9),
	      numEssPavementSensors		(essNtcipPavement, 1),
	      essPavementSensorTable		(essNtcipPavement, 2),
	        essPavementSensorEntry		(essPavementSensorTable, 1),
	          essPavementSensorIndex	(essPavementSensorEntry, 1),
	          essPavementSensorLocation	(essPavementSensorEntry, 2),
	          essPavementType		(essPavementSensorEntry, 3),
	          essPavementElevation		(essPavementSensorEntry, 4),
	          essPavementExposure		(essPavementSensorEntry, 5),
	          essPavementSensorType		(essPavementSensorEntry, 6),
	          essSurfaceStatus		(essPavementSensorEntry, 7),
	          essSurfaceTemperature		(essPavementSensorEntry, 8),
	          essPavementTemperature	(essPavementSensorEntry, 9),
	          essSurfaceWaterDepth		(essPavementSensorEntry, 10),
	          essSurfaceSalinity		(essPavementSensorEntry, 11),
	          essSurfaceConductivity	(essPavementSensorEntry, 12),
	          essSurfaceFreezePoint		(essPavementSensorEntry, 13),
	          essSurfaceBlackIceSignal	(essPavementSensorEntry, 14),
	          essPavementSensorError	(essPavementSensorEntry, 15),
	          essSurfaceIceOrWaterDepth	(essPavementSensorEntry, 16),
	          essSurfaceConductivityV2	(essPavementSensorEntry, 17),
	          pavementSensorModelInformation(essPavementSensorEntry, 18),
	          pavementSensorTemperatureDepth(essPavementSensorEntry, 19),
	          pavementSensorLatitude	(essPavementSensorEntry, 20), // V4
	          pavementSensorLongitude	(essPavementSensorEntry, 21), // V4
	          pavementSensorSurfaceCondition(essPavementSensorEntry, 22), // V4
	          pavementSensorForecastCondition(essPavementSensorEntry, 23), // V4
	          pavementSensorFrictionCoefficient(essPavementSensorEntry, 24), // V4
	          pavementMonitorLatitude	(essPavementSensorEntry, 25), // V4
	          pavementMonitorLongitude	(essPavementSensorEntry, 26), // V4
	          pavementIcePercentage		(essPavementSensorEntry, 27), // V4
	      numEssSubSurfaceSensors		(essNtcipPavement, 3),
	      essSubSurfaceSensorTable		(essNtcipPavement, 4),
	        essSubSurfaceSensorEntry	(essSubSurfaceSensorTable, 1),
	          essSubSurfaceSensorIndex	(essSubSurfaceSensorEntry, 1),
	          essSubSurfaceSensorLocation	(essSubSurfaceSensorEntry, 2),
	          essSubSurfaceType		(essSubSurfaceSensorEntry, 3),
	          essSubSurfaceSensorDepth	(essSubSurfaceSensorEntry, 4),
	          essSubSurfaceTemperature	(essSubSurfaceSensorEntry, 5),
	          essSubSurfaceMoisture		(essSubSurfaceSensorEntry, 7),
	          essSubSurfaceSensorError	(essSubSurfaceSensorEntry, 8),
	    essNtcipMobile			(essNtcip, 10),
	      essMobileFriction			(essNtcipMobile, 1),
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
}
