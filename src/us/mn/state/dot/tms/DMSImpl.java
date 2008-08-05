/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.tms.comm.DMSPoller;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.ntcip.ShortErrorStatus;
import us.mn.state.dot.tms.event.EventType;
import us.mn.state.dot.tms.event.SignStatusEvent;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * Dynamic Message Sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSImpl extends TrafficDeviceImpl implements DMS, Storable {

	/** ObjectVault table name */
	static public final String tableName = "dms";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Name to use for messages with no owner */
	static protected final String NO_OWNER = "Nobody";

	/** Default pitch between pixels (mm) */
	static protected final int DEFAULT_PITCH = 69;

	/** Default character height (pixels) */
	static protected final int DEFAULT_CHARACTER_HEIGHT = 7;

	/** Minimum speed for travel time trip calculation */
	static protected final int MINIMUM_TRIP_SPEED = 15;

	/** Validate travel time text */
	static protected void validateTravel(String s)
		throws ChangeVetoException
	{
		if(!MultiString.isValid(s))
			throw new ChangeVetoException("Invalid travel: " + s);
	}

	/** Special value to indicate an invalid line spacing */
	static protected final int INVALID_LINE_SPACING = -1;

	/** Calculate the line spacing for a given sign and font height */
	static protected int calculateLineSpacing(int sign_height,
		int font_height)
	{
		int extra = sign_height % font_height;
		int gaps = (sign_height / font_height) - 1;
		if(extra == 0)
			return 0;
		else if((gaps > 0) && (extra % gaps == 0))
			return extra / gaps;
		return INVALID_LINE_SPACING;
	}

	/** Maximum route legs */
	static protected final int MAX_ROUTE_LEGS = 8;

	/** Maximum route distance */
	static protected final int MAX_ROUTE_DISTANCE = 16;

	/** Notify all observers for an update */
	public void notifyUpdate() {
		super.notifyUpdate();
		dmsList.update(id);
	}

	/** Create a new dynamic message sign */
	public DMSImpl(String id) throws TMSException, RemoteException {
		super(id);
		mile = new Float(0);
		plans = new TimingPlanImpl[0];
		deviceList.add(id, this);
		resetTransients();
		message = createBlankMessage(NO_OWNER);
		s_routes = new HashMap<String, Route>();
	}

	/** Constructor needed for ObjectVault */
	protected DMSImpl(FieldMap fields) throws RemoteException {
		super(fields);
		message = createBlankMessage(NO_OWNER);
		s_routes = new HashMap<String, Route>();
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		// FIXME: implement this for SONAR
		return null;
	}

	/** Get the DMS poller */
	protected DMSPoller getDMSPoller() {
		if(isActive()) {
			MessagePoller mp = getPoller();
			if(mp instanceof DMSPoller)
				return (DMSPoller)mp;
		}
		return null;
	}

	/** Previous message */
	protected transient SignMessage old_mess;

	/** Test if the message has changed */
	protected boolean isChanged() {
		boolean changed = message != old_mess;
		changed |= !message.equals(old_mess);
		old_mess = message;
		return changed;
	}

	/** Status code from last notification */
	protected transient int status_code;

	/** Notify all observers for a status change */
	public void notifyStatus() {
		int s = getStatusCode();
		if(isChanged() || s != status_code) {
			status_code = s;
			dmsList.update(id);
		}
		super.notifyStatus();
	}

	/** Set the controller to which this DMS is assigned */
	public void setController(String c) throws TMSException {
		super.setController(c);
		if(c == null)
			deviceList.add(id, this);
		else {
			deviceList.remove(id);
			setReset(false);
		}
	}

	/** Reset flag */
	protected transient boolean reset;

	/** Set the reset flag */
	public void setReset(boolean r) {
		if(r && !reset) {
			DMSPoller p = getDMSPoller();
			if(p != null) {
				resetTransients();
				p.queryConfiguration(this);
			}
		}
		reset = r;
	}

	/** Initialize the transient state */
	public void initTransients() throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.initTransients();
		LinkedList p = new LinkedList();
		Set s = plan_mapping.lookup("traffic_device", this);
		Iterator it = s.iterator();
		while(it.hasNext())
			p.add(vault.load(it.next()));
		plans = (TimingPlanImpl [])p.toArray(new TimingPlanImpl[0]);
		Arrays.sort(plans);
		for(int i = 0; i < plans.length; i++)
			planList.append(plans[i]);
		resetTransients();
	}

	/** Reset some transient fields */
	protected void resetTransients() {
		pixelFailureCount = 0;
		minCabinetTemp = UNKNOWN_TEMP;
		maxCabinetTemp = UNKNOWN_TEMP;
		minAmbientTemp = UNKNOWN_TEMP;
		maxAmbientTemp = UNKNOWN_TEMP;
		minHousingTemp = UNKNOWN_TEMP;
		maxHousingTemp = UNKNOWN_TEMP;
		make = UNKNOWN;
		model = UNKNOWN;
		version = UNKNOWN;
		signAccess = UNKNOWN;
		signType = UNKNOWN;
		signHeight = 0;
		signWidth = 0;
		horizontalBorder = 0;
		verticalBorder = 0;
		legend = UNKNOWN;
		beaconType = UNKNOWN;
		signTechnology = UNKNOWN;
		characterHeightPixels = -1;
		characterWidthPixels = -1;
		signHeightPixels = 0;
		signWidthPixels = 0;
		horizontalPitch = 0;
		verticalPitch = 0;
		maxPhotocellLevel = 0;
		photocellLevel = 0;
		brightnessLevels = 0;
		brightnessLevel = 0;
		brightnessTable = new int[0];
		lightOutput = 0;
		manualBrightness = false;
		lamp_status = UNKNOWN;
		fan_status = UNKNOWN;
		power_table = new StatusTable();
		heat_tape = UNKNOWN;
	}

	/** Camera from which this can be seen */
	protected CameraImpl camera;

	/** Set the verification camera */
	public void setCamera(String id) throws TMSException {
		setCamera((CameraImpl)cameraList.getElement(id));
	}

	/** Set the verification camera */
	protected synchronized void setCamera(CameraImpl c)
		throws TMSException
	{
		if(c == camera)
			return;
		// FIXME: use toString() instead of getOID()
		if(c == null)
			store.update(this, "camera", "0");
		else
			store.update(this, "camera", c.getOID());
		camera = c;
	}

	/** Get verification camera */
	public TrafficDevice getCamera() {
		return camera;
	}

	/** Miles downstream of reference point */
	protected Float mile;

	/** Get the miles downstream of reference point */
	public Float getMile() {
		return mile;
	}

	/** Set the miles downstream of reference point */
	public synchronized void setMile(Float m) throws TMSException {
		if(m.equals(mile))
			return;
		store.update(this, "mile", m);
		mile = m;
	}

	/** Travel time message template */
	protected String travel;

	/** Get the travel time message template */
	public String getTravel() {
		return travel;
	}

	/** Set the travel time message template */
	public synchronized void setTravel(String t) throws TMSException {
		if(t.equals(travel))
			return;
		validateTravel(t);
		store.update(this, "travel", t);
		travel = t;
		s_routes.clear();
	}

	/** Array of timing plans for this sign */
	protected transient TimingPlanImpl[] plans;

	/** Add a timing plan to the sign */
	protected synchronized void addTimingPlan(TimingPlanImpl plan)
		throws TMSException
	{
		TimingPlanImpl[] p = new TimingPlanImpl[plans.length + 1];
		for(int i = 0; i < plans.length; i++) {
			p[i] = plans[i];
			if(p[i].equals(plan))
				return;
		}
		p[plans.length] = plan;
		try {
			vault.save(plan, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		setTimingPlans(p);
	}

	/** Add a new timing plan to the sign */
	public void addTimingPlan(int period) throws TMSException,
		RemoteException
	{
		TimingPlanImpl plan = new TimingPlanImpl(period);
		addTimingPlan(plan);
		planList.append(plan);
	}

	/** Add an existing timing plan to the sign */
	protected void addExistingTimingPlan(TimingPlan plan)
		throws TMSException
	{
		TimingPlanImpl p = planList.lookup(plan);
		if(p == null)
			throw new ChangeVetoException("Cannot find plan");
		addTimingPlan(p);
	}

	/** Remove a timing plan from the sign */
	protected synchronized TimingPlanImpl removeTimingPlan(TimingPlan plan)
		throws TMSException
	{
		TimingPlanImpl old_plan = null;
		TimingPlanImpl[] p = new TimingPlanImpl[plans.length - 1];
		for(int i = 0, j = 0; i < plans.length; i++) {
			if(plans[i].equals(plan))
				old_plan = plans[i];
			else {
				p[j] = plans[i];
				j++;
			}
		}
		if(old_plan == null)
			throw new ChangeVetoException("Plan not found");
		boolean lastReference = old_plan.isDeletable();
		setTimingPlans(p);
		if(lastReference)
			return old_plan;
		else
			return null;
	}

	/** Associate (or dissociate) a timing plan with this sign */
	public void setTimingPlan(TimingPlan plan, boolean a)
		throws TMSException
	{
		if(a)
			addExistingTimingPlan(plan);
		else {
			TimingPlanImpl p = removeTimingPlan(plan);
			if(p != null) {
				p.notifyDelete();
				planList.remove(p);
			}
		}
	}

	/** Set all current timing plans which affect this sign */
	protected void setTimingPlans(TimingPlanImpl[] p) throws TMSException {
		Arrays.sort(p);
		if(Arrays.equals(p, plans))
			return;
		plan_mapping.update("traffic_device", this, p);
		plans = p;
	}

	/** Check if a timing plan is associated with this sign */
	public boolean hasTimingPlan(TimingPlan plan) {
		TimingPlanImpl[] plans = this.plans;	// Avoid races
		for(int i = 0; i < plans.length; i++) {
			if(plans[i].equals(plan))
				return true;
		}
		return false;
	}

	/** Calculate the maximum trip time to display on the sign */
	static protected int maximumTripTime(float distance) {
		float hours = distance / MINIMUM_TRIP_SPEED;
		return Math.round(hours * 60);
	}

	/** Round up to the next 5 minutes */
	static protected int roundUp5Min(int minutes) {
		return ((minutes - 1) / 5 + 1) * 5;
	}

	/** Calculate the travel time for the given route */
	protected int calculateTravelTime(Route route, boolean final_dest)
		throws InvalidMessageException
	{
		try {
			float hours = route.getTravelTime(final_dest);
			return (int)(hours * 60) + 1;
		}
		catch(BadRouteException e) {
			throw new InvalidMessageException("Bad route for " +
				id + ": " + e.getMessage());
		}
	}

	/** Are all the routes confined to the same single corridor */
	protected boolean isSingleCorridor() {
		Corridor cor = null;
		for(Route r: s_routes.values()) {
			Corridor c = r.getOnlyCorridor();
			if(c == null)
				return false;
			if(cor == null)
				cor = c;
			else if(c != cor)
				return false;
		}
		return cor != null;
	}

	/** Are two routes confined to the same single corridor */
	protected boolean isSameCorridor(Route r1, Route r2) {
		if(r1 != null && r2 != null) {
			Corridor c1 = r1.getOnlyCorridor();
			Corridor c2 = r2.getOnlyCorridor();
			if(c1 != null && c2 != null)
				return c1 == c2;
		}
		return false;
	}

	/** Check if the given route is a final destination */
	protected boolean isFinalDest(Route r) {
		for(Route ro: s_routes.values()) {
			if(ro != r && isSameCorridor(r, ro) &&
				r.getLength() < ro.getLength())
			{
				return false;
			}
		}
		return true;
	}

	/** Compose a travel time message */
	protected String composeTravelTimeMessage()
		throws InvalidMessageException
	{
		MultiString m = new MultiString(travel);
		MultiString.TravelCallback cb = new MultiString.TravelCallback()
		{
			/* If all routes are on the same corridor, when the
			 * "OVER X" form is used, it must be used for all
			 * destinations. So, first we must calculate the times
			 * for each destination. Then, determine if the "OVER"
			 * form should be used. After that, replace the travel
			 * time tags with the selected values. */
			protected boolean any_over = false;
			protected boolean all_over = false;

			/** Calculate the travel time to the given station */
			public String calculateTime(String sid)
				throws InvalidMessageException
			{
				Route r = lookupRoute(sid);
				if(r == null) {
					throw new InvalidMessageException(id +
						": NO ROUTE TO " + sid);
				}
				boolean final_dest = isFinalDest(r);
				int m = calculateTravelTime(r, final_dest);
				int slow = maximumTripTime(r.getLength());
				boolean over = m > slow;
				if(over) {
					any_over = true;
					m = slow;
				}
				if(over || all_over) {
					m = roundUp5Min(m);
					return "OVER " + String.valueOf(m);
				} else
					return String.valueOf(m);
			}

			/** Check if the callback has changed formatting mode */
			public boolean isChanged() {
				all_over = any_over && isSingleCorridor();
				return all_over;
			}
		};
		String t = m.replaceTravelTimes(cb);
		if(cb.isChanged())
			t = m.replaceTravelTimes(cb);
		return t;
	}

	/** Check if an interval is within an active timing plan */
	protected boolean isWithin(int interval) {
		TimingPlanImpl[] plans = this.plans;	// Avoid races
		for(int i = 0; i < plans.length; i++) {
			if(plans[i].isActive()) {
				if(plans[i].checkWithin(interval))
					return true;
			}
		}
		return false;
	}

	/** Check if an interval is within an active testing timing plan */
	protected boolean isTesting(int interval) {
		TimingPlanImpl[] plans = this.plans;	// Avoid races
		for(int i = 0; i < plans.length; i++) {
			if(plans[i].isActive() && plans[i].isTesting()) {
				if(plans[i].checkWithin(interval))
					return true;
			}
		}
		return false;
	}

	/** Mapping of station IDs to routes */
	protected transient HashMap<String, Route> s_routes;

	/** Create one route to a travel time destination */
	protected Route createRoute(StationImpl s) {
		R_NodeImpl r_node = s.getR_Node();
		GeoLoc dest = r_node.lookupGeoLoc();
		RouteBuilder builder = new RouteBuilder(getId(), nodeMap,
			MAX_ROUTE_LEGS, MAX_ROUTE_DISTANCE);
		GeoLoc loc = lookupGeoLoc();
		SortedSet<Route> routes = builder.findRoutes(loc, dest);
		if(routes.size() > 0)
			return routes.first();
		else
			return null;
	}

	/** Create one route to a travel time destination */
	protected Route createRoute(String sid) {
		StationImpl s = (StationImpl)statMap.getElement(sid);
		if(s != null)
			return createRoute(s);
		else
			return null;
	}

	/** Lookup a route by station ID */
	protected Route lookupRoute(String sid) {
		if(!s_routes.containsKey(sid))
			s_routes.put(sid, createRoute(sid));
		return s_routes.get(sid);
	}

	/** Update the travel times for this sign */
	protected void updateTravelTime() {
		try {
			setTravelTime(composeTravelTimeMessage());
		}
		catch(InvalidMessageException e) {
			if(RouteBuilder.TRAVEL_LOG.isOpen())
				RouteBuilder.TRAVEL_LOG.log(e.getMessage());
			clearTravelTime();
		}
	}

	/** Update the travel times for this sign */
	public void updateTravelTimes(int interval) {
		if(isWithin(interval))
			updateTravelTime();
		else {
			s_routes.clear();
			clearTravelTime();
		}
	}

	/** Create a blank message for the sign */
	protected SignMessage createBlankMessage(String owner) {
		MultiString multi = new MultiString();
		BitmapGraphic bitmap = new BitmapGraphic(signWidthPixels,
			signHeightPixels);
		return new SignMessage(owner, multi, bitmap, 0);
	}

	/** Currently displayed message */
	protected transient SignMessage message;

	/** Set a new message on the sign, all pages rendered */
	public void setMessage(String owner, String text, int duration)
		throws InvalidMessageException
	{
		MultiString multi = new MultiString(text);
		sendMessage(new SignMessage(owner, multi,
			createPixelMaps(multi), duration));
	}

	/** Set a new message on the sign */
	public void setMessage(SignMessage m) throws InvalidMessageException {
		sendMessage(m);
	}

	/** Update graphic for all pages for the current message */
	public void updateMessageGraphic() {
		SignMessage m = message;	// Avoid races
		m.setBitmaps(createPixelMaps(m.getMulti()));
	}

	/** 
	 * Update graphic using a new bitmap. This is used by DMS that return
	 * bitmaps (and possibly no text) on status querries.
	 */
	public void updateMessageGraphic(BitmapGraphic bm) {
		message.setBitmap(bm);
	}

	/** Set a new alert on the sign */
	public void setAlert(String owner, String text)
		throws InvalidMessageException
	{
		if(isActive() && (message.isBlank() ||
			message instanceof SignAlert ||
			message instanceof SignTravelTime))
		{
			MultiString multi = new MultiString(text);
			sendMessage(new SignAlert(owner, multi,
				createPixelMaps(multi),
				SignMessage.DURATION_INFINITE));
		}
	}

	/** Clear any alert on the sign */
	public void clearAlert(String owner) {
		if(message instanceof SignAlert)
			clearMessage(owner);
	}

	/** Set a travel time message on the sign */
	protected void setTravelTime(String text) throws InvalidMessageException
	{
		if(isActive() && (message.isBlank() ||
			message instanceof SignTravelTime))
		{
			sendTravelTime(text);
		}
	}

	/** Send a travel time message to the sign */
	protected void sendTravelTime(String text)
		throws InvalidMessageException
	{
		SignMessage m = message;
		if(m.equalsString(text)) {
			m.setDuration(SignTravelTime.MESSAGE_DURATION);
			setMessageTimeRemaining(m);
			return;
		}
		MultiString multi = new MultiString(text);
		sendMessage(new SignTravelTime(multi, createPixelMaps(multi)));
	}

	/** Clear a travel time message */
	protected void clearTravelTime() {
		if(message instanceof SignTravelTime)
			clearMessage(NO_OWNER);
	}

	/** Send a new message to the sign */
	protected void sendMessage(SignMessage m) throws InvalidMessageException
	{
		DMSPoller p = getDMSPoller();
		if(p != null)
			p.sendMessage(this, m);
	}

	/** Set the time remaining for the currently displayed message */
	protected void setMessageTimeRemaining(SignMessage m) {
		DMSPoller p = getDMSPoller();
		if(p != null)
			p.setMessageTimeRemaining(this, m);
	}

	/** Clear the message displayed on the sign */
	public void clearMessage(String owner) {
		setMessageTimeRemaining(createBlankMessage(owner));
	}

	/** Set the active sign message (called after a message has been
	 * successfully activated) */
	public void setActiveMessage(SignMessage m) {
		// If the new message is different from the old message
		// then log it.  Required to prevent double-logging
		// when users double-click the send.
		if(m.isBlank()) {
			if(!message.isBlank()) {
				logMessage(EventType.DMS_CLEARED, id, null,
					m.getOwner());
			}
		} else {
			if(!message.equals(m)) {
				logMessage(EventType.DMS_DEPLOYED, id,
					m.getMulti().toString(), m.getOwner());
			}
		}
		message = m;
	}

	/** 
	 * Set the message from information read from the controller.
	 * All pages are rendered.
	 */
	public void setMessageFromController(String text, int time) {
		if(message.equalsString(text))
			return;
		MultiString multi = new MultiString(text);
		setActiveMessage(new SignMessage(NO_OWNER, multi, 
			createPixelMaps(multi), time));
	}

	/** Log a message */
	protected void logMessage(EventType et, String id, String text, 
		String owner)
	{
		SignStatusEvent sse = new SignStatusEvent(et, id, text, owner);
		try {
			sse.doStore();
		}
		catch(TMSException e) {
			e.printStackTrace();
		}
	}

	/** Get current sign messasge
	 * @return Currently active message (cannot be null) */
	public SignMessage getMessage() {
		return message;
	}

	/** Get the number of text lines */
	public int getTextLines() {
		return signHeightPixels / getLineHeightPixels();
	}

	/** 
	 * Create a pixel map of the message for all pages within the
	 * MultiString message.
	 */
	public Map<Integer, BitmapGraphic> createPixelMaps(MultiString multi) {
		final FontImpl font = getFont();
		if(font == null)
			return new TreeMap<Integer, BitmapGraphic>();
		PixelMapBuilder builder = new PixelMapBuilder(signWidthPixels,
			signHeightPixels, characterWidthPixels, font,
			new PixelMapBuilder.GlyphFinder() {
				public Graphic lookupGraphic(int cp)
					throws InvalidMessageException
				{
					return font.getGraphic(cp);
				}
			});
		multi.parse(builder);
		return builder.getPixmaps();
	}

	/** Lookup the best font */
	static protected FontImpl _lookupFont(final int h, final int w,
		final int ls) throws NamespaceError
	{
		return (FontImpl)namespace.findObject(Font.SONAR_TYPE,
			new Checker<FontImpl>()
		{
			public boolean check(FontImpl f) {
				return f.matches(h, w, ls);
			}
		});
	}

	/** Lookup the best font */
	static protected FontImpl lookupFont(int h, int w, int ls) {
		try {
			FontImpl f = _lookupFont(h, w, ls);
			if(f != null || w == 0)
				return f;
			else
				return _lookupFont(h, 0, ls);
		}
		catch(NamespaceError e) {
			return null;
		}
	}

	/** Get the appropriate font for this sign */
	public FontImpl getFont() {
		return lookupFont(getLineHeightPixels(),characterWidthPixels,0);
	}

	/** Test if the sign status is unavailable */
	public boolean isUnavailable() {
		return pixelFailureCount >= BAD_PIXEL_LIMIT ||
			maxCabinetTemp >= HIGH_TEMP_CUTOFF ||
			maxHousingTemp >= HIGH_TEMP_CUTOFF ||
			hasErrorStatus(
				ShortErrorStatus.POWER |
				ShortErrorStatus.ATTACHED_DEVICE |
				ShortErrorStatus.LAMP |
				ShortErrorStatus.PHOTOCELL |
				ShortErrorStatus.CONTROLLER |
				ShortErrorStatus.TEMPERATURE
			);
	}

	/** Get the current sign status code */
	public int getStatusCode() {
		if(!isActive())
			return STATUS_INACTIVE;
		if(isFailed())
			return STATUS_FAILED;
		if(message instanceof SignTravelTime)
			return STATUS_TRAVEL_TIME;
		if(!message.isBlank())
			return STATUS_DEPLOYED;
		if(isUnavailable())
			return STATUS_UNAVAILABLE;
		else
			return STATUS_AVAILABLE;
	}

	/** Sign error status */
	protected transient ShortErrorStatus error_status;

	/** Check if a sign has the specified error status */
	protected boolean hasErrorStatus(int mask) {
		ShortErrorStatus s = error_status;
		if(s == null)
			return false;
		return s.checkError(mask);
	}

	/** Set the error status */
	public void setErrorStatus(ShortErrorStatus s) {
		error_status = s;
		ControllerImpl c = controller;	// Avoid races
		if(c != null)
			c.setSetup(s.getValue());
	}

	/** Pixel failure count */
	protected transient int pixelFailureCount;

	/** Set the pixel failure count */
	public void setPixelFailureCount(int c) {
		pixelFailureCount = c;
	}

	/** Get the pixel failure count */
	public int getPixelFailureCount() {
		return pixelFailureCount;
	}

	/** Minimum cabinet temperature */
	protected transient int minCabinetTemp;

	/** Set the minimum cabinet temperature */
	public void setMinCabinetTemp(int t) {
		minCabinetTemp = t;
	}

	/** Get the minimum cabinet temperature */
	public int getMinCabinetTemp() {
		return minCabinetTemp;
	}

	/** Maximum cabinet temperature */
	protected transient int maxCabinetTemp;

	/** Set the maximum cabinet temperature */
	public void setMaxCabinetTemp(int t) {
		maxCabinetTemp = t;
	}

	/** Get the maximum cabinet temperature */
	public int getMaxCabinetTemp() {
		return maxCabinetTemp;
	}

	/** Minimum ambient temperature */
	protected transient int minAmbientTemp;

	/** Set the minimum ambient temperature */
	public void setMinAmbientTemp(int t) {
		minAmbientTemp = t;
	}

	/** Get the minimum ambient temperature */
	public int getMinAmbientTemp() {
		return minAmbientTemp;
	}

	/** Maximum ambient temperature */
	protected transient int maxAmbientTemp;

	/** Set the maximum ambient temperature */
	public void setMaxAmbientTemp(int t) {
		maxAmbientTemp = t;
	}

	/** Get the maximum ambient temperature */
	public int getMaxAmbientTemp() {
		return maxAmbientTemp;
	}

	/** Minimum housing temperature */
	protected transient int minHousingTemp;

	/** Set the minimum housing temperature */
	public void setMinHousingTemp(int t) {
		minHousingTemp = t;
	}

	/** Get the minimum housing temperature */
	public int getMinHousingTemp() {
		return minHousingTemp;
	}

	/** Maximum housing temperature */
	protected transient int maxHousingTemp;

	/** Set the maximum housing temperature */
	public void setMaxHousingTemp(int t) {
		maxHousingTemp = t;
	}

	/** Get the maximum housing temperature */
	public int getMaxHousingTemp() {
		return maxHousingTemp;
	}

	/** Make (manufacturer) */
	protected transient String make;

	/** Set the make */
	public void setMake(String m) {
		make = m;
	}

	/** Get the make */
	public String getMake() {
		return make;
	}

	/** Model */
	protected transient String model;

	/** Set the model */
	public void setModel(String m) {
		model = m;
	}

	/** Get the model */
	public String getModel() {
		return model;
	}

	/** Software version */
	protected transient String version;

	/** Set the version */
	public void setVersion(String v) {
		version = v;
		ControllerImpl c = controller;	// Avoid races
		if(c != null)
			c.setVersion(version);
	}

	/** Get the version */
	public String getVersion() {
		return version;
	}

	/** Sign access description */
	protected transient String signAccess;

	/** Set sign access description */
	public void setSignAccess(String a) {
		signAccess = a;
	}

	/** Get sign access description */
	public String getSignAccess() {
		return signAccess;
	}

	/** Sign type description */
	protected transient String signType;

	/** Set sign type description */
	public void setSignType(String t) {
		signType = t;
	}

	/** Get sign type description */
	public String getSignType() {
		return signType;
	}

	/** Sign height (mm) */
	protected transient int signHeight;

	/** Set sign height (mm) */
	public void setSignHeight(int h) {
		signHeight = h;
	}

	/** Get sign height (mm) */
	public int getSignHeight() {
		return signHeight;
	}

	/** Sign width (mm) */
	protected transient int signWidth;

	/** Set sign width (mm) */
	public void setSignWidth(int w) {
		signWidth = w;
	}

	/** Get sign width (mm) */
	public int getSignWidth() {
		return signWidth;
	}

	/** Horizontal border (mm) */
	protected transient int horizontalBorder;

	/** Set horizontal border (mm) */
	public void setHorizontalBorder(int b) {
		horizontalBorder = b;
	}

	/** Get horizontal border (mm) */
	public int getHorizontalBorder() {
		return horizontalBorder;
	}

	/** Vertical border (mm) */
	protected transient int verticalBorder;

	/** Set vertical border (mm) */
	public void setVerticalBorder(int b) {
		verticalBorder = b;
	}

	/** Get vertical border (mm) */
	public int getVerticalBorder() {
		return verticalBorder;
	}

	/** Sign legend string */
	protected transient String legend;

	/** Set sign legend */
	public void setSignLegend(String l) {
		legend = l;
	}

	/** Get sign legend */
	public String getSignLegend() {
		return legend;
	}

	/** Beacon type description */
	protected transient String beaconType;

	/** Set beacon type description */
	public void setBeaconType(String t) {
		beaconType = t;
	}

	/** Get beacon type description */
	public String getBeaconType() {
		return beaconType;
	}

	/** Sign technology description */
	protected transient String signTechnology;

	/** Set sign technology description */
	public void setSignTechnology(String t) {
		signTechnology = t;
	}

	/** Get sign technology description */
	public String getSignTechnology() {
		return signTechnology;
	}

	/** Character height (pixels) */
	protected transient int characterHeightPixels;

	/** Set character height (pixels) */
	public void setCharacterHeightPixels(int h) {
		characterHeightPixels = h;
	}

	/** Get character height (pixels) */
	public int getCharacterHeightPixels() {
		return characterHeightPixels;
	}

	/** Get the optimal line height (pixels) */
	public int getLineHeightPixels() {
		int h = characterHeightPixels;	// Avoid race
		if(h > 0)
			return h;
		int s = signHeightPixels;	// Avoid race
		int w = characterWidthPixels;	// Avoid race
		for(int i = s; i > 0; i--) {
			int ls = calculateLineSpacing(s, i);
			if(ls != INVALID_LINE_SPACING) {
				if(lookupFont(i, w, ls) != null)
					return i;
			}
		}
		// No optimal height found; just grab a font...
		FontImpl font = lookupFont(0, w, 0);
		if(font != null)
			return font.getHeight();
		else
			return DEFAULT_CHARACTER_HEIGHT;
	}

	/** Character width (pixels; 0 means variable) */
	protected transient int characterWidthPixels;

	/** Set character width (pixels) */
	public void setCharacterWidthPixels(int w) {
		characterWidthPixels = w;
	}

	/** Get character width (pixels) */
	public int getCharacterWidthPixels() {
		return characterWidthPixels;
	}

	/** Does the sign have proportional fonts? */
	public boolean hasProportionalFonts() {
		return characterWidthPixels == 0;
	}

	/** Sign height (pixels) */
	protected transient int signHeightPixels;

	/** Set sign height (pixels) */
	public void setSignHeightPixels(int h) {
		signHeightPixels = h;
	}

	/** Get sign height (pixels) */
	public int getSignHeightPixels() {
		return signHeightPixels;
	}

	/** Sign width in pixels */
	protected transient int signWidthPixels;

	/** Set sign width (pixels) */
	public void setSignWidthPixels(int w) {
		signWidthPixels = w;
	}

	/** Get sign width (pixels) */
	public int getSignWidthPixels() {
		return signWidthPixels;
	}

	/** Horizontal pitch (mm) */
	protected transient int horizontalPitch;

	/** Set horizontal pitch (mm) */
	public void setHorizontalPitch(int p) {
		horizontalPitch = p;
	}

	/** Get horizontal pitch (mm) */
	public int getHorizontalPitch() {
		return horizontalPitch;
	}

	/** Get an estimate of the horizontal pitch (mm) */
	public int getEstimatedHorizontalPitch() {
		// FIXME: this is fragile
		if(signType.contains("Full") || signType.contains("Line")) {
			float w = signWidth - horizontalBorder / 2.0f;
			int wp = signWidthPixels;	// Avoid race
			if(w > 0 && wp > 0)
				return Math.round(w / wp);
		}
		return DEFAULT_PITCH;
	}

	/** Vertical pitch (mm) */
	protected transient int verticalPitch;

	/** Set vertical pitch (mm) */
	public void setVerticalPitch(int p) {
		verticalPitch = p;
	}

	/** Get vertical pitch (mm) */
	public int getVerticalPitch() {
		return verticalPitch;
	}

	/** Get an estimate of the vertical pitch (mm) */
	public int getEstimatedVerticalPitch() {
		// FIXME: this is fragile
		if(signType.contains("Full")) {
			float h = signHeight - verticalBorder / 2.0f;
			int hp = signHeightPixels;	// Avoid race
			if(h > 0 && hp > 0)
				return Math.round(h / hp);
		}
		return DEFAULT_PITCH;
	}

	/** Maximum photocell level */
	protected transient int maxPhotocellLevel;

	/** Set the maximum photocell level */
	public void setMaxPhotocellLevel(int l) {
		maxPhotocellLevel = l;
	}

	/** Get the maximum photocell level */
	public int getMaxPhotocellLevel() {
		return maxPhotocellLevel;
	}

	/** Photocell level */
	protected transient int photocellLevel;

	/** Set the current photocell level */
	public void setPhotocellLevel(int l) {
		photocellLevel = l;
	}

	/** Get the current photocell level */
	public int getPhotocellLevel() {
		return photocellLevel;
	}

	/** Number of supported brightness levels */
	protected transient int brightnessLevels;

	/** Set the number of supported brightness levels */
	public void setBrightnessLevels(int l) {
		brightnessLevels = l;
	}

	/** Get the number of supported brightness levels */
	public int getBrightnessLevels() {
		return brightnessLevels;
	}

	/** Current brightness level */
	protected transient int brightnessLevel;

	/** Set the current brightness level */
	public void setBrightnessLevel(int l) {
		brightnessLevel = l;
	}

	/** Get the current brightness level */
	public int getBrightnessLevel() {
		return brightnessLevel;
	}

	/** Current light output (percentage) of the sign */
	protected transient int lightOutput;

	/** Set the light output of the sign */
	public void setLightOutput(int l) {
		lightOutput = l;
	}

	/** Get the light output of the sign */
	public int getLightOutput() {
		return lightOutput;
	}

	/** Brightness table */
	protected transient int[] brightnessTable;

	/** Set the brightness table */
	public void setBrightnessTable(int[] t) {
		brightnessTable = t;
	}

	/** Get the brightness table */
	public int[] getBrightnessTable() {
		return brightnessTable;
	}

	/** Manual brightness control (on or off) */
	protected transient boolean manualBrightness;

	/** Set manual brightness control (on or off) */
	public void setManualBrightness(boolean m) {
		manualBrightness = m;
	}

	/** Get manual brightness control (on or off) */
	public boolean isManualBrightness() {
		return manualBrightness;
	}

	/** Send brightness level command to sign */
	protected void _setBrightnessLevel(Integer l) {
		DMSPoller p = getDMSPoller();
		if(p != null)
			p.setBrightnessLevel(this, l);
	}

	/** Activate/deactivate manual brightness */
	public void activateManualBrightness(boolean m) {
		if(m == manualBrightness)
			return;
		if(m)
			_setBrightnessLevel(brightnessLevel);
		else
			_setBrightnessLevel(null);
	}

	/** Set manual brightness level */
	public void setManualBrightness(int l) {
		if(manualBrightness) {
			if(l != brightnessLevel)
				_setBrightnessLevel(l);
		}
	}

	/** Activate a pixel test */
	public void testPixels() {
		DMSPoller p = getDMSPoller();
		if(p != null)
			p.testPixels(this);
	}

	/** Activate a lamp test */
	public void testLamps() {
		DMSPoller p = getDMSPoller();
		if(p != null)
			p.testLamps(this);
	}

	/** Lamp status */
	protected transient String lamp_status;

	/** Set the lamp status */
	public void setLampStatus(String l) {
		lamp_status = l;
	}

	/** Get the lamp status */
	public String getLampStatus() {
		return lamp_status;
	}

	/** Activate a fan test */
	public void testFans() {
		DMSPoller p = getDMSPoller();
		if(p != null)
			p.testFans(this);
	}

	/** Fan status */
	protected transient String fan_status;

	/** Set the fan status */
	public void setFanStatus(String f) {
		fan_status = f;
	}

	/** Get the fan status */
	public String getFanStatus() {
		return fan_status;
	}

	/** Power supply status table */
	protected transient StatusTable power_table;

	/** Set the power supply status table */
	public void setPowerSupplyTable(StatusTable t) {
		power_table = t;
	}

	/** Get the power supply status table */
	public StatusTable getPowerSupplyTable() {
		return power_table;
	}

	/** Sign face heat tape status */
	protected transient String heat_tape;

	/** Set sign face heat tape status */
	public void setHeatTapeStatus(String h) {
		heat_tape = h;
	}

	/** Get sign face heat tape status */
	public String getHeatTapeStatus() {
		return heat_tape;
	}

	/** Set the time (in minutes) to heat the sign housing */
	public void setHousingHeatTime(int minutes) {
		// FIXME
	}

	/** Get the remaining housing heat time (in minutes) */
	public int getHousingHeatTime() {
		// FIXME
		return 0;
	}

	/** Start a Ledstar pixel configuration operation */
	protected void startLedstarPixel() {
		DMSPoller p = getDMSPoller();
		if(p != null) {
			p.setLedstarPixel(this, ldcPotBase, pixelCurrentLow,
				pixelCurrentHigh, badPixelLimit);
		}
	}

	/** LDC pot base (Ledstar-specific value) */
	protected transient int ldcPotBase;

	/** Set the LDC pot base */
	public void setLdcPotBase(int base, boolean send) {
		if(base == ldcPotBase)
			return;
		ldcPotBase = base;
		if(send)
			startLedstarPixel();
	}

	/** Set the LDC pot base */
	public void setLdcPotBase(int base) {
		setLdcPotBase(base, true);
	}

	/** Get the LDC pot base */
	public int getLdcPotBase() {
		return ldcPotBase;
	}

	/** Pixel low current threshold (Ledstar-specific value) */
	protected transient int pixelCurrentLow;

	/** Set the pixel low curent threshold */
	public void setPixelCurrentLow(int low, boolean send) {
		if(low == pixelCurrentLow)
			return;
		pixelCurrentLow = low;
		if(send)
			startLedstarPixel();
	}

	/** Set the pixel low curent threshold */
	public void setPixelCurrentLow(int low) {
		setPixelCurrentLow(low, true);
	}

	/** Get the pixel low current threshold */
	public int getPixelCurrentLow() {
		return pixelCurrentLow;
	}

	/** Pixel high current threshold (Ledstar-specific value) */
	protected transient int pixelCurrentHigh;

	/** Set the pixel high curent threshold */
	public void setPixelCurrentHigh(int high, boolean send) {
		if(high == pixelCurrentHigh)
			return;
		pixelCurrentHigh = high;
		if(send)
			startLedstarPixel();
	}

	/** Set the pixel high curent threshold */
	public void setPixelCurrentHigh(int high) {
		setPixelCurrentHigh(high, true);
	}

	/** Get the pixel high current threshold */
	public int getPixelCurrentHigh() {
		return pixelCurrentHigh;
	}

	/** Bad pixel limit (Ledstar-specific value) */
	protected transient int badPixelLimit;

	/** Set the bad pixel limit */
	public void setBadPixelLimit(int bad, boolean send) {
		if(bad == badPixelLimit)
			return;
		badPixelLimit = bad;
		if(send)
			startLedstarPixel();
	}

	/** Set the bad pixel limit */
	public void setBadPixelLimit(int bad) {
		setBadPixelLimit(bad, true);
	}

	/** Get the bad pixel limit */
	public int getBadPixelLimit() {
		return badPixelLimit;
	}
}
