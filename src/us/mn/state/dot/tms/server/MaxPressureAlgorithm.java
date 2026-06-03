/*
 * Max-pressure ramp metering implemented in IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025-2026 Michael Levin
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneCode;
import us.mn.state.dot.tms.MeterQueueState;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.R_NodeType;
import static us.mn.state.dot.tms.R_NodeType.ENTRANCE;
import static us.mn.state.dot.tms.R_NodeType.STATION;
import us.mn.state.dot.tms.RampMeterHelper;
import static us.mn.state.dot.tms.RampMeterHelper.filterRate;
import static us.mn.state.dot.tms.RampMeterHelper.getMaxRelease;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.server.Constants.FEET_PER_MILE;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.HOUR;
import us.mn.state.dot.tms.server.event.MeterEvent;
import us.mn.state.dot.tms.server.maxpressure.CTMLink;
import us.mn.state.dot.tms.server.maxpressure.CTMNetwork;
import us.mn.state.dot.tms.server.maxpressure.DivergeNode;
import us.mn.state.dot.tms.server.maxpressure.EntranceLink;
import us.mn.state.dot.tms.server.maxpressure.ExitLink;
import us.mn.state.dot.tms.server.maxpressure.SimLink;
import us.mn.state.dot.tms.server.maxpressure.MergeNode;
import us.mn.state.dot.tms.server.maxpressure.SeriesNode;
import us.mn.state.dot.tms.server.maxpressure.SimNode;
import us.mn.state.dot.tms.server.maxpressure.SimpleCCSamplerSet;
import static us.mn.state.dot.tms.units.Interval.HOUR;

/**
 * Density-based Adaptive Metering Algorithm.
 *
 * I'm going to copy KAdaptive because it has a lot of what I need.
 * There doesn't seem to be an easy way to inherit from KAdaptive
 * because of its use of child classes and private methods.
 * I would need to replace some of KAdaptive.MeterState methods.
 */
public class MaxPressureAlgorithm implements MeterAlgorithmState {

    /** Enum for minimum limit control */
    enum MinimumRateLimit {
        passage_fail,
        storage_limit,
        wait_limit,
        target_min,
        backup_limit,
    };

    // are we running in a SUMO test environment?
    static private boolean SUMO = false;

    /** Algorithm debug log */
    static public final DebugLog ALG_LOG = new DebugLog("max-pressure");

    /** Number of seconds for one time step */
    static public final int STEP_SECONDS = 30;

    /** used in my test version that connects to SUMO */
    static public final int SUMO_DELAY = 30;
    static private final int SIM_DELAY = SUMO? SUMO_DELAY : 0;

    /** Number of milliseconds for one time step */
    static private final int PERIOD_MS =
        (int) new Interval(STEP_SECONDS + SIM_DELAY).ms();

    /** time step for CTM in seconds */
    public static final double CTM_DT = STEP_SECONDS / 5.0;

    /** Calculate steps per hour */
    static private final double STEP_HOUR =
        new Interval(STEP_SECONDS).per(HOUR);

    /** Critical density (vehicles / mile) */
    static private final int K_CRIT = 37;

    /** Minutes to flush meter before stop metering */
    static private final int FLUSH_MINUTES = 2;

    /** Number of seconds to store demand accumulator history */
    static private final int DEMAND_ACCUM_SECS = 600;

    /** Queue occupancy override threshold */
    static private final int QUEUE_OCC_THRESHOLD = 25;

    /** Number of seconds queue must be empty before resetting green */
    static private final int QUEUE_EMPTY_RESET_SECS = 60;

    /** Threshold to determine when queue is empty */
    static private final int QUEUE_EMPTY_THRESHOLD = -5;

    static private final float MAX_DELAY = 30.0f;

    /** Minimum rate */
    static private final float MIN_RATE = (int) Math.round(3600.0 / (MAX_DELAY / 2.0));

    /** Base percentage for backup minimum limit */
    static private final float BACKUP_LIMIT_BASE = 0.5f;

    /** Ratio for target waiting time to max wait time */
    static private final float WAIT_TARGET_RATIO = 0.75f;

    /** Ratio for target storage to max storage */
    static private final float STORAGE_TARGET_RATIO = 0.75f;

    /* this needs to be updated */
    static private final double AVG_VEH_LEN = 27.6;

    static private final int NUM_PRESSURE_INTERVAL = 10;

    // jam density
    static private final double K = SUMO? 119.1 * 1.609 : AVG_VEH_LEN;

    /** Ramp queue jam density (vehicles per foot) */
    static private final float JAM_VPF = (float) K / FEET_PER_MILE;

    /** Calculate the number of steps for an interval */
    static private int steps(int seconds) {
        float secs = seconds;
        return Math.round(secs / STEP_SECONDS);
    }

    /** Convert single step vehicle count to flow rate.
    * @param v Vehicle count to convert.
    * @return Flow rate (vehicles / hour), or null for missing data. */
    static private Double flowRate(float v) {
        return (v >= 0) ? (v * STEP_HOUR) : null;
    }
        
    /** Convert step vehicle count to flow rate.
     * @param v Vehicle count to convert.
     * @param n_steps Number of time steps of vehicle count.
     * @return Flow rate (vehicles / hour) */
    static private int flowRate(float v, int n_steps) {
        if (v >= 0) {
            Interval period = new Interval(n_steps * STEP_SECONDS);
            float hour_frac = period.per(HOUR);
            return Math.round(v * hour_frac);
        } else
            return MISSING_DATA;
    }

    /** Convert flow rate to vehicle count for a given period.
     * @param flow Flow rate to convert (vehicles / hour).
     * @param per_sec Period for vehicle count (seconds).
     * @return Vehicle count over given period. */
    static private float vehCountPeriod(int flow, int per_sec) {
        if (flow >= 0 && per_sec > 0) {
            float hour_frac = HOUR.per(new Interval(per_sec));
            return flow * hour_frac;
        } else
            return MISSING_DATA;
    }

    /** States for all K adaptive algorithms */
    static private HashMap<String, MaxPressureAlgorithm> ALL_ALGS =
        new HashMap<String, MaxPressureAlgorithm>();

    /** Create algorithm state for a meter */
    static public MaxPressureAlgorithm createState(RampMeterImpl meter) {
        Corridor c = meter.getCorridor();

        if (c != null) {
            MaxPressureAlgorithm alg = lookupAlgorithm(c);
            if (alg.createMeterState(meter))
                return alg;
        }
        return null;
    }

    /** Lookup an algorithm for a corridor */
    static private MaxPressureAlgorithm lookupAlgorithm(Corridor c) {
        MaxPressureAlgorithm alg = ALL_ALGS.get(c.getName());
        if (null == alg) {
            alg = new MaxPressureAlgorithm(c);
            alg.log("adding");
            ALL_ALGS.put(c.getName(), alg);
        }
        return alg;
    }

    /** Metering corridor */
    private final Corridor corridor;

    /** Hash map of ramp meter states */
    private final HashMap<String, MeterState> meter_states =
        new HashMap<String, MeterState>();

    /** All entrance / station nodes on corridor */
    private final ArrayList<Node> nodes;

    /** Create a new MaxPressureAlgorithm */
    private MaxPressureAlgorithm(Corridor c) {
        corridor = c;
        nodes = createNodes();
        debug();
    }

    /** Create nodes from corridor structure */
    private ArrayList<Node> createNodes() {
        NFinder finder = new NFinder();
        corridor.findActiveNode(finder);
        return finder.nodes;
    }

    /** Node finder */
    private class NFinder implements Corridor.NodeFinder {
        private ArrayList<Node> nodes = new ArrayList<Node>();
        public boolean check(float m, R_NodeImpl rnode) {
            Node n = createNode(rnode, m);
            if (n != null)
                nodes.add(n);
            return false;
        }
    }

    /** Create one node */
    private Node createNode(R_NodeImpl rnode, float mile) {
        switch (R_NodeType.fromOrdinal(rnode.getNodeType())) {
        case ENTRANCE:
            // I was getting duplicates because there's the start of the on-ramp and the merge point that could both be labeled as 'entrance'
            if (rnode.getSamplerSet().filter(LaneCode.MERGE).size() > 0) {
                return new EntranceNode(rnode, mile);
            } else {
                break;
            }
        case STATION:
            StationImpl stat = rnode.getStation();
            if (stat != null && stat.getActive())
                return new StationNode(rnode, mile, stat);
        }
        return null;
    }

    /** Debug corridor structure */
    private void debug() {
        log("-------- Corridor Structure --------");
        for (Node n : nodes)
            log(n.toString());
    }

    /** Log one message */
    public void log(String msg) {
        if (ALG_LOG.isOpen())
            ALG_LOG.log(corridor.getName() + ": " + msg);
    }

    /** Validate algorithm state for a meter */
    @Override
    public void validate(RampMeterImpl meter) {
        MeterState ms = getMeterState(meter);
        if (ms != null) {
            ms.validate();

            log(ms.toString());
            ms.logMeterEvent();
        } else {
            log("No state for " + meter.getName());
        }
    }

    /** Get meter queue state enum value */
    @Override
    public MeterQueueState getQueueState(RampMeterImpl meter) {
        MeterState ms = getMeterState(meter);
        return (ms != null)
            ? ms.getQueueState()
            : MeterQueueState.UNKNOWN;
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
        R_NodeImpl rnode = findRNode(meter);

        if (null == rnode)
            return null;
        for (Node n : nodes) {
            if (n instanceof EntranceNode) {
                EntranceNode en = (EntranceNode) n;
                if (en.rnode.equals(rnode))
                    return en;
            }
        }
        if (ALG_LOG.isOpen()) {
            log("Entrance " + rnode.getName() + " for " +
                meter.getName() + " not found");
        }
        return null;
    }

    /** Find node from meter onto this corridor */
    private R_NodeImpl findRNode(RampMeterImpl meter) {
        R_NodeImpl rnode = meter.getEntranceNode();
        if (null == rnode)
            return null;
        String cid = R_NodeHelper.getCorridorName(rnode);
        if (corridor.getName().equals(cid))
            return rnode;
        Corridor cor = BaseObjectImpl.corridors.getCorridor(cid);
        if (null == cor)
            return null;
        R_NodeImpl n = cor.findActiveNode(new ForkFinder(rnode));
        if (n != null)
            return n.getFork();
        else {
            log("Fork not found " + rnode.getName() +
                " for " + meter.getName());
            return null;
        }
    }

    /** Fork finder for CD roads */
    private class ForkFinder implements Corridor.NodeFinder {
        /** Entrance node of meter on CD road */
        private final R_NodeImpl rnode;
        /** Have we found the entrance node yet? */
        private boolean found;
        /** Create a fork finder */
        private ForkFinder(R_NodeImpl n) {
            rnode = n;
        }
        public boolean check(float m, R_NodeImpl n) {
            found |= (n == rnode);
            if (!found)
                return false;
            R_NodeImpl f = n.getFork();
            return (f != null) && corridor.getName().equals(
                R_NodeHelper.getCorridorName(f)
            );
        }
    }

    /** Node to manage station or entrance */
    abstract protected class Node {

        protected LaneCode lanecode;

        /** R_Node reference */
        protected final R_NodeImpl rnode;

        /** Mile point of the node */
        protected final float mile;

        /** Create a new node */
        protected Node(R_NodeImpl n, float m) {
            rnode = n;
            mile = m;
        }

        /** Get the distance to another node (in miles) */
        protected float distanceMiles(Node other) {
            return Math.abs(mile - other.mile);
        }

        /** Get the distancee to another node (in feet) */
        protected int distanceFeet(Node other) {
            return Math.round(distanceMiles(other) * FEET_PER_MILE);
        }
    }

    /** Node to manage station on corridor */
    protected class StationNode extends Node {

        /** StationImpl mapping this state */
        private final StationImpl station;

        /** Create a new station node. */
        public StationNode(R_NodeImpl rnode, float m, StationImpl st) {
            super(rnode, m);

            lanecode = LaneCode.MAINLINE;

            for (DetectorImpl det : rnode.getDetectors()){
                if (det.getLaneCode().equals(LaneCode.MERGE.lcode)
                    || det.getLaneCode().equals(LaneCode.PASSAGE.lcode)
                    || det.getLaneCode().equals(LaneCode.GREEN.lcode)){

                    lanecode = LaneCode.MERGE;
                }
                else if (det.getLaneCode().equals(LaneCode.EXIT.lcode)) {
                    lanecode = LaneCode.EXIT;
                }
            }

            station = st;
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
        public EntranceNode(R_NodeImpl rnode, float m) {
            super(rnode, m);
            lanecode = LaneCode.MERGE;
        }

        /** Get a string representation of an entrance node */
        @Override
        public String toString() {
            return "EN:" + rnode.getName();
        }
    }

    /** Enum for metering phase */
    private enum MeteringPhase {
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
        private final Node s_node;

        /** Queue sampler set */
        private final SimpleCCSamplerSet queue;

        /** Passage sampler set */
        private final SimpleCCSamplerSet passage;

        /** Merge sampler set */
        private final SimpleCCSamplerSet merge;

        /** Bypass sampler set */
        private final SimpleCCSamplerSet bypass;

        /** Green count sampler set */
        private final SimpleCCSamplerSet green;

        /** Metering phase */
        private MeteringPhase phase = MeteringPhase.stopped;

        /** Is the meter currently metering? */
        private boolean isMetering() {
            return phase != MeteringPhase.stopped;
        }

        /** Minimum metering rate (vehicles / hour) */
        private int min_rate = 0;

        /** Current metering rate (vehicles / hour) */
        private int release_rate = 0;

        /** Maximum metering rate (vehicles / hour) */
        private int max_rate = 0;

        private boolean smoothing = true;
        private double smoothing_factor = 0.2;

        /** Cumulative demand count (vehicles) */
        private float demand_accum = 0;

        /** Cumulative demand history (vehicles) */
        private final BoundedSampleHistory demand_accum_hist =
                new BoundedSampleHistory(steps(DEMAND_ACCUM_SECS));

        /** Demand adjustment (vehicles) */
        private float demand_adj = 0;

        /** Tracking queue demand rate (vehicles / hour) */
        private int tracking_demand = 0;

        /** Duration spent flushing, used to ensure sufficient flushing time
         * before turning off */
        private int flushing_time;

        /** Passage sampling good (latches until queue empty) */
        private boolean passage_good = true;

        /** Cumulative passage count (vehicles) */
        private int passage_accum = 0;
        
        /** Queue demand history (vehicles / hour) */
        private final BoundedSampleHistory demand_hist =
            new BoundedSampleHistory(steps(300));

        /** Cumulative green count (vehicles) */
        private int green_accum = 0;

        /** Time queue has been empty (seconds) */
        private int queue_empty_secs = 0;

        /** Time queue has been backed-up (seconds) */
        private int queue_backup_secs = 0;

        /** Total occupancy for duration of a queue backup */
        private int backup_occ = 0;
        
        /** Ratio for min rate to target rate */
	static private final float TARGET_MIN_RATIO = 0.75f;

        /** Controlling minimum rate limit */
        private MinimumRateLimit limit_control = MinimumRateLimit.target_min;

        /** End time stamp */
        private long stamp;

        // upstream node, node at merge point, downstream, node for mainline
        private StationNode upstream;
        private StationNode downstream;

        private double ramp_length = 0.25; // this is a filler value

        private double Q_r; // ramp capacity
        private double K_r; // ramp jam density
        private double Q_u, Q_d;

        private double L_u, L_d;

        // construct CTM model of the region around this ramp meter
        // this may require multiple merge/diverge nodes
        // depending on where my upstream and downstream detectors are located
        private CTMNetwork network;

        // point queue model for ramp, this is number of vehicles in queue
        private double ramp_queue;

        /** Create a new meter state */
        public MeterState(RampMeterImpl mtr, EntranceNode en) {
            meter = mtr;
            node = en;

            s_node = en;
            // I need the free flow speed
            double v_r = 45; // ramp free flow speed

            double min_link_len = 60.0 * STEP_SECONDS / 3600;

            log(mtr.getGeoLoc().getName() + " " + en.rnode.getName());
            upstream = findUpstreamStation(en, min_link_len);
            downstream = findDownstreamStation(en, min_link_len);

            log("\tupstream  " + upstream +
                " (" + upstream.mile + ") merge " + en +
                " (" + en.mile + ") downstream " + downstream +
                " (" + downstream.mile + ") min is " + min_link_len
            );

            if (downstream == null) {
                log(mtr.getEntranceNode().getName() + " " + min_link_len);
            }

            int ramp_lanes = 2; // assume 2 lanes form on ramp
            int upstream_lanes = upstream.rnode.getLanes(); // number of lanes on upstream. Used for capacity, etc.
            int downstream_lanes = downstream.rnode.getLanes();

            double K_u = upstream_lanes * K;
            K_r = ramp_lanes * K;
            double K_d = downstream_lanes * K;

            double v_u = getFFSpeed(upstream.rnode.getSpeedLimit());
            double v_d = getFFSpeed(downstream.rnode.getSpeedLimit());

            // capacity
            // can adjust these for ACC if desired
            Q_r = SUMO? 2107 : getCapacity(45);
            release_rate = (int) Q_r; // meter starts as off
            Q_u = getCapacity(v_u);
            Q_d = getCapacity(v_d);
            L_u = en.mile - upstream.mile;
            L_d = downstream.mile - en.mile;
            network = constructCTMNetwork();
            log(network.toString());
            ramp_queue = 0;
            stamp = DetectorImpl.calculateEndTime(PERIOD_MS);
            long max_lookback_r = STEP_SECONDS * 10 * 1000;

            // I want cumulative counts to obtain queue counts
            queue = new SimpleCCSamplerSet(meter.getSamplerSet(LaneCode.QUEUE), stamp);
            passage = new SimpleCCSamplerSet(meter.getSamplerSet(LaneCode.PASSAGE), stamp);
            merge = new SimpleCCSamplerSet(meter.getSamplerSet(LaneCode.MERGE), stamp);
            bypass = new SimpleCCSamplerSet(meter.getSamplerSet(LaneCode.BYPASS), stamp);
            green = new SimpleCCSamplerSet(meter.getSamplerSet(LaneCode.GREEN), stamp);
        }

        private double getCapacity(double ffspeed) {
            if (SUMO) {
                return 2150.3;
            } else {
                //HCM
                return Math.min(2400, 2200 + 10 * (ffspeed - 50));
            }
        }

        public double getW(double ffspeed) {
            if (SUMO) {
                return 22.586274214148 * 2.23694;
            } else {
                // guess for freeways
                return ffspeed / 2;
            }
        }

        private double getFFSpeed(double speed_limit) {
            if (SUMO) {
                return 26.82 * 2.23694; // sumo
            } else {
                // not enough information to use basic freeway segments from HCM. This is for multilane freeways.
                return speed_limit + 5;
            }
        }

        private CTMNetwork constructCTMNetwork() {
            // rely on upstream and downstream points to model everything in-between

            List<SimNode> simnodes = new ArrayList<>();
            List<SimLink> simlinks = new ArrayList<>();
            MergeNode center_merge = null;

            Node curr = upstream;
            Node next = curr;
            SimNode start_node = null;

            EntranceLink upstream_link = new EntranceLink(
                upstream.rnode, upstream.rnode.getSamplerSet().filter(LaneCode.MAINLINE)
            );
            SimNode curr_build = new SeriesNode(upstream.rnode, upstream_link);
            simlinks.add(upstream_link);
            simnodes.add(curr_build);

            double length = 0;

            Iterator<Node> iter = nodes.iterator();

            while (iter.next() != upstream);

            while (curr != downstream) {
                curr = next;
                next = iter.next();
                length += next.mile - curr.mile;
                int numLanes = 1;
                if (curr.lanecode == LaneCode.MAINLINE) {
                    numLanes = curr.rnode.getLanes();
                } else {
                    numLanes = next.rnode.getLanes();
                }
                double v = getFFSpeed(curr.rnode.getSpeedLimit());
                double Q = getCapacity(v) * numLanes;
                double w = getW(v);
                double K_link = K * numLanes;

                // I need to use the detector lane code of the detector at curr to find the type
                if (next.lanecode == LaneCode.MAINLINE) {
                    if (next == downstream) {
                        CTMLink mainline = new CTMLink(length, numLanes, v, Q, w, K_link);
                        curr_build.setMainlineOut(mainline);
                        SamplerSet sampler = next.rnode.getSamplerSet().filter(LaneCode.MAINLINE);
                        ExitLink exit = new ExitLink(next.rnode, sampler);

                        curr_build = new SeriesNode(next.rnode, mainline, exit);
                        simnodes.add(curr_build);
                        simlinks.add(mainline);
                        simlinks.add(exit);
                        // if curr = downstream, then we are done with the entire network
                        break;
                    }
                    // otherwise, this is a detector loop that I'm not using (because it's too close to the meter)
                    // ignore it
                }
                else if(next.lanecode == LaneCode.MERGE) {
                    CTMLink mainline = new CTMLink(length, numLanes, v, Q, w, K_link);
                    curr_build.setMainlineOut(mainline);

                    EntranceLink ent = new EntranceLink(next.rnode,
                        next.rnode.getSamplerSet().filter(LaneCode.GREEN));

                    curr_build = new MergeNode(next.rnode, mainline, ent);
                    simnodes.add(curr_build);
                    simlinks.add(mainline);
                    simlinks.add(ent);
                    if (next == s_node) {
                        center_merge = (MergeNode) curr_build;
                    }
                    length = 0; // reset length
                }
                else if(next.lanecode == LaneCode.EXIT) {
                    CTMLink mainline = new CTMLink(length, numLanes, v, Q, w, K_link);
                    curr_build.setMainlineOut(mainline);
                    ExitLink exit = new ExitLink(next.rnode,
                        next.rnode.getSamplerSet().filter(LaneCode.EXIT));
                    curr_build = new DivergeNode(next.rnode, mainline, exit);
                    simnodes.add(curr_build);
                    simlinks.add(mainline);
                    simlinks.add(exit);
                    length = 0; // reset length
                }

                // if type = exit, create divergenode
                // if type = merge, create mergenode
                // if type = detector, add to length. Ignore unless it is equal to downstream, then create exitnode
            }

            Collections.reverse(simlinks); // I want downstream links handled first so that excess exiting flow gets propagated backwards
            Collections.reverse(simnodes);

            // I need to find all nodes in between upstream and downstream
            // such nodes could be detectors (ignore), on-ramps (merge), or off-ramps (diverge)
            // the start of the segment is a SeriesNode and EntranceLink
            // the end of the segment is a SeriesNode and ExitLink
            CTMNetwork output = new CTMNetwork(center_merge, simnodes, simlinks);
            return output;
        }

        /** Check if ramp meter should continue flushing.
         * @return New metering phase. */
        private boolean checkContinueFlushing() {
            return !isQueueEmpty();
        }

        private StationNode findUpstreamStation(Node start, double min_link_len) {
            // assume that nodes are in sorted order
            // s_node is the base point. I want the closest node with distance > min_link_len
            StationNode closest = null;
            short road_dir = start.rnode.getGeoLoc().getRoadDir();

            for (Node n : nodes) {
                if (start.mile - n.mile > min_link_len) {
                    if ((n instanceof StationNode) && n.rnode.getSamplerSet().filter(LaneCode.MAINLINE).size() > 0
                             && n.rnode.getGeoLoc().getRoadDir() == road_dir)
                    {
                        closest = (StationNode) n;
                    }
                } else {
                    // we've gone too far
                    break;
                }
            }

            return closest;
        }

        private StationNode findDownstreamStation(Node start, double min_link_len) {
            // assume nodes in sorted order
            // s_node is the base point. I want the closest node with distance > min_link_len
            short road_dir = start.rnode.getGeoLoc().getRoadDir();

            for (Node n : nodes) {
                // first matching node is the closest
                if ((n instanceof StationNode) && (n.mile - start.mile  > min_link_len) && n.rnode.getSamplerSet().filter(LaneCode.MAINLINE).size() > 0 && n.rnode.getGeoLoc().getRoadDir() == road_dir) {
                    return (StationNode) n;
                }
            }
            return null;
        }

        /** Validate meter state.
         *   - Update state timers.
         *   - Update passage flow and accumulator.
         *   - Update demand flow and accumulator.
         *   - Calculate metering rate. */
        private void validate() {
            stamp = DetectorImpl.calculateEndTime(PERIOD_MS);
            try {
                // these are copied by k-adaptive and used to determine the minimum metering rate
                checkQueueBackedUp();
                checkQueueEmpty();
                updateDemandState();

                min_rate = filterRate((int)MIN_RATE);
                max_rate = filterRate(calculateMaximumRate());

                if (s_node != null)
                    calculateMeteringRate();
            }
            catch (Exception ex) {
                log(ex.toString());
            }
        }

        /** Check the queue backed-up state */
        private void checkQueueBackedUp() {
            if (isQueueOccupancyHigh()) {
                queue_backup_secs += STEP_SECONDS;
                backup_occ += queue.getMaxOccupancy(stamp, PERIOD_MS);
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
            {
                green_accum = passage_accum;
            }
        }

        /** Get queue demand count for the current period */
        private float queueDemandCount() {
            float vol = queue.getVehCount(stamp, PERIOD_MS);
            if (vol >= 0)
                return vol;
            else {
                int target = getDefaultTarget();
                return vehCountPeriod(target, STEP_SECONDS);
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
            float o = queue.getMaxOccupancy(stamp, PERIOD_MS)
                    - QUEUE_OCC_THRESHOLD;
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
         * @return Vehicle overcount at queue detector (may be negative). */
        private float estimateDemandOvercount() {
            return queueRatio(queue_empty_secs) * queueLength();
        }

        /** Estimate the length of queue (vehicles).
         * @return Queue length (may be negative). */
        private float queueLength() {
            long stamp = DetectorImpl.calculateEndTime(PERIOD_MS);
            return (float) getRampQueueLength(stamp);
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
            float occ = queue.getMaxOccupancy(stamp, PERIOD_MS);
            return occ > QUEUE_OCC_THRESHOLD;
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

        /** Get meter queue state enum value */
        private MeterQueueState getQueueState() {
            if (isMetering()) {
                if (isQueueFull())
                    return MeterQueueState.FULL;
                else if (!passage_good)
                    return MeterQueueState.UNKNOWN;
                else if (isQueueEmpty())
                    return MeterQueueState.EMPTY;
                else
                    return MeterQueueState.EXISTS;
            }
            return MeterQueueState.UNKNOWN;
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
                  (isQueueStorageFull());
        }

        /** Check if the ramp queue storage is full */
        private boolean isQueueStorageFull() {
            return queueLength() >= targetStorage();
        }

        /** Check if the meter queue is empty */
        private boolean isQueueEmpty() {
            return queueLength() < 1;
        }

        private void updateDemandState() {
            float dem_veh = queueDemandCount();
            float da = demand_accum;
            // Calculate demand without adjustment
            demand_accum += dem_veh;
            demand_adj = calculateDemandAdjustment();
            float adjusted_dem = Math.max(dem_veh + demand_adj, 0);
            
            demand_hist.push(flowRate(adjusted_dem));

            // Recalculate demand with adjustment
            demand_accum = da + adjusted_dem;
            demand_accum_hist.push((double) demand_accum);
            tracking_demand = trackingDemand();
        }
        
        /** Calculate tracking demand rate at queue detector.
        * @return Tracking demand flow rate (vehicles / hour) */
        private int trackingDemand() {
            Double d = demand_hist.average();
            return (d != null)
                ?	(int) Math.round(d)
                : getDefaultTarget();
        }
        
        /** Calculate target minimum rate.
        * @return Target minimum rate (vehicles / hour). */
        private int targetMinRate() {
            return Math.round(tracking_demand * TARGET_MIN_RATIO);
        }
        
        private int calculateMinimumRate() {
            if (passage_good) {
                limit_control = MinimumRateLimit.target_min;
                return calculateMinimumRate(targetMinRate());
            } else {
                limit_control = MinimumRateLimit.passage_fail;
                return tracking_demand;
            }
        }

        /** Calculate minimum rate (vehicles / hour).
         * Copied from K-adaptive: I want to include K-adaptive queue
         * length protections */
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

        private int queueWaitLimit() {
            assert passage_good;
            int wait_limit = 0;
            int wait_target = targetWaitTime();
            int wait_steps = steps(wait_target);
            for (int i = 1; i <= wait_steps; i++) {
                int dem = Math.round(cumulativeDemand(wait_steps - i));
                int pass_min = dem - passage_accum;
                int limit = flowRate(pass_min, i);
                wait_limit = Math.max(limit, wait_limit);
            }
            return wait_limit;
        }

        /** Get the total cumulative demand (vehicles).
         * @param step Time step in past (0 for current).
         * @return Cumulative demand at specified time. */
        private float cumulativeDemand(int step) {
            Double d = demand_accum_hist.get(step);
            return (d != null) ? d.floatValue() : 0;
        }

        /** Calculate queue storage limit.  Project into the future the
         * duration of the target wait time.  Using the target demand,
         * estimate the cumulative demand at that point in time.  From
         * there, subtract the target ramp storage count to find the
         * required cumulative passage vehicle count at that time.
         * @return Queue storage limit (vehicles / hour). */
        private int queueStorageLimit() {
            assert passage_good;
            float proj_arrive = vehCountPeriod(tracking_demand,
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

        /** Get the target wait time (seconds) */
        private int targetWaitTime() {
            return Math.round(maxWaitTime() * WAIT_TARGET_RATIO);
        }

        /** Get the max wait time (seconds) */
        private float maxWaitTime() {
            return Math.max(meter.getMaxWait(), 1);
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

        /** Calculate target maximum rate.
         * @return Target maxumum rate (vehicles / hour). */
        private int calculateMaximumRate() {
            return RampMeterHelper.getMaxRelease();
        }

        /** Calculate the metering rate */
        private void calculateMeteringRate() {
            network.simulateLastTimestep(stamp, PERIOD_MS);

            long stamp = DetectorImpl.calculateEndTime(PERIOD_MS);

            // need num lanes to help calculate capacities
            int numlanes = upstream.rnode.getLanes();

            // I need sending flow, receiving flow for mainline and ramp
            double S_rd = getRampSendingFlow(stamp); // units of veh
            double S_ud = network.getUpstreamSendingFlow(); // units of veh
            double R_d = network.getDownstreamReceivingFlow(); // units of veh

            // these are weighting factors
            double c_u = 1.0 / network.getUpstreamLanes();
            // weight by number of upstream lanes so that the weights are more comparable
            double c_r = 1 / 2.0; // divide by 2 because 2 ramp lanes
            double c_d = 1.0 / network.getDownstreamLanes();

            // these are the position weights
            double downstream_weight = c_d * network.getDownstreamWeight(false);
            double ramp_weight = c_r * getRampWeight(stamp);
            double upstream_weight = c_u * network.getUpstreamWeight(false);

            double weight_ud = upstream_weight - downstream_weight;
            double weight_rd = ramp_weight - downstream_weight;

            int max_rate = getMaximumRate();
            int min_rate = Math.min(max_rate, Math.max(calculateMinimumRate(), getMinimumRate())); // if min rate is less than max rate for some reason, use max rate

            int new_rate = calcBestRate(S_ud, S_rd, R_d, weight_ud, weight_rd, min_rate, max_rate);

            if (smoothing) {
                new_rate = smoothRate(new_rate, release_rate, min_rate, max_rate);
            }

            // if equal to capacity, that is effectively no metering
            // null means meter is off
            if (new_rate > max_rate) {
                if (phase == MeteringPhase.stopped) {
                    meter.setRatePlanned(null);
                } else if (phase == MeteringPhase.flushing && flushing_time > FLUSH_MINUTES * 60) {
                    flushing_time = 0;
                    phase = MeteringPhase.stopped;
                    meter.setRatePlanned(null);
                } else { // phase == MeteringPhase.metering or phase == MeteringPhase.flushing
                    flushing_time += STEP_SECONDS;
                    phase = MeteringPhase.flushing;
                    meter.setRatePlanned(max_rate);
                }
                release_rate = (int) Q_r;
            }
            else {
                // it is possible that the new metering rate is the maximum rate.
                // If so, then add to flushing time
                // if not, then reset flushing time
                if (new_rate < max_rate) {
                    flushing_time = 0;
                    phase = MeteringPhase.metering;
                } else {
                    flushing_time += STEP_SECONDS;
                    phase = MeteringPhase.flushing;
                }
                meter.setRatePlanned(new_rate);
                release_rate = new_rate;
            }
        }

        /**
         * This prevents large changes in the rate by limiting the rate to x% changes of the previous rate
         */
        private int smoothRate(int rate, int release_rate, int min_rate, int max_rate){
            int new_min_rate = 0;
            int new_max_rate = 0;

            if (phase == MeteringPhase.metering || phase == MeteringPhase.flushing) {
                // it's ok for rate to exceed max_rate
                // it is possible that best_rate = Q_r indicating meter off
                // this will cause the meter to start flushing
                new_max_rate = Math.min((int) Q_r, (int) (release_rate * (1 + smoothing_factor)));
                new_min_rate = Math.max(min_rate, (int) (release_rate * (1 - smoothing_factor)));
            }
            // if the meter is off, then turn it on slowly
            else {
                new_min_rate = max_rate;
                new_max_rate = (int) Q_r;
            }

            // min rate will always dominate if needed
            return Math.max(new_min_rate, Math.min(rate, new_max_rate));
        }

        private int calcBestRate(double S_ud, double S_rd, double R_d, double weight_ud, double weight_rd, int min_rate, int max_rate) {

            // ignore negative values from detector errors
            S_ud = Math.max(0, S_ud);
            S_rd = Math.max(0, S_rd);

            R_d = Math.max(0, R_d);

            // base case: no metering
            // Q_r should be higher than max_rate
            int best_rate = (int) Q_r;

            // S <= R: everyone can move. Should everyone move?
            if (S_ud + S_rd <= R_d) {
                if (weight_rd < 0 && network.isDownstreamCongested()) {
                    best_rate = min_rate;
                }
                return best_rate;
            }

            // check obj value for no metering
            double best_obj = calcMPObj(weight_ud, weight_rd, S_ud, S_rd, R_d, Q_u, Q_r);

            // brute force line search. Faster than solving nonlinear optimization program.
            int interval_rate = 10;

            for (int rate = min_rate; rate <= max_rate; rate+= interval_rate) {

                double obj = calcMPObj(weight_ud, weight_rd, S_ud,
                    Math.min(S_rd, rate * STEP_SECONDS/3600.0), R_d, Q_u, rate);
                // this uses the rate as the capacity for the ramp
                if (obj >= best_obj) {
                    best_rate = rate;
                    best_obj = obj;
                }
            }
            return best_rate;
        }

        private double calcMPObj(double w_ud, double w_rd, double S_ud, double S_rd, double R_d, double Q_u, double Q_r) {
            double y_ud = 0;
            double y_rd = 0;

            // uncongested merge case
            if (S_ud + S_rd < R_d) {
                y_ud = S_ud;
                y_rd = S_rd;
            }
            // congested merge case, 2 upstream links
            else {
                double lambda_rd = R_d * Q_r / (Q_u + Q_r);
                y_rd = MergeNode.median(R_d - S_ud, S_rd, lambda_rd);
                y_ud = R_d - y_rd;
            }

            return w_ud * y_ud + w_rd * y_rd;
        }

        private double getRampWeight(long stamp) {
            double ramp_n = getRampQueueLength(stamp);

            // assume density is equal to K_r, so queue is at end of ramp
            // density behind queue is assumed to be 0
            // evaluate integral from 0 to L of x/L * k
            // integral is piecewise with k either K_r, 0
            double end = ramp_length;
            double start = ramp_length - ramp_n / K_r;

            double integral = (end*end / 2 - start * start / 2) / ramp_length * K_r;

            return integral;
        }
        private double getRampQueueLength(long stamp) {
            double queuein = queue.getCumulativeCount(stamp, PERIOD_MS);
            double queueout = queuein;

            if (green.isPerfect()) {
                queueout = green.getCumulativeCount(stamp, PERIOD_MS);

                if (bypass.isPerfect()) {
                    queueout += bypass.getCumulativeCount(stamp, PERIOD_MS);
                }
            }
            else if (passage.isPerfect()) {
                queueout = passage.getCumulativeCount(stamp, PERIOD_MS);
            }

            return queuein - queueout;
        }

        private double getRampSendingFlow(long stamp) {
            double S_rd = Math.min(Q_r * STEP_SECONDS / 3600.0, getRampQueueLength(stamp));

            if (S_rd < 0) {
                S_rd = 0;
            }

            return S_rd;
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

        /** Log a meter event */
        protected void logMeterEvent() {
            long stamp = DetectorImpl.calculateEndTime(PERIOD_MS);

            String dns = meter.getName();
            float seg_den = (float)network.getDownstreamAvgDensity();
            MeterEvent ev = new MeterEvent(EventType.METER_EVENT,
                meter.name, phase.ordinal(),
                getQueueState().ordinal(), queueLength(),
                demand_adj, estimateWaitSecs(),
                limit_control.ordinal(), min_rate, release_rate,
                max_rate, dns, seg_den
            );
            BaseObjectImpl.logEvent(ev);
        }

        private int estimateWaitSecs() {
            return (int) Math.round(queueLength() * 3600.0 / release_rate);
        }

        /** Get current metering rate.
         * @return metering rate */
        private double getRate() {
            double r = release_rate;
            return r;
        }

        /** Get a string representation of a meter state */
        @Override
        public String toString() {
            return "meter:" + meter.getName() + " rate: " + release_rate;
        }
    }
}
