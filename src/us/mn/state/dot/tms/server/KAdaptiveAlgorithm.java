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
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RampMeter;
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
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Douglas Lau
 * @author Soobin Jeon
 */
public class KAdaptiveAlgorithm implements MeterAlgorithmState {

	/** Enum for minimum limit control */
	enum MinimumRateLimit {
		pf,	/* passage failure */
		sl,	/* storage limit */
		wl,	/* wait limit */
		tm	/* target minimum */
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

	/** Rate value for checking if metering is started */
	static private final double START_FLOW_RATIO = 0.8;

	/** Acceleration threshold to decide bottleneck */
	static private final int A_BOTTLENECK = 1000;

	/** Number fo time steps to check before start metering */
	static private final int START_STEPS = steps(90);

	/** Number fo time steps to check density before start metering */
	static private final int START_STEPS_K = steps(60);

	/** Number of time steps to check before stop metering */
	static private final int STOP_STEPS = steps(300);

	/** Number of time steps to check before restart metering */
	static private final int RESTART_STEPS = steps(300);

	/** Maximum number of time steps needed for sample history */
	static private final int MAX_STEPS = Math.max(Math.max(START_STEPS,
		STOP_STEPS), RESTART_STEPS);

	/** Number of time steps for bottleneck trend before stop metering */
	static private final int BOTTLENECK_TREND_1_STEPS = steps(60);

	/** Number of time steps for bottleneck trend after stop metering */
	static private final int BOTTLENECK_TREND_2_STEPS = steps(120);

	/** Spacing between two bottlenecks (soft minimum) */
	static private final float BOTTLENECK_SPACING_MILES = 1.5f;

	/** Distance threshold for upstream station to meter association */
	static private final float UPSTREAM_STATION_MILES = 1.0f;

	/** Distance threshold for downstream station to meter association */
	static private final int DOWNSTREAM_STATION_FEET = 500;

	/** Number of steps for average density to check corridor state */
	static private final int AVG_K_STEPS = steps(900);

	/** Number of trend steps for average density to check corridor state */
	static private final int AVG_K_TREND_STEPS = steps(300);

	/** Number of steps for trending density history */
	static private final int TREND_STEPS = Math.max(MAX_STEPS,
		AVG_K_STEPS + AVG_K_TREND_STEPS);

	/** Queue occupancy override threshold */
	static private final int QUEUE_OCC_THRESHOLD = 25;

	/** Number of steps queue must be empty before resetting accumulators */
	static private final int QUEUE_EMPTY_STEPS = steps(90);

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

	/** Is started metering in this corridor? */
	private boolean isMeteringStarted = false;

	/** Should check stop condition? (depends on corridor density trend) */
	private boolean doStopChecking = false;

	/** Corridor density history for triggering metering stop */
	private final BoundedSampleHistory k_hist_corridor =
		new BoundedSampleHistory(AVG_K_STEPS + AVG_K_TREND_STEPS);

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
		R_NodeImpl rnode = meter.getR_Node();
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
		checkStopCondition();
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
		mergeBottleneckZones();
		debugBottlenecks();
	}

	/** Find bottleneck candidates. */
	private void findBottleneckCandidates() {
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			sn.clearBottleneck();
			sn.checkBottleneck();
		}
	}

	/** Merge zone by distnace and acceleration.
	 * Iterate from downstream to upstream. */
	private void mergeBottleneckZones() {
		for(StationNode sn = lastStation(); sn != null;
		    sn = sn.upstreamStation())
		{
			if(sn.isBottleneck)
				checkBottleneckMerge(sn);
		}
	}

	/** Check if a bottleneck station should be merged. */
	private void checkBottleneckMerge(final StationNode sn) {
		double k = sn.getAggregatedDensity();
		for(StationNode un = sn.upstreamStation(); un != null;
			un = un.upstreamStation())
		{
			if(!un.isBottleneck)
				continue;
			if(un.isBottleneckTooClose(sn)) {
				// close but independent bottleneck
				if(un.getAggregatedDensity() > k &&
				   un.getAcceleration() > A_BOTTLENECK)
					break;
				else
					un.isBottleneck = false;
			} else if(un.getAcceleration() > A_BOTTLENECK)
				break;
			else
				un.isBottleneck = false;
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

	/** Check whether any ramp meters should stop metering */
	private void checkStopCondition() {
		if(isMeteringStarted && !doStopChecking)
			checkCorridorStop();
	}

	/** Check if corridor stop checking should happen */
	private void checkCorridorStop() {
		StationNode bs = downstreamBottleneck();
		k_hist_corridor.push(calculateCorridorDensity(bs));
		if(bs == null && isDensityTrendingDown())
			doStopChecking = true;
	}

	/** Find the furthest downstream bottleneck station */
	private StationNode downstreamBottleneck() {
		for(StationNode sn = lastStation(); sn != null;
		    sn = sn.upstreamStation())
		{
			if(sn.isBottleneck)
				return sn;
		}
		return null;
	}

	/** Calculate the average density of a corridor up to a bottleneck.
	 * @param bs Bottleneck station.
	 * @return Average density up to the bottleneck. */
	private double calculateCorridorDensity(StationNode bs) {
		if(bs != null)
			return bs.calculateSegmentDensity(firstStation());
		else
			return 0;
	}

	/** Check if corridor density is trending downward */
	private boolean isDensityTrendingDown() {
		if(k_hist_corridor.isFull()) {
			for(int i = 0; i < AVG_K_TREND_STEPS; i++) {
				Double ma_next = k_hist_corridor.average(i,
					AVG_K_STEPS);
				Double ma_prev = k_hist_corridor.average(i + 1,
					AVG_K_STEPS);
				if(ma_next == null || ma_prev == null)
					return false;
				if(ma_next > ma_prev)
					return false;
			}
			return true;
		} else
			return false;
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

	/** Get number of time steps to check for bottleneck */
	private int bottleneckTrendSteps() {
		return doStopChecking ? BOTTLENECK_TREND_2_STEPS :
		                        BOTTLENECK_TREND_1_STEPS;
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

	/** Node to manage station on corridor */
	protected class StationNode extends Node {

		/** StationImpl mapping this state */
		private final StationImpl station;

		/** Speed history */
		private final BoundedSampleHistory speedHist =
			new BoundedSampleHistory(steps(60));

		/** Density history */
		private final BoundedSampleHistory densityHist =
			new BoundedSampleHistory(steps(300));

		/** Is bottleneck? */
		private boolean isBottleneck = false;

		/** Is bottleneck at previous time step? */
		private boolean isPrevBottleneck = false;

		/** Create a new station node. */
		public StationNode(R_NodeImpl rnode, float m, Node up,
			StationImpl stat)
		{
			super(rnode, m, up);
			station = stat;
		}

		/** Get the current station density */
		private Double getDensity() {
			float d = station.getDensity();
			return d >= 0 ? (double)d : null;
		}

		/** Get the current station speed */
		private Double getSpeed() {
			float s = station.getSpeed();
			return s >= 0 ? (double)s : null;
		}

		/** Update station state.
		 * It must be called before finding bottleneck. */
		private void updateState() {
			densityHist.push(getDensity());
			speedHist.push(getSpeed());
		}

		/** Check if a station is a bottleneck */
		protected void checkBottleneck() {
			if(getAggregatedDensity() >= K_BOTTLENECK) {
				if(isPrevBottleneck || isDensityIncreasing())
					isBottleneck = true;
			}
		}

		/** Check if density has been increasing for a number of steps
		 * or that all previous steps are high. */
		private boolean isDensityIncreasing() {
			boolean increasing = true;
			boolean high_k = true;
			for(int i = 0; i < bottleneckTrendSteps(); i++) {
				double k = getAggregatedDensity(i);
				double pk = getAggregatedDensity(i + 1);
				if(k < pk)
					increasing = false;
				if(k < K_BOTTLENECK || pk < K_BOTTLENECK)
					high_k = false;
			}
			return increasing || high_k;
		}

		/** Clear bottleneck state */
		protected void clearBottleneck() {
			isPrevBottleneck = isBottleneck;
			isBottleneck = false;
		}

		/** Is a bottleneck station too close to another? */
		protected boolean isBottleneckTooClose(StationNode sn) {
			return distanceMiles(sn) < BOTTLENECK_SPACING_MILES;
		}

		/** Get average density of a mainline segment ending at the
		 * current station.
		 * @param upStation upstream station of segment.
		 * @return average density (distance weight). */
		private double calculateSegmentDensity(StationNode upStation) {
			return calculateSegmentDensity(upStation, 0);
		}

		/** Get average density of a mainline segment ending at the
		 * current station.  This works by splitting each consecutive
		 * pair of stations into 3 equal links and assigning average
		 * density to the middle link.  All links are then averaged,
		 * weighted by length.
		 *
		 * @param upStation upstream station of segment.
		 * @param prevStep previous time steps (0 for current).
		 * @return average density (distance weight). */
		private double calculateSegmentDensity(StationNode upStation,
			int prevStep)
		{
			StationNode cursor = upStation;
			double dist_seg = 0;	/* Segment distance */
			double veh_seg = 0;	/* Sum of vehicles in segment */
			double k_cursor = cursor.getAggregatedDensity(prevStep);
			for(StationNode down = cursor.downstreamStation();
			    down != null && cursor != this;
			    down = down.downstreamStation())
			{
				double k_down = down.getAggregatedDensity(
					prevStep);
				double k_middle = (k_cursor + k_down) / 2;
				double dist = cursor.distanceMiles(down);
				dist_seg += dist;
				veh_seg += (k_cursor + k_middle + k_down) / 3 *
					dist;
				cursor = down;
				k_cursor = k_down;
			}
			if(dist_seg > 0)
				return veh_seg / dist_seg;
			else
				return k_cursor;
		}

		/** Return aggregated density at current time step.
		 * @return average 1 min density; missing data returns 0. */
		public double getAggregatedDensity() {
			return getAggregatedDensity(0);
		}

		/** Get aggregated density at 'prevStep' time steps ago.
		 * @return average 1 min density at 'prevStep' time steps ago.*/
		public double getAggregatedDensity(int prevStep) {
			Double avg = densityHist.average(prevStep, steps(60));
			if(avg != null)
				return avg;
			else
				return 0;
		}

		/** Return aggregated speed at current time step.
		 * @return average 1 min speed. */
		public double getAggregatedSpeed() {
			Double avg = speedHist.average(0, steps(60));
			if(avg != null)
				return avg;
			else
				return 0;
		}

		/** Return acceleration from current station to down station
		 * @return acceleration from current station to down station. */
		public double getAcceleration() {
			double u2 = getAggregatedSpeed();
			StationNode down = downstreamStation();
			if(down == null)
				return 0;
			double u1 = down.getAggregatedSpeed();
			double dm = distanceMiles(down);
			if(dm > 0)
				return (u1 * u1 - u2 * u2) / (2 * dm);
			else
				return 0;
		}

		/** Return next downstream bottleneck station node.
		 * @return Downstream bottleneck station node. */
		protected StationNode bottleneckStation() {
			for(StationNode sn = this; sn != null;
			    sn = sn.downstreamStation())
			{
				if(sn.isBottleneck)
					return sn;
			}
			return null;
		}

		/** Get a string representation of a station node */
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

		/** Current metering rate (vehicles / hour) */
		private int currentRate = 0;

		/** Minimum metering rate (vehicles / hour) */
		private int minimumRate = 0;

		/** Maximum metering rate (vehicles / hour) */
		private int maximumRate = 0;

		/** How many time steps there's no bottleneck at downstream */
		private int noBottleneckCount = 0;

		/** Queue demand history (vehicles / hour) */
		private final BoundedSampleHistory demandHist =
			new BoundedSampleHistory(steps(300));

		/** Cumulative demand history (vehicles) */
		private final BoundedSampleHistory demandAccumHist =
			new BoundedSampleHistory(steps(300));

		/** Target queue demand rate (vehicles / hour) */
		private int target_demand = 0;

		/** Passage sampling failure (latches until queue empty) */
		private boolean passage_failure = false;

		/** Cumulative passage count (vehicles) */
		private int passage_accum = 0;

		/** Ramp passage history (vehicles / hour) */
		private final BoundedSampleHistory passage_hist =
			new BoundedSampleHistory(MAX_STEPS);

		/** Cumulative green count (vehicles) */
		private int green_accum = 0;

		/** Time queue has been empty (steps) */
		private int queueEmptyCount = 0;

		/** Time queue has been full (steps) */
		private int queueFullCount = 0;

		/** Controlling minimum rate limit */
		private MinimumRateLimit limit_control = null;

		/** Metering rate flow history (vehicles / hour) */
		private final BoundedSampleHistory rate_hist =
			new BoundedSampleHistory(MAX_STEPS);

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
			Double d = demandAccumHist.get(i);
			if(d != null)
				return d.floatValue();
			else
				return 0;
		}

		/** Get the total cumulative demand (vehicles) */
		private float cumulativeDemand() {
			return cumulativeDemand(0);
		}

		/** Validate meter state.
		 *   - Save cumulative demand and merging flow
		 *   - Set current demand and merging flow
		 *   - Calculate metering rate */
		private void validate() {
			updateDemandState();
			updatePassageState();
			checkQueueEmpty();
			minimumRate = calculateMinimumRate();
			maximumRate = calculateMaximumRate();
			if(s_node != null)
				calculateMeteringRate();
		}

		/** Update ramp queue demand state */
		private void updateDemandState() {
			float demand_vol = calculateQueueDemand();
			demandHist.push(flowRate(demand_vol));
			double demand_accum = cumulativeDemand() + demand_vol;
			demandAccumHist.push(demand_accum);
			target_demand = targetDemand();
		}

		/** Calculate ramp queue demand.  Normally, this would be an
		 * integer value, but when estimates are used, it may need to
		 * have a fractional part.
		 * @return Ramp queue demand for current period (vehicles) */
		private float calculateQueueDemand() {
			float vol = queueDemandVolume();
			if(isQueueOccupancyHigh()) {
				queueFullCount++;
				vol += estimateQueueUndercount();
			} else
				queueFullCount = 0;
			return vol;
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

		/** Estimate queue undercount when occupancy is high.
		 * @return Vehicle undercount at queue detector. */
		private float estimateQueueUndercount() {
			float under = maxStorage() - queueLength();
			return Math.max(queueOverflowRatio() * under,
				minDemandAdjustment());
		}

		/** Estimate the queue overflow ratio.
		 * @return Ratio from 0 to 1. */
		private float queueOverflowRatio() {
			return Math.min(2 * queueFullSecs() / maxWaitTime(), 1);
		}

		/** Get the queue full duration (seconds) */
		private float queueFullSecs() {
			return queueFullCount * STEP_SECONDS;
		}

		/** Calculate the minimum demand undercount adjustment */
		private float minDemandAdjustment() {
			int minutes = Math.round(queueFullSecs() / 60);
			float per_min = STEP_SECONDS / 60.0f;
			return minutes * per_min;
		}

		/** Estimate the length of queue (vehicles) */
		private float queueLength() {
			return cumulativeDemand() - passage_accum;
		}

		/** Calculate target demand rate at queue detector.
		 * @return Target demand flow rate (vehicles / hour) */
		private int targetDemand() {
			Double avg_demand = demandHist.average();
			if(avg_demand != null)
				return (int)Math.round(avg_demand);
			else
				return getDefaultTarget();
		}

		/** Get the default target metering rate (vehicles / hour) */
		private int getDefaultTarget() {
			int t = meter.getTarget();
			return (t > 0) ? t : getMaxRelease();
		}

		/** Update ramp passage output state */
		private void updatePassageState() {
			int passage_vol = calculatePassageCount();
			passage_hist.push(flowRate(passage_vol));
			if(passage_vol >= 0)
				passage_accum += passage_vol;
			else
				passage_failure = true;
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
			if(isMetering()) {
				vol = green.getVolume();
				if(vol >= 0)
					return vol;
			}
			return MISSING_DATA;
		}

		/** Check if queue is empty */
		private void checkQueueEmpty() {
			if(isQueueEmpty())
				queueEmptyCount++;
			else
				queueEmptyCount = 0;
			if(queueEmptyCount >= QUEUE_EMPTY_STEPS)
				resetAccumulators();
		}

		/** Check if the meter queue is empty */
		private boolean isQueueEmpty() {
			return isQueueVolumeLow() && !isQueueOccupancyHigh();
		}

		/** Check if the queue volume is low */
		private boolean isQueueVolumeLow() {
			return isDemandBelowPassage() || isPassageBelowGreen();
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
			return passage_accum - green_accum;
		}

		/** Reset the demand / passage accumulators */
		private void resetAccumulators() {
			passage_failure = false;
			demandAccumHist.clear();
			passage_accum = 0;
			green_accum = 0;
			queueEmptyCount = 0;
			queueFullCount = 0;
		}

		/** Get ramp meter queue state enum value */
		private RampMeterQueue getQueueState() {
			if(isMetering()) {
				if(isQueueFull())
					return RampMeterQueue.FULL;
				else if(isQueueEmpty())
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
			      (isQueueStorageFull() ||
			       isQueueWaitAboveTarget());
		}

		/** Check if the ramp queue storage is full */
		private boolean isQueueStorageFull() {
			return queueLength() >= targetStorage();
		}

		/** Check if the ramp queue wait time is above target */
		private boolean isQueueWaitAboveTarget() {
			int wait_target = targetWaitTime();
			int wait_steps = steps(wait_target);
			int dem = Math.round(cumulativeDemand(wait_steps));
			return dem > passage_accum;
		}

		/** Calculate minimum rate (vehicles / hour) */
		private int calculateMinimumRate() {
			if(passage_failure) {
				limit_control = MinimumRateLimit.pf;
				return target_demand;
			} else {
				int r = queueStorageLimit();
				limit_control = MinimumRateLimit.sl;
				int rr = queueWaitLimit();
				if(rr > r) {
					r = rr;
					limit_control = MinimumRateLimit.wl;
				}
				rr = targetMinRate();
				if(rr > r) {
					r = rr;
					limit_control = MinimumRateLimit.tm;
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
			float proj_arrive = volumePeriod(target_demand,
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
		private int maxWaitTime() {
			return Math.max(meter.getMaxWait(), 1);
		}

		/** Calculate target minimum rate.
		 * @return Target minimum rate (vehicles / hour). */
		private int targetMinRate() {
			return Math.round(target_demand * TARGET_MIN_RATIO);
		}

		/** Calculate target maximum rate.
		 * @return Target maxumum rate (vehicles / hour). */
		private int calculateMaximumRate() {
			int target_max = Math.round(target_demand *
				TARGET_MAX_RATIO);
			return filterRate(Math.max(target_max, minimumRate));
		}

		/** Calculate the metering rate */
		private void calculateMeteringRate() {
			StationNode bs = s_node.bottleneckStation();
			double k = calculateSegmentDensity(bs);
			double r = calculateRate(k);
			segment_k_hist.push(k);
			rate_hist.push(r);
			if(shouldMeter(bs))
				setRate(r);
		}

		/** Calculate the segment density from meter to bottleneck.
		 * @param bs Bottlneck (downstream) station node.
		 * @return Segment density (vehicles / mile) */
		private double calculateSegmentDensity(StationNode bs) {
			if(bs != null)
				return bs.calculateSegmentDensity(s_node);
			else
				return s_node.getAggregatedDensity();
		}

		/** Should we be metering?
		 * @param bs bottleneck station.
		 * @return true if the meter should be metering. */
		private boolean shouldMeter(StationNode bs) {
			switch(phase) {
			case not_started:
				return shouldStart(bs) && startMetering();
			case early_metering:
				return checkDoneEarlyMetering();
			case metering:
				return shouldContinueMetering();
			case stopped:
				return shouldRestart(bs) && restartMetering();
			default:
				return true;
			}
		}

		/** Check if initial metering should start.
		 * @param bs bottleneck station.
		 * @return true if metering should start. */
		private boolean shouldStart(StationNode bs) {
			return (bs != null) &&
			       (shouldStartFlow(START_STEPS) ||
			        shouldStartDensity(bs, START_STEPS_K));
		}

		/** Check if metering should restart (after stopping).
		 * @param bs bottleneck station.
		 * @return true if metering should restart. */
		private boolean shouldRestart(StationNode bs) {
			return (bs != null) &&
			       (shouldStartFlow(RESTART_STEPS) ||
			        shouldStartDensity(bs, RESTART_STEPS));
		}

		/** Check if metering should start from flow.
		 * @param n_steps Number of steps to check.
		 * @return true if metering should start based on merge flow. */
		private boolean shouldStartFlow(int n_steps) {
			if(countRateHistory() >= n_steps) {
				for(int i = 0; i < n_steps; i++) {
					double q = getFlow(i);
					double rate = getRate(i);
					if(q < START_FLOW_RATIO * rate)
						return false;
				}
				return true;
			} else
				return false;
		}

		/** Check if metering should start from density.
		 * @param bs Bottleneck station.
		 * @param n_steps Number of steps to check.
		 * @return true if metering should start, based on segment
		 *         density. */
		private boolean shouldStartDensity(StationNode bs, int n_steps){
			assert s_node != null;
			if(countRateHistory() >= n_steps) {
				for(int i = 0; i < n_steps; i++) {
					double k = bs.calculateSegmentDensity(
						s_node, i);
					if(k < K_BOTTLENECK)
						return false;
				}
				return true;
			} else
				return false;
		}

		/** Start metering */
		private boolean startMetering() {
			phase = MeteringPhase.early_metering;
			resetAccumulators();
			isMeteringStarted = true;
			return true;
		}

		/** Retart metering */
		private boolean restartMetering() {
			phase = MeteringPhase.metering;
			resetAccumulators();
			return true;
		}

		/** Check if we're done with early metering.
		 * @return true if metering should continue. */
		private boolean checkDoneEarlyMetering() {
			if(isSegmentDensityTrendingDown() ||
			   isSegmentDensityLow())
			{
				phase = MeteringPhase.metering;
			}
			return true;
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
		 * @return true if metering should continue. */
		private boolean shouldContinueMetering() {
			/* Invert stop metering logic */
			return !(shouldStop() && stopMetering());
		}

		/** Check if ramp meter should stop metering.
		 * @return true if metering should stop. */
		private boolean shouldStop() {
			boolean bn = hasBottleneck();
			updateNoBottleneckCount(bn);
			if(isSegmentDensityHigh())
				return false;
			if(bn)
				return shouldStopFlow();
			else
				return noBottleneckCount >= STOP_STEPS;
		}

		/** Check if there is a bottleneck downstream of meter */
		private boolean hasBottleneck() {
			return (s_node != null) &&
			       (s_node.bottleneckStation() != null);
		}

		/** Update the "no bottleneck" count */
		private void updateNoBottleneckCount(boolean bn) {
			if(bn)
				noBottleneckCount = 0;
			else
				noBottleneckCount++;
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

		/** Check if metering should stop from flow.
		 * @return true if metering should stop, based on merge flow. */
		private boolean shouldStopFlow() {
			if(countRateHistory() >= STOP_STEPS) {
				for(int i = 0; i < STOP_STEPS; i++) {
					double q = getFlow(i);
					double rate = getRate(i);
					if(q > rate)
						return false;
				}
				return true;
			} else
				return false;
		}

		/** Stop metering.
		 * @return true if metering should stop (always). */
		private boolean stopMetering() {
			phase = MeteringPhase.stopped;
			currentRate = 0;
			resetAccumulators();
			rate_hist.clear();
			noBottleneckCount = 0;
			return true;
		}

		/** Set metering rate.
		 * @param rn Next metering rate. */
		private void setRate(double rn) {
			int r = (int)Math.round(rn);
			r = Math.max(r, minimumRate);
			r = Math.min(r, maximumRate);
			currentRate = r;
			meter.setRatePlanned(currentRate);
		}

		/** Count length of metering rate history */
		private int countRateHistory() {
			return rate_hist.size();
		}

		/** Get historical ramp flow.
		 * @param prevStep Time step in past.
		 * @param secs Number of seconds to average.
		 * @return ramp flow at 'prevStep' time steps ago. */
		private int getFlow(int prevStep, int secs) {
			Double p = passage_hist.average(prevStep, steps(secs));
			if(p != null)
				return (int)Math.round(p);
			else
				return getMaxRelease();
		}

		/** Get historical ramp flow.
		 * @param prevStep Time step in past.
		 * @return ramp flow at 'prevStep' time steps ago. */
		private int getFlow(int prevStep) {
			return getFlow(prevStep, 30);
		}

		/** Get current metering rate
		 * @return metering rate */
		private int getRate() {
			int r = currentRate;
			return r > 0 ? r : getFlow(0, 90);
		}

		/** Get metering rate at 'prevStep' time steps ago
		 * @return metering rate */
		private double getRate(int prevStep) {
			return rate_hist.get(prevStep);
		}

		/** Get segment density at 'prevStep' time steps ago.
		 * @param prevStep Number of time steps ago.
		 * @return segment density, or null for missing data. */
		private Double getSegmentDensity(int prevStep) {
			return segment_k_hist.get(prevStep);
		}

		/** Get the minimum metering rate.
		 * @return minimum metering rate */
		private int getMinimumRate() {
			return minimumRate;
		}

		/** Return maximum metering rate */
		private int getMaximumRate() {
			return maximumRate;
		}

		/**
		 * Calculate metering rate.
		 * <pre>
		 *                       ^
		 *                       | k
		 *                       |                 +
		 *                       |
		 *                       |
		 *                       |             +
		 *                       |
		 *                       +
		 *                       |
		 *                       |
		 *                 +     |
		 *    +                  |
		 * --p0------------p1----p2------------p3---p4-----&gt; K_DES-k
		 *                       |
		 *                       |
		 * p0's x = K_DES - K_JAM
		 * p2's x = 0
		 * p4's x = K_DES
		 * </pre>
		 *
		 * @param k Segment density.
		 * @return Metering rate (vehicles per hour).
		 */
		private double calculateRate(double k) {
			double Rmin = getMinimumRate();
			double Rmax = getMaximumRate();
			double Rt = getRate();

			// Calculate MainLine Alpha value with MainLine Density
			double x = K_DES - k;

			KPoint p0 = new KPoint(K_DES - K_JAM, Rmin / Rt);
			KPoint p1 = new KPoint((K_DES - K_JAM) / 3,
				Rmin / Rt + (1 - Rmin / Rt) / 3);
			KPoint p2 = new KPoint(0, 1);
			if(Rmin >= Rt)
				p0.y = p1.y = p2.y = Rmin / Rt;
			KPoint p4 = new KPoint(K_DES, Rmax / Rt);

			// Mainline graph connection 2 points
			double alpha = calculateAlpha(p0, p2, p4, x);

			// Ramp meter rate for next time interval
			double Rnext = Rt * alpha;

			// Check minimum / max rate
			Rnext = Math.max(Rnext, Rmin);
			Rnext = Math.min(Rnext, Rmax);

			if(Rt == 0)
				Rnext = Rmax;

			return Rnext;
		}

		/** Get a string representation of a meter state */
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(meter.name);
			sb.append(",");
			sb.append(phase);
			sb.append(",");
			sb.append(getQueueState());
			sb.append(",dem=");
			sb.append(Math.round(cumulativeDemand()));
			sb.append(",pas=");
			sb.append(passage_accum);
			sb.append(",grn=");
			sb.append(green_accum);
			sb.append(",min[");
			sb.append(limit_control);
			sb.append("]=");
			sb.append(minimumRate);
			sb.append(",max=");
			sb.append(maximumRate);
			sb.append(",rate=");
			sb.append(currentRate);
			sb.append(",q(");
			sb.append(queueEmptyCount);
			sb.append(",");
			sb.append(queueFullCount);
			sb.append("),");
			sb.append(s_node);
			if(s_node != null) {
				StationNode bs = s_node.bottleneckStation();
				if(bs != null) {
					// copied from shouldStart...
					boolean sf = shouldStartFlow(
						START_STEPS);
					boolean sd = shouldStartDensity(bs,
						START_STEPS_K);
					sb.append(",bs=");
					sb.append(bs);
					sb.append(",");
					sb.append(getSegmentDensity(0));
					sb.append(",");
					if(sf)
						sb.append("flow");
					sb.append(",");
					if(sd)
						sb.append("density");
				}
			}
			return sb.toString();
		}
	}

	/** Class : Point */
	class KPoint {

		double x;
		double y;

		public KPoint(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	/** Calculate Alpha Value */
	static private double calculateAlpha(KPoint P0, KPoint P2, KPoint P4,
		double x)
	{
		KPoint start = P0, end = P2;
		if(x >= 0) {
			start = P2;
			end = P4;
		} else {
			start = P0;
			end = P2;
		}
		double yd = end.y - start.y;
		double xd = end.x - start.x;
		if(xd != 0)
			return (yd / xd) * (x - start.x) + start.y;
		else
			return 0;
	}
}
