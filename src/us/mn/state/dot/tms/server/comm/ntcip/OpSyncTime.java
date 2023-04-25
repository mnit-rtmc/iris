/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1201.GlobalDaylightSaving;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201.globalDaylightSaving;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201.globalTime;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.Counter;

/**
 * Operation to synchronize the clock time.
 *
 * @author Douglas Lau
 */
public class OpSyncTime extends OpNtcip {

	/** Create new sync time operation */
	public OpSyncTime(DeviceImpl d) {
		super(PriorityLevel.POLL_LOW, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryGlobalTime();
	}

	/** Phase to query global time */
	private class QueryGlobalTime extends Phase {

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			Counter global_time = new Counter(globalTime.node);
			ASN1Enum<GlobalDaylightSaving> global_dst =
				new ASN1Enum<GlobalDaylightSaving>(
				GlobalDaylightSaving.class,
				globalDaylightSaving.node);
			mess.add(global_time);
			mess.add(global_dst);
			try {
				mess.queryProps();
				logQuery(global_time);
				logQuery(global_dst);
				return new SetGlobalTime();
			}
			catch (ControllerException e) {
				System.err.println("QueryGlobalTime: " +
					e.getMessage());
				return null;
			}
		}
	}

	/** Phase to set global time */
	private class SetGlobalTime extends Phase {

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			Counter global_time = new Counter(globalTime.node);
			long ms = TimeSteward.currentTimeMillis();
			global_time.setInteger((int) (ms / 1000));
			ASN1Enum<GlobalDaylightSaving> global_dst =
				new ASN1Enum<GlobalDaylightSaving>(
				GlobalDaylightSaving.class,
				globalDaylightSaving.node);
			global_dst.setEnum(GlobalDaylightSaving.disableDST);
			mess.add(global_time);
			mess.add(global_dst);
			try {
				logStore(global_time);
				logStore(global_dst);
				mess.storeProps();
			}
			catch (ControllerException e) {
				System.err.println("SetGlobalTime: " +
					e.getMessage());
			}
			return null;
		}
	}
}
