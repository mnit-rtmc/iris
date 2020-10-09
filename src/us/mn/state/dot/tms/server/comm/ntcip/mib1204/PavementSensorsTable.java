/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
 * Copyright (C) 2019  Minnesota Department of Transportation
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

import java.util.ArrayList;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.Json;

import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;

/**
 * Pavement sensors data table, where each table row contains data read from
 * a single pavement sensor within the same controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class PavementSensorsTable {

	/** A height of 1001 is an error condition or missing value */
	static private final int HEIGHT_ERROR_MISSING = 1001;

	/** Convert height to Distance.
	 * @param h Height in meters with 1001 indicating an error or missing
	 *          value.
	 * @return Height distance or null for missing */
	static private Distance convertHeight(ASN1Integer h) {
		if (h != null) {
			int ih = h.getInteger();
			if (ih < HEIGHT_ERROR_MISSING)
				return new Distance(ih, METERS);
		}
		return null;
	}

	/** An exposure of 101 is an error condition or missing value */
	static private final int EXPOSURE_ERROR_MISSING = 101;

	/** Convert solar exposure to percent.
	 * @param e Exposure in percent with 101 indicating an error or missing
	 *          value.
	 * @return Exposure or null for missing */
	static private Integer convertExposure(ASN1Integer e) {
		if (e != null) {
			int ie = e.getInteger();
			if (ie >= 0 && ie < EXPOSURE_ERROR_MISSING)
				return ie;
		}
		return null;
	}

	/** A depth of 255 is an error condition or missing value */
	static private final int DEPTH_ERROR_MISSING = 255;

	/** Convert depth to Distance.
	 * @param d Depth in millimeters with 255 indicating an error or missing
	 *          value.
	 * @return Depth distance or null for missing */
	static private Distance convertDepth(ASN1Integer d) {
		if (d != null) {
			int id = d.getInteger();
			if (id < DEPTH_ERROR_MISSING)
				return new Distance(id, MILLIMETERS);
		}
		return null;
	}

	/** A salinity of 65535 is an error condition or missing value */
	static private final int SALINITY_ERROR_MISSING = 65535;

	/** Convert value to salinity.
	 * @param s Salinity in parts per 100,000 by weight with 65535
	 * 	    indicating an error or missing value.
	 * @return Depth distance or null for missing */
	static private Integer convertSalinity(ASN1Integer s) {
		if (s != null) {
			int is = s.getInteger();
			if (is < SALINITY_ERROR_MISSING)
				return is;
		}
		return null;
	}

	/** Number of sensors in table */
	public final ASN1Integer num_sensors = numEssPavementSensors.makeInt();

	/** Table row */
	static public class Row {
		public final DisplayString location;
		public final ASN1Enum<EssPavementType> pavement_type;
		public final ASN1Integer height;
		public final ASN1Integer exposure;
		public final ASN1Enum<EssPavementSensorType> sensor_type;
		public final ASN1Enum<EssSurfaceStatus> surface_status;
		public final TemperatureObject surface_temp;
		public final TemperatureObject pavement_temp;
		public final ASN1Enum<EssPavementSensorError> sensor_error;
		public final ASN1Integer surface_water_depth;
		public final ASN1Integer salinity;
		public final TemperatureObject surface_freeze_point;
		public final ASN1Enum<EssSurfaceBlackIceSignal> black_ice_signal;

		/** Create a table row */
		private Row(int row) {
			location = new DisplayString(
				essPavementSensorLocation.node, row);
			pavement_type = new ASN1Enum<EssPavementType>(
				EssPavementType.class, essPavementType.node,
				row);
			height = essPavementElevation.makeInt(row);
			height.setInteger(HEIGHT_ERROR_MISSING);
			exposure = essPavementExposure.makeInt(row);
			exposure.setInteger(EXPOSURE_ERROR_MISSING);
			sensor_type = new ASN1Enum<EssPavementSensorType>(
				EssPavementSensorType.class,
				essPavementSensorType.node, row);
			surface_status = new ASN1Enum<EssSurfaceStatus>(
				EssSurfaceStatus.class, essSurfaceStatus.node,
				row);
			surface_temp = new TemperatureObject(
				essSurfaceTemperature.makeInt(row));
			pavement_temp = new TemperatureObject(
				essPavementTemperature.makeInt(row));
			sensor_error = new ASN1Enum<EssPavementSensorError>(
				EssPavementSensorError.class,
				essPavementSensorError.node, row);
			surface_water_depth = essSurfaceWaterDepth.makeInt(row);
			surface_water_depth.setInteger(DEPTH_ERROR_MISSING);
			salinity = essSurfaceSalinity.makeInt(row);
			salinity.setInteger(SALINITY_ERROR_MISSING);
			surface_freeze_point = new TemperatureObject(
				essSurfaceFreezePoint.makeInt(row));
			black_ice_signal = new ASN1Enum<EssSurfaceBlackIceSignal>
				(EssSurfaceBlackIceSignal.class,
				essSurfaceBlackIceSignal.node, row);
		}

		/** Get the sensor location */
		public String getSensorLocation() {
			String sl = location.getValue();
			return (sl.length() > 0) ? sl : null;
		}

		/** Get pavement type or null on error */
		public EssPavementType getPavementType() {
			EssPavementType ept = pavement_type.getEnum();
			return (ept != EssPavementType.undefined) ? ept : null;
		}

		/** Get pavement height in meters */
		public Integer getHeight() {
			Distance pe = convertHeight(height);
			return (pe != null) ? pe.round(METERS) : null;
		}

		/** Get pavement exposure in percent */
		public Integer getExposure() {
			return convertExposure(exposure);
		}

		/** Get pavement sensor type or null on error */
		public EssPavementSensorType getPavementSensorType() {
			EssPavementSensorType pst = sensor_type.getEnum();
			return (pst != EssPavementSensorType.undefined)
			      ? pst
			      : null;
		}

		/** Get surface status or null on error */
		public EssSurfaceStatus getSurfStatus() {
			EssSurfaceStatus ess = surface_status.getEnum();
			return (ess != EssSurfaceStatus.undefined) ? ess : null;
		}

		/** Get surface temp or null on error */
		public Integer getSurfTempC() {
			return surface_temp.getTempC();
		}

		/** Get pavement temp or null on error */
		public Integer getPvmtTempC() {
			return pavement_temp.getTempC();
		}

		/** Get pavement sensor error or null on error */
		public EssPavementSensorError getPavementSensorError() {
			EssPavementSensorError pse = sensor_error.getEnum();
			return (pse != null && pse.isError()) ? pse : null;
		}

		/** Get surface water depth in meters */
		public Float getSurfaceWaterDepth() {
			Distance d = convertDepth(surface_water_depth);
			return (d != null) ? d.asFloat(METERS) : null;
		}

		/** Get surface salinity in parts per 100,000 by weight */
		public Integer getSalinity() {
			return convertSalinity(salinity);
		}

		/** Get surf freeze temp or null on error */
		public Integer getSurfFreezePointC() {
			return surface_freeze_point.getTempC();
		}

		/** Get black ice signal or null on error */
		public EssSurfaceBlackIceSignal getBlackIceSignal() {
			EssSurfaceBlackIceSignal bis = black_ice_signal.getEnum();
			return (bis != null && bis.isValue()) ? bis : null;
		}

		/** Get JSON representation */
		private String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(Json.str("location", getSensorLocation()));
			sb.append(Json.str("pavement_type", getPavementType()));
			sb.append(Json.num("height", getHeight()));
			sb.append(Json.num("exposure", getExposure()));
			sb.append(Json.str("sensor_type",
				getPavementSensorType()));
			sb.append(Json.str("surface_status", getSurfStatus()));
			sb.append(surface_temp.toJson("surface_temp"));
			sb.append(pavement_temp.toJson("pavement_temp"));
			sb.append(Json.str("sensor_error",
				getPavementSensorError()));
			sb.append(Json.num("surface_water_depth",
				getSurfaceWaterDepth()));
			sb.append(Json.num("salinity", getSalinity()));
			sb.append(surface_freeze_point.toJson(
				"surface_freeze_point"));
			sb.append(Json.str("black_ice_signal",
				getBlackIceSignal()));
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("},");
			return sb.toString();
		}
	}

	/** Rows in table */
	private final ArrayList<Row> table_rows = new ArrayList<Row>();

	/** Get number of rows in table reported by ESS */
	private int size() {
		return num_sensors.getInteger();
	}

	/** Check if all rows have been read */
	public boolean isDone() {
		return table_rows.size() >= size();
	}

	/** Add a row to the table */
	public Row addRow() {
		Row tr = new Row(table_rows.size() + 1);
		table_rows.add(tr);
		return tr;
	}

	/** Get one table row */
	public Row getRow(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1)
		      : null;
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		if (table_rows.size() > 0) {
			sb.append("\"pavement_sensor\":[");
			for (Row row : table_rows)
				sb.append(row.toJson());
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("],");
		}
		return sb.toString();
	}
}
