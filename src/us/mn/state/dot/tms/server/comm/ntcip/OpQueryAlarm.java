/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025-2026  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.server.AlarmImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Object;

/**
 * Operation to query an alarm.
 *
 * @author Douglas Lau
 */
public class OpQueryAlarm extends OpController {

	/** Alarm to query */
	private final AlarmImpl alarm;

	/** Number of digital ports */
	private final ASN1Integer num_ports_digital =
		maxAuxIOv2TableNumDigitalPorts.makeInt();

	/** Number of analog ports */
	private final ASN1Integer num_ports_analog =
		maxAuxIOv2TableNumAnalogPorts.makeInt();

	/** Log a property query */
	protected void logQuery(ASN1Object prop) {
		if (OpNtcip.NTCIP_LOG.isOpen())
			OpNtcip.NTCIP_LOG.log(alarm.getName() + ": " + prop);
	}

	/** Create a new query alarm operation */
	public OpQueryAlarm(AlarmImpl a, ControllerImpl c) {
		super(PriorityLevel.POLL_LOW, c);
		alarm = a;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase phaseOne() {
		return new QueryNumPorts();
	}

	/** Phase to query the number of aux ports */
	private class QueryNumPorts extends Phase {

		/** Query the number of supported fonts */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(num_ports_digital);
			mess.add(num_ports_analog);
			mess.queryProps();
			logQuery(num_ports_digital);
			logQuery(num_ports_analog);
			return nextAlarm(0);
		}
	}

	/** Get phase to query the next alarm */
	private Phase nextAlarm(int r) throws IOException {
		int n = num_ports_digital.getInteger() +
			num_ports_analog.getInteger();
		return (r < n) ? new QueryAlarmStatus(r + 1) : null;
	}

	/** Phase to query the alarm status */
	private class QueryAlarmStatus extends Phase {

		/** Row to query */
		private final int row;

		private final ASN1Integer p_type;
		private final ASN1Integer p_num;
		private final ASN1Integer p_value;

		/** Create a query alarm status phase */
		private QueryAlarmStatus(int r) throws IOException {
			row = r;
			p_type = auxIOv2PortType.makeInt(row);
			p_num = auxIOv2PortNumber.makeInt(row);
			p_value = auxIOv2PortValue.makeInt(row);
		}

		/** Query the alarm status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(p_type);
			mess.add(p_num);
			mess.add(p_value);
			mess.queryProps();
			logQuery(p_type);
			logQuery(p_num);
			logQuery(p_value);
			// Is this a digital alarm?
			if (p_type.getInteger() != 3)
				return nextAlarm(row);
			// Is this the right pin / port?
			int pin = 10 + p_num.getInteger();
			if (alarm.getPin() != pin)
				return nextAlarm(row);
			int state = p_value.getInteger();
			alarm.setStateNotify(state != 0);
			return null;
		}
	}
}
