/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.OccupancySample;
import us.mn.state.dot.tms.server.PeriodicSample;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1202.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1202.MIB1202.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Object;

/**
 * This operation queries sample data.
 *
 * @author Douglas Lau
 */
public class OpQuerySamples extends OpController {

	/** NTCIP debug log */
	static private final DebugLog MIB1202_LOG = new DebugLog("mib1202");

	/** Vehicle count overflow code */
	static private final int VEH_OVERFLOW = 255;

	/** Maximum occupancy value (100%) */
	static private final int MAX_OCC = 200;

	/** Binning period */
	private final int per_sec;

	/** Create a new query samples object */
	public OpQuerySamples(ControllerImpl c, int p) {
		super(PriorityLevel.POLL_LOW, c);
		per_sec = p;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseOne() {
		return new QueryDetectors();
	}

	/** Phase to query the detectors */
	protected class QueryDetectors extends Phase {

		/** Query the detectors */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer max_det = maxVehicleDetectors.makeInt();
			ASN1Integer seq = volumeOccupancySequence.makeInt();
			ASN1Integer vo_per = volumeOccupancyPeriod.makeInt();
			ASN1Integer n_dets = activeVolumeOccupancyDetectors
			                    .makeInt();
			mess.add(max_det);
			mess.add(seq);
			mess.add(vo_per);
			mess.add(n_dets);
			mess.queryProps();
			logQuery(max_det);
			logQuery(seq);
			logQuery(vo_per);
			logQuery(n_dets);
			int n = n_dets.getInteger();
			int r = nextDetRow(n, 0);
			return (r <= n) ? new QueryVolOcc(n, r) : null;
		}
	}

	/** Query vehicle count and occupancy data */
	private class QueryVolOcc extends Phase {

		/** Sample timestamp */
		private final long stamp;

		/** Count of detectors */
		private final int n_dets;

		/** Current row in volumeOccupancyTable */
		private int row;

		/** Create a new phase to query vehicle count / occupancy */
		public QueryVolOcc(int n, int r) {
			stamp = TimeSteward.currentTimeMillis();
			n_dets = n;
			row = r;
		}

		/** Query one row of data */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer veh = detectorVolume.makeInt(row);
			ASN1Integer occ = detectorOccupancy.makeInt(row);
			mess.add(veh);
			mess.add(occ);
			mess.queryProps();
			logQuery(veh);
			logQuery(occ);
			storeVehOcc(veh, occ);
			row = nextDetRow(n_dets, row);
			return (row <= n_dets) ? this : null;
		}

		/** Store sample data */
		private void storeVehOcc(ASN1Integer veh, ASN1Integer occ) {
			DetectorImpl det = controller.getDetectorAtPin(row);
			if (det != null) {
				storeVehOcc(det, veh.getInteger(),
					occ.getInteger());
			}
		}

		/** Store sample data */
		private void storeVehOcc(DetectorImpl det, int veh, int occ) {
			if (veh < VEH_OVERFLOW) {
				det.storeVehCount(new PeriodicSample(stamp,
					per_sec, veh), false);
			}
			if (occ <= MAX_OCC) {
				det.storeOccupancy(new OccupancySample(stamp,
					per_sec, occ, MAX_OCC), false);
			}
			// FIXME: deal with detector fault codes
		}
	}

	/** Get the next assigned detector */
	private int nextDetRow(int n_dets, int row) {
		while (row < n_dets) {
			row++;
			if (controller.getDetectorAtPin(row) != null)
				return row;
		}
		return n_dets + 1;
	}

	/** Log a property query */
	protected void logQuery(ASN1Object prop) {
		if (MIB1202_LOG.isOpen())
			MIB1202_LOG.log(controller.getName() + ": " + prop);
	}
}
