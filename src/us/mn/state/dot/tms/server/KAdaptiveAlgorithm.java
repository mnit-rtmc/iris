/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.HOUR;
import static us.mn.state.dot.tms.server.Constants.FEET_PER_MILE;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import static us.mn.state.dot.tms.server.RampMeterImpl.filterRate;
import static us.mn.state.dot.tms.server.RampMeterImpl.getMaxRelease;

/**
 * Density-based Adaptive Metering with Variable Bottleneck
 * Metering Algorithm.
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
	};

	/** Algorithm debug log */
	static private final DebugLog ALG_LOG = new DebugLog("kadaptive");

	/** Number of seconds for one time step */
	static private final int STEP_SECONDS = 30;

	/** Calculate steps per hour */
	static private final double STEP_HOUR =
		new Interval(STEP_SECONDS).per(HOUR);

	/** Bottleneck density (vehicles per lane-mile) */
	static private final int K_BOTTLENECK = 30;

	/** Critical density (vehicles / mile) */
	static private final int K_CRIT = 37;

	/** Desired density (vehicles / mile) */
	static private final double K_DES = K_CRIT * 0.8;

	/** Low density (vehicles / mile) */
	static private final double K_LOW = K_CRIT * 0.65;

	/** Jam density (vehicles / mile) */
	static private final int K_JAM = 180;

	/** Ramp queue jam density (vehicles / mile) */
	static private final int K_JAM_RAMP = 140;

	/** Ramp queue jam density (vehicles per foot) */
	static private final float JAM_VPF = (float)K_JAM_RAMP / FEET_PER_MILE;

	/** Number fo time steps to check before start metering */
	static private final int START_STEPS = steps(60);

	/** Time threshold for stop metering (seconds) */
	static private final int STOP_SECONDS = 300;

	/** Number of time steps to check before stop metering */
	static private final int STOP_STEPS = steps(STOP_SECONDS);

	/** Number of time steps to check before restart metering */
	static private final int RESTART_STEPS = steps(300);

	/** Maximum number of time steps needed for sample history */
	static private final int MAX_STEPS = Math.max(Math.max(START_STEPS,
		STOP_STEPS), RESTART_STEPS);

	/** Number of time steps for bottleneck trend check */
	static private final int BOTTLENECK_TREND_STEPS = steps(90);

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

	/** Number of steps for average density to check corridor state */
	static private final int AVG_K_STEPS = steps(900);

	/** Number of trend steps for average density to check corridor state */
	static private final int AVG_K_TREND_STEPS = steps(300);

	/** Number of steps for trending density history */
	static private final int TREND_STEPS = Math.max(MAX_STEPS,
		AVG_K_STEPS + AVG_K_TREND_STEPS);

	/** Queue occupancy override threshold */
	static private final int QUEUE_OCC_THRESHOLD = 25;

	/** Number of seconds queue must be empty before resetting green */
	static private final int QUEUE_EMPTY_RESET_SECS = 90;

	/** Threshold to determine when queue is empty */
	static private final int QUEUE_EMPTY_THRESHOLD = -1;

	/** Ratio for max rate to target rate */
	static private final float TARGET_MAX_RATIO = 1.3f;

	/** Ratio for min rate to target rate */
	static private final float TARGET_MIN_RATIO = 0.7f;

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
		if(vol >= 0) {
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
		if(vol >= 0)
			return vol * STEP_HOUR;
		else
			return null;
	}

	/** Convert flow rate to volume for a given period.
	 * @param flow Flow rate to convert (vehicles / hour).
	 * @param period Period for volume (seconds).
	 * @return Volume over given period. */
	static private float volumePeriod(int flow, int period) {
		if(flow >= 0 && period > 0) {
			float hour_frac = HOUR.per(new Interval(period));
			return flow * hour_frac;
		} else
			return MISSING_DATA;
	}

	/** States for all K adaptive algorithms */
	static private HashMap<String, KAdaptiveAlgorithm> ALL_ALGS =
		new HashMap<String, KAdaptiveAlgorithm>();

	/** Get the K adaptive algorithm state for a meter */
	static public KAdaptiveAlgorithm meterState(RampMeterImpl meter) {
		Corridor c = meter.getCorridor();
		if(c != null) {
			KAdaptiveAlgorithm alg = lookupAlgorithm(c);
			if(alg.createMeterState(meter))
				return alg;
		}
		return null;
	}

	/** Lookup an algorithm for a corridor */
	static private KAdaptiveAlgorithm lookupAlgorithm(Corridor c) {
		KAdaptiveAlgorithm alg = ALL_ALGS.get(c.getID());
		if(alg == null) {
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
		while(it.hasNext()) {
			KAdaptiveAlgorithm alg = it.next();
			alg.processInterval();
			if(alg.isDone()) {
				alg.log("isDone: removing");
				it.remove();
			}
		}
	}

	/** Metering corridor */
	private final Corridor corridor;

	/** Hash map of ramp meter states */
	private final HashMap<String, MeterState> meterStates =
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
		if(ALG_LOG.isOpen())
			debug();
	}

	/** Create nodes from corridor structure */
	private Node createNodes() {
		Node first = null;
		Node prev = null;
		Iterator<R_Node> itr = corridor.iterator();
		while(itr.hasNext()) {
			R_NodeImpl rnode = (R_NodeImpl) itr.next();
			Node n = createNode(rnode, prev);
			if(n != null)
				prev = n;
			if(first == null)
				first = prev;
		}
		return first;
	}

	/** Create one node */
	private Node createNode(R_NodeImpl rnode, Node prev) {
		Float mile = corridor.getMilePoint(rnode);
		if(mile != null)
			return createNode(rnode, mile, prev);
		else
			return null;
	}

	/** Create one node */
	private Node createNode(R_NodeImpl rnode, float mile, Node prev) {
		switch(R_NodeType.fromOrdinal(rnode.getNodeType())) {
		case ENTRANCE:
			return new EntranceNode(rnode, mile, prev);
		case STATION:
			StationImpl stat = rnode.getStation();
			if(stat != null && stat.getActive())
				return new StationNode(rnode, mile, prev, stat);
		default:
			return null;
		}
	}

	/** Debug corridor structure */
	private void debug() {
		log("-------- Corridor Structure --------");
		for(Node n = head; n != null; n = n.downstream)
			log(n.toString());
	}

	/** Log one message */
	private void log(String msg) {
		ALG_LOG.log(corridor.getName() + ": " + msg);
	}

	/** Validate algorithm state for a meter */
	@Override
	public void validate(RampMeterImpl meter) {
		MeterState ms = getMeterState(meter);
		if(ms != null) {
			ms.validate();
			if(ALG_LOG.isOpen())
				log(ms.toString());
		}
	}

	/** Get ramp meter queue state enum value */
	@Override
	public RampMeterQueue getQueueState(RampMeterImpl meter) {
		MeterState ms = getMeterState(meter);
		if(ms != null)
			return ms.getQueueState();
		else
			return RampMeterQueue.UNKNOWN;
	}

	/** Get the meter state for a given ramp meter */
	private MeterState getMeterState(RampMeterImpl meter) {
		if(meter.getCorridor() == corridor)
			return meterStates.get(meter.getName());
		else {
			// Meter must have been changed to a different
			// corridor; throw away old meter state
			meterStates.remove(meter.getName());
			return null;
		}
	}

	/** Create the meter state for a given ramp meter */
	private boolean createMeterState(RampMeterImpl meter) {
		EntranceNode en = findEntranceNode(meter);
		if(en != null) {
			MeterState ms = new MeterState(meter, en);
			meterStates.put(meter.getName(), ms);
			return true;
		} else
			return false;
	}

	/** Find an entrance node matching the given ramp meter.
	 * @param meter Ramp meter to search for.
	 * @return Entrance node matching ramp meter. */
	private EntranceNode findEntranceNode(RampMeterImpl meter) {
		R_NodeImpl rnode = meter.getEntranceNode();
		for(Node n = head; n != null; n = n.downstream) {
			if(n instanceof EntranceNode) {
				EntranceNode en = (EntranceNode)n;
				if(en.rnode.equals(rnode))
					return en;
			}
		}
		return null;
	}

	/** Process the algorithm for the one interval */
	private void processInterval() {
		updateStations();
		findBottlenecks();
	}

	/** Update the station nodes */
	private void updateStations() {
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			sn.updateState();
		}
	}

	/** Find bottlenecks */
	private void findBottlenecks() {
		findBottleneckCandidates();
		if(ALG_LOG.isOpen())
			debugBottlenecks();
	}

	/** Find bottleneck candidates. */
	private void findBottleneckCandidates() {
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			sn.checkBottleneck();
		}
	}

	/** Debug bottlenecks */
	private void debugBottlenecks() {
		StringBuilder sb = new StringBuilder();
		sb.append("Bottlenecks: ");
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			if(sn.isBottleneck) {
				sb.append(sn.toString());
				sb.append(',');
			}
		}
		log(sb.toString());
	}

	/** Get the furthest upstream station node. */
	private StationNode firstStation() {
		for(Node n = head; n != null; n = n.downstream) {
			if(n instanceof StationNode)
				return (StationNode) n;
		}
		return null;
	}

	/** Get the furthest downstream station node. */
	private StationNode lastStation() {
		for(Node n = tail; n != null; n = n.upstream) {
			if(n instanceof StationNode)
				return (StationNode) n;
		}
		return null;
	}

	/** Is this KAdaptiveAlgorithm done? */
	private boolean isDone() {
		for(MeterState ms : meterStates.values()) {
			if(ms.meter.isOperating())
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
			if(up != null)
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
			while(n.downstream != null)
				n = n.downstream;
			return n;
		}

		/** Find next upstream station node.
		 * @return Upstream station node. */
		protected StationNode upstreamStation() {
			for(Node n = upstream; n != null; n = n.upstream) {
				if(n instanceof StationNode)
					return (StationNode) n;
			}
			return null;
		}

		/** Find next downstream station node.
		 * @return Downstream station node. */
		protected StationNode downstreamStation() {
			for(Node n = downstream; n != null; n = n.downstream) {
				if(n instanceof StationNode)
					return (StationNode) n;
			}
			return null;
		}
	}

	/** Check if a station node is a bottleneck */
	static private boolean isBottleneck(StationNode sn) {
		return sn != null && sn.isBottleneck;
	}

	/** Node to manage station on corridor */
	protected class StationNode extends Node {

		/** StationImpl mapping this state */
		private final StationImpl station;

		/** Speed history */
		private final BoundedSampleHistory speed_hist =
			new BoundedSampleHistory(steps(60));

		/** Density history */
		private final BoundedSampleHistory density_hist =
			new BoundedSampleHistory(MAX_STEPS);

		/** Is bottleneck? */
		private boolean isBottleneck = false;

		/** Create a new station node. */
		public StationNode(R_NodeImpl rnode, float m, Node up,
			StationImpl stat)
		{
			super(rnode, m, up);
			station = stat;
		}

		/** Update station state.
		 * It must be called before finding bottleneck. */
		private void updateState() {
			density_hist.push(getStationDensity());
			speed_hist.push(getStationSpeed());
		}

		/** Get the current station density */
		private Double getStationDensity() {
			float d = station.getDensity();
			return d >= 0 ? (double)d : null;
		}

		/** Get the current station speed */
		private Double getStationSpeed() {
			float s = station.getSpeed();
			return s >= 0 ? (double)s : null;
		}

		/** Check if a station is a bottleneck */
		protected void checkBottleneck() {
			isBottleneck = isCurrentBottleneck();
		}

		/** Is a station currently a bottleneck? */
		private boolean isCurrentBottleneck() {
			return isDensityHigh() ||
			      (isCurrentDensityHigh() &&
			      (isBottleneck || isDensityIncreasing()));
		}

		/** Check if density is high for all trend steps */
		private boolean isDensityHigh() {
			for(int i = 0; i < BOTTLENECK_TREND_STEPS; i++) {
				if(getDensity(i) < K_BOTTLENECK)
					return false;
			}
			return true;
		}

		/** Check if current density is high */
		private boolean isCurrentDensityHigh() {
			return getDensity() >= K_BOTTLENECK;
		}

		/** Check if density has been increasing for all trend steps */
		private boolean isDensityIncreasing() {
			double k = getDensity(0);
			for(int i = 1; i < BOTTLENECK_TREND_STEPS; i++) {
				double nk = getDensity(i);
				if(k < nk)
					return false;
				k = nk;
			}
			return true;
		}

		/** Get average density of a mainline segment beginning at the
		 * current station.
		 * @param dn Segment downstream station node.
		 * @return average density (distance weight). */
		private double calculateSegmentDensity(StationNode dn) {
			return calculateSegmentDensity(dn, 0);
		}

		/** Get average density of a mainline segment beginning at the
		 * current station.  This works by splitting each consecutive
		 * pair of stations into 3 equal links and assigning average
		 * density to the middle link.  All links are then averaged,
		 * weighted by length.
		 *
		 * @param dn Segment downstream station node.
		 * @param step Time step in past (0 for current).
		 * @return average density (distance weight). */
		private double calculateSegmentDensity(final StationNode dn,
			int step)
		{
			StationNode cursor = this;
			double dist_seg = 0;	/* Segment distance */
			double veh_seg = 0;	/* Sum of vehicles in segment */
			double k_cursor = cursor.getDensity(step);
			for(StationNode sn = cursor.downstreamStation();
			    sn != null && cursor != dn;
			    sn = sn.downstreamStation())
			{
				double k_down = sn.getDensity(step);
				double k_middle = (k_cursor + k_down) / 2;
				double dist = cursor.distanceMiles(sn);
				dist_seg += dist;
				veh_seg += (k_cursor + k_middle + k_down) / 3 *
					dist;
				cursor = sn;
				k_cursor = k_down;
			}
			if(dist_seg > 0)
				return veh_seg / dist_seg;
			else
				return k_cursor;
		}

		/** Get aggregated density at current time step.
		 * @return average 1 min density; missing data returns 0. */
		public double getDensity() {
			return getDensity(0);
		}

		/** Get aggregated density at specified time step.
		 * @param step Time step in past (0 for current).
		 * @return average 1 min density at 'step' time steps ago.*/
		public double getDensity(int step) {
			Double avg = density_hist.average(step, steps(60));
			if(avg != null)
				return avg;
			else
				return 0;
		}

		/** Get speed at current time step.
		 * @return Average 1 min speed. */
		private double getSpeed() {
			Double avg = speed_hist.average(0, steps(60));
			if(avg != null)
				return avg;
			else
				return 0;
		}

		/** Find downstream segment station node.
		 * @return Downstream segment station node. */
		protected StationNode segmentStationNode() {
			StationNode dn = this;
			for(StationNode sn = this; sn != null;
			    sn = sn.downstreamStation())
			{
				if(sn.isBottleneck)
					return sn;
				if(distanceMiles(sn) > SEGMENT_LENGTH_MILES)
					break;
				dn = sn;
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
		early_metering,
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

		/** Queue detector set */
		private final DetectorSet queue = new DetectorSet();

		/** Passage detector set */
		private final DetectorSet passage = new DetectorSet();

		/** Merge detector set */
		private final DetectorSet merge = new DetectorSet();

		/** Bypass detector set */
		private final DetectorSet bypass = new DetectorSet();

		/** Green count detector set */
		private final DetectorSet green = new DetectorSet();

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
			new BoundedSampleHistory(steps(300));

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

		/** Time with no downstream bottleneck (seconds) */
		private int no_bottleneck_secs = 0;

		/** Time queue has been empty (seconds) */
		private int queue_empty_secs = 0;

		/** Time queue has been backed-up (seconds) */
		private int queue_backup_secs = 0;

		/** Controlling minimum rate limit */
		private MinimumRateLimit limit_control =
			MinimumRateLimit.target_min;

		/** Segment density history (vehicles / mile) */
		private final BoundedSampleHistory segment_k_hist =
			new BoundedSampleHistory(TREND_STEPS);

		/** Create a new meter state */
		public MeterState(RampMeterImpl mtr, EntranceNode en) {
			meter = mtr;
			node = en;
			DetectorSet ds = meter.getDetectorSet();
			queue.addDetectors(ds, LaneType.QUEUE);
			passage.addDetectors(ds, LaneType.PASSAGE);
			merge.addDetectors(ds, LaneType.MERGE);
			bypass.addDetectors(ds, LaneType.BYPASS);
			green.addDetectors(ds, LaneType.GREEN);
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
			if(us == null)
				return true;
			if(ds == null)
				return false;
			int uf = node.distanceFeet(us);
			int df = node.distanceFeet(ds);
			return df < DOWNSTREAM_STATION_FEET && df < uf;
		}

		/** Get the total cumulative demand (vehicles) */
		private float cumulativeDemand(int i) {
			Double d = demand_accum_hist.get(i);
			if (d != null)
				return d.floatValue();
			else
				return 0;
		}

		/** Get the total cumulative demand (vehicles) */
		private float cumulativeDemand() {
			return cumulativeDemand(0);
		}

		/** Validate meter state.
		 *   - Update state timers.
		 *   - Update passage flow and accumulator.
		 *   - Update demand flow and accumulator.
		 *   - Calculate metering rate. */
		private void validate() {
			// NOTE: these must happen in proper order
			updateNoBottleneckTime();
			checkQueueBackedUp();
			checkQueueEmpty();
			demand_adj = calculateDemandAdjustment();
			updatePassageState();
			updateDemandState();
			min_rate = calculateMinimumRate();
			max_rate = calculateMaximumRate();
			if(s_node != null)
				calculateMeteringRate();
		}

		/** Update the "no bottleneck" time */
		private void updateNoBottleneckTime() {
			if (hasBottleneck())
				no_bottleneck_secs = 0;
			else
				no_bottleneck_secs += STEP_SECONDS;
		}

		/** Check the queue backed-up state */
		private void checkQueueBackedUp() {
			if (isQueueOccupancyHigh())
				queue_backup_secs += STEP_SECONDS;
			else
				queue_backup_secs = 0;
		}

		/** Check if queue is empty */
		private void checkQueueEmpty() {
			if (isQueueEmpty())
				queue_empty_secs += STEP_SECONDS;
			else
				queue_empty_secs = 0;
			// Get rid of unused greens
			if (queue_empty_secs > QUEUE_EMPTY_RESET_SECS &&
			    isPassageBelowGreen())
				green_accum = passage_accum;
		}

		/** Calculate the demand adjustment.
		 * @return Demand adjustment (number of vehicles) */
		private float calculateDemandAdjustment() {
			return estimateDemandUndercount()
			     - estimateDemandOvercount();
		}

		/** Estimate demand undercount when occupancy is high.
		 * @return Vehicle undercount at queue detector. */
		private float estimateDemandUndercount() {
			return queueFullRatio() * availableStorage();
		}

		/** Estimate the queue full ratio.
		 * @return Ratio from 0 to 1. */
		private float queueFullRatio() {
			return Math.min(2*queue_backup_secs / maxWaitTime(), 1);
		}

		/** Estimate the available storage in queue.
		 * @return Available storage (vehicles) from 0 to maxStorage. */
		private float availableStorage() {
			if(passage_good) {
				float q_len = Math.max(queueLength(), 0);
				return Math.max(maxStorage() - q_len, 0);
			} else
				return maxStorage() / 3;
		}

		/** Estimate demand overcount when queue is empty.
		 * @return Vehicle overcount at queue detector (may be
		 *          negative). */
		private float estimateDemandOvercount() {
			return queueEmptyRatio() * queueLength();
		}

		/** Estimate the queue empty ratio.
		 * @return Ratio from 0 to 1. */
		private float queueEmptyRatio() {
			return Math.min(2* queue_empty_secs / maxWaitTime(), 1);
		}

		/** Update ramp passage output state */
		private void updatePassageState() {
			int passage_vol = calculatePassageCount();
			passage_hist.push(flowRate(passage_vol));
			if(passage_vol >= 0)
				passage_accum += passage_vol;
			else
				passage_good = false;
			int green_vol = green.getVolume();
			if(green_vol > 0)
				green_accum += green_vol;
		}

		/** Calculate passage count (vehicles).
		 * @return Passage vehicle count */
		private int calculatePassageCount() {
			int vol = passage.getVolume();
			if(vol >= 0)
				return vol;
			vol = merge.getVolume();
			if(vol >= 0) {
				int b = bypass.getVolume();
				if(b > 0) {
					vol -= b;
					if(vol < 0)
						return 0;
				}
				return vol;
			}
			return MISSING_DATA;
		}

		/** Update ramp queue demand state */
		private void updateDemandState() {
			float demand_vol = calculateQueueDemand();
			demand_hist.push(flowRate(demand_vol));
			double demand_accum = cumulativeDemand() + demand_vol;
			demand_accum_hist.push(demand_accum);
			tracking_demand = trackingDemand();
		}

		/** Calculate ramp queue demand.
		 * @return Current ramp queue demand (vehicles) */
		private float calculateQueueDemand() {
			return Math.max(queueDemandVolume() + demand_adj, 0);
		}

		/** Get queue demand volume for the current period */
		private float queueDemandVolume() {
			float vol = queue.getVolume();
			if(vol >= 0)
				return vol;
			else {
				int target = getDefaultTarget();
				return volumePeriod(target, STEP_SECONDS);
			}
		}

		/** Estimate the length of queue (vehicles).
		 * @return Queue length (may be negative). */
		private float queueLength() {
			return (passage_good)
			     ? (cumulativeDemand() - passage_accum)
			     : 0;
		}

		/** Calculate tracking demand rate at queue detector.
		 * @return Tracking demand flow rate (vehicles / hour) */
		private int trackingDemand() {
			Double d = demand_hist.average();
			if (d != null)
				return (int)Math.round(d);
			else
				return getDefaultTarget();
		}

		/** Get the default target metering rate (vehicles / hour) */
		private int getDefaultTarget() {
			int t = meter.getTarget();
			return (t > 0) ? t : getMaxRelease();
		}

		/** Check if the meter queue is empty */
		private boolean isQueueEmpty() {
			return isQueueFlowLow() && !isQueueOccupancyHigh();
		}

		/** Check if the queue flow is low.  If the passage detector
		 * is not good, assume this is true. */
		private boolean isQueueFlowLow() {
			return (passage_good)
			     ? (isDemandBelowPassage() || isPassageBelowGreen())
			     : true;
		}

		/** Check if cumulative demand is below cumulative passage */
		private boolean isDemandBelowPassage() {
			return queueLength() < QUEUE_EMPTY_THRESHOLD;
		}

		/** Check if cumulative passage is below cumulative green */
		private boolean isPassageBelowGreen() {
			return violationCount() < QUEUE_EMPTY_THRESHOLD;
		}

		/** Check if queue occupancy is above threshold */
		private boolean isQueueOccupancyHigh() {
			return queue.getMaxOccupancy() > QUEUE_OCC_THRESHOLD;
		}

		/** Calculate violation count (passage above green count) */
		private int violationCount() {
			if(passage_good)
				return passage_accum - green_accum;
			else
				return 0;
		}

		/** Reset the demand / passage accumulators */
		private void resetAccumulators() {
			demand_accum_hist.clear();
			demand_adj = 0;
			passage_good = true;
			passage_accum = 0;
			green_accum = 0;
		}

		/** Get ramp meter queue state enum value */
		private RampMeterQueue getQueueState() {
			if(isMetering()) {
				if(isQueueFull())
					return RampMeterQueue.FULL;
				else if(!passage_good)
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
			return isQueueOccupancyHigh() || isQueueLimitFull();
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

		/** Calculate minimum rate (vehicles / hour) */
		private int calculateMinimumRate() {
			if(!passage_good) {
				limit_control = MinimumRateLimit.passage_fail;
				return tracking_demand;
			} else {
				int r = queueStorageLimit();
				limit_control = MinimumRateLimit.storage_limit;
				int rr = queueWaitLimit();
				if(rr > r) {
					r = rr;
					limit_control =
						MinimumRateLimit.wait_limit;
				}
				rr = targetMinRate();
				if(rr > r) {
					r = rr;
					limit_control =
						MinimumRateLimit.target_min;
				}
				return filterRate(r);
			}
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
			float demand_proj = cumulativeDemand() + proj_arrive;
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
			for(int i = 1; i <= wait_steps; i++) {
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

		/** Calculate target minimum rate.
		 * @return Target minimum rate (vehicles / hour). */
		private int targetMinRate() {
			return Math.round(tracking_demand * TARGET_MIN_RATIO);
		}

		/** Calculate target maximum rate.
		 * @return Target maxumum rate (vehicles / hour). */
		private int calculateMaximumRate() {
			int target_max = Math.round(tracking_demand *
				TARGET_MAX_RATIO);
			return filterRate(Math.max(target_max, min_rate));
		}

		/** Calculate the metering rate */
		private void calculateMeteringRate() {
			assert s_node != null;
			StationNode dn = s_node.segmentStationNode();
			double k = s_node.calculateSegmentDensity(dn);
			segment_k_hist.push(k);
			phase = checkMeterPhase();
			if(isMetering())
				setRate(calculateRate(k));
		}

		/** Check metering phase transitions.
		 * @return New metering phase. */
		private MeteringPhase checkMeterPhase() {
			switch(phase) {
			case not_started:
				return checkStart();
			case early_metering:
				return checkDoneEarlyMetering();
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
			if(shouldStart()) {
				resetAccumulators();
				return MeteringPhase.early_metering;
			} else if(isEarlyPeriodOver())
				return stopMetering();
			else
				return MeteringPhase.not_started;
		}

		/** Check if initial metering should start.
		 * @return true if metering should start. */
		private boolean shouldStart() {
			return shouldStartDensity(START_STEPS);
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

		/** Check if metering should start from density.
		 * @param n_steps Number of steps to check.
		 * @return true if metering should start, based on segment
		 *         density. */
		private boolean shouldStartDensity(int n_steps) {
			for(int i = 0; i < n_steps; i++) {
				Double sk = getSegmentDensity(i);
				if(sk != null && sk < K_BOTTLENECK)
					return false;
			}
			return true;
		}

		/** Check if we're done with early metering.
		 * @return New metering phase. */
		private MeteringPhase checkDoneEarlyMetering() {
			if(isSegmentDensityTrendingDown() ||
			   isSegmentDensityLow() ||
			   isEarlyPeriodOver())
			{
				return MeteringPhase.metering;
			} else
				return MeteringPhase.early_metering;
		}

		/** Check if segment density is trending downward */
		private boolean isSegmentDensityTrendingDown() {
			Double kn = segment_k_hist.average(0, AVG_K_STEPS);
			if(kn == null)
				return false;
			for(int i = 1; i < AVG_K_TREND_STEPS; i++) {
				Double k = segment_k_hist.average(i,
					AVG_K_STEPS);
				if(k == null || kn > k)
					return false;
				kn = k;
			}
			return true;
		}

		/** Check if segment density is low */
		private boolean isSegmentDensityLow() {
			Double sk = getSegmentDensity(0);
			return sk != null && sk < K_LOW;
		}

		/** Check if ramp meter should continue metering.
		 * @return New metering phase. */
		private MeteringPhase checkContinueMetering() {
			return shouldFlush()
			     ? MeteringPhase.flushing
			     : MeteringPhase.metering;
		}

		/** Check if there is a bottleneck downstream of meter */
		private boolean hasBottleneck() {
			return s_node != null &&
			       isBottleneck(s_node.segmentStationNode());
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
			return (no_bottleneck_secs >= STOP_SECONDS) &&
			       !isSegmentDensityHigh();
		}

		/** Check if the segment density is higher than desired. */
		private boolean isSegmentDensityHigh() {
			for(int i = 0; i < STOP_STEPS; i++) {
				Double sk = getSegmentDensity(i);
				if(sk != null && sk > K_DES)
					return true;
			}
			return false;
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
			return shouldStartDensity(RESTART_STEPS) &&
			       !isFlushTime();
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
			release_rate = (int)Math.round(limitRate(rn));
			meter.setRatePlanned(release_rate);
		}

		/** Get historical passage flow.
		 * @param step Time step in past (0 for current).
		 * @param secs Number of seconds to average.
		 * @return Passage flow at 'step' time steps ago. */
		private Double getPassage(int step, int secs) {
			return passage_hist.average(step, steps(secs));
		}

		/** Get segment density at 'step' time steps ago.
		 * @param step Time step in past (0 for current).
		 * @return segment density, or null for missing data. */
		private Double getSegmentDensity(int step) {
			return segment_k_hist.get(step);
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
			if(phase == MeteringPhase.flushing)
				return getMaximumRate();
			double rate = limitRate(getRate());
			if(k <= K_DES)
				return lerpBelow(rate, k);
			else
				return lerpAbove(rate, k);
		}

		/** Get current metering rate.
		 * @return metering rate */
		private double getRate() {
			double r = release_rate;
			if(r > 0)
				return r;
			else {
				Double p = getPassage(0, 90);
				if(p != null)
					return p;
				else
					return getMaxRelease();
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

		/** Get a string representation of a meter state */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(meter.name);
			sb.append(",");
			sb.append(phase);
			sb.append(",");
			sb.append(getQueueState());
			sb.append(",");
			sb.append(queueLength());
			sb.append(",");
			sb.append(demand_adj);
			sb.append(",");
			sb.append(limit_control);
			sb.append(",");
			sb.append(min_rate);
			sb.append(",");
			sb.append(release_rate);
			sb.append(",");
			sb.append(max_rate);
			sb.append(",");
			sb.append(s_node);
			if(s_node != null) {
				StationNode dn = s_node.segmentStationNode();
				if(dn != null) {
					sb.append(",");
					sb.append(dn);
					sb.append(",");
					sb.append(dn.isBottleneck);
					sb.append(",");
					sb.append(getSegmentDensity(0));
				}
			}
			return sb.toString();
		}
	}
}
