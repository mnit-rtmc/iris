/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.IpawsAlertDeployer;
import us.mn.state.dot.tms.IpawsAlertDeployerHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.UniqueNameCreator;;

/**
 * Integrated Public Alert and Warning System (IPAWS) Alert deployer object
 * server-side implementation.
 *
 * @author Gordon Parikh
 */
public class IpawsAlertDeployerImpl extends BaseObjectImpl
	implements IpawsAlertDeployer {
	
	/** Database table name */
	static private final String TABLE = "event.ipaws_alert_deployer";
	
	/** Name creator */
	static UniqueNameCreator UNC;
	static {
		UNC = new UniqueNameCreator("ipaws_dplr_%d",
				(n)->lookupIpawsAlertDeployer(n));
		UNC.setMaxLength(20);
	}
	
	/** Create a unique IpawsAlertDeployer record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}
	
	/** Time units to use for calculating pre/post alert deployment times.
	 *  This is provided for convenience when testing, where changing to
	 *  TimeUnit.MINUTES will generally allow for easier testing than the
	 *  value of TimeUnit.HOURS that is used in production.
	 */
	static private TimeUnit prePostTimeUnits = TimeUnit.HOURS;
	
	/** Lookup an IpawsAlertDeployerImpl given and IpawsAlert identifier/name
	 *  and an IpawsAlertConfig name. Returns the most recent active deployer
	 *  for the alert with a matching config.
	 */
	static public IpawsAlertDeployerImpl lookupFromAlert(
			String name, String cname) {
		// ask the helper to find the IpawsAlertDeployer object for this alert
		IpawsAlertDeployer ian =
				IpawsAlertDeployerHelper.lookupDeployerFromAlert(name, cname);
		if (ian != null)
			// lookup the IpawsAlertDeployerImpl object using the name
			return lookupIpawsAlertDeployer(ian.getName());
		return null;
	}
	
	/** Load all the IPAWS alert deployers */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, IpawsAlertDeployerImpl.class);
		store.query("SELECT name, gen_time, approved_time, alert_id, " + 
			"geo_loc, alert_start, alert_end, config, sign_group, " +
			"quick_message, pre_alert_time, post_alert_time, auto_dms, " + 
			"optional_dms, deployed_dms, area_threshold, auto_multi, " +
			"deployed_multi, msg_priority, approved_by, deployed, " +
			"was_deployed, active, replaces FROM event." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			@Override
			public void create(ResultSet row) throws Exception {
				try {
					namespace.addObject(new IpawsAlertDeployerImpl(row));
				} catch (Exception e) {
					IpawsProcJob.log("Error adding: " + row.getString(1));
					e.printStackTrace();
				}
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
		map.put("geo_loc", geo_loc);
		map.put("alert_start", alert_start);
		map.put("alert_end", alert_end);
		map.put("config", config);
		map.put("sign_group", sign_group);
		map.put("quick_message", quick_message);
		map.put("pre_alert_time", pre_alert_time);
		map.put("post_alert_time", post_alert_time);
		map.put("auto_dms", arrayToString(auto_dms));
		map.put("optional_dms", arrayToString(optional_dms));
		map.put("deployed_dms", arrayToString(deployed_dms));
		map.put("auto_multi", auto_multi);
		map.put("deployed_multi", deployed_multi);
		map.put("msg_priority", msg_priority);
		map.put("approved_by", approved_by);
		map.put("deployed", deployed);
		map.put("was_deployed", was_deployed);
		map.put("active", active);
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
	
	private IpawsAlertDeployerImpl(ResultSet row) throws SQLException {
		this(row.getString(1),			// name
			row.getTimestamp(2),		// generated time
			row.getTimestamp(3),		// approval time
			row.getString(4),			// alert identifier
			row.getString(5),			// geo_loc
			row.getTimestamp(6),		// alert start
			row.getTimestamp(7),		// alert end
			row.getString(8),			// config
			row.getString(9),			// sign group
			row.getString(10),			// quick message
			row.getInt(11),				// pre-alert time
			row.getInt(12),				// post-alert time
			getStringArray(row, 13),	// auto DMS list
			getStringArray(row, 14),	// optional DMS list
			getStringArray(row, 15),	// deployed DMS list
			row.getDouble(16),			// area threshold
			row.getString(17),			// auto MULTI
			row.getString(18),			// deployed MULTI
			row.getInt(19),				// message priority
			row.getString(20),			// approving user
			row.getBoolean(21),			// deployed
			row.getBoolean(22),			// was_deployed
			row.getBoolean(23),			// active
			row.getString(24)			// replaces
		 );
	}
	
	public IpawsAlertDeployerImpl(String n, String aid)  {
		super(n);
		alert_id = aid;
		gen_time = new Date();
	}
	
	public IpawsAlertDeployerImpl(String n, String aid, String[] dms) {
		super(n);
		alert_id = aid;
		auto_dms = dms;
		gen_time = new Date();
	}
	
	public IpawsAlertDeployerImpl(String n, String aid, String[] dms,
			String m, String u) {
		super(n);
		alert_id = aid;
		auto_dms = dms;
		auto_multi = m;
		approved_by = u;
		gen_time = new Date();
	}
	
	public IpawsAlertDeployerImpl(String n, String aid, GeoLoc gl,
			Date as, Date ae, String c, String sg, String[] adms,
			String[] ddms, String qm, String m, int mp, int preh,
			int posth, String r) {
		super(n);
		alert_id = aid;
		geo_loc = gl;
		alert_start = as;
		alert_end = ae;
		config = c;
		sign_group = sg;
		auto_dms = adms;
		deployed_dms = ddms;
		quick_message = qm;
		auto_multi = m;
		msg_priority = mp;
		pre_alert_time = preh;
		post_alert_time = posth;
		replaces = r;
		gen_time = new Date();
	}
	
	public IpawsAlertDeployerImpl(String n, Date gt, Date at, String aid,
			String gl, Date as, Date ae, String c, String sg, String qm,
			int preh, int posth, String[] adms, String[] odms, String[] ddms,
			Double t, String am, String dm, int mp, String u, Boolean d,
			Boolean wd, boolean a, String r) {
		super(n);
		gen_time = gt;
		approved_time = at;
		alert_id = aid;
		geo_loc = lookupGeoLoc(gl);
		alert_start = as;
		alert_end = ae;
		config = c;
		sign_group = sg;
		quick_message = qm;
		pre_alert_time = preh;
		post_alert_time = posth;
		auto_dms = adms;
		optional_dms = odms;
		deployed_dms = ddms;
		auto_multi = am;
		deployed_multi = dm;
		msg_priority = mp;
		approved_by = u;
		deployed = d;
		was_deployed = wd;
		active = a;
		replaces = r;
	}

	/** Check if the provided values (alert start, alert end, auto-suggested
	 *  DMS, and auto-generated MULTI) are equal to the corresponding values
	 *  contained in this alert deployer.
	 */
	public boolean autoValsEqual(Date aStart, Date aEnd,
			String[] adms, String aMulti, int mp) {
		boolean startEq = objectEquals(aStart, alert_start);
		boolean endEq = objectEquals(aEnd, alert_end);
		boolean dmsEq = Arrays.equals(auto_dms, adms);
		boolean multiEq = objectEquals(aMulti, auto_multi);
		boolean mpEq = msg_priority == mp;
		return startEq && endEq && dmsEq && multiEq && mpEq;
	}
	
	/** Generation time of alert deployer */
	private Date gen_time;
	
	/** Set the generation time of this deployer object */
	@Override
	public void setGenTime(Date gt) {
		gen_time = gt;
	}

	/** Set the generation time of this deployer object */
	public void doSetGenTime(Date gt) throws TMSException {
		if (!objectEquals(gt, gen_time)) {
			store.update(this, "gen_time", gt);
			setGenTime(gt);
		}
	}
	
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
	
	/** Set the Alert ID. */
	@Override
	public void setAlertId(String aid) {
		alert_id = aid;
	}
	
	/** Set the Alert ID. */
	public void doSetAlertId(String aid) throws TMSException {
		if (!objectEquals(aid, alert_id)) {
			store.update(this, "alert_id", aid);
			setAlertId(aid);
		}
	}
	
	/** Get the Alert ID. */
	@Override
	public String getAlertId() {
		return alert_id;
	}
	
	/** GeoLoc of this deployer */
	private GeoLoc geo_loc;
	
	/** Set the deployer GeoLoc. */
	@Override
	public void setGeoLoc(GeoLoc gl) {
		geo_loc = gl;
	}
	
	/** Set the Alert ID. */
	public void doSetGeoLoc(GeoLoc gl) throws TMSException {
		if (!objectEquals(gl, geo_loc)) {
			store.update(this, "geo_loc", gl);
			setGeoLoc(gl);
		}
	}
	
	/** Get the deployer GeoLoc. */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}
	
	/** Start time of alert (stored here for convenience) */
	private Date alert_start;
	
	/** Set the alert start time */
	@Override
	public void setAlertStart(Date t) {
		alert_start = t;
	}
	
	/** Set the alert start time */
	public void doSetAlertStart(Date t) throws TMSException {
		if (!objectEquals(t, alert_start)) {
			store.update(this, "alert_start", t);
			setAlertStart(t);
		}
	}
	
	/** Get the alert start time */
	@Override
	public Date getAlertStart() {
		return alert_start;
	}
	
	/** End time of alert (stored here for convenience) */
	private Date alert_end;
	
	/** Set the alert end time */
	@Override
	public void setAlertEnd(Date t) {
		alert_end = t;
	}

	/** Set the alert end time */
	public void doSetAlertEnd(Date t) throws TMSException {
		if (!objectEquals(t, alert_end)) {
			store.update(this, "alert_end", t);
			setAlertEnd(t);
		}
	}
	
	/** Get the alert end time */
	@Override
	public Date getAlertEnd() {
		return alert_end;
	}
	
	/** IpawsAlertConfig used for this deployment */
	private String config;
	
	/** Set the config used for this deployment */
	@Override
	public void setConfig(String c) {
		config = c;
	}

	/** Set the config used for this deployment */
	public void doSetConfig(String c) throws TMSException {
		if (!objectEquals(c, config)) {
			store.update(this, "config", c);
			setSignGroup(c);
		}
	}
	
	/** Get the config used for this deployment */
	@Override
	public String getConfig() {
		return config;
	}
	
	/** Sign group used for this deployment */
	private String sign_group;
	
	/** Set the sign group used for this deployment */
	@Override
	public void setSignGroup(String sg) {
		sign_group = sg;
	}
	
	/** Set the sign group used for this deployment */
	public void doSetSignGroup(String sg) throws TMSException {
		if (!objectEquals(sg, sign_group)) {
			store.update(this, "sign_group", sg);
			setSignGroup(sg);
		}
	}
	
	/** Get the sign group used for this deployment */
	@Override
	public String getSignGroup() {
		return sign_group;
	}

	/** Quick message used for this deployment */
	private String quick_message;
	
	/** Set the quick message (template) used for this deployment */
	@Override
	public void setQuickMessage(String qm) {
		quick_message = qm;
	}

	/** Set the quick message (template) used for this deployment */
	public void doSetQuickMessage(String qm) throws TMSException {
		if (!objectEquals(qm, quick_message)) {
			store.update(this, "quick_message", qm);
			setQuickMessage(qm);
		}
	}
	
	/** Get the quick message (template) used for this deployment */
	@Override
	public String getQuickMessage() {
		return quick_message;
	}
	
	/** Amount of time (in hours) to display a pre-alert message before an
	 *  alert becomes active. First set from the config, then can be
	 *  changed for each alert.
	 */
	private int pre_alert_time;
	
	/** Set amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active. First set from the config, then can be
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
	 *  the time provided (which should be an alert start time). Using this is
	 *  advised when processing updates to handle changes to an alert's
	 *  onset time. If this returns true, it generally means the deployment
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
	
	/** Check if the current time is past the pre-alert time given t  */
	
	/** Amount of time (in hours) to display a post-alert message after an
	 *  alert expires or an AllClear response type is sent via IPAWS. First
	 *  set from the config, then can be changed for each alert.
	 */
	private int post_alert_time;

	/** Set amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS. First
	 *  set from the config, then can be changed for each alert.
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
	
	/** Set the list of DMS (represented as a string array) automatically 
	 *  selected for deploying alert messages.
	 */
	@Override
	public void setAutoDms(String[] dms) {
		auto_dms = dms;
	}
	
	/** Set the list of DMS (represented as a string array) automatically 
	 *  selected for deploying alert messages.
	 */
	public void doSetAutoDms(String[] dms) throws TMSException {
		if (!Arrays.deepEquals(dms, this.auto_dms)) {
			store.update(this, "auto_dms", arrayToString(dms));
			setAutoDms(dms);
		}
	}
	
	/** Set the list of DMS (represented as a string array) automatically 
	 *  selected for deploying alert messages, notifying clients if it has
	 *  changed.
	 */
	public void setAutoDmsNotify(String[] dms) throws TMSException {
		if (!Arrays.deepEquals(dms, this.auto_dms)) {
			doSetAutoDms(dms);
			notifyAttribute("autoDms");
		}
	}
	
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
	
	/** Set the list of DMS suggested automatically by the system as optional 
	 *  DMS that users may want to include for the deployment.
	 */
	@Override
	public void setOptionalDms(String[] dms) {
		optional_dms = dms;
	}

	/** Set the list of DMS suggested automatically by the system as optional 
	 *  DMS that users may want to include for the deployment.
	 */
	public void doSetOptionalDms(String[] dms) throws TMSException {
		if (!Arrays.deepEquals(dms, this.optional_dms)) {
			store.update(this, "optional_dms", arrayToString(dms));
			setOptionalDms(dms);
		}
	}
	
	/** Set the list of DMS suggested automatically by the system as optional 
	 *  DMS that users may want to include for the deployment, notifying
	 *  clients.
	 */
	public void setOptionalDmsNotify(String[] dms) throws TMSException {
		if (!Arrays.deepEquals(dms, this.optional_dms)) {
			doSetOptionalDms(dms);
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

	/** List of DMS actually used to deploy the message. */
	private String[] deployed_dms;
	
	/** List of DMS we used to deploy the last time we deployed (for canceling
	 *  after updates). 
	 */
	private String[] lastDeployedDms;
	
	/** Set the list of DMS actually used to deploy the message. */
	@Override
	public void setDeployedDms(String[] dms) {
		deployed_dms = dms;
	}

	/** Set the list of DMS actually used to deploy the message. */
	public void doSetDeployedDms(String[] dms) throws TMSException {
		if (!Arrays.deepEquals(dms, this.deployed_dms)) {
			store.update(this, "deployed_dms", arrayToString(dms));
			lastDeployedDms = deployed_dms;
			setDeployedDms(dms);
		}
	}

	/** Set the list of DMS actually used to deploy the message, notifying
	 *  clients.
	 */
	public void setDeployedDmsNotify(String[] dms) throws TMSException {
		if (!Arrays.deepEquals(dms, this.deployed_dms)) {
			doSetDeployedDms(dms);
			notifyAttribute("deployedDms");
		}
	}
	
	/** Get the list of DMS actually used to deploy the message. */
	@Override
	public String[] getDeployedDms() {
		return deployed_dms;
	}

	/** Area threshold used for including DMS outside the alert area. */
	private Double alert_threshold;
	
	/** Set area threshold used for including DMS outside the alert area. */
	@Override
	public void setAreaThreshold(Double t) {
		alert_threshold = t;
	}

	/** Set area threshold used for including DMS outside the alert area. */
	public void doSetAreaThreshold(Double t) throws TMSException {
		if (!objectEquals(t, alert_threshold)) {
			store.update(this, "alert_threshold", t);
			setAreaThreshold(t);
		}
	}
	
	/** Get area threshold used for including DMS outside the alert area. */
	@Override
	public Double getAreaThreshold() {
		return alert_threshold;
	}

	/** MULTI generated automatically by the system for to deploying to DMS. */
	private String auto_multi;
	
	/** Set the MULTI generated automatically by the system for deploying to
	 *  DMS.
	 */
	@Override
	public void setAutoMulti(String m) {
		auto_multi = m;
	}
	
	/** Set the MULTI generated automatically by the system for deploying to
	 *  DMS.
	 */
	public void doSetAutoMulti(String m) throws TMSException {
		if (objectEquals(m, auto_multi)) {
			store.update(this, "auto_multi", m);
			setAutoMulti(m);
		}
	}

	/** Set the MULTI generated automatically by the system for deploying to
	 *  DMS, notifying clients if it has changed.
	 */
	public void setAutoMultiNotify(String m) throws TMSException {
		if (!objectEquals(m, auto_multi)) {
			doSetAutoMulti(m);
			notifyAttribute("autoMulti");
		}
	}
	
	/** Get the MULTI generated automatically by the system for deploying to
	 *  DMS.
	 */
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
	
	/** Set the message priority */
	@Override
	public void setMsgPriority(Integer p) {
		msg_priority = p;
	}
	
	/** Set the message priority  */
	public void doSetMsgPriority(Integer p) throws TMSException {
		if (!objectEquals(p, msg_priority)) {
			store.update(this, "msg_priority", p);
			setMsgPriority(p);
		}
	}
	
	/** Get the message priority */
	@Override
	public Integer getMsgPriorty() {
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

	/** Deployed state of this alert (whether it was ever deployed). */
	private Boolean deployed;
	
	/** Set the deployed state of this alert (whether it was ever deployed). */
	@Override
	public void setDeployed(Boolean d) {
		deployed = d;
	}

	/** Set the deployed state of this alert (whether it is currently
	 *  deployed). This triggers changes in the alert deployment (i.e.
	 *  deploying/updating a deployment or canceling).
	 */
	public void doSetDeployed(Boolean d) throws TMSException {
		if (d != null) {	// don't allow setting back to null
			IpawsProcJob.log("Setting deployed on deployer " +
						name + " to " + d);
			if (Boolean.TRUE.equals(d))
				// call with flag if it's already deployed (then it's an
				// update)
				d = checkDeployCancel(Boolean.TRUE.equals(deployed));
			else
				cancelAlert();
			
			store.update(this, "deployed", d);
			setDeployed(d);
		}
	}
	
	/** Set the deployed state of the alert without triggering any changes
	 *  in the actual state of the deployment.
	 */
	public void doSetDeployedSilent(Boolean d) throws TMSException {
		if (d != null) {
			store.update(this, "deployed", d);
			setDeployed(d);
		}
	}
	
	/** Check the alert against the pre-/post-alert times and either repost
	 *  or cancel the alert as needed. Returns false if the alert was canceled
	 *  and true otherwise.
	 */
	public boolean checkDeployCancel(boolean update) throws TMSException {
		// check if we are after the pre-alert time and before the
		// post-alert time
		boolean pastPre = isPastPreAlertTime();
		boolean pastPost = isPastPostAlertTime();
		if (pastPre && !pastPost)
			deployAlert(update);
		else if (pastPre && pastPost) {
			// if past post-alert time but we were told to deploy, we
			// need to cancel
			cancelAlert();
			return false;
		} else if (!pastPre) {
			// if before the pre-alert time, we're waiting
			int millis = (int) TimeUnit.MILLISECONDS.convert(
					pre_alert_time, prePostTimeUnits);
			Date start = new Date(alert_start.getTime() - millis);
			IpawsProcJob.log("Waiting until " + start +
					" for alert to start...");
		}
		return true;
	}
	
	/** Set the deployed state of this alert (whether it was ever deployed),
	 *  notifying clients if it has changed.
	 */
	public void setDeployedNotify(Boolean d) throws TMSException {
		Boolean dlast = deployed;
		doSetDeployed(d);
		
		// NOTE we do this test here because doSetDeployed does stuff...
		if (!objectEquals(dlast, d))
			notifyAttribute("deployed");
	}
	
	/** Get the deployed state of this alert (whether it was ever deployed). */
	@Override
	public Boolean getDeployed() {
		return deployed;
	}
	
	/** Whether this alert deployer was ever deployed or not. */
	private boolean was_deployed;
	
	/** Set whether this alert deployer was ever deployed or not. */
	@Override
	public void setWasDeployed(boolean wd) {
		was_deployed = wd;
	}

	/** Set whether this alert deployer was ever deployed or not. */
	public void doSetWasDeployed(boolean wd) throws TMSException {
		if (wd != was_deployed) {
			store.update(this, "was_deployed", wd);
			setWasDeployed(wd);
		}
	}
	
	/** Set whether this alert deployer was ever deployed or not, notifying
	 *  clients if it has changed.
	 */
	public void setWasDeployedNotify(boolean wd) throws TMSException {
		if (wd != was_deployed) {
			doSetWasDeployed(wd);
			notifyAttribute("wasDeployed");
		}
	}
	
	/** Get whether this alert deployer was ever deployed or not. */
	@Override
	public boolean getWasDeployed() {
		return was_deployed;
	}
	
	/** Whether this alert deployer is currently active (i.e. visible on DMS).
	 *  Alerts can be deployed but not active if they have been approved for
	 *  deployment but the pre-alert time value indicates that they should not
	 *  be deployed yet.
	 */
	private boolean active;
	
	/** Set whether this alert deployment is currently active (i.e. visible on
	 *  DMS). Alerts can be deployed but not active if they have been approved
	 *  for deployment but the pre-alert time value indicates that they should
	 *  not be deployed yet.
	 */
	@Override
	public void setActive(boolean a) {
		active = a;
	}
	
	/** Set whether this alert deployment is currently active (i.e. visible on
	 *  DMS). Alerts can be deployed but not active if they have been approved
	 *  for deployment but the pre-alert time value indicates that they should
	 *  not be deployed yet.
	 */
	public void doSetActive(boolean a) throws TMSException {
		if (a != active) {
			store.update(this, "active", a);
			setActive(a);
		}
	}
	
	/** Set whether this alert deployment is currently active (i.e. visible on
	 *  DMS), notifying clients if it has changed.
	 */
	public void setActiveNotify(boolean a) throws TMSException {
		doSetActive(a);
		notifyAttribute("active");
	}
	
	/** Get whether this alert deployment is currently active (i.e. visible on
	 *  DMS). Alerts can be deployed but not active if they have been approved
	 *  for deployment but the pre-alert time value indicates that they should
	 *  not be deployed yet.
	 */
	@Override
	public boolean getActive() {
		return active;
	}
	
	/** Alert deployer that this replaces (if any). Note that updates to
	 *  alerts trigger creation of a new deployer (not an update).
	 */
	private String replaces;
	
	/** Get the alert deployer that this replaces (if any). Note that updates
	 *  to alerts trigger creation of a new deployer (not an update).
	 */
	@Override
	public void setReplaces(String r) {
		replaces = r;
	}

	/** Get the alert deployer that this replaces (if any). Note that updates
	 *  to alerts trigger creation of a new deployer (not an update).
	 */
	public void doSetReplaces(String r) throws TMSException {
		if (!objectEquals(r, replaces)) {
			store.update(this, "replaces", r);
			setReplaces(r);
		}
	}
	
	/** Set the alert deployer that this replaces (if any). */
	@Override
	public String getReplaces() {
		return replaces;
	}
	
	/** Calculate the duration of a message based on the current time and the
	 *  alert start/end time.
	 */
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
	
	/** Deploy an alert with all the parameters we have in this deployer. If
	 *  the alert is already deployed, the deployment is updated to reflect
	 *  any changes that have been made.
	 */
	private void deployAlert(boolean update) throws TMSException {
		// get the MULTI, priority, and duration for the alert
		if (deployed_multi == null || deployed_multi.isEmpty()) {
			// deployed MULTI not set yet - use auto
			setDeployedMultiNotify(auto_multi);
		}
		DmsMsgPriority mp = DmsMsgPriority.fromOrdinal(msg_priority);
		int duration = calculateMsgDuration();
		if (deployed_multi != null && !deployed_multi.isEmpty()
				&& mp != DmsMsgPriority.INVALID && duration != -1) {
			// if everything is OK, go through all the DMS and deploy
			
			// if this deployer is replacing another one that is still
			// deployed, make sure to cancel that one
			if (replaces != null) {
				IpawsAlertDeployerImpl old =
						lookupIpawsAlertDeployer(replaces);
				if (old != null && Boolean.TRUE.equals(old.getDeployed())) {
					IpawsProcJob.log("Canceling old deployer " +
						old.getName() + " for alert " + old.getAlertId());
					old.setDeployedNotify(false);
				}
			}
			
			// if this is an update, cancel the previous deployment and repost
			if (update)
				cancelAlert(lastDeployedDms);
			
			// get the list of DMS to use
			if (deployed_dms == null || deployed_dms.length == 0)
				// use the auto DMS if we don't have deployed DMS
				setDeployedDmsNotify(auto_dms);
			
			IpawsProcJob.log("Deploying alert " + alert_id +
				" with message " + deployed_multi + ", priority " +
				mp.toString() + ", and duration " + duration + " to " +
				deployed_dms.length + " DMS from deployer " + name + " ...");
			
			boolean wd = false;
			for (String dmsName: deployed_dms) {
				DMSImpl dms = lookupDMS(dmsName);
				if (dms != null) {
					try {
						if (dms.sendIpawsMsg(deployed_multi,
								msg_priority, duration))
							// if the alert makes it to one sign, record that
							// it was deployed
							wd = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// record that the alert was deployed and is currently active
			if (wd != was_deployed)
				setWasDeployedNotify(wd);
			if (wd != active)
				setActiveNotify(wd);
		}
	}
	
	/** Cancel an alert with all the parameters we have in this deployer. */
	private void cancelAlert() throws TMSException {
		// if we don't have deployed DMS, nothing should have been deployed
		if (deployed_dms != null && deployed_dms.length > 0)
			cancelAlert(deployed_dms);
	}
	
	/** Cancel an alert on the specified list of DMS. */
	private void cancelAlert(String[] dmsList) throws TMSException {
		if (dmsList != null && dmsList.length > 0) {
			IpawsProcJob.log("Canceling deployment " + name
					+ " of alert " + alert_id + " on " + dmsList.length +
					" DMS...");
			for (String dmsName: dmsList) {
				DMSImpl dms = lookupDMS(dmsName);
				if (dms != null) {
					// blank the message on this sign (if it's owned by the 
					// "IPAWS" user)
					// if another one should take it's place, it will be
					// posted by that alert deployer
					try {
						dms.blankIpawsMsg();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// record that the alert is now inactive
			setActiveNotify(false);
		}
	}
}
