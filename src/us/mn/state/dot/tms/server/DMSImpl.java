/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2023  Minnesota Department of Transportation
 * Copyright (C) 2008-2009  AHMCT, University of California
 * Copyright (C) 2012-2021  Iteris Inc.
 * Copyright (C) 2016-2020  SRF Consulting Group
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

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconState;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.SignDetailHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.event.BrightnessSample;
import us.mn.state.dot.tms.server.event.PriceMessageEvent;
import us.mn.state.dot.tms.server.event.SignEvent;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Dynamic Message Sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author John L. Stanley - SRF Consulting
 */
public class DMSImpl extends DeviceImpl implements DMS, Comparable<DMSImpl> {

	/** DMS / hashtag mapping */
	static private TagMapping mapping;

	/** Test if a sign message is from a specified source */
	static private boolean isMsgSource(SignMessage sm, SignMsgSource src) {
		int bits = SignMessageHelper.sourceBits(sm);
		return src.checkBit(bits);
	}

	/** Comm fail time threshold to blank user message */
	static private final long COMM_FAIL_BLANK_THRESHOLD_MS = 5 * 60 * 1000;

	/** Minimum duration of a DMS action (minutes) */
	static private final int DURATION_MINIMUM_MINS = 1;

	/** Number of polling periods for DMS action duration */
	static private final int DURATION_PERIODS = 3;

	/** Get the expiration time of a sign message (at activation time). */
	static private Long getExpireTime(SignMessage sm) {
		if (SignMessageHelper.isOperatorExpiring(sm)) {
			long dur_ms = sm.getDuration() * 60 * 1000;
			long now = TimeSteward.currentTimeMillis();
			return now + dur_ms;
		} else
			return null;
	}

	/** Interface for handling brightness samples */
	static public interface BrightnessHandler {
		void feedback(EventType et, int photo, int output);
	}

	/** Load all the DMS */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DMSImpl.class);
		mapping = new TagMapping(store, "iris", SONAR_TYPE,
			"hashtag");
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"gps, static_graphic, beacon, preset, sign_config, " +
			"sign_detail, msg_user, msg_sched, msg_current, " +
			"expire_time, status, stuck_pixels FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DMSImpl(row));
			}
		});
	}

	/** Update all DMS item styles */
	static void updateAllStyles() {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS d = it.next();
			if (d instanceof DMSImpl) {
				DMSImpl dms = (DMSImpl) d;
				dms.updateStyles();
			}
		}
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("gps", gps);
		map.put("static_graphic", static_graphic);
		map.put("beacon", beacon);
		map.put("preset", preset);
		map.put("sign_config", sign_config);
		map.put("sign_detail", sign_detail);
		map.put("msg_user", msg_user);
		map.put("msg_sched", msg_sched);
		map.put("msg_current", msg_current);
		map.put("expire_time", asTimestamp(expire_time));
		map.put("status", status);
		map.put("stuck_pixels", stuck_pixels);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Compare to another DMS */
	@Override
	public int compareTo(DMSImpl o) {
		return name.compareTo(o.name);
	}

	/** Create a new DMS with a string name */
	public DMSImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
		expire_time = null;
		status = null;
		stuck_pixels = null;
	}

	/** Create a dynamic message sign */
	private DMSImpl(ResultSet row) throws Exception {
		this(row.getString(1),     // name
		     row.getString(2),     // geo_loc
		     row.getString(3),     // controller
		     row.getInt(4),        // pin
		     row.getString(5),     // notes
		     row.getString(6),     // gps
		     row.getString(7),     // static_graphic
		     row.getString(8),     // beacon
		     row.getString(9),     // preset
		     row.getString(10),    // sign_config
		     row.getString(11),    // sign_detail
		     row.getString(12),    // msg_user
		     row.getString(13),    // msg_sched
		     row.getString(14),    // msg_current
		     row.getTimestamp(15), // expire_time
		     row.getString(16),    // status
		     row.getString(17)     // stuck_pixels
		);
	}

	/** Create a dynamic message sign */
	private DMSImpl(String n, String loc, String c, int p, String nt,
		String g, String sg, String b, String cp, String sc,
		String sd, String mu, String ms, String mc, Date et,
		String st, String sp) throws TMSException
	{
		super(n, lookupController(c), p, nt);
		geo_loc = lookupGeoLoc(loc);
		gps = lookupGps(g);
		static_graphic = lookupGraphic(sg);
		beacon = lookupBeacon(b);
		setPreset(lookupPreset(cp));
		sign_config = SignConfigHelper.lookup(sc);
		sign_detail = SignDetailHelper.lookup(sd);
		msg_user = SignMessageHelper.lookup(mu);
		msg_sched = SignMessageHelper.lookup(ms);
		msg_current = SignMessageHelper.lookup(mc);
		expire_time = stampMillis(et);
		status = st;
		stuck_pixels = sp;
		hashtags = lookupHashtagMapping();
		initTransients();
	}

	/** Lookup mapping of hashtags */
	private String[] lookupHashtagMapping() throws TMSException {
		TreeSet<String> ht_set = new TreeSet<String>();
		for (String ht: mapping.lookup(this))
			ht_set.add(ht);
		return ht_set.toArray(new String[0]);
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		setPreset(null);
		geo_loc.notifyRemove();
	}

	/** Set the controller to which this DMS is assigned */
	@Override
	public void setController(Controller c) {
		super.setController(c);
		if (c != null)
			setConfigure(false);
	}

	/** Request to query configuration of the DMS */
	public void requestConfigure() {
		if (!configure)
			sendDeviceRequest(DeviceRequest.QUERY_CONFIGURATION);
	}

	/** Configure flag indicates that the sign has been configured */
	private boolean configure;

	/** Set the configure flag.
	 *  @param c Set to true to indicate the DMS is configured. */
	public void setConfigure(boolean c) {
		configure = c;
		// Since this is called after every failed operation, check if
		// communication has been failed too long.  If so, clear the
		// user message to prevent it from popping up days later, after
		// communication is restored.
		if (!c && getFailMillis() >= COMM_FAIL_BLANK_THRESHOLD_MS)
			setMsgUserNotify(null);
	}

	/** Get the configure flag.
	 *  @return True to indicate the DMS is configured else false. */
	public boolean getConfigure() {
		return configure;
	}

	/** Device location */
	private GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Associated GPS */
	private GpsImpl gps;

	/** Set associated GPS */
	@Override
	public void setGps(Gps g) {
		if (g instanceof GpsImpl)
			gps = (GpsImpl) g;
	}

	/** Set associated GPS */
	public void doSetGps(Gps g) throws TMSException {
		if (g != gps) {
			store.update(this, "gps", g);
			setGps(g);
		}
	}

	/** Get associated GPS */
	@Override
	public Gps getGps() {
		return gps;
	}

	/** Static graphic (hybrid sign) */
	private Graphic static_graphic;

	/** Set static graphic (hybrid sign) */
	@Override
	public void setStaticGraphic(Graphic sg) {
		static_graphic = sg;
	}

	/** Set static graphic (hybrid sign) */
	public void doSetStaticGraphic(Graphic sg) throws TMSException {
		if (sg != static_graphic) {
			store.update(this, "static_graphic", sg);
			setStaticGraphic(sg);
		}
	}

	/** Get static graphic (hybrid sign) */
	@Override
	public Graphic getStaticGraphic() {
		return static_graphic;
	}

	/** Hashtags for the DMS */
	private String[] hashtags = new String[0];

	/** Set the hashtags assigned to the DMS */
	@Override
	public void setHashtags(String[] ht) {
		hashtags = ht;
	}

	/** Set the hashtags assigned to the DMS */
	public synchronized void doSetHashtags(String[] ht)
		throws TMSException
	{
		String[] ht2 = DMSHelper.makeHashtags(ht);
		if (!Arrays.equals(ht, ht2))
			throw new ChangeVetoException("BAD HASHTAGS");
		if (!Arrays.equals(ht, hashtags)) {
			TreeSet<String> ht_set = new TreeSet<String>(
				Arrays.asList(ht)
			);
			mapping.update(this, ht_set);
			setHashtags(ht);
			updateStyles();
		}
	}

	/** Add a hashtag to the DMS */
	public synchronized void addHashtagNotify(String aht) {
		aht = DMSHelper.normalizeHashtag(aht);
		if (aht == null)
			return;
		TreeSet<String> ht_set = new TreeSet<String>(
			Arrays.asList(hashtags)
		);
		if (ht_set.add(aht)) {
			try {
				mapping.update(this, ht_set);
				hashtags = ht_set.toArray(new String[0]);
				notifyAttribute("hashtags");
				updateStyles();
			}
			catch (TMSException e) {
				logError("hashtags map: " + e.getMessage());
			}
		}
	}

	/** Get the hashtags assigned to the DMS */
	@Override
	public String[] getHashtags() {
		return hashtags;
	}

	/** Remote beacon */
	private Beacon beacon;

	/** Set remote beacon */
	@Override
	public void setBeacon(Beacon b) {
		beacon = b;
	}

	/** Set remote beacon */
	public void doSetBeacon(Beacon b) throws TMSException {
		if (b != beacon) {
			store.update(this, "beacon", b);
			setBeacon(b);
		}
	}

	/** Get remote beacon */
	@Override
	public Beacon getBeacon() {
		return beacon;
	}

	/** Update remote beacon */
	private void updateBeacon() {
		Beacon b = beacon;
		if (b != null) {
			boolean f = isOnline() && isMsgBeacon();
			BeaconState bs = (f)
				? BeaconState.FLASHING_REQ
				: BeaconState.DARK_REQ;
			b.setState(bs.ordinal());
		}
	}

	/** Camera preset from which this can be seen */
	private CameraPreset preset;

	/** Set the verification camera preset */
	@Override
	public void setPreset(CameraPreset cp) {
		final CameraPreset ocp = preset;
		if (cp instanceof CameraPresetImpl) {
			CameraPresetImpl cpi = (CameraPresetImpl) cp;
			cpi.setAssignedNotify(true);
		}
		preset = cp;
		if (ocp instanceof CameraPresetImpl) {
			CameraPresetImpl ocpi = (CameraPresetImpl) ocp;
			ocpi.setAssignedNotify(false);
		}
	}

	/** Set the verification camera preset */
	public void doSetPreset(CameraPreset cp) throws TMSException {
		if (cp != preset) {
			store.update(this, "preset", cp);
			setPreset(cp);
		}
	}

	/** Get verification camera preset */
	@Override
	public CameraPreset getPreset() {
		return preset;
	}

	/** Sign configuration */
	private SignConfig sign_config;

	/** Get the sign configuration */
	@Override
	public SignConfig getSignConfig() {
		return sign_config;
	}

	/** Set the sign config */
	public void setSignConfigNotify(SignConfigImpl sc) {
		if (!objectEquals(sc, sign_config)) {
			try {
				store.update(this, "sign_config", sc);
			}
			catch (TMSException e) {
				logError("sign_config: " + e.getMessage());
				return;
			}
			sign_config = sc;
			notifyAttribute("signConfig");
			resetStateNotify();
			updateStyles();
		}
	}

	/** Sign detail */
	private SignDetail sign_detail;

	/** Get the sign detail */
	@Override
	public SignDetail getSignDetail() {
		return sign_detail;
	}

	/** Set the sign detail */
	public void setSignDetailNotify(SignDetailImpl sd) {
		if (!objectEquals(sd, sign_detail)) {
			try {
				store.update(this, "sign_detail", sd);
			}
			catch (TMSException e) {
				logError("sign_detail: " + e.getMessage());
				return;
			}
			sign_detail = sd;
			notifyAttribute("signDetail");
		}
	}

	/** Reset sign state (and notify clients) */
	public void resetStateNotify() {
		setStatusNotify(null);
		setStuckPixelsNotify(null);
		setMsgUserNotify(null);
		setMsgSchedNotify(null);
		setMsgCurrentNotify(null);
	}

	/** Create a blank message for the sign */
	public SignMessage createMsgBlank(int src) {
		src |= SignMsgSource.blank.bit();
		String owner = SignMessageHelper.makeMsgOwner(src);
		SignMsgPriority mp = SignMsgPriority.low_1;
		return SignMessageImpl.findOrCreate(sign_config, null, "",
			owner, false, mp, null);
	}

	/** Create a message for the sign.
	 * @param ms MULTI string for message.
	 * @param owner Message owner.
	 * @param fb Flash beacon flag.
	 * @param mp Message priority.
	 * @param dur Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	public SignMessage createMsg(String ms, String owner, boolean fb,
		SignMsgPriority mp, Integer dur)
	{
		return SignMessageImpl.findOrCreate(sign_config, null, ms,
			owner, fb, mp, dur);
	}

	/** Create a scheduled message.
	 * @param amsg DMS action message.
	 * @return New sign message, or null on error. */
	private SignMessage createMsgSched(DmsActionMsg amsg) {
		assert (amsg != null);
		DmsAction da = amsg.action;
		String ms = amsg.getMulti();
		int src = amsg.getSources();
		String owner = SignMessageHelper.makeMsgOwner(src,
			da.getActionPlan().getName());
		MsgPattern pat = da.getMsgPattern();
		boolean fb = (pat != null) && pat.getFlashBeacon();
		SignMsgPriority mp = SignMsgPriority.fromOrdinal(
			da.getMsgPriority());
		Integer dur = getDuration(da);
		return SignMessageImpl.findOrCreate(sign_config, null, ms,
			owner, fb, mp, dur);
	}

	/** Get the duration of a DMS action.
	 * @param da DMS action.
	 * @return Duration (minutes), or null for indefinite. */
	private Integer getDuration(DmsAction da) {
		return da.getActionPlan().getSticky()
		     ? null
		     : getUnstickyDurationMins();
	}

	/** Get the duration of an unsticky action */
	private int getUnstickyDurationMins() {
		return Math.max(DURATION_MINIMUM_MINS, getDurationMins());
	}

	/** Get the duration of a DMS action */
	private int getDurationMins() {
		return getPollPeriodSec() * DURATION_PERIODS / 60;
	}

	/** User selected sign message.
	 *
	 * This is cached to allow combining with scheduled messages in
	 * getMsgCombined().
	 *
	 * A null value indicates that the user message is unknown. */
	private SignMessage msg_user;

	/** Get the user sign messasge.
	 * @return User sign message */
	@Override
	public SignMessage getMsgUser() {
		return msg_user;
	}

	/** Set the user selected sign message */
	@Override
	public void setMsgUser(SignMessage sm) {
		msg_user = sm;
	}

	/** Set the user selected sign message */
	public void doSetMsgUser(SignMessage sm) throws TMSException {
		if (!objectEquals(msg_user, sm)) {
			String unm = SignMessageHelper.getMsgOwnerName(sm);
			String pusr = getProcUser();
			if (!unm.equals(pusr)) {
				throw new ChangeVetoException("USER: " +
					unm + " != " + pusr);
			}
			checkMsgUser(sm);
			validateMsg(sm);
			// only retain non-blank user messages
			SignMessage smu = SignMessageHelper.isBlank(sm)
				? null
				: sm;
			store.update(this, "msg_user", smu);
			setMsgUser(smu);
			sm = getMsgValidated();
			sendMsg(sm);
		}
	}

	/** Check if the user has permission to send a given message */
	private void checkMsgUser(SignMessage sm) throws TMSException {
		switch (queryPermAccess()) {
		case 2: // "Operate" access level
			denyFreeForm(sm);
			return;
		case 3: // "Manage" access level
			checkFreeFormBanned(sm);
			return;
		case 4: // "Configure" access level
			// not checked
			return;
		default:
			throw new ChangeVetoException("NOT PERMITTED");
		}
	}

	/** Deny free-form text in a message */
	private void denyFreeForm(SignMessage sm) throws TMSException {
		if (SignMessageHelper.isBlank(sm))
			return;
		String msg = DMSHelper.validateFreeFormLines(this,
			sm.getMulti());
		if (msg != null)
			throw new ChangeVetoException(msg);
	}

	/** Check for banned words in free-form text */
	private void checkFreeFormBanned(SignMessage sm) throws TMSException {
		if (SignMessageHelper.isBlank(sm))
			return;
		String msg = DMSHelper.validateFreeFormWords(this,
			sm.getMulti());
		if (msg != null)
			throw new ChangeVetoException(msg);
	}

	/** Set the user selected sign message,
	 *  without validating or sending anything to the sign. */
	public void setMsgUserNotify(SignMessage sm) {
		if (!objectEquals(msg_user, sm)) {
			try {
				store.update(this, "msg_user", sm);
				setMsgUser(sm);
				notifyAttribute("msgUser");
			}
			catch (TMSException e) {
				logError("msg_user: " + e.getMessage());
			}
		}
	}

	/** Scheduled sign message */
	private SignMessage msg_sched;

	/** Get the scheduled sign messasge.
	 * @return Scheduled sign message */
	@Override
	public SignMessage getMsgSched() {
		return msg_sched;
	}

	/** Set the scheduled DMS action message */
	public void setActionMsg(DmsActionMsg amsg) {
		SignMessage sm = (amsg != null) ? createMsgSched(amsg) : null;
		setPrices(amsg);
		if (setMsgSchedNotify(sm))
			updateSchedMsg();
	}

	/** Set the scheduled sign message.
	 * @param sm New scheduled sign message.
	 * @return true If scheduled message changed. */
	private boolean setMsgSchedNotify(SignMessage sm) {
		if (!objectEquals(msg_sched, sm)) {
			try {
				store.update(this, "msg_sched", sm);
				msg_sched = sm;
				notifyAttribute("msgSched");
				return true;
			}
			catch (TMSException e) {
				logError("msg_sched: " + e.getMessage());
				return false;
			}
		} else
			return false;
	}

	/** Update scheduled message.
	 *
	 * This updates the message's expire time, so it should be called on
	 * each polling period.  Also, if a scheduled message has just expired,
	 * it will be replaced by the user message. */
	private void updateSchedMsg() {
		if (isMsgScheduled() || msg_sched != null) {
			SignMessage usm = getMsgValidated();
			if (isMsgSource(usm, SignMsgSource.schedule) ||
			    isMsgScheduled())
			{
				sendMsgLogError(usm);
			}
		}
	}

	/** Tolling prices */
	private transient ArrayList<PriceMessageEvent> prices;

	/** Set tolling prices */
	private void setPrices(DmsActionMsg amsg) {
		prices = (amsg != null) ? amsg.getPrices() : null;
	}

	/** Log price (tolling) messages.
	 * @param et Event type. */
	private void logPriceMessages(EventType et) {
		ArrayList<PriceMessageEvent> p = prices;
		if (p != null) {
			for (PriceMessageEvent ev : p) {
				logEvent(ev.withEventType(et));
			}
		}
	}

	/** Current message */
	private SignMessage msg_current;

	/** Set the current message */
	private void setMsgCurrent(SignMessage sm) {
		try {
			store.update(this, "msg_current", sm);
			msg_current = sm;
		}
		catch (TMSException e) {
			logError("msg_current: " + e.getMessage());
		}
	}

	/** Set the current message.
	 * @param sm Sign message. */
	public void setMsgCurrentNotify(SignMessage sm) {
		if (isMsgSource(sm, SignMsgSource.tolling))
			logPriceMessages(EventType.PRICE_VERIFIED);
		if (sm != msg_current) {
			logMsg(sm);
			setMsgCurrent(sm);
			notifyAttribute("msgCurrent");
			updateExpireTime(sm);
			updateStyles();
		}
		updateBeacon();
		// If current msg is blank, check if a scheduled msg should be
		// sent.  This is needed for comm links with long polling
		// periods, otherwise the scheduled msg will not display until
		// the next poll.  This really shouldn't be needed at all, but
		// some DMS will randomly blank themselves for unknown reasons.
		if (isMsgBlank())
			updateSchedMsg();
	}

	/** Get the current messasge.
	 * @return Currently active message */
	@Override
	public SignMessage getMsgCurrent() {
		return msg_current;
	}

	/** Log a message.
	 * @param sm Sign message. */
	private void logMsg(SignMessage sm) {
		EventType et = EventType.DMS_DEPLOYED;
		String text = (sm != null) ? sm.getMulti() : null;
		String owner = (sm != null)
			? sm.getMsgOwner()
			: SignMessageHelper.makeMsgOwner(
				SignMsgSource.reset.bit());
		Integer duration = (sm != null) ? sm.getDuration() : null;
		if (SignMessageHelper.isBlank(sm)) {
			et = EventType.DMS_CLEARED;
			text = null;
			duration = null;
		}
		logEvent(new SignEvent(et, name, text, owner, duration));
	}

	/** Next sign message (sending in process) */
	private transient SignMessage msg_next;

	/** Set the next sign message.  This must be called by operations after
	 * getting exclusive device ownership.  It must be set back to null
	 * after the operation completes.  This is necessary to prevent the
	 * ReaperJob from destroying a SignMessage before it has been sent to
	 * a sign.
	 * @see us.mn.state.dot.tms.server.DeviceImpl#acquire */
	public void setMsgNext(SignMessage sm) {
		msg_next = sm;
	}

	/** Get validated user/scheduled sign message.
	 * @return Validated sign message. */
	private SignMessage getMsgValidated() {
		SignMessage sm = getMsgCombined();
		if (sm != null) {
			try {
				validateMsg(sm);
				return sm;
			}
			catch (InvalidMsgException e) {
				// message can't be displayed,
				// most likely due to stuck pixels
			}
		}
		// no message, or invalid -- blank the sign
		return createMsgBlank(0);
	}

	/** Validate a sign message */
	private void validateMsg(SignMessage sm) throws InvalidMsgException {
		try {
			SignMessageHelper.validate(sm, this);
		}
		catch (InvalidMsgException e) {
			logEvent(new SignEvent(
				e.getEventType(),
				name,
				sm.getMulti(),
				sm.getMsgOwner(),
				sm.getDuration()
			));
			throw e;
		}
	}

	/** Get combined user / scheduled sign message.
	 * @return The appropriate sign message, or null. */
	private SignMessage getMsgCombined() {
		SignMessage sched = msg_sched;	// Avoid race
		SignMessage user = msg_user;	// Avoid race
		SignMessage combined = tryCombine(sched, user);
		if (combined != null)
			return combined;
		else
			return checkPriority(sched, user) ? sched : user;
	}

	/** Try to make a combined sched/user message */
	private SignMessage tryCombine(SignMessage sched, SignMessage user) {
		if (null == sched || null == user)
			return null;
		RasterBuilder rb = DMSHelper.createRasterBuilder(this);
		String ms = rb.combineMulti(sched.getMulti(), user.getMulti());
		if (rb.isRasterizable(ms)) {
			SignMessage sm = createMsgCombined(sched, user, ms);
			if (sm != null) {
				// Check whether combined message can be
				// displayed without too many pixel errors
				try {
					SignMessageHelper.validate(sm, this);
					return sm;
				}
				catch (InvalidMsgException e) {
					// too many pixel errors
				}
			}
		}
		return null;
	}

	/** Create a combined sign message */
	private SignMessage createMsgCombined(SignMessage sched,
		SignMessage user, String ms)
	{
		String inc = user.getIncident();
		boolean fb = user.getFlashBeacon();
		SignMsgPriority mp = SignMsgPriority.fromOrdinal(
			user.getMsgPriority());
		// combine user and scheduled message sources
		int src = SignMsgSource.fromString(
				SignMessageHelper.getMsgOwnerSources(user)
			) |
			SignMsgSource.fromString(
				SignMessageHelper.getMsgOwnerSources(sched)
			);
		String unm = SignMessageHelper.getMsgOwnerName(user);
		String owner = SignMessageHelper.makeMsgOwner(src, unm);
		Integer dur = user.getDuration();
		return SignMessageImpl.findOrCreate(sign_config, inc, ms,
			owner, fb, mp, dur);
	}

	/** Compare sign messages for higher priority */
	private boolean checkPriority(SignMessage sm1, SignMessage sm2) {
		return (sm2 == null) ||
		       (sm1 != null &&
		        sm1.getMsgPriority() > sm2.getMsgPriority());
	}

	/** Send message to DMS, logging any exceptions.
	 * @param sm Sign message (not null). */
	private void sendMsgLogError(SignMessage sm) {
		try {
			sendMsg(sm);
		}
		catch (TMSException e) {
			logError("sendMsgLogError: " + e.getMessage());
		}
	}

	/** Send message to DMS.
	 * @param sm Sign message (not null). */
	private void sendMsg(SignMessage sm) throws TMSException {
		DMSPoller p = getDMSPoller();
		if (null == p) {
			throw new ChangeVetoException(name +
				": NO ACTIVE POLLER");
		}
		if (sm != null)
			sendMsg(p, sm);
	}

	/** Set the next sign message.
	 * @param p DMS poller.
	 * @param sm Sign message (not null). */
	private void sendMsg(DMSPoller p, SignMessage sm) {
		if (isMsgSource(sm, SignMsgSource.tolling))
		    logPriceMessages(EventType.PRICE_DEPLOYED);
		p.sendMessage(this, sm);
	}

	/** Check if the sign has a reference to a sign message */
	public boolean hasReference(final SignMessage sm) {
		return sm == msg_user ||
		       sm == msg_sched ||
		       sm == msg_current ||
		       sm == msg_next;
	}

	/** Current message expiration time */
	private Long expire_time;

	/** Update the current message expiration time */
	private void updateExpireTime(SignMessage sm) {
		Long et = getExpireTime(sm);
		try {
			setExpireTimeNotify(et);
		}
		catch (TMSException e) {
			logError("updateExpireTime: " + e.getMessage());
		}
	}

	/** Set the current message expiration time */
	private void setExpireTimeNotify(Long et) throws TMSException {
		if (!objectEquals(et, expire_time)) {
			store.update(this, "expire_time", asTimestamp(et));
			expire_time = et;
			notifyAttribute("expireTime");
		}
	}

	/** Get current message expiration time.
	 * @return Expiration time for the current message (ms since epoch), or
	 *         null for no expiration.
	 * @see java.lang.System#currentTimeMillis */
	@Override
	public Long getExpireTime() {
		return expire_time;
	}

	/** Current (JSON) sign status */
	private String status;

	/** Set the current sign status as JSON */
	public void setStatusNotify(String st) {
		if (!objectEquals(st, status)) {
			try {
				store.update(this, "status", st);
				status = st;
				notifyAttribute("status");
			}
			catch (TMSException e) {
				logError("status: " + e.getMessage());
			}
		}
	}

	/** Set a status value and notify clients of the change */
	public void setStatusNotify(String key, Object value) {
		String s = status;
		try {
			JSONObject jo = (s != null)
				? new JSONObject(s)
				: new JSONObject();
			jo.put(key, value);
			setStatusNotify(jo.toString());
		}
		catch (JSONException e) {
			// malformed JSON
			e.printStackTrace();
		}
	}

	/** Get the current status as JSON */
	@Override
	public String getStatus() {
		return status;
	}

	/** Stuck pixel bitmaps as JSON.  There are two Base64-encoded bitmaps
	 * as attributes STUCK_ON_BITMAP and STUCK_OFF_BITMAP. */
	private String stuck_pixels;

	/** Set the stuck pixel JSON and notify clients */
	public void setStuckPixelsNotify(String sp) {
		if (!objectEquals(sp, stuck_pixels)) {
			try {
				store.update(this, "stuck_pixels", sp);
				stuck_pixels = sp;
				notifyAttribute("stuckPixels");
			}
			catch (TMSException e) {
				logError("stuck_pixels: " + e.getMessage());
			}
		}
	}

	/** Get the stuck pixels as JSON */
	@Override
	public String getStuckPixels() {
		return stuck_pixels;
	}

	/** Feedback brightness sample data */
	public void feedbackBrightness(EventType et, int photo, int output) {
		logBrightnessSample(new BrightnessSample(et, this, photo,
			output));
	}

	/** Log a brightness sample */
	private void logBrightnessSample(final BrightnessSample bs) {
		FLUSH.addJob(new Job() {
			public void perform() throws TMSException {
				bs.purgeConflicting();
				bs.doStore();
			}
		});
	}

	/** Lookup recent brightness feedback sample data */
	public void queryBrightnessFeedback(BrightnessHandler bh) {
		try {
			BrightnessSample.lookup(this, bh);
		}
		catch (TMSException e) {
			logError("brightness feedback: " + e.getMessage());
		}
	}

	/** Test if DMS is available */
	@Override
	protected boolean isAvailable() {
		return super.isAvailable()
		    && (isMsgBlank() || isMsgStandby())
		    && DMSHelper.isGeneralPurpose(this);
	}

	/** Test if current message is blank */
	public boolean isMsgBlank() {
		return SignMessageHelper.isBlank(msg_current);
	}

	/** Test if current message is standby */
	private boolean isMsgStandby() {
		return isMsgSource(getMsgCurrent(), SignMsgSource.standby);
	}

	/** Test if the current message source contains "operator" */
	private boolean isMsgOperator() {
		return isMsgSource(getMsgCurrent(), SignMsgSource.operator);
	}

	/** Test if the current message source contains "scheduled" */
	private boolean isMsgScheduled() {
		return isMsgSource(getMsgCurrent(), SignMsgSource.schedule);
	}

	/** Test if the current message source contains "external" */
	private boolean isMsgExternal() {
		return isMsgSource(getMsgCurrent(), SignMsgSource.external);
	}

	/** Test if the current message source contains "alert" or "rwis" */
	private boolean isMsgAlert() {
		return isMsgSource(getMsgCurrent(), SignMsgSource.alert)
		    || isMsgSource(getMsgCurrent(), SignMsgSource.rwis);
	}

	/** Test if the current message has beacon flashing */
	private boolean isMsgBeacon() {
		SignMessage sm = getMsgCurrent();
		return (sm != null) && sm.getFlashBeacon();
	}

	/** Test if a DMS is active, not failed and deployed */
	public boolean isMsgDeployed() {
		return isOnline() && !isMsgBlank();
	}

	/** Test if a DMS has been deployed by a user */
	public boolean isUserDeployed() {
		return isMsgDeployed()
		    && isMsgOperator()
		    && !isMsgStandby();
	}

	/** Test if a DMS has been deployed by schedule */
	public boolean isScheduleDeployed() {
		return isMsgDeployed()
		    && isMsgScheduled()
		    && !isMsgStandby();
	}

	/** Test if a DMS has been deployed by an external system */
	private boolean isExternalDeployed() {
		return isMsgDeployed() && isMsgExternal();
	}

	/** Test if a DMS has been deployed by alert system */
	private boolean isAlertDeployed() {
		return isMsgDeployed() && isMsgAlert();
	}

	/** Test if DMS needs maintenance */
	@Override
	protected boolean needsMaintenance() {
		return super.needsMaintenance() || hasCriticalError();
	}

	/** Test if DMS has a critical error */
	private boolean hasCriticalError() {
		return !DMSHelper.getCriticalError(this).isEmpty();
	}

	/** Calculate the item styles */
	@Override
	protected long calculateStyles() {
		long s = ItemStyle.ALL.bit();
		if (getController() == null)
			s |= ItemStyle.NO_CONTROLLER.bit();
		if (isActive())
			s |= ItemStyle.ACTIVE.bit();
		else
			s |= ItemStyle.INACTIVE.bit();
		if (DMSHelper.isHidden(this))
			s |= ItemStyle.HIDDEN.bit();
		else {
			if (isAvailable())
				s |= ItemStyle.AVAILABLE.bit();
			if (isUserDeployed())
				s |= ItemStyle.DEPLOYED.bit();
		}
		if (isScheduleDeployed())
			s |= ItemStyle.SCHEDULED.bit();
		if (isExternalDeployed() || isAlertDeployed())
			s |= ItemStyle.EXTERNAL.bit();
		if (isOnline() && needsMaintenance())
			s |= ItemStyle.MAINTENANCE.bit();
		if (isActive() && isFailed())
			s |= ItemStyle.FAILED.bit();
		if (isActive() && !DMSHelper.isGeneralPurpose(this))
			s |= ItemStyle.PURPOSE.bit();
		return s;
	}

	/** Write DMS as an XML element */
	public void writeXml(Writer w) throws IOException {
		w.write("<dms");
		w.write(createAttribute("name", getName()));
		w.write(createAttribute("description",
			GeoLocHelper.getLocation(geo_loc)));
		Position pos = GeoLocHelper.getWgs84Position(geo_loc);
		if (pos != null) {
			w.write(createAttribute("lon",
				formatDouble(pos.getLongitude())));
			w.write(createAttribute("lat",
				formatDouble(pos.getLatitude())));
		}
		SignConfig sc = sign_config;
		if (sc != null) {
			w.write(createAttribute("width_pixels",
				sc.getPixelWidth()));
			w.write(createAttribute("height_pixels",
				sc.getPixelHeight()));
		}
		w.write("/>\n");
	}

	/** Write the sign message as xml */
	public void writeSignMessageXml(Writer w) throws IOException {
		SignMessage sm = getMsgCurrent();
		if (sm instanceof SignMessageImpl)
			((SignMessageImpl) sm).writeXml(w, this);
	}

	/** Get the DMS poller */
	private DMSPoller getDMSPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof DMSPoller) ? (DMSPoller) dp : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		if (DeviceRequest.RESET_DEVICE == dr)
			resetStateNotify();
		DMSPoller p = getDMSPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll(boolean is_long) {
		if (is_long)
			sendDeviceRequest(DeviceRequest.QUERY_STATUS);
		else {
			sendDeviceRequest(DeviceRequest.QUERY_MESSAGE);
			checkMsgExpiration();
			updateSchedMsg();
			LCSArrayImpl la = lookupLCSArray();
			if (la != null)
				la.periodicPoll(is_long);
		}
	}

	/** Check if current sign message has expired */
	private void checkMsgExpiration() {
		Long et = expire_time;
		if (et != null) {
			long now = TimeSteward.currentTimeMillis();
			if (now >= et) {
				try {
					int src = SignMsgSource.expired.bit();
					doSetMsgUser(createMsgBlank(src));
				}
				catch (TMSException e) {
					logError("checkMsgExpiration: " +
						e.getMessage());
				}
			}
		}
	}

	/** Lookup LCS array if this DMS is lane one */
	private LCSArrayImpl lookupLCSArray() {
		LCS lcs = LCSHelper.lookup(name);
		if (lcs != null && lcs.getLane() == 1) {
			LCSArray la = lcs.getArray();
			if (la instanceof LCSArrayImpl)
				return (LCSArrayImpl) la;
		}
		return null;
	}
}
