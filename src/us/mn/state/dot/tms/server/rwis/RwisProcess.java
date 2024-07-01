/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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

package us.mn.state.dot.tms.server.rwis;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.RwisCondition;
import us.mn.state.dot.tms.RwisConditionSet;
import us.mn.state.dot.tms.RwisSign;
import us.mn.state.dot.tms.RwisDmsHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.RwisSignImpl;
import us.mn.state.dot.tms.server.SignMessageImpl;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.utils.MultiString;

/** RwisProcess background daemon
 *  
 * @author John L. Stanley - SRF Consulting
 */
public class RwisProcess extends Thread {

	/** RWIS process logfile */
	static final DebugLog RWIS_LOG = new DebugLog("rwis");

	/** A single thread for the RWIS process */
	static private RwisProcess THREAD;

	/** Previous cycle (milliseconds) value.
	 * If <= 0, RWIS is disabled */
	long prevRwisCycle_ms = -1;

	/** When should next RWIS update occur?
	 *     null = RWIS is disabled.
	 *  When RWIS is enabled, this is recalculated
	 *  during first call to doWait() */
	Long nextRwisUpdate = null;

	/** is RWIS enabled? */
	static private boolean bRwisEnabled = false;

	/** Initialize the thread. */
	private RwisProcess() {
		super("rwis");
	}

	/** Main RWIS loop.
	 *  Exits when server ends. */
	@Override
	public void run() {
		// run loop at minimum priority
		setPriority(MIN_PRIORITY);
		// Make sure an RwisScratchpad is defined for each rwis_dms record...
		// (Only needs to be done once at thread startup.)
		Iterator<RwisSign> rdit = RwisDmsHelper.iterator();
		while (rdit.hasNext()) {
			RwisSign rd = rdit.next();
			if (rd != null) {
				DMS d = DMSHelper.lookup(rd.getName());
				ensureScratchpad(d);
			}
		}
		// the main checker loop
		while (true) {
			try {
				if (doWait() == false)
					break;
				updateRwis();
			} catch (Exception e) {
				//FIXME:  Following should be: RWIS_LOG.logException(e);
				e.printStackTrace();
			}
		}
	}

	/** Idle-loop waiting for one of the following:
	 *    Time for a normal RWIS update,
	 *    System attribute rwis_cycle_sec was changed,
	 *    A system time-change (DST change or similar).
	 */
	private boolean doWait() {
		long prev;
		long rwisCycle_ms = 0;
		long now  = System.currentTimeMillis();
		while (true) {
			prev = now;
			// Sleep for ~1 second
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				return false; // Server shutdown; exit thread
			}
			// Update local variables
			rwisCycle_ms = 1000 * SystemAttrEnum.RWIS_CYCLE_SEC.getInt();
			bRwisEnabled = (rwisCycle_ms > 0);
			now = System.currentTimeMillis();
			// If rwisCycle_ms has changed, reset cycle and do an update now
			if (prevRwisCycle_ms != rwisCycle_ms) {
				prevRwisCycle_ms = rwisCycle_ms;
				nextRwisUpdate = now;
				break;
			}
			// If RWIS is disabled, go back to sleep
			if (bRwisEnabled == false)
				continue;
			// If time-change is detected, reset cycle and do an update
			if (Math.abs(now - prev) > 10000) {
				nextRwisUpdate = now;
				break;
			}
			// If time for automatic update, do an update
			if ((nextRwisUpdate != null) && (nextRwisUpdate - now <= 0))
				break;
		}

		// calculate time of next update before we start current update.
		if (bRwisEnabled) {
			while (nextRwisUpdate - now < 0)
				nextRwisUpdate += rwisCycle_ms;
		}
		else
			nextRwisUpdate = null;

		String nextUpdateTimestamp = "disabled";
		if (nextRwisUpdate != null)
			nextUpdateTimestamp = (new Timestamp(nextRwisUpdate)).toString();
		RWIS_LOG.log("" + (new Timestamp(now)) + "\t" + nextUpdateTimestamp);// + "\r\n");
		return true;
	}

	/** Calculate a crude message-complexity metric.
	 * Fewer pages are better.  For messages with the 
	 * same number of pages, shorter messages are better. */
	private int calcMsgComplexity(MsgPattern msg) {
		if (msg == null)
			return Integer.MAX_VALUE; // avoid blank messages
		MultiString m = new MultiString(msg.getMulti());
		int pages = m.getNumPages();
		int len   = m.asText().length();
		return (pages * 10000) + len;
	}
	
	/** Persistent map of RwisScratchpad
	 *  objects indexed by DMS name. */
	static private HashMap<String, RwisScratchpad> scratchpadMap = new HashMap<String, RwisScratchpad>();
	
	/** Ensure a RwisScratchpad object for a
	 *  given sign exists in the scratchpadMap. */
	private void ensureScratchpad(DMS d) {
		if (d instanceof DMSImpl) {
			DMSImpl dms = (DMSImpl)d;
			String dmsName = dms.getName();
			RwisScratchpad signInfo = scratchpadMap.get(dmsName);
			if (signInfo == null) {
				signInfo = new RwisScratchpad(dms);
				scratchpadMap.put(dmsName, signInfo);
			}
		}
	}
		
	/** Perform an update for all RWIS-enabled signs */
	private void updateRwis() {
		// initialize work areas
		HashMap<String, RwisConditionSet> essConditionsMap =
				new HashMap<String, RwisConditionSet>();
		RwisMsgDataset rwisMsgs = new RwisMsgDataset();
		WeatherSensorImpl.loadTestProperties();
		
		// Make sure an RwisScratchpad is defined for each enabled RWIS sign...
		for (DMS d: getEnabledRwisDms()) {
			ensureScratchpad(d);
		}
		// For each RWIS sign...
		for (RwisScratchpad scratchpad: scratchpadMap.values()) {
			DMSImpl dms = scratchpad.getDms();
			scratchpad.clearConditions();
			String dmsName = dms.getName();
			RWIS_LOG.log("RWIS Sign: "+dmsName);
			boolean bDmsHasRwisHashtags = false;
			boolean bDmsHasWeatherSensors = false;
			RwisSignImpl rwisSign = RwisSignImpl.findOrCreate(dmsName);
			ArrayList<String> hashTags = getRwisHashtags(dms);
			if (!hashTags.isEmpty())
				bDmsHasRwisHashtags = true;
			
			// Collect RWIS conditions from ESS associated with the DMS
			RwisConditionSet dmsConditions = new RwisConditionSet();
			if (bRwisEnabled) {
				ArrayList<WeatherSensor> wsArray = DMSHelper.getAssociatedWeatherSensors(dms);
				for (WeatherSensor ws: wsArray) {
					bDmsHasWeatherSensors = true;
					String essName = ws.getName();
					RWIS_LOG.log("\tESS: "+essName);
					RWIS_LOG.log("\t\tmax.wind.gust.speed = " + ws.getMaxWindGustSpeed());  
					RWIS_LOG.log("\t\tvisibility.m....... = " + ws.getVisibility());  
					RWIS_LOG.log("\t\tsurf.temp.......... = " + ws.getSurfTemp());  
					RWIS_LOG.log("\t\tpvmt.friction...... = " + ws.getPvmtFriction());
					// Only calculate RWIS conditions once at each ESS
					RwisConditionSet conditions = essConditionsMap.get(essName);
					if (conditions == null) {
						conditions = new RwisConditionSet(ws);
						essConditionsMap.put(essName, conditions);
					}
					dmsConditions.add(conditions);
					RWIS_LOG.log("\t\tESS RWIS Conditions: "+conditions);
				}
			}
			scratchpad.setConditionSet(dmsConditions);
			RWIS_LOG.log("\tDMS RWIS Conditions: "+dmsConditions);
			RWIS_LOG.log("\tRWIS Hashtags: " + hashTags);
			if (!hashTags.isEmpty())
				bDmsHasRwisHashtags = true;
				
			if (rwisSign != null)
				rwisSign.setRwisConditionsNotify(dmsConditions.toString());
			
			// Find highest priority RWIS message(s) 
			// that matches the DMS hashtags...
			ArrayList<MsgPattern> msgs = new ArrayList<MsgPattern>();
			if (bDmsHasRwisHashtags) {
				for (RwisCondition cond: dmsConditions.getArray()) {
					Integer priority = cond.getPriority();
					for (String hashtag: hashTags) {
						MsgPattern msg = rwisMsgs.get(hashtag, priority);
						if (msg != null)
							msgs.add(msg);
					}
					if (!msgs.isEmpty())
						break;
				}
			}
			RWIS_LOG.log("\tPotential Messages..: " + msgs);
			
			// Find the least-complex MsgPattern of the ones found
			MsgPattern curMsg = null;
			MsgPattern newMsg;
			int curComplexity = Integer.MAX_VALUE;
			int newComplexity;
			int cnt = msgs.size();
			for (int i = 0; (i < cnt); ++i) {
				newMsg = msgs.get(i);
				newComplexity = calcMsgComplexity(newMsg);
				if (newComplexity < curComplexity) {
					curMsg = newMsg;
					curComplexity = newComplexity;
				}
			}
			RWIS_LOG.log("\tSelected Message....: "+curMsg);
			if (rwisSign != null) {
				if (curMsg == null)
					rwisSign.setMsgPatternNotify(null);
				else
					rwisSign.setMsgPatternNotify(curMsg.getName());
			}
			
			// deploy/blank message on sign...
			deployUserMessage(dms, curMsg);
			
			// Delete rwis_dms record if DMS isn't an RWIS-enabled DMS any more.
			if ((curMsg == null) 
			 && (!bDmsHasRwisHashtags || !bDmsHasWeatherSensors)) {
				try {
					if (rwisSign != null)
						rwisSign.doDestroy();
				} catch (TMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				scratchpadMap.remove(dmsName);
			}
		}
	}

	/** Deploy an RWIS message-pattern or blank a sign.
	 * Returns without doing anything if on-sign user
	 * message has priority higher than RWIS_MSG_PRIORITY.
	 * Also, will not blank sign if on-sign message is not
	 * an RWIS message. */
	private void deployUserMessage(DMSImpl dms, MsgPattern mpat) {
		if (dms == null)
			return;
		SignMessage oldSignMsg = dms.getMsgUser();
		int oldPriority = (oldSignMsg == null) ? 0 : oldSignMsg.getMsgPriority();
		int rwisPriority = SystemAttrEnum.RWIS_MSG_PRIORITY.getInt();
		boolean oldWasRwisMsg = SignMessageHelper.isRwis(oldSignMsg);
		SignConfig sc = dms.getSignConfig();
		if ((oldPriority > rwisPriority)) // || (sc == null))
			return; // Don't overwrite higher priority msgs.
		SignMessage newSignMsg;
		if (mpat == null) {
			if (!oldWasRwisMsg)
				return; // Don't blank non-RWIS msgs.
			newSignMsg = dms.createMsgBlank(SignMsgSource.blank.bit());
		}
		else {
			int src = SignMsgSource.rwis.bit();
			String ms = mpat.getMulti();
			String owner = SignMessageHelper.makeMsgOwner(src, "RWIS");
			boolean fb = mpat.getFlashBeacon();
			SignMsgPriority mp = SignMsgPriority.fromOrdinal(rwisPriority);
			newSignMsg = SignMessageImpl.findOrCreate(sc, null, ms, owner, fb, mp, null);
		}
		RWIS_LOG.log("deploying: newSignMsg = "+newSignMsg);
		if (newSignMsg != null) {
			try {
				dms.doSetMsgUser(newSignMsg);
			} catch (TMSException e) {
				RWIS_LOG.log("RWIS exception: "+e.getMessage());
				//FIXME:  Following should be: RWIS_LOG.logException(e);
				e.printStackTrace();
			}
		}
	}
		
	/** Start the RwisProcess thread. */
	static public void startProcess() {
		// start the RWIS process thread
		THREAD = new RwisProcess();
		THREAD.setDaemon(true);
		THREAD.start();
	}

	// --- Assorted helper methods...
	
	/** Is a sign an RWIS sign */
	static public boolean isRwisSign(DMS dms) {
		if (dms == null)
			return false;
		ArrayList<WeatherSensor> wsArray = DMSHelper.getAssociatedWeatherSensors(dms);
		if (wsArray.isEmpty())
			return false;
		for (String tag: dms.getHashtags())
			if (tag.startsWith("#RWIS"))
				return true;
		return false;
	}

	/** Get array of RWIS hashtag strings for a
	 *  specific DMS (without leading '#').
	 *  Returns empty array if no RWIS hashtags
	 *  are associated with the sign. */
	static public ArrayList<String> getRwisHashtags(DMS dms) {
		ArrayList<String> tagList = new ArrayList<String>();
		if (dms != null)
			for (String tag: dms.getHashtags())
				if (tag.startsWith("#RWIS"))
					tagList.add(tag.substring(1));
		return tagList;
	}
			
	/** Is a device's commlink enabled?
	 * (With device, controller, & commlink null-checks.) */
	static public boolean commlinkEnabled(Device dev) {
		if (dev == null)
			return false;
		Controller c = dev.getController();
		if (c == null)
			return false;
		CommLink cl = c.getCommLink();
		return ((cl != null) && (cl.getPollEnabled() == true));
	}

	/** Get RWIS DMS objects with enabled commlinks */
	static public Collection<DMS> getEnabledRwisDms() {
		HashMap<String, DMS> dmsMap = new HashMap<String, DMS>();
		Iterator<DMS> it = DMSHelper.iterator();
		DMS dms;
		while (it.hasNext()) {
			dms = it.next();
			if (!commlinkEnabled(dms) || !isRwisSign(dms))
				continue;
			dmsMap.put(dms.getName(), dms);
		}
		return dmsMap.values();
	}

	/** Build hashmap of ESS objects
	 * @param bActiveOnly only include signs with active commlinks */
	static public HashMap<String, WeatherSensor> genEssMap(boolean bActiveOnly) {
		HashMap<String, WeatherSensor> essMap = new HashMap<String, WeatherSensor>();
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		WeatherSensor ess; 
		while (it.hasNext()) {
			ess = it.next();
			if (ess == null)
				continue;
			if (bActiveOnly) {
				//FIXME:  Don't skip if we have test data for this ESS.
				if (commlinkEnabled(ess) == false)
					continue;
			}
			essMap.put(ess.getName(), ess);
		}
		return essMap;
	}

	/** Build hashmap of all ESS */
	static public HashMap<String, WeatherSensor> genEssMap() {
		return genEssMap(true);
	}
	
	/** Is a GeoLoc valid? */
	static public boolean isValid(GeoLoc geo) {
		if (geo == null)
			return false;
		Double lat = geo.getLat();
		Double lon = geo.getLon();
		if ((lat == null) || (lat < -90) || (lat > 90)
		 || (lon == null) || (lon < -180) || (lon > 180))
			return false;
		if ((lat == 0.0) && (lon == 0.0))
			return false; // There's no IRIS hardware on Null Island.
		return true;
	}
}
