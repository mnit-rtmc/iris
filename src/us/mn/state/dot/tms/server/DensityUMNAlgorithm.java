/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  University of Minnesota
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

import java.util.Date;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.SystemAttributeHelper;

/**
 * Density metering algorithm state
 *
 * @author Anupam
 */
public class DensityUMNAlgorithm implements MeterAlgorithmState {

	/** UNDEFINED used as default for all types : Congestion level, Bottleneck state, Metering Strategy
	 * Used to catch all cases where proper values are not assigned.
	 */
	static protected final int UNDEFINED = -1;

	/** Congestion Levels at meter locations */
	public enum CongestionLevel {
		UNDEFINED,
		UNCONGESTED,
		APPROACHING,
		CONGESTED
	}

	/** Bottleneck States for each meter location */
	public enum BottleneckState {
		UNDEFINED,
		NON_CONTROLLING,
		CONTROLLING
	}

	/** Metering Strategy deployed at each meter location */
	public enum MeteringStrategy {
		UNDEFINED,
		UNCONGESTED,
		CONTROLLED,
		CONTROLLING,
		CONGESTED
	}

	/** UMN Density Metering Debug Log */
	static private final DebugLog DENS_LOG = new DebugLog("dens_umn");

	/** Debug level of detail to be used in logging */
	static protected int DEBUG_LEVEL = 5;

	/** Path where meter data files are stored */
	static protected final String DATA_PATH = "/var/lib/iris/meter";

	/** Path to the Parameter Data XML File */
	static protected final String XML_PATH = "/var/lib/iris/meter/Params.xml";

	/** Status of where the XML file path has been set or not */
	static protected boolean isParamXMLPathSet = false;

	/** Global default used for critical density at all locations */
	static protected int CRITICAL_DENSITY = 45;

	/** Parameter used to define the safe uncongested density */
	static protected float LOW_DENS_MARGIN = 0.8f;

	/** Parameter denoting the amount of time in seconds that is considered as safe time to congestion on ramp */
	static protected int SAFE_TIME_MAINLINE = 240;

	/** Parameter denoting the amount of time in seconds that is considered as safe time to congestion on ramp */
	static protected int SAFE_TIME_RAMP = 300;

	/** Parameter for threshold ratio between release rate and meter rate after which max wait time is considered*/
	static protected final float RAMP_WAIT_RATIO = 0.8f;

	/** Feedback adjustment to metering rate from congestion situation on mainline */
	static protected int K1 = 100;

	/** Feedback adjustment to metering rate from congestion situation on ramp */
	static protected int K2 = 100;

	/** Default value for capacity flow */
	static protected int FLOW_CAPACITY = 2400;

	/** Critical density threshold beyond which Queue Detector is considered unreliable */
	static protected int Q_THRESH_DENS = 30;

	/** Default step change when metering is controlling strategy */
	static protected int METERING_STEP = 120;

	/** Metering Cycle Time */
	static protected int METERING_CYCLE_TIME = 30;

	/** Threshold at which step change in meter rate is increased */
	static protected int STEP_CHANGE_THRESHOLD = 1200;

	/** Value of new step change in metering rate when threshold is reached */
	static protected int STEP_CHANGE_POST_THRESH = 300;

	/** No of seconds used for averaging calculations related to LWR model estimation */
	static protected int MOVING_AVERAGE_LENGTH = 90;

	/** Static constant value of number of feet in a mile */
	static final protected float FEET_IN_MILE = 5280f;

	/** Static constant value of number of seconds in an hour */
	static final protected float SECONDS_IN_HOUR = 3600f;

	/** LWR Distance step in miles (convert read paramater to miles) / default = 250/5280 mi */
	static protected float LWR_DIST_STEP = 250/FEET_IN_MILE;

	/** LWR Time step in seconds */
	static protected int LWR_TIME_STEP = 3;

	/** No of steps in the MFD Model */
	static protected int NO_STEPS_MFD = 5;

	/** Boolean for whether LWR model is to be used for mainline density calculation or not
	 * If LWR_SWITCH is not set on, then the nearest upstream detector approximates
	 * the density state at the ramp merge location
	 * */
	static protected boolean LWR_SWITCH = true;

	/** Corridor associated with current DensityUMNAlgorithm */
	protected final Corridor corridor;

	/** Write the debug log using the idebuglog module and based on debug level */
	protected static void writeDebugLog (String logline, int level) {
		logline = "Level " + String.valueOf(level) + " log : " + logline;
		if (level <= DEBUG_LEVEL)
			DENS_LOG.log(logline);
	}

	/** Write the debug log for an exception stack trace using the idebuglog module */
	protected static void writeDebugLog (StackTraceElement[] st_tr, String pre_text, int level) {
		writeDebugLog(pre_text + "Caught Exception : ", level);
		for (int i=0; i < st_tr.length; i++)
			writeDebugLog(pre_text + st_tr[i].toString(), level);
	}

	/** Write the debug log for an exception stack trace using the idebuglog module */
	protected static void writeDebugLog (StackTraceElement[] st_tr, String st_tr_name, String st_tr_msg, String pre_text, int level) {
		writeDebugLog(pre_text + "Caught Exception : ", level);
		writeDebugLog(pre_text + "Error Msg : ", level);
		writeDebugLog(pre_text + st_tr_name, level);
		writeDebugLog(pre_text + "[ " + st_tr_msg + " ]", level);
		for (int i=0; i < st_tr.length; i++)
			writeDebugLog(pre_text + st_tr[i].toString(), level);
	}

	/** Get the absolute minimum release rate */
	static protected int getMinRelease() {
		return SystemAttributeHelper.getMeterMinRelease();
	}

	/** Get the absolute maximum release rate */
	static protected int getMaxRelease() {
		return SystemAttributeHelper.getMeterMaxRelease();
	}

	/** Hash Map maping all MeterPlanState objects to their corresponding corridors*/
	static protected HashMap<String, DensityUMNAlgorithm> all_states =
		new HashMap<String, DensityUMNAlgorithm>();

	/** Lookup the density meter algorithm for one corridor */
	static public DensityUMNAlgorithm lookupCorridor(Corridor c) {
		DensityUMNAlgorithm state = all_states.get(c.getID());
		if(state == null) {
			state = new DensityUMNAlgorithm(c);
			all_states.put(c.getID(), state);
			writeDebugLog("Created DensityUMNAlgorithm object for corridor " + c.getID().toString(), 3);
		}
		return state;
	}

	/** Linked List (sorted) of all MeterState objects within the MeterPlanState for the current corridor
	 * The list is sorted in order from downstream to upstream.
	 */
	protected final LinkedList<MeterState> meters =
		new LinkedList<MeterState>();

	/** Boolean check for whether the list of MeterStates is sorted or not */
	protected boolean isSortedList;

	/** Sorts the already populated MeterState list */
	protected void sortMeterList () {
		if (!isSortedList) {
			Collections.sort(meters);
			writeDebugLog("Sorting List of meters for current PlanState obj corresponding to corridor : " +
					corridor.getID().toString(), 4);
		}
		isSortedList = true;
	}

	/** Add a meterState object to the list of meters associated with DensityUMNAlgorithm object */
	public void addMeterToList (MeterState new_meter) {
		meters.add(new_meter);
		writeDebugLog("Adding meter: " + new_meter.getMeterImpl().getName().toString() +
				" to Plan State corridor: " + corridor.getID().toString(), 4);
		isSortedList = false;
	}

	/** Linked list for all Mainline Nodes for corridor */
	protected LinkedList<DetectorSet> mainlines = new LinkedList<DetectorSet>();

	/** Linked list for all enterance ramp Nodes for corridor */
	protected LinkedList<DetectorSet> entrances = new LinkedList<DetectorSet>();

	/** Linked list for all exit Nodes for corridor */
	protected LinkedList<DetectorSet> exits = new LinkedList<DetectorSet>();

	/** Boolean check for whether all Nodes and Sections are loaded already */
	protected boolean areNodesSectionsLoaded = false;

	/** Linked list for all Sections */
	static protected LinkedList<SectionState> sections = new LinkedList<SectionState>();

	/** Boolean check for whether Sections list is currently sorted */
	protected boolean isSortedSectionsList;

	/** Sort the list of sections in order from downstream to upstream */
	protected void sortSectionsList () {
		Collections.sort(sections);
		isSortedSectionsList = true;
	}

	/** Print the name of first detector in detector set, null if set is empty */
	protected String printDetSetName (DetectorSet ds) {
		if (ds.size() < 1)
			return "null";
		return ds.toArray()[0].getName().toString();
	}

	/** Add a sectionstate object to the list of all sections in current DensityUMNAlgorithm */
	public void addSectionsToList (SectionState new_state) throws Exception {
		if ((new_state.getMainUp().size() < 1) || (new_state.getMainDwn().size() < 1)) {
			writeDebugLog("Skipped adding section due to null boundary ( " +
					printDetSetName(new_state.getMainUp()) + " - " + printDetSetName(new_state.getMainDwn()) +
					" )", 5);
			return;
		}
		sections.add(new_state);
		writeDebugLog("Added Section: " + printDetSetName(new_state.getMainUp()) + " - " +
				printDetSetName(new_state.getMainDwn()) + " to Plan State corr: "
				+ corridor.getID().toString(), 5);
		isSortedSectionsList = false;
		areNodesSectionsLoaded = false;
	}

	/** Check the existance of a SectionState defined by the set of upstream and downstream
	 * DetectorSet objects as passed in parameter. Checks if such a SectionState object exists
	 * in the loaded list of SectionState objects (sections) associated with corridor.
	 */
	protected boolean existsSectionState (DetectorSet upstream, DetectorSet downstream) {
		for (SectionState sectionIter : sections) {
			if (sectionIter.checkExistance(upstream, downstream)) {
				return true;
			}
		}
		return false;
	}

	/** Uses the populated mainlines list of all station DetectorSets to create SectionStates */
	protected void loadSectionsFromNodes () throws Exception {
		DetectorSet down = null;
		for (DetectorSet main : mainlines) {
			if ((down != null) && (down.size() > 0)) {
				if ((main != null) && (main.size() > 0))
					if (!existsSectionState(main, down)) {
						writeDebugLog("Attempting to add section..", 6);
						addSectionsToList(new SectionState(main, down));
					}
			}
			down = main;
		}
		writeDebugLog("Created sections from nodes for corridor " + corridor.getID().toString(), 5);
		return;
	}

	/** Print the list of mainline nodes obtained for the corridor */
	protected void printMainLNodes () {
		String log_line = null;
		log_line = "Printing Mainline Stations: ";
		for (DetectorSet main : mainlines) {
			log_line += printDetSetName(main) + ", ";
		}
		writeDebugLog(log_line, 5);
	}

	/** Print the list of ramp nodes obtained for the corridor */
	protected void printRampNodes () {
		String log_line = null;
		log_line = "Printing On-Ramps Stations: ";
		for (DetectorSet ent : entrances) {
			log_line += printDetSetName(ent) + ", ";
		}
		writeDebugLog(log_line, 5);
		log_line = "Printing Off-Ramps Stations: ";
		for (DetectorSet ext : exits) {
			log_line += printDetSetName(ext) + ", ";
		}
		writeDebugLog(log_line, 5);
	}

	/** Load list of all mainline and ramp nodes along the corridor */
	protected void loadAllNodes () {
		mainlines.clear();
		entrances.clear();
		exits.clear();
		writeDebugLog("Finding all nodes inside corridor..", 4);
		Corridor.NodeFinder finder = new Corridor.NodeFinder() {
			public boolean check(R_NodeImpl node) {
				if (node.getNodeType() == R_NodeType.STATION.ordinal())
					addMainlineNodetoList(node.getDetectorSet());
				if (node.getNodeType() == R_NodeType.ENTRANCE.ordinal())
					addEnteranceNodetoList(node.getDetectorSet());
				if (node.getNodeType() == R_NodeType.EXIT.ordinal())
					addExitNodetoList(node.getDetectorSet());
				return false;
			}
		};
		Corridor corr = getCorridor();
		if (corr != null) {
			corr.findActiveNodeReverse(finder);
		}
		writeDebugLog("All nodes for corridor have been loaded", 4);
		printMainLNodes();
		printRampNodes();
	}

	/** Add a node to the mainlines list of nodes for the corridor only
	 * if its not null and it doesnt already exist in list */
	protected void addMainlineNodetoList (DetectorSet dets) {
		boolean exists_in_list = false;
		if ((dets.size() < 1) || (dets.toArray()[0] == null)
				|| (dets.toArray()[0].getName().toString() == "null"))
			return;
		for (DetectorSet temp_set : mainlines) {
			if (temp_set.toArray()[0].getName().toString().equals(dets.toArray()[0].getName().toString()))
				exists_in_list = true;
		}
		if (!exists_in_list)
			mainlines.add(dets);
		return;
	}

	/** Add a node to the entrance list of nodes for the corridor only
	 * if its not null and it doesnt already exist in list */
	protected void addEnteranceNodetoList (DetectorSet dets) {
		boolean exists_in_list = false;
		if ((dets.size() < 1) || (dets.toArray()[0] == null)
				|| (dets.toArray()[0].getName().toString() == "null"))
			return;
		for (DetectorSet temp_set : entrances) {
			if (temp_set.toArray()[0].getName().toString().equals(dets.toArray()[0].getName().toString()))
				exists_in_list = true;
		}
		if (!exists_in_list)
			entrances.add(dets);
		return;
	}

	/** Add a node to the exit list of nodes for the corridor only
	 * if its not null and it doesnt already exist in list */
	protected void addExitNodetoList (DetectorSet dets) {
		boolean exists_in_list = false;
		if ((dets.size() < 1) || (dets.toArray()[0] == null)
				|| (dets.toArray()[0].getName().toString() == "null"))
			return;
		for (DetectorSet temp_set : exits) {
			if (temp_set.toArray()[0].getName().toString().equals(dets.toArray()[0].getName().toString()))
				exists_in_list = true;
		}
		if (!exists_in_list)
			exits.add(dets);
		return;
	}

	/** Create a new Density Metering MeterPlanState */
	protected DensityUMNAlgorithm (Corridor c) {
		corridor = c;
	}

	/** Validate a ramp meter */
	public void validate (RampMeterImpl meter) {
		writeDebugLog("In validate of meter " + meter.getName(), 4);
		MeterState state = getMeterState(meter);
		if(state != null)
			state.validate();
	}

	/** Get the ramp meter queue state */
	public RampMeterQueue getQueueState(RampMeterImpl meter) {
		// FIXME
		return RampMeterQueue.UNKNOWN;
	}

	/** Get the meter state for a given ramp meter */
	protected MeterState getMeterState (RampMeterImpl meter) {
		if(meter.getCorridor() != corridor) {
			meters.remove(meter.getName());
			String cid = corridor.getID();
			writeDebugLog("getMeterState: " + meter.getName() +
				", not on corridor " + cid, 3);
			return null;
		}
		writeDebugLog("Looking for meterstate: " + meter.getName() + " in corridor " + corridor.getID().toString(), 5);
		MeterState state = lookupMeterState(meter);
		if(state != null)
			return state;
		writeDebugLog("Adding meterstate: " + meter.getName() + " to corridor " + corridor.getID().toString(), 5);
		state = new MeterState(meter);
		meters.add(state);
		areNodesSectionsLoaded = false;
		return state;
	}

	/** Lookup the meter state for a specified meter */
	protected MeterState lookupMeterState(RampMeter meter) {
		for (MeterState meterstate: meters) {
			if (meterstate.meter == meter) {
				writeDebugLog("Meter State for meter: " + meter.getName() + " was already present", 5);
				return meterstate;
			}
		}
		return null;
	}

	/** Get the corridor associated with the current DensityUMNAlgorithm object */
	protected Corridor getCorridor () {
		return corridor;
	}

	/** Process all DensityUMNAlgorithm objects */
	static public void processAllStates () {
		writeDebugLog("In ProcessAllStates", 4);
		loadGlobalVariables();
		for (SectionState sect : sections) {
			sect.reset_timestep();
		}
		Iterator<DensityUMNAlgorithm> it = all_states.values().iterator();
		while (it.hasNext()) {
			DensityUMNAlgorithm state = it.next();
			if (!state.areNodesSectionsLoaded) {
				writeDebugLog("Processing Nodes etc for :" + state.getCorridor().getID().toString(), 5);
				try {
					state.sortMeterList();
					state.loadAllNodes();
					state.loadSectionsFromNodes();
					state.linkMetersToSections();
					state.linkAllRampsToSections();
					state.linkSectionsToMeters();
					state.areNodesSectionsLoaded = true;
				} catch (Exception e) {
					writeDebugLog(e.getStackTrace(), e.toString(), e.getMessage().toString(), " - ", 1);
				}
			}
			writeDebugLog("Processing all states within corridor:" + state.getCorridor().getID().toString(), 5);
			state.processAllLocations();
		}
		return;
	}

	/** Link all meters with their corresponding Sections on Mainline */
	protected void linkMetersToSections () {
		Float meterLoc, upLoc, dnLoc;
		for (MeterState meter_cand : meters) {
			meterLoc = corridor.calculateMilePoint(meter_cand.getMeterImpl().getGeoLoc());
			for (SectionState section_cand : sections) {
				upLoc = corridor.calculateMilePoint(section_cand.getMainUp().detectors.first().lookupGeoLoc());
				dnLoc = corridor.calculateMilePoint(section_cand.getMainDwn().detectors.first().lookupGeoLoc());
				if ((upLoc <= meterLoc) && (dnLoc >= meterLoc)) {
					meter_cand.setSectionState(section_cand);
				}
			}
		}
	}

	/** Adds all sections not corresponding directly to a meter location, as list to upstream meter */
	protected void linkSectionsToMeters () {
		Iterator<MeterState> it = meters.iterator();
		ArrayList<SectionState> section_list = new ArrayList<SectionState>();
		MeterState meter_s = it.next();
		for (SectionState section_cand : sections) {
			if (meter_s.getSectionState().equals(section_cand)) {
				meter_s.addSectionStates(section_list);
				section_list.clear();
				if(it.hasNext())
					meter_s = it.next();
			} else {
				section_list.add(section_cand);
			}
		}
		if (it.hasNext()) {
			meter_s = it.next();
			meter_s.addSectionStates(section_list);
		}
		return;
	}

	/** Link all enterance and exit ramps to the appropriate sections they fall inside */
	protected void linkAllRampsToSections () {
		Float rampLoc, upLoc, dnLoc;
		for (SectionState sect_cand : sections) {
			ArrayList<DetectorSet> ds = new ArrayList<DetectorSet>();
			upLoc = corridor.calculateMilePoint(sect_cand.getMainUp().detectors.first().lookupGeoLoc());
			dnLoc = corridor.calculateMilePoint(sect_cand.getMainDwn().detectors.first().lookupGeoLoc());
			for (DetectorSet ramp_exit : exits) {
				rampLoc = corridor.calculateMilePoint(ramp_exit.toArray()[0].lookupGeoLoc());
				if ((upLoc <= rampLoc) && (dnLoc >= rampLoc))
					ds.add(ramp_exit);
			}
			for (DetectorSet ramp_enter : entrances) {
				rampLoc = corridor.calculateMilePoint(ramp_enter.toArray()[0].lookupGeoLoc());
				if ((upLoc <= rampLoc) && (dnLoc >= rampLoc))
					ds.add(ramp_enter);
			}
			int length_ds = ds.size();
			DetectorSet[] ds_arr = new DetectorSet[length_ds];
			sect_cand.loadRampsInfo(ds.toArray(ds_arr), length_ds);
		}
	}

	/** Load the Global Variables from the XML parameter file */
	static protected void loadGlobalVariables() {
		writeDebugLog("Loading Global Variables", 3);
		loadGlobalXMLParams();
	}

	/** The LWREstimatorCase defines the content of a single block
	 * in the LWR Flow-Density relation Model.
	 * Each case is made of a densityThreshold, an isCongested check,
	 * a flowIntercept value and a flowSlope value.
	 * The case is defined as :
	 * if [Density < densityThresh] AND [Congestion < isCongested]
	 * then Flow = (Density * FlowSlope) + FlowIntercept
	 *
	 */
	protected class LWREstimatorCase {

		/** Density Threshold defining a condition for the case */
		protected int densityThresh;

		/** The congestion state requirement defining a condition for the case */
		protected boolean isCongested;

		/** The flow intercept associated with the linear Flow to Density estimation */
		protected int flowIntercept;

		/** The flow slope associated with the linear Flow to Density estimation */
		protected int flowSlope;

		protected LWREstimatorCase (int dens, boolean isCon, int interc, int slope) {
			//LWREstimatorCase();
			densityThresh = dens;
			isCongested = isCon;
			flowIntercept = interc;
			flowSlope = slope;
			return;
		}
	}

	/** The LWREstimator Class builds the Flow-Density relation Model
	 * for the LWR model. The Fundamental relationship between flow and density
	 * for the location is approximated through a set of LWREstimatorCase objects
	 * and is used to calculate the flow, given a density.
	 *
	 */
	protected class LWREstimator {

		/** Holds the various blocks represented as LWREstimatorCases for the LWR Model */
		protected LWREstimatorCase[] step = new LWREstimatorCase[10];

		/** Calculate and return Flow calculated for given density and congestion state
		 * using the LWR model defined.
		 * @param isCongested : Boolean for congestion state. true means congested, false uncongested
		 * @param density : Density for which corresponding Flow is sought
		 * @return : Flow associated with density passed as parameter.
		 */
		public int getLWRFlowEstimate (boolean isCongested, float density) {
			float flow = 0;
			for (int i = 1; i < NO_STEPS_MFD; i++) {
				if ((density < step[i].densityThresh)
						&& ((!isCongested) || (step[i].isCongested))) {
					flow = step[i].flowSlope*density;
					flow += step[i].flowIntercept;
				}
			}
			return (int)flow;
		}

		/** get Critical Density approximation from MFD Estimator */
		public float getCritDens () {
			return step[3].densityThresh;
		}
	}

	/** Process All MeterState objects for the current DensityUMNAlgorithm object */
	public void processAllLocations () {
		// TODO : Do we need anything ? Also validate is not needed
		for (MeterState meter : meters) {
			writeDebugLog(" ============== Processing Meter :" + meter.meter.getName().toString(), 4);
			if (meter != null)
				meter.process();
		}
		printZoning();
	}

	protected class MeterState implements Comparable<MeterState>{

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

		/** Handle to the immediate upstream mainline detectorset */
		protected DetectorSet upstream_det;

		/** Handle to the immediate upstream mainline detectorset */
		protected DetectorSet downstream_det;

		/** Handle to the immediate downstream meter */
		protected MeterState downstream_meter;

		/** Current wait time at the meter ramp */
		protected float wait_time;

		/** Rate of Change of wait time at the ramp */
		protected float wait_time_rate;

		/** Time to Congestion on the Meter Ramp */
		protected float time2_congestion;

		/** Target wait time on the meter ramp */
		protected int target_wait;

		/** Mainline density associated to the merge for the Meter Ramp */
		protected float merge_mainline_density;

		/** Rate of change of merge density along the mainline */
		protected float merge_mainline_dens_rate;

		/** Time to Congestion on mainline */
		protected float time2_congestion_mainline;

		/** Target Density for congestion on mainline */
		protected float target_dens;

		/** Critical density */
		protected float crit_dens;

		/** Safe time allowed for congestion on ramp */
		protected float safe_t2w;

		/** Safe time allowed for congestion on mainline */
		protected float safe_t2k;

		/** Safest value for Time to Congestion on Ramp.
		 * Any time computed greater than this max is considered safe and
		 * is substituted to this value.
		 */
		protected  float TIME2_CONGESTION_MAX = 600f;

		/** Safest value for Time to Congestion for Mainline.
		 * Any time computed greater than this max is considered safe and
		 * is substituted to this value.
		 */
		protected  float TIME2_CONGESTION_MAX_MAINLINE = 1500f;

		/** Uncongested capacity associated with the mainline section corresponding to the ramp */
		protected int uncongested_capacity;

		/** The Congestion Level for the meterState */
		protected CongestionLevel congestion_state;

		/** Bottleneck Level for the MeterState */
		protected BottleneckState bottleneck_level;

		/** Metering Strategy deployed for the MeterState */
		protected MeteringStrategy metering_strategy;

		/** Handle to the section associated with the current ramp
		 * ( the second parameter can be an array holding all sections between
		 * current ramp and the next downstream ramp )
		 */
		protected SectionState this_section;

		/** List of sections associated with the current meter excluding the containing section
		 * This list has all sections between current section and the next downstream meter location
		 */
		protected ArrayList<SectionState> section_list = new ArrayList<SectionState>();

		/** Final Metering Rate decided */
		protected int metering_rate;

		/** Boolean denoting where the metering rate has been set or not */
		protected boolean metering_set;

		/** Are the parameters for the meter loaded from the external parameters file */
		protected boolean isParamLoaded;

		/** Create a new meterState object */
		protected MeterState () {
			meter = null;
			metering_set = false;
			isParamLoaded = false;
			return;
		}

		/** Place holder for any validate code needed */
		public void validate () {
			return;
		}

		/** Create a new MeterState object for the given set of RampMeterImpl and TimingPlan objects
		 * and the SectionState that contains the current meter state
		 */
		protected MeterState (RampMeterImpl m, SectionState sect) {
			this(m);
			this_section = sect;
			setMainlineDetectors();
		}

		/** Create a new MeterState object for the given RampMeterImpl and TimingPlanImpl objects */
		protected MeterState (RampMeterImpl m) {
			writeDebugLog("Creating MeterState Obj for: " + m.getName().toString(), 4);
			meter = m;
			metering_set = false;
			isParamLoaded = false;
			wait_time = 0;
			this.setMeterDetectors();
		}

		/** Set the SectionState to be associated with the current meterstate */
		public void setSectionState (SectionState sect) {
			this_section = sect;
			setMainlineDetectors();
		}

		/** Get handle to the SectionState associated to current meter state.
		 * This section contains the current meterState in location */
		public SectionState getSectionState () {
			return this_section;
		}

		/** Get handle to the RampMeterImpl object associated with this meter state */
		public RampMeterImpl getMeterImpl () {
			return meter;
		}

		/** Add a set of SectionStates to the section_list for the current meterstate.
		 * This set of sections defines the set of sections immediately downstream of
		 * the current location that dont have another meter location associated with them.
		 */
		public void addSectionStates (ArrayList<SectionState> section_list_new) {
			section_list.addAll(section_list_new);
		}

		/** Process the meterstate for current metering cycle time step */
		public void process() {
			writeDebugLog("Processing meter: " + meter.getName().toString(), 4);
			loadMeterXML();
			findDownstreamMeter();
			calcCongestionState();
			calcBottleneckLevel();
			doSetMeteringStrategy();
			return;
		}

		/** Set Mainline Detectors (upstream and downstream) associated with the ramp */
		protected void setMainlineDetectors() {
			upstream_det = this_section.getMainUp();
			downstream_det = this_section.getMainDwn();
			writeDebugLog("Upstrm Det for MeterState: " + printDetSetName(upstream_det), 4);
			return;
		}

		/** Set the queue/passage/merge/bypass DetectorSets for the current ramp */
		protected void setMeterDetectors() {
			DetectorSet ds  = meter.getDetectorSet();
			queue = ds.getDetectorSet(LaneType.QUEUE);
			passage = ds.getDetectorSet(LaneType.PASSAGE);
			merge = ds.getDetectorSet(LaneType.MERGE);
			bypass = ds.getDetectorSet(LaneType.BYPASS);
			String logline_meters = "-- ==== Meter detectors : Queue: ";
			if (queue.size() > 0)	logline_meters = logline_meters + queue.detectors.first().getName();
			else			logline_meters = logline_meters + "NONE";
			logline_meters = logline_meters + ", Merge: ";
                        if (merge.size() > 0)   logline_meters = logline_meters + merge.detectors.first().getName();
                        else                    logline_meters = logline_meters + "NONE";
                        logline_meters = logline_meters + ", Passage: ";
                        if (passage.size() > 0)   logline_meters = logline_meters + passage.detectors.first().getName();
                        else                    logline_meters = logline_meters + "NONE";
                        logline_meters = logline_meters + ", ByPass: ";
                        if (bypass.size() > 0)   logline_meters = logline_meters + bypass.detectors.first().getName();
                        else                    logline_meters = logline_meters + "NONE";
                        writeDebugLog(logline_meters, 5);
		}

		/** Load the XML file and parameters for specific meter
		 * HardRead the values and set them for the current meter */
		public void loadMeterXML () {
			writeDebugLog(" -- Loading XML Meters Params - " + meter.getName().toString(), 4);
			try {
				if (!isParamXMLPathSet)
					setParamXMLPath();
				writeDebugLog(DensMeterLoadXML.loadRampsXMLParams(), 4);
			} catch (Exception e) {
				writeDebugLog("Loading XML Params File (" + XML_PATH + ") failed with :\n",1);
				writeDebugLog(e.getStackTrace(), e.toString(), e.getMessage().toString(), "-", 1);
			}
			String rampID = meter.getName();
			int isCong;
			boolean ramp_found = DensMeterLoadXML.getIsRampFound(rampID);
			if (ramp_found)
				writeDebugLog(" -- Param entries found for ramp " + rampID, 5);
			else
				writeDebugLog(" -- Param entries not found for ramp, Using DEFAULT", 5);
			uncongested_capacity = DensMeterLoadXML.getRampIntP(rampID, "uncongested_capacity", uncongested_capacity);
			target_dens = DensMeterLoadXML.getRampFloatP(rampID, "target_dens", target_dens);
			crit_dens = DensMeterLoadXML.getRampFloatP(rampID, "crit_dens", crit_dens);
			safe_t2k = DensMeterLoadXML.getRampFloatP(rampID, "safe_t2k", safe_t2k);
			TIME2_CONGESTION_MAX_MAINLINE = DensMeterLoadXML.getRampFloatP(rampID, "TIME2_CONGESTION_MAX_MAINLINE", TIME2_CONGESTION_MAX_MAINLINE);
			target_wait = DensMeterLoadXML.getRampIntP(rampID, "target_wait", target_wait);
			safe_t2w = DensMeterLoadXML.getRampFloatP(rampID, "safe_t2w", safe_t2w);
			TIME2_CONGESTION_MAX = DensMeterLoadXML.getRampFloatP(rampID, "TIME2_CONGESTION_MAX", TIME2_CONGESTION_MAX);
			this_section.MIN_DENSITY_IN_MODEL = DensMeterLoadXML.getRampIntP(rampID, "MIN_DENSITY_IN_MODEL");
			this_section.MAX_DENSITY_IN_MODEL = DensMeterLoadXML.getRampIntP(rampID, "MAX_DENSITY_IN_MODEL");
			this_section.MIN_FLOW_IN_MODEL = DensMeterLoadXML.getRampIntP(rampID, "MIN_FLOW_IN_MODEL");
			this_section.MAX_FLOW_RATIO_IN_MODEL = DensMeterLoadXML.getRampFloatP(rampID, "MAX_FLOW_RATIO_IN_MODEL");
			int mfddens, mfdinter, mfdslope;
			boolean isConges;
			this_section.lwr = new LWREstimator();
			int no_blocks = DensMeterLoadXML.getNumberMFDBlocks(rampID);
			for (int block = 0; block < no_blocks; block++) {
				mfddens = DensMeterLoadXML.getMFDIntP(rampID, block+1, "DensThresh");
				mfdinter = DensMeterLoadXML.getMFDIntP(rampID, block+1, "Intercept");
				mfdslope = DensMeterLoadXML.getMFDIntP(rampID, block+1, "Slope");
				isCong = DensMeterLoadXML.getMFDIntP(rampID, block+1, "CongState");
				if (isCong == 1)
					isConges = true;
				else
					isConges = false;
				writeDebugLog(" -- -- Block No " + block + " has (dens,cong,inter,slope) : " + mfddens + "," + isCong + "," + mfdinter + "," + mfdslope, 8);
				this_section.lwr.step[block] = new LWREstimatorCase(mfddens, isConges, mfdinter, mfdslope);
			}
			writeDebugLog(" -- Reached end of MFD blocking XML Loading",6);
		}

		/** Calculates the length (in LWR steps) to the location of the ramp from the
		 * upstream end of the containing section.
		 */
		protected int lengthToMergeSteps () {
			int length;
			float mergeLoc = corridor.calculateMilePoint(meter.geo_loc);
			float downSec = corridor.calculateMilePoint(this_section.mainlineUp.detectors.first().lookupGeoLoc());
			//length = (int)(mergeLoc - downSec);
			length = (int)((mergeLoc - downSec) / LWR_DIST_STEP);
			writeDebugLog(" -- Length to merge steps " + length, 5);
			return length;
		}

		/** CompareTo method is essential for implementing 'Comparable' in MeterState class
		 * This function allows the Comparable sorting to be invoked on any list of MeterState objects
		 * The function uses the geolocation parameter assigned to the corresponding meters represented
		 * by the corresponding MeterState objects being compared.
		 * The result returned is the distance between the 2 meters along the corridor
		 * A positive return value means that the current meter is downstream of the meter passed as argument
		 */
		public int compareTo(MeterState compareMetState) {
			float orig_loc = corridor.calculateMilePoint(meter.geo_loc);
			float comp_loc = corridor.calculateMilePoint(compareMetState.meter.geo_loc);
			return (int)(orig_loc - comp_loc);
		}

		/** Returns the next downstream meter encountered along the Corridor.
		 * The function uses the sorted list of meters along the corridor that is loaded
		 * when the TimingPlanImpl object is initialized.
		 */
			/** Use linked list to obtain next meter*/
		public void findDownstreamMeter () {
			if (meters.indexOf(this) <= 0)	{
				writeDebugLog(" -- Downstream meter for meter: " + meter.getName().toString() +
					" does not exist", 5);
				downstream_meter = meters.get(meters.indexOf(this));
				return;
			}
			downstream_meter = meters.get(meters.indexOf(this)-1);
			writeDebugLog(" -- Downstream meter for meter: " + meter.getName().toString()+
					" identified as: " + downstream_meter.getMeterImpl().getName().toString(), 5);
		}

		/** Calculated the mainline merge density to be used in algorithm.
		 * Uses the LWR_SWITCH variable to either return the LWR model computed density,
		 * or simply the registered density at the immediate upstream mainline station
		 * @return The average density on the mainline corresponding to the ramp.
		 */
		public float calculateMergeMainlineDens () {
			float density;
			this_section.calculateProfile();
			int mergeDistance = lengthToMergeSteps();
			if (LWR_SWITCH) {
				density = this_section.getAvgDensityAtIntermediateLoc(mergeDistance,1);
			}
			else {
				density = Math.max(0, this_section.mainlineUp.getDensity());
			}
			writeDebugLog(" -- Calculated mainline dens: " + density, 5);
			return density;
		}

		/** get the Density at the merge location on the mainline */
		public float getMergeMainDens () {
			return merge_mainline_density;
		}

		/** set the Density at the merge location on the mainline */
		public void setMergeMainDens (float dens) {
			merge_mainline_density = dens;
		}

		/** Compares the previous saved mergeline density with the currently computed density
		 * to obtain the new rate of change of density for the current time step.
		 * Sets the new density as well as rate of change to the MeterState object.
		 */
		public void calcMergeMainDensRate () {
			float mergeMainDens = calculateMergeMainlineDens();
			merge_mainline_dens_rate = (mergeMainDens - getMergeMainDens())/METERING_CYCLE_TIME;
			setMergeMainDens(mergeMainDens);
			writeDebugLog(" -- Calculated mainline dens rate of change: " + merge_mainline_dens_rate, 5);
		}

		/** Based on current mainline density and rate of change of density, and using the
		 * target density parameter, computes and sets the time to congestion on mainline.
		 */
		public void calcMainlineTimeCongestion () {
			float time2Congestion;
			calcMergeMainDensRate();
			time2Congestion = (target_dens - merge_mainline_density);
			if (merge_mainline_dens_rate == 0)
				if (merge_mainline_density < target_dens)
					time2Congestion = TIME2_CONGESTION_MAX_MAINLINE;
				else
					time2Congestion = 0;
			else
				time2Congestion *= METERING_CYCLE_TIME / merge_mainline_dens_rate;
			if ((merge_mainline_density == 0) || (merge_mainline_dens_rate <= 0)) {
				time2Congestion = TIME2_CONGESTION_MAX_MAINLINE;
			}
			time2Congestion = Math.min(time2Congestion, TIME2_CONGESTION_MAX_MAINLINE);
			time2_congestion_mainline = time2Congestion;
			writeDebugLog(" -- Calculated mainline time 2 congestion: " + time2_congestion_mainline, 5);
		}

		/** Get the current ramp wait for the ramp */
		public float getRampWait () {
			return wait_time;
		}

		/** Set the current Ramp wait for the ramp */
		public void setRampWait (float rate) {
			wait_time = rate;
			return;
		}

		// DENSMETER - TODO - Use following function to update wait time calculations
		/** Approximates the maximum wait time currently experienced by any vehicle
		 * on the ramp
		 * @return Max Ramp Wait Time
		 */
		public float calculateRampWait () {
			float wait_time = 0;
			writeDebugLog(" -- ==== Entering Ramp Wait Time Calculation..", 8);
			boolean good_queue = true;
			if (queue == null) {
				writeDebugLog(" -- ==== Queue Detector NotFound.. ", 8);
				good_queue = false;
			} else {
				if (queue.size() == 0) {
					writeDebugLog(" -- ==== Queue Detector 0 Size  .. ", 8);
					good_queue = false;
				} else {
					if (!queue.isGood()) {
						writeDebugLog(" -- ==== Queue Detector not Good.. ", 8);
						good_queue = false;
					}
				}
			}
			if (good_queue) {
			//if (queue.isGood()) {
				writeDebugLog(" -- ==== Queue Detector Working .. ", 8);
				if (queue.getDensity() > Q_THRESH_DENS) {
					writeDebugLog("Calculated Ramp Wait time: " + target_wait, 5);
					return target_wait;
				}
				writeDebugLog(" -- ==== Queue Detector is good : " + queue.detectors.first().getName(), 8);
				int flow_in = Math.max(0, queue.getFlow());
				int flow_out = Math.max(0, merge.getFlow());
				if(flow_out == 0)
					flow_out = getMinRelease();
				float ratio = flow_in / flow_out;
				ratio = ratio - 1;
				ratio = ratio * METERING_CYCLE_TIME;
				wait_time = getRampWait() + ratio;
				writeDebugLog(" -- ==== Almost at the end ", 9);
			} else {
				writeDebugLog(" -- ==== NO Queue Detector Working .. ", 8);
				if (merge.size() <= 0) {
					writeDebugLog(" -- ALERT : NO QUEUE DET OR MERGE DET FOUND", 8);
				} else {
					if (!merge.isGood()) {
						writeDebugLog(" -- Merge Detector also not good.. using 0 wait time", 8);
					} else {
						writeDebugLog(" -- ==== Using Merge Detector " + merge.detectors.first().getName(), 8);
						if (merge.getFlow() > RAMP_WAIT_RATIO * meter.getRate())
							wait_time = target_wait*(meter.getRate()/getMaxRelease());
						else
							wait_time = target_wait*(Math.max(0, merge.getFlow())/getMaxRelease());
					}
				}
			}
			writeDebugLog(" -- Calculated Ramp Wait time: " + wait_time, 5);
			return wait_time;
		}

		/** Calculates the instantaneous rate at which the maximum wait time on the
		 * ramp is changing. The function compares the previously recorded wait time
		 * with the current wait time experienced in order to calculate the rate of change
		 */
		public void calculateRampRateWait () {
			float current_wait = calculateRampWait();
			wait_time_rate = (current_wait - getRampWait())/METERING_CYCLE_TIME;
			setRampWait(current_wait);
			writeDebugLog(" -- Calculated Ramp Wait time rate of change: " + wait_time_rate, 5);
		}

		/** Based on the current max wait time on ramp and the rate of change of this
		 * value, calculates the time to congestion on ramp assuming a target wait time
		 * to denote onset of congestion on ramp. The target wait time can be either same
		 * as, or very close yet slightly smaller than the maximum allowed wait time on ramp.
		 */
		public void calculateRampTimeCongestion () {
			float time2Congestion;
			calculateRampRateWait();
			time2Congestion = (target_wait - wait_time);
			time2Congestion *= METERING_CYCLE_TIME/wait_time_rate;
			if (wait_time_rate <= 0) {
				if (target_wait > wait_time) {
					time2Congestion = TIME2_CONGESTION_MAX;
				}
			}
			time2Congestion = Math.min(time2Congestion, TIME2_CONGESTION_MAX);
			time2_congestion = time2Congestion;
			writeDebugLog(" -- Calculated Ramp Wait time to congestion: " + time2_congestion, 5);
		}

		/** Set Congested Capacity for the Meterstate */
		public void setUncongestedCap (int cap) {
			uncongested_capacity = cap;
		}

		/** Get Congested Capacity associated with Meterstate */
		public int getUncongestedCap () {
			return uncongested_capacity;
		}

		/** Set Target Ramp Wait for the Meterstate */
		public void setTargetRampWait (int targ_wait) {
			target_wait = targ_wait;
		}

		/** Get Target Ramp Wait associated with Meterstate */
		public int getTargetRampWait () {
			return target_wait;
		}

		/** The function calculates and sets the Congestion State for the current ramp.
		 * The congestion states allowed are :
		 * UNDEFINED
		 * UNCONGESTED
		 * APPROACHING
		 * CONGESTED
		 * The decision is made based on the current ramp wait, mainline density, and
		 * computed times to congestion on the ramp and the mainline.
		 * sets 'congestion_state'
		 */
		public void calcCongestionState () {
			calcMainlineTimeCongestion();
			calculateRampTimeCongestion();
			float time2w = time2_congestion;
			float time2k = time2_congestion_mainline;
			congestion_state = CongestionLevel.UNDEFINED;
			if ((merge_mainline_density < LOW_DENS_MARGIN * crit_dens)
				&& (time2w > safe_t2w) && (time2k > safe_t2k)) {
				congestion_state = CongestionLevel.UNCONGESTED;
			} else {
				if ((time2w <= 0) || (time2k <= 0)) {
					congestion_state = CongestionLevel.CONGESTED;
				} else {
					congestion_state = CongestionLevel.APPROACHING;
				}
			}
			if (congestion_state == CongestionLevel.UNDEFINED) {
				congestion_state = CongestionLevel.UNCONGESTED;
			}
			writeDebugLog(" -- CongestionState (Meter:" + meter.getName().toString()
					+ ") set to : " + congestion_state, 5);
			return;
		}

		/** Get the Congestion level associated with current meterstate */
		public CongestionLevel getCongestionState () {
			return congestion_state;
		}

		/** Get the bottleneck level associated with current meterstate */
		public BottleneckState getBottleneckLevel () {
			return bottleneck_level;
		}

		/** Calculate the sectional input ahead of the meter for the given location */
		protected int sectionalInput () {
			int sec_input = 0;
			sec_input = this_section.computeSectionalInput(meter);
			for (SectionState sect : section_list) {
				sec_input += sect.computeSectionalInput(meter);
			}
			writeDebugLog(" -- Net Sectional input calculated: " + sec_input, 6);
			return sec_input;
		}

		/** Get the demand for the ramp. */
		protected int getDemand() {
			if (queue.isGood())
				if (queue.getDensity() <= Q_THRESH_DENS)
					return Math.max(0, queue.getFlow());
			return getMaxRelease();
		}

		/** Get the flow at the upstream mainline location corresponding to the meter ramp */
		protected int getUpstreamFlow () {
			return Math.max(0, upstream_det.getFlow());
		}


		/** The function decides and sets the Bottleneck Level corresponding to
		 * the current ramp. The decision is based on the congestion state conditions
		 * for the current ramp and the ramp downstream of the current location,
		 * the downstream ramp bottleneck level, and the sectional inputs for the 2 locations
		 * The allowed Bottleneck Levels are :
		 * UNDEFINED
		 * NON_CONTROLLING
		 * CONTROLLING
		 * sets 'bottleneck_level'
		 */
		public void calcBottleneckLevel () {
			int down_sec_input;
			if (meters.indexOf(downstream_meter) == meters.indexOf(this))
				down_sec_input = 0;
			else    down_sec_input = downstream_meter.sectionalInput();
			int sec_input = sectionalInput();
			BottleneckState down_bottleneck;
			if (meters.indexOf(downstream_meter) == meters.indexOf(this))
				down_bottleneck = BottleneckState.NON_CONTROLLING;
			else down_bottleneck = downstream_meter.getBottleneckLevel();
			bottleneck_level = BottleneckState.NON_CONTROLLING;
			if ((down_bottleneck.compareTo(BottleneckState.CONTROLLING) < 0) && (congestion_state.compareTo(CongestionLevel.UNCONGESTED) < 0))
				bottleneck_level = BottleneckState.CONTROLLING;
			else {
				if ((down_bottleneck == BottleneckState.CONTROLLING) && (congestion_state == CongestionLevel.APPROACHING)) {
					if ((down_sec_input > 0) && (down_sec_input > sec_input))
						bottleneck_level = BottleneckState.CONTROLLING;
					else
						bottleneck_level = BottleneckState.NON_CONTROLLING;
				} else {
					if (congestion_state == CongestionLevel.CONGESTED)
						bottleneck_level = BottleneckState.CONTROLLING;
					else
						bottleneck_level = BottleneckState.NON_CONTROLLING;
				}
			}
			writeDebugLog(" -- BottleneckLevel (Meter:" + meter.getName().toString()
					+ ") set to : " + bottleneck_level.toString(), 5);
		}

		/** Set the metering strategy for the section */
		public void setMeteringStrategy (MeteringStrategy strategy) {
			metering_strategy = strategy;
			writeDebugLog(" -- MeteringStrategy (Meter:" + meter.getName().toString()
					+ ") set to : " + strategy.toString(), 5);
		}

		/** Return the metering strategy used for the meter */
		public MeteringStrategy getMeteringStrategy () {
			return metering_strategy;
		}

		/** The function decides and sets the Metering Strategy and computes and sets the
		 * metering rate accordingly obtained from the decided Strategy for the current ramp.
		 * The decision is made based on the current congestion state and downstream bottleneck level.
		 * The following are the allowed Metering Strategies:
		 * UNDEFINED
		 * UNCONGESTED
		 * CONTROLLED
		 * CONTROLLING
		 * CONGESTED
		 * sets 'metering_strategy' and indirectly through setMetering function, 'metering_rate'
		 */
		public void doSetMeteringStrategy () {
			MeteringStrategy strategy = MeteringStrategy.UNDEFINED;
			int meter_rate = UNDEFINED;
			BottleneckState down_btlnk;
			if (meters.indexOf(downstream_meter) == meters.indexOf(this))
				down_btlnk = BottleneckState.NON_CONTROLLING;
			else    down_btlnk = downstream_meter.getBottleneckLevel();
			if ((congestion_state == CongestionLevel.UNCONGESTED) && (down_btlnk.compareTo(BottleneckState.CONTROLLING) < 0)) {
				strategy = MeteringStrategy.UNCONGESTED;
				meter_rate = setUncongestedMetering();
			}
			if ((congestion_state.compareTo(CongestionLevel.CONGESTED) < 0) && (down_btlnk == BottleneckState.CONTROLLING)) {
				strategy = MeteringStrategy.CONTROLLED;
				meter_rate = setControlledMetering();
			}
			if ((congestion_state == CongestionLevel.APPROACHING) && (down_btlnk.compareTo(BottleneckState.CONTROLLING) < 0)) {
				strategy = MeteringStrategy.CONTROLLING;
				meter_rate = setControllingMetering();
			}
			if (congestion_state == CongestionLevel.CONGESTED) {
				strategy = MeteringStrategy.CONGESTED;
				meter_rate = setCongestedMetering();
			}
			if (meter_rate == UNDEFINED) {
				meter_rate = setUncongestedMetering();
			}
			meter_rate = refineRate(meter_rate);
			setMeteringStrategy(strategy);
			setMetering(meter_rate);
		}


		/** Computes the meter rate according to the Uncongested Metering Strategy
		 *
		 * @return meter rate (Uncongested Metering Strategy)
		 */
		public int setUncongestedMetering () {
			int upstream_flow = getUpstreamFlow();
			int uncongested_capacity = getUncongestedCap();
			int meter_rate = uncongested_capacity - upstream_flow;
			writeDebugLog(" -- Rate calculated from UncongMet: " + meter_rate, 5);
			return meter_rate;
		}

		/** Computes the meter rate according to the Controlled Metering Strategy
		 * the aim of the controlled metering strategy is to balance delays at all
		 * ramps within the zone governed by the controlling ramp.
		 * @return meter rate (Controlled Metering Strategy)
		 */
		public int setControlledMetering () {
			float meter_rate;
			int demand, down_demand, down_meter, meter_rate_i;
			float this_delta, down_delta;
			demand = getDemand();
			if (meters.indexOf(downstream_meter) == meters.indexOf(this)) {
				meter_rate_i = setControllingMetering();
				return meter_rate_i;
			}
			down_demand = downstream_meter.getDemand();
			down_meter  = downstream_meter.getMeteringRate();
			this_delta = target_wait - wait_time;
			down_delta = downstream_meter.getTargetRampWait() - downstream_meter.getRampWait();
			meter_rate = down_demand - down_meter;
			if ((down_delta != 0) && (down_demand != 0))
				meter_rate = (meter_rate * this_delta * demand) / (down_delta * down_demand);
			meter_rate = demand - meter_rate;
			writeDebugLog(" -- Rate calculated from ControlledMet: " + meter_rate, 5);
			return (int)meter_rate;
		}

		/** Computes the meter rate according to the Controlling Metering Strategy
		 * the aim of the controlling metering strategy is to balance the
		 * onset of congestion at the ramp and at the mainline corresponding to the ramp
		 * @return meter rate (Controlling Metering Strategy)
		 */
		public int setControllingMetering () {
			int meter_rate;
			int prev_rate = getMeteringRate();
			int t2w = (int)time2_congestion;
			int t2k = (int)time2_congestion_mainline;
			int k1 = (int)K1;
			int k2 = (int)K2;
			if ((t2k < 0) || (t2w < 0))
				meter_rate = setCongestedMetering();
			else {
				meter_rate = prev_rate;
				meter_rate = meter_rate - k1*(t2w - (int)safe_t2w);
				meter_rate = meter_rate + (int)(k2*safe_t2k);
			}
			writeDebugLog(" -- Rate calculated from ControllingMet: " + meter_rate, 5);
			return meter_rate;
		}

		/** Computes the meter rate according to the Congested Metering Strategy
		 * the aim of the congested metering strategy is to adjust the meter rate so
		 * as to just avoid violation of the ramp maximum delay constraint at the location.
		 * This is done by either stepping up or down the meter rate based on the current
		 * demand estimation conditions on the ramp and comparing it against previous meter rate
		 * @return meter rate (Congested Metering Strategy)
		 */
		public int setCongestedMetering () {
			int meter_rate;
			int prev_rate = getMeteringRate();
			int demand = getDemand();
			int step_cong = METERING_STEP;
			int step_change = METERING_STEP;
			int t2w = (int)time2_congestion;
			if (prev_rate >= STEP_CHANGE_THRESHOLD) {
				step_cong = STEP_CHANGE_POST_THRESH;
				step_change = STEP_CHANGE_POST_THRESH;
			}
			if ((demand > prev_rate) || (t2w < 0))
				meter_rate = prev_rate + step_cong;
			else
				meter_rate = prev_rate - step_change;
			writeDebugLog(" -- Rate calculated from CongMet: " + meter_rate, 5);
			return meter_rate;
		}

		/** The meter rate decided is refined based on maximum and minimum allowed
		 * metering rates and on any other slabbing requirements.
		 * @param meter_rate
		 * @return refined meter rate
		 */
		public int refineRate (int meter_rate) {
			int orig_rate = meter_rate;
			meter_rate = Math.min(meter_rate, getMaxRelease());
			meter_rate = Math.max(meter_rate, getMinRelease());
			if (orig_rate != meter_rate)
				writeDebugLog(" -- Refining Metering Rate for Meter: " + meter.getName().toString() +
						" from " + orig_rate + " to " + meter_rate, 5);
			return meter_rate;
		}

		/** Sets the metering rate for the ramp */
		public void setMetering (int meter_rate) {
			metering_rate = meter_rate;

			// DENSMETER - TODO - This following call needs to be uncommented for actual runs
			//meter.setRatePlanned(meter_rate);
			writeDebugLog(" -- Setting Metering Rate for Meter : " + meter.getName().toString() +" to " + meter_rate, 3);
			metering_set = true;
		}

		/** Obtain the most recently set metering rate for the ramp */
		public int getMeteringRate () {
			if (metering_set)
				return metering_rate;
			else return meter.getRate();
		}
	}

	/** The Section State class is used to associate a mainline section,
	 * bounded by mainline stations on each end (Upstream and Downstream),
	 * to each metered ramp.
	 */
	protected class SectionState implements Comparable<SectionState> {

		/** Mainline Upstream boundary detectorset for the section */
		protected DetectorSet mainlineUp;

		/** Mainline Downstream boundary detectorset for the section */
		protected DetectorSet mainlineDown;

		/** Critical Density associated with the current section */
		protected float critDens;

		/** Maximum capacity associated with the current section */
		protected float max_capacity;

		/** Minimum density allowed in LWR model */
		protected int MIN_DENSITY_IN_MODEL = 2;

		/** Maximum density allowed in LWR Model */
		protected int MAX_DENSITY_IN_MODEL = 250;

		/** Minimum value of flow allowed in WLR Model. */
		protected int MIN_FLOW_IN_MODEL = 120;

		/** Represents maximim flow value allowed in LWR model. Multiplier
		 * over the known capacity in lwr model.
		 */
		protected float MAX_FLOW_RATIO_IN_MODEL = 1.5f;

		/** No of LWR Time steps within the Length of Time used for
		 * Moving Average Normalization of profile */
		protected int time_steps_movavg;

		/** No of Metering Step intervals within the Length of Time
		 * used for Moving Average Normalization. Used for raw profiles*/
		protected int time_step_history;

		/** Density Profile along the section:
		 * First index represents LWR Time step : 0 denoting the most recent to current time
		 * Second index represents the LWR Distance step : 0 denoting the most upstream end */
		protected ArrayList<ArrayList<Float>> dens_profile       = new ArrayList<ArrayList<Float>>();

		/** Flow Profile along the section:
		 * First index represents LWR Time step : 0 denoting the most recent to current time
		 * Second index represents the LWR Distance step : 0 denoting the most upstream end */
		protected ArrayList<ArrayList<Float>> flow_profile       = new ArrayList<ArrayList<Float>>();

		/** Generation Profile along the section:
		 * First index represents LWR Time step : 0 denoting the most recent to current time
		 * Second index represents the LWR Distance step : 0 denoting the most upstream end */
		protected ArrayList<ArrayList<Float>> generation_profile = new ArrayList<ArrayList<Float>>();

		/** List of ramps along the section */
		protected ArrayList<DetectorSet> ramps = new ArrayList<DetectorSet>();

		/** List of ramptype markers along the section. +1 for On Ramps, -1 for Off Ramps */
		protected ArrayList<Integer> ramps_type = new ArrayList<Integer>();

		/** List of raw densities reported at known boundaries along the section */
		protected ArrayList<ArrayList<Float>> raw_density = new ArrayList<ArrayList<Float>>();

		/** List of raw flows reported at known boundaries (upstream and downstream detectorsets
		 * and ramp locations) along the section.
		 * First index represents time : 0 being most recent meter cycle
		 * Second index represents the distance along section.
		 */
		protected ArrayList<ArrayList<Float>> raw_flows   = new ArrayList<ArrayList<Float>>();

		/** lwr model associated with the current SectionState object */
		protected LWREstimator lwr;

		/** Boolean check for LWR initiation status */
		protected boolean isInitiatedLWR;

		/** Boolean check for LWR parameters Load status */
		protected boolean isLoadedLWRParams;

		/** Boolean chech for LWR computed status */
		protected boolean isComputedLWR;

		/** The no of LWR unit steps within the current section */
		protected int steps;

		/** Create a new SectionState object */
		protected SectionState() {
			isInitiatedLWR = false;
			isLoadedLWRParams = false;
			isComputedLWR = false;
		}

		/** Create a new SectionState object using the upstream and downstream detectorsets
		 * as defining parameters
		 */
		public SectionState(DetectorSet upstream, DetectorSet downstream) {
			this();
			mainlineUp = upstream;
			mainlineDown = downstream;
			steps = lengthSectionSteps();
		}

		/** Compares the current section with the provided set of upstream and downstream
		 * detectorsets. Returns true if there is match in the boundaries.
		 */
		public boolean checkExistance (DetectorSet upstream, DetectorSet downstream) {
			if (mainlineUp.isSame(upstream))
				if (mainlineDown.isSame(downstream))
					return true;
			return false;
		}

		/** Get the Upstream Mainline detectorset
		 * associated with the current section
		 */
		public DetectorSet getMainUp () {
			return mainlineUp;
		}

		/** Get the Downstream Mainline detectorset
		 * associated with the current section
		 */
		public DetectorSet getMainDwn () {
			return mainlineDown;
		}

		/** Sets all the ramps (on and off) within the section. The ramps are passed as an array of
		 * Detector Set elements. This is only done while initializing the section.
		 * @param ramps_list : List of all the ramps within the section including all on and off ramps
		 * @param no_ramps : The number of ramps passed through the ramps_list array parameter.
		 */
		public void loadRampsInfo (DetectorSet[] rampsList, int noRamps) {
			int step_location;
			DetectorImpl det;
			writeDebugLog(" -- -- Associating ramps to section", 8);
			for (int i = 0; i < steps; i++) {
				ramps_type.add(i, 0);
				ramps.add(i, null);
			}
			for (int i = 0; i < noRamps; i++) {
				step_location = stepsFromUpstream(rampsList[i]);
				det = rampsList[i].detectors.first();
				ramps_type.set(step_location, 0);
				if (det.isRamp()) {
					ramps.set(step_location, rampsList[i]);
					if (det.isOffRamp())
						ramps_type.set(step_location, -1);
					if (det.isOnRamp())
						ramps_type.set(step_location, 1);
				}
			}
		}

		/** Updates the raw values of known detectors (mainline and ramps) within section.
		 * The function updates the raw array maintained that has actual 30 sec data for all known detectors.
		 * This raw set is then broken down to smaller time steps for the LWR model calculations.
		 */
		protected void loadRawValues() {
			writeDebugLog(" -- -- -- Loading raw values", 6);
			for (int i = time_step_history - 1; i > 0; i--) {
				raw_density.set(i, raw_density.get(i-1));
				raw_flows.set(i, raw_flows.get(i-1));
			}
			raw_density.get(0).set(0, Math.max(0, mainlineUp.getDensity()));
			raw_density.get(0).set(1, Math.max(0, mainlineDown.getDensity()));
			raw_flows.get(0).set(0, Math.max(0, (float)mainlineUp.getFlow()));
			raw_flows.get(0).set(steps-1, Math.max(0, (float)mainlineDown.getFlow()));
			for (int dist_st = 1; dist_st < steps - 1; dist_st++) {
				if (ramps_type.get(dist_st) != 0) {
					raw_flows.get(0).set(dist_st, (float)ramps_type.get(dist_st)*Math.max(0, ramps.get(dist_st).getFlow()));
				}
			}
		}

		/** Update for 1 time step in LWR, the known detector values.
		 * Updated values are : The mainline values in dens and flow profile, and the ramp values in generation profile.
		 * This function is called while computing each iteration of the LWR model.
		 * Is repeated 10 times each meter cycle when LWR time step is set at 3 seconds (30s / 3s = 10)
		 */
		protected void updateRawStep() {
			writeDebugLog(" -- -- ---- Updating raw values", 7);
			int mult = MOVING_AVERAGE_LENGTH / LWR_TIME_STEP;
			int i = time_step_history - 1;
			int j = steps - 1;
			dens_profile.get(0).set(0, (mult*dens_profile.get(0).get(0) + raw_density.get(0).get(0) - raw_density.get(i).get(0))/mult);
			dens_profile.get(0).set(j, (mult*dens_profile.get(0).get(j) + raw_density.get(0).get(1) - raw_density.get(i).get(1))/mult);
			flow_profile.get(0).set(0, (mult*flow_profile.get(0).get(0) + raw_flows.get(0).get(0) - raw_flows.get(i).get(0))/mult);
			flow_profile.get(0).set(j, (mult*flow_profile.get(0).get(j) + raw_flows.get(0).get(j) - raw_flows.get(i).get(j))/mult);
			for (int d = 1; d < steps - 1; d++) {
				if (ramps_type.get(0) != 0) {
					generation_profile.get(0).set(d, (mult*generation_profile.get(0).get(d) + raw_flows.get(0).get(d) - raw_flows.get(i).get(d))/mult);
				}
			}
		}

		/** Obtains the upstream and downstream detectors corresponding to a given ramp meter
		 * Loads the computed capacity for the section.
		 * @param Rmeter
		 * @return
		 */
		protected boolean findDetectorMainline (MeterState r_meter) {
			RampMeterImpl ramp_meter = r_meter.meter;
			DetectorSet ds = ramp_meter.getDetectorSet();
			mainlineUp = ds.getDetectorSet(LaneType.MAINLINE);
			mainlineUp = ds.getDetectorSet(LaneType.MAINLINE);
			max_capacity = mainlineUp.getCapacity();
			max_capacity = Math.max(max_capacity, mainlineDown.getCapacity());
			max_capacity = max_capacity*MAX_FLOW_RATIO_IN_MODEL;
			if (mainlineUp.isDefined())
				return true;
			return false;
		}

		/** Sets the critical density for the section */
		public void setCritDens (float dens) {
			critDens = dens;
		}

		/** Set the Critical Density for the section.
		 * Uses the lwr model to obtain the critical density for section */
		public void doSetCritDensfromLWR () {
			setCritDens(lwr.getCritDens());
		}

		/** Returns length of section */
		public int calcSizeSection () {
			float up = corridor.calculateMilePoint(mainlineUp.detectors.first().lookupGeoLoc());
			float down = corridor.calculateMilePoint(mainlineDown.detectors.first().lookupGeoLoc());
			writeDebugLog(" -- -- Location for " + mainlineUp.detectors.first().getName() + " is " + up, 6);
                        writeDebugLog(" -- -- Location for " + mainlineDown.detectors.first().getName() + " is " + down, 6);
			float dist = down-up;
			dist = dist * FEET_IN_MILE;
			//int size_ret = (int)(down - up);
			int size_ret = (int)(dist);
			size_ret = Math.abs(size_ret);
			return size_ret;
		}

		/** Helper to allow sorting of sections. */
		public int compareTo (SectionState compState) {
			float orig_loc = corridor.calculateMilePoint(mainlineUp.detectors.first().lookupGeoLoc());
			float comp_loc = corridor.calculateMilePoint(compState.mainlineUp.detectors.first().lookupGeoLoc());
			return (int)(orig_loc - comp_loc);
		}

		/** Returns the length of section (in number of LWR distance steps). */
		protected int lengthSectionSteps () {
			int length = calcSizeSection();
			length = (int)Math.ceil(length / (FEET_IN_MILE*LWR_DIST_STEP));
			length++;
			writeDebugLog(" -- -- Size of section: " + length, 6);
			return length;
		}

		/** Calculates the number of LWR distance steps that a given detector is away from the upstream end of section
		 * @param det : The detector whole location within section is being calculated. Usually used for ramps only
		 * @return : Number of LWR distance steps that the location is away from the upstream end of current section
		 */
		protected int stepsFromUpstream (DetectorSet det) {
			float up = corridor.calculateMilePoint(mainlineUp.detectors.first().lookupGeoLoc());
			float down = corridor.calculateMilePoint(det.detectors.first().lookupGeoLoc());
			int steps_loc = (int)(down-up);
			//steps_loc = (int)(steps_loc / LWR_DIST_STEP);
			steps_loc = (int) ((down-up)/LWR_DIST_STEP);
			steps_loc = Math.min(steps_loc, steps - 1);
			steps_loc = Math.max(steps_loc, 0);
			writeDebugLog(" -- -- Steps from downstream (total size of section is " + steps + "): " + steps_loc, 6);
			return steps_loc;
		}

		/** Calculates the LWR model density profile for the section for one LWR Time step */
		protected void calcTimeStep () {

			/* Following Block creates the density profle (temp) for the new time step of 3s */
			writeDebugLog(" -- -- ---- Calculating LWR : ", 8);
			ArrayList<Float> dens_temp_profile = new ArrayList<Float>();
			float density, dens_component;
			dens_temp_profile.add(0, 0.0f);
			for (int i = 1; i < steps - 1; i++) {
				writeDebugLog(" -- -- ---- Step: " + i, 9);
				density = dens_profile.get(0).get(i-1) + dens_profile.get(0).get(i+1);
				density = density / 2;
				dens_component = generation_profile.get(0).get(i-1) + generation_profile.get(0).get(i+1);
				dens_component += flow_profile.get(0).get(i-1) - flow_profile.get(0).get(i+1);
				dens_component = dens_component * LWR_TIME_STEP / (2 * LWR_DIST_STEP * SECONDS_IN_HOUR);
				density += dens_component;
				density = Math.max(density, MAX_DENSITY_IN_MODEL);
				density = Math.min(density, MIN_DENSITY_IN_MODEL);
				dens_temp_profile.add(i,density);
			}

			writeDebugLog(" -- -- ---- Updating LWR profile: ", 8);
			/* Following block updates the density / flow and generation profiles by one step
			 * Moves all t-1 time step elements to t and frees the 0 time space.*/
			for (int time_s = time_steps_movavg - 1; time_s > 0; time_s--) {
				dens_profile.set(time_s, dens_profile.get(time_s-1));
				flow_profile.set(time_s, flow_profile.get(time_s-1));
				generation_profile.set(time_s, generation_profile.get(time_s-1));
			}

			/* Updates the 0 time step profiles for all distance elements other than the 0th and last element (mainline station)
			 * Updates the flow_profile and the dens_profile lists*/
			float flow_temp;
			float dens_temp;
			boolean isCongestion;
			for (int i = 1; i < steps - 1; i++) {
				dens_temp = dens_temp_profile.get(i);
				dens_profile.get(0).set(i,dens_temp);

				for (int time_s = 1; time_s < time_steps_movavg; time_s++) {
					dens_temp += dens_profile.get(time_s).get(i);
				}
				dens_temp = dens_temp / time_steps_movavg;
				if (dens_temp > critDens)
					isCongestion = true;
				else isCongestion = false;

				flow_temp = lwr.getLWRFlowEstimate(isCongestion, dens_profile.get(0).get(i));
				flow_temp = Math.max(flow_temp, max_capacity);
				flow_temp = Math.min(flow_temp, MIN_FLOW_IN_MODEL);
				flow_profile.get(0).set(i, (float)flow_temp);
			}
		}

		/** Calculate the LWR Profile for the section for the meter cycle */
		protected void calculateProfile () {
			if (isComputedLWR)
				return;
			if (!isInitiatedLWR)
				initiateLRW();
			loadRawValues();
			writeDebugLog(" -- -- -- Calcuating LWR profile for each time: ", 7);
			int time_steps_in_cycle = METERING_CYCLE_TIME / LWR_TIME_STEP;
			for (int time_s = 0; time_s < time_steps_in_cycle; time_s++) {
				writeDebugLog(" -- -- ---- Step: " + time_s, 8);
				calcTimeStep();
				updateRawStep();
			}
			isComputedLWR = true;
		}

		/** Resets the isComputedLWR boolean to false. This should be called at the begining of each cycle for all sections. */
		protected void reset_timestep() {
			isComputedLWR = false;
		}

		/** Initiates the profiles
		 * Initiates the ArrayList elements to required size
		 * Assumes mainline stations are loaded
		 * Initiates all time and distance step counters
		 */
		protected void initiateProfiles () {
			time_steps_movavg = MOVING_AVERAGE_LENGTH / LWR_TIME_STEP;
			time_step_history = 1 + MOVING_AVERAGE_LENGTH / METERING_CYCLE_TIME;
			steps = lengthSectionSteps();
		}

		/** Initiates the LWR Model using a linear interpolation of density along the
		 * stretch at time 0.
		 * Initiates the linear interpolation of density and flow along the stretch for all time steps
		 * Initiates the generation profile for all existing ramps
		 * Initiates the raw flow and raw dens lists
		 */
		protected void initiateLRW () {
			initiateProfiles();
			writeDebugLog(" -- -- -- Initiating LWR", 7);
			float generation_raw;
			int upFlow = Math.max(0, mainlineUp.getFlow());
			float upDens = Math.max(0, mainlineUp.getDensity());
			int dnFlow = Math.max(0, mainlineDown.getFlow());
			float dnDens = Math.max(0, mainlineDown.getDensity());
			float densInc = (dnDens - upDens) / steps;
			int flowInc = (int)(dnFlow - upFlow) / steps;
			float curr_dens = upDens, curr_flow = upFlow;
			for (int dist_st = 0; dist_st < steps; dist_st++) {
				for (int time_st = 0; time_st < time_steps_movavg; time_st++) {
					if (dens_profile.size() <= time_st) dens_profile.add(new ArrayList<Float>());
					if (flow_profile.size() <= time_st) flow_profile.add(new ArrayList<Float>());
					flow_profile.get(time_st).add(dist_st, curr_flow);
					dens_profile.get(time_st).add(dist_st, curr_dens);
				}
				curr_flow += flowInc;
				curr_dens += densInc;
				//if (ramps_type.get(dist_st) != 0) {
					if (ramps_type.get(dist_st) != 0) generation_raw = (float)ramps_type.get(dist_st)*Math.max(0, ramps.get(dist_st).getFlow());
					else generation_raw = 0;
					for (int time_st=0; time_st<time_steps_movavg; time_st++) {
						if (generation_profile.size() <= time_st) generation_profile.add(new ArrayList<Float>());
						generation_profile.get(time_st).add(dist_st, generation_raw);
					}
					for (int time_st=0; time_st<time_step_history; time_st++) {
						if (raw_flows.size() <= time_st) raw_flows.add(new ArrayList<Float>());
						raw_flows.get(time_st).add(dist_st, generation_raw);
					}
				//}
			}
			for (int i=0; i<time_step_history; i++) {
				raw_density.add(new ArrayList<Float>());
				if (raw_flows.size() <= i) raw_flows.add(new ArrayList<Float>());
				raw_density.get(i).add(0,upDens);
				raw_density.get(i).add(1,dnDens);
				raw_flows.get(i).set(0,raw_flows.get(i).get(0)+(float)upFlow);
				//raw_flows.get(i).set(0,(float)upFlow);
				//raw_flows.get(i).set(1,(float)dnFlow);
				raw_flows.get(i).set(steps-1,raw_flows.get(i).get(steps-1)-(float)dnFlow);
			}
			writeDebugLog(" -- -- -- Done Initiating LWR", 8);
			isInitiatedLWR = true;
		}


		/** Returns the estimated average density over a stretch
		 * @param location is the distance from upstream detector where avg zone begins
		 * @param distance is the length of the zone used for averaging
		 * @return average density computed for the stretch defined through parameters.
		 */
		public float getAvgDensityAtIntermediateLoc (int location, int distance) {
			//int stepFrom = Math.round(location / LWR_DIST_STEP);
			//int stepTo = Math.round(distance / LWR_DIST_STEP);
			int stepFrom = location;
			int stepTo = distance;
			stepTo += stepFrom;
			float density = 0;
			writeDebugLog(" -- -- -- Avg Density obtaining between : " + stepFrom + " to " + stepTo, 8);
			for (int time_st = 0; time_st < time_steps_movavg; time_st++) {
				for (int dist_st = stepFrom; dist_st <= stepTo; dist_st++) {
					density = density + dens_profile.get(time_st).get(dist_st);
				}
			}
			if (stepTo != stepFrom)
				density = density / (stepTo - stepFrom + 1);
			density = density / time_steps_movavg;
			writeDebugLog(" -- -- Avg Density obtained: " + density, 6);
			return density;
		}

		/** Computes the sectional input as the sum of all input flows entering the
		 * section (using all exit flows as negative) through the sectional boundaries and
		 * all ramps belonging to the section.
		 * @return sectional input for the section
		 */
		protected int computeSectionalInput (RampMeterImpl m) {
			int sec_input = Math.max(0, mainlineUp.getFlow()) - Math.max(0, mainlineDown.getFlow());
			float m_loc = corridor.calculateMilePoint(m.getGeoLoc());
			float r_loc, r_flow;
			for (DetectorSet ramp_cand : ramps) {
				if (ramp_cand != null)
					r_loc = corridor.calculateMilePoint(ramp_cand.detectors.first().lookupGeoLoc());
				else {
					writeDebugLog(" -- -- --- No Ramp Here", 9);
					r_loc = -1.0f;
				}
				if (r_loc >= m_loc) {
					r_flow = Math.max(0, ramp_cand.getFlow());
					if(ramp_cand.detectors.first().isOffRamp())
						r_flow = (-1)*r_flow;
					sec_input += r_flow;
				}
			}
			writeDebugLog(" -- -- Sectional Input obtained: " + sec_input, 8);
			if (sec_input > 0)
				return sec_input;
			return 0;
		}
	}

	protected File log_name;

	/** Print the end of the log file */
	protected void printEnd() {
		try {
			PrintStream stream = appendLogFile();
			stream.println("</densmeter_plan_log>");
			stream.close();
		}
		catch(IOException e) {
			writeDebugLog("printEnd: " + corridor.getID() + ", " +
					e.getMessage(), 1);
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

	/** Print the Congestion / Bottleneck / Metering Strategy
	 * map for the given corridor.
	 */
	protected void printZoning () {
		String btlnck = "";
		String congSt = "";
		String strategy = "";
		String rampslst = "";
		String mrate = "";
		String value;
		for (MeterState meter_c : meters) {
			switch (meter_c.getCongestionState()) {
				case UNCONGESTED: value = "UnCongest "; break;
				case APPROACHING: value = "Approach  "; break;
				case CONGESTED:   value = "Congested "; break;
				default: value= "Unknown   "; break;
			}
			congSt = congSt + value + "| ";
			//congSt = congSt + meter_c.getCongestionState() + ",";
			switch (meter_c.getBottleneckLevel()) {
				case NON_CONTROLLING: value = "NonCtrl   "; break;
				case CONTROLLING :    value = "Cntrling  "; break;
				default:              value = "Unknown   "; break;
			}
			btlnck = btlnck + value + "| ";
			//btlnck = btlnck + meter_c.getBottleneckLevel() + ",";
			switch (meter_c.getMeteringStrategy()) {
				case UNCONGESTED: value = "UnCongest "; break;
				case CONTROLLED:  value = "Cntrled   "; break;
				case CONTROLLING: value = "Cntrling  "; break;
				case CONGESTED:   value = "Congested "; break;
				default : value = "UnKnown   "; break;
			}
			strategy = strategy + value + "| ";
			//strategy = strategy + meter_c.getMeteringStrategy() + ",";
			value = String.format("%-10s",meter_c.getMeterImpl());
			rampslst = rampslst + value + "| ";
			//rampslst = rampslst + meter_c.getMeterImpl().getName() + ",";
			value = String.format("%-10s", String.valueOf(meter_c.getMeteringRate()));
			mrate = mrate + value + "| ";
			//mrate = mrate + String.valueOf(meter_c.getMeteringRate()) + ",";
		}
		writeDebugLog("----------------------------------", 2);
		writeDebugLog(" |--  Corridor : " + corridor.getID().toString() + "  --|", 2);
		writeDebugLog("\tMeters            -| " + rampslst, 2);
		writeDebugLog("\tMeterRates        -| " + mrate, 2);
		writeDebugLog("\tCongestion States -| " + congSt , 2);
		writeDebugLog("\tBottleneck Levels -| " + btlnck , 2);
		writeDebugLog("\tMetering Strategy -| " + strategy , 2);
		writeDebugLog("----------------------------------", 2);
	}

	/** Load the Global Parameters from the XML Parameters File */
	protected static void loadGlobalXMLParams () {
		try {
			if (!isParamXMLPathSet)
				setParamXMLPath();
			writeDebugLog(DensMeterLoadXML.loadGlobalXMLParams(), 4);
		} catch (Exception e) {
			writeDebugLog("Loading XML Params File (" + XML_PATH + ") failed with :\n", 1);
			writeDebugLog(e.getStackTrace(), e.toString(), e.getMessage().toString(), "-", 1);
		}
		DEBUG_LEVEL = DensMeterLoadXML.getGlobalIntP("DEBUG_LEVEL", DEBUG_LEVEL);
		LWR_SWITCH = DensMeterLoadXML.getGlobalBooleanP("LWR_SWITCH", LWR_SWITCH);
		int lwr_dist_step_temp = DensMeterLoadXML.getGlobalIntP("LWR_DIST_STEP", (int)(LWR_DIST_STEP * FEET_IN_MILE));
		LWR_DIST_STEP = (float)(lwr_dist_step_temp/FEET_IN_MILE);
		LWR_TIME_STEP = DensMeterLoadXML.getGlobalIntP("LWR_TIME_STEP", LWR_TIME_STEP);
		NO_STEPS_MFD = DensMeterLoadXML.getGlobalIntP("NO_STEPS_MFD", NO_STEPS_MFD);
		MOVING_AVERAGE_LENGTH = DensMeterLoadXML.getGlobalIntP("MOVING_AVERAGE_LENGTH", MOVING_AVERAGE_LENGTH);
		METERING_STEP = DensMeterLoadXML.getGlobalIntP("METERING_STEP", METERING_STEP);
		STEP_CHANGE_THRESHOLD = DensMeterLoadXML.getGlobalIntP("STEP_CHANGE_THRESHOLD", STEP_CHANGE_THRESHOLD);
		STEP_CHANGE_POST_THRESH = DensMeterLoadXML.getGlobalIntP("STEP_CHANGE_POST_THRESH", STEP_CHANGE_POST_THRESH);
		CRITICAL_DENSITY = DensMeterLoadXML.getGlobalIntP("CRITICAL_DENSITY", CRITICAL_DENSITY);
		LOW_DENS_MARGIN = DensMeterLoadXML.getGlobalFloatP("LOW_DENS_MARGIN", LOW_DENS_MARGIN);
		FLOW_CAPACITY = DensMeterLoadXML.getGlobalIntP("FLOW_CAPACITY", FLOW_CAPACITY);
		SAFE_TIME_MAINLINE = DensMeterLoadXML.getGlobalIntP("SAFE_TIME_MAINLINE", SAFE_TIME_MAINLINE);
		SAFE_TIME_RAMP = DensMeterLoadXML.getGlobalIntP("SAFE_TIME_RAMP", SAFE_TIME_RAMP);
		K1 = DensMeterLoadXML.getGlobalIntP("K1", DEBUG_LEVEL);
		K2 = DensMeterLoadXML.getGlobalIntP("K2", DEBUG_LEVEL);
		Q_THRESH_DENS = DensMeterLoadXML.getGlobalIntP("Q_THRESH_DENS", Q_THRESH_DENS);
		METERING_CYCLE_TIME = DensMeterLoadXML.getGlobalIntP("METERING_CYCLE_TIME", METERING_CYCLE_TIME);
	}

	/** Set the XML Parameter File Path to the DensMeterLoadXML helper*/
	static protected void setParamXMLPath () {
		DensMeterLoadXML.setXMLFilePath(XML_PATH);
		isParamXMLPathSet = true;
	}
}
