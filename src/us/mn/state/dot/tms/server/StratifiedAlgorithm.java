/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2012  Minnesota Department of Transportation
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.server.RampMeterImpl.getMaxRelease;
import static us.mn.state.dot.tms.server.RampMeterImpl.getMinRelease;

/**
 * Stratified metering algorithm
 *
 * @author Douglas Lau
 */
public class StratifiedAlgorithm implements MeterAlgorithmState {

	/** Zone debug log */
	static private final DebugLog SZM_LOG = new DebugLog("szm");

	/** Path where meter data files are stored */
	static protected final String DATA_PATH = "/var/lib/iris/meter";

	/** Constant for standard filter equation */
	static protected final float K = 0.15f;

	/** Constant for filtering accumulated release rate */
	static protected final float K_RATE_ACCUM = 0.2f;

	/** Total number of layers in stratified algorithm */
	static protected final int TOTAL_LAYERS = 6;

	/** Queue occupancy override threshold */
	static protected final int QUEUE_OCC_THRESHOLD = 25;

	/** Queue override demand increment */
	static protected final int QUEUE_OVERRIDE_INCREMENT = 150;

	/** Distance of queue from detector at queue occ threshold (feet) */
	static private final int QUEUE_THRESHOLD_FT = 100;

	/** Factor to determine whether a queue exists */
	static protected final float QUEUE_EXISTS_FACTOR = 0.8f;

	/** Slope for queue density calculation */
	static protected final float DENSITY_SLOPE = -0.03445f;

	/** Y-intercept for queue density calculation */
	static protected final float DENSITY_Y_INTERCEPT = 206.715f;

	/** Factor to compute ramp demand from passage/merge flow */
	static protected final float PASSAGE_DEMAND_FACTOR = 1.15f;

	/** Adjustment to passage/merge flow to compute ramp demand */
	static protected final int PASSAGE_DEMAND_ADJUSTMENT = 60;

	/** Ramp meter demand turn on threshold (first half window) */
	static protected final float TURN_ON_THRESHOLD_1 = 0.8f;

	/** Ramp meter demand turn on threshold (second half window) */
	static protected final float TURN_ON_THRESHOLD_2 = 1.0f;

	/** Ramp meter demand turn off threshold (second half window) */
	static protected final float TURN_OFF_THRESHOLD = 0.5f;

	/** Number of minutes to flush meter before shutoff */
	static protected final int FLUSH_MINUTES = 2;

	/** States for all stratified zone corridors */
	static protected HashMap<String, StratifiedAlgorithm> all_states =
		new HashMap<String, StratifiedAlgorithm>();

	/** Lookup the stratified zone state for one corridor */
	static public StratifiedAlgorithm lookupCorridor(Corridor c) {
		StratifiedAlgorithm state = all_states.get(c.getID());
		if(state == null) {
			state = new StratifiedAlgorithm(c);
			all_states.put(c.getID(), state);
		}
		return state;
	}

	/** Process one interval for all stratified zone states */
	static public void processAllStates() {
		Iterator<StratifiedAlgorithm> it =
			all_states.values().iterator();
		while(it.hasNext()) {
			StratifiedAlgorithm state = it.next();
			state.processInterval();
			if(state.isDone())
				it.remove();
		}
	}

	/** Get the number of metered lanes */
	static protected int getMeteringLanes(RampMeter m) {
		return RampMeterType.fromOrdinal(m.getMeterType()).lanes;
	}

	/** Create a set of entrance detectors for a zone */
	static protected DetectorSet createEntranceSet(DetectorSet ds) {
		DetectorSet ent = ds.getDetectorSet(LaneType.BYPASS);
		ent.addDetectors(ds, LaneType.OMNIBUS);
		DetectorSet p = ds.getDetectorSet(LaneType.PASSAGE);
		if(p.isDefined())
			ent.addDetectors(p);
		else
			ent.addDetectors(ds, LaneType.MERGE);
		if(ent.size() > 0)
			return ent;
		ent.addDetectors(ds, LaneType.EXIT);
		ent.addDetectors(ds, LaneType.MAINLINE);
		ent.addDetectors(ds, LaneType.AUXILIARY);
		return ent;
	}

	/** Create a set of exit detectors for a zone */
	static protected DetectorSet createExitSet(DetectorSet ds) {
		DetectorSet exit = ds.getDetectorSet(LaneType.EXIT);
		if(exit.size() > 0)
			return exit;
		exit.addDetectors(ds, LaneType.MAINLINE);
		exit.addDetectors(ds, LaneType.AUXILIARY);
		exit.addDetectors(ds, LaneType.CD_LANE);
		if(exit.size() > 0)
			return exit;
		exit.addDetectors(ds, LaneType.BYPASS);
		DetectorSet q = ds.getDetectorSet(LaneType.QUEUE);
		if(q.size() > 0) {
			exit.addDetectors(q);
			return exit;
		}
		DetectorSet p = ds.getDetectorSet(LaneType.PASSAGE);
		if(p.size() > 0) {
			exit.addDetectors(p);
			return exit;
		}
		exit.addDetectors(ds, LaneType.MERGE);
		return exit;
	}

	/** Get a stamp of the current 30 second interval */
	static public String stamp_30() {
		int i30 = TimeSteward.currentSecondOfDayInt() / 30 + 1;
		StringBuilder b = new StringBuilder();
		b.append(i30 / 120);
		while(b.length() < 2)
			b.insert(0, '0');
		b.append(':');
		b.append((i30 % 120) / 2);
		while(b.length() < 5)
			b.insert(3, '0');
		b.append(':');
		b.append((i30 % 2) * 30);
		while(b.length() < 8)
			b.insert(6, '0');
		return b.toString();
	}

	/** Meter state holds stratified plan state for a meter. For each meter
	 *  in the stratified plan, there will be one MeterState object. */
	protected class MeterState {

		/** Ramp meter */
		protected final RampMeterImpl meter;

		/** Queue detector set */
		protected DetectorSet queue;

		/** Passage detector set */
		protected DetectorSet passage;

		/** Merge detector set */
		protected DetectorSet merge;

		/** Bypass detector set */
		protected DetectorSet bypass;

		/** Accumulated release rate */
		protected float rate_accum;

		/** Smoothed flow rate at the passage (or merge) */
		protected float p_flow;

		/** Queue probability factor */
		protected float q_prob;

		/** Has queue flag */
		protected boolean has_queue;

		/** Queue density */
		protected float density;

		/** Maximum number of stored vehicles in the queue */
		protected float max_stored;

		/** Flag if meter state is valid */
		protected final boolean valid;

		/** Minimum release rate */
		protected int minimum;

		/** Demand rate assigned */
		protected int demand;

		/** Release rate assigned.  This rate is a combination of
		 * rates assigned by all zones for the meter. */
		protected int release;

		/** Metering flag */
		protected boolean metering;

		/** Warning flag for finding plan errors */
		protected boolean warning;

		/** Flag indicating at least one good zone */
		protected boolean good;

		/** Queue backup flag */
		protected boolean queue_backup;

		/** Zone (rule) which is controlling the meter */
		protected Zone control;

		/** Proposed rate (used for zone rule balancing) */
		protected int prop;

		/** Zone rule rate (used for zone rule balancing) */
		protected int rate;

		/** Done flag (used for zone rule balancing) */
		protected boolean done;

		/** Create a new ramp meter state */
		protected MeterState(RampMeterImpl m) {
			meter = m;
			valid = findDetectors();
			rate_accum = getMaxRelease();
			p_flow = getMaxRelease();
			release = getMaxRelease();
			minimum = getMinRelease();
			demand = getMinRelease();
			good = false;
			has_queue = false;
			queue_backup = false;
		}

		/** Reset the meter's zone state */
		protected void reset() {
			if(good)
				release = getMaxRelease();
			else
				release = meter.getTarget();
			control = null;
		}

		/** Reset the zone rule state */
		protected void resetRule() {
			prop = 0;
			rate = getMaxRelease();
			done = false;
		}

		/** Apply the zone rule */
		protected void applyRule(Zone zone) {
			if(passage.isPerfect() || queue.isPerfect() ||
				(merge.isPerfect() && bypass.isPerfect()))
			{
				warning = false;
			}
			if(rate < release) {
				release = rate;
				control = zone;
			}
		}

		/** Find the detectors for a given ramp meter */
		protected boolean findDetectors() {
			DetectorSet ds = meter.getDetectorSet();
			queue = ds.getDetectorSet(LaneType.QUEUE);
			passage = ds.getDetectorSet(LaneType.PASSAGE);
			merge = ds.getDetectorSet(LaneType.MERGE);
			bypass = ds.getDetectorSet(LaneType.BYPASS);
			return queue.isDefined() || passage.isDefined() ||
				merge.isDefined();
		}

		/** Get the metered detector set */
		protected DetectorSet getMetered() {
			if(passage.isDefined())
				return passage;
			else
				return merge;
		}

		/** Validate a ramp meter state */
		protected void validate() {
			if(valid)
				computeDemand();
			if(metering) {
				if(isFlushing() && !has_queue)
					stopMetering();
			} else {
				float thresh = rate_accum;
				if(isFirstHalf())
					thresh *= TURN_ON_THRESHOLD_1;
				else
					thresh *= TURN_ON_THRESHOLD_2;
				if(demand > thresh)
					startMetering();
			}
		}

		/** Check if we're in the flushing window */
		protected boolean isFlushing() {
			int min = TimeSteward.currentMinuteOfDayInt();
			int stop_min = meter.getStopMin();
			return min >= stop_min - FLUSH_MINUTES &&
			       min <= stop_min;
		}

		/** Check if we're in the first half of the plan window */
		protected boolean isFirstHalf() {
			int min = TimeSteward.currentMinuteOfDayInt();
			return min * 2 < meter.getStartMin()+meter.getStopMin();
		}

		/** Start metering */
		protected void startMetering() {
			metering = true;
			has_queue = false;
			queue_backup = false;
		}

		/** Stop metering */
		protected void stopMetering() {
			metering = false;
			has_queue = false;
			queue_backup = false;
		}

		/** Compute the demand for the ramp meter */
		protected void computeDemand() {
			queue_backup = false;
			warning = true;

			int r = getReleaseRate();
			rate_accum += K_RATE_ACCUM * (r - rate_accum);
			density = DENSITY_SLOPE * rate_accum +
				DENSITY_Y_INTERCEPT;
			Distance storage = computeStorage();
			max_stored = density * storage.asFloat(MILES);
			Interval max_cycle = new Interval(meter.getMaxWait() /
				max_stored);
			minimum = (int)max_cycle.per(Interval.HOUR);
			int p_demand = calculatePassageDemand();
			int t_demand = demand;
			q_prob = Math.min(p_flow / rate_accum, 1.0f);
			if(queue.getMaxOccupancy() > QUEUE_OCC_THRESHOLD) {
				q_prob = 1;
				queue_backup = true;
				t_demand += QUEUE_OVERRIDE_INCREMENT;
				minimum = Math.max(minimum, t_demand);
			} else if(queue.isPerfect()) {
				t_demand +=
					(int)(K * (queue.getFlow() - t_demand));
				minimum = Math.round(minimum * q_prob);
				minimum = Math.min(minimum, p_demand);
			} else {
				t_demand = p_demand;
				minimum = p_demand;
			}
			if(!metering) {
				q_prob = 0;
				minimum = getMinRelease();
			} else if(isFlushing())
				minimum = getMaxRelease();
			has_queue = q_prob > QUEUE_EXISTS_FACTOR;
			demand = t_demand;
		}

		/** Compute the storage distance of a meter */
		private Distance computeStorage() {
			int s = meter.getStorage();
			s -= QUEUE_THRESHOLD_FT * getMeteringLanes(meter);
			s = Math.max(s, 1);
			return new Distance(s, FEET);
		}

		/** Get the most recent release rate from the meter */
		protected int getReleaseRate() {
			Integer r = meter.getRate();
			if(r != null)
				return r;
			else
				return release;
		}

		/** Calculate the passage demand (and smoothed flow) */
		protected int calculatePassageDemand() {
			int p;
			if(passage.isPerfect())
				p = passage.getFlow();
			else if(merge.isPerfect()) {
				p = merge.getFlow();
				if(bypass.isPerfect()) {
					p -= bypass.getFlow();
					if(p < 0)
						p = 0;
				}
			} else {
				p_flow = getMaxRelease();
				return getMaxRelease();
			}
			p_flow += K_RATE_ACCUM * (p - p_flow);
			p *= PASSAGE_DEMAND_FACTOR;
			return demand + (int)(K * (p - demand));
		}

		/** Send a release rate to the ramp meter */
		protected void sendRate() {
			if(metering)
				meter.setRatePlanned(release);
		}

		/** Check for the existence of a queue */
		private RampMeterQueue getQueueState() {
			if(metering) {
				if(queue_backup)
					return RampMeterQueue.FULL;
				else if(has_queue)
					return RampMeterQueue.EXISTS;
				else
					return RampMeterQueue.EMPTY;
			}
			return RampMeterQueue.UNKNOWN;
		}

		/** Print the meter setup */
		protected void print(PrintStream stream) {
			StringBuffer buf = new StringBuffer();
			buf.append("  <meter id='");
			buf.append(meter.getName());
			buf.append('\'');
			if(queue.isDefined()) {
				buf.append(" queue=");
				buf.append(queue);
			}
			if(passage.isDefined()) {
				buf.append(" passage=");
				buf.append(passage);
			}
			if(merge.isDefined()) {
				buf.append(" merge=");
				buf.append(merge);
			}
			if(bypass.isDefined()) {
				buf.append(" bypass=");
				buf.append(bypass);
			}
			buf.append(" />");
			stream.println(buf);
		}

		/** Print the meter state */
		protected void printState(PrintStream stream) {
			StringBuffer buf = new StringBuffer();
			buf.append("    <meter_state id='");
			buf.append(meter.getName());
			if(queue.isPerfect()) {
				buf.append("' Q='");
				buf.append(queue.getFlow());
				buf.append("' O='");
				buf.append((int)queue.getMaxOccupancy());
				buf.append("' N='");
				buf.append(Math.round(density));
				buf.append("' T='");
				buf.append(Math.round(max_stored));
			}
			buf.append("' Q_prob='");
			buf.append(Math.round(q_prob * 100));
			buf.append("' P_flow='");
			buf.append(Math.round(p_flow));
			buf.append("' R_acc='");
			buf.append(Math.round(rate_accum));
			buf.append("' R_min='");
			buf.append(minimum);
			buf.append("' D='");
			buf.append(demand);
			buf.append("' R='");
			buf.append(release);
			if(control != null) {
				buf.append("' Z='");
				buf.append(control.getId());
			}
			if(warning && good && meter.isActive() &&
			   !meter.isFailed())
			{
				buf.append("' warning='1");
			}
			buf.append("' />");
			stream.println(buf);
		}
	}

	/** Zone is one individual zone within the stratified plan */
	protected class Zone implements Comparable<Zone> {

		/** Layer number */
		protected final int layer;

		/** Zone number within layer */
		protected final int znum;

		/** Count of stations in the zone */
		protected int n_stations;

		/** Compare for sorting by layer/number */
		public int compareTo(Zone ozone) {
			if(layer != ozone.layer)
				return layer - ozone.layer;
			else
				return znum - ozone.znum;
		}

		/** Mainline upstream detectors */
		protected final DetectorSet upstream = new DetectorSet();

		/** Entrance (on-ramp; passage, bypass or merge) detectors */
		protected final DetectorSet entrance = new DetectorSet();

		/** Exit (off-ramp) detectors */
		protected final DetectorSet exit = new DetectorSet();

		/** Mainline detectors within the zone */
		protected final DetectorSet mainline = new DetectorSet();

		/** Downstream mainline detectors */
		protected final DetectorSet downstream = new DetectorSet();

		/** List of ramp meter states within the zone */
		protected final LinkedList<MeterState> meters =
			new LinkedList<MeterState>();

		/** Combined release rate for all meters in the zone */
		protected int M;

		/** Downstream capacity */
		protected int B;

		/** Exit ramp flow rate (filtered) */
		protected float X;

		/** Spare capacity on mainline */
		protected float S;

		/** Upstream flow rate (filtered) */
		protected float A;

		/** Unmetered entrance flow rate (filtered) */
		protected float U;

		/** Create a new zone */
		protected Zone(int l, int n) {
			layer = l;
			znum = n;
			n_stations = 0;
		}

		/** Add a mainline station to the zone */
		protected void addStation(DetectorSet ds) {
			addMainline(ds);
			if(n_stations == 0)
				addUpstream(ds);
			if(n_stations == layer)
				addDownstream(ds);
			n_stations++;
		}

		/** Is the zone completely defined? */
		protected boolean isComplete() {
			return n_stations > layer;
		}

		/** Is the zone valid? */
		protected boolean isValid() {
			return isComplete() && entrance.size() > 0;
		}

		/** Get the zone ID */
		public String getId() {
			return "Z" + layer + '~' + znum;
		}

		/** Test if the zone is properly defined */
		protected boolean isDefined() {
			return entrance.isDefined() && downstream.isDefined();
		}

		/** Add an upstream station to the zone */
		protected void addUpstream(DetectorSet ds) {
			for(DetectorImpl det: ds.toArray()) {
				if(det.isStation())
					upstream.addDetector(det);
				else
					entrance.addDetector(det);
			}
		}

		/** Add a mainline station to the zone */
		protected void addMainline(DetectorSet ds) {
			for(DetectorImpl det: ds.toArray()) {
				if(det.isStation())
					mainline.addDetector(det);
			}
		}

		/** Add a downstream station to the zone */
		protected void addDownstream(DetectorSet ds) {
			for(DetectorImpl det: ds.toArray()) {
				if(det.isStation())
					downstream.addDetector(det);
				else
					exit.addDetector(det);
			}
		}

		/** Add an entrance to the zone */
		protected void addEntrance(DetectorSet ds) {
			entrance.addDetectors(ds);
		}

		/** Add an exit to the zone */
		protected void addExit(DetectorSet ds) {
			exit.addDetectors(ds);
		}

		/** Add a meter if it's within the zone */
		protected void addMeter(MeterState state) {
			if(entrance.removeDetectors(state.getMetered())) {
				meters.add(state);
				zone_change = true;
			}
		}

		/** Calculate the zone release rate */
		protected void calculateRate() {
			A += K * (upstream.getFlow() - A);
			U += K * (entrance.getFlow() - U);
			X += K * (exit.getFlow() - X);
			S = mainline.getUpstreamCapacity() * upstream.size();
			B = downstream.getCapacity();
			M = Math.round(B + X + S - A - U);
			if(M < 1)
				M = 1;
		}

		/** Check if the zone data is good */
		protected boolean isGood() {
			return upstream.isGood() &&
			       entrance.isNotBad() &&
			       exit.isNotBad() &&
			       mainline.isFlowing() &&
			       meters.size() > 0;
		}

		/** Set meter states to good if the zone is good */
		protected void testGood() {
			if(isGood()) {
				for(MeterState state: meters)
					state.good = true;
			}
		}

		/** Process the zone */
		protected void process() {
			for(MeterState state: meters)
				state.resetRule();
			while(balance() != 0);
			for(MeterState state: meters)
				state.applyRule(this);
		}

		/** Balance the zone rules */
		protected int balance() {
			int rate = M;
			int demand = 0;
			int delta = 0;
			// Calculate the remaining demand and rates
			for(MeterState state: meters) {
				if(state.done)
					rate -= state.release;
				else
					demand += state.demand;
			}
			// Distribute the metering rates within the zone
			for(MeterState state: meters) {
				if(state.done)
					continue;
				RampMeterImpl meter = state.meter;
				int r = rate * state.demand / demand;
				state.prop = r;
				rate -= r;
				demand -= state.demand;
				if(r > state.release) {
					delta += r - state.release;
					r = state.release;
				}
				if(r < state.minimum) {
					delta -= state.minimum - r;
					r = state.minimum;
				}
				state.rate = r;
			}
			if(delta == 0)
				return 0;
			for(MeterState state: meters) {
				if((delta > 0) && (state.prop > state.release))
					state.done = true;
				if((delta < 0) && (state.prop < state.minimum))
					state.done = true;
			}
			return delta;
		}

		/** Test whether the zone rule is "broken" */
		protected boolean isBroken() {
			int release = 0;
			boolean control = false;
			for(MeterState state: meters) {
				release += state.release;
				if(state.control == this)
					control = true;
			}
			if(M > release)
				return control;
			else
				return false;
		}

		/** Reset state of all meters controlled by this zone */
		protected void resetControlled() {
			for(MeterState state: meters) {
				if(state.control == this)
					state.reset();
			}
		}

		/** Print the zone setup */
		protected void print(PrintStream stream) {
			StringBuffer buf = new StringBuffer();
			buf.append("  <zone id='");
			buf.append(getId());
			buf.append("' upstream=");
			buf.append(upstream);
			if(entrance.isDefined()) {
				buf.append(" entrance=");
				buf.append(entrance);
			}
			if(exit.isDefined()) {
				buf.append(" exit=");
				buf.append(exit);
			}
			buf.append(" mainline=");
			buf.append(mainline);
			buf.append(" downstream=");
			buf.append(downstream);
			buf.append(" meters='");
			for(MeterState state: meters) {
				buf.append(state.meter.getName());
				buf.append(' ');
			}
			if(meters.size() == 0)
				buf.append('\'');
			else
				buf.setCharAt(buf.length() - 1, '\'');
			buf.append(" />");
			stream.println(buf);
		}

		/** Print the zone state */
		protected void printState(PrintStream stream) {
			StringBuffer buf = new StringBuffer();
			buf.append("    <zone_state id='");
			buf.append(getId());
			if(isGood()) {
				buf.append("' M='");
				buf.append(M);
				buf.append("' B='");
				buf.append(B);
				buf.append("' X='");
				buf.append(Math.round(X));
				buf.append("' S='");
				buf.append(Math.round(S));
				buf.append("' A='");
				buf.append(Math.round(A));
				buf.append("' U='");
				buf.append(Math.round(U));
			} else {
				buf.append("' M='");
				if(meters.size() > 0)
					buf.append("exist");
				else
					buf.append("none");
				buf.append("' X='");
				if(exit.isNotBad())
					buf.append("good");
				else
					buf.append("bad");
				buf.append("' S='");
				if(isCongested())
					buf.append("congested");
				else
					buf.append("flowing");
				buf.append("' A='");
				if(upstream.isGood())
					buf.append("good");
				else
					buf.append("bad");
	   			buf.append("' U='");
				if(entrance.isNotBad())
					buf.append("good");
				else
					buf.append("bad");
			}
			buf.append("' />");
			stream.println(buf);
		}

		/** Check if the zone is congested */
		protected boolean isCongested() {
			return !mainline.isFlowing();
		}
	}

	/** Inner class to build zones */
	protected class ZoneBuilder implements Corridor.NodeFinder {
		TreeSet<Zone> _zones = new TreeSet<Zone>();
		int znum = 0;
		protected void removeInvalidZones() {
			Iterator<Zone> it = _zones.iterator();
			while(it.hasNext()) {
				Zone z = it.next();
				if(!z.isValid())
					it.remove();
			}
		}
		protected void addStation(DetectorSet ds) {
			znum++;
			for(int layer = 1; layer <= TOTAL_LAYERS; layer++)
				_zones.add(new Zone(layer, znum));
			for(Zone z: _zones) {
				if(!z.isComplete())
					z.addStation(ds);
			}
		}
		protected void addEntrance(DetectorSet ds) {
			DetectorSet ent = createEntranceSet(ds);
			for(Zone z: _zones) {
				if(!z.isComplete())
					z.addEntrance(ent);
			}
		}
		protected void addExit(DetectorSet ds) {
			DetectorSet exit = createExitSet(ds);
			for(Zone z: _zones) {
				if(!z.isComplete())
					z.addExit(exit);
			}
		}
		protected void followEntrance(R_NodeImpl n) {
			GeoLoc branch = n.getGeoLoc();
			Corridor c = n.getLinkedCorridor();
			if(c != null) {
				Corridor.NodeFinder nf =
					new EntranceFollower(this, branch);
				if(c.findActiveNodeReverse(nf) != null)
					return;
			}
			SZM_LOG.log("Missing entrance detection @ " +
				GeoLocHelper.getDescription(branch));
		}
		protected void followExit(R_NodeImpl n) {
			GeoLoc branch = n.getGeoLoc();
			Corridor c = n.getLinkedCorridor();
			if(c != null) {
				Corridor.NodeFinder nf =
					new ExitFollower(this, branch);
				if(c.findActiveNode(nf) != null)
					return;
			}
			SZM_LOG.log("Missing exit detection @ " +
				GeoLocHelper.getDescription(branch));
		}
		public boolean check(R_NodeImpl n) {
			if(R_NodeHelper.isIntersection(n)) {
				removeInvalidZones();
				return false;
			}
			DetectorSet ds = n.getDetectorSet();
			if(ds.size() == 0) {
 				if(R_NodeHelper.isEntrance(n))
					followEntrance(n);
				if(R_NodeHelper.isExit(n))
					followExit(n);
				return false;
			}
			if(R_NodeHelper.isStation(n))
				addStation(ds);
			else if(R_NodeHelper.isEntrance(n))
				addEntrance(ds);
			else if(R_NodeHelper.isExit(n))
				addExit(ds);
			return false;
		}
		public LinkedList<Zone> getList() {
			LinkedList<Zone> zl = new LinkedList<Zone>();
			for(Zone z: _zones) {
				if(z.isValid())
					zl.add(z);
			}
			return zl;
		}
	}

	/** Inner class to add entrances to zones */
	protected class EntranceFollower implements Corridor.NodeFinder {

		/** Zone builder for the current corridor */
		protected final ZoneBuilder zone_builder;

		/** Location of the entrance node onto corridor */
		protected final GeoLoc branch;

		/** Have we found the matching exit node to branch? */
		protected boolean found;

		protected EntranceFollower(ZoneBuilder zb, GeoLoc b) {
			zone_builder = zb;
			branch = b;
			found = false;
		}
		public boolean check(R_NodeImpl n) {
			if(found)
				return check_found(n);
			else
				return check_not_found(n);
		}
		protected boolean check_found(R_NodeImpl n) {
			if(n.getTransition()==R_NodeTransition.COMMON.ordinal())
				return true;
			else
				return check_found_inside(n);
		}
		protected boolean check_found_inside(R_NodeImpl n) {
			if(R_NodeHelper.isIntersection(n))
				return true;
			DetectorSet ds = n.getDetectorSet();
			if(ds.size() > 0) {
				if(R_NodeHelper.isEntrance(n)) {
					zone_builder.addEntrance(ds);
					if(n.getLanes() == 0)
						return true;
				}
				if(R_NodeHelper.isStation(n) && is_not_CD(n)) {
					zone_builder.addEntrance(ds);
					return true;
				}
			}
			return false;
		}
		protected boolean is_not_CD(R_NodeImpl n) {
			GeoLoc loc = n.getGeoLoc();
			return !GeoLocHelper.matchesRoot(loc, branch);
		}
		protected boolean check_not_found(R_NodeImpl n) {
			if(!R_NodeHelper.isExit(n))
				return false;
			GeoLoc loc = n.getGeoLoc();
			if(GeoLocHelper.rampMatches(loc, branch)) {
				found = true;
				DetectorSet ds = n.getDetectorSet();
				if(ds.size() > 0) {
					zone_builder.addEntrance(ds);
					return true;
				}
			}
			return false;
		}
	}

	/** Inner class to add exits to zones */
	protected class ExitFollower implements Corridor.NodeFinder {

		/** Zone builder for the current corridor */
		protected final ZoneBuilder zone_builder;

		/** Location of the exit node onto corridor */
		protected final GeoLoc branch;

		/** Have we found the matching entrance node to branch? */
		protected boolean found;

		protected ExitFollower(ZoneBuilder zb, GeoLoc b) {
			zone_builder = zb;
			branch = b;
			found = false;
		}
		public boolean check(R_NodeImpl n) {
			if(found)
				return check_found(n);
			else
				return check_not_found(n);
		}
		protected boolean check_found(R_NodeImpl n) {
			if(n.getTransition()==R_NodeTransition.COMMON.ordinal())
				return true;
			else
				return check_found_inside(n);
		}
		protected boolean check_found_inside(R_NodeImpl n) {
			if(R_NodeHelper.isIntersection(n))
				return true;
			DetectorSet ds = n.getDetectorSet();
			if(ds.size() > 0) {
				if(R_NodeHelper.isStation(n)) {
					zone_builder.addExit(ds);
					return true;
				} else if(R_NodeHelper.isExit(n))
					zone_builder.addExit(ds);
			}
			return false;
		}
		protected boolean check_not_found(R_NodeImpl n) {
			if(!R_NodeHelper.isEntrance(n))
				return false;
			GeoLoc loc = n.getGeoLoc();
			if(GeoLocHelper.rampMatches(loc, branch)) {
				found = true;
				DetectorSet ds = n.getDetectorSet();
				if(ds.size() > 0) {
					zone_builder.addExit(ds);
					return true;
				}
			}
			return false;
		}
	}

	/** Hash map of ramp meter states */
	protected final HashMap<String, MeterState> states =
		new HashMap<String, MeterState>();

	/** Linked list of zones */
	protected final LinkedList<Zone> zones = new LinkedList<Zone>();

	/** Zone change flag */
	protected boolean zone_change = false;

	/** Corridor */
	protected final Corridor corridor;

	/** Current log file name */
	protected File log_name;

	/** Create a new stratified algorithm state */
	protected StratifiedAlgorithm(Corridor c) {
		corridor = c;
	}

	/** Validate a ramp meter */
	public void validate(RampMeterImpl meter) {
		MeterState state = getMeterState(meter);
		if(state != null)
			state.validate();
	}

	/** Check for the existence of a queue */
	public RampMeterQueue getQueueState(RampMeterImpl meter) {
		MeterState state = getMeterState(meter);
		if(state != null)
			return state.getQueueState();
		else
			return RampMeterQueue.UNKNOWN;
	}

	/** Get the meter state for a given meter */
	protected MeterState getMeterState(RampMeterImpl meter) {
		if(meter.getCorridor() != corridor) {
			// Meter must have been changed to a different
			// corridor; throw away old meter state
			states.remove(meter.getName());
			String cid = corridor.getID();
			SZM_LOG.log("getMeterState: " + meter.getName() +
				", not on corridor " + cid);
			return null;
		}
		MeterState state = lookupMeterState(meter);
		if(state != null)
			return state;
		state = new MeterState(meter);
		if(state.valid) {
			if(zones.isEmpty())
				createAllLayers();
			for(Zone zone: zones)
				zone.addMeter(state);
		}
		states.put(meter.getName(), state);
		return state;
	}

	/** Create all the layers */
	protected void createAllLayers() {
		states.clear();
		ZoneBuilder zone_builder = new ZoneBuilder();
		corridor.findActiveNode(zone_builder);
		zones.addAll(zone_builder.getList());
	}

	/** Lookup meter state for a specified meter */
	protected MeterState lookupMeterState(RampMeter meter) {
		return states.get(meter.getName());
	}

	/** Process the stratified plan for the next interval */
	protected void processInterval() {
		if(zone_change) {
			printSetup();
			zone_change = false;
		}
		calculateRates();
		if(!isDone())
			cleanupExpiredPlans();
		sendMeteringRates();
	}

	/** Is this stratified zone done? */
	protected boolean isDone() {
		return states.isEmpty() && zones.isEmpty();
	}

	/** Calculate all the metering rates */
	protected void calculateRates() {
		testGood();
		for(MeterState state: states.values())
			state.reset();
		for(Zone zone: zones) {
			if(zone.isGood()) {
				zone.calculateRate();
				zone.process();
			}
		}
		ListIterator<Zone> li = getZoneIterator();
		while(li.hasPrevious()) {
			Zone z = (Zone)li.previous();
			if(z.isBroken()) {
				z.resetControlled();
				for(Zone zone: zones) {
					if(zone.isGood())
						zone.process();
				}
			}
		}
		printStates();
	}

	/** Test the zones for goodness */
	protected void testGood() {
		for(MeterState state: states.values())
			state.good = false;
		for(Zone zone: zones)
			zone.testGood();
	}

	/** Get a list iterator of all zones starting with last zone */
	protected ListIterator<Zone> getZoneIterator() {
		int last = Math.max(zones.size() - 1, 0);
		ListIterator<Zone> li = zones.listIterator(last);
		// Make sure the iterator is past the end of the list
		while(li.hasNext())
			li.next();
		return li;
	}

	/** Cleanup the meter states for expired plans */
	protected void cleanupExpiredPlans() {
		Iterator<MeterState> it = states.values().iterator();
		while(it.hasNext()) {
			MeterState state = it.next();
			if(!state.meter.isOperating())
				it.remove();
		}
		if(states.isEmpty()) {
			zones.clear();
			printEnd();
		}
	}

	/** Send new metering rates to ramp meters */
	protected void sendMeteringRates() {
		for(MeterState state: states.values())
			state.sendRate();
	}

	/** Print the setup information for all meters and zones */
	protected void printSetup() {
		String cid = corridor.getID();
		MeterState state = getOneMeterState();
		if(state == null)
			return;
		String name = cid + '.' + stamp_hhmm(state.meter.getStartMin());
		try {
			String date = TimeSteward.currentDateShortString();
			PrintStream stream = createLogFile(date, name);
			stream.println("<?xml version=\"1.0\"?>");
			stream.println("<stratified_plan_log corridor='" + cid +
				"' date='" + date + "'>");
			printMeterSetup(stream);
			printZoneSetup(stream);
			stream.close();
		}
		catch(IOException e) {
			SZM_LOG.log("printSetup: " + cid + ", " +
				e.getMessage());
		}
	}

	/** Create a 4 character time stamp.
	 * @param min Minute of the day (0-1440)
	 * @return 4 character time stamp (1330 for 1:30 PM) */
	static protected String stamp_hhmm(int min) {
		StringBuilder b = new StringBuilder();
		b.append(min / 60);
		while(b.length() < 2)
			b.insert(0, '0');
		b.append(min % 60);
		while(b.length() < 4)
			b.insert(2, '0');
		return b.toString();
	}

	/** Get one meter state which has been defined */
	protected MeterState getOneMeterState() {
		for(MeterState state: states.values())
			return state;
		return null;
	}

	/** Print all meter setup information to a stream */
	protected void printMeterSetup(PrintStream stream) {
		for(MeterState state: states.values()) {
			if(state.valid)
				state.print(stream);
		}
	}

	/** Print all zone setup information to a stream */
	protected void printZoneSetup(PrintStream stream) {
		for(Zone zone: zones)
			zone.print(stream);
	}

	/** Print the zone and meter state information */
	protected void printStates() {
		try {
			PrintStream stream = appendLogFile();
			stream.println("  <interval time='" + stamp_30() +"'>");
			printZoneState(stream);
			printMeterState(stream);
			stream.println("  </interval>");
			stream.close();
		}
		catch(IOException e) {
			SZM_LOG.log("printStates: " + corridor.getID() + ", " +
				e.getMessage());
		}
	}

	/** Print all zone state information to a stream */
	protected void printZoneState(PrintStream stream) {
		for(Zone zone: zones)
			zone.printState(stream);
	}

	/** Print all meter state information to a stream */
	protected void printMeterState(PrintStream stream) {
		for(MeterState state: states.values()) {
			if(state.valid)
				state.printState(stream);
		}
	}

	/** Print the end of the log file */
	protected void printEnd() {
		try {
			PrintStream stream = appendLogFile();
			stream.println("</stratified_plan_log>");
			stream.close();
		}
		catch(IOException e) {
			SZM_LOG.log("printEnd: " + corridor.getID() + ", " +
				e.getMessage());
		}
		log_name = null;
	}

	/** Create a new log file */
	protected PrintStream createLogFile(String date, String name)
		throws IOException
	{
		File dir = new File(DATA_PATH + File.separator + date);
		if(!dir.exists()) {
			if(!dir.mkdir())
				throw new IOException("mkdir failed: " + dir);
		}
		log_name = new File(dir.getCanonicalPath() + File.separator +
			name + ".xml");
		FileOutputStream fos = new FileOutputStream(
			log_name.getCanonicalPath());
		return new PrintStream(fos);
	}

	/** Open log file for appending */
	protected PrintStream appendLogFile() throws IOException {
		if(log_name == null)
			throw new FileNotFoundException("No log file");
		FileOutputStream fos = new FileOutputStream(
			log_name.getCanonicalPath(), true);
		return new PrintStream(fos);
	}
}
