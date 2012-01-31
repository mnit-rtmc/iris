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
import java.util.LinkedList;
import java.util.Queue;
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

	/** Critical Density */
	static private final double K_CRIT = 40;

	/** Desired Density */
	static private final double K_DES = K_CRIT * 0.8;

	/** Jam Density */
	static private final double K_JAM = 180;

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

	/** Bottleneck Density */
	protected double Kb = 25;

	/** Is started metering in this corridor? */
	protected boolean isMeteringStarted = false;

	/** Should check stop condition? (depends on corridor density trend) */
	private boolean doStopChecking = false;

	/** Are station-entrance associated? */
	private boolean isAssociated = false;

	/** Corridor density history for triggering metering stop */
	private final BoundedSampleHistory k_hist_corridor =
		new BoundedSampleHistory(AVG_K_STEPS + AVG_K_TREND_STEPS);

	/** All r_node states */
	private final ArrayList<RNodeState> states =
		new ArrayList<RNodeState>();

	/** Station states list in the corridor */
	private final ArrayList<StationState> stationStates =
		new ArrayList<StationState>();

	/** Hash map of ramp meter states */
	private final HashMap<String, MeterState> meterStates =
		new HashMap<String, MeterState>();

	@Override
	public void validate(RampMeterImpl meter) {
		MeterState state = getMeterState(meter);
		if(state != null)
			state.validate();
	}

	/** Get the meter state for a given timing plan */
	protected MeterState getMeterState(RampMeterImpl meter) {
		if(meter.getCorridor() != corridor) {
			// Meter must have been changed to a different
			// corridor; throw away old meter state
			meterStates.remove(meter.getName());
			return null;
		}
		MeterState state = lookupMeterState(meter);
		if(state != null)
			return state;
		state = new MeterState(meter);
		addMeterState(state);
		meterStates.put(meter.getName(), state);
		return state;
	}

	/** Lookup the meter state for a specified meter */
	protected MeterState lookupMeterState(RampMeter meter) {
		return meterStates.get(meter.getName());
	}

	/**
	 * Add meter state
	 * @param state
	 */
	private void addMeterState(MeterState state) {
		R_NodeImpl rnode = state.meter.getR_Node();
		for(RNodeState ns : states) {
			if(ns instanceof EntranceState) {
				EntranceState es = (EntranceState)ns;
				if(es.rnode.equals(rnode)) {
					es.meterState = state;
					state.entrance = es;
					es.checkFreewayToFreeway();
				}
			}
		}
	}

	/** Process the algorithm for the one interval */
	protected void processInterval() {
		if(!isAssociated)
			doAssociateStationAndEntrance();
		for(StationState s : stationStates)
			s.updateState();
		findBottlenecks();
		calculateMeteringRates();
		if(isMeteringStarted && !doStopChecking)
			checkCorridorState();
		else if(doStopChecking)
			checkStopCondition();
		afterMetering();
	}

	/**
	 * Should be called after added all stations and meters
	 */
	private void doAssociateStationAndEntrance() {
		for(StationState s : stationStates)
			s.setAssociatedEntrances();

		/** for debugging of corridor structure */
		if(ALG_LOG.isOpen())
			debug();
		isAssociated = true;
	}

	/** Debug algorithm. */
	private void debug() {
		ALG_LOG.log("Corridor Structure : " + corridor.getName() +
			" --------------------");
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < states.size(); i++) {
			RNodeState state = states.get(i);
			sb.append("[" + String.format("%02d", state.idx) +"] ");
			state.debug(sb);
			sb.append("\n");
		}
		ALG_LOG.log(sb.toString());
	}

	/**
	 * Find bottlenecks
	 */
	private void findBottlenecks() {

		// find bottleneck candidates
		for(int i = 0; i < stationStates.size(); i++) {
			StationState s = stationStates.get(i);

			if(s.getAggregatedDensity() < Kb)
				continue;

			boolean increaseTrend = true;
			boolean highDensity = true;

			for(int j = 0; j < bottleneckTrendSteps(); j++) {
				double k = s.getAggregatedDensity(j);
				double pk = s.getAggregatedDensity(j + 1);
				if(k < pk)
					increaseTrend = false;
				if(k < Kb || pk < Kb)
					highDensity = false;
			}

			if(s.isPrevBottleneck || increaseTrend || highDensity)
				s.isBottleneck = true;
		}

		// merge zone by distnace and acceleration
		// iterate from downstream to upstream
		for(int i = stationStates.size() - 1; i >= 0; i--) {
			StationState s = stationStates.get(i);
			double k = s.getAggregatedDensity();

			if(!s.isBottleneck)
				continue;

			// check zone to merge
			for(int j = s.stationIdx - 1; j >= 0; j--) {
				StationState us = stationStates.get(j);
				if(!us.isBottleneck)
					continue;
				// close bottleneck
				if(s.stationIdx - us.stationIdx < 3) {
					// close but independent BS
					if(us.getAggregatedDensity() > k && us.getAcceleration() > A_BOTTLENECK)
						break;
					// close -> merge
					us.isBottleneck = false;
				} else if(us.getAcceleration() > A_BOTTLENECK) {
					// acceleration is heigh -> BS
					break;
				} else
					us.isBottleneck = false;
			}
		}
	}

	/**
	 * Check corridor average density condition
	 */
	private void checkCorridorState() {
		int bottleneckCount = 0;
		StationState downstreamBS = null;
		for(StationState s : stationStates) {
			if(s.isBottleneck) {
				downstreamBS = s;
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

		// restrict condition
		Kb = K_DES;

		// let's check stop condition from now
		doStopChecking = true;
	}

	/**
	 * Calculate the average density up to a bottleneck.
	 * @param bottleneck Bottleneck station.
	 * @return Average density up to the bottleneck.
	 */
	private double calculateAverageDensity(StationState bottleneck) {
		if(bottleneck != null) {
			StationState up = stationStates.get(0);
			return getAverageDensity(up, bottleneck);
		} else
			return 0;
	}

	/** Get number of time steps to check for bottleneck */
	private int bottleneckTrendSteps() {
		if(doStopChecking)
			return BOTTLENECK_TREND_STEPS_AFTER_STOP;
		else
			return BOTTLENECK_TREND_STEPS_BEFORE_STOP;
	}

	/**
	 * Stop metering of ramp meter satisfying conditions
	 */
	private void checkStopCondition() {

		int N = STOP_STEPS;

		// iterate from downstream to upstream
		boolean hasBottleneck = false;
		for(int i = stationStates.size() - 1; i >= 0; i--) {
			StationState s = stationStates.get(i);

			// station is bottleneck
			if(s.isBottleneck) {
				hasBottleneck = true;
				// set entrance's no-bottleneck count 0
				for(EntranceState es : s.getAssociatedEntrances()) {
					es.resetNoBottleneckCount();
				}
				continue;
			}

			// for all entrances
			for(EntranceState es : s.getAssociatedEntrances()) {
				// if meter isn't working, pass
				if(!es.isMetering)
					continue;

				// if rate history is short, pass (do not stop)
				if(es.countRateHistory() < N)
					continue;

				boolean shouldStop = false;
				// COMMON STOP CONDITION : segment density is low for n times
				for(int k = 0; k < N; k++) {
					Double sk = es.getSegmentDensity(k);
					if(sk > K_DES) {
						shouldStop = true;
						break;
					}
				}

				if(shouldStop)
					continue;

				/////////////////////////////////////////////////////////////
				// No bottleneck at downstream side
				/////////////////////////////////////////////////////////////

				// Stop Condition 1 : no bottleneck at downstream side for n times
				if(!hasBottleneck) {
					es.addNoBottleneckCount();

					// if there's no bottleneck for a long time (N times)
					if(es.getNoBottleneckCount() >= N) {
						es.stopMetering();
						continue;
					}
					continue;
				}

				/////////////////////////////////////////////////////////////
				// Bottleneck exists at downstream side
				/////////////////////////////////////////////////////////////
				boolean satisfyRateCondition = true;
				es.resetNoBottleneckCount();

				// Stop Condition 2 : qj,t <= Rj,t for n times
				//   - n : 10 (5min)
				for(int k = 0; k < N; k++) {
					double q = es.getFlow(k);
					double rate = es.getRate(k);
					if(q > rate)
						satisfyRateCondition = false;
				}
				if(satisfyRateCondition)
					es.stopMetering();
			}
		}
	}

	/**
	 * Calculate metering rates for all bottlenecks
	 */
	private void calculateMeteringRates() {

		// downstream boundary -> upstream
		for(int i = stationStates.size() - 1; i >= 0; i--) {
			StationState station = stationStates.get(i);
			if(!station.isBottleneck || station.station == null)
				continue;
			calculateMeteringRates(station, i);
		}

		// when meter is not in zone
		for(StationState s : stationStates)
			defaultMetering(s);
	}

	/**
	 * Calculate metering rates for a bottleneck
	 * @param bottleneck bottleneck station
	 * @param stationIndex station index from upstream of station state list
	 */
	private void calculateMeteringRates(StationState bottleneck, int stationIndex) {

		// calculate rates for entrance associated with bottleneck
		for(EntranceState es : bottleneck.getAssociatedEntrances()) {
			if(!es.hasMeter())
				continue;
			double Rnext = equation(bottleneck, es);
			if(!checkStartCondition(bottleneck, null, es, Rnext))
				continue;
			es.setBottleneck(bottleneck);
			es.setRate(Rnext);
		}

		// calculate rates
		// from upstream station of bottleneck to upstream boundary or next bottleneck
		for(int i = stationIndex - 1; i >= 0; i--) {

			// break, if upstation is bottleneck
			StationState upStation = stationStates.get(i);
			if(upStation.isBottleneck)
				break;
			if(upStation.station == null)
				continue;
			for(EntranceState es : upStation.getAssociatedEntrances()) {
				if(!es.hasMeter())
					continue;
				double Rnext = equation(bottleneck, upStation, es);
				if(!checkStartCondition(bottleneck, upStation, es, Rnext))
					continue;
				es.setBottleneck(bottleneck);
				es.setRate(Rnext);
			}
		}
	}

	/**
	 * Calculate metering rates
	 * @param stationState station state
	 * @param entranceState entrance state
	 * @return next metering rate
	 */
	private double equation(StationState stationState, EntranceState entrance) {
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
	 * --p0------------p1----p2------------p3---p4-----> K_DES-Kt
	 *                       |
	 *                       |
	 * p0's x = K_DES - K_JAM
	 * p2's x = 0
	 * p4's x = K_DES
	 * </pre>
	 * @param bottleneck
	 * @return
	 */
	private double equation(StationState bottleneck, StationState upstream,
		EntranceState entrance)
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
	private boolean checkStartCondition(StationState bs, StationState us,
		EntranceState es, double Rnext)
	{
		es.saveRateHistory(Rnext);

		// Condition 0 : already started
		if(es.isMetering)
			return true;

		///////////////////////////////////
		// Check Start Conditions
		///////////////////////////////////

		// Before stopped
		//    - SegmentDensity >= Kb (just once)
		//    - OR -
		//    - Merging flow of ramp >= KstartRate * Allocated Rate (for n times)
		boolean satisfyDensityCondition = false;
		boolean satisfyRateCondition = false;

		// segment density (average density from upstream station to bottleneck)
		if(!es.hasBeenStoped) {
			// Start Condition 1 : segment density > Kb
			double segmentDensity = 0;
			if(us != null)
				segmentDensity = getAverageDensity(us, bs);
			else
				segmentDensity = bs.getAggregatedDensity();

			if(segmentDensity >= Kb)
				satisfyDensityCondition = true;

			// Start Condition 2 : merging flow of ramp > Rate * 0.8
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
			}
			return false;
		}

		// After stopped
		//    - SegmentDensity >= Kb (for n times)
		//    - AND -
		//    - Merging flow of ramp >= KstartRate * Allocated Rate (for n times)
		double segmentDensity = 0;

		// if rate history is short, pass (do not start)
		if(es.countRateHistory() < 10)
			return false;

		for(int i = 0; i < 10; i++) {
			if(us != null)
				segmentDensity = getAverageDensity(us, bs, i);
			else
				segmentDensity = bs.getAggregatedDensity(i);

			// Start Condition 1 : segment density > Kb
			if(segmentDensity < Kb)
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
	private void defaultMetering(StationState stationState) {
		for(EntranceState e : stationState.getAssociatedEntrances()) {
			if(e != null && e.isMetering && !e.isRateUpdated) {
				double Rnext = equation(stationState, null, e);
				e.setRate(Rnext);
			}
		}
	}

	/**
	 * Post metering process
	 */
	private void afterMetering() {
		for(StationState s : stationStates)
			s.afterMetering();
		for(RNodeState ns : states) {
			if(ns instanceof EntranceState) {
				EntranceState es = (EntranceState)ns;
				es.setBottleneck(null);
			}
		}
	}

	/** Create a new KAdaptiveAlgorithm */
	private KAdaptiveAlgorithm(Corridor c) {
		corridor = c;
		createStates();
	}

	/** construct corridor structure */
	private void createStates() {
		Iterator<R_Node> itr = corridor.iterator();
		while(itr.hasNext()) {
			R_NodeImpl rnode = (R_NodeImpl) itr.next();
			R_NodeType nType = R_NodeType.fromOrdinal(
				rnode.getNodeType());
			if(nType == R_NodeType.ENTRANCE)
				addEntranceState(new EntranceState(rnode));
			else if(nType == R_NodeType.STATION) {
				if (rnode.station_id != null && Integer.parseInt(rnode.station_id.substring(1)) / 100 != 17 /* check wavetronics */ && rnode.getDetectorSet().size() > 0) {
					addStationState(new StationState(rnode));
				}
			}
		}
	}

	/**
	 * Add entrance state
	 * @param entranceState
	 */
	private void addEntranceState(EntranceState entranceState) {
		entranceState.idx = states.size();
		states.add(entranceState);
	}

	/**
	 * Add station state
	 * @param stationState
	 */
	private void addStationState(StationState stationState) {
		stationState.stationIdx = stationStates.size();
		stationState.idx = states.size();
		stationStates.add(stationState);
		states.add(stationState);
	}

	/** Is this KAdaptiveAlgorithm done? */
	private boolean isDone() {
		boolean done = true;
		for(MeterState state : meterStates.values()) {
			if(state.meter.isOperating()) {
				done = false;
				break;
			}
		}
		return done;
	}

	/**
	 * Return distance between two r_nodes in feet
	 * @param rn1 r_node
	 * @param rn2 r_node
	 */
	public double getDistanceInFeet(R_NodeImpl rn1, R_NodeImpl rn2) {
		int e1 = rn1.getGeoLoc().getEasting();
		int e2 = rn2.getGeoLoc().getEasting();
		int n1 = rn1.getGeoLoc().getNorthing();
		int n2 = rn2.getGeoLoc().getNorthing();
		return (int) (Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609 * 5280);
	}

	/**
	 * Return downstream station state
	 * @param idx rnode-index of state
	 * @return downstream station state
	 */
	public StationState getDownstreamStationState(int idx) {
		if(idx < 0 || idx >= states.size() - 1)
			return null;
		for(int i = idx + 1; i < states.size(); i++) {
			RNodeState st = states.get(i);
			if(st instanceof StationState)
				return (StationState) st;
		}
		return null;
	}

	/**
	 * Returns average density between 2 station
	 * @param upStation upstream station
	 * @param downStation downstream station (not need to be next downstream of upStation)
	 * @return average density (distance weight)
	 */
	public double getAverageDensity(StationState upStation, StationState downStation) {
		return getAverageDensity(upStation, downStation, 0);
	}

	/**
	 * Returns average density between 2 station at prevStep time steps ago
	 * @param upStation upstream station
	 * @param downStation downstream station (not need to be next downstream of upStation)
	 * @param prevStep previous time steps
	 * @return average density (distance weight)
	 */
	public double getAverageDensity(StationState upStation, StationState downStation, int prevStep) {
		StationState cursor = upStation;

		double totalDistance = 0;
		double avgDensity = 0;
		while(true) {
			StationState dStation = getDownstreamStationState(cursor.idx);
			double upDensity = cursor.getAggregatedDensity(prevStep);
			double downDensity = dStation.getAggregatedDensity(prevStep);
			double middleDensity = (upDensity + downDensity) / 2;
			double distance = getDistanceInFeet(cursor.rnode, dStation.rnode) / 5280;
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
	abstract class RNodeState {
		R_NodeImpl rnode;
		int idx;

		/** Debug an RNodeState */
		abstract protected void debug(StringBuilder sb);
	}

	/**
	 * Class : Station State to manage station
	 */
	class StationState extends RNodeState {

		/** StationImpl mapping this state */
		StationImpl station;

		/** Station index from upstream */
		int stationIdx;

		/** Associated entrances to metering */
		ArrayList<EntranceState> associatedEntrances = new ArrayList<EntranceState>();

		/** Speed history */
		BoundedSampleHistory speedHistory = new BoundedSampleHistory(10);

		/** Density history */
		BoundedSampleHistory densityHistory = new BoundedSampleHistory(10);

		/** Is bottleneck ? */
		private boolean isBottleneck = false;

		/** Is bottleneck at previous time step? */
		private boolean isPrevBottleneck = false;

		/** Detector set for the station : mainline, aux, not-wavetronics */
		DetectorSet detectorSet;

		/**
		 * Construct
		 * @param rnode
		 */
		public StationState(R_NodeImpl rnode) {
			this.rnode = rnode;
			station = rnode.station;
			// use mainline and auxiliary lane
			detectorSet = rnode.getDetectorSet().getDetectorSet(LaneType.MAINLINE);
			detectorSet.addDetectors(rnode.getDetectorSet().getDetectorSet(LaneType.AUXILIARY));
		}

		/**
		 * Update station state status
		 * It must be called before finding bottleneck.
		 */
		public void updateState() {
			if(station == null)
				return;
			Iterator<DetectorImpl> itr = detectorSet.detectors.iterator();
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
		 * Process after metering
		 */
		public void afterMetering() {
			isPrevBottleneck = isBottleneck;
			isBottleneck = false;
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
			StationState downStationState = getDownstreamStationState(idx);
			if(downStationState == null)
				return 0;
			double u1 = downStationState.getAggregatedSpeed();
			return (u1 * u1 - u2 * u2) / (2 * getDistanceInFeet(rnode, downStationState.rnode) / 5280);
		}

		/**
		 * Return associated entrance
		 * @return associated entrance
		 */
		public ArrayList<EntranceState> getAssociatedEntrances() {
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
			ArrayList<EntranceState> upstreamEntrances = getUpstreamEntrances();
			ArrayList<EntranceState> downstreamEntrances = getDownstreamEntrances();

			StationState us = null, ds = null;
			if(stationIdx > 0)
				us = stationStates.get(stationIdx - 1);
			if(stationIdx < stationStates.size() - 1)
				ds = stationStates.get(stationIdx + 1);

			if(us != null) {
				for(EntranceState es : upstreamEntrances) {
					if(!es.hasMeter())
						continue;
					int d = getDistanceToUpstreamEntrance(es);
					int ud = us.getDistanceToDownstreamEntrance(es);

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
				for(EntranceState es : downstreamEntrances) {
					if(!es.hasMeter())
						continue;
					int d = getDistanceToDownstreamEntrance(es);
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
		public ArrayList<EntranceState> getUpstreamEntrances() {
			ArrayList<EntranceState> list =
				new ArrayList<EntranceState>();
			if(idx <= 0)
				return list;
			for(int i = idx - 1; i >= 0; i--) {
				RNodeState s = states.get(i);
				if(s instanceof StationState)
					break;
				if(s instanceof EntranceState) {
					EntranceState es = (EntranceState)s;
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
		public ArrayList<EntranceState> getDownstreamEntrances() {
			ArrayList<EntranceState> list =
				new ArrayList<EntranceState>();
			if(idx >= states.size() - 1)
				return list;
			for(int i = idx + 1; i < states.size(); i++) {
				RNodeState s = states.get(i);
				if(s instanceof StationState)
					break;
				if(s instanceof EntranceState) {
					EntranceState es = (EntranceState)s;
					if(es.hasMeter())
						list.add(es);
				}
			}
			return list;
		}

		/**
		 * Return distance to upstream entrance
		 * @param es entrance state
		 * @return distance in feet
		 */
		public int getDistanceToUpstreamEntrance(EntranceState es) {
			if(idx <= 0)
				return -1;
			int distance = 0;
			RNodeState cursor = this;
			RNodeState s = null;

			boolean found = false;
			for(int i = idx - 1; i >= 0; i--) {
				s = states.get(i);
				distance += getDistanceInFeet(cursor.rnode, s.rnode);
				if(s.equals(es)) {
					found = true;
					break;
				}
				cursor = s;
			}
			if(found)
				return distance;
			else
				return -1;
		}

		/**
		 * Return distance to downstream entrance
		 * @param es entrance state
		 * @return distance in feet
		 */
		public int getDistanceToDownstreamEntrance(EntranceState es) {
			if(idx >= states.size() - 1)
				return -1;
			int distance = 0;
			RNodeState cursor = this;
			RNodeState s = null;

			boolean found = false;
			for(int i = idx + 1; i < states.size(); i++) {
				s = states.get(i);
				distance += getDistanceInFeet(cursor.rnode, s.rnode);
				if(s.equals(es)) {
					found = true;
					break;
				}
				cursor = s;
			}
			if(found)
				return distance;
			else
				return -1;
		}

		/** Debug a StationState */
		protected void debug(StringBuilder sb) {
			sb.append(rnode.station_id + " -> ");
			for(EntranceState es : associatedEntrances) {
				if(es != null && es.hasMeter())
					sb.append(es.meterState.meter.name + ", ");
			}
		}
	}

	/**
	 * Class : Entrance State to manage entrance
	 */
	class EntranceState extends RNodeState {

		/** Factor to compute ramp demand from passage/merge flow */
		private double PASSAGE_DEMAND_FACTOR = 1.15;

		/** Meter state mapping this entrance */
		private MeterState meterState;

		/** Associated station to metering */
		private StationState associatedStation;

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
		private StationState bottleneck;

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
		public EntranceState(R_NodeImpl rnode) {
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
			return meterState != null;
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
			DetectorSet ds = meterState.meter.getDetectorSet();
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
			DetectorSet ds = meterState.meter.getDetectorSet();
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
			meterState.meter.setRatePlanned(releaseRate);
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
		private void setBottleneck(StationState bottleneck) {
			this.bottleneck = bottleneck;
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

		/** Debug an EntranceState */
		protected void debug(StringBuilder sb) {
			if(hasMeter())
				sb.append("Ent(" + meterState.meter.name + ")");
			else
				sb.append("Ent(" + rnode.name + ")");
		}
	}

	/**
	 * Meter State to manage meter
	 */
	public class MeterState {

		RampMeterImpl meter;
		boolean valid;
		private EntranceState entrance;

		private MeterState(RampMeterImpl meter) {
			this.meter = meter;
		}

		/** Validate a ramp meter state */
		private void validate() {
			if(entrance != null)
				entrance.updateState();
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
