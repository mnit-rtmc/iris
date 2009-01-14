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
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.comm.DMSPoller;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.event.EventType;
import us.mn.state.dot.tms.event.SignStatusEvent;

/**
 * Dynamic Message Sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSImpl extends Device2Impl implements DMS {

	/** Special value to indicate an invalid line spacing */
	static protected final int INVALID_LINE_SPACING = -1;

	/** Calculate the maximum trip minute to display on the sign */
	static protected int maximumTripMinutes(float miles) {
		float hours = miles /
			SystemAttributeHelper.getTravelTimeMinMPH();
		return Math.round(hours * 60);
	}

	/** Round up to the next 5 minutes */
	static protected int roundUp5Min(int min) {
		return ((min - 1) / 5 + 1) * 5;
	}

	/** Lookup a station */
	static protected StationImpl lookupStation(String sid) {
		return (StationImpl)namespace.lookupObject(Station.SONAR_TYPE,
			sid);
	}

	/** DMS / timing plan table mapping */
	static protected TableMapping mapping;

	/** Load all the DMS */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading DMS...");
		namespace.registerType(SONAR_TYPE, DMSImpl.class);
		mapping = new TableMapping(store, "dms", "timing_plan");
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"travel, camera FROM " + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new DMSImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6),	// travel
					row.getString(7)	// camera
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("travel", travel);
		map.put("camera", camera);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new DMS with a string name */
	public DMSImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		MainServer.server.createObject(g);
		geo_loc = g;
	}

	/** Create a dynamic message sign */
	protected DMSImpl(String n, GeoLocImpl loc, ControllerImpl c,
		int p, String nt, String t, Camera cam)
	{
		super(n, c, p, nt);
		geo_loc = loc;
		travel = t;
		camera = cam;
	}

	/** Create a dynamic message sign */
	protected DMSImpl(Namespace ns, String n, String loc, String c,
		int p, String nt, String t, String cam)
	{
		this(n, (GeoLocImpl)ns.lookupObject(GeoLoc.SONAR_TYPE, loc),
			(ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE,
			c), p, nt, t,
			(Camera)ns.lookupObject(Camera.SONAR_TYPE, cam));
	}

	/** Initialize the transient state */
	public void initTransients() {
		super.initTransients();
		TreeSet<TimingPlanImpl> p = new TreeSet<TimingPlanImpl>();
		for(Object o: mapping.lookup(SONAR_TYPE, this)) {
			p.add((TimingPlanImpl)namespace.lookupObject(
				TimingPlan.SONAR_TYPE, (String)o));
		}
		plans = p.toArray(new TimingPlanImpl[0]);
		messageCurrent = createBlankMessage(
			DMSMessagePriority.SCHEDULED);
		s_routes = new HashMap<String, Route>();
	}

	/** Create a blank message for the sign */
	protected SignMessage createBlankMessage(DMSMessagePriority p) {
		String[] bitmaps = new String[] {
			Base64.encode(new byte[0])
		};
		return new SignMessageImpl("", bitmaps, p);
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		store.destroy(geo_loc);
	}

	/** Set the controller to which this DMS is assigned */
	public void setController(Controller c) {
		super.setController(c);
		if(c != null)
			setConfigure(false);
	}

	/** Configure flag */
	protected boolean configure;

	/** Set the configure flag */
	public void setConfigure(boolean c) {
		if(c && !configure) {
			DMSPoller p = getDMSPoller();
			if(p != null) {
				// NOTE: this avoids a stack overflow with
				// DMSOperation.cleanup()
				configure = true;
				p.sendRequest(this,
					SignRequest.QUERY_CONFIGURATION);
			}
		}
		configure = c;
	}

	/** Device location */
	protected GeoLocImpl geo_loc;

	/** Get the device location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Travel time message template */
	protected String travel;

	/** Set the travel time message template */
	public void setTravel(String t) {
		travel = t;
		s_routes.clear();
	}

	/** Set the travel time message template */
	public void doSetTravel(String t) throws TMSException {
		if(t.equals(travel))
			return;
		if(!MultiString.isValid(t))
			throw new ChangeVetoException("Invalid travel: " + t);
		store.update(this, "travel", t);
		setTravel(t);
	}

	/** Get the travel time message template */
	public String getTravel() {
		return travel;
	}

	/** Array of timing plans for this sign */
	protected TimingPlanImpl[] plans = new TimingPlanImpl[0];

	/** Set all current timing plans which affect this sign */
	public void setTimingPlans(TimingPlan[] p) {
		// NOTE: this is needed for DMS interface --
		//       doSetTimingPlans will always be used
	}

	/** Set all current timing plans which affect this sign */
	public void doSetTimingPlans(TimingPlan[] p) throws TMSException {
		TreeSet<Storable> pset = new TreeSet<Storable>();
		for(TimingPlan plan: p) {
			if(plan instanceof TimingPlanImpl)
				pset.add((TimingPlanImpl)plan);
			else
				throw new ChangeVetoException("Invalid plan");
		}
		mapping.update(SONAR_TYPE, this, pset);
		plans = pset.toArray(new TimingPlanImpl[0]);
	}

	/** Get an array of all timing plans which affect this sign */
	public TimingPlan[] getTimingPlans() {
		TimingPlanImpl[] plans = this.plans;	// Avoid race
		TimingPlan[] p = new TimingPlan[plans.length];
		for(int i = 0; i < plans.length; i++)
			p[i] = plans[i];
		return p;
	}

	/** Camera from which this can be seen */
	protected Camera camera;

	/** Set the verification camera */
	public void setCamera(Camera c) {
		camera = c;
	}

	/** Set the verification camera */
	public void doSetCamera(Camera c) throws TMSException {
		if(c == camera)
			return;
		store.update(this, "camera", c);
		setCamera(c);
	}

	/** Get verification camera */
	public Camera getCamera() {
		return camera;
	}

	/** Make (manufacturer) */
	protected transient String make;

	/** Set the make */
	public void setMake(String m) {
		if(!m.equals(make)) {
			make = m;
			notifyAttribute("make");
		}
	}

	/** Get the make */
	public String getMake() {
		return make;
	}

	/** Model */
	protected transient String model;

	/** Set the model */
	public void setModel(String m) {
		if(!m.equals(model)) {
			model = m;
			notifyAttribute("model");
		}
	}

	/** Get the model */
	public String getModel() {
		return model;
	}

	/** Software version */
	protected transient String version;

	/** Set the version */
	public void setVersion(String v) {
		if(!v.equals(version)) {
			version = v;
			notifyAttribute("version");
			ControllerImpl c = (ControllerImpl)getController();
			if(c != null)
				c.setVersion(version);
		}
	}

	/** Get the version */
	public String getVersion() {
		return version;
	}

	/** Sign access description */
	protected transient String signAccess;

	/** Set sign access description */
	public void setSignAccess(String a) {
		if(!a.equals(signAccess)) {
			signAccess = a;
			notifyAttribute("signAccess");
		}
	}

	/** Get sign access description */
	public String getSignAccess() {
		return signAccess;
	}

	/** Sign type enum value */
	protected transient DMSType dms_type = DMSType.VMS_CHAR;

	/** Set sign type */
	public void setDmsType(DMSType t) {
		if(t != dms_type) {
			dms_type = t;
			notifyAttribute("dmsType");
		}
	}

	/** Get sign type as an int (via enum) */
	public int getDmsType() {
		return dms_type.ordinal();
	}

	/** Sign legend string */
	protected transient String legend;

	/** Set sign legend */
	public void setLegend(String l) {
		if(!l.equals(legend)) {
			legend = l;
			notifyAttribute("legend");
		}
	}

	/** Get sign legend */
	public String getLegend() {
		return legend;
	}

	/** Beacon type description */
	protected transient String beaconType;

	/** Set beacon type description */
	public void setBeaconType(String t) {
		if(!t.equals(beaconType)) {
			beaconType = t;
			notifyAttribute("beaconType");
		}
	}

	/** Get beacon type description */
	public String getBeaconType() {
		return beaconType;
	}

	/** Sign technology description */
	protected transient String technology;

	/** Set sign technology description */
	public void setTechnology(String t) {
		if(!t.equals(technology)) {
			technology = t;
			notifyAttribute("technology");
		}
	}

	/** Get sign technology description */
	public String getTechnology() {
		return technology;
	}

	/** Height of sign face (mm) */
	protected transient Integer faceHeight;

	/** Set height of sign face (mm) */
	public void setFaceHeight(Integer h) {
		if(!h.equals(faceHeight)) {
			faceHeight = h;
			notifyAttribute("faceHeight");
		}
	}

	/** Get height of the sign face (mm) */
	public Integer getFaceHeight() {
		return faceHeight;
	}

	/** Width of the sign face (mm) */
	protected transient Integer faceWidth;

	/** Set width of sign face (mm) */
	public void setFaceWidth(Integer w) {
		if(!w.equals(faceWidth)) {
			faceWidth = w;
			notifyAttribute("faceWidth");
		}
	}

	/** Get width of the sign face (mm) */
	public Integer getFaceWidth() {
		return faceWidth;
	}

	/** Horizontal border (mm) */
	protected transient Integer horizontalBorder;

	/** Set horizontal border (mm) */
	public void setHorizontalBorder(Integer b) {
		if(!b.equals(horizontalBorder)) {
			horizontalBorder = b;
			notifyAttribute("horizontalBorder");
		}
	}

	/** Get horizontal border (mm) */
	public Integer getHorizontalBorder() {
		return horizontalBorder;
	}

	/** Vertical border (mm) */
	protected transient Integer verticalBorder;

	/** Set vertical border (mm) */
	public void setVerticalBorder(Integer b) {
		if(!b.equals(verticalBorder)) {
			verticalBorder = b;
			notifyAttribute("verticalBorder");
		}
	}

	/** Get vertical border (mm) */
	public Integer getVerticalBorder() {
		return verticalBorder;
	}

	/** Horizontal pitch (mm) */
	protected transient Integer horizontalPitch;

	/** Set horizontal pitch (mm) */
	public void setHorizontalPitch(Integer p) {
		if(!p.equals(horizontalPitch)) {
			horizontalPitch = p;
			notifyAttribute("horizontalPitch");
		}
	}

	/** Get horizontal pitch (mm) */
	public Integer getHorizontalPitch() {
		return horizontalPitch;
	}

	/** Vertical pitch (mm) */
	protected transient Integer verticalPitch;

	/** Set vertical pitch (mm) */
	public void setVerticalPitch(Integer p) {
		if(!p.equals(verticalPitch)) {
			verticalPitch = p;
			notifyAttribute("verticalPitch");
		}
	}

	/** Get vertical pitch (mm) */
	public Integer getVerticalPitch() {
		return verticalPitch;
	}

	/** Sign height (pixels) */
	protected transient Integer heightPixels;

	/** Set sign height (pixels) */
	public void setHeightPixels(Integer h) {
		if(!h.equals(heightPixels)) {
			heightPixels = h;
			// FIXME: update bitmap graphics plus stuck on/off
			notifyAttribute("heightPixels");
		}
	}

	/** Get sign height (pixels) */
	public Integer getHeightPixels() {
		return heightPixels;
	}

	/** Sign width in pixels */
	protected transient Integer widthPixels;

	/** Set sign width (pixels) */
	public void setWidthPixels(Integer w) {
		if(!w.equals(widthPixels)) {
			widthPixels = w;
			// FIXME: update bitmap graphics plus stuck on/off
			notifyAttribute("widthPixels");
		}
	}

	/** Get sign width (pixels) */
	public Integer getWidthPixels() {
		return widthPixels;
	}

	/** Character height (pixels) */
	protected transient Integer charHeightPixels;

	/** Set character height (pixels) */
	public void setCharHeightPixels(Integer h) {
		if(!h.equals(charHeightPixels)) {
			charHeightPixels = h;
			notifyAttribute("charHeightPixels");
		}
	}

	/** Get character height (pixels) */
	public Integer getCharHeightPixels() {
		return charHeightPixels;
	}

	/** Character width (pixels; 0 means variable) */
	protected transient Integer charWidthPixels;

	/** Set character width (pixels) */
	public void setCharWidthPixels(Integer w) {
		if(!w.equals(charWidthPixels)) {
			charWidthPixels = w;
			notifyAttribute("charWidthPixels");
		}
	}

	/** Get character width (pixels) */
	public Integer getCharWidthPixels() {
		return charWidthPixels;
	}

	/** Does the sign have proportional fonts? */
	public boolean hasProportionalFonts() {
		Integer w = charWidthPixels;
		return w != null && w == 0;
	}

	/** Minimum cabinet temperature */
	protected transient Integer minCabinetTemp;

	/** Set the minimum cabinet temperature */
	public void setMinCabinetTemp(Integer t) {
		if(!t.equals(minCabinetTemp)) {
			minCabinetTemp = t;
			notifyAttribute("minCabinetTemp");
		}
	}

	/** Get the minimum cabinet temperature */
	public Integer getMinCabinetTemp() {
		return minCabinetTemp;
	}

	/** Maximum cabinet temperature */
	protected transient Integer maxCabinetTemp;

	/** Set the maximum cabinet temperature */
	public void setMaxCabinetTemp(Integer t) {
		if(!t.equals(maxCabinetTemp)) {
			maxCabinetTemp = t;
			notifyAttribute("maxCabinetTemp");
		}
	}

	/** Get the maximum cabinet temperature */
	public Integer getMaxCabinetTemp() {
		return maxCabinetTemp;
	}

	/** Minimum ambient temperature */
	protected transient Integer minAmbientTemp;

	/** Set the minimum ambient temperature */
	public void setMinAmbientTemp(Integer t) {
		if(!t.equals(minAmbientTemp)) {
			minAmbientTemp = t;
			notifyAttribute("minAmbientTemp");
		}
	}

	/** Get the minimum ambient temperature */
	public Integer getMinAmbientTemp() {
		return minAmbientTemp;
	}

	/** Maximum ambient temperature */
	protected transient Integer maxAmbientTemp;

	/** Set the maximum ambient temperature */
	public void setMaxAmbientTemp(Integer t) {
		if(!t.equals(maxAmbientTemp)) {
			maxAmbientTemp = t;
			notifyAttribute("maxAmbientTemp");
		}
	}

	/** Get the maximum ambient temperature */
	public Integer getMaxAmbientTemp() {
		return maxAmbientTemp;
	}

	/** Minimum housing temperature */
	protected transient Integer minHousingTemp;

	/** Set the minimum housing temperature */
	public void setMinHousingTemp(Integer t) {
		if(!t.equals(minHousingTemp)) {
			minHousingTemp = t;
			notifyAttribute("minHousingTemp");
		}
	}

	/** Get the minimum housing temperature */
	public Integer getMinHousingTemp() {
		return minHousingTemp;
	}

	/** Maximum housing temperature */
	protected transient Integer maxHousingTemp;

	/** Set the maximum housing temperature */
	public void setMaxHousingTemp(Integer t) {
		if(!t.equals(maxHousingTemp)) {
			maxHousingTemp = t;
			notifyAttribute("maxHousingTemp");
		}
	}

	/** Get the maximum housing temperature */
	public Integer getMaxHousingTemp() {
		return maxHousingTemp;
	}

	/** Current light output (percentage) of the sign */
	protected transient Integer lightOutput;

	/** Set the light output of the sign (percentage) */
	public void setLightOutput(Integer l) {
		if(!l.equals(lightOutput)) {
			lightOutput = l;
			notifyAttribute("lightOutput");
		}
	}

	/** Get the light output of the sign (percentage) */
	public Integer getLightOutput() {
		return lightOutput;
	}

	/** Pixel status.  This is an array of two Base64-encoded bitmaps.
	 * The first indicates stuck-off pixels, the second stuck-on pixels. */
	protected transient String[] pixelStatus;

	/** Set the pixel status array */
	public void setPixelStatus(String[] p) {
		pixelStatus = p;
		notifyAttribute("pixelStatus");
	}

	/** Get the pixel status array */
	public String[] getPixelStatus() {
		return pixelStatus;
	}

	/** Lamp status.  This is an array of two Base64-encoded bitmaps.
	 * The first indicates stuck-off lamps, the second stuck-on lamps. */
	protected transient String[] lampStatus;

	/** Set the lamp status */
	public void setLampStatus(String[] l) {
		lampStatus = l;
		notifyAttribute("lampStatus");
	}

	/** Get the lamp status */
	public String[] getLampStatus() {
		return lampStatus;
	}

	/** Request a sign operation (query message, test pixels, etc.) */
	public void setSignRequest(int r) {
		SignRequest sr = SignRequest.fromOrdinal(r);
		DMSPoller p = getDMSPoller();
		if(p != null)
			p.sendRequest(this, sr);
	}

	/** User note */
	protected transient String userNote;

	/** Set the user note */
	public void setUserNote(String n) {
		if(!n.equals(userNote)) {
			userNote = n;
			notifyAttribute("userNote");
		}
	}

	/** Get the user note */
	public String getUserNote() {
		return userNote;
	}

	/** Next message to be displayed */
	protected transient SignMessage messageNext;

	/** Set the next sign message */
	public void setMessageNext(SignMessage m) {
		messageNext = m;
	}

	/** Set the next sign message */
	public void doSetMessageNext(SignMessage m) throws TMSException {
		// FIXME: needs to be synchronized, since this can be called
		// by server and clients
		final DMSPoller p = getDMSPoller();
		if(p == null)
			throw new ChangeVetoException("No active poller");
		if(!MultiString.isValid(m.getMulti())) {
			throw new ChangeVetoException("Invalid message: " +
				m.getMulti());
		}
		ControllerImpl c = (ControllerImpl)getController();
		if(c != null) {
			String error = c.getError();
			if(error.length() > 0) {
				throw new ChangeVetoException(
					"Controller error: " + error);
			}
		}
		if(!checkPriority(m.getActivationPriority()))
			throw new ChangeVetoException("Priority too low");
		// FIXME: only blank sign if activation priority equals
		// current runtime priority (unless priority is CLEAR).
		validateBitmaps(m);
		p.sendMessage(this, m);
		setMessageNext(m);
	}

	/** Validate the message bitmaps */
	protected void validateBitmaps(SignMessage m)
		throws ChangeVetoException
	{
		try {
			String[] pixels = pixelStatus;	// Avoid races
			if(pixels != null && pixels.length == 2)
				validateBitmaps(m, pixels);
		}
		catch(IOException e) {
			throw new ChangeVetoException("Base64 decode error");
		}
		catch(IndexOutOfBoundsException e) {
			throw new ChangeVetoException(e.getMessage());
		}
	}

	/** Validate the message bitmaps */
	protected void validateBitmaps(SignMessage m, String[] pixels)
		throws IOException, ChangeVetoException
	{
		int off_limit = SystemAttributeHelper.getDmsPixelOffLimit();
		int on_limit = SystemAttributeHelper.getDmsPixelOnLimit();
		BitmapGraphic bitmap = createBlankBitmap();
		BitmapGraphic stuckOff = createBlankBitmap();
		BitmapGraphic stuckOn = createBlankBitmap();
		stuckOff.setBitmap(Base64.decode(pixels[STUCK_OFF_BITMAP]));
		stuckOn.setBitmap(Base64.decode(pixels[STUCK_ON_BITMAP]));
		for(String b64: m.getBitmaps()) {
			byte[] b = Base64.decode(b64);
			bitmap.setBitmap(b);
			bitmap.union(stuckOff);
			int n_lit = bitmap.getLitCount();
			if(n_lit > off_limit) {
				throw new ChangeVetoException(
					"Too many stuck off pixels: " + n_lit);
			}
			bitmap.setBitmap(b);
			bitmap.outline();
			bitmap.union(stuckOn);
			n_lit = bitmap.getLitCount();
			if(n_lit > on_limit) {
				throw new ChangeVetoException(
					"Too many stuck on pixels: " + n_lit);
			}
		}
	}

	/** Create a blank bitmap */
	protected BitmapGraphic createBlankBitmap()
		throws ChangeVetoException
	{
		Integer w = widthPixels;	// Avoid race
		Integer h = heightPixels;	// Avoid race
		if(w != null && h != null)
			return new BitmapGraphic(w, h);
		else
			throw new ChangeVetoException("Width/height is null");
	}

	/** Check if a message has priority over existing messages */
	public boolean checkPriority(int ap) {
		return checkCurrentPriority(ap) && checkNextPriority(ap);
	}

	/** Check if a message has priority over "current" message */
	protected boolean checkCurrentPriority(int ap) {
		return ap >= messageCurrent.getRunTimePriority();
	}

	/** Check if a message has priority over "next" message */
	protected boolean checkNextPriority(int ap) {
		SignMessage n = messageNext;
		return n == null || ap >= n.getRunTimePriority();
	}

	/** Send a sign message creates by IRIS server */
	public void sendMessage(SignMessage m) throws TMSException {
		try {
			doSetMessageNext(m);
			notifyAttribute("messageNext");
		}
		catch(TMSException e) {
			// FIXME: destroy SignMessage
			throw e;
		}
	}

	/** Get the next (in process) sign message */
	public SignMessage getMessageNext() {
		return messageNext;
	}

	/** Current message */
	protected transient SignMessage messageCurrent;

	/** Set the current message */
	public void setMessageCurrent(SignMessage m) {
		if(m.equals(messageCurrent))
			return;
		logMessage(m);
		messageCurrent = m;
		notifyAttribute("messageCurrent");
		// FIXME: destroy the previous message if no other signs are
		// using it
	}

	/** Get the current messasge.
	 * @return Currently active message (cannot be null) */
	public SignMessage getMessageCurrent() {
		return messageCurrent;
	}

	/** Log a message */
	protected void logMessage(SignMessage m) {
		EventType et = EventType.DMS_DEPLOYED;
		String text = m.getMulti();
		if(((SignMessageImpl)m).isBlank()) {
			et = EventType.DMS_CLEARED;
			text = null;
		}
		User user = m.getOwner();
		String owner = null;
		if(user != null)
			owner = user.getName();
		SignStatusEvent ev = new SignStatusEvent(et, name, text, owner);
		try {
			ev.doStore();
		}
		catch(TMSException e) {
			e.printStackTrace();
		}
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

	/** LDC pot base (Ledstar-specific value) */
	protected transient Integer ldcPotBase;

	/** Set the LDC pot base */
	public void setLdcPotBase(Integer base) {
		if(!base.equals(ldcPotBase)) {
			ldcPotBase = base;
			notifyAttribute("ldcPotBase");
		}
	}

	/** Get the LDC pot base */
	public Integer getLdcPotBase() {
		return ldcPotBase;
	}

	/** Pixel low current threshold (Ledstar-specific value) */
	protected transient Integer pixelCurrentLow;

	/** Set the pixel low curent threshold */
	public void setPixelCurrentLow(Integer low) {
		if(!low.equals(pixelCurrentLow)) {
			pixelCurrentLow = low;
			notifyAttribute("pixelCurrentLow");
		}
	}

	/** Get the pixel low current threshold */
	public Integer getPixelCurrentLow() {
		return pixelCurrentLow;
	}

	/** Pixel high current threshold (Ledstar-specific value) */
	protected transient Integer pixelCurrentHigh;

	/** Set the pixel high curent threshold */
	public void setPixelCurrentHigh(Integer high) {
		if(!high.equals(pixelCurrentHigh)) {
			pixelCurrentHigh = high;
			notifyAttribute("pixelCurrentHigh");
		}
	}

	/** Get the pixel high current threshold */
	public Integer getPixelCurrentHigh() {
		return pixelCurrentHigh;
	}

	/** Sign face heat tape status */
	protected transient String heatTapeStatus;

	/** Set sign face heat tape status */
	public void setHeatTapeStatus(String h) {
		if(!h.equals(heatTapeStatus)) {
			heatTapeStatus = h;
			notifyAttribute("heatTapeStatus");
		}
	}

	/** Get sign face heat tape status */
	public String getHeatTapeStatus() {
		return heatTapeStatus;
	}

	/** Power supply status table */
	protected transient String[] powerStatus;

	/** Set the power supply status table */
	public void setPowerStatus(String[] t) {
		powerStatus = t;
		notifyAttribute("powerStatus");
	}

	/** Get the power supply status table */
	public String[] getPowerStatus() {
		return powerStatus;
	}

	/** Update the travel times for this sign */
	public void updateTravelTimes() {
		if(isWithin())
			updateTravelTime();
		else {
			s_routes.clear();
			sendTravelTime("");
		}
	}

	/** Check if a timing plan is operating */
	protected boolean isWithin() {
		// FIXME: this should use validate and isOperating
		TimingPlanImpl[] plans = this.plans;	// Avoid race
		for(TimingPlanImpl plan: plans) {
			if(plan.getActive()) {
				TimingPlanState s = plan.getState();
				if(s.isWithin())
					return true;
			}
		}
		return false;
	}

	/** Update the travel times for this sign */
	protected void updateTravelTime() {
		try {
			sendTravelTime(composeTravelTimeMessage());
		}
		catch(InvalidMessageException e) {
			if(RouteBuilder.TRAVEL_LOG.isOpen())
				RouteBuilder.TRAVEL_LOG.log(e.getMessage());
			sendTravelTime("");
		}
	}

	/** Send a new travel time message */
	protected void sendTravelTime(String t) {
		if(!checkPriority(DMSMessagePriority.TRAVEL_TIME.ordinal()))
			return;
		// FIXME: bail out if t is blank and the current message is not
		//        a travel time
		SignMessage m = createMessage(t,DMSMessagePriority.TRAVEL_TIME);
		try {
			sendMessage(m);
		}
		catch(TMSException e) {
			RouteBuilder.TRAVEL_LOG.log(e.getMessage());
		}
	}

	/** Create a message for the sign */
	public SignMessage createMessage(String m, DMSMessagePriority p) {
		Integer w = widthPixels;
		Integer h = heightPixels;
		Integer cw = charWidthPixels;
		Integer ch = charHeightPixels;
		if(w == null)
			w = 0;
		if(h == null)
			h = 0;
		if(cw == null)
			cw = 0;
		if(ch == null)
			ch = 0;
		PixelMapBuilder builder = new PixelMapBuilder(namespace, w, h,
			cw, ch);
		MultiString multi = new MultiString(m);
		multi.parse(builder);
		BitmapGraphic[] bmaps = builder.getPixmaps();
		String[] bitmaps = new String[bmaps.length];
		for(int i = 0; i < bmaps.length; i++)
			bitmaps[i] = Base64.encode(bmaps[i].getBitmap());
		return new SignMessageImpl(m, bitmaps, p);
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
					throw new InvalidMessageException(name +
						": NO ROUTE TO " + sid);
				}
				boolean final_dest = isFinalDest(r);
				int m = calculateTravelTime(r, final_dest);
				int slow = maximumTripMinutes(r.getLength());
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

	/** Mapping of station IDs to routes */
	protected transient HashMap<String, Route> s_routes;

	/** Lookup a route by station ID */
	protected Route lookupRoute(String sid) {
		if(!s_routes.containsKey(sid))
			s_routes.put(sid, createRoute(sid));
		return s_routes.get(sid);
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
				name + ": " + e.getMessage());
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

	/** Create one route to a travel time destination */
	protected Route createRoute(StationImpl s) {
		GeoLoc dest = s.getR_Node().getGeoLoc();
		RouteBuilder builder = new RouteBuilder(getName(),
			TMSImpl.corridors);
		SortedSet<Route> routes = builder.findRoutes(geo_loc, dest);
		if(routes.size() > 0)
			return routes.first();
		else
			return null;
	}

	/** Create one route to a travel time destination */
	protected Route createRoute(String sid) {
		StationImpl s = lookupStation(sid);
		if(s != null)
			return createRoute(s);
		else
			return null;
	}
}
