/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.IpawsConfig;
import us.mn.state.dot.tms.IpawsDeployer;
import us.mn.state.dot.tms.IpawsDeployerHelper;
import us.mn.state.dot.tms.IteratorWrapper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Integrated Public Alert and Warning System (IPAWS) Alert deployer object
 * server-side implementation.
 *
 * @author Gordon Parikh
 */
public class IpawsDeployerImpl extends BaseObjectImpl implements IpawsDeployer {

	/** Name creator */
	static UniqueNameCreator UNC;
	static {
		UNC = new UniqueNameCreator("ipaws_dplr_%d",
			(n)->lookupIpawsDeployer(n));
		UNC.setMaxLength(20);
	}

	/** Create a unique IpawsDeployer record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}

	/** Time units to use for calculating pre/post alert deployment times.
	 *  This is provided for convenience when testing, where changing to
	 *  TimeUnit.MINUTES will generally allow for easier testing than the
	 *  value of TimeUnit.HOURS that is used in production.
	 */
	static private TimeUnit prePostTimeUnits = TimeUnit.HOURS;

	static public Iterator<IpawsDeployerImpl> iterator() {
		return new IteratorWrapper<IpawsDeployerImpl>(
			namespace.iterator(SONAR_TYPE));
	}

	/** Load all the IPAWS alert deployers */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, IpawsDeployerImpl.class);
		store.query("SELECT name, gen_time, approved_time, alert_id, " +
			"alert_start, alert_end, config, pre_alert_time, " +
			"post_alert_time, auto_dms, optional_dms, " +
			"deployed_dms, auto_multi, deployed_multi, " +
			"msg_priority, approved_by, alert_state, " +
			"was_deployed, replaces FROM event." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			@Override
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new IpawsDeployerImpl(row));
			}
		});
	}

	/** Get columns (names and values) for storing this in the database. */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("gen_time", gen_time);
		map.put("approved_time", approved_time);
		map.put("alert_id", alert_id);
		map.put("alert_start", alert_start);
		map.put("alert_end", alert_end);
		map.put("config", config);
		map.put("pre_alert_time", pre_alert_time);
		map.put("post_alert_time", post_alert_time);
		map.put("auto_dms", arrayToString(auto_dms));
		map.put("optional_dms", arrayToString(optional_dms));
		map.put("deployed_dms", arrayToString(deployed_dms));
		map.put("auto_multi", auto_multi);
		map.put("deployed_multi", deployed_multi);
		map.put("msg_priority", msg_priority);
		map.put("approved_by", approved_by);
		map.put("alert_state", getAlertState());
		map.put("was_deployed", was_deployed);
		map.put("replaces", replaces);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	private IpawsDeployerImpl(ResultSet row) throws SQLException {
		this(row.getString(1),          // name
		     row.getTimestamp(2),       // generated time
		     row.getTimestamp(3),       // approval time
		     row.getString(4),          // alert identifier
		     row.getTimestamp(5),       // alert start
		     row.getTimestamp(6),       // alert end
		     row.getString(7),          // config
		     row.getInt(8),             // pre-alert time
		     row.getInt(9),             // post-alert time
		     getStringArray(row, 10),   // auto DMS list
		     getStringArray(row, 11),   // optional DMS list
		     getStringArray(row, 12),   // deployed DMS list
		     row.getString(13),         // auto MULTI
		     row.getString(14),         // deployed MULTI
		     row.getInt(15),            // message priority
		     row.getString(16),         // approving user
		     row.getInt(17),            // alert_state
		     row.getBoolean(18),        // was_deployed
		     row.getString(19)          // replaces
		);
	}

	private IpawsDeployerImpl(String n, Date gt, Date at, String aid,
		Date as, Date ae, String c, int preh, int posth, String[] adms,
		String[] odms, String[] ddms, String am, String dm, int mp,
		String u, int st, boolean wd, String rep)
	{
		super(n);
		gen_time = gt;
		approved_time = at;
		alert_id = aid;
		alert_start = as;
		alert_end = ae;
		config = lookupIpawsConfig(c);
		pre_alert_time = preh;
		post_alert_time = posth;
		auto_dms = adms;
		optional_dms = odms;
		deployed_dms = ddms;
		auto_multi = am;
		deployed_multi = dm;
		msg_priority = mp;
		approved_by = u;
		alert_state = AlertState.fromOrdinal(st);
		was_deployed = wd;
		replaces = rep;
	}

	public IpawsDeployerImpl(String n, String aid, Date as, Date ae,
		IpawsConfig cfg, String[] adms, String[] ddms, String m, int mp,
		int preh, int posth, String rep)
	{
		super(n);
		alert_id = aid;
		alert_start = as;
		alert_end = ae;
		config = cfg;
		auto_dms = adms;
		deployed_dms = ddms;
		auto_multi = m;
		msg_priority = mp;
		pre_alert_time = preh;
		post_alert_time = posth;
		alert_state = AlertState.PENDING;
		was_deployed = false;
		replaces = rep;
		gen_time = new Date();
	}

	/** Check if the provided values (alert start, alert end, auto-suggested
	 *  DMS, and auto-generated MULTI) are equal to the corresponding values
	 *  contained in this alert deployer.
	 */
	public boolean autoValsEqual(Date aStart, Date aEnd, String[] adms,
		String aMulti, int mp)
	{
		boolean startEq = objectEquals(aStart, alert_start);
		boolean endEq = objectEquals(aEnd, alert_end);
		boolean dmsEq = Arrays.equals(auto_dms, adms);
		boolean multiEq = objectEquals(aMulti, auto_multi);
		boolean mpEq = (msg_priority == mp);
		return startEq && endEq && dmsEq && multiEq && mpEq;
	}

	/** Generation time of alert deployer */
	private Date gen_time;

	/** Get the generation time of this deployer object */
	@Override
	public Date getGenTime() {
		return gen_time;
	}

	/** Approved time of alert deployer */
	private Date approved_time;

	/** Set the approval time of this deployer object */
	@Override
	public void setApprovedTime(Date at) {
		approved_time = at;
	}

	/** Set the approval time of this deployer object */
	public void doSetApprovedTime(Date at) throws TMSException {
		if (!objectEquals(at, approved_time)) {
			store.update(this, "approved_time", at);
			setApprovedTime(at);
		}
	}

	/** Get the approval time of this deployer object */
	@Override
	public Date getApprovedTime() {
		return approved_time;
	}

	/** Identifier of the alert triggering this notification. */
	private String alert_id;

	/** Get the Alert ID */
	@Override
	public String getAlertId() {
		return alert_id;
	}

	/** Start time of alert (stored here for convenience) */
	private Date alert_start;

	/** Get the alert start time */
	@Override
	public Date getAlertStart() {
		return alert_start;
	}

	/** End time of alert (stored here for convenience) */
	private Date alert_end;

	/** Get the alert end time */
	@Override
	public Date getAlertEnd() {
		return alert_end;
	}

	/** IpawsConfig used for this deployment */
	private IpawsConfig config;

	/** Get the config used for this deployment */
	@Override
	public IpawsConfig getConfig() {
		return config;
	}

	/** Amount of time (in hours) to display a pre-alert message before an
	 *  alert becomes active.  First set from the config, then can be
	 *  changed for each alert.
	 */
	private int pre_alert_time;

	/** Set amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active.  First set from the config, then can be
	 *  changed for each alert.
	 */
	@Override
	public void setPreAlertTime(int hours) {
		pre_alert_time = hours;
	}

	/** Set amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active. First set from the config, then can be
	 *  changed for each alert.
	 */
	public void doSetPreAlertTime(int hours) throws TMSException {
		if (hours != pre_alert_time) {
			store.update(this, "pre_alert_time", hours);
			setPreAlertTime(hours);
		}
	}

	/** Get amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active.
	 */
	@Override
	public int getPreAlertTime() {
		return pre_alert_time;
	}

	/** Check if the current time is past the allowed pre alert time given
	 *  the deployer's alert start time. If this returns true, it generally
	 *  means the deployment should be started (assuming it is inactive).
	 */
	public boolean isPastPreAlertTime() {
		return isPastPreAlertTime(alert_start);
	}

	/** Check if the current time is past the allowed pre alert time given
	 *  the time provided (which should be an alert start time).  Using this
	 *  is advised when processing updates to handle changes to an alert's
	 *  onset time.  If this returns true, it generally means the deployment
	 *  should be started (assuming it is active).
	 */
	public boolean isPastPreAlertTime(Date alertStart) {
		Date now = new Date();

		if (now.before(alertStart)) {
			long t = alertStart.getTime() - now.getTime();
			int units = (int) prePostTimeUnits.convert(
				t, TimeUnit.MILLISECONDS);
			return units < pre_alert_time;
		}
		// if after the alert start, we must be past the pre-alert time
		return true;
	}

	/** Amount of time (in hours) to display a post-alert message after an
	 *  alert expires or an AllClear response type is sent via IPAWS. First
	 *  set from the config, then can be changed for each alert.
	 */
	private int post_alert_time;

	/** Set amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS.
	 *  First set from the config, then can be changed for each alert.
	 */
	@Override
	public void setPostAlertTime(int hours) {
		post_alert_time = hours;
	}

	/** Set amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS.
	 */
	public void doSetPostAlertTime(int hours) throws TMSException {
		if (hours != post_alert_time) {
			store.update(this, "post_alert_time", hours);
			setPostAlertTime(hours);
		}
	}

	/** Get amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS.
	 */
	@Override
	public int getPostAlertTime() {
		return post_alert_time;
	}

	/** Check if the current time is past the allowed post alert time given
	 *  the deployer's alert end time. If this returns true, it generally
	 *  means the deployment should be canceled.
	 */
	public boolean isPastPostAlertTime() {
		return isPastPostAlertTime(alert_end);
	}

	/** Check if the current time is past the allowed post alert time given
	 *  the time provided (which should be an alert end time). Using this is
	 *  advised when processing updates to handle changes to an alert's
	 *  expiration time. If this returns true, it generally means the
	 *  deployment should be canceled.
	 */
	public boolean isPastPostAlertTime(Date alertEnd) {
		Date now = new Date();
		if (now.after(alertEnd)) {
			long t = now.getTime() - alertEnd.getTime();
			int units = (int) prePostTimeUnits.convert(
					t, TimeUnit.MILLISECONDS);
			return units >= post_alert_time;
		}
		return false;
	}

	/** List of DMS automatically selected for this alert. */
	private String[] auto_dms;

	/** Get the list of DMS (represented as a string array) automatically
	 *  selected for deploying alert messages.
	 */
	@Override
	public String[] getAutoDms() {
		return auto_dms;
	}

	/** List of DMS suggested automatically as optional DMS that users may
	 *  want to include in the deployment. */
	private String[] optional_dms;

	/** Set the list of DMS suggested automatically by the system as
	 *  optional DMS that users may want to include for the deployment,
	 *  notifying clients. */
	public void setOptionalDmsNotify(String[] dms) throws TMSException {
		if (!Arrays.deepEquals(dms, optional_dms)) {
			store.update(this, "optional_dms", arrayToString(dms));
			optional_dms = dms;
			notifyAttribute("optionalDms");
		}
	}

	/** Get the list of DMS suggested automatically by the system as optional
	 *  DMS that users may want to include for the deployment.
	 */
	@Override
	public String[] getOptionalDms() {
		return optional_dms;
	}

	/** List of DMS actually used to deploy the alert */
	private String[] deployed_dms;

	/** Get the list of DMS actually used to deploy the alert */
	@Override
	public String[] getDeployedDms() {
		return deployed_dms;
	}

	/** Set the list of DMS actually used to deploy the alert */
	private void setDeployedDmsNotify(String[] dms) throws TMSException {
		if (!Arrays.deepEquals(dms, deployed_dms)) {
			store.update(this, "deployed_dms", arrayToString(dms));
			deployed_dms = dms;
			notifyAttribute("deployedDms");
		}
	}

	/** List of DMS requested to deploy the alert */
	private String[] requested_dms;

	/** Set the list of DMS requested to deploy the alert */
	@Override
	public void setRequestedDms(String[] dms) {
		requested_dms = dms;
	}

	/** MULTI generated automatically for to deploying to DMS */
	private String auto_multi;

	/** Get the MULTI generated automatically for deploying to DMS */
	@Override
	public String getAutoMulti() {
		return auto_multi;
	}

	/** MULTI actually deployed to DMS. */
	private String deployed_multi;

	/** Set the MULTI actually deployed to DMS. */
	@Override
	public void setDeployedMulti(String m) {
		deployed_multi = m;
	}

	/** Set the MULTI actually deployed to DMS. */
	public void doSetDeployedMulti(String m) throws TMSException {
		if (!objectEquals(m, deployed_multi)) {
			store.update(this, "deployed_multi", m);
			setDeployedMulti(m);
		}
	}

	/** Set the MULTI actually deployed to DMS, notifying clients. */
	public void setDeployedMultiNotify(String m) throws TMSException {
		if (!objectEquals(m, deployed_multi)) {
			doSetDeployedMulti(m);
			notifyAttribute("deployedMulti");
		}
	}

	/** Get the MULTI actually deployed to DMS. */
	@Override
	public String getDeployedMulti() {
		return deployed_multi;
	}

	/** Message priority calculated from alert fields (can be overridden by
	 *  user).
	 */
	private int msg_priority;

	/** Set the message priority.
	 * @param mp Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	@Override
	public void setMsgPriority(int mp) {
		msg_priority = mp;
	}

	/** Set the message priority.
	 * @param mp Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	public void doSetMsgPriority(int mp) throws TMSException {
		if (mp != msg_priority) {
			store.update(this, "msg_priority", mp);
			setMsgPriority(mp);
		}
	}

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	@Override
	public int getMsgPriorty() {
		return msg_priority;
	}

	/** User that approved the alert message posting (may be null or "AUTO"). */
	private String approved_by;

	/** Set the approving user. */
	@Override
	public void setApprovedBy(String u) {
		approved_by = u;
	}

	/** Set the approving user. */
	public void doSetApprovedBy(String u) throws TMSException {
		if (!objectEquals(u, approved_by)) {
			store.update(this, "approved_by", u);
			setApprovedBy(u);
		}
	}

	/** Get the approving user. */
	@Override
	public String getApprovedBy() {
		return approved_by;
	}

	/** Alert state */
	private AlertState alert_state;

	/** Get alert state (ordinal of AlertState) */
	@Override
	public int getAlertState() {
		AlertState st = alert_state;
		return (st != null)
		      ? st.ordinal()
		      : AlertState.PENDING.ordinal();
	}

	/** Set alert state (ordinal of AlertState) */
	public void setAlertStateNotify(int st) throws TMSException {
		if (st != getAlertState()) {
			store.update(this, "alert_state", st);
			alert_state = AlertState.fromOrdinal(st);
			notifyAttribute("alertState");
		}
	}

	/** Set alert state (ordinal of AlertState) */
	@Override
	public void setAlertStateReq(int st) {
		// handled by doSetAlertStateReq
	}

	/** Set alert state (ordinal of AlertState) */
	public void doSetAlertStateReq(int st) throws TMSException {
		AlertState req = AlertState.fromOrdinal(st);
		switch (req) {
			case APPROVE_REQ:
				setAlertStateNotify(AlertState.APPROVED
					.ordinal());
				return;
			case CANCEL_REQ:
				cancelAlert();
				setAlertStateNotify(AlertState.INACTIVE
					.ordinal());
				return;
		}
		throw new ChangeVetoException("Invalid alert state request: " +
			req);
	}

	/** Check the alert against the pre-/post-alert times and either repost
	 *  or cancel the alert as needed. */
	public boolean checkStateChange() throws TMSException {
		boolean pastPre = isPastPreAlertTime();
		boolean pastPost = isPastPostAlertTime();
		if (pastPre && !pastPost) {
			deployAlert();
			return true;
		} else if (pastPre && pastPost) {
			// if past post-alert time but we were told to deploy,
			// we need to cancel
			cancelAlert();
			return false;
		} else if (!pastPre) {
			// if before the pre-alert time, we're waiting
			int millis = (int) TimeUnit.MILLISECONDS.convert(
				pre_alert_time, prePostTimeUnits);
			Date start = new Date(alert_start.getTime() - millis);
			IpawsProcJob.log("Waiting until " + start +
				" for alert to start...");
			return true;
		} else
			return false;
	}

	/** Whether this alert deployer was ever deployed or not. */
	private boolean was_deployed;

	/** Set whether this alert deployer was ever deployed or not.  Note that
	 *  this will be true if an alert message is successfully sent to at
	 *  least one sign. */
	public void setWasDeployed(boolean wd) throws TMSException {
		if (wd != was_deployed) {
			store.update(this, "was_deployed", wd);
			was_deployed = wd;
		}
	}

	/** Alert deployer that this replaces (if any). Note that updates to
	 *  alerts trigger creation of a new deployer (not an update).
	 */
	private String replaces;

	/** Get the alert deployer that this replaces (if any). */
	@Override
	public String getReplaces() {
		return replaces;
	}

	/** Calculate the duration of a message based on the current time and
	 *  the alert start/end time.  */
	private int calculateMsgDuration() {
		if (alert_start != null && alert_end != null) {
			// check the time of the alert relative to now
			Date now = new Date();
			long dm = -1;
			if (now.before(alert_start)) {
				// use time between now and start
				dm = alert_start.getTime() - now.getTime();
			} else if (now.after(alert_start) && now.before(alert_end)) {
				// use time between now and end
				dm = alert_end.getTime() - now.getTime();
			} else if (now.after(alert_end)) {
				// use time between now and end + post_alert_time
				long postMillis = TimeUnit.MILLISECONDS.convert(
						post_alert_time, prePostTimeUnits);
				Date msgEnd = new Date(alert_end.getTime() + postMillis);
				dm = msgEnd.getTime() - now.getTime();
			}
			if (dm != -1)
				return (int) dm / 1000;
		}
		// if alert is in past or duration could not be calculated
		return -1;
	}

	/** Deploy an alert with all the parameters we have in this deployer.
	 *  If the alert is already deployed, the deployment is updated to
	 *  reflect any changes that have been made. */
	private void deployAlert() throws TMSException {
		if (deployed_multi == null || deployed_multi.isEmpty()) {
			// deployed MULTI not set yet - use auto
			setDeployedMultiNotify(auto_multi);
		}
		DmsMsgPriority mp = DmsMsgPriority.fromOrdinal(msg_priority);
		int duration = calculateMsgDuration();
		if (deployed_multi != null && !deployed_multi.isEmpty()
			&& mp != DmsMsgPriority.INVALID && duration != -1)
		{
			deployAlert(mp, duration);
		}
	}

	/** Go through all the DMS and deploy */
	private void deployAlert(DmsMsgPriority mp, int duration)
		throws TMSException
	{
		// if this deployer is replacing another one that is still
		// deployed, make sure to cancel that one
		if (replaces != null) {
			IpawsDeployerImpl old = lookupIpawsDeployer(replaces);
			if (old != null && old.getAlertState() ==
			    AlertState.DEPLOYED.ordinal())
			{
				IpawsProcJob.log("Canceling old deployer " +
					old.getName() + " for alert " +
					old.getAlertId());
				old.cancelAlert();
				old.setAlertStateNotify(AlertState.INACTIVE
					.ordinal());
			}
		}

		cancelAlert();

		// use the auto DMS if we don't have requested DMS
		if (requested_dms == null || requested_dms.length == 0)
			setRequestedDms(auto_dms);

		IpawsProcJob.log("Deploying alert " + alert_id +
			" with message " + deployed_multi + ", priority " +
			mp.toString() + ", and duration " + duration + " to " +
			requested_dms.length + " DMS from deployer " + name);

		boolean wd = false;
		for (String dmsName: requested_dms) {
			DMSImpl dms = lookupDMS(dmsName);
			// if the alert makes it to one sign,
			// record that it was deployed
			wd |= (dms != null) && dms.sendIpawsMsg(deployed_multi,
				msg_priority, duration);
		}
		if (wd)
			setWasDeployed(wd);
		setAlertStateNotify(AlertState.DEPLOYED.ordinal());
	}

	/** Cancel an alert on the deployed list of DMS */
	private void cancelAlert() throws TMSException {
		String[] dmsList = deployed_dms;
		if (dmsList != null && dmsList.length > 0) {
			IpawsProcJob.log("Canceling " + name + " on alert " +
				alert_id + " for " + dmsList.length + " DMS");
			for (String dmsName: dmsList) {
				DMSImpl dms = lookupDMS(dmsName);
				if (dms != null)
					dms.blankIpawsMsg();
			}
		}
	}
}
