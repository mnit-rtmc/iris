/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
 * Copyright (C) 2021-2022  Iteris Inc.
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.ParkingAreaHelper;
import us.mn.state.dot.tms.PlanPhaseHelper;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.TollZoneHelper;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;
import us.mn.state.dot.tms.server.event.PriceMessageEvent;
import us.mn.state.dot.tms.server.event.TravelTimeEvent;
import us.mn.state.dot.tms.server.comm.clearguide.ClearGuidePoller;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.MINUTES;
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.MPH;
import static us.mn.state.dot.tms.utils.Multi.OverLimitMode;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Processor for action tags.  They are similar to regular MULTI tags, but
 * processed before sending to the device.  They are also not limited to DMS,
 * and can add conditions which trigger whether a device action is performed.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class TagProcessor {

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
	static private void logEvent(EventType et, String d, String sid) {
		final TravelTimeEvent ev = new TravelTimeEvent(et, d, sid);
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

	/** Active RWIS Weather Sensor collection */
	static private class ActiveSensors {
		private final ArrayList<WeatherSensor> sensors =
			new ArrayList<WeatherSensor>();
		private ActiveSensors() { }
		private ActiveSensors(WeatherSensor ws) {
			if (ws != null) {
				if (!WeatherSensorHelper.isSampleExpired(ws))
					this.sensors.add(ws);
			}
		}
		private ActiveSensors(WeatherSensor[] sensors) {
			for (WeatherSensor ws: sensors) {
				if (!WeatherSensorHelper.isSampleExpired(ws))
					this.sensors.add(ws);
			}
		}
		private boolean isEmpty() {
			return sensors.isEmpty();
		}
		/** Check if pavement friction is less than a threshold */
		private boolean isFrictionLt(int threshold) {
			for (WeatherSensor ws : sensors) {
				Integer f = ws.getPvmtFriction();
				if (f != null && f < threshold)
					return true;
			}
			return false;
		}
		/** Check if surface temperature is less than a threshold */
		private boolean isSurfaceTempLt(int threshold) {
			for (WeatherSensor ws : sensors) {
				Integer t = ws.getSurfTemp();
				if (t != null && t < threshold)
					return true;
			}
			return false;
		}
		/** Check if wind gusts are greather than a threshold */
		private boolean isWindGustGt(int threshold) {
			for (WeatherSensor ws : sensors) {
				Integer s = ws.getMaxWindGustSpeed();
				if (s != null && s > threshold)
					return true;
			}
			return false;
		}
		/** Check if visibility is less than a threshold */
		private boolean isVisibilityLt(int threshold) {
			for (WeatherSensor ws : sensors) {
				Integer v = ws.getVisibility();
				if (v != null && v < threshold)
					return true;
			}
			return false;
		}
		/** Check if precipitation is greater than a threshold */
		private boolean isPrecipGt(int threshold) {
			for (WeatherSensor ws : sensors) {
				Integer p = ws.getPrecipOneHour();
				if (p != null && p > threshold)
					return true;
			}
			return false;
		}
	}

	/** Device action */
	private final DeviceAction action;

	/** Device in question */
	private final DeviceImpl device;

	/** Device location */
	private final GeoLoc loc;

	/** Plan debug log */
	private final DebugLog logger;

	/** Flag to indicate passing all action tag conditions */
	private boolean condition;

	/** Sign message sources */
	private int sources;

	/** Add a message source */
	private void addSource(SignMsgSource s) {
		sources |= s.bit();
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
			device.getName(), zid, det, price);
	}

	/** Get tolling prices */
	private ArrayList<PriceMessageEvent> getPrices() {
		return (condition && prices.size() > 0) ? prices : null;
	}

	/** Fail parsing message */
	private String fail(String msg) {
		condition = false;
		if (logger.isOpen()) {
			logger.log(toString() + " [fail]: " + msg +
				" (" + getActionMulti() + ")");
		}
		return EMPTY_SPAN;
	}

	/** Create a new device action tag processor */
	public TagProcessor(DeviceAction da, DeviceImpl d, GeoLoc gl) {
		action = da;
		device = d;
		loc = gl;
		logger = DeviceActionJob.PLAN_LOG;
		condition = true;
	}

	/** Get the MULTI string for the device action */
	private String getActionMulti() {
		MsgPattern pat = action.getMsgPattern();
		return (pat != null) ? pat.getMulti().trim() : EMPTY_SPAN;
	}

	/** Process device action tags */
	public PlannedAction process() {
		String ms = getActionMulti();
		String multi = (ms.length() > 0) ? process(ms) : null;
		if (condition && logger.isOpen()) {
			logger.log(toString() + " [ok]: " + multi +
				" (" + getActionMulti() + ")");
		}
		return new PlannedAction(action, condition, multi, sources,
			getPrices());
	}

	/** Process action tags */
	private String process(String ms) {
		addSource(SignMsgSource.schedule);
		if (isGateArm())
			addSource(SignMsgSource.gate_arm);
		if (isAlert())
			addSource(SignMsgSource.alert);
		new MultiString(ms).parse(builder);
		MultiString multi = builder.toMultiString();
		if (isBlank(multi))
			return (condition) ? feed_msg : null;
		else
			return postProcess(multi.toString());
	}

	/** MULTI string builder for parsing action tags */
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
		@Override public void addStandby() {
			addSource(SignMsgSource.standby);
		}
		@Override public void addClearGuideAdvisory(String dms,
			int wid, int min, int max, String mode, int idx)
		{
			addSpan(clearGuideSpan(dms, wid, min, max, mode, idx));
		}
		@Override public void addRwis(String cond, int level) {
			addSpan(rwisSpan(cond, level));
		}
		@Override public void addSlowWarning(int spd, int dist,
			String mode)
		{
			addSpan(slowWarningSpan(spd, dist, mode));
		}
		@Override public void addExitWarning(String did, int occ) {
			addSpan(exitWarningSpan(did, occ));
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
		@Override public void addTimeAction(String dir, String format) {
			addSpan(timeActionSpan(dir, format));
		}
	};

	/** Check if the action source is gate arm */
	private boolean isGateArm() {
		return PlanPhaseHelper.isGateArm(action.getPhase());
	}

	/** Check if the action source is alert */
	private boolean isAlert() {
		return PlanPhaseHelper.isAlert(action.getPhase());
	}

	/** Check if message is blank */
	private boolean isBlank(MultiString multi) {
		return multi.isBlank() && travel.isEmpty();
	}

	/** Post-process action tags */
	private String postProcess(String ms) {
		if (feed_msg != null)
			fail("Malformed feed message");
		ms = processTravelTimes(ms);
		return (condition) ? ms : null;
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
		FeedMsg msg = FeedBucket.getMessage(fid, device.getName());
		return (msg != null)
		      ? getFeedMsg(msg)
		      : fail("No message for sign");
	}

	/** Get the feed message string */
	private String getFeedMsg(FeedMsg msg) {
		addSource(SignMsgSource.external);
		String ms = msg.getMulti().toString();
		if (!isMsgFeedVerifyEnabled() || isFeedMsgValid(ms))
			return ms;
		else
			return fail("Invalid feed msg: " + ms);
	}

	/** Test if a feed message is valid */
	private boolean isFeedMsgValid(String ms) {
		if (device instanceof DMSImpl) {
			DMSImpl dms = (DMSImpl) device;
			MsgPattern pat = action.getMsgPattern();
			return MsgPatternHelper.validateLines(pat, dms, ms)
			    == null;
		} else
			return false;
	}

	/** Calculate speed advisory span */
	private String speedAdvisorySpan() {
		addSource(SignMsgSource.speed_advisory);
		Corridor cor = lookupCorridor();
		return (cor != null)
		      ? calculateSpeedAdvisory(cor)
		      : fail("Corridor not found");
	}

	/** Lookup the corridor for the device */
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
		if (logger.isOpen())
			vss_finder.debug(logger);
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

	/** Make an RWIS warning span.
	 * @param cond Weather condition.
	 * @param level Warning level. */
	private String rwisSpan(String cond, int level) {
		addSource(SignMsgSource.rwis);
		if (cond.equals("slippery"))
			return rwisSlipperySpan(level);
		else if (cond.equals("windy"))
			return rwisWindySpan(level);
		else if (cond.equals("visibility"))
			return rwisVisibilitySpan(level);
		else if (cond.equals("flooding"))
			return rwisFloodingSpan(level);
		else
			return fail("Invalid condition: " + cond);
	}

	/** Get active RWIS weather sensors */
	private ActiveSensors activeSensors() {
		if (device instanceof DMSImpl) {
			DMSImpl dms = (DMSImpl) device;
			WeatherSensor[] sensors = dms.getWeatherSensors();
			if (sensors != null && sensors.length > 0)
				return new ActiveSensors(sensors);
		}
		WeatherSensor ws = WeatherSensorHelper.findNearest(loc);
		Distance dist = GeoLocHelper.distanceTo(loc, ws.getGeoLoc());
		Distance max_dist = new Distance(
			SystemAttrEnum.RWIS_AUTO_MAX_DIST_MILES.getFloat(),
			MILES
		);
		return (dist.m() <= max_dist.m())
		      ? new ActiveSensors(ws)
		      : new ActiveSensors();
	}

	/** Make an RWIS slippery condition span.
	 * @param level Warning level. */
	private String rwisSlipperySpan(final int level) {
		ActiveSensors sensors = activeSensors();
		if (sensors.isEmpty())
			return fail("No current weather data");
		int lv = 0;
		if (sensors.isFrictionLt(
			SystemAttrEnum.RWIS_SLIPPERY_1_PERCENT.getInt()
		)) lv++;
		if (level >= 2 && sensors.isSurfaceTempLt(
			SystemAttrEnum.RWIS_SLIPPERY_2_DEGREES.getInt()
		)) lv++;
		if (level >= 3 && sensors.isFrictionLt(
			SystemAttrEnum.RWIS_SLIPPERY_3_PERCENT.getInt()
		)) lv++;
		return (lv == level)
		      ? EMPTY_SPAN
		      : fail("Condition not slippery " + level);
	}

	/** Make an RWIS windy condition span.
	 * @param level Warning level. */
	private String rwisWindySpan(int level) {
		ActiveSensors sensors = activeSensors();
		if (sensors.isEmpty())
			return fail("No current weather data");
		int lv = 0;
		if (sensors.isWindGustGt(
			SystemAttrEnum.RWIS_WINDY_1_KPH.getInt()
		)) lv++;
		if (level >= 2 && sensors.isWindGustGt(
			SystemAttrEnum.RWIS_WINDY_2_KPH.getInt()
		)) lv++;
		return (lv == level)
		      ? EMPTY_SPAN
		      : fail("Condition not windy " + level);
	}

	/** Make an RWIS visibility condition span.
	 * @param level Warning level. */
	private String rwisVisibilitySpan(int level) {
		ActiveSensors sensors = activeSensors();
		if (sensors.isEmpty())
			return fail("No current weather data");
		int lv = 0;
		if (sensors.isVisibilityLt(
			SystemAttrEnum.RWIS_VISIBILITY_1_M.getInt()
		)) lv++;
		if (level >= 2 && sensors.isVisibilityLt(
			SystemAttrEnum.RWIS_VISIBILITY_2_M.getInt()
		)) lv++;
		return (lv == level)
		      ? EMPTY_SPAN
		      : fail("Condition not visibility " + level);
	}

	/** Make an RWIS flooding condition span.
	 * @param level Warning level. */
	private String rwisFloodingSpan(int level) {
		ActiveSensors sensors = activeSensors();
		if (sensors.isEmpty())
			return fail("No current weather data");
		int lv = 0;
		if (sensors.isPrecipGt(
			SystemAttrEnum.RWIS_FLOODING_1_MM.getInt()
		)) lv++;
		if (level >= 2 && sensors.isPrecipGt(
			SystemAttrEnum.RWIS_FLOODING_2_MM.getInt()
		)) lv++;
		return (lv == level)
		      ? EMPTY_SPAN
		      : fail("Condition not flooding " + level);
	}

	/** Add a slow traffic warning.
	 * @param spd Highest speed to activate warning.
	 * @param dist Distance to search for slow traffic (1/10 mile).
	 * @param mode Tag replacement mode (none, dist or speed). */
	private String slowWarningSpan(int spd, int dist, String mode) {
		addSource(SignMsgSource.slow_warning);
		return slowWarningSpan(createSpeed(spd), createDist(dist),mode);
	}

	/** Add an exit backup warning.
	 * @param did Exit detector ID.
	 * @param occ Threshold occupancy to activate warning. */
	private String exitWarningSpan(String did, int occ) {
		addSource(SignMsgSource.exit_warning);
		Detector det = DetectorHelper.lookup(did);
		return (det instanceof DetectorImpl)
		      ? exitWarningSpan((DetectorImpl) det, occ)
		      : fail("Detector not found");
	}

	/** Add an exit backup warning.
	 * @param det Exit detector.
	 * @param occ Threshold occupancy to activate warning. */
	private String exitWarningSpan(DetectorImpl det, int occ) {
		ActionPlan plan = action.getActionPlan();
		float o = det.getOccupancy(
			DetectorImpl.BIN_PERIOD_MS * 3,
			plan.getIgnoreAutoFail()
		);
		return (o > occ) ? EMPTY_SPAN : fail("Occupancy too low");
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
		ActionPlan plan = action.getActionPlan();
		boolean ig = plan.getIgnoreAutoFail();
		BackupFinder bf = new BackupFinder(spd, dist, m, ig);
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
		addSource(SignMsgSource.tolling);
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
			VehicleSampler vs = tz.findMaxDensity(device.getName(),
				loc);
			if (null == vs)
				return fail("Zone sampler not found: " + zid);
			Float zone_price = tz.getPrice(device.getName(), vs,
				loc);
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
		addSource(SignMsgSource.travel_time);
		Route r = findRoute(sid);
		if (r != null && r.legCount() > 0)
			processTravelTime(r, sid, mode, o_txt);
		else {
			logEvent(EventType.TT_NO_ROUTE, device.getName(), sid);
			fail("No route to destination: " + sid);
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
			logEvent(e.event_type, device.getName(), e.sid);
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
		addSource(SignMsgSource.parking);
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

	/** Calculate ClearGuide advisory span
	 * @param dms DMS name
	 * @param wid Workzone ID
	 * @param min Minimum valid value
	 * @param max Maximum valid value
	 * @param mode Tag replacement mode: tt, delay, etc.
	 * @param idx Workzone index, zero based */
	private String clearGuideSpan(String dms, int wid, int min, int max,
		String mode, int idx)
	{
		addSource(SignMsgSource.clearguide);
		addSource(SignMsgSource.external);
		return calcClearGuideAdvisory(dms, wid, min, max, mode, idx);
	}

	/** Calculate the span
	 * @param dms DMS name
	 * @param wid Workzone ID
	 * @param min Minimum valid value
	 * @param max Maximum valid value
	 * @param mode Tag replacement mode: tt, delay, etc.
	 * @param idx Workzone index, zero based
	 * @return Span or empty string on error */
	private String calcClearGuideAdvisory(String dms, int wid, int min,
		int max, String mode, int idx)
	{
		Integer stat = ClearGuidePoller.cg_dms.getStat(
			dms, wid, mode, idx);
		if (stat != null && (stat < min || stat > max))
			stat = null;
		if (logger.isOpen()) {
			logger.log("calcClearGuideAdvisory:" +
				" dms=" + dms + " wid=" + wid +
				" min=" + min + " max=" + max +
				" mode=" + mode + " idx=" + idx +
				" stat=" + stat);
		}
		if (stat != null) {
			if ("sp_cond".equals(mode))
				return EMPTY_SPAN;
			else
				return Integer.toString(Math.round(stat));
		} else {
			String msg = "No match: does statistic, " + 
				"route_id and index match?";
			if (logger.isOpen())
				logger.log("calcClearGuideAdvisory: " + msg);
			return fail(msg);
		}
	}

	/** Calculate time action span */
	private String timeActionSpan(String dir, String format) {
		ActionPlan plan = action.getActionPlan();
		Date dt = getDateDir(plan, dir);
		if (dt != null) {
			LocalDateTime ldt = dt.toInstant().atZone(
				ZoneId.systemDefault()).toLocalDateTime();
			return ldt.format(DateTimeFormatter.ofPattern(format));
		} else
			return fail("Scheduled time action not found");
	}

	/** Get scheduled date that's most recent or soonest from now */
	private Date getDateDir(ActionPlan plan, String dir) {
		Date now = TimeSteward.getDateInstance();
		return ("p".equals(dir))
			? TimeActionHelper.getMostRecent(plan, now)
			: TimeActionHelper.getSoonest(plan, now);
	}
}
