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

	/** Critical Density */
	static private final double K_CRIT = 40;

	/** Desired Density */
	static private final double K_DES = K_CRIT * 0.8;

	/** Jam Density */
	static private final double K_JAM = 180;

	/** Acceleration threshold to decide bottleneck */
	static private final int A_BOTTLENECK = 1000;

	/** How many time steps must be satisfied to stop metering */
	static private final int STOP_STEPS = 10;

	/** How many time steps for bottleneck trend after stop metering */
	static private final int BOTTLENECK_TREND_STEPS_AFTER_STOP = 4;

	/** Get the absolute minimum release rate */
	static protected int getMinRelease() {
		return SystemAttributeHelper.getMeterMinRelease();
	}

	/** Get the absolute maximum release rate */
	static protected int getMaxRelease() {
		return SystemAttributeHelper.getMeterMaxRelease();
	}

	/** States for all stratified zone corridors */
	static protected HashMap<String, KAdaptiveAlgorithm> all_states =
		new HashMap<String, KAdaptiveAlgorithm>();

	/** Lookup the stratified zone state for one corridor */
	static public KAdaptiveAlgorithm lookupCorridor(Corridor c) {
		KAdaptiveAlgorithm state = all_states.get(c.getID());
		if(state == null) {
			state = new KAdaptiveAlgorithm(c);
			all_states.put(c.getID(), state);
		}
		return state;
	}

	/** Process one interval for all stratified zone states */
	static public void processAllStates() {
		Iterator<KAdaptiveAlgorithm> it =
			all_states.values().iterator();
		while(it.hasNext()) {
			KAdaptiveAlgorithm state = it.next();
			state.processInterval();
			if(state.isDone())
				it.remove();
		}
	}

	/** Corridor */
	protected final Corridor corridor;

	/** Corridor Helper */
	protected CorridorHelper corridorHelper = new CorridorHelper();

	/** Bottleneck Finder */
	protected final BottleneckFinder bottleneckFinder;

	/** Bottleneck Density */
	protected double Kb = 25;

	/** How many time steps must be */
	protected int BottleneckTrendCount = 2;

	/** Rate value to calculated metering rate for checking if metering is started */
	protected double KstartThres = 0.8;

	/** Window for average density to check corridor state */
	protected int avgDensityWindow = 30;

	/** Trend count for average density to check corridor state */
	protected int avgDensityTrend = 10;

	/** Is started metering in this corridor? */
	protected boolean isMeteringStarted = false;

	/** Should do check stop condition ? (it depends on corridor's density trend) */
	protected boolean doStopChecking = false;

	/** Are station-entrance associated? */
	private boolean isAssociated = false;

	/** Corridor density history for triggering metering stop */
	protected BoundedSampleHistory<Double> corridorKHistory = new BoundedSampleHistory<Double>(avgDensityWindow + avgDensityTrend);

	/** Hash map of ramp meter states */
	protected final HashMap<String, MeterState> meterStates =
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
		corridorHelper.addMeterState(state);
		meterStates.put(meter.getName(), state);
		return state;
	}

	/** Lookup the meter state for a specified meter */
	protected MeterState lookupMeterState(RampMeter meter) {
		return meterStates.get(meter.getName());
	}

	/** Process the stratified plan for the next interval */
	protected void processInterval() {
		if(!this.isAssociated)
			this.corridorHelper.doAssociateStationAndEntrance();
		for(StationState s : this.corridorHelper.stationStates)
			s.updateState();
		bottleneckFinder.findBottlenecks();
		calculateMeteringRates();
		if(isMeteringStarted && !doStopChecking)
			checkCorridorState();
		else if(doStopChecking)
			checkStopCondition();
		afterMetering();
	}

	/**
	 * Check corridor average density condition
	 */
	private void checkCorridorState() {
		int bottleneckCount = 0;
		StationState downstreamBS = null;
		// calculate sum of density of bottlenecks
		for(StationState s : corridorHelper.stationStates) {
			if(s.isBottleneck) {
				downstreamBS = s;
				bottleneckCount++;
			}
		}
		double avgK = 0;
		StationState upStation = corridorHelper.stationStates.get(0);
		if(downstreamBS != null)
			avgK = corridorHelper.getAverageDensity(upStation, downstreamBS);
		corridorKHistory.push(avgK);
		int size = this.corridorKHistory.size();
		if(bottleneckCount > 1)
			return;
		if(size < avgDensityWindow + avgDensityTrend)
			return;
		// check avg K of corridor average density
		for(int i = 0; i < avgDensityTrend; i++) {
			double ma_next = this.corridorKHistory.getAverage(i, avgDensityWindow);
			double ma_prev = this.corridorKHistory.getAverage(i + 1, avgDensityWindow);
			if(ma_next > ma_prev)
				return;
		}

		// restrict condition
		Kb = K_DES;
		BottleneckTrendCount = BOTTLENECK_TREND_STEPS_AFTER_STOP;

		// let's check stop condition from now
		this.doStopChecking = true;
	}

	/**
	 * Stop metering of ramp meter satisfying conditions
	 */
	private void checkStopCondition() {

		int N = STOP_STEPS;

		// iterate from downstream to upstream
		boolean hasBottleneck = false;
		for(int i = corridorHelper.stationStates.size() - 1; i >= 0; i--) {
			StationState s = corridorHelper.stationStates.get(i);

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
		for(int i = corridorHelper.stationStates.size() - 1; i >= 0; i--) {
			StationState station = corridorHelper.stationStates.get(i);
			if(!station.isBottleneck || station.station == null)
				continue;
			calculateMeteringRates(station, i);
		}

		// when meter is not in zone
		for(StationState s : corridorHelper.stationStates)
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
			StationState upStation = corridorHelper.stationStates.get(i);
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
	 * Calculate metering rates
	 * @param bottleneck
	 * @return
	 */
	private double equation(StationState bottleneck, StationState upstream, EntranceState entrance) {

		//                       | Kt
		//                       |                 +
		//                       |
		//                       |
		//                       |             +
		//                       |
		//                       +
		//                       |
		//                       |
		//                 +     |
		//    +                  |
		// --p0------------p1----p2------------p3---p4-----> K_DES-Kt
		//                       |
		//                       |
		// p0's x = K_DES - K_JAM
		// p2's x = 0
		// p4's x = K_DES

		double Kt = bottleneck.getAggregatedDensity();
		if(upstream != null)
			Kt = corridorHelper.getAverageDensity(upstream, bottleneck);

		entrance.saveSegmentDensityHistory(Kt);

		double Rmin = entrance.getMinimumRate();
		double Rmax = getMaxRelease();
		double Rt = entrance.getRate();
		double x = K_DES - Kt;

		KPoint p0 = new KPoint(K_DES - K_JAM, Rmin / Rt);
		KPoint p1 = new KPoint((K_DES - K_JAM) / 3, Rmin / Rt + (1 - Rmin / Rt) / 3);
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
	private boolean checkStartCondition(StationState bs, StationState us, EntranceState es, double Rnext) {

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
				segmentDensity = corridorHelper.getAverageDensity(us, bs);
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
					if(q < KstartThres * rate)
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
				segmentDensity = corridorHelper.getAverageDensity(us, bs, i);
			else
				segmentDensity = bs.getAggregatedDensity(i);

			// Start Condition 1 : segment density > Kb
			if(segmentDensity < Kb)
				return false;

			// Start Condition 2 : Merging flow >= KstartRate
			double q = es.getFlow(i);
			double rate = es.getRate(i);
			if(q < KstartThres * rate)
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
		for(StationState s : corridorHelper.stationStates)
			s.afterMetering();
		for(EntranceState es : corridorHelper.entranceStates)
			es.setBottleneck(null);
	}

	/** Create a new KAdaptiveAlgorithm */
	private KAdaptiveAlgorithm(Corridor c) {
		this.corridor = c;
		this.createStates();
		bottleneckFinder = new BottleneckFinder(corridorHelper);
	}

	/** construct corridor structure */
	private void createStates() {
		Iterator<R_Node> itr = this.corridor.iterator();
		while(itr.hasNext()) {
			R_NodeImpl rnode = (R_NodeImpl) itr.next();
			int nType = rnode.getNodeType();
			if(nType == 1)   // entrance
				corridorHelper.addEntranceState(new EntranceState(rnode));
			else if (nType == 0) {    // station
				if (rnode.station_id != null && Integer.parseInt(rnode.station_id.substring(1)) / 100 != 17 /* check wavetronics */ && rnode.getDetectorSet().size() > 0) {
					corridorHelper.addStationState(new StationState(rnode));
				}
			}
		}
	}

	/** Is this KAdaptiveAlgorithm zone done? */
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
	 * Class : Corridor Helper
	 *     - manage station, entrance structures
	 */
	class CorridorHelper {

		/** All r_node states */
		ArrayList<RNodeState> states = new ArrayList<RNodeState>();
		/** Station states list in the corridor */
		ArrayList<StationState> stationStates = new ArrayList<StationState>();
		/** Entrance states list in the corridor */
		ArrayList<EntranceState> entranceStates = new ArrayList<EntranceState>();
		/** Meter states list in the corridor */
		ArrayList<MeterState> meters = new ArrayList<MeterState>();

		/**
		 * Add meter state
		 * @param state
		 */
		private void addMeterState(MeterState state) {
			meters.add(state);
			R_NodeImpl rnode = state.meter.getR_Node();
			for(EntranceState es : this.entranceStates) {
				if(es.rnode.equals(rnode)) {
					es.meterState = state;
					state.entrance = es;
					es.checkFreewayToFreeway();
				}
			}
		}

		/**
		 * Add entrance state
		 * @param entranceState
		 */
		private void addEntranceState(EntranceState entranceState) {
			entranceState.entranceIdx = entranceStates.size();
			entranceState.idx = states.size();
			this.entranceStates.add(entranceState);
			this.states.add(entranceState);
		}

		/**
		 * Add station state
		 * @param stationState
		 */
		private void addStationState(StationState stationState) {
			stationState.stationIdx = stationStates.size();
			stationState.idx = states.size();
			this.stationStates.add(stationState);
			this.states.add(stationState);
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
		 * Return upstream station state
		 * @param idx rnode-index of state
		 * @return upstream station state
		 */
		public StationState getUpstreamStationState(int idx) {
			if(idx <= 0 || idx >= states.size())
				return null;
			for(int i = idx - 1; i >= 0; i--) {
				RNodeState st = states.get(i);
				if(st.type.isStation())
					return (StationState) st;
			}
			return null;
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
				if(st.type.isStation())
					return (StationState) st;
			}
			return null;
		}

		/**
		 * Should be called after added all stations and meters
		 */
		public void doAssociateStationAndEntrance() {
			for(StationState s : stationStates)
				s.setAssociatedEntrances();

			/** for debugging of corridor structure */
			if(false) {
				System.err.println("Corridor Structure : " + corridor.getName() + " --------------------");
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < states.size(); i++) {
					RNodeState state = states.get(i);
					sb.append("[" + String.format("%02d", state.idx) + "] ");
					if(state.type.isStation()) {
						StationState ss = (StationState) state;
						sb.append(state.rnode.station_id + " -> ");
						for(EntranceState es : ss.getAssociatedEntrances()) {
							if(es != null && es.hasMeter())
								sb.append(es.meterState.meter.name + ", ");
						}
					}
					if(state.type.isEntrance()) {
						EntranceState e = (EntranceState) state;
					if(e.hasMeter())
						sb.append("Ent(" + e.meterState.meter.name + ")");
					else
						sb.append("Ent(" + e.rnode.name + ")");
					}
					sb.append("\n");
				}
				System.err.println(sb.toString());
			}
			isAssociated = true;
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
				StationState dStation = this.getDownstreamStationState(cursor.idx);
				double upDensity = cursor.getAggregatedDensity(prevStep);
				double downDensity = dStation.getAggregatedDensity(prevStep);
				double middleDensity = (upDensity + downDensity) / 2;
				double distance = this.getDistanceInFeet(cursor.rnode, dStation.rnode) / 5280;
				double distanceFactor = distance / 3;
				totalDistance += distance;
				avgDensity += (upDensity + middleDensity + downDensity) * distanceFactor;

				if(dStation.equals(downStation))
					break;
				cursor = dStation;
			}
			return avgDensity / totalDistance;
		}
	}

	/**
	 * Enum : R_Node State Type
	 */
	enum RSType {

		EntrancerState, StationState;

		public boolean isEntrance() {
			return this == RSType.EntrancerState;
		}

		public boolean isStation() {
			return this == RSType.StationState;
		}
	};

	/**
	 * Class : R_Node State to manage station and entrance (ancestor class)
	 */
	class RNodeState {
		RSType type;
		R_NodeImpl rnode;
		int idx;
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
		BoundedSampleHistory<Double> speedHistory = new BoundedSampleHistory<Double>(10);

		/** Density history */
		BoundedSampleHistory<Double> densityHistory = new BoundedSampleHistory<Double>(10);

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
			this.station = rnode.station;
			this.type = RSType.StationState;
			// use mainline and auxiliary lane
			this.detectorSet = this.rnode.getDetectorSet().getDetectorSet(LaneType.MAINLINE);
			this.detectorSet.addDetectors(this.rnode.getDetectorSet().getDetectorSet(LaneType.AUXILIARY));
		}

		/**
		 * Update station state status
		 * It must be called before finding bottleneck.
		 */
		public void updateState() {
			if(station == null)
				return;
			Iterator<DetectorImpl> itr = this.detectorSet.detectors.iterator();
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

			this.densityHistory.push(density);
			this.speedHistory.push(speed);
		}

		/**
		 * Process after metering
		 */
		public void afterMetering() {
			this.isPrevBottleneck = this.isBottleneck;
			this.isBottleneck = false;
		}

		/**
		 * Return aggregated density at current time step
		 * @return average 1min density
		 */
		public double getAggregatedDensity() {
			return this.getAggregatedDensity(0);
		}

		/**
		 * Return aggregated density at 'prevStep' time steps ago
		 * @return average 1min density at 'prevStep' time steps ago
		 */
		public double getAggregatedDensity(int prevStep) {
			return getAggregatedData(this.densityHistory, prevStep);
		}

		/**
		 * Return aggregated speed at current time step
		 * @return average 1min speed
		 */
		public double getAggregatedSpeed() {
			return this.getAggregatedSpeed(0);
		}

		/**
		 * Return aggregated speed at 'prevStep' time steps ago
		 * @param prevStep how many time steps ago?
		 * @return average 1min speed at 'prevStep' time steps ago
		 */
		public double getAggregatedSpeed(int prevStep) {
			return getAggregatedData(this.speedHistory, prevStep);
		}

		/**
		 * Return aggregated data (average)
		 * @param data
		 * @param prevStep
		 * @return average 1min data of given data at 'prevStep' time steps ago
		 */
		private double getAggregatedData(BoundedSampleHistory<Double> data, int prevStep) {
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
			double u2 = this.getAggregatedSpeed();
			StationState downStationState = corridorHelper.getDownstreamStationState(idx);
			if(downStationState == null)
				return 0;
			double u1 = downStationState.getAggregatedSpeed();
			return (u1 * u1 - u2 * u2) / (2 * corridorHelper.getDistanceInFeet(this.rnode, downStationState.rnode) / 5280);
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
			ArrayList<EntranceState> upstreamEntrances = this.getUpstreamEntrances();
			ArrayList<EntranceState> downstreamEntrances = this.getDownstreamEntrances();

			StationState us = null, ds = null;
			if(this.stationIdx > 0)
				us = corridorHelper.stationStates.get(this.stationIdx - 1);
			if(this.stationIdx < corridorHelper.stationStates.size() - 1)
				ds = corridorHelper.stationStates.get(this.stationIdx + 1);

			if(us != null) {
				for(EntranceState es : upstreamEntrances) {
					if(!es.hasMeter())
						continue;
					int d = this.getDistanceToUpstreamEntrance(es);
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
					int d = this.getDistanceToDownstreamEntrance(es);
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
			ArrayList<EntranceState> list = new ArrayList<EntranceState>();
			if(this.idx <= 0)
				return list;
			for(int i = this.idx - 1; i >= 0; i--) {
				RNodeState s = corridorHelper.states.get(i);
				if(s.type.isStation())
					break;
				if(s.type.isEntrance() && ((EntranceState) s).hasMeter())
					list.add((EntranceState) s);
			}
			return list;
		}

		/**
		 * Return downstream entrances up to next downstream station
		 * @return downstream entrance list
		 */
		public ArrayList<EntranceState> getDownstreamEntrances() {
			ArrayList<EntranceState> list = new ArrayList<EntranceState>();
			if(this.idx >= corridorHelper.states.size() - 1)
				return list;

			for(int i = this.idx + 1; i < corridorHelper.states.size(); i++) {
				RNodeState s = corridorHelper.states.get(i);
				if(s.type.isStation())
					break;
				if(s.type.isEntrance() && ((EntranceState) s).hasMeter())
					list.add((EntranceState) s);
			}
			return list;
		}

		/**
		 * Return distance to upstream entrance
		 * @param es entrance state
		 * @return distance in feet
		 */
		public int getDistanceToUpstreamEntrance(EntranceState es) {
			if(this.idx <= 0)
				return -1;
			int distance = 0;
			RNodeState cursor = this;
			RNodeState s = null;

			boolean found = false;
			for(int i = this.idx - 1; i >= 0; i--) {
				s = corridorHelper.states.get(i);
				distance += corridorHelper.getDistanceInFeet(cursor.rnode, s.rnode);
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
			if(this.idx >= corridorHelper.states.size() - 1)
				return -1;
			int distance = 0;
			RNodeState cursor = this;
			RNodeState s = null;

			boolean found = false;
			for(int i = this.idx + 1; i < corridorHelper.states.size(); i++) {
				s = corridorHelper.states.get(i);
				distance += corridorHelper.getDistanceInFeet(cursor.rnode, s.rnode);
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
	}

	/**
	 * Class : Entrance State to manage entrance
	 */
	class EntranceState extends RNodeState {

		/** Factor to compute ramp demand from passage/merge flow */
		private double PASSAGE_DEMAND_FACTOR = 1.15;

		/** Entrance index from upstream */
		private int entranceIdx;

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
		private BoundedSampleHistory<Double> cumulativeDemand = new BoundedSampleHistory<Double>(maxWaitTimeIndex+1);

		/** Cumulative merging flow history */
		private BoundedSampleHistory<Double> cumulativeMergingFlow = new BoundedSampleHistory<Double>(1);

		/** Metering rate flow history */
		private BoundedSampleHistory<Double> rateHistory = new BoundedSampleHistory<Double>(STOP_STEPS);

		/** Segment density history */
		private BoundedSampleHistory<Double> segmentDensityHistory = new BoundedSampleHistory<Double>(STOP_STEPS);

		/** Ramp flow history */
		private BoundedSampleHistory<Double> rampFlowHistory = new BoundedSampleHistory<Double>(STOP_STEPS);

		/** Ramp demand history */
		private BoundedSampleHistory<Double> rampDemandHistory = new BoundedSampleHistory<Double>(1);

		/**
		 * Construct
		 * @param rnode
		 */
		public EntranceState(R_NodeImpl rnode) {
			this.rnode = rnode;
			this.type = RSType.EntrancerState;
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

			this.isRateUpdated = false;
			calculateRampFlowAndDemand();

			double demand = this.rampDemandHistory.tail();
			double p_flow = this.rampFlowHistory.tail();

			double prevCd = 0;
			double prevCq = 0;

			if(this.cumulativeDemand.size() > 0)
				prevCd = this.cumulativeDemand.tail();
			if(this.cumulativeMergingFlow.size() > 0)
				prevCq = this.cumulativeMergingFlow.tail();

			this.cumulativeDemand.push(prevCd + demand);
			this.cumulativeMergingFlow.push(prevCq + p_flow);

			this.currentDemand = demand;
			this.currentFlow = p_flow;

			this.calculateMinimumRate();
		}

		/**
		 * Calculate ramp flow
		 * @return flow
		 */
		private int calculateRampFlow() {
			DetectorSet ds = this.meterState.meter.getDetectorSet();
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
			DetectorSet ds = this.meterState.meter.getDetectorSet();
			DetectorSet qDets = ds.getDetectorSet(LaneType.QUEUE);

			double rampFlow = calculateRampFlow();
			double rampDemand = 0;

			this.rampFlowHistory.push(rampFlow);

			// queue detector is ok
			if(qDets != null)
				rampDemand = qDets.getFlow();
			else
				rampDemand = rampFlow * PASSAGE_DEMAND_FACTOR;
			this.rampDemandHistory.push(rampDemand);
		}

		/**
		 * Calculate minimum rate according to waiting time
		 */
		private void calculateMinimumRate() {
			if(this.cumulativeDemand.size() - 1 < maxWaitTimeIndex) {
				minimumRate = this.currentFlow;
				return;
			}

			// cumulative demand 4 min ago
			double Cd_4mAgo = this.cumulativeDemand.get(maxWaitTimeIndex);

			// current cumulative passage flow
			double Cf_current = this.cumulativeMergingFlow.tail();

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
			if(!this.hasMeter())
				return;
			this.currentRate = Rnext;
			this.isRateUpdated = true;
			int releaseRate = (int) Math.round(Rnext);
			this.meterState.meter.setRatePlanned(releaseRate);
		}

		/**
		 * Stop metering
		 */
		public void stopMetering() {
			this.isMetering = false;
			this.currentDemand = 0;
			this.currentFlow = 0;
			this.currentRate = 0;
			this.rateHistory.clear();
			this.noBottleneckCount = 0;
			this.hasBeenStoped = true;
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
			this.rateHistory.push(Rnext);
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
			return this.rampFlowHistory.get(prevStep);
		}

		/**
		 * Return current metering rate
		 * @return metering rate
		 */
		private double getRate() {
			// initial rate = average(last 3 flows) or MAX_RATE
			if(this.currentRate == 0) {
				this.currentRate = this.currentFlow;
				double flowSum = 0;
				int cnt = 0;

				for(int i = 0; i < 3; i++) {
					flowSum += this.rampFlowHistory.get(i);
					cnt++;
				}

				if(cnt > 0)
					this.currentRate = flowSum / cnt;
				else
					this.currentRate = getMaxRelease();  // no flow
			}
			return this.currentRate;
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
			this.isMetering = true;
		}

		/**
		 * Save segment density history
		 * @param Kt
		 */
		private void saveSegmentDensityHistory(double Kt) {
			this.segmentDensityHistory.push(Kt);
		}

		/**
		 * Return segment density at 'prevStep' time steps ago
		 * @param prevStep
		 * @return segment density at 'prevStep' time steps ago
		 */
		private double getSegmentDensity(int prevStep) {
			return this.segmentDensityHistory.get(prevStep);
		}

		/**
		 * Return minimum metering rate
		 * @return minimum metering rate
		 */
		private double getMinimumRate() {
			return this.minimumRate;
		}

		/**
		 * Reset no bottleneck count
		 */
		private void resetNoBottleneckCount() {
			this.noBottleneckCount = 0;
		}

		/**
		 * Add no bottleneck count
		 */
		public void addNoBottleneckCount() {
			this.noBottleneckCount++;
		}

		/**
		 * Return no bottleneck count
		 * @return no-bottleneck count
		 */
		public int getNoBottleneckCount() {
			return this.noBottleneckCount;
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
			if(this.entrance != null)
				this.entrance.updateState();
		}
	}

	/**
	 * Class : Bottleneck Finder
	 */
	public class BottleneckFinder {

		CorridorHelper corridorHelper;

		/**
		 * Constructor
		 * @param corridorHelper
		 */
		public BottleneckFinder(CorridorHelper corridorHelper) {
			this.corridorHelper = corridorHelper;
		}

		/**
		 * Find bottlenecks
		 */
		public void findBottlenecks() {

			ArrayList<StationState> stationStates = corridorHelper.stationStates;

			// find bottleneck candidates
			for(int i = 0; i < stationStates.size(); i++) {
				StationState s = stationStates.get(i);

				if(s.getAggregatedDensity() < Kb)
					continue;

				boolean increaseTrend = true;
				boolean highDensity = true;

				for(int j = 0; j < BottleneckTrendCount; j++) {
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
