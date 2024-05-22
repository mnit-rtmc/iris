/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
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

import java.util.ArrayList;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;
import static us.mn.state.dot.tms.units.Distance.Units.MICROMETERS;
import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;
import us.mn.state.dot.tms.utils.Json;

/**
 * Pavement sensors data table, where each table row contains data read from
 * a single pavement sensor within the same controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class PavementSensorsTable {

	/** A depth of 255 is an error condition or missing value */
	static private final int DEPTH_V1_ERROR_MISSING = 255;

	/** Convert depth to Distance.
	 * @param d Depth in millimeters with 255 indicating an error or missing
	 *          value.
	 * @return Depth distance or null for missing */
	static private Distance convertDepthV1(ASN1Integer d) {
		if (d != null) {
			int id = d.getInteger();
			if (id < DEPTH_V1_ERROR_MISSING)
				return new Distance(id, MILLIMETERS);
		}
		return null;
	}

	/** A depth of 65,535 is an error condition or missing value */
	static private final int DEPTH_V2_ERROR_MISSING = 65535;

	/** Convert depth to Distance.
	 * @param d Depth in tenth of millimeters with 65,535 indicating an
	 *          error or missing value.
	 * @return Depth distance or null for missing */
	static private Distance convertDepthV2(ASN1Integer d) {
		if (d != null) {
			int id = d.getInteger();
			if (id < DEPTH_V2_ERROR_MISSING)
				return new Distance(100 * id, MICROMETERS);
		}
		return null;
	}

	/** A salinity of 65,535 is an error condition or missing value */
	static private final int SALINITY_ERROR_MISSING = 65535;

	/** Convert salinity to ppm.
	 * @param s Salinity in parts per 100,000 by weight with 65535
	 * 	    indicating an error or missing value.
	 * @return Salinity in ppm or null for missing */
	static private Integer convertSalinity(ASN1Integer s) {
		if (s != null) {
			int is = s.getInteger();
			if (is >= 0 && is < SALINITY_ERROR_MISSING)
				return 10 * is;
		}
		return null;
	}

	/** Number of sensors in table */
	public final ASN1Integer num_sensors = numEssPavementSensors.makeInt();

	/** Table row */
	static public class Row {
		public final int number;
		public final DisplayString location;
		public final ASN1Enum<PavementType> pavement_type;
		public final HeightObject height;
		public final PercentObject exposure;
		public final ASN1Enum<PavementSensorType> sensor_type;
		public final ASN1Enum<SurfaceStatus> surface_status;
		public final TemperatureObject surface_temp;
		public final TemperatureObject pavement_temp;
		public final ASN1Enum<PavementSensorError> sensor_error;
		public final ASN1Integer water_depth;
		public final ASN1Integer ice_or_water_depth;
		public final ASN1Integer salinity;
		public final TemperatureObject freeze_point;
		public final ASN1Enum<SurfaceBlackIceSignal> black_ice_signal;
		public final ASN1Integer surface_conductivity_v2;
		public final PercentObject friction;

		/** Create a table row */
		private Row(int row) {
			number = row;
			location = new DisplayString(
				essPavementSensorLocation.node, row);
			pavement_type = new ASN1Enum<PavementType>(
				PavementType.class, essPavementType.node, row);
			height = new HeightObject("height",
				essPavementElevation.makeInt(row));
			exposure = new PercentObject("exposure",
				essPavementExposure.makeInt(row));
			sensor_type = new ASN1Enum<PavementSensorType>(
				PavementSensorType.class,
				essPavementSensorType.node, row);
			surface_status = new ASN1Enum<SurfaceStatus>(
				SurfaceStatus.class, essSurfaceStatus.node,
				row);
			surface_temp = new TemperatureObject("surface_temp",
				essSurfaceTemperature.makeInt(row));
			pavement_temp = new TemperatureObject("pavement_temp",
				essPavementTemperature.makeInt(row));
			sensor_error = new ASN1Enum<PavementSensorError>(
				PavementSensorError.class,
				essPavementSensorError.node, row);
			water_depth = essSurfaceWaterDepth.makeInt(row);
			water_depth.setInteger(DEPTH_V1_ERROR_MISSING);
			ice_or_water_depth =
				essSurfaceIceOrWaterDepth.makeInt(row);
			ice_or_water_depth.setInteger(DEPTH_V2_ERROR_MISSING);
			salinity = essSurfaceSalinity.makeInt(row);
			salinity.setInteger(SALINITY_ERROR_MISSING);
			freeze_point = new TemperatureObject("freeze_point",
				essSurfaceFreezePoint.makeInt(row));
			black_ice_signal = new ASN1Enum<SurfaceBlackIceSignal>(
				SurfaceBlackIceSignal.class,
				essSurfaceBlackIceSignal.node, row);
			surface_conductivity_v2 = essSurfaceConductivityV2.makeInt(row);
			friction = new PercentObject("friction",
				pavementSensorFrictionCoefficient.makeInt(row));
		}

		/** Get the sensor location */
		public String getSensorLocation() {
			String sl = location.getValue();
			return (sl.length() > 0) ? sl : null;
		}

		/** Get pavement type or null on error */
		public PavementType getPavementType() {
			PavementType pt = pavement_type.getEnum();
			return (pt != PavementType.undefined) ? pt : null;
		}

		/** Get pavement sensor type or null on error */
		public PavementSensorType getPavementSensorType() {
			PavementSensorType pst = sensor_type.getEnum();
			return (pst != PavementSensorType.undefined)
			      ? pst
			      : null;
		}

		/** Get surface status or null on error */
		public SurfaceStatus getSurfStatus() {
			SurfaceStatus ess = surface_status.getEnum();
			return (ess != SurfaceStatus.undefined) ? ess : null;
		}

		/** Get surface temp or null on error */
		public Integer getSurfTempC() {
			return surface_temp.getTempC();
		}

		/** Get pavement temp or null on error */
		public Integer getPvmtTempC() {
			return pavement_temp.getTempC();
		}

		/** Get pavement friction as Integer or null on error */
		public Integer getPvmtFriction() {
			return (friction != null) ? friction.getPercent() : null;
		}

		/** Get pavement sensor error or null if OK */
		public PavementSensorError getPavementSensorError() {
			PavementSensorError pse = sensor_error.getEnum();
			return (pse != null && pse.isError()) ? pse : null;
		}

		/** Get surface water depth formatted to meter units */
		private String getWaterDepth() {
			Distance d = convertDepthV1(water_depth);
			if (d != null) {
				Float mm = d.asFloat(METERS);
				return Num.format(mm, 3); // mm
			} else
				return null;
		}

		/** Get surface ice or water depth formatted to meter units */
		private String getIceOrWaterDepth() {
			// With an error condition, some Vaisala firmware
			// return 255 for essSurfaceIceOrWaterDepth instead of
			// 65535.  We must check for a sensor error first to
			// avoid erroneously returning 25.5 mm in that case.
			if (getPavementSensorError() != null)
				return null;
			Distance d = convertDepthV2(ice_or_water_depth);
			if (d != null) {
				Float mm = d.asFloat(METERS);
				return Num.format(mm, 4); // tenth of mm
			} else
				return getWaterDepth();
		}

		/** Get surface salinity in parts per million by weight */
		private Integer getSalinity() {
			return convertSalinity(salinity);
		}

		/** Get surface freeze temp or null on error */
		public Integer getFreezePointC() {
			return freeze_point.getTempC();
		}

		/** Get black ice signal or null on error */
		public SurfaceBlackIceSignal getBlackIceSignal() {
			SurfaceBlackIceSignal bis = black_ice_signal.getEnum();
			return (bis != null && bis.isValue()) ? bis : null;
		}

		/** Get surface conductivity (V2) as Integer or null on error */
		public Integer getSurfCondV2() {
			return (surface_conductivity_v2 != null)
					? surface_conductivity_v2.getInteger()
					: null;
		}

		/** Get JSON representation */
		private String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(Json.str("location", getSensorLocation()));
			sb.append(Json.str("pavement_type", getPavementType()));
			sb.append(height.toJson());
			sb.append(exposure.toJson());
			sb.append(Json.str("sensor_type",
				getPavementSensorType()));
			sb.append(Json.str("surface_status", getSurfStatus()));
			sb.append(surface_temp.toJson());
			sb.append(pavement_temp.toJson());
			sb.append(Json.str("sensor_error",
				getPavementSensorError()));
			sb.append(Json.num("ice_or_water_depth",
				getIceOrWaterDepth()));
			sb.append(Json.num("salinity", getSalinity()));
			sb.append(freeze_point.toJson());
			sb.append(Json.str("black_ice_signal",
				getBlackIceSignal()));
			sb.append(Json.num("surface_conductivity_v2",
					getSurfCondV2()));
			sb.append(friction.toJson());
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
