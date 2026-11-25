/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.server.comm.ntcip.mibssi;

import us.mn.state.dot.tms.server.comm.ntcip.mib1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.units.Temperature;
import us.mn.state.dot.tms.utils.Json;
import us.mn.state.dot.tms.utils.SString;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;

import static us.mn.state.dot.tms.units.Temperature.Units.CELSIUS;

/**
 * Temperature Data Probe sensors data table. Each table row
 * contains one temperature reading along the probe.
 * <p>
 * Unlike NTCIP subsurface table, this table is delivered as
 * an 85 length OCTET STRING.
 *
 * @author Darren Jaeckel, Wostmann & Associates
 */

public class SsiTdpTable {
    private static final int NUM_SENSORS_BYTE = 4;
    private static final int TDP_END_BYTE = 85;

    public final ASN1OctetString tdp_bytes = MIBSsi.subSurface.makeOctetString();

    /** Seconds since Unix Epoch UTC */
    private static Integer convertGlobalTime(byte[] tdp_bytes) {
        if (tdp_bytes.length >= NUM_SENSORS_BYTE) {
            byte[] time_buffer = Arrays.copyOfRange(tdp_bytes, 0, 4);
            return MIBSsi.unsignedBytesToInt(time_buffer);
        }
        return 0;
    }

    /** Get Unix Epoch UTC */
    public Integer getGlobalTime() {
        try {
            return convertGlobalTime(tdp_bytes.getOctetString());
        } catch (IOException ex) {
            // TDP table not found
        }
        return null;
    }

    protected static Integer convertNumberOfSensors(byte[] tdp_bytes) {
        if (tdp_bytes.length >= NUM_SENSORS_BYTE) {
            return MIBSsi.unsignedByteToInt(tdp_bytes[4]);
        }
        return 0;
    }

    /** Returns number of sub-surface sensors */
    public Integer getNumberOfSensors() {
        try {
            return convertNumberOfSensors(tdp_bytes.getOctetString());
        } catch (IOException ex) {
            // TDP table not found
        }
        return 0;
    }

    protected static TreeMap<Integer, Row> convertTdpTable(byte[] tdp_bytes) {
        final int temp_len = 5;
        TreeMap<Integer, Row> table_rows = new TreeMap<>();
        if (tdp_bytes.length >= TDP_END_BYTE) {
            int startIdx = 5;
            while (startIdx < TDP_END_BYTE) {
                Row row = new Row(Arrays.copyOfRange(tdp_bytes, startIdx, startIdx + temp_len));
                table_rows.put(row.sensor_index, row);
                startIdx += temp_len;
            }
        }
        return table_rows;
    }

    public TreeMap<Integer, Row> getTdpTable() {
        try {
            return convertTdpTable(tdp_bytes.getOctetString());
        } catch (IOException ex) {
            // TDP table not found
        }
        return new TreeMap<>();
    }

    static public class Row {
        public final Integer sensor_index;
        // The current sub-surface temperature in tenths of degrees Celsius. The value 1001 shall
        // indicate an error condition or missing value.
        public final Integer temp;
        // The sub-surface moisture expressed as a percentage (eg. 0 indicates dry, 100 indicates
        // saturated). The value 101 indicates an error condition or missing value
        public final Integer moisture;
        public final SubSurfaceSensorError sensor_error;

        private Row(byte[] buffer) {
            sensor_index = MIBSsi.unsignedByteToInt(buffer[0]);
            temp = MIBSsi.signedBytesToInt(buffer[1], buffer[2]);
            moisture = MIBSsi.unsignedByteToInt(buffer[3]);
            sensor_error = SubSurfaceSensorError.fromOrdinal(MIBSsi.unsignedByteToInt(buffer[4]));
        }

        public Temperature getTemperature() {
            return (temp != null) ? new Temperature(0.1 * (double) temp) : null;
        }

        /** Get value as degrees celsius */
        public Integer getTempC() {
            Temperature t = getTemperature();
            return (t != null) ? t.round(CELSIUS) : null;
        }

        public String getTempAsJson() {
            String val = (temp != null) ? Num.format((double) temp * 0.1, 1) : null;
            return Json.num("temp", val);
        }

        public SubSurfaceSensorError getSensorError() {
            return (sensor_error != null && sensor_error.isError()) ? sensor_error : null;
        }

        private String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append(Json.str("sensor_index", sensor_index));
            sb.append(getTempAsJson());
            sb.append(Json.num("moisture", moisture));
            sb.append(Json.str("sensor_error", getSensorError()));
            SString.removeTrailingComma(sb);
            sb.append("},");
            return sb.toString();
        }
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        TreeMap<Integer, Row> table_rows = getTdpTable();
        if (table_rows.size() > 0) {
            sb.append("\"temperature_data_probe\":[");
            for (SsiTdpTable.Row row : table_rows.values()) {
                sb.append(row.toJson());
            }
            SString.removeTrailingComma(sb);
            sb.append("],");
        }
        return sb.toString();
    }
}
