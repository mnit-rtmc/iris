/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Iteris Inc.
 * Copyright (C) 2026  Alaska DOT&PF
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
package us.mn.state.dot.tms.server.comm.ntcip.mibvaisala;

import java.util.ArrayList;

import static us.mn.state.dot.tms.server.comm.ntcip.mibvaisala.MibVaisala.*;

import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PercentObject;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.SurfaceStatus;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.TemperatureObject;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;
import us.mn.state.dot.tms.units.Temperature;
import us.mn.state.dot.tms.utils.Json;
import us.mn.state.dot.tms.utils.SString;

/**
 * Table to hold Vaisala Spectro Table.
 * @author Michael Darter
 * @author Darren Jaeckel, Wostmann & Associates
 */
public class SpectroTable {

    /** Convert a ntcip ice or water layer thickness to double.
     * @param it Surface ice or water thickness in hundreths of millimeters
     * 	or 65535 indicating error.
     * @return Thickness in millimeters or null if missing or error */
    static private Double convertThickness(ASN1Integer it) {
        if (it != null) {
            int iit = it.getInteger();
            // valid range is 0-65535, where 65535 is error
            if (iit >= 0 && iit < 65535) {
                double dit = (double)iit / 100;
                return roundDouble(dit, 2);
            }
        }
        return null;
    }

    /** Table row */
    static public class Row {
        // row
        public final int row;
        public final DisplayString loc;		// Location
        public final PercentObject friction;	// surface friction index
        public final ASN1Integer ice_layer;	// surface ice layer
        public final ASN1Integer snow_layer;	// surface snow layer
        public final ASN1Integer water_layer;	// surface water layer
        public final TemperatureObject air_temp;	// air temp
        public final PercentObject rh;	// relative humidity
        public final TemperatureObject dewpoint;	// dewpoint temp
        public final TemperatureObject surface_temp;	// surface temperature
        public final ASN1Enum<SurfaceStatus> surface_status;	// surface status
        public final ASN1Enum<SpectroHwStatus> hardware_status;	// hardware status

        /** Row constructor */
        private Row(int num)
        {
            row = num;
            loc = new DisplayString(spectroLocation.node, row);
            friction = new PercentObject("friction", spectroSurfaceFrictionIndex.makeInt(row));
            ice_layer = spectroSurfaceIceLayer.makeInt(row);
            snow_layer = spectroSurfaceSnowLayer.makeInt(row);
            water_layer = spectroSurfaceWaterLayer.makeInt(row);
            air_temp = new TemperatureObject("air_temp", spectroAirTemperature.makeInt(row));
            rh = new PercentObject("rh", spectroRelativeHumidity.makeInt(row));
            dewpoint = new TemperatureObject("dewpoint", spectroDewpointTemp.makeInt(row));
            surface_temp = new TemperatureObject("surface_temp", spectroSurfaceTemperature.makeInt(row));
            surface_status = new ASN1Enum<>(SurfaceStatus.class, spectroSurfaceStatus.node, row);
            hardware_status = new ASN1Enum<>(SpectroHwStatus.class, spectroHardwareStatus.node, row);
        }
    }

    /** Number of sensors */
    public final ASN1Integer num_sensors = spectroTableNumSensors.makeInt();

    /** Table of rows, which maps row number (1-based) to row */
    private final ArrayList<Row> table_rows = new ArrayList< Row>();

    /** Clear table contents */
    public void clear() {
        num_sensors.setInteger(0);
        table_rows.clear();
    }

    /** Get number of rows in table reported by ESS */
    public int size() {
        return num_sensors.getInteger();
    }

    /** Check if all rows have been read */
    public boolean isDone() {
        return table_rows.size() >= size();
    }

    /** Add a row to the table. */
    public Row addRow()
    {
        Row tr = new Row(table_rows.size() + 1);
        table_rows.add(tr);
        return tr;
    }

    public Row getRow(int row) {
        return (row >= 1 && row <= table_rows.size())
                ? table_rows.get(row - 1)
                : null;
    }

    /** Get nth friction index as integer (ranges 0-100) or null */
    public Integer getFriction(int row) {
        Row tr = getRow(row);
        if (tr != null) {
            Integer ifr = tr.friction.getValue();
            // valid range is 0-101, where 101 is error
            if (ifr != null && ifr >= 0 && ifr <= 100) {
                return ifr;
            }
        }
        return null;
    }

    /** Get nth surface ice layer thickness for the specified row
     *  in hundreths of millimeters or null on error */
    private ASN1Integer getIceThicknessNtcip(int row) {
        Row tr = getRow(row);
        return (tr != null ? tr.ice_layer : null);
    }

    /** Get nth surface ice layer thickness in mm */
    public Double getIceThickness(int row) {
        return convertThickness(getIceThicknessNtcip(row));
    }

    /** Get nth surface snow layer thickness for the specified row
     *  in hundreths of millimeters or null on error */
    private ASN1Integer getSnowThicknessNtcip(int row) {
        Row tr = getRow(row);
        return (tr != null ? tr.snow_layer : null);
    }

    /** Get nth surface ice layer thickness in mm */
    public Double getSnowThickness(int row) {
        return convertThickness(getSnowThicknessNtcip(row));
    }

    /** Get nth surface ice layer thickness in hundreths of mm or null */
    public Integer getSnowThicknessInt(int row) {
        Double dst = getSnowThickness(row);
        return toHundreths(dst);
    }

    /** Get nth surface ice layer thickness in hundreths of mm or null */
    public Integer getIceThicknessInt(int row) {
        Double dit = getIceThickness(row);
        return toHundreths(dit);
    }

    /** Get nth surface water thickness for the specified row
     *  in hundreths of millimeters or null on error */
    private ASN1Integer getWaterThicknessNtcip(int row) {
        Row tr = getRow(row);
        return (tr != null ? tr.water_layer : null);
    }

    /** Get nth surface water layer thickness in mm */
    public Double getWaterThickness(int row) {
        return convertThickness(getWaterThicknessNtcip(row));
    }

    /** Get nth surface water layer thickness in hundreths of mm or null */
    public Integer getWaterThicknessInt(int row) {
        Double dwt = getWaterThickness(row);
        return toHundreths(dwt);
    }

    public Temperature getSurfaceTemp(int row) {
        Row tr = getRow(row);
        return (tr != null ? tr.surface_temp.getTemperature() : null);
    }

    /** Combined measurements match values reported by ScanWeb */
    public Integer getCombinedIceSnowWaterThicknessInt(int row) {
        Integer ice = getIceThicknessInt(row);
        Integer snow = getSnowThicknessInt(row);
        Integer water = getWaterThicknessInt(row);
        if (ice == null && snow == null && water == null) {
            return null;
        }
        return (ice != null ? ice : 0)
                + (snow != null ? snow : 0)
                + (water != null ? water : 0);
    }

    public Integer getHwStatus(int row) {
        Row tr = getRow(row);
        return (tr != null ? tr.hardware_status.getInteger() : null);
    }

    public static Integer toHundreths(Double val) {
        if (val != null) {
            return Math.toIntExact(Math.round(val * 100));
        }
        return null;
    }

    public static double roundDouble(double value, int numDecimals) {
        if (numDecimals >= 0) {
            double pow = Math.pow(10, numDecimals);
            return Math.round(value * pow) / pow;
        }
        return value;
    }

    /** Get nth hardware status as an enum */
    public SpectroHwStatus getHwStatusEnum(int row) {
        return SpectroHwStatus.fromVal(getHwStatus(row));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SpectroTable:");
        final int nrows = size();
        sb.append(" size=").append(nrows);
        for (int i = 1; i <= nrows; ++i) {
            sb.append(" friction=").
                    append(getFriction(i));
            sb.append(" ice_thickness_mm=").
                    append(getIceThickness(i));
            sb.append(" water_thickness_mm=").
                    append(getWaterThickness(i));
            sb.append(" hardware_status=").
                    append(getHwStatusEnum(i));
        }
        return sb.toString();
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        if (size() > 0) {
            sb.append("\"spectro_sensors\":[");
            for (int i = 1; i < size(); i++) {
                sb.append("{");
                sb.append(Json.str("sensor_index", i));
                sb.append(Json.num("friction", getFriction(i)));
                sb.append(Json.num("ice_thickness_100s_mm", getIceThicknessInt(i)));
                sb.append(Json.num("water_thickness_100s_mm", getWaterThicknessInt(i)));
                sb.append(Json.num("hw_status", getHwStatusEnum(i)));
                SString.removeTrailingComma(sb);
                sb.append("},");
            }
            SString.removeTrailingComma(sb);
            sb.append("],");
        }
        return sb.toString();
    }
}
