/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2012  Minnesota Department of Transportation
 * Copyright (C) 2011  University of Minnesota Duluth (NATSRL)
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
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.SystemAttributeHelper;

/**
 * Density-based Adaptive Metering with Variable Bottleneck
 * Metering Algorithm.
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Douglas Lau
 */
public class KAdaptiveAlgorithm implements MeterAlgorithmState {

	/** Algorithm debug log */
	static private final IDebugLog ALG_LOG = new IDebugLog("kadaptive");

	/** Number of seconds for one time step */
	static private final int STEP_SECONDS = 30;

	/** Calculate the number of steps for an interval */
	static private int steps(int seconds) {
		return seconds / STEP_SECONDS;
	}

	/** Bottleneck Density */
	static private final int K_BOTTLENECK = 25;

	/** Critical Density */
	static private final int K_CRIT = 40;

	/** Desired Density */
	static private final double K_DES = K_CRIT * 0.8;

	/** Jam Density */
	static private final int K_JAM = 180;

	/** Rate value for checking if metering is started */
	static private final double K_START_THRESH = 0.8;

	/** Acceleration threshold to decide bottleneck */
	static private final int A_BOTTLENECK = 1000;

	/** Number fo time steps to check before start metering */
	static private final int START_STEPS = steps(90);

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

	/** Number of steps for average density to check corridor state */
	static private final int AVG_K_STEPS = steps(900);

	/** Number of trend steps for average density to check corridor state */
	static private final int AVG_K_TREND_STEPS = steps(300);

	/** Factor to compute ramp demand from passage/merge flow */
	static private final double PASSAGE_DEMAND_FACTOR = 1.15;

	/** Get the absolute minimum release rate */
	static protected int getMinRelease() {
		return SystemAttributeHelper.getMeterMinRelease();
	}

	/** Get the absolute maximum release rate */
	static protected int getMaxRelease() {
		return SystemAttributeHelper.getMeterMaxRelease();
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

	/** Are station-entrance associated? */
	private boolean isAssociated = false;

	/** Corridor density history for triggering metering stop */
	private final BoundedSampleHistory k_hist_corridor =
		new BoundedSampleHistory(AVG_K_STEPS + AVG_K_TREND_STEPS);

	/** Hash map of ramp meter entrance nodes */
	private final HashMap<String, EntranceNode> meterNodes =
		new HashMap<String, EntranceNode>();

	/** Head (furthest upstream) node on corridor */
	private final Node head;

	/** Tail (furthest downstream) node on corridor */
	private final Node tail;

	/** Create a new KAdaptiveAlgorithm */
	private KAdaptiveAlgorithm(Corridor c) {
		corridor = c;
		head = createNodes();
		tail = head.tailNode();
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
		EntranceNode en = getEntranceNode(meter);
		if(en != null)
			en.updateState();
	}

	/** Get the entrance node for a given ramp meter */
	protected EntranceNode getEntranceNode(RampMeterImpl meter) {
		if(meter.getCorridor() != corridor) {
			// Meter must have been changed to a different
			// corridor; throw away old meter node
			meterNodes.remove(meter.getName());
			return null;
		}
		EntranceNode en = lookupEntranceNode(meter);
		if(en != null)
			return en;
		en = findEntranceNode(meter);
		if(en != null) {
			en.setMeter(meter);
			meterNodes.put(meter.getName(), en);
		}
		return en;
	}

	/** Lookup the entrance node for a specified meter */
	protected EntranceNode lookupEntranceNode(RampMeter meter) {
		return meterNodes.get(meter.getName());
	}

	/**
	 * Find an entrance node matching the given ramp meter.
	 */
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
		if(!isAssociated)
			doAssociateStationAndEntrance();
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

	/**
	 * Should be called after added all stations and meters
	 */
	private void doAssociateStationAndEntrance() {
		for(StationNode sn = firstStation(); sn != null;
		    sn = sn.downstreamStation())
		{
			sn.setAssociatedEntrances();
		}
		isAssociated = true;

		/** for debugging of corridor structure */
		if(ALG_LOG.isOpen())
			debug();
	}

	/** Debug algorithm. */
	private void debug() {
		ALG_LOG.log("Corridor Structure : " + corridor.getName() +
		            " --------------------");
		StringBuilder sb = new StringBuilder();
		for(Node n = head; n != null; n = n.downstream) {
			n.debug(sb);
			sb.append("\n");
		}
		ALG_LOG.log(sb.toString());
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
		k_hist_corridor.push(calculateAverageDensity(downstreamBS));
		if(bottleneckCount > 1 || !k_hist_corridor.isFull())
			return;
		// check avg K of corridor average density
		for(int i = 0; i < AVG_K_TREND_STEPS; i++) {
			Double ma_next = k_hist_corridor.average(i, AVG_K_STEPS);
			Double ma_prev = k_hist_corridor.average(i + 1, AVG_K_STEPS);
			if(ma_next == null || ma_prev == null)
				return;
			if(ma_next > ma_prev)
				return;
		}

		// let's check stop condition from now
		doStopChecking = true;
	}

	/**
	 * Calculate the average density up to a bottleneck.
	 * @param bottleneck Bottleneck station.
	 * @return Average density up to the bottleneck.
	 */
	private double calculateAverageDensity(StationNode bottleneck) {
		if(bottleneck != null) {
			StationNode sn = firstStation();
			if(sn != null)
				return getAverageDensity(sn, bottleneck);
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
	private boolean checkStopCondition(StationNode s,
		boolean hasBottleneck)
	{
		if(s.isBottleneck) {
			for(EntranceNode en : s.getAssociatedEntrances())
				en.resetNoBottleneckCount();
			return true;
		}
		for(EntranceNode en : s.getAssociatedEntrances())
			en.checkStopCondition(hasBottleneck);
		return hasBottleneck;
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
		for(EntranceNode en : bottleneck.getAssociatedEntrances()) {
			if(!en.hasMeter())
				continue;
			double Rnext = equation(bottleneck, en);
			en.saveRateHistory(Rnext);
			if(!checkStartCondition(bottleneck, null, en))
				continue;
			en.setBottleneck(bottleneck);
			en.setRate(Rnext);
		}

		// calculate rates
		// from upstream station of bottleneck to upstream boundary or next bottleneck
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
		for(EntranceNode en : upStation.getAssociatedEntrances()) {
			if(!en.hasMeter())
				continue;
			double Rnext = equation(bottleneck, upStation, en);
			en.saveRateHistory(Rnext);
			if(!checkStartCondition(bottleneck, upStation, en))
				continue;
			en.setBottleneck(bottleneck);
			en.setRate(Rnext);
		}
	}

	/**
	 * Calculate metering rates
	 * @param stationState station state
	 * @param entranceState entrance state
	 * @return next metering rate
	 */
	private double equation(StationNode stationState, EntranceNode entrance) {
		return equation(stationState, null, entrance);
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
	 * @param bottleneck
	 * @return
	 */
	private double equation(StationNode bottleneck, StationNode upstream,
		EntranceNode entrance)
	{
		double Kt = bottleneck.getAggregatedDensity();
		if(upstream != null)
			Kt = getAverageDensity(upstream, bottleneck);

		entrance.saveSegmentDensityHistory(Kt);

		double Rmin = entrance.getMinimumRate();
		double Rmax = getMaxRelease();
		double Rt = entrance.getRate();
		double x = K_DES - Kt;

		KPoint p0 = new KPoint(K_DES - K_JAM, Rmin / Rt);
		KPoint p1 = new KPoint((K_DES - K_JAM) / 3,
			Rmin / Rt + (1 - Rmin / Rt) / 3);
		KPoint p2 = new KPoint(0, 1);
		if(Rmin >= Rt)
			p0.y = p1.y = p2.y = Rmin / Rt;
		KPoint p4 = new KPoint(K_DES, Rmax / Rt);
		KPoint start = p0, end = p1;

		if(x >= 0) {
			start = p2;
			end = p4;
		} else {
			start = p0;
			end = p2;
		}

		// line graph connection 2 points
		double alpha = ((end.y - start.y) / (end.x - start.x)) * (x - start.x) + start.y;

		// Ramp meter rate for next time interval
		double Rnext = Rt * alpha;

		// Check minimum / max rate
		Rnext = Math.max(Rnext, Rmin);
		Rnext = Math.min(Rnext, Rmax);

		if(Rt == 0)
			Rnext = Rmax;

		return Rnext;
	}

	/**
	 * Does metering of given entrance start?
	 * @param bs bottleneck
	 * @param us associated station of entrance
	 * @param en target entrance
	 * @param Rnext calculated next metering rate
	 * @return start ?
	 */
	private boolean checkStartCondition(StationNode bs, StationNode us,
		EntranceNode en)
	{
		if(en.isMetering)
			return true;
		if(!en.hasBeenStoped)
			return checkStartFirst(bs, us, en);
		else
			return checkStartAfterStop(bs, us, en);
	}

	/**
	 * Check start condition (first time -- has not stopped previously).
	 * <pre>
	 *	SegmentDensity &gt;= bottleneckDensity (just once)
	 *	   - OR -
	 *	Merging flow &gt;= KstartRate * Allocated Rate (for n steps)
	 * </pre>
	 * Segment density is the average density from the upstream station to
	 * bottleneck.
	 */
	private boolean checkStartFirst(StationNode bs, StationNode us,
		EntranceNode en)
	{
		boolean satisfyDensityCondition = false;
		boolean satisfyRateCondition = false;

		double segmentDensity = 0;
		if(us != null)
			segmentDensity = getAverageDensity(us, bs);
		else
			segmentDensity = bs.getAggregatedDensity();

		if(segmentDensity >= bottleneckDensity())
			satisfyDensityCondition = true;

		if(en.countRateHistory() >= START_STEPS) {
			satisfyRateCondition = true;
			for(int i = 0; i < START_STEPS; i++) {
				double q = en.getFlow(i);
				double rate = en.getRate(i);
				if(q < K_START_THRESH * rate)
					satisfyRateCondition = false;
			}
		}

		if(satisfyRateCondition || satisfyDensityCondition) {
			en.startMetering();
			isMeteringStarted = true;
			return true;
		} else
			return false;
	}

	/**
	 * Check start condition (after stopping previously).
	 * <pre>
	 *	SegmentDensity &gt;= bottleneckDensity (for n steps)
	 *	   - AND -
	 *	Merging flow &gt;= KstartRate * Allocated Rate (for n times)
	 * </pre>
	 * Segment density is the average density from the upstream station to
	 * bottleneck.
	 */
	private boolean checkStartAfterStop(StationNode bs, StationNode us,
		EntranceNode en)
	{
		double segmentDensity = 0;

		// if rate history is short, pass (do not start)
		if(en.countRateHistory() < RESTART_STEPS)
			return false;

		for(int i = 0; i < RESTART_STEPS; i++) {
			if(us != null)
				segmentDensity = getAverageDensity(us, bs, i);
			else
				segmentDensity = bs.getAggregatedDensity(i);

			// Start Condition 1 : segment density > bottleneck K
			if(segmentDensity < bottleneckDensity())
				return false;

			// Start Condition 2 : Merging flow >= KstartRate
			double q = en.getFlow(i);
			double rate = en.getRate(i);
			if(q < K_START_THRESH * rate)
				return false;
		}

		en.startMetering();
		isMeteringStarted = true;
		return true;
	}

	/**
	 * Calculate metering rates for all operating ramp but not in zone
	 *   - do local metering
	 * @param stationState station state
	 */
	private void defaultMetering(StationNode stationState) {
		for(EntranceNode e : stationState.getAssociatedEntrances()) {
			if(e != null && e.isMetering && !e.isRateUpdated) {
				double Rnext = equation(stationState, null, e);
				e.setRate(Rnext);
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
		for(EntranceNode en : meterNodes.values()) {
			if(en.meter != null && en.meter.isOperating())
				return false;
		}
		return true;
	}

	/**
	 * Returns average density between 2 station
	 * @param upStation upstream station
	 * @param downStation downstream station (not need to be next downstream of upStation)
	 * @return average density (distance weight)
	 */
	public double getAverageDensity(StationNode upStation,
		StationNode downStation)
	{
		return getAverageDensity(upStation, downStation, 0);
	}

	/**
	 * Returns average density between 2 station at prevStep time steps ago.
	 * This works by splitting each "segment" between stations into 3 equal
	 * lengths and assigning average density to the middle sub-segment.
	 *
	 * @param upStation upstream station
	 * @param downStation downstream station (not necessarily consecutive)
	 * @param prevStep previous time steps
	 * @return average density (distance weight)
	 */
	public double getAverageDensity(StationNode upStation,
		StationNode downStation, int prevStep)
	{
		StationNode cursor = upStation;
		double dist_sum3 = 0;
		double k_sum3 = 0;
		double k_cursor = cursor.getAggregatedDensity(prevStep);
		for(StationNode down = cursor.downstreamStation();
		    down != null && cursor != downStation;
		    down = down.downstreamStation())
		{
			double k_down = down.getAggregatedDensity(prevStep);
			double k_middle = (k_cursor + k_down) / 2;
			double distance = cursor.distanceMiles(down);
			dist_sum3 += distance * 3;
			k_sum3 += (k_cursor + k_middle + k_down) * distance;
			cursor = down;
			k_cursor = k_down;
		}
		return k_sum3 / dist_sum3;
	}

	/**
	 * Class : Node State to manage station and entrance (ancestor class)
	 */
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
			return Math.round(distanceMiles(other) *
			                  Constants.FEET_PER_MILE);
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

		/** Debug an Node */
		abstract protected void debug(StringBuilder sb);
	}

	/**
	 * Class : Station State to manage station
	 */
	class StationNode extends Node {

		/** StationImpl mapping this state */
		protected final StationImpl station;

		/** Associated entrances to metering */
		private final ArrayList<EntranceNode> associatedEntrances =
			new ArrayList<EntranceNode>();

		/** Speed history */
		private final BoundedSampleHistory speedHistory =
			new BoundedSampleHistory(steps(60));

		/** Density history */
		private final BoundedSampleHistory densityHistory =
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

		/**
		 * Update station state.
		 * It must be called before finding bottleneck.
		 */
		public void updateState() {
			densityHistory.push((double)station.getDensity());
			speedHistory.push((double)station.getSpeed());
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

		/**
		 * Clear bottleneck state.
		 */
		protected void clearBottleneck() {
			isPrevBottleneck = isBottleneck;
			isBottleneck = false;
		}

		/** Is a bottleneck station too close to another? */
		protected boolean isBottleneckTooClose(StationNode sn) {
			return distanceMiles(sn) < BOTTLENECK_SPACING_MILES;
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
			Double avg = densityHistory.average(prevStep,steps(60));
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
			Double avg = speedHistory.average(0, steps(60));
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
		 * Return associated entrance
		 * @return associated entrance
		 */
		public ArrayList<EntranceNode> getAssociatedEntrances() {
			return associatedEntrances;
		}

		/**
		 * Associated Meter to Station
		 *   - Iterate from upstream to downstream
		 *   - Upstream meter will be associated to the station
		 *     when distance(Upstream Meter, Station) less than 500 feet
		 *          or Meter is not associated to any station
		 *   - Downstream meter will be associated to the station
		 *     when distance(Downstream Meter, Station) less than 1 mile
		 */
		private void setAssociatedEntrances() {
			ArrayList<EntranceNode> upstreamEntrances = getUpstreamEntrances();
			ArrayList<EntranceNode> downstreamEntrances = getDownstreamEntrances();

			StationNode us = upstreamStation();
			StationNode ds = downstreamStation();

			if(us != null) {
				for(EntranceNode en : upstreamEntrances) {
					if(!en.hasMeter())
						continue;
					int d = distanceFeet(en);
					int ud = us.distanceFeet(en);

					// very close(?) or not allocated with upstream station
					if((d < 500 && d < ud) || en.associatedStation == null) {
						if(en.associatedStation != null)
							en.associatedStation.associatedEntrances.remove(en);
						associatedEntrances.add(en);
						en.associatedStation = this;
					}
				}
			}

			if(ds != null) {
				for(EntranceNode en : downstreamEntrances) {
					if(!en.hasMeter())
						continue;
					int d = distanceFeet(en);
					// distance to downstream entrance is less than 1 mile
					if(d < 5280) {
						associatedEntrances.add(en);
						en.associatedStation = this;
					}
				}
			}
		}

		/**
		 * Return upstream entrances up to next upstream station
		 * @return upstream entrance list
		 */
		protected ArrayList<EntranceNode> getUpstreamEntrances() {
			ArrayList<EntranceNode> list =
				new ArrayList<EntranceNode>();
			for(Node n = upstream; n != null; n = n.upstream) {
				if(n instanceof StationNode)
					break;
				if(n instanceof EntranceNode) {
					EntranceNode en = (EntranceNode)n;
					if(en.hasMeter())
						list.add(en);
				}
			}
			return list;
		}

		/**
		 * Return downstream entrances up to next downstream station
		 * @return downstream entrance list
		 */
		protected ArrayList<EntranceNode> getDownstreamEntrances() {
			ArrayList<EntranceNode> list =
				new ArrayList<EntranceNode>();
			for(Node n = downstream; n != null; n = n.downstream) {
				if(n instanceof StationNode)
					break;
				if(n instanceof EntranceNode) {
					EntranceNode en = (EntranceNode)n;
					if(en.hasMeter())
						list.add(en);
				}
			}
			return list;
		}

		/** Debug a StationNode */
		protected void debug(StringBuilder sb) {
			sb.append(station.getName() + " -> ");
			for(EntranceNode en : associatedEntrances) {
				if(en != null && en.hasMeter())
					sb.append(en.meter.name + ", ");
			}
		}
	}

	/**
	 * Class : Entrance State to manage entrance
	 */
	class EntranceNode extends Node {

		/** Meter at this entrance */
		private RampMeterImpl meter;

		/** Associated station to metering */
		private StationNode associatedStation;

		/** Ramp demand at current time step */
		private double currentDemand = 0;

		/** Ramp flow at current time step */
		private double currentFlow = 0;

		/** Metering rate at current time step */
		private double currentRate = 0;

		/** Is it metering ? */
		private boolean isMetering = false;

		/** Is metering rate updated at current time step ? */
		private boolean isRateUpdated = false;

		/** Minimum metering rate */
		private double minimumRate = 0;

		/** How many time steps there's no bottleneck at downstream */
		private int noBottleneckCount = 0;

		/** Corresponding bottleneck */
		private StationNode bottleneck;

		/** Has been stopped before */
		private boolean hasBeenStoped = false;

		/** Queue detector set */
		private final DetectorSet queue = new DetectorSet();

		/** Passage detector set */
		private final DetectorSet passage = new DetectorSet();

		/** Merge detector set */
		private final DetectorSet merge = new DetectorSet();

		/** Bypass detector set */
		private final DetectorSet bypass = new DetectorSet();

		/** Cumulative demand history */
		private final BoundedSampleHistory cumulativeDemand =
			new BoundedSampleHistory(steps(240));

		/** Cumulative passage history */
		private final BoundedSampleHistory cumulativePassage =
			new BoundedSampleHistory(steps(30));

		/** Metering rate flow history */
		private final BoundedSampleHistory rateHistory =
			new BoundedSampleHistory(MAX_STEPS);

		/** Segment density history */
		private final BoundedSampleHistory segmentDensityHistory =
			new BoundedSampleHistory(MAX_STEPS);

		/** Ramp passage history */
		private final BoundedSampleHistory passageHistory =
			new BoundedSampleHistory(MAX_STEPS);

		/** Ramp demand history */
		private final BoundedSampleHistory rampDemandHistory =
			new BoundedSampleHistory(1);

		/**
		 * Construct
		 * @param rnode
		 */
		public EntranceNode(R_NodeImpl rnode, float m, Node prev) {
			super(rnode, m, prev);
		}

		/**
		 * Associate a ramp meter with the entrance node.
		 */
		public void setMeter(RampMeterImpl mtr) {
			meter = mtr;
			queue.clear();
			passage.clear();
			merge.clear();
			bypass.clear();
			DetectorSet ds = meter.getDetectorSet();
			queue.addDetectors(ds, LaneType.QUEUE);
			passage.addDetectors(ds, LaneType.PASSAGE);
			merge.addDetectors(ds, LaneType.MERGE);
			bypass.addDetectors(ds, LaneType.BYPASS);
		}

		/**
		 * Get the max wait time index for the ramp meter.
		 */
		private int maxWaitTimeIndex() {
			RampMeterImpl m = meter;
			if(m != null) {
				int max_wait = m.getMaxWait();
				int max_idx = steps(max_wait) - 1;
				if(max_idx > 0)
					return max_idx;
			}
			return steps(RampMeterImpl.DEFAULT_MAX_WAIT) - 1;
		}

		/**
		 * Does it have meter ?
		 * @return true if has meter, else false
		 */
		public boolean hasMeter() {
			return meter != null;
		}

		/**
		 * Update status
		 *   - Save cumulative demand and merging flow
		 *   - Set current demand and merging flow
		 *   - Calculate minimum metering rate
		 */
		private void updateState() {
			isRateUpdated = false;

			double p_flow = calculateRampFlow();
			double demand = calculateRampDemand(p_flow);
			double prevCd = getCumulativeDemand();
			double prevCq = getCumulativePassage();

			passageHistory.push(p_flow);
			cumulativePassage.push(prevCq + p_flow);
			rampDemandHistory.push(demand);
			cumulativeDemand.push(prevCd + demand);
			currentDemand = demand;
			currentFlow = p_flow;
			calculateMinimumRate();
		}

		/**
		 * Calculate ramp flow
		 * @return flow
		 */
		private int calculateRampFlow() {
			int p_flow = 0;

			// passage detector is ok
			if(passage.isPerfect())
				p_flow = passage.getFlow();
			else {
				// merge detector is ok
				if(merge.isPerfect()) {
					p_flow = merge.getFlow();
					// bypass detector is ok
					if(bypass.isPerfect()) {
						p_flow -= bypass.getFlow();
						if(p_flow < 0)
							p_flow = 0;
					}
				}
			}
			return p_flow;
		}

		/**
		 * Calculate ramp demand
		 */
		private double calculateRampDemand(double p_flow) {
			if(queue.isPerfect())
				return queue.getFlow();
			else
				return p_flow * PASSAGE_DEMAND_FACTOR;
		}

		/** Get the previous cumulative demand. */
		private double getCumulativeDemand() {
			if(cumulativeDemand.size() > 0)
				return cumulativeDemand.get(0);
			else
				return 0;
		}

		/** Get the previous cumulative passage */
		private double getCumulativePassage() {
			if(cumulativePassage.size() > 0)
				return cumulativePassage.get(0);
			else
				return 0;
		}

		/**
		 * Calculate minimum rate according to waiting time
		 */
		private void calculateMinimumRate() {
			int wait_idx = maxWaitTimeIndex();
			if(cumulativeDemand.size() - 1 < wait_idx) {
				minimumRate = currentFlow;
				return;
			}
			double Cd_4mAgo = cumulativeDemand.get(wait_idx);
			double Cf_current = cumulativePassage.get(0);
			minimumRate = (Cd_4mAgo - Cf_current);
			if(minimumRate <= 0)
				minimumRate = getMinRelease();
		}

		/**
		 * Set metering rate
		 * @param Rnext next metering rate
		 */
		private void setRate(double Rnext) {
			if(!hasMeter())
				return;
			currentRate = Rnext;
			isRateUpdated = true;
			int releaseRate = (int) Math.round(Rnext);
			meter.setRatePlanned(releaseRate);
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
			currentDemand = 0;
			currentFlow = 0;
			currentRate = 0;
			rateHistory.clear();
			noBottleneckCount = 0;
			hasBeenStoped = true;
		}

		/**
		 * Set bottleneck station
		 * @param bottleneck
		 */
		private void setBottleneck(StationNode bottleneck) {
			this.bottleneck = bottleneck;
		}

		/**
		 * Clear bottleneck state.
		 */
		protected void clearBottleneck() {
			setBottleneck(null);
		}

		/**
		 * Save metering rate history
		 * @param Rnext
		 */
		private void saveRateHistory(double Rnext) {
			rateHistory.push(Rnext);
		}

		/**
		 * Return length of metering rate history
		 * @return
		 */
		private int countRateHistory() {
			return rateHistory.size();
		}

		/**
		 * Return ramp flow at 'prevStep' time steps ago
		 * @param prevStep
		 * @return ramp flow at 'prevStep' time steps ago
		 */
		private double getFlow(int prevStep) {
			return passageHistory.get(prevStep);
		}

		/**
		 * Return current metering rate
		 * @return metering rate
		 */
		private double getRate() {
			if(currentRate == 0)
				currentRate = getInitialRate();
			return currentRate;
		}

		/** Initial rate = average(last 90 seconds) or MAX_RATE */
		private double getInitialRate() {
			Double flow = passageHistory.average(0, steps(90));
			if(flow != null)
				return flow;
			else
				return getMaxRelease();  // no flow
		}

		/**
		 * Return current metering rate at 'prevStep' time steps ago
		 * @return metering rate
		 */
		private double getRate(int prevStep) {
			return rateHistory.get(prevStep);
		}

		/**
		 * Start metering
		 */
		private void startMetering() {
			isMetering = true;
		}

		/**
		 * Save segment density history
		 * @param Kt
		 */
		private void saveSegmentDensityHistory(double Kt) {
			segmentDensityHistory.push(Kt);
		}

		/**
		 * Return segment density at 'prevStep' time steps ago
		 * @param prevStep
		 * @return segment density at 'prevStep' time steps ago
		 */
		private double getSegmentDensity(int prevStep) {
			return segmentDensityHistory.get(prevStep);
		}

		/**
		 * Return minimum metering rate
		 * @return minimum metering rate
		 */
		private double getMinimumRate() {
			return minimumRate;
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

		/** Debug an EntranceNode */
		protected void debug(StringBuilder sb) {
			if(hasMeter())
				sb.append("Ent(" + meter.name + ")");
			else
				sb.append("Ent(" + rnode.name + ")");
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
