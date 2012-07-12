/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2012  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Interval;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterQueue;
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
	static private final IDebugLog ALG_LOG = new IDebugLog("kadaptive");

	/** Number of seconds for one time step */
	static private final int STEP_SECONDS = 30;

	/** Bottleneck density (vehicles / mile) */
	static private final int K_BOTTLENECK = 30;

	/** Critical density (vehicles / mile) */
	static private final int K_CRIT = 40;

	/** Desired density (vehicles / mile) */
	static private final double K_DES = K_CRIT * 0.8;

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
			float period = n_steps * STEP_SECONDS;
			float hour_frac = Interval.HOUR / period;
			return Math.round(vol * hour_frac);
		} else
			return MISSING_DATA;
	}

	/** Convert step volume count to flow rate (vehicles / hour) */
	static private int flowRate(float vol) {
		return flowRate(vol, 1);
	}

	/** Convert flow rate to volume for a given period.
	 * @param flow Flow rate to convert (vehicles / hour).
	 * @param period Period for volume (seconds).
	 * @return Volume over given period. */
	static private float volumePeriod(int flow, int period) {
		if(flow >= 0 && period > 0) {
			float hour_frac = (float)period / Interval.HOUR;
			return flow * hour_frac;
		} else
			return MISSING_DATA;
	}

	/** States for all K adaptive algorithms */
	static protected HashMap<String, KAdaptiveAlgorithm> ALL_ALGS =
		new HashMap<String, KAdaptiveAlgorithm>();

	/** Lookup the K adaptive algorithm state for one corridor */
	static public KAdaptiveAlgorithm lookupCorridor(Corridor c) {
		KAdaptiveAlgorithm alg = ALL_ALGS.get(c.getID());
		if(alg == null) {
			alg = new KAdaptiveAlgorithm(c);
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
			if(alg.isDone())
				it.remove();
		}
	}

	/** Corridor */
	protected final Corridor corridor;

	/** Is started metering in this corridor? */
	protected boolean isMeteringStarted = false;

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
	public RampMeterQueue getQueueState(RampMeterImpl meter) {
		MeterState ms = getMeterState(meter);
		if(ms != null)
			return ms.getQueueState();
		else
			return RampMeterQueue.UNKNOWN;
	}

	/** Get the meter state for a given ramp meter */
	private MeterState getMeterState(RampMeterImpl meter) {
		if(meter.getCorridor() != corridor) {
			// Meter must have been changed to a different
			// corridor; throw away old meter node
			meterStates.remove(meter.getName());
			return null;
		}
		MeterState ms = lookupMeterState(meter);
		if(ms != null)
			return ms;
		EntranceNode en = findEntranceNode(meter);
		if(en != null) {
			ms = new MeterState(meter, en);
			meterStates.put(meter.getName(), ms);
		}
		return ms;
	}

	/** Lookup the meter state for a specified meter */
	private MeterState lookupMeterState(RampMeter meter) {
		return meterStates.get(meter.getName());
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
	protected void processInterval() {
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			sn.updateState();
		}
		findBottlenecks();
		calculateMeteringRates();
		if(isMeteringStarted && !doStopChecking)
			checkCorridorState();
		else if(doStopChecking)
			checkStopCondition();
		clearBottlenecks();
	}

	/** Debug corridor structure. */
	private void debug() {
		log("-------- Corridor Structure --------");
		for(Node n = head; n != null; n = n.downstream)
			log(n.toString());
	}

	/** Log one message */
	private void log(String msg) {
		ALG_LOG.log(corridor.getName() + ": " + msg);
	}

	/**
	 * Find bottlenecks.
	 */
	private void findBottlenecks() {
		findBottleneckCandidates();
		mergeBottleneckZones();
	}

	/**
	 * Find bottleneck candidates.
	 */
	private void findBottleneckCandidates() {
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			sn.checkBottleneck();
		}
	}

	/**
	 * Merge zone by distnace and acceleration.
	 * Iterate from downstream to upstream.
	 */
	private void mergeBottleneckZones() {
		for(StationNode sn = lastStation(); sn != null;
		    sn = sn.upstreamStation())
		{
			if(sn.isBottleneck)
				checkBottleneckMerge(sn);
		}
	}

	/**
	 * Check if a bottleneck station should be merged.
	 */
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

	/**
	 * Check corridor average density condition.
	 */
	private void checkCorridorState() {
		int bottleneckCount = 0;
		StationNode downstreamBS = null;
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			if(sn.isBottleneck) {
				downstreamBS = sn;
				bottleneckCount++;
			}
		}
		k_hist_corridor.push(calculateCorridorDensity(downstreamBS));
		if(bottleneckCount > 1 || !k_hist_corridor.isFull())
			return;
		// check avg K of corridor average density
		for(int i = 0; i < AVG_K_TREND_STEPS; i++) {
			Double ma_next = k_hist_corridor.average(i,
				AVG_K_STEPS);
			Double ma_prev = k_hist_corridor.average(i + 1,
				AVG_K_STEPS);
			if(ma_next == null || ma_prev == null)
				return;
			if(ma_next > ma_prev)
				return;
		}

		// let's check stop condition from now
		doStopChecking = true;
	}

	/**
	 * Calculate the average density of a corridor up to a bottleneck.
	 * @param bottleneck Bottleneck station.
	 * @return Average density up to the bottleneck.
	 */
	private double calculateCorridorDensity(StationNode bottleneck) {
		if(bottleneck != null) {
			return bottleneck.calculateSegmentDensity(
				firstStation());
		}
		return 0;
	}

	/**
	 * Get the furthest upstream station node.
	 */
	private StationNode firstStation() {
		for(Node n = head; n != null; n = n.downstream) {
			if(n instanceof StationNode)
				return (StationNode) n;
		}
		return null;
	}

	/**
	 * Get the furthest downstream station node.
	 */
	private StationNode lastStation() {
		for(Node n = tail; n != null; n = n.upstream) {
			if(n instanceof StationNode)
				return (StationNode) n;
		}
		return null;
	}

	/** Get the density for determining a bottleneck */
	private double bottleneckDensity() {
		return doStopChecking ? K_DES : K_BOTTLENECK;
	}

	/** Get number of time steps to check for bottleneck */
	private int bottleneckTrendSteps() {
		return doStopChecking ? BOTTLENECK_TREND_2_STEPS :
		                        BOTTLENECK_TREND_1_STEPS;
	}

	/**
	 * Stop metering of ramp meter satisfying conditions
	 */
	private void checkStopCondition() {
		boolean hasBottleneck = false;
		for(StationNode sn = lastStation(); sn != null;
		    sn = sn.upstreamStation())
		{
			hasBottleneck = checkStopCondition(sn, hasBottleneck);
		}
	}

	/** Check stop condition for one station. */
	private boolean checkStopCondition(StationNode sn,
		boolean hasBottleneck)
	{
		if(sn.isBottleneck) {
			for(MeterState ms : sn.getMeters())
				ms.resetNoBottleneckCount();
			return true;
		} else {
			for(MeterState ms : sn.getMeters())
				ms.checkStopCondition(hasBottleneck);
			return hasBottleneck;
		}
	}

	/**
	 * Calculate metering rates for all bottlenecks
	 */
	private void calculateMeteringRates() {
		for(StationNode sn = lastStation(); sn != null;
		    sn = sn.upstreamStation())
		{
			if(sn.isBottleneck)
				calculateMeteringRates(sn);
		}

		// when meter is not in zone
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			defaultMetering(sn);
		}
	}

	/**
	 * Calculate metering rates for a bottleneck
	 * @param bottleneck bottleneck station
	 */
	private void calculateMeteringRates(StationNode bottleneck) {

		// calculate rates for entrance associated with bottleneck
		for(MeterState ms : bottleneck.getMeters()) {
			double Rnext = equation(bottleneck, ms);
			ms.saveRateHistory(Rnext);
			if(ms.shouldMeter(bottleneck, null))
				ms.setRate(Rnext);
		}

		// calculate rates from upstream station of bottleneck
		// to upstream boundary or next bottleneck
		for(StationNode sn = bottleneck.upstreamStation(); sn != null;
		    sn = sn.upstreamStation())
		{
			if(sn.isBottleneck)
				break;
			calculateMeteringRates(sn, bottleneck);
		}
	}

	/**
	 * Calculate metering rates from one node to a bottleneck
	 */
	private void calculateMeteringRates(StationNode upStation,
		StationNode bottleneck)
	{
		for(MeterState ms : upStation.getMeters()) {
			double Rnext = equation(bottleneck, upStation, ms);
			ms.saveRateHistory(Rnext);
			if(ms.shouldMeter(bottleneck, upStation))
				ms.setRate(Rnext);
		}
	}

	/**
	 * Calculate metering rates
	 * @param stationState station state
	 * @param ms Meter state
	 * @return next metering rate
	 */
	private double equation(StationNode stationState, MeterState ms) {
		return equation(stationState, null, ms);
	}

	/**
	 * Calculate metering rates.
	 * <pre>
	 *                       ^
	 *                       | Kt
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
	 * --p0------------p1----p2------------p3---p4-----&gt; K_DES-Kt
	 *                       |
	 *                       |
	 * p0's x = K_DES - K_JAM
	 * p2's x = 0
	 * p4's x = K_DES
	 * </pre>
	 *
	 * @param bottleneck Bottleneck station.
	 * @param upstream Upstream station (may be null).
	 * @param ms Meter state.
	 * @return Metering rate (vehicles per hour).
	 */
	private double equation(StationNode bottleneck, StationNode upstream,
		MeterState ms)
	{
		double Kt = bottleneck.calculateSegmentDensity(upstream);

		ms.saveSegmentDensity(Kt);

		double Rmin = ms.getMinimumRate();
		double Rmax = ms.getMaximumRate();
		double Rt = ms.getRate();

                // Calculate MainLine Alpha value with MainLine Density
		double x = K_DES - Kt;

		KPoint p0 = new KPoint(K_DES - K_JAM, Rmin / Rt);
		KPoint p1 = new KPoint((K_DES - K_JAM) / 3,
			Rmin / Rt + (1 - Rmin / Rt) / 3);
		KPoint p2 = new KPoint(0, 1);
		if(Rmin >= Rt)
			p0.y = p1.y = p2.y = Rmin / Rt;
		KPoint p4 = new KPoint(K_DES, Rmax / Rt);

                // Mainline graph connection 2 points
		double alpha = getAlpha(p0, p2, p4, x);

		// Ramp meter rate for next time interval
		double Rnext = Rt * alpha;

		// Check minimum / max rate
		Rnext = Math.max(Rnext, Rmin);
		Rnext = Math.min(Rnext, Rmax);

		if(Rt == 0)
			Rnext = Rmax;

		return Rnext;
	}

	/** Calculate Alpha Value */
	private double getAlpha(KPoint P0, KPoint P2, KPoint P4, double x) {
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

	/**
	 * Calculate metering rates for all operating ramp but not in zone
	 *   - do local metering
	 * @param stationState station state
	 */
	private void defaultMetering(StationNode stationState) {
		for(MeterState ms : stationState.getMeters()) {
			if(ms.isMetering && !ms.isRateUpdated) {
				double Rnext = equation(stationState, null, ms);
				ms.setRate(Rnext);
			}
		}
	}

	/**
	 * Clear all bottleneck state.
	 */
	private void clearBottlenecks() {
		for(Node n = head; n != null; n = n.downstream)
			n.clearBottleneck();
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

		/** Clear bottleneck state */
		abstract protected void clearBottleneck();

		/**
		 * Return next upstream station node.
		 * @return Upstream station node.
		 */
		protected StationNode upstreamStation() {
			for(Node n = upstream; n != null; n = n.upstream) {
				if(n instanceof StationNode)
					return (StationNode) n;
			}
			return null;
		}

		/**
		 * Return next downstream station node.
		 * @return Downstream station node.
		 */
		protected StationNode downstreamStation() {
			for(Node n = downstream; n != null; n = n.downstream) {
				if(n instanceof StationNode)
					return (StationNode) n;
			}
			return null;
		}
	}

	/** Node to manage station on corridor */
	class StationNode extends Node {

		/** StationImpl mapping this state */
		protected final StationImpl station;

		/** Associated meters */
		private final ArrayList<MeterState> associatedMeters =
			new ArrayList<MeterState>();

		/** Speed history */
		private final BoundedSampleHistory speedHist =
			new BoundedSampleHistory(steps(60));

		/** Density history */
		private final BoundedSampleHistory densityHist =
			new BoundedSampleHistory(steps(300));

		/** Is bottleneck ? */
		private boolean isBottleneck = false;

		/** Is bottleneck at previous time step? */
		private boolean isPrevBottleneck = false;

		/**
		 * Create a new station node.
		 * @param rnode
		 */
		public StationNode(R_NodeImpl rnode, float m, Node up,
			StationImpl stat)
		{
			super(rnode, m, up);
			station = stat;
		}

		/**
		 * Update station state.
		 * It must be called before finding bottleneck.
		 */
		public void updateState() {
			densityHist.push((double)station.getDensity());
			speedHist.push((double)station.getSpeed());
		}

		/**
		 * Check if the station is a bottleneck.
		 */
		protected void checkBottleneck() {
			double kb = bottleneckDensity();
			if(getAggregatedDensity() >= kb) {
				if(isPrevBottleneck || isDensityIncreasing(kb))
					isBottleneck = true;
			}
		}

		/**
		 * Check if density has been increasing for a number of steps
		 * or that all previous steps are high.
		 */
		private boolean isDensityIncreasing(final double kb) {
			boolean increasing = true;
			boolean high_k = true;
			for(int i = 0; i < bottleneckTrendSteps(); i++) {
				double k = getAggregatedDensity(i);
				double pk = getAggregatedDensity(i + 1);
				if(k < pk)
					increasing = false;
				if(k < kb || pk < kb)
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

		/**
		 * Get average density of a mainline segment ending at the
		 * current station.
		 * @param upStation upstream station of segment.
		 * @return average density (distance weight).
		 */
		private double calculateSegmentDensity(StationNode upStation) {
			return calculateSegmentDensity(upStation, 0);
		}

		/**
		 * Get average density of a mainline segment ending at the
		 * current station.  This works by splitting each "segment"
		 * between stations into 3 equal lengths and assigning average
		 * density to the middle sub-segment.
		 *
		 * @param upStation upstream station of segment.
		 * @param prevStep previous time steps (0 for current).
		 * @return average density (distance weight).
		 */
		private double calculateSegmentDensity(StationNode upStation,
			int prevStep)
		{
			if(upStation == null || upStation == this)
				return getAggregatedDensity(prevStep);
			StationNode cursor = upStation;
			double dist_sum3 = 0;
			double k_sum3 = 0;
			double k_cursor = cursor.getAggregatedDensity(prevStep);
			for(StationNode down = cursor.downstreamStation();
			    down != null && cursor != this;
			    down = down.downstreamStation())
			{
				double k_down = down.getAggregatedDensity(
					prevStep);
				double k_middle = (k_cursor + k_down) / 2;
				double distance = cursor.distanceMiles(down);
				dist_sum3 += distance * 3;
				k_sum3 += (k_cursor + k_middle + k_down) *
					distance;
				cursor = down;
				k_cursor = k_down;
			}
			if(dist_sum3 > 0)
				return k_sum3 / dist_sum3;
			else
				return 0;
		}

		/**
		 * Return aggregated density at current time step
		 * @return average 1min density
		 */
		public double getAggregatedDensity() {
			return getAggregatedDensity(0);
		}

		/**
		 * Return aggregated density at 'prevStep' time steps ago
		 * @return average 1 min density at 'prevStep' time steps ago
		 */
		public double getAggregatedDensity(int prevStep) {
			Double avg = densityHist.average(prevStep,steps(60));
			if(avg != null)
				return avg;
			else
				return 0;
		}

		/**
		 * Return aggregated speed at current time step
		 * @return average 1min speed
		 */
		public double getAggregatedSpeed() {
			Double avg = speedHist.average(0, steps(60));
			if(avg != null)
				return avg;
			else
				return 0;
		}

		/**
		 * Return acceleration from current station to down station
		 * @return acceleration from current station to down station
		 */
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

		/**
		 * Return associated meters list.
		 * @return associated meters list.
		 */
		public ArrayList<MeterState> getMeters() {
			return associatedMeters;
		}

		/** Get a string representation of a station node */
		public String toString() {
			return "Station " + station.getName();
		}
	}

	/** Node to manage entrance onto corridor */
	class EntranceNode extends Node {

		/** Create a new entrance node */
		public EntranceNode(R_NodeImpl rnode, float m, Node prev) {
			super(rnode, m, prev);
		}

		/** Clear bottleneck state */
		protected void clearBottleneck() { }

		/** Get a string representation of an entrance node */
		public String toString() {
			return "Entrance " + rnode.getName();
		}
	}

	/** Ramp meter state */
	class MeterState {

		/** Meter at this entrance */
		private final RampMeterImpl meter;

		/** Entrance node for the meter */
		private final EntranceNode node;

		/** Station node association */
		private final StationNode s_node;

		/** Is it metering? */
		private boolean isMetering = false;

		/** Has been stopped before */
		private boolean hasBeenStoped = false;

		/** Is metering rate updated at current time step? */
		private boolean isRateUpdated = false;

		/** Current metering rate (vehicles / hour) */
		private int currentRate = 0;

		/** Minimum metering rate (vehicles / hour) */
		private int minimumRate = 0;

		/** Maximum metering rate (vehicles / hour) */
		private int maximumRate = 0;

		/** How many time steps there's no bottleneck at downstream */
		private int noBottleneckCount = 0;

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
		private final BoundedSampleHistory passageHist =
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
		private final BoundedSampleHistory rateHist =
			new BoundedSampleHistory(MAX_STEPS);

		/** Segment density history (vehicles / mile) */
		private final BoundedSampleHistory segmentDensityHist =
			new BoundedSampleHistory(MAX_STEPS);

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
			if(s_node != null)
				s_node.associatedMeters.add(this);
		}

		/** Get station to associate with the meter state.
		 * @return Associated station node, or null. */
		private StationNode getAssociatedStation() {
			StationNode us = getAssociatedUpstream();
			StationNode ds = node.downstreamStation();
			return isCloser(us, ds) ? ds : us;
		}

		/** Get associated upstream station */
		private StationNode getAssociatedUpstream() {
			StationNode us = node.upstreamStation();
			if(isUpstreamStationOk(us))
				return us;
			else
				return null;
		}

		/** Check if an upstream station is OK */
		private boolean isUpstreamStationOk(StationNode us) {
			return us != null &&
			       node.distanceMiles(us) < UPSTREAM_STATION_MILES;
		}

		/** Test if downstream station should be associated */
		private boolean isCloser(StationNode us, StationNode ds) {
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

		/**
		 * Validate meter state.
		 *   - Save cumulative demand and merging flow
		 *   - Set current demand and merging flow
		 *   - Calculate minimum metering rate
		 */
		private void validate() {
			isRateUpdated = false;
			updateDemandState();
			updatePassageState();
			checkQueueEmpty();
			minimumRate = calculateMinimumRate();
			maximumRate = calculateMaximumRate();
		}

		/** Update ramp queue demand state */
		private void updateDemandState() {
			float demand_vol = calculateQueueDemand();
			double demand_rate = flowRate(demand_vol);
			demandHist.push(demand_rate);
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
				int target = getDefaultTarget(meter);
				return volumePeriod(target, STEP_SECONDS);
			}
		}

		/** Estimate the queue undercount (vehicles) */
		private float estimateQueueUndercount() {
			float full_secs = queueFullCount * STEP_SECONDS;
			float r = Math.min(2 * full_secs / maxWaitTime(), 1);
			float under = maxStorage() - queueLength();
			return Math.max(r * under, 1);
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
				return getDefaultTarget(meter);
		}

		/** Get the default target metering rate (vehicles / hour) */
		private int getDefaultTarget(RampMeterImpl m) {
			if(m != null) {
				int t = m.getTarget();
				if(t > 0)
					return t;
			}
			return getMaxRelease();
		}

		/** Update ramp passage output state */
		private void updatePassageState() {
			int passage_vol = calculatePassageCount();
			if(passage_vol < 0)
				passage_failure = true;
			double passage_rate = flowRate(passage_vol);
			passageHist.push(passage_rate);
			passage_accum += passage_vol;
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
			if(isMetering) {
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
			return (!isQueueOccupancyHigh()) &&
			       (isDemandBelowPassage() ||isPassageBelowGreen());
		}

		/** Check if cumulative demand is below cumulative passage */
		private boolean isDemandBelowPassage() {
			return queueLength() < QUEUE_EMPTY_THRESHOLD;
		}

		/** Check if cumulative passage is below cumulative green */
		private boolean isPassageBelowGreen() {
			return violationCount() < QUEUE_EMPTY_THRESHOLD;
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
			RampMeterImpl m = meter;
			if(m != null && isMetering) {
				if(isQueueEmpty())
					return RampMeterQueue.EMPTY;
				else if(isQueueFull())
					return RampMeterQueue.FULL;
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
			RampMeterImpl m = meter;
			if(m != null)
				return m.getMaxWait();
			else
				return RampMeterImpl.DEFAULT_MAX_WAIT;
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

		/** Check if queue occupancy is above threshold */
		private boolean isQueueOccupancyHigh() {
			return queue.getMaxOccupancy() > QUEUE_OCC_THRESHOLD;
		}

		/**
		 * Set metering rate
		 * @param Rnext next metering rate
		 */
		private void setRate(double Rnext) {
			int r = (int)Math.round(Rnext);
			r = Math.max(r, minimumRate);
			r = Math.min(r, maximumRate);
			currentRate = r;
			RampMeterImpl m = meter;
			if(m != null) {
				isRateUpdated = true;
				m.setRatePlanned(currentRate);
			}
		}

		/** Should metering be started?
		 * @param bs bottleneck station
		 * @param us associated station of entrance
		 * @return true if metering should be started.
		 */
		private boolean shouldMeter(StationNode bs, StationNode us) {
			if(isMetering)
				return true;
			if(shouldStart(bs, us)) {
				startMetering();
				isMeteringStarted = true;
				return true;
			} else
				return false;
		}

		/** Check if metering should start.
		 * @return true if metering should start.
		 */
		private boolean shouldStart(StationNode bs, StationNode us) {
			if(hasBeenStoped)
				return shouldRestart(bs, us);
			else
				return shouldStartInitial(bs, us);
		}

		/** Check if initial metering should start.
		 * @return true if metering should start.
		 */
		private boolean shouldStartInitial(StationNode bs,
			StationNode us)
		{
			if(shouldStartFlow(START_STEPS) ||
			   shouldStartDensity(bs, us, START_STEPS_K))
				return true;
			else
				return false;
		}

		/** Check if metering should restart (after stopping).
		 * @return true if metering should restart.
		 */
		private boolean shouldRestart(StationNode bs, StationNode us) {
			if(shouldStartFlow(RESTART_STEPS) ||
			   shouldStartDensity(bs, us, RESTART_STEPS))
				return true;
			else
				return false;
		}

		/** Check if initial metering should start from flow.
		 * @param n_steps Number of steps to check.
		 * @return true if metering should start, based on merge flow.
		 */
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
		 * @param us Upstream station.
		 * @param n_steps Number of steps to check.
		 * @return true if metering should start, based on segment
		 *         density.
		 */
		private boolean shouldStartDensity(StationNode bs,
			StationNode us, int n_steps)
		{
			if(countRateHistory() >= n_steps) {
				for(int i = 0; i < n_steps; i++) {
					double k = bs.calculateSegmentDensity(
						us, i);
					if(k < bottleneckDensity())
						return false;
				}
				return true;
			} else
				return false;
		}

		/**
		 * Check if the metering should be stopped.
		 */
		public void checkStopCondition(boolean hasBottleneck) {
			if(!isMetering)
				return;
			if(countRateHistory() < STOP_STEPS)
				return;
			if(isSegmentDensityHigh())
				return;
			if(!hasBottleneck) {
				addNoBottleneckCount();
				if(getNoBottleneckCount() >= STOP_STEPS)
					stopMetering();
				return;
			}
			resetNoBottleneckCount();
			if(satisfiesRateCondition())
				stopMetering();
		}

		/**
		 * Check if the segment density is high.
		 */
		private boolean isSegmentDensityHigh() {
			for(int i = 0; i < STOP_STEPS; i++) {
				Double sk = getSegmentDensity(i);
				if(sk != null && sk > K_DES)
					return true;
			}
			return false;
		}

		/**
		 * Check if the metering rate has been less than flow for
		 * STOP_STEPS time intervals.
		 */
		private boolean satisfiesRateCondition() {
			for(int i = 0; i < STOP_STEPS; i++) {
				double q = getFlow(i);
				double rate = getRate(i);
				if(q > rate)
					return false;
			}
			return true;
		}

		/**
		 * Stop metering.
		 */
		private void stopMetering() {
			isMetering = false;
			currentRate = 0;
			resetAccumulators();
			rateHist.clear();
			noBottleneckCount = 0;
			hasBeenStoped = true;
		}

		/**
		 * Save metering rate history
		 * @param Rnext
		 */
		private void saveRateHistory(double Rnext) {
			rateHist.push(Rnext);
		}

		/**
		 * Return length of metering rate history
		 * @return
		 */
		private int countRateHistory() {
			return rateHist.size();
		}

		/**
		 * Get historical ramp flow.
		 * @param prevStep Time step in past.
		 * @param secs Number of seconds to average.
		 * @return ramp flow at 'prevStep' time steps ago
		 */
		private int getFlow(int prevStep, int secs) {
			Double p = passageHist.average(prevStep, steps(secs));
			if(p != null)
				return (int)Math.round(p);
			else
				return getMaxRelease();
		}

		/**
		 * Get historical ramp flow.
		 * @param prevStep Time step in past.
		 * @return ramp flow at 'prevStep' time steps ago
		 */
		private int getFlow(int prevStep) {
			return getFlow(prevStep, 30);
		}

		/**
		 * Get current metering rate
		 * @return metering rate
		 */
		private int getRate() {
			int r = currentRate;
			return r > 0 ? r : getFlow(0, 90);
		}

		/**
		 * Return current metering rate at 'prevStep' time steps ago
		 * @return metering rate
		 */
		private double getRate(int prevStep) {
			return rateHist.get(prevStep);
		}

		/** Start metering */
		private void startMetering() {
			isMetering = true;
			resetAccumulators();
		}

		/**
		 * Save segment density.
		 * @param Kt
		 */
		private void saveSegmentDensity(double Kt) {
			segmentDensityHist.push(Kt);
		}

		/**
		 * Return segment density at 'prevStep' time steps ago
		 * @param prevStep
		 * @return segment density at 'prevStep' time steps ago
		 */
		private double getSegmentDensity(int prevStep) {
			return segmentDensityHist.get(prevStep);
		}

		/**
		 * Return minimum metering rate
		 * @return minimum metering rate
		 */
		private int getMinimumRate() {
			return minimumRate;
		}

		/** Return maximum metering rate */
		private int getMaximumRate() {
			return maximumRate;
		}

		/**
		 * Reset no bottleneck count
		 */
		private void resetNoBottleneckCount() {
			noBottleneckCount = 0;
		}

		/**
		 * Add no bottleneck count
		 */
		public void addNoBottleneckCount() {
			noBottleneckCount++;
		}

		/**
		 * Return no bottleneck count
		 * @return no-bottleneck count
		 */
		public int getNoBottleneckCount() {
			return noBottleneckCount;
		}

		/** Get a string representation of a meter state */
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(meter.name);
			sb.append(" ");
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
			return sb.toString();
		}
	}

	/**
	 * Class : Point
	 */
	class KPoint {

		double x;
		double y;

		public KPoint(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
