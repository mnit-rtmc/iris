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

	/** Number of time steps to check before stop metering */
	static private final int STOP_STEPS = 10;

	/** Number of time steps for bottleneck trend before stop metering */
	static private final int BOTTLENECK_TREND_STEPS_BEFORE_STOP = 2;

	/** Number of time steps for bottleneck trend after stop metering */
	static private final int BOTTLENECK_TREND_STEPS_AFTER_STOP = 4;

	/** Number of steps for average density to check corridor state */
	static private final int AVG_K_STEPS = 30;

	/** Number of trend steps for average density to check corridor state */
	static private final int AVG_K_TREND_STEPS = 10;

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
			if(rnode.station_id != null &&
			   rnode.getDetectorSet().size() > 0)
			{
				return new StationNode(rnode, mile, prev);
			}
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
			en.meter = meter;
			en.checkFreewayToFreeway();
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
				EntranceNode es = (EntranceNode)n;
				if(es.rnode.equals(rnode))
					return es;
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
		return doStopChecking ? BOTTLENECK_TREND_STEPS_AFTER_STOP :
		                        BOTTLENECK_TREND_STEPS_BEFORE_STOP;
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
			for(EntranceNode es : s.getAssociatedEntrances())
				es.resetNoBottleneckCount();
			return true;
		}
		for(EntranceNode es : s.getAssociatedEntrances()) {
			if(!es.isMetering)
				continue;
			if(es.countRateHistory() < STOP_STEPS)
				continue;
			boolean shouldStop = false;
			for(int k = 0; k < STOP_STEPS; k++) {
				Double sk = es.getSegmentDensity(k);
				if(sk > K_DES) {
					shouldStop = true;
					break;
				}
			}
			if(shouldStop)
				continue;
			if(!hasBottleneck) {
				es.addNoBottleneckCount();
				if(es.getNoBottleneckCount() >= STOP_STEPS) {
					es.stopMetering();
					continue;
				}
				continue;
			}
			boolean satisfyRateCondition = true;
			es.resetNoBottleneckCount();
			for(int k = 0; k < STOP_STEPS; k++) {
				double q = es.getFlow(k);
				double rate = es.getRate(k);
				if(q > rate)
					satisfyRateCondition = false;
			}
			if(satisfyRateCondition)
				es.stopMetering();
		}
		return hasBottleneck;
	}

	/**
	 * Calculate metering rates for all bottlenecks
	 */
	private void calculateMeteringRates() {
		for(StationNode sn = lastStation(); sn != null;
		    sn = sn.upstreamStation())
		{
			if(sn.isBottleneck && sn.station != null)
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
		for(EntranceNode es : bottleneck.getAssociatedEntrances()) {
			if(!es.hasMeter())
				continue;
			double Rnext = equation(bottleneck, es);
			es.saveRateHistory(Rnext);
			if(!checkStartCondition(bottleneck, null, es))
				continue;
			es.setBottleneck(bottleneck);
			es.setRate(Rnext);
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
		if(upStation.station == null)
			return;
		for(EntranceNode es : upStation.getAssociatedEntrances()) {
			if(!es.hasMeter())
				continue;
			double Rnext = equation(bottleneck, upStation, es);
			es.saveRateHistory(Rnext);
			if(!checkStartCondition(bottleneck, upStation, es))
				continue;
			es.setBottleneck(bottleneck);
			es.setRate(Rnext);
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
	 * @param es target entrance
	 * @param Rnext calculated next metering rate
	 * @return start ?
	 */
	private boolean checkStartCondition(StationNode bs, StationNode us,
		EntranceNode es)
	{
		if(es.isMetering)
			return true;
		if(!es.hasBeenStoped)
			return checkStartFirst(bs, us, es);
		else
			return checkStartAfterStop(bs, us, es);
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
		EntranceNode es)
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

		if(es.countRateHistory() >= 3) {
			satisfyRateCondition = true;
			for(int i = 0; i < 3; i++) {
				double q = es.getFlow(i);
				double rate = es.getRate(i);
				if(q < K_START_THRESH * rate)
					satisfyRateCondition = false;
			}
		}

		if(satisfyRateCondition || satisfyDensityCondition) {
			es.startMetering();
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
		EntranceNode es)
	{
		double segmentDensity = 0;

		// if rate history is short, pass (do not start)
		if(es.countRateHistory() < 10)
			return false;

		for(int i = 0; i < 10; i++) {
			if(us != null)
				segmentDensity = getAverageDensity(us, bs, i);
			else
				segmentDensity = bs.getAggregatedDensity(i);

			// Start Condition 1 : segment density > bottleneck K
			if(segmentDensity < bottleneckDensity())
				return false;

			// Start Condition 2 : Merging flow >= KstartRate
			double q = es.getFlow(i);
			double rate = es.getRate(i);
			if(q < K_START_THRESH * rate)
				return false;
		}

		es.startMetering();
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
		boolean done = true;
		for(EntranceNode en : meterNodes.values()) {
			if(en.meter != null && en.meter.isOperating()) {
				done = false;
				break;
			}
		}
		return done;
	}

	/**
	 * Returns average density between 2 station
	 * @param upStation upstream station
	 * @param downStation downstream station (not need to be next downstream of upStation)
	 * @return average density (distance weight)
	 */
	public double getAverageDensity(StationNode upStation, StationNode downStation) {
		return getAverageDensity(upStation, downStation, 0);
	}

	/**
	 * Returns average density between 2 station at prevStep time steps ago
	 * @param upStation upstream station
	 * @param downStation downstream station (not need to be next downstream of upStation)
	 * @param prevStep previous time steps
	 * @return average density (distance weight)
	 */
	public double getAverageDensity(StationNode upStation, StationNode downStation, int prevStep) {
		StationNode cursor = upStation;

		double totalDistance = 0;
		double avgDensity = 0;
		while(true) {
			StationNode dStation = cursor.downstreamStation();
			double upDensity = cursor.getAggregatedDensity(prevStep);
			double downDensity = dStation.getAggregatedDensity(prevStep);
			double middleDensity = (upDensity + downDensity) / 2;
			double distance = cursor.distanceMiles(dStation);
			double distanceFactor = distance / 3;
			totalDistance += distance;
			avgDensity += (upDensity + middleDensity + downDensity) * distanceFactor;

			if(dStation.equals(downStation))
				break;
			cursor = dStation;
		}
		return avgDensity / totalDistance;
	}

	/**
	 * Class : R_Node State to manage station and entrance (ancestor class)
	 */
	abstract protected class Node {
		R_NodeImpl rnode;

		/** Mile point of the node */
		protected final float mile;

		/** Link to upstream node */
		protected final Node upstream;

		/** Link to downstream node */
		protected Node downstream;

		/** Create a new node */
		protected Node(float m, Node up) {
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
			new BoundedSampleHistory(10);

		/** Density history */
		private final BoundedSampleHistory densityHistory =
			new BoundedSampleHistory(10);

		/** Is bottleneck ? */
		private boolean isBottleneck = false;

		/** Is bottleneck at previous time step? */
		private boolean isPrevBottleneck = false;

		/** Detector set for the station : mainline, aux */
		private final DetectorSet dets = new DetectorSet();

		/**
		 * Create a new station node.
		 * @param rnode
		 */
		public StationNode(R_NodeImpl rnode, float m, Node up) {
			super(m, up);
			this.rnode = rnode;
			station = rnode.station;
			DetectorSet ds = rnode.getDetectorSet();
			// use mainline and auxiliary lane
			dets.addDetectors(ds.getDetectorSet(LaneType.MAINLINE));
			dets.addDetectors(ds.getDetectorSet(LaneType.AUXILIARY));
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
		 * Update station state status
		 * It must be called before finding bottleneck.
		 */
		public void updateState() {
			if(station == null)
				return;
			Iterator<DetectorImpl> itr = dets.detectors.iterator();
			double density = 0, speed = 0;
			float u = -1, k = -1;
			int n_u = 0, n_k = 0;

			while(itr.hasNext()) {
				DetectorImpl d = itr.next();
				if(!d.abandoned) {
					k = d.getDensity();
					u = d.getSpeed();
				} else
					continue;
				if(u > 0) {
					speed += u;
					n_u++;
				}
				if(k > 0) {
					density += k;
					n_k++;
				}
			}

			if(n_u > 0)
				speed /= n_u;
			if(n_k > 0)
				density /= n_k;

			densityHistory.push(density);
			speedHistory.push(speed);
		}

		/**
		 * Check if the station is a bottleneck.
		 */
		protected void checkBottleneck() {
			double kb = bottleneckDensity();
			if(getAggregatedDensity() >= kb) {
				boolean increasing = true;
				boolean high_k = true;
				for(int i = 0; i < bottleneckTrendSteps(); i++){
					double k = getAggregatedDensity(i);
					double pk = getAggregatedDensity(i + 1);
					if(k < pk)
						increasing = false;
					if(k < kb || pk < kb)
						high_k = false;
				}
				if(isPrevBottleneck || increasing || high_k)
					isBottleneck = true;
			}
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
			// FIXME: we need to use actual distance, not count
			// return sn.stationIdx - stationIdx < 3;
			return false;
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
		 * @return average 1min density at 'prevStep' time steps ago
		 */
		public double getAggregatedDensity(int prevStep) {
			return getAggregatedData(densityHistory, prevStep);
		}

		/**
		 * Return aggregated speed at current time step
		 * @return average 1min speed
		 */
		public double getAggregatedSpeed() {
			return getAggregatedSpeed(0);
		}

		/**
		 * Return aggregated speed at 'prevStep' time steps ago
		 * @param prevStep how many time steps ago?
		 * @return average 1min speed at 'prevStep' time steps ago
		 */
		public double getAggregatedSpeed(int prevStep) {
			return getAggregatedData(speedHistory, prevStep);
		}

		/**
		 * Return aggregated data (average)
		 * @param data
		 * @param prevStep
		 * @return average 1min data of given data at 'prevStep' time steps ago
		 */
		private double getAggregatedData(BoundedSampleHistory data, int prevStep) {
			Double d1 = data.get(prevStep);
			Double d2 = data.get(prevStep + 1);
			int n = 2;

			if(d1 == null || d1 == 0) {
				d1 = 0D;
				n--;
			}

			if(d2 == null || d2 == 0) {
				d2 = 0D;
				n--;
			}

			if(n == 0)
				return 0;
			else
				return (d1 + d2) / n;
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
				for(EntranceNode es : upstreamEntrances) {
					if(!es.hasMeter())
						continue;
					int d = distanceFeet(es);
					int ud = us.distanceFeet(es);

					// very close(?) or not allocated with upstream station
					if((d < 500 && d < ud) || es.associatedStation == null) {
						if(es.associatedStation != null)
							es.associatedStation.associatedEntrances.remove(es);
						associatedEntrances.add(es);
						es.associatedStation = this;
					}
				}
			}

			if(ds != null) {
				for(EntranceNode es : downstreamEntrances) {
					if(!es.hasMeter())
						continue;
					int d = distanceFeet(es);
					// distance to downstream entrance is less than 1 mile
					if(d < 5280) {
						associatedEntrances.add(es);
						es.associatedStation = this;
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
					EntranceNode es = (EntranceNode)n;
					if(es.hasMeter())
						list.add(es);
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
					EntranceNode es = (EntranceNode)n;
					if(es.hasMeter())
						list.add(es);
				}
			}
			return list;
		}

		/** Debug a StationNode */
		protected void debug(StringBuilder sb) {
			sb.append(rnode.station_id + " -> ");
			for(EntranceNode es : associatedEntrances) {
				if(es != null && es.hasMeter())
					sb.append(es.meter.name + ", ");
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

		/** Max wait time index (7 => 4min) */
		private int maxWaitTimeIndex = 7;

		/** How many time steps there's no bottleneck at downstream for */
		private int noBottleneckCount = 0;

		/** Corresponding bottleneck */
		private StationNode bottleneck;

		/** Has been stopped before */
		private boolean hasBeenStoped = false;

		/** Cumulative demand history */
		private BoundedSampleHistory cumulativeDemand = new BoundedSampleHistory(maxWaitTimeIndex+1);

		/** Cumulative merging flow history */
		private BoundedSampleHistory cumulativeMergingFlow = new BoundedSampleHistory(1);

		/** Metering rate flow history */
		private BoundedSampleHistory rateHistory = new BoundedSampleHistory(STOP_STEPS);

		/** Segment density history */
		private BoundedSampleHistory segmentDensityHistory = new BoundedSampleHistory(STOP_STEPS);

		/** Ramp flow history */
		private BoundedSampleHistory rampFlowHistory = new BoundedSampleHistory(STOP_STEPS);

		/** Ramp demand history */
		private BoundedSampleHistory rampDemandHistory = new BoundedSampleHistory(1);

		/**
		 * Construct
		 * @param rnode
		 */
		public EntranceNode(R_NodeImpl rnode, float m, Node prev) {
			super(m, prev);
			this.rnode = rnode;
		}

		/**
		 * Check if entrance is Freeway to Freeway
		 */
		public void checkFreewayToFreeway() {
			// check if it is freeway-to-freeway entrance
			if(rnode.geo_loc == null)
				return;
			String mod = GeoLocHelper.getModifier(rnode.geo_loc);
			if(rnode.geo_loc.getCrossMod() == 0)
				mod = "";
			String lbl = GeoLocHelper.getCrossDescription(rnode.geo_loc, mod);
			Iterator<Corridor> itr = BaseObjectImpl.corridors.corridors.values().iterator();
			while(itr.hasNext()) {
				if(itr.next().getName().contains(lbl)) {
					maxWaitTimeIndex = 3;   // 2 minutes in 30 seconds unit
					break;
				}
			}
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
			calculateRampFlowAndDemand();

			double demand = rampDemandHistory.get(0);
			double p_flow = rampFlowHistory.get(0);

			double prevCd = 0;
			double prevCq = 0;

			if(cumulativeDemand.size() > 0)
				prevCd = cumulativeDemand.get(0);
			if(cumulativeMergingFlow.size() > 0)
				prevCq = cumulativeMergingFlow.get(0);

			cumulativeDemand.push(prevCd + demand);
			cumulativeMergingFlow.push(prevCq + p_flow);

			currentDemand = demand;
			currentFlow = p_flow;

			calculateMinimumRate();
		}

		/**
		 * Calculate ramp flow
		 * @return flow
		 */
		private int calculateRampFlow() {
			DetectorSet ds = meter.getDetectorSet();
			DetectorSet pDet = ds.getDetectorSet(LaneType.PASSAGE);
			DetectorSet mDet = ds.getDetectorSet(LaneType.MERGE);
			DetectorSet bpDet = ds.getDetectorSet(LaneType.BYPASS);

			int p_flow = 0;

			// passage detector is ok
			if(pDet != null)
				p_flow = pDet.getFlow();
			else {
				// merge detector is ok
				if(mDet != null) {
					p_flow = mDet.getFlow();
					// bypass detector is ok
					if(bpDet != null) {
						p_flow -= bpDet.getFlow();
						if(p_flow < 0)
							p_flow = 0;
					}
				}
			}
			return p_flow;
		}

		/**
		 * Calculate ramp flow and demand
		 */
		private void calculateRampFlowAndDemand() {
			DetectorSet ds = meter.getDetectorSet();
			DetectorSet qDets = ds.getDetectorSet(LaneType.QUEUE);

			double rampFlow = calculateRampFlow();
			double rampDemand = 0;

			rampFlowHistory.push(rampFlow);

			// queue detector is ok
			if(qDets != null)
				rampDemand = qDets.getFlow();
			else
				rampDemand = rampFlow * PASSAGE_DEMAND_FACTOR;
			rampDemandHistory.push(rampDemand);
		}

		/**
		 * Calculate minimum rate according to waiting time
		 */
		private void calculateMinimumRate() {
			if(cumulativeDemand.size() - 1 < maxWaitTimeIndex) {
				minimumRate = currentFlow;
				return;
			}

			// cumulative demand 4 min ago
			double Cd_4mAgo = cumulativeDemand.get(maxWaitTimeIndex);

			// current cumulative passage flow
			double Cf_current = cumulativeMergingFlow.get(0);

			// minimum rates to guarantee 4 min waitting time
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
		 * Stop metering
		 */
		public void stopMetering() {
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
			return rampFlowHistory.get(prevStep);
		}

		/**
		 * Return current metering rate
		 * @return metering rate
		 */
		private double getRate() {
			// initial rate = average(last 3 flows) or MAX_RATE
			if(currentRate == 0) {
				currentRate = currentFlow;
				double flowSum = 0;
				int cnt = 0;

				for(int i = 0; i < 3; i++) {
					flowSum += rampFlowHistory.get(i);
					cnt++;
				}

				if(cnt > 0)
					currentRate = flowSum / cnt;
				else
					currentRate = getMaxRelease();  // no flow
			}
			return currentRate;
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
