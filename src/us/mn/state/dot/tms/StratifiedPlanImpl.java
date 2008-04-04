/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

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
import java.rmi.RemoteException;

/**
 * StratifiedPlanImpl
 *
 * @author Douglas Lau
 */
public class StratifiedPlanImpl extends MeterPlanImpl implements Constants {

	/** Zone debug log */
	static protected final DebugLog ZONE_LOG = new DebugLog("zone");

	/** ObjectVault table name */
	static public final String tableName = "stratified_plan";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Path where meter data files are stored */
	static protected final String DATA_PATH = "/data/meter";

	/** Constant for standard filter equation */
	static protected final float K = 0.15f;

	/** Constant for filtering accumulated release rate */
	static protected final float K_RATE_ACCUM = 0.2f;

	/** Total number of layers in stratified timing plan */
	static protected final int TOTAL_LAYERS = 6;

	/** Queue occupancy override threshold */
	static protected final int QUEUE_OCC_THRESHOLD = 25;

	/** Queue override demand increment */
	static protected final int QUEUE_OVERRIDE_INCREMENT = 150;

	/** Distance of queue from detector at queue occ threshold */
	static protected final int QUEUE_THRESHOLD_DISTANCE = 100;

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

	/** Create a new stratified timing plan */
	public StratifiedPlanImpl(int period) throws TMSException,
		RemoteException
	{
		super(period);
		segList = null;
	}

	/** Create a stratified timing plan */
	protected StratifiedPlanImpl() throws RemoteException {
		super();
		segList = null;
	}

	/** Get the plan type */
	public String getPlanType() { return "Stratified"; }

	/** Get the target release rate for the specified ramp meter */
	public int getTarget(RampMeter m) {
		return RampMeter.MAX_RELEASE_RATE;
	}

	/** Set the target release rate for the specified ramp meter */
	public void setTarget(RampMeter m, int t) throws TMSException {
		if(t != RampMeter.MAX_RELEASE_RATE)
			throw new ChangeVetoException("Invalid target rate");
	}

	/** Segment list for this timing plan */
	protected transient SegmentListImpl segList;

	/** Get the segment list for this timing plan */
	protected SegmentListImpl getSegmentList(RampMeterImpl meter) {
		if(segList == null)
			segList = meter.getSegmentList();
		else if(segList != meter.getSegmentList()) {
			System.err.println("ERROR: Segment List mismatch for " +
				meter.getId());
		}
		return segList;
	}

	/** Meter state holds stratified plan state for a meter. For each meter
	    in the stratified plan, there will be one MeterState object. */
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

		/** Minimum release rate assigned by stratified timing plan */
		protected int minimum;

		/** Release rate assigned by stratified timing plan */
		protected int release;

		/** Warning flag for finding plan errors */
		protected boolean warning;

		/** Congested mainline flag */
		protected boolean congested;

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
		protected MeterState(RampMeterImpl meter) {
			this.meter = meter;
			valid = findDetectors();
			rate_accum = RampMeter.MAX_RELEASE_RATE;
			p_flow = RampMeter.MAX_RELEASE_RATE;
		}

		/** Reset the meter's zone state */
		protected void reset(int minute) {
			if(congested)
				release = meter.getTarget(minute);
			else
				release = RampMeter.MAX_RELEASE_RATE;
			control = null;
		}

		/** Reset the zone rule state */
		protected void resetRule() {
			prop = 0;
			rate = RampMeter.MAX_RELEASE_RATE;
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
			queue = ds.getDetectorSet(Detector.QUEUE);
			passage = ds.getDetectorSet(Detector.PASSAGE);
			merge = ds.getDetectorSet(Detector.MERGE);
			bypass = ds.getDetectorSet(Detector.BYPASS);
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

		/** Compute the demand for the ramp meter */
		protected int computeDemand() {
			has_queue = false;
			queue_backup = false;
			warning = true;
			congested = true;
			if(!valid) {
				minimum = RampMeter.MIN_RELEASE_RATE;
				return minimum;
			}
			rate_accum += K_RATE_ACCUM *
				(meter.getReleaseRate() - rate_accum);
			density = DENSITY_SLOPE * rate_accum +
				DENSITY_Y_INTERCEPT;
			int storage = meter.getStorage();
			if(meter.isSingleRelease())
				storage -= QUEUE_THRESHOLD_DISTANCE;
			else
				storage -= QUEUE_THRESHOLD_DISTANCE * 2;
			storage = Math.max(storage, 1);
			max_stored = density * storage / FEET_PER_MILE;
			float max_cycle = meter.getMaxWait() / max_stored;
			minimum = (int)(SECONDS_PER_HOUR / max_cycle);
			int demand = meter.getDemand();
			int p_demand = calculatePassageDemand(demand);
			q_prob = Math.min(p_flow / rate_accum, 1.0f);
			if(queue.getMaxOccupancy() > QUEUE_OCC_THRESHOLD) {
				q_prob = 1;
				queue_backup = true;
				demand += QUEUE_OVERRIDE_INCREMENT;
				minimum = Math.max(minimum, demand);
			} else if(queue.isPerfect()) {
				demand += (int)(K * (queue.getFlow() - demand));
				minimum = Math.round(minimum * q_prob);
				minimum = Math.min(minimum, p_demand);
			} else {
				demand = p_demand;
				minimum = p_demand;
			}
			if(!meter.isMetering()) {
				q_prob = 0;
				minimum = RampMeter.MIN_RELEASE_RATE;
			} else if(flushing)
				minimum = RampMeter.MAX_RELEASE_RATE;
			has_queue = q_prob > QUEUE_EXISTS_FACTOR;
			return demand;
		}

		/** Calculate the passage demand (and smoothed flow) */
		protected int calculatePassageDemand(int demand) {
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
				p_flow = RampMeter.MAX_RELEASE_RATE;
				return RampMeter.MAX_RELEASE_RATE;
			}
			p_flow += K_RATE_ACCUM * (p - p_flow);
//			if(testing)
//				p += PASSAGE_DEMAND_ADJUSTMENT;
//			else
				p *= PASSAGE_DEMAND_FACTOR;
			return demand + (int)(K * (p - demand));
		}

		/** Print the meter setup */
		protected void print(PrintStream stream) {
			StringBuffer buf = new StringBuffer();
			buf.append("  <meter id='");
			buf.append(meter.getId());
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
			buf.append(meter.getId());
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
			buf.append(meter.getMinimum());
			buf.append("' D='");
			buf.append(meter.getDemand());
			buf.append("' R='");
			buf.append(release);
			if(control != null) {
				buf.append("' Z='");
				buf.append(control.getId());
			}
			if(warning && (!congested) &&
				meter.isActive() && !meter.isFailed())
			{
				buf.append("' warning='1");
			}
			buf.append("' />");
			stream.println(buf);
		}
	}

	/** Compute the demand (and the minimum release rate) for the
	    specified ramp meter */
	public synchronized int computeDemand(RampMeterImpl meter,
		int interval)
	{
		if(!active)
			return RampMeter.MIN_RELEASE_RATE;
		MeterState state = getMeterState(meter);
		if(state == null)
			return RampMeter.MIN_RELEASE_RATE;
		next_interval = true;
		if(interval >= 2 * stopTime - 4 && interval <= 2 * stopTime)
			flushing = true;
		else
			flushing = false;
		return state.computeDemand();
	}

	/** Get the minimum release rate for the specified ramp meter */
	public synchronized int getMinimum(RampMeterImpl meter) {
		if(!active)
			return RampMeter.MIN_RELEASE_RATE;
		MeterState state = getMeterState(meter);
		if(state == null)
			return RampMeter.MIN_RELEASE_RATE;
		return state.minimum;
	}

	/** Flag to indicate ramp meters should be flushing */
	protected transient boolean flushing;

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

		/** Flag to determine whether the zone is currently valid */
		protected boolean valid;

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

		/** Create a new zone */
		protected Zone(int l, int n, SegmentImpl[] segs, int start,
			int stop)
		{
			layer = l;
			znum = n;
			addUpstream(segs[start]);
			for(int i = start; i <= stop; i++) {
				SegmentImpl seg = segs[i];
				DetectorSet ds = seg.getDetectorSet();
				if(seg instanceof StationSegmentImpl)
					addMainline(seg);
				else if(seg instanceof OnRampImpl) {
					OnRampImpl ramp = (OnRampImpl)seg;
					if(ramp.isToCd())
						continue;
					if(ramp.isFromCd())
						scanUpstreamCD(segs, i, start);
					else
						addEntrance(ds, false);
				} else if(seg instanceof OffRampImpl) {
					OffRampImpl ramp = (OffRampImpl)seg;
					if(ramp.isFromCd())
						continue;
					addExit(ds);
				}
			}
			addDownstream(segs[stop]);
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

		/** Add an upstream station to the zone */
		protected void addUpstream(SegmentImpl segment) {
			addUpstream(segment.getDetectorSet());
		}

		/** Add a mainline station to the zone */
		protected void addMainline(DetectorSet ds) {
			for(DetectorImpl det: ds.toArray()) {
				if(det.isStation())
					mainline.addDetector(det);
			}
		}

		/** Add a mainline station to the zone */
		protected void addMainline(SegmentImpl segment) {
			addMainline(segment.getDetectorSet());
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

		/** Add a downstream station to the zone */
		protected void addDownstream(SegmentImpl segment) {
			addDownstream(segment.getDetectorSet());
		}

		/** Add an entrance to the zone */
		protected boolean addEntrance(DetectorSet ds, boolean cd_merge)
		{
			boolean q = (ds.getDetectorSet(
				Detector.QUEUE)).isDefined();
			entrance.addDetectors(ds.getDetectorSet(
				Detector.BYPASS));
			entrance.addDetectors(ds.getDetectorSet(
				Detector.MAINLINE));
			entrance.addDetectors(ds.getDetectorSet(
				Detector.OMNIBUS));
			DetectorSet passage = ds.getDetectorSet(
				Detector.PASSAGE);
			if(passage.isDefined())
				entrance.addDetectors(passage);
			else if(q || !cd_merge) {
				entrance.addDetectors(ds.getDetectorSet(
					Detector.MERGE));
				entrance.addDetectors(ds.getDetectorSet(
					Detector.EXIT));
			}
			return q;
		}

		/** Add an exit to the zone */
		protected void addExit(DetectorSet ds) {
			exit.addDetectors(ds);
		}

		/** Scan upstream for meterable segments on the CD road.
		 * @param segs Array of segments for this freeway
		 * @param end End segment of CD, scan upstream from here
		 * @param start Start segment of zone */
		protected void scanUpstreamCD(SegmentImpl[] segs, int end,
			int start)
		{
			for(int i = end; i > 0; i--) {
				if(checkSegment(segs[i], i < start))
					break;
			}
		}

		/** Check segment for upstream CD detectors.
		 * @param seg Segment to check
		 * @param before True if before the upstream zone segment
		 * @return True means stop scanning upstream */
		protected boolean checkSegment(SegmentImpl seg, boolean before)
		{
			DetectorSet ds = seg.getDetectorSet();
			if(seg instanceof OnRampImpl) {
				OnRampImpl ramp = (OnRampImpl)seg;
				if(ramp.isFromCd() && addEntrance(ds, true))
					return true;
				if(ramp.isToCd())
					addEntrance(ds, false);
			} else if(seg instanceof MeterableImpl) {
				if(addEntrance(ds, true))
					return true;
			} else if(seg instanceof OffRampImpl) {
				OffRampImpl ramp = (OffRampImpl)seg;
				if(ramp.isToCd()) {
					if(before)
						entrance.addDetectors(seg);
					return true;
				}
				if(ramp.isFromCd())
					addExit(ds);
			}
			return false;
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
			if(upstream.isGood() && entrance.isNotBad() &&
				exit.isNotBad() && mainline.isFlowing() &&
				meters.size() > 0)
			{
				A += K * (upstream.getFlow() - A);
				U += K * (entrance.getFlow() - U);
				X += K * (exit.getFlow() - X);
				S = mainline.getUpstreamCapacity() *
					upstream.size();
				B = downstream.getCapacity();
				M = Math.round(B + X + S - A - U);
				if(M < 1)
					M = 1;
				valid = true;
			} else
				valid = false;
		}

		/** Reset all the meters in the zone */
		protected void resetMeters(int minute) {
			for(MeterState state: meters) {
				if(valid && mainline.isFlowing())
					state.congested = false;
				state.reset(minute);
			}
		}

		/** Process the zone */
		protected void process() {
			if(!valid)
				return;
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
			for(MeterState state: meters) {
				if(state.done)
					rate -= state.release;
				else
					demand += state.meter.getDemand();
			}
			for(MeterState state: meters) {
				if(state.done)
					continue;
				RampMeterImpl meter = state.meter;
				int r = rate * meter.getDemand() / demand;
				state.prop = r;
				rate -= r;
				demand -= meter.getDemand();
				if(r > state.release) {
					delta += r - state.release;
					r = state.release;
				}
				if(r < meter.getMinimum()) {
					delta -= meter.getMinimum() - r;
					r = meter.getMinimum();
				}
				state.rate = r;
			}
			if(delta == 0)
				return 0;
			for(MeterState state: meters) {
				if((delta > 0) && (state.prop > state.release))
					state.done = true;
				if((delta < 0) && (state.prop <
					state.meter.getMinimum()))
				{
					state.done = true;
				}
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
		protected void resetControlled(int minute) {
			for(MeterState state: meters) {
				if(state.control == this)
					state.reset(minute);
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
				buf.append(state.meter.getId());
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
			if(valid) {
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
				if(mainline.isFlowing())
					buf.append("flowing");
				else
					buf.append("congested");
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
	}

	/** Linked list of zones in this timing plan */
	protected transient final LinkedList<Zone> zones =
		new LinkedList<Zone>();

	/** Create all the layers for this stratified timing plan */
	protected void createAllLayers(RampMeterImpl meter) {
		zones.clear();

if(testing) {
		ZoneBuilder zone_builder = new ZoneBuilder();
		Corridor c = meter.getCorridor();
		c.findNode(zone_builder);
		LinkedList<Zone> _zones = zone_builder.getList();
		zones.addAll(_zones);
} else {
		SegmentListImpl sList = getSegmentList(meter);
		if(sList == null)
			return;
		SegmentImpl[] segs = sList.toArray();
		for(int layer = 1; layer <= TOTAL_LAYERS; layer++)
			createLayer(segs, layer);
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
		protected void addEntranse(DetectorSet ds) {
			for(Zone z: _zones) {
				if(!z.isComplete())
					z.addEntrance(ds, false);
			}
		}
		protected void addExit(DetectorSet ds) {
			for(Zone z: _zones) {
				if(!z.isComplete())
					z.addExit(ds);
			}
		}
		public boolean check(R_NodeImpl n) {
			int nt = n.getNodeType();
			if(nt == R_Node.TYPE_INTERSECTION) {
				removeInvalidZones();
				return false;
			}
			DetectorSet ds = n.getDetectorSet();
			if(ds.size() == 0) {
// FIXME: follow links for missing detection
LocationImpl loc = (LocationImpl)n.getLocation();
if(nt == R_Node.TYPE_ENTRANCE)
	ZONE_LOG.log("Missing entrance detection @ " + loc.getDescription());
if(nt == R_Node.TYPE_EXIT)
	ZONE_LOG.log("Missing exit detection @ " + loc.getDescription());
				return false;
			}
			if(nt == R_Node.TYPE_STATION)
				addStation(ds);
			else if(nt == R_Node.TYPE_ENTRANCE)
				addEntranse(ds);
			else if(nt == R_Node.TYPE_EXIT)
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

	/** Create a layer of zones and add them to the zone list */
	protected void createLayer(SegmentImpl[] segs, int layer) {
		int znum = 1;
		for(int start = 0; start < segs.length; start++) {
			if(!(segs[start] instanceof StationSegment))
				continue;
			int stations = 0;
			for(int stop = start + 1; stop < segs.length; stop++) {
				if(!(segs[stop] instanceof StationSegment))
					continue;
				stations++;
				if(stations == layer) {
					if(createZone(layer, znum, segs, start,
						stop))
					{
						znum++;
					}
					break;
				}
			}
		}
	}

	/** Create a zone and add it to the zone list */
	protected boolean createZone(int layer, int znum, SegmentImpl[] segs,
		int start, int stop)
	{
		Zone zone = new Zone(layer, znum, segs, start, stop);
		if(zone.isDefined()) {
			zones.add(zone);
			return true;
		}
		return false;
	}

	/** Validate the timing plan for the start time */
	protected int validateStart(RampMeterImpl meter) {
		states.clear();
		createAllLayers(meter);
		return RampMeter.MAX_RELEASE_RATE;
	}

	/** Validate the timing plan for the stop time */
	protected int validateStop(RampMeterImpl meter) {
		for(MeterState state: states.values())
			stopMetering(state.meter);
		states.clear();
		zones.clear();
		printEnd();
		return RampMeter.MAX_RELEASE_RATE;
	}

	/** Zone change debugging flag */
	protected transient boolean zone_change = false;

	/** Current log file name */
	protected transient File log_name;

	/** Create a 4-digit time string from the minute-of-day */
	static protected String createTime(int minute) {
		StringBuffer buf = new StringBuffer();
		buf.append(minute / 60);
		while(buf.length() < 2)
			buf.insert(0, '0');
		buf.append(minute % 60);
		while(buf.length() < 4)
			buf.insert(2, '0');
		return buf.toString();
	}

	/** Create a new log file */
	protected PrintStream createLogFile(RampMeterImpl meter, String date)
		throws IOException
	{
		File dir = new File(DATA_PATH + File.separator + date);
		if(!dir.exists()) {
			if(!dir.mkdir())
				throw new IOException("mkdir failed: " + dir);
		}
		log_name = new File(dir.getCanonicalPath() + File.separator +
			meter.getCorridorID() + '.' + createTime(startTime) +
			'-' + createTime(stopTime) + ".xml");
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

	/** Print the setup information for all meters and zones */
	protected void printSetup(RampMeterImpl meter) {
		try {
			String date = TrafficDataBuffer.date(
				System.currentTimeMillis());
			PrintStream stream = createLogFile(meter, date);
			stream.println("<?xml version=\"1.0\"?>");
			stream.println("<stratified_plan_log corridor='" +
				meter.getCorridorID() + "' date='" + date +
				"'>");
			printMeterSetup(stream);
			printZoneSetup(stream);
			stream.close();
		}
		catch(IOException e) {
			System.err.println("XML " + meter.getCorridorID() +
				": " + e.getMessage());
		}
		zone_change = false;
	}

	/** Get a string representation of an interval time */
	static protected String intervalString(int interval) {
		StringBuffer buf = new StringBuffer();
		interval++;
		buf.append(interval / 120);
		while(buf.length() < 2)
			buf.insert(0, '0');
		buf.append(':');
		buf.append((interval % 120) / 2);
		while(buf.length() < 5)
			buf.insert(3, '0');
		buf.append(':');
		buf.append((interval % 2) * 30);
		while(buf.length() < 8)
			buf.insert(6, '0');
		return buf.toString();
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

	/** Print the zone and meter state information */
	protected void printStates(int interval) {
		try {
			PrintStream stream = appendLogFile();
			stream.println("  <interval time='" +
				intervalString(interval) + "'>");
			printZoneState(stream);
			printMeterState(stream);
			stream.println("  </interval>");
			stream.close();
		}
		catch(IOException e) {
			System.err.println("XML: " + e.getMessage());
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
			System.err.println("XML: " + e.getMessage());
		}
		log_name = null;
	}

	/** Flag to indicate the start of the next interval */
	protected transient boolean next_interval = false;

	/** Get a list iterator of all zones starting with last zone */
	protected ListIterator<Zone> getZoneIterator() {
		int last = Math.max(zones.size() - 1, 0);
		ListIterator<Zone> li = zones.listIterator(last);
		// Make sure the iterator is past the end of the list
		while(li.hasNext())
			li.next();
		return li;
	}

	/** Calculate all the metering rates */
	protected void calculateRates(int interval) {
		for(Zone zone: zones)
			zone.resetMeters(interval / 2);
		for(Zone zone: zones) {
			zone.calculateRate();
			zone.process();
		}
		ListIterator<Zone> li = getZoneIterator();
		while(li.hasPrevious()) {
			Zone z = (Zone)li.previous();
			if(z.isBroken()) {
				z.resetControlled(interval / 2);
				for(Zone zone: zones)
					zone.process();
			}
		}
		printStates(interval);
		next_interval = false;
	}

	/** Hash map of ramp meter states */
	protected transient final HashMap<String, MeterState> states =
		new HashMap<String, MeterState>();

	/** Get the meter state for a specified meter */
	protected MeterState getMeterState(RampMeterImpl meter) {
		return states.get(meter.getId());
	}

	/** Get the meter state for a given ramp meter */
	protected MeterState getOrCreateMeterState(
		RampMeterImpl meter)
	{
		MeterState state = getMeterState(meter);
		if(state != null)
			return state;
		state = new MeterState(meter);
		states.put(meter.getId(), state);
		if(state.valid) {
			for(Zone zone: zones)
				zone.addMeter(state);
		}
		return state;
	}

	/** Check if this timing plan knows of a queue backup */
	public synchronized boolean checkQueueBackup(RampMeterImpl meter) {
		MeterState state = getMeterState(meter);
		if(state == null)
			return false;
		return state.queue_backup;
	}

	/** Check if this timing plan is in warning mode */
	public synchronized boolean checkWarning(RampMeterImpl meter) {
		MeterState state = getMeterState(meter);
		if(state == null)
			return false;
		return state.warning;
	}

	/** Check if this timing plan knows of a congested mainline state */
	public synchronized boolean checkCongested(RampMeterImpl meter) {
		MeterState state = getMeterState(meter);
		if(state == null)
			return false;
		return state.congested;
	}

	/** Check for the existance of a queue */
	public synchronized boolean checkQueue(RampMeterImpl meter) {
		MeterState state = getMeterState(meter);
		if(state == null)
			return false;
		return state.has_queue;
	}

	/** Validate the timing plan within the time frame */
	protected int validateWithin(RampMeterImpl meter, int interval) {
		if(zone_change)
			printSetup(meter);
		if(next_interval)
			calculateRates(interval);
		MeterState state = getOrCreateMeterState(meter);
		boolean first = (interval < startTime + stopTime);
		int demand = meter.getDemand();
		if(meter.isMetering()) {
//			float thresh = state.rate_accum * TURN_OFF_THRESHOLD;
//			if(!first)
//				if(demand < thresh)
//					stopMetering(meter);
			if(flushing && !state.has_queue)
				stopMetering(meter);
		} else {
			float thresh = state.rate_accum;
			if(first)
				thresh *= TURN_ON_THRESHOLD_1;
			else
				thresh *= TURN_ON_THRESHOLD_2;
			if(demand > thresh)
				startMetering(meter);
		}
		return state.release;
	}
}
