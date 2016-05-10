/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2016  Minnesota Department of Transportation
 * Copyright (C) 2011-2012  University of Minnesota Duluth (NATSRL)
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
package us.mn.state.dot.tms.server;

import java.util.HashMap;
import java.util.Iterator;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.HOUR;
import static us.mn.state.dot.tms.server.Constants.FEET_PER_MILE;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import static us.mn.state.dot.tms.server.RampMeterImpl.filterRate;
import static us.mn.state.dot.tms.server.RampMeterImpl.getMaxRelease;
import us.mn.state.dot.tms.server.event.MeterEvent;

/**
 * Density-based Adaptive Metering Algorithm.
 *
 * @author Douglas Lau
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Soobin Jeon
 */
public class KAdaptiveAlgorithm implements MeterAlgorithmState {

	/** Enum for minimum limit control */
	enum MinimumRateLimit {
		passage_fail,
		storage_limit,
		wait_limit,
		target_min,
		backup_limit,
	};

	/** Algorithm debug log */
	static private final DebugLog ALG_LOG = new DebugLog("kadaptive");

	/** Number of seconds for one time step */
	static private final int STEP_SECONDS = 30;

	/** Calculate steps per hour */
	static private final double STEP_HOUR =
		new Interval(STEP_SECONDS).per(HOUR);

	/** Critical density (vehicles / mile) */
	static private final int K_CRIT = 37;

	/** Desired density (vehicles / mile) */
	static private final double K_DES = K_CRIT * 0.9;

	/** Low density (vehicles / mile) */
	static private final double K_LOW = K_CRIT * 0.75;

	/** Jam density (vehicles / mile) */
	static private final int K_JAM = 180;

	/** Ramp queue jam density (vehicles / mile) */
	static private final int K_JAM_RAMP = 140;

	/** Ramp queue jam density (vehicles per foot) */
	static private final float JAM_VPF = (float) K_JAM_RAMP / FEET_PER_MILE;

	/** Seconds to average segment density for start metering check */
	static private final int START_SECS = 120;

	/** Seconds to average segment density for stop metering check */
	static private final int STOP_SECS = 600;

	/** Seconds to average segment density for restart metering check */
	static private final int RESTART_SECS = 300;

	/** Maximum number of time steps needed for sample history */
	static private final int MAX_STEPS = steps(Math.max(Math.max(START_SECS,
		STOP_SECS), RESTART_SECS));

	/** Minutes before end of period to disallow early metering */
	static private final int EARLY_METER_END_MINUTES = 30;

	/** Minutes to flush meter before stop metering */
	static private final int FLUSH_MINUTES = 2;

	/** Distance threshold for upstream station to meter association */
	static private final float UPSTREAM_STATION_MILES = 1.0f;

	/** Distance threshold for downstream station to meter association */
	static private final int DOWNSTREAM_STATION_FEET = 500;

	/** Maximum segment length */
	static private final float SEGMENT_LENGTH_MILES = 3.0f;

	/** Number of seconds to store demand accumulator history */
	static private final int DEMAND_ACCUM_SECS = 600;

	/** Queue occupancy override threshold */
	static private final int QUEUE_OCC_THRESHOLD = 25;

	/** Number of seconds queue must be empty before resetting green */
	static private final int QUEUE_EMPTY_RESET_SECS = 60;

	/** Threshold to determine when queue is empty */
	static private final int QUEUE_EMPTY_THRESHOLD = -5;

	/** Ratio for max rate to target rate */
	static private final float TARGET_MAX_RATIO = 1.25f;

	/** Ratio for max rate to target rate while flushing */
	static private final float TARGET_MAX_RATIO_FLUSHING = 1.5f;

	/** Ratio for min rate to target rate */
	static private final float TARGET_MIN_RATIO = 0.75f;

	/** Base percentage for backup minimum limit */
	static private final float BACKUP_LIMIT_BASE = 0.5f;

	/** Ratio for target waiting time to max wait time */
	static private final float WAIT_TARGET_RATIO = 0.75f;

	/** Ratio for target storage to max storage */
	static private final float STORAGE_TARGET_RATIO = 0.75f;

	/** Calculate the number of steps for an interval */
	static private int steps(int seconds) {
		float secs = seconds;
		return Math.round(secs / STEP_SECONDS);
	}

	/** Convert step volume count to flow rate.
	 * @param vol Volume to convert (number of vehicles).
	 * @param n_steps Number of time steps of volume.
	 * @return Flow rate (vehicles / hour) */
	static private int flowRate(float vol, int n_steps) {
		if (vol >= 0) {
			Interval period = new Interval(n_steps * STEP_SECONDS);
			float hour_frac = period.per(HOUR);
			return Math.round(vol * hour_frac);
		} else
			return MISSING_DATA;
	}

	/** Convert single step volume count to flow rate.
	 * @param vol Volume to convert (number of vehicles)
	 * @return Flow rate (vehicles / hour), or null for missing data. */
	static private Double flowRate(float vol) {
		return (vol >= 0) ? (vol * STEP_HOUR) : null;
	}

	/** Convert flow rate to volume for a given period.
	 * @param flow Flow rate to convert (vehicles / hour).
	 * @param period Period for volume (seconds).
	 * @return Volume over given period. */
	static private float volumePeriod(int flow, int period) {
		if (flow >= 0 && period > 0) {
			float hour_frac = HOUR.per(new Interval(period));
			return flow * hour_frac;
		} else
			return MISSING_DATA;
	}

	/** Check if density is below "low" threshold */
	static private boolean isDensityLow(Double k) {
		return (k != null) && (k < K_LOW);
	}

	/** States for all K adaptive algorithms */
	static private HashMap<String, KAdaptiveAlgorithm> ALL_ALGS =
		new HashMap<String, KAdaptiveAlgorithm>();

	/** Get the K adaptive algorithm state for a meter */
	static public KAdaptiveAlgorithm meterState(RampMeterImpl meter) {
		Corridor c = meter.getCorridor();
		if (c != null) {
			KAdaptiveAlgorithm alg = lookupAlgorithm(c);
			if (alg.createMeterState(meter))
				return alg;
		}
		return null;
	}

	/** Lookup an algorithm for a corridor */
	static private KAdaptiveAlgorithm lookupAlgorithm(Corridor c) {
		KAdaptiveAlgorithm alg = ALL_ALGS.get(c.getID());
		if (alg == null) {
			alg = new KAdaptiveAlgorithm(c);
			alg.log("adding");
			ALL_ALGS.put(c.getID(), alg);
		}
		return alg;
	}

	/** Process one interval for all K adaptive algorithm states */
	static public void processAllStates() {
		Iterator<KAdaptiveAlgorithm> it =
			ALL_ALGS.values().iterator();
		while (it.hasNext()) {
			KAdaptiveAlgorithm alg = it.next();
			alg.updateStations();
			if (alg.isDone()) {
				alg.log("isDone: removing");
				it.remove();
			}
		}
	}

	/** Metering corridor */
	private final Corridor corridor;

	/** Hash map of ramp meter states */
	private final HashMap<String, MeterState> meter_states =
		new HashMap<String, MeterState>();

	/** Head (furthest upstream) node on corridor */
	private final Node head;

	/** Tail (furthest downstream) node on corridor */
	private final Node tail;

	/** Create a new KAdaptiveAlgorithm */
	private KAdaptiveAlgorithm(Corridor c) {
		corridor = c;
		head = createNodes();
		tail = head.tailNode();
		debug();
	}

	/** Create nodes from corridor structure */
	private Node createNodes() {
		Node first = null;
		Node prev = null;
		Iterator<R_NodeImpl> itr = corridor.iterator();
		while (itr.hasNext()) {
			R_NodeImpl rnode = itr.next();
			Node n = createNode(rnode, prev);
			if (n != null)
				prev = n;
			if (first == null)
				first = prev;
		}
		return first;
	}

	/** Create one node */
	private Node createNode(R_NodeImpl rnode, Node prev) {
		Float mile = corridor.getMilePoint(rnode);
		return (mile != null) ? createNode(rnode, mile, prev) : null;
	}

	/** Create one node */
	private Node createNode(R_NodeImpl rnode, float mile, Node prev) {
		switch (R_NodeType.fromOrdinal(rnode.getNodeType())) {
		case ENTRANCE:
			return new EntranceNode(rnode, mile, prev);
		case STATION:
			StationImpl stat = rnode.getStation();
			if (stat != null && stat.getActive())
				return new StationNode(rnode, mile, prev, stat);
		default:
			return null;
		}
	}

	/** Debug corridor structure */
	private void debug() {
		log("-------- Corridor Structure --------");
		for (Node n = head; n != null; n = n.downstream)
			log(n.toString());
	}

	/** Log one message */
	private void log(String msg) {
		if (ALG_LOG.isOpen())
			ALG_LOG.log(corridor.getName() + ": " + msg);
	}

	/** Validate algorithm state for a meter */
	@Override
	public void validate(RampMeterImpl meter) {
		MeterState ms = getMeterState(meter);
		if (ms != null) {
			ms.validate();
			if (ALG_LOG.isOpen())
				log(ms.toString());
			if (MeterEvent.getMeterEventPurgeDays() > 0)
				ms.logMeterEvent();
		}
	}

	/** Get ramp meter queue state enum value */
	@Override
	public RampMeterQueue getQueueState(RampMeterImpl meter) {
		MeterState ms = getMeterState(meter);
		return (ms != null)
		      ? ms.getQueueState()
		      : RampMeterQueue.UNKNOWN;
	}

	/** Get the meter state for a given ramp meter */
	private MeterState getMeterState(RampMeterImpl meter) {
		if (meter.getCorridor() == corridor)
			return meter_states.get(meter.getName());
		else {
			// Meter must have been changed to a different
			// corridor; throw away old meter state
			meter_states.remove(meter.getName());
			return null;
		}
	}

	/** Create the meter state for a given ramp meter */
	private boolean createMeterState(RampMeterImpl meter) {
		EntranceNode en = findEntranceNode(meter);
		if (en != null) {
			MeterState ms = new MeterState(meter, en);
			meter_states.put(meter.getName(), ms);
			return true;
		} else
			return false;
	}

	/** Find an entrance node matching the given ramp meter.
	 * @param meter Ramp meter to search for.
	 * @return Entrance node matching ramp meter. */
	private EntranceNode findEntranceNode(RampMeterImpl meter) {
		R_NodeImpl rnode = meter.getEntranceNode();
		for (Node n = head; n != null; n = n.downstream) {
			if (n instanceof EntranceNode) {
				EntranceNode en = (EntranceNode)n;
				if (en.rnode.equals(rnode))
					return en;
			}
		}
		return null;
	}

	/** Update the station nodes for the current interval */
	private void updateStations() {
		for (StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			sn.updateState();
		}
	}

	/** Get the furthest upstream station node. */
	private StationNode firstStation() {
		for (Node n = head; n != null; n = n.downstream) {
			if (n instanceof StationNode)
				return (StationNode) n;
		}
		return null;
	}

	/** Get the furthest downstream station node. */
	private StationNode lastStation() {
		for (Node n = tail; n != null; n = n.upstream) {
			if (n instanceof StationNode)
				return (StationNode) n;
		}
		return null;
	}

	/** Is this KAdaptiveAlgorithm done? */
	private boolean isDone() {
		for (MeterState ms : meter_states.values()) {
			if (ms.meter.isOperating())
				return false;
		}
		return true;
	}

	/** Node to manage station or entrance */
	abstract protected class Node {

		/** R_Node reference */
		protected final R_NodeImpl rnode;

		/** Mile point of the node */
		protected final float mile;

		/** Link to upstream node */
		protected final Node upstream;

		/** Link to downstream node */
		protected Node downstream;

		/** Create a new node */
		protected Node(R_NodeImpl n, float m, Node up) {
			rnode = n;
			mile = m;
			if (up != null)
				up.downstream = this;
			upstream = up;
			downstream = null;
		}

		/** Get the distance to another node (in miles) */
		protected float distanceMiles(Node other) {
			return Math.abs(mile - other.mile);
		}

		/** Get the distancee to another node (in feet) */
		protected int distanceFeet(Node other) {
			return Math.round(distanceMiles(other) * FEET_PER_MILE);
		}

		/** Get the tail of a node list */
		protected Node tailNode() {
			Node n = this;
			while (n.downstream != null)
				n = n.downstream;
			return n;
		}

		/** Find next upstream station node.
		 * @return Upstream station node. */
		protected StationNode upstreamStation() {
			for (Node n = upstream; n != null; n = n.upstream) {
				if (n instanceof StationNode)
					return (StationNode) n;
			}
			return null;
		}

		/** Find next downstream station node.
		 * @return Downstream station node. */
		protected StationNode downstreamStation() {
			for (Node n = downstream; n != null; n = n.downstream) {
				if (n instanceof StationNode)
					return (StationNode) n;
			}
			return null;
		}
	}

	/** Node to manage station on corridor */
	protected class StationNode extends Node {

		/** StationImpl mapping this state */
		private final StationImpl station;

		/** Density history */
		private final BoundedSampleHistory density_hist =
			new BoundedSampleHistory(steps(60));

		/** Speed history */
		private final BoundedSampleHistory speed_hist =
			new BoundedSampleHistory(steps(60));

		/** Create a new station node. */
		public StationNode(R_NodeImpl rnode, float m, Node up,
			StationImpl stat)
		{
			super(rnode, m, up);
			station = stat;
		}

		/** Update station state */
		private void updateState() {
			density_hist.push(getStationDensity());
			speed_hist.push(getStationSpeed());
		}

		/** Get the current station density */
		private Double getStationDensity() {
			float d = station.getDensity();
			return (d >= 0) ? (double) d : null;
		}

		/** Get the current station speed */
		private Double getStationSpeed() {
			float s = station.getSpeed();
			return (s >= 0) ? (double) s : null;
		}

		/** Get average density of a mainline segment beginning at the
		 * current station.  This works by splitting each consecutive
		 * pair of stations into 3 equal links and assigning average
		 * density to the middle link.  All links are then averaged,
		 * weighted by length.
		 *
		 * @param dn Segment downstream station node.
		 * @return average density (distance weight). */
		private double calculateSegmentDensity(final StationNode dn) {
			StationNode cursor = this;
			double dist_seg = 0;	/* Segment distance */
			double veh_seg = 0;	/* Sum of vehicles in segment */
			double k_cursor = cursor.getDensity();
			for (StationNode sn = cursor.downstreamStation();
			     sn != null && cursor != dn;
			     sn = sn.downstreamStation())
			{
				double k_down = sn.getDensity();
				double k_middle = (k_cursor + k_down) / 2;
				double dist = cursor.distanceMiles(sn);
				dist_seg += dist;
				veh_seg += (k_cursor + k_middle + k_down) / 3 *
					dist;
				cursor = sn;
				k_cursor = k_down;
			}
			if (dist_seg > 0)
				return veh_seg / dist_seg;
			else
				return k_cursor;
		}

		/** Get 1 minute density at current time step.
		 * @return average 1 min density; missing data returns 0. */
		public double getDensity() {
			Double avg = density_hist.average(0, steps(60));
			return (avg != null) ? avg : 0;
		}

		/** Get 1 minute speed at current time step.
		 * @return Average 1 min speed; missing data returns 0. */
		private double getSpeed() {
			Double avg = speed_hist.average(0, steps(60));
			return (avg != null) ? avg : 0;
		}

		/** Find downstream segment station node.  This is the station
		 * downstream which results in the highest segment density.
		 * @return Downstream segment station node. */
		protected StationNode segmentStationNode() {
			StationNode dn = this;
			double dk = 0;
			for (StationNode sn = this; sn != null;
			     sn = sn.downstreamStation())
			{
				if (distanceMiles(sn) > SEGMENT_LENGTH_MILES)
					break;
				double k = calculateSegmentDensity(sn);
				if (k >= dk) {
					dk = k;
					dn = sn;
				}
			}
			return dn;
		}

		/** Get a string representation of a station node */
		@Override
		public String toString() {
			return "SN:" + station.getName();
		}
	}

	/** Node to manage entrance onto corridor */
	class EntranceNode extends Node {

		/** Create a new entrance node */
		public EntranceNode(R_NodeImpl rnode, float m, Node prev) {
			super(rnode, m, prev);
		}

		/** Get a string representation of an entrance node */
		@Override
		public String toString() {
			return "EN:" + rnode.getName();
		}
	}

	/** Enum for metering phase */
	private enum MeteringPhase {
		not_started,
		metering,
		flushing,
		stopped,
	};

	/** Ramp meter state */
	class MeterState {

		/** Meter at this entrance */
		private final RampMeterImpl meter;

		/** Entrance node for the meter */
		private final EntranceNode node;

		/** Station node association */
		private final StationNode s_node;

		/** Queue sampler set */
		private final SamplerSet queue;

		/** Passage sampler set */
		private final SamplerSet passage;

		/** Merge sampler set */
		private final SamplerSet merge;

		/** Bypass sampler set */
		private final SamplerSet bypass;

		/** Green count sampler set */
		private final SamplerSet green;

		/** Metering phase */
		private MeteringPhase phase = MeteringPhase.not_started;

		/** Is the meter currently metering? */
		private boolean isMetering() {
			return phase != MeteringPhase.not_started &&
			       phase != MeteringPhase.stopped;
		}

		/** Minimum metering rate (vehicles / hour) */
		private int min_rate = 0;

		/** Current metering rate (vehicles / hour) */
		private int release_rate = 0;

		/** Maximum metering rate (vehicles / hour) */
		private int max_rate = 0;

		/** Queue demand history (vehicles / hour) */
		private final BoundedSampleHistory demand_hist =
			new BoundedSampleHistory(steps(300));

		/** Cumulative demand history (vehicles) */
		private final BoundedSampleHistory demand_accum_hist =
			new BoundedSampleHistory(steps(DEMAND_ACCUM_SECS));

		/** Cumulative demand count (vehicles) */
		private float demand_accum = 0;

		/** Demand adjustment (vehicles) */
		private float demand_adj = 0;

		/** Tracking queue demand rate (vehicles / hour) */
		private int tracking_demand = 0;

		/** Passage sampling good (latches until queue empty) */
		private boolean passage_good = true;

		/** Cumulative passage count (vehicles) */
		private int passage_accum = 0;

		/** Ramp passage history (vehicles / hour) */
		private final BoundedSampleHistory passage_hist =
			new BoundedSampleHistory(MAX_STEPS);

		/** Cumulative green count (vehicles) */
		private int green_accum = 0;

		/** Time queue has been empty (seconds) */
		private int queue_empty_secs = 0;

		/** Time queue has been backed-up (seconds) */
		private int queue_backup_secs = 0;

		/** Total occupancy for duration of a queue backup */
		private int backup_occ = 0;

		/** Controlling minimum rate limit */
		private MinimumRateLimit limit_control =
			MinimumRateLimit.target_min;

		/** Segment density history (vehicles / mile) */
		private final BoundedSampleHistory segment_k_hist =
			new BoundedSampleHistory(MAX_STEPS);

		/** Create a new meter state */
		public MeterState(RampMeterImpl mtr, EntranceNode en) {
			meter = mtr;
			node = en;
			SamplerSet ss = meter.getSamplerSet();
			queue = new SamplerSet(ss.filter(LaneType.QUEUE));
			passage = new SamplerSet(ss.filter(LaneType.PASSAGE));
			merge = new SamplerSet(ss.filter(LaneType.MERGE));
			bypass = new SamplerSet(ss.filter(LaneType.BYPASS));
			green = new SamplerSet(ss.filter(LaneType.GREEN));
			s_node = getAssociatedStation();
		}

		/** Get station to associate with the meter state.
		 * @return Associated station node, or null. */
		private StationNode getAssociatedStation() {
			StationNode us = getAssociatedUpstream();
			StationNode ds = node.downstreamStation();
			return useDownstream(us, ds) ? ds : us;
		}

		/** Get associated upstream station.
		 * @return Station node upstream of meter, or null. */
		private StationNode getAssociatedUpstream() {
			StationNode us = node.upstreamStation();
			return isUpstreamStationOk(us) ? us : null;
		}

		/** Check if an upstream station is OK.
		 * @param us Station just upstream of meter.
		 * @return true if upstream station is suitable. */
		private boolean isUpstreamStationOk(StationNode us) {
			return us != null &&
			       node.distanceMiles(us) < UPSTREAM_STATION_MILES;
		}

		/** Test if downstream station should be associated.
		 * @param us Station just upstream of meter.
		 * @param ds Station just downstream of meter.
		 * @return true if downstream station should be associated. */
		private boolean useDownstream(StationNode us, StationNode ds) {
			if (us == null)
				return true;
			if (ds == null)
				return false;
			int uf = node.distanceFeet(us);
			int df = node.distanceFeet(ds);
			return df < DOWNSTREAM_STATION_FEET && df < uf;
		}

		/** Get the total cumulative demand (vehicles).
		 * @param step Time step in past (0 for current).
		 * @return Cumulative demand at specified time. */
		private float cumulativeDemand(int step) {
			Double d = demand_accum_hist.get(step);
			return (d != null) ? d.floatValue() : 0;
		}

		/** Validate meter state.
		 *   - Update state timers.
		 *   - Update passage flow and accumulator.
		 *   - Update demand flow and accumulator.
		 *   - Calculate metering rate. */
		private void validate() {
			// NOTE: these must happen in proper order
			checkQueueBackedUp();
			checkQueueEmpty();
			updatePassageState();
			updateDemandState();
			min_rate = filterRate(calculateMinimumRate());
			max_rate = filterRate(calculateMaximumRate());
			if (s_node != null)
				calculateMeteringRate();
		}

		/** Check the queue backed-up state */
		private void checkQueueBackedUp() {
			if (isQueueOccupancyHigh()) {
				queue_backup_secs += STEP_SECONDS;
				backup_occ += queue.getMaxOccupancy();
			} else {
				queue_backup_secs = 0;
				backup_occ = 0;
			}
		}

		/** Check if queue is empty */
		private void checkQueueEmpty() {
			if (isQueuePossiblyEmpty())
				queue_empty_secs += STEP_SECONDS;
			else
				queue_empty_secs = 0;
			// Get rid of unused greens
			if (queue_empty_secs > QUEUE_EMPTY_RESET_SECS &&
			    isPassageBelowGreen())
				green_accum = passage_accum;
		}

		/** Update ramp passage output state */
		private void updatePassageState() {
			int passage_vol = calculatePassageCount();
			passage_hist.push(flowRate(passage_vol));
			if (passage_vol >= 0)
				passage_accum += passage_vol;
			else
				passage_good = false;
			int green_vol = green.getCount();
			if (green_vol > 0)
				green_accum += green_vol;
		}

		/** Calculate passage count (vehicles).
		 * @return Passage vehicle count */
		private int calculatePassageCount() {
			int vol = passage.getCount();
			if (vol >= 0)
				return vol;
			vol = merge.getCount();
			if (vol >= 0) {
				int b = bypass.getCount();
				if (b > 0) {
					vol -= b;
					if (vol < 0)
						return 0;
				}
				return vol;
			}
			return MISSING_DATA;
		}

		/** Update ramp queue demand state */
		private void updateDemandState() {
			float dem_vol = queueDemandVolume();
			float da = demand_accum;
			// Calculate demand without adjustment
			demand_accum += dem_vol;
			demand_adj = calculateDemandAdjustment();
			float adjusted_dem = Math.max(dem_vol + demand_adj, 0);
			demand_hist.push(flowRate(adjusted_dem));
			// Recalculate demand with adjustment
			demand_accum = da + adjusted_dem;
			demand_accum_hist.push((double) demand_accum);
			tracking_demand = trackingDemand();
		}

		/** Get queue demand volume for the current period */
		private float queueDemandVolume() {
			float vol = queue.getCount();
			if (vol >= 0)
				return vol;
			else {
				int target = getDefaultTarget();
				return volumePeriod(target, STEP_SECONDS);
			}
		}

		/** Calculate the demand adjustment.
		 * @return Demand adjustment (number of vehicles) */
		private float calculateDemandAdjustment() {
			return estimateDemandUndercount()
			     - estimateDemandOvercount();
		}

		/** Estimate demand undercount when occupancy is high.
		 * @return Demand undercount (may be negative). */
		private float estimateDemandUndercount() {
			return queueFullRatio() * availableStorage();
		}

		/** Estimate the queue full ratio.
		 * @return Ratio from 0 to 1. */
		private float queueFullRatio() {
			float qor = queueOccRatio();
			float qbr = queueRatio(queue_backup_secs);
			return Math.max(qor, qbr);
		}

		/** Get queue occupancy ratio.  Map occupancy values between
		 * QUEUE_OCC_THRESHOLD and 100% to a range of 0 and 1.
		 * @return Ratio from 0 to 1. */
		private float queueOccRatio() {
			float o = queue.getMaxOccupancy() - QUEUE_OCC_THRESHOLD;
			return (o > 0)
			     ? Math.min(o / (100 - QUEUE_OCC_THRESHOLD), 1)
			     : 0;
		}

		/** Calculate a queue ratio.
		 * @param secs Number of seconds.
		 * @return Ratio compared to max wait time, between 0 and 1. */
		private float queueRatio(int secs) {
			return Math.min(2 * secs / maxWaitTime(), 1);
		}

		/** Estimate the available storage in queue.
		 * @return Available storage (vehicles, may be negative). */
		private float availableStorage() {
			if (passage_good) {
				float q_len = Math.max(queueLength(), 0);
				return maxStorage() - q_len;
			} else
				return maxStorage() / 3;
		}

		/** Estimate demand overcount when queue is empty.
		 * @return Vehicle overcount at queue detector (may be
		 *         negative). */
		private float estimateDemandOvercount() {
			return queueRatio(queue_empty_secs) * queueLength();
		}

		/** Estimate the length of queue (vehicles).
		 * @return Queue length (may be negative). */
		private float queueLength() {
			return (passage_good)
			     ? (demand_accum - passage_accum)
			     : 0;
		}

		/** Calculate tracking demand rate at queue detector.
		 * @return Tracking demand flow rate (vehicles / hour) */
		private int trackingDemand() {
			Double d = demand_hist.average();
			return (d != null)
			      ?	(int) Math.round(d)
			      : getDefaultTarget();
		}

		/** Get the default target metering rate (vehicles / hour) */
		private int getDefaultTarget() {
			int t = meter.getTarget();
			return (t > 0) ? t : getMaxRelease();
		}

		/** Check if the queue is possibly empty */
		private boolean isQueuePossiblyEmpty() {
			return isQueueFlowLow() && !isQueueOccupancyHigh();
		}

		/** Check if the queue flow is low.  If the passage detector
		 * is not good, assume this is false. */
		private boolean isQueueFlowLow() {
			return (passage_good)
			     ? (isDemandBelowPassage() || isPassageBelowGreen())
			     : false;
		}

		/** Check if cumulative demand is below cumulative passage */
		private boolean isDemandBelowPassage() {
			return queueLength() < QUEUE_EMPTY_THRESHOLD;
		}

		/** Check if queue occupancy is above threshold */
		private boolean isQueueOccupancyHigh() {
			return queue.getMaxOccupancy() > QUEUE_OCC_THRESHOLD;
		}

		/** Check if cumulative passage is below cumulative green */
		private boolean isPassageBelowGreen() {
			return violationCount() < QUEUE_EMPTY_THRESHOLD;
		}

		/** Calculate violation count (passage above green count) */
		private int violationCount() {
			return (passage_good)
			     ? (passage_accum - green_accum)
			     : 0;
		}

		/** Reset the demand / passage accumulators */
		private void resetAccumulators() {
			demand_accum = 0;
			demand_accum_hist.clear();
			demand_adj = 0;
			passage_good = true;
			passage_accum = 0;
			green_accum = 0;
		}

		/** Get ramp meter queue state enum value */
		private RampMeterQueue getQueueState() {
			if (isMetering()) {
				if (isQueueFull())
					return RampMeterQueue.FULL;
				else if (!passage_good)
					return RampMeterQueue.UNKNOWN;
				else if (isQueueEmpty())
					return RampMeterQueue.EMPTY;
				else
					return RampMeterQueue.EXISTS;
			}
			return RampMeterQueue.UNKNOWN;
		}

		/** Check if the ramp meter queue is full */
		private boolean isQueueFull() {
			return isQueueOccupancyHigh() ||
			      (isQueueLimitFull() && !isPassageBelowGreen());
		}

		/** Check if the meter queue is full (by storage/wait limit) */
		private boolean isQueueLimitFull() {
			return queue.isPerfect() &&
			       passage_good &&
			      (isQueueStorageFull() ||
			       isQueueWaitAboveTarget());
		}

		/** Check if the ramp queue storage is full */
		private boolean isQueueStorageFull() {
			return queueLength() >= targetStorage();
		}

		/** Check if the ramp queue wait time is above target */
		private boolean isQueueWaitAboveTarget() {
			assert passage_good;
			int wait_target = targetWaitTime();
			int wait_steps = steps(wait_target);
			int dem = Math.round(cumulativeDemand(wait_steps));
			return dem > passage_accum;
		}

		/** Check if the meter queue is empty */
		private boolean isQueueEmpty() {
			return queueLength() < 1;
		}

		/** Calculate minimum rate (vehicles / hour) */
		private int calculateMinimumRate() {
			if (passage_good) {
				limit_control = MinimumRateLimit.target_min;
				return calculateMinimumRate(targetMinRate());
			} else {
				limit_control = MinimumRateLimit.passage_fail;
				return tracking_demand;
			}
		}

		/** Calculate minimum rate (vehicles / hour) */
		private int calculateMinimumRate(int r) {
			int qsl = queueStorageLimit();
			if (qsl > r) {
				r = qsl;
				limit_control = MinimumRateLimit.storage_limit;
			}
			int qwl = queueWaitLimit();
			if (qwl > r) {
				r = qwl;
				limit_control = MinimumRateLimit.wait_limit;
			}
			int bml = backupMinLimit();
			if (bml > r) {
				r = bml;
				limit_control = MinimumRateLimit.backup_limit;
			}
			return r;
		}

		/** Caculate queue storage limit.  Project into the future the
		 * duration of the target wait time.  Using the target demand,
		 * estimate the cumulative demand at that point in time.  From
		 * there, subtract the target ramp storage volume to find the
		 * required cumulative passage volume at that time.
		 * @return Queue storage limit (vehicles / hour). */
		private int queueStorageLimit() {
			assert passage_good;
			float proj_arrive = volumePeriod(tracking_demand,
				targetWaitTime());
			float demand_proj = demand_accum + proj_arrive;
			int req = Math.round(demand_proj - targetStorage());
			int pass_min = req - passage_accum;
			return flowRate(pass_min, steps(targetWaitTime()));
		}

		/** Calculate the target storage on the ramp (vehicles) */
		private float targetStorage() {
			return maxStorage() * STORAGE_TARGET_RATIO;
		}

		/** Calculate the maximum storage on the ramp (vehicles) */
		private float maxStorage() {
			int stor_ft = meter.getStorage() * meter.getLaneCount();
			return stor_ft * JAM_VPF;
		}

		/** Calculate queue wait limit (minimum rate).
		 * @return Queue wait limit (vehicles / hour) */
		private int queueWaitLimit() {
			assert passage_good;
			int wait_limit = 0;
			int wait_target = targetWaitTime();
			int wait_steps = steps(wait_target);
			for (int i = 1; i <= wait_steps; i++) {
				int dem = Math.round(cumulativeDemand(
					wait_steps - i));
				int pass_min = dem - passage_accum;
				int limit = flowRate(pass_min, i);
				wait_limit = Math.max(limit, wait_limit);
			}
			return wait_limit;
		}

		/** Get the target wait time (seconds) */
		private int targetWaitTime() {
			return Math.round(maxWaitTime() * WAIT_TARGET_RATIO);
		}

		/** Get the max wait time (seconds) */
		private float maxWaitTime() {
			return Math.max(meter.getMaxWait(), 1);
		}

		/** Estimate the wait time for vehicle at head of queue */
		private int estimateWaitSecs() {
			if (!passage_good)
				return 0;
			float pd = demand_accum;
			if (pd <= passage_accum)
				return 0;
			for (int i = 1; i < steps(DEMAND_ACCUM_SECS); i++) {
				float dem = cumulativeDemand(i);
				if (dem <= passage_accum && pd > dem) {
					float p = passage_accum - dem;
					float r = p / (pd - dem);
					assert r >= 0 && r <= 1;
					// Estimate wait time (in steps)
					float wait = (i - 1) + r;
					return Math.round(wait * STEP_SECONDS);
				}
				pd = dem;
			}
			return DEMAND_ACCUM_SECS;
		}

		/** Calculate target minimum rate.
		 * @return Target minimum rate (vehicles / hour). */
		private int targetMinRate() {
			return Math.round(tracking_demand * TARGET_MIN_RATIO);
		}

		/** Calculate backup minimum limit.
		 * @return Backup minimum limit (vehicles / hour). */
		private int backupMinLimit() {
			// NOTE: The following is the proper calculation:
			//    occ_avg = backup_occ / steps(queue_backup_secs)
			//    backup_mins = queue_backup_secs / 60.0f
			//    ratio = 50 + occ_avg * backup_mins
			// This can be simplified to this:
			//    ratio = 50 + backup_occ * STEP_SECONDS *
			//        queue_backup_secs / (queue_backup_secs * 60)
			// Which can be further simplified to:
			//    ratio = 50 + backup_occ * STEP_SECONDS / 60
			float ratio = BACKUP_LIMIT_BASE + backup_occ *
				STEP_SECONDS / (60.0f * 100.0f);
			return Math.round(tracking_demand * ratio);
		}

		/** Get the target maximum rate ratio for current phase */
		private float targetMaxRatio() {
			switch (phase) {
			case flushing:
			     return TARGET_MAX_RATIO_FLUSHING;
			default:
			     return TARGET_MAX_RATIO;
			}
		}

		/** Calculate target maximum rate.
		 * @return Target maxumum rate (vehicles / hour). */
		private int calculateMaximumRate() {
			int target_max = Math.round(tracking_demand *
				targetMaxRatio());
			return Math.max(target_max, min_rate);
		}

		/** Calculate the metering rate */
		private void calculateMeteringRate() {
			assert s_node != null;
			StationNode dn = s_node.segmentStationNode();
			double k = s_node.calculateSegmentDensity(dn);
			segment_k_hist.push(k);
			phase = checkMeterPhase();
			if (isMetering())
				setRate(calculateRate(k));
		}

		/** Check metering phase transitions.
		 * @return New metering phase. */
		private MeteringPhase checkMeterPhase() {
			switch (phase) {
			case not_started:
				return checkStart();
			case metering:
				return checkContinueMetering();
			case flushing:
				return checkContinueFlushing();
			case stopped:
				return checkRestart();
			default:
				return MeteringPhase.stopped;
			}
		}

		/** Check if metering should start.
		 * @return New metering phase. */
		private MeteringPhase checkStart() {
			if (shouldStart()) {
				resetAccumulators();
				return MeteringPhase.metering;
			} else if (isEarlyPeriodOver())
				return stopMetering();
			else
				return MeteringPhase.not_started;
		}

		/** Check if initial metering should start.
		 * @return true if metering should start. */
		private boolean shouldStart() {
			return shouldStart(START_SECS);
		}

		/** Check if metering should start.
		 * @param n_secs Number of seconds to average data.
		 * @return true if metering should start, based on segment
		 *         density. */
		private boolean shouldStart(int n_secs) {
			Double sk = segment_k_hist.average(0, steps(n_secs));
			return (sk != null) && (sk > K_DES);
		}

		/** Check if early metering period is over */
		private boolean isEarlyPeriodOver() {
			return isPeriodExpiring(EARLY_METER_END_MINUTES);
		}

		/** Check if metering period is expiring soon.
		 * @param m Number of minutes before end of metering period.
		 * @return true if within m minutes of end of period. */
		private boolean isPeriodExpiring(int m) {
			int min = TimeSteward.currentMinuteOfDayInt();
			int stop_min = meter.getStopMin();
			return min >= stop_min - m;
		}

		/** Check if ramp meter should continue metering.
		 * @return New metering phase. */
		private MeteringPhase checkContinueMetering() {
			return shouldFlush()
			     ? MeteringPhase.flushing
			     : MeteringPhase.metering;
		}

		/** Check if ramp meter should transition to flushing phase.
		 * @return true if meter should flush queue. */
		private boolean shouldFlush() {
			return isFlushTime() || isSegmentFlowing();
		}

		/** Check if it's time to flush queue */
		private boolean isFlushTime() {
			return isPeriodExpiring(FLUSH_MINUTES);
		}

		/** Check if mainline segment is flowing */
		private boolean isSegmentFlowing() {
			Double str_k = segment_k_hist.average(0,
				steps(START_SECS));
			Double stp_k = segment_k_hist.average(0,
				steps(STOP_SECS));
			return isDensityLow(str_k) && isDensityLow(stp_k);
		}

		/** Check if ramp meter should continue flushing.
		 * @return New metering phase. */
		private MeteringPhase checkContinueFlushing() {
			return isQueueEmpty()
			     ? stopMetering()
			     : MeteringPhase.flushing;
		}

		/** Stop metering.
		 * @return New metering phase. */
		private MeteringPhase stopMetering() {
			release_rate = 0;
			return MeteringPhase.stopped;
		}

		/** Check if metering should restart.
		 * @return New metering phase. */
		private MeteringPhase checkRestart() {
			return shouldRestart()
			     ? restartMetering()
			     : MeteringPhase.stopped;
		}

		/** Check if metering should restart (after stopping).
		 * @return true if metering should restart. */
		private boolean shouldRestart() {
			return shouldStart(RESTART_SECS) && !isFlushTime();
		}

		/** Retart metering.
		 * @return New metering phase. */
		private MeteringPhase restartMetering() {
			resetAccumulators();
			return MeteringPhase.metering;
		}

		/** Set metering rate.  Minimum and maximum rates are applied
		 * before setting rate.
		 * @param rn Next metering rate. */
		private void setRate(double rn) {
			release_rate = (int) Math.round(limitRate(rn));
			meter.setRatePlanned(release_rate);
		}

		/** Get historical passage flow.
		 * @param step Time step in past (0 for current).
		 * @param secs Number of seconds to average.
		 * @return Passage flow at 'step' time steps ago. */
		private Double getPassage(int step, int secs) {
			return passage_hist.average(step, steps(secs));
		}

		/** Get current segment density.
		 * @return segment density, or null for missing data. */
		private Double getSegmentDensity() {
			return segment_k_hist.get(0);
		}

		/** Get the minimum metering rate.
		 * @return Minimum metering rate */
		private int getMinimumRate() {
			return min_rate;
		}

		/** Get the maximum metering rate.
		 * @return Maximum metering rate */
		private int getMaximumRate() {
			return max_rate;
		}

		/** Limit metering rate within minimum and maximum rates */
		private double limitRate(double r) {
			return Math.min(getMaximumRate(),
			       Math.max(getMinimumRate(), r));
		}

		/** Calculate a new metering rate.
		 *
		 * @param k Segment density.
		 * @return Metering rate (vehicles per hour).
		 */
		private double calculateRate(double k) {
			if (phase == MeteringPhase.flushing)
				return getMaximumRate();
			double rate = limitRate(getRate());
			return (k <= K_DES)
			      ?	lerpBelow(rate, k)
			      : lerpAbove(rate, k);
		}

		/** Get current metering rate.
		 * @return metering rate */
		private double getRate() {
			double r = release_rate;
			if (r > 0)
				return r;
			else {
				Double p = getPassage(0, 90);
				return (p != null) ? p : getMaxRelease();
			}
		}

		/** Calculate a linear interpolation of metering rate below
		 * desired segment density.
		 *
		 * <pre>
		 * r_mx |__
		 *      |  --__
		 *      |      --__
		 *      |          --__
		 *      |              --__
		 *      |                  --__
		 *      |                      --__
		 * rate |                          --__
		 *      |
		 *      .------------------------------
		 *      0          k (density)       K_DES
		 * </pre>
		 *
		 * @param rate Previous metering rate.
		 * @param k Segment density.
		 * @return Interpolated metering rate.
		 */
		private double lerpBelow(double rate, double k) {
			assert k <= K_DES;
			double r_mx = getMaximumRate();
			double ratio = (r_mx - rate) / K_DES;
			return r_mx - k * ratio;
		}

		/** Calculate a linear interpolation of metering rate above
		 * desired segment density.
		 *
		 * <pre>
		 * rate |__
		 *      |  --__
		 *      |      --__
		 *      |          --__
		 *      |              --__
		 *      |                  --__
		 *      |                      --__
		 * r_mn |                          --__
		 *      |
		 *      .------------------------------
		 *      K_DES      k (density)       K_JAM
		 * </pre>
		 *
		 * @param rate Previous metering rate.
		 * @param k Segment density.
		 * @return Interpolated metering rate.
		 */
		private double lerpAbove(double rate, double k) {
			assert k > K_DES;
			double r_mn = getMinimumRate();
			double ratio = (rate - r_mn) / (K_JAM - K_DES);
			return rate - (k - K_DES) * ratio;
		}

		/** Get the downstream node for the segment */
		private StationNode segmentDownstream() {
			if (s_node != null) {
				StationNode dn = s_node.segmentStationNode();
				if (dn != null)
					return dn;
			}
			return null;
		}

		/** Log a meter event */
		protected void logMeterEvent() {
			StationNode dn = segmentDownstream();
			String dns = (dn != null) ? dn.station.getName() : null;
			Double sd = getSegmentDensity();
			float seg_den = (sd != null) ? sd.floatValue() : 0;
			MeterEvent ev = new MeterEvent(EventType.METER_EVENT,
				meter.name, phase.ordinal(),
				getQueueState().ordinal(), queueLength(),
				demand_adj, estimateWaitSecs(),
				limit_control.ordinal(), min_rate, release_rate,
				max_rate, dns, seg_den);
			BaseObjectImpl.logEvent(ev);
		}

		/** Get a string representation of a meter state */
		@Override
		public String toString() {
			Double sd = getSegmentDensity();
			float seg_den = (sd != null) ? sd.floatValue() : 0;
			return "meter:" + meter.getName() + " phase:" + phase +
			       " seg_den:" + seg_den;
		}
	}
}
