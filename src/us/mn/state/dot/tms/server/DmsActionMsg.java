/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2020  Minnesota Department of Transportation
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DmsAction;
import static us.mn.state.dot.tms.DmsMsgPriority.GATE_ARM;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.ParkingAreaHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.SignTextHelper;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.TollZoneHelper;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;
import us.mn.state.dot.tms.server.event.PriceMessageEvent;
import us.mn.state.dot.tms.server.event.TravelTimeEvent;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.MINUTES;
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.MPH;
import static us.mn.state.dot.tms.utils.Multi.OverLimitMode;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A DMS action message parses custom action tags, which are similar to MULTI,
 * but processed before sending to the sign.
 *
 * @author Douglas Lau
 */
public class DmsActionMsg {

	/** Empty text span */
	static private final String EMPTY_SPAN = "";

	/** Check if msg feed verify is enabled */
	static private boolean isMsgFeedVerifyEnabled() {
		return SystemAttrEnum.MSG_FEED_VERIFY.getBoolean();
	}

	/** Get the minimum speed to display for advisory */
	static private int getMinAdvisory() {
		return SystemAttrEnum.VSA_MIN_DISPLAY_MPH.getInt();
	}

	/** Get the maximum speed to display for advisory */
	static private int getMaxAdvisory() {
		return SystemAttrEnum.VSA_MAX_DISPLAY_MPH.getInt();
	}

	/** Round value to the nearest 5 */
	static private int round5(float v) {
		return Math.round(v / 5) * 5;
	}

	/** Round value up to the next 5 */
	static private int roundUp5(int v) {
		return ((v - 1) / 5 + 1) * 5;
	}

	/** Calculate the maximum trip minutes to display */
	static private int maximumTripMinutes(Distance d) {
		Speed min_trip = new Speed(
			SystemAttrEnum.TRAVEL_TIME_MIN_MPH.getInt(), MPH);
		Interval e_trip = min_trip.elapsed(d);
		return e_trip.round(MINUTES);
	}

	/** Log an event */
	static private void logEvent(EventType et, String d) {
		final TravelTimeEvent ev = new TravelTimeEvent(et, d);
		FLUSH.addJob(new Job() {
			public void perform() throws TMSException {
				ev.doStore();
			}
		});
	}

	/** Travel time data (for hashmap) */
	static private class TravelTime {
		private final Route route;
		private final OverLimitMode mode;
		private final String o_txt;
		private final int min;
		private final int min_final;
		private final int slow;
		private TravelTime(Route r, OverLimitMode m, String ot, int mn,
			int mn_final, int sl)
		{
			route = r;
			mode = m;
			o_txt = ot;
			min = mn;
			min_final = mn_final;
			slow = sl;
		}
	}

	/** DMS action */
	public final DmsAction action;

	/** DMS for message formatting */
	private final DMSImpl dms;

	/** DMS location */
	private final GeoLoc loc;

	/** Schedule debug log */
	private final DebugLog dlog;

	/** MULTI string after processing DMS action tags */
	private final String multi;

	/** Valid message flag */
	private boolean valid;

	/** DMS message source flags */
	private int src;

	/** Add a message source flag */
	private void addSrc(SignMsgSource s) {
		src |= s.bit();
	}

	/** Get the source flag bits */
	public int getSrc() {
		return src;
	}

	/** Mapping of station IDs to travel times */
	private final HashMap<String, TravelTime> travel =
		new HashMap<String, TravelTime>();

	/** Feed message */
	private String feed_msg;

	/** Tolling prices */
	private final ArrayList<PriceMessageEvent> prices =
		new ArrayList<PriceMessageEvent>();

	/** Create a price message */
	private PriceMessageEvent createPriceMessage(String zid, String det,
		float price)
	{
		return new PriceMessageEvent(EventType.PRICE_DEPLOYED,
			dms.getName(), zid, det, price);
	}

	/** Get tolling prices */
	public ArrayList<PriceMessageEvent> getPrices() {
		return (valid && prices.size() > 0) ? prices : null;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return action.toString() + " on " + dms;
	}

	/** Check if the message is valid */
	public boolean isValid() {
		return valid && (multi != null);
	}

	/** Get the MULTI string */
	public String getMulti() {
		return (multi != null)
		      ? DMSHelper.adjustMulti(dms, multi)
		      : null;
	}

	/** Fail parsing message */
	private String fail(String msg) {
		valid = false;
		if (dlog.isOpen()) {
			dlog.log(toString() + " [fail]: " + msg +
				" (" + getActionMulti() + ")");
		}
		return EMPTY_SPAN;
	}

	/** Create a new DMS action message */
	public DmsActionMsg(DmsAction da, DMSImpl d, DebugLog l) {
		action = da;
		dms = d;
		loc = d.getGeoLoc();
		dlog = l;
		valid = true;
		multi = processAction();
		if (valid && dlog.isOpen()) {
			dlog.log(toString() + " [ok]: " + multi +
				" (" + getActionMulti() + ")");
		}
	}

	/** Get the MULTI string for the DMS action */
	private String getActionMulti() {
		QuickMessage qm = action.getQuickMessage();
		return (qm != null) ? qm.getMulti().trim() : "";
	}

	/** Process a DMS action */
	private String processAction() {
		String ms = getActionMulti();
		return (ms.length() > 0) ? process(ms) : null;
	}

	/** MULTI string builder for parsing DMS action tags */
	private final MultiBuilder builder = new MultiBuilder() {
		@Override public void addTravelTime(String sid,
			OverLimitMode mode, String o_txt)
		{
			processTravelTime(sid, mode, o_txt);
			// Add tag for processTravelTimes to replace
			super.addTravelTime(sid, null, null);
		}
		@Override public void addSpeedAdvisory() {
			addSpan(speedAdvisorySpan());
		}
		@Override public void addSlowWarning(int spd, int dist,
			String mode)
		{
			addSpan(slowWarningSpan(spd, dist, mode));
		}
		@Override public void addFeed(String fid) {
			parseFeed(fid);
		}
		@Override public void addTolling(String mode, String[] zones) {
			addSpan(tollingSpan(mode, zones));
		}
		@Override public void addParking(String pid, String l_txt,
			String c_txt)
		{
			addSpan(parkingSpan(pid, l_txt, c_txt));
		}
	};

	/** Process DMS action tags */
	private String process(String ms) {
		addSrc(SignMsgSource.schedule);
		if (isGateArm())
			addSrc(SignMsgSource.gate_arm);
		new MultiString(ms).parse(builder);
		MultiString _multi = builder.toMultiString();
		if (isBlank(_multi))
			return (valid) ? feed_msg : null;
	 	else
			return postProcess(_multi.toString());
	}

	/** Check if the action has gate arm priority */
	private boolean isGateArm() {
		return action.getMsgPriority() == GATE_ARM.ordinal();
	}

	/** Check if message is blank */
	private boolean isBlank(MultiString _multi) {
		return _multi.isBlank() && travel.isEmpty();
	}

	/** Post-process action tags */
	private String postProcess(String ms) {
		if (feed_msg != null)
			fail("Malformed feed message");
		ms = processTravelTimes(ms);
		return (valid) ? ms : null;
	}

	/** Process travel time tags */
	private String processTravelTimes(String ms) {
		boolean all_over = isAllOver();
		for (Map.Entry<String, TravelTime> ent : travel.entrySet()) {
			String sid = ent.getKey();
			TravelTime tt = ent.getValue();
			String sp = travelTimeSpan(tt, all_over);
			ms = ms.replace("[tt" + sid + "]", sp);
		}
		return ms;
	}

	/** Parse a message feed tag */
	private void parseFeed(String fid) {
		feed_msg = getFeedMsg(fid);
	}

	/** Lookup a feed message */
	private String getFeedMsg(String fid) {
		FeedMsg msg = FeedBucket.getMessage(fid, dms.getName());
		return (msg != null)
		      ? getFeedMsg(msg)
		      : fail("Invalid feed ID");
	}

	/** Get the feed message string */
	private String getFeedMsg(FeedMsg msg) {
		addSrc(SignMsgSource.external);
		MultiString _multi = msg.getMulti();
		if (!isMsgFeedVerifyEnabled() || isFeedMsgValid(_multi))
			return _multi.toString();
		else
			return fail("Invalid feed msg: " + _multi);
	}

	/** Test if a feed message is valid */
	private boolean isFeedMsgValid(MultiString _multi) {
		int n_lines = DMSHelper.getLineCount(dms);
		String[] lines = _multi.getLines(n_lines);
		for (int i = 0; i < lines.length; i++) {
			if (!isValidSignText((short) (i + 1), lines[i]))
				return false;
		}
		return true;
	}

	/** Check if a MULTI string is a valid sign text for the sign group */
	private boolean isValidSignText(short line, String ms) {
		return ms.isEmpty() ||
		       SignTextHelper.match(action.getSignGroup(), line, ms);
	}

	/** Calculate speed advisory span */
	private String speedAdvisorySpan() {
		addSrc(SignMsgSource.speed_advisory);
		Corridor cor = lookupCorridor();
		return (cor != null)
		      ? calculateSpeedAdvisory(cor)
		      : fail("Corridor not found");
	}

	/** Lookup the corridor for the DMS */
	private Corridor lookupCorridor() {
		return BaseObjectImpl.corridors.getCorridor(loc);
	}

	/** Calculate the speed advisory */
	private String calculateSpeedAdvisory(Corridor cor) {
		Float m = cor.calculateMilePoint(loc);
		return (m != null)
		      ? calculateSpeedAdvisory(cor, m)
		      : fail("No mile point on corridor");
	}

	/** Calculate the speed advisory */
	private String calculateSpeedAdvisory(Corridor cor, float m) {
		VSStationFinder vss_finder = new VSStationFinder(m);
		cor.findStation(vss_finder);
		if (dlog.isOpen())
			vss_finder.debug(dlog);
		if (!vss_finder.foundVSS())
			return fail("Start station not found");
		Integer lim = vss_finder.getSpeedLimit();
		if (null == lim)
			return fail("Unknown speed limit");
		Float a = vss_finder.calculateSpeedAdvisory();
		if (null == a)
			return fail("Missing speed data");
		int sa = Math.max(round5(a), getMinAdvisory());
		if (sa > lim && sa > getMaxAdvisory())
			return fail("Speed too high");
		else
			return Integer.toString(sa);
	}

	/** Add a slow traffic warning.
	 * @param spd Highest speed to activate warning.
	 * @param dist Distance to search for slow traffic (1/10 mile).
	 * @param mode Tag replacement mode (none, dist or speed). */
	private String slowWarningSpan(int spd, int dist, String mode) {
		addSrc(SignMsgSource.slow_warning);
		return slowWarningSpan(createSpeed(spd), createDist(dist),mode);
	}

	/** Create a speed.
	 * @param v Speed value.
	 * @return Matching speed. */
	private Speed createSpeed(int v) {
		// FIXME: use system attribute for units
		return new Speed(v, Speed.Units.MPH);
	}

	/** Create a distance.
	 * @param v Distance value (1/10 mile).
	 * @return Matching distance. */
	private Distance createDist(int v) {
		// FIXME: use system attribute for units
		int m = Math.max(v, 0);
		return new Distance(m * 0.1f, Distance.Units.MILES);
	}

	/** Calculate slow warning text span.
	 * @param spd Highest speed to activate warning.
	 * @param dist Distance to search for slow traffic.
	 * @param mode Tag replacement mode (none, dist or speed).
	 * @return Tag replacement span. */
	private String slowWarningSpan(Speed spd, Distance dist, String mode) {
		Corridor cor = lookupCorridor();
		return (cor != null)
		      ? slowWarningSpan(spd, dist, mode, cor)
		      : fail("Corridor not found");
	}

	/** Calculate slow warning text span.
	 * @param spd Highest speed to activate warning.
	 * @param dist Distance to search for slow traffic.
	 * @param mode Tag replacement mode (none, dist or speed).
	 * @param cor Freeway corridor.
	 * @return Tag replacement span. */
	private String slowWarningSpan(Speed spd, Distance dist, String mode,
		Corridor cor)
	{
		Float m = cor.calculateMilePoint(loc);
		return (m != null)
		      ? slowWarningSpan(spd, dist, mode, cor, m)
		      : fail("No mile point on corridor");
	}

	/** Calculate slow warning text span.
	 * @param spd Highest speed to activate warning.
	 * @param dist Distance to search for slow traffic.
	 * @param mode Tag replacement mode (none, dist or speed).
	 * @param cor Freeway corridor.
	 * @param m Milepoint on corridor.
	 * @return Tag replacement span. */
	private String slowWarningSpan(Speed spd, Distance dist, String mode,
		Corridor cor, float m)
	{
		BackupFinder bf = new BackupFinder(spd, dist, m);
		cor.findStation(bf);
		if (!bf.isBackedUp())
			return fail("No backup found");
		if ("dist".equals(mode))
			return slowWarningDist(bf);
		else if ("speed".equals(mode))
			return slowWarningSpeed(bf);
		else
			return EMPTY_SPAN;
	}

	/** Get backup distance as a text span */
	private String slowWarningDist(BackupFinder bf) {
		assert (bf.isBackedUp());
		Distance d = bf.distance();
		assert (d != null);
		int di = d.round(d.units);
		return (di > 0)
		      ? String.valueOf(di)
		      : fail("Invalid distance: " + di);
	}

	/** Get backup speed as a text span */
	private String slowWarningSpeed(BackupFinder bf) {
		assert (bf.isBackedUp());
		Speed s = bf.speed();
		assert (s != null);
		int si = round5((float) s.value);
		return (si > 0)
		      ? String.valueOf(si)
		      : fail("Invalid speed: " + si);
	}

	/** Calculate tolling text span */
	private String tollingSpan(String mode, String[] zones) {
		addSrc(SignMsgSource.tolling);
		if (zones.length < 1)
			return fail("No toll zones");
		switch (mode) {
		case "p": // priced
			return calculatePriceMessage(zones);
		case "o": // open
		case "c": // closed
			String last_zid = zones[zones.length - 1];
			prices.add(createPriceMessage(last_zid, null, 0f));
			return EMPTY_SPAN;
		default:
			return fail("Invalid toll mode");
		}
	}

	/** Calculate the price for tolling zones */
	private String calculatePriceMessage(String[] zones) {
		assert (zones.length > 0);
		String last_zid = null; // last toll zone
		String det = null;
		float price = 0f;
		float max_price = TollZoneImpl.max_price();
		for (String zid: zones) {
			TollZoneImpl tz = lookupZone(zid);
			if (null == tz)
				return fail("Toll zone not found: " + zid);
			VehicleSampler vs = tz.findMaxDensity(dms.getName(),
				loc);
			if (null == vs)
				return fail("Zone sampler not found: " + zid);
			Float zone_price = tz.getPrice(vs, dms.getName(), loc);
			if (null == zone_price)
				return fail("No Zone density: " + zid);
			last_zid = zid;
			det = vs.toString();
			price += zone_price;
			Float mp = tz.getMaxPrice();
			if (mp != null)
				max_price = Math.min(max_price, mp);
		}
		price = Math.min(price, max_price);
		PriceMessageEvent ev = createPriceMessage(last_zid, det, price);
		prices.add(ev);
		return priceSpan(ev.price);
	}

	/** Lookup a toll zone by ID */
	private TollZoneImpl lookupZone(String zid) {
		TollZone tz = TollZoneHelper.lookup(zid);
		return (tz instanceof TollZoneImpl)
		      ? (TollZoneImpl) tz
		      : null;
	}

	/** Format a price as a text span (like "3.50") */
	private String priceSpan(float p) {
		NumberFormat pf = NumberFormat.getNumberInstance();
		pf.setGroupingUsed(false);
		pf.setMinimumFractionDigits(2);
		pf.setMaximumFractionDigits(2);
		return pf.format(p);
	}

	/** Process travel time tag */
	private void processTravelTime(String sid, OverLimitMode mode,
		String o_txt)
	{
		addSrc(SignMsgSource.travel_time);
		Route r = findRoute(sid);
		if (r != null && r.legCount() > 0)
			processTravelTime(r, sid, mode, o_txt);
		else {
			logEvent(EventType.TT_NO_ROUTE, dms.getName());
			fail("No route to destination");
		}
	}

	/** Find a route to a travel time destination */
	private Route findRoute(String sid) {
		Station s = StationHelper.lookup(sid);
		return (s != null) ? findRoute(s) : null;
	}

	/** Find a route to a travel time destination */
	private Route findRoute(Station s) {
		GeoLoc dest = s.getR_Node().getGeoLoc();
		RouteFinder rf = new RouteFinder(BaseObjectImpl.corridors);
		return rf.findRoute(loc, dest);
	}

	/** Process travel time tag */
	private void processTravelTime(Route r, String sid, OverLimitMode mode,
		String o_txt)
	{
		try {
			travel.put(sid, createTravelTime(r, mode, o_txt));
		}
		catch (BadRouteException e) {
			logEvent(e.event_type, dms.getName());
			fail("Invalid route: " + e.getMessage());
		}
	}

	/** Create a travel time */
	private TravelTime createTravelTime(Route r, OverLimitMode mode,
		String o_txt) throws BadRouteException
	{
		int mn = calculateTravelTime(r, false);
		int mn_final = calculateTravelTime(r, true);
		int slow = maximumTripMinutes(r.getDistance());
		return new TravelTime(r, mode, o_txt, mn, mn_final, slow);
	}

	/** Calculate the travel time for the given route */
	private int calculateTravelTime(Route r, boolean final_dest)
		throws BadRouteException
	{
		return getTravelTime(r, final_dest).floor(MINUTES) +
			(r.getTurns() + 1);
	}

	/** Get the current travel time */
	private Interval getTravelTime(Route r, boolean final_dest)
		throws BadRouteException
	{
		RouteLeg leg = r.leg;
		Interval t = new Interval(0);
		while (leg != null) {
			RouteLegTimer rlt = new RouteLegTimer(leg, final_dest);
			t = t.add(rlt.calculateTime());
			leg = leg.prev;
		}
		return t;
	}

	/** Determine if all travel times should display "OVER" mode.
	 * If all routes are on the same corridor, when the "OVER X" form is
	 * used, it must be used for all destinations. */
	private boolean isAllOver() {
		return isSingleCorridor() && isAnyOver();
	}

	/** Are all travel time routes confined to the same single corridor */
	private boolean isSingleCorridor() {
		Route r = null;
		for (TravelTime tt : travel.values()) {
			if (r != null && !r.isSameCorridor(tt.route))
				return false;
			r = tt.route;
		}
		return (r != null) && (r.legCount() == 1);
	}

	/** Determine if any travel times will display "OVER" mode */
	private boolean isAnyOver() {
		for (TravelTime tt : travel.values()) {
			int mn = isFinalDest(tt.route) ? tt.min_final : tt.min;
			if (mn > tt.slow)
				return true;
		}
		return false;
	}

	/** Calculate travel time text span */
	private String travelTimeSpan(TravelTime tt, boolean all_over) {
		int mn = isFinalDest(tt.route) ? tt.min_final : tt.min;
		return travelTimeSpan(mn, tt.slow, all_over, tt.mode, tt.o_txt);
	}

	/** Check if the given route is a final destination */
	private boolean isFinalDest(Route r) {
		for (TravelTime tt : travel.values()) {
			Route ro = tt.route;
			if (ro != r && r.isSameCorridor(ro) &&
			   (r.getDistance().m() < ro.getDistance().m()))
			{
				return false;
			}
		}
		return true;
	}

	/** Calculate travel time text span */
	private String travelTimeSpan(int mn, int slow, boolean all_over,
		OverLimitMode mode, String o_txt)
	{
		boolean over = mn > slow;
		if (over)
			mn = slow;
		if (over || all_over)
			return overLimitSpan(mn, mode, o_txt);
		else
			return String.valueOf(mn);
	}

	/** Get over limit text span */
	private String overLimitSpan(int mn, OverLimitMode mode, String o_txt) {
		String lim = String.valueOf(roundUp5(mn));
		switch (mode) {
		case prepend:
			return o_txt + lim;
		case append:
			return lim + o_txt;
		case blank:
			return fail("Over limit: " + mn);
		}
		return fail("Invalid mode: " + mode);
	}

	/** Calculate parking area availability span */
	private String parkingSpan(String pid, String l_txt, String c_txt) {
		addSrc(SignMsgSource.parking);
		ParkingArea pa = ParkingAreaHelper.lookup(pid);
		if (pa instanceof ParkingAreaImpl) {
			ParkingAreaImpl pai = (ParkingAreaImpl) pa;
			Boolean open = pai.getOpen();
			if (null == open || !open)
				return c_txt;
			Boolean trust = pai.getTrustData();
			if (null == trust || !trust)
				return fail("Not trusted data: " + pid);
			Integer a = pai.getTrueAvailable();
			Integer low = pai.getLowThreshold();
			if (null == a)
				return fail("Availability unknown: " + pid);
			if (low != null && a <= low)
				return l_txt;
			else
				return a.toString();
		} else
			return fail("Invalid parking area: " + pid);
	}
}
