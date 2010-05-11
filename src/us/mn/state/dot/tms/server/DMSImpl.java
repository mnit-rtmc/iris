/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
 * Copyright (C) 2010 AHMCT, University of California
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
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.event.SignStatusEvent;
import us.mn.state.dot.tms.kml.Kml;
import us.mn.state.dot.tms.kml.KmlColor;
import us.mn.state.dot.tms.kml.KmlColorImpl;
import us.mn.state.dot.tms.kml.KmlGeometry;
import us.mn.state.dot.tms.kml.KmlIconImpl;
import us.mn.state.dot.tms.kml.KmlIconStyle;
import us.mn.state.dot.tms.kml.KmlIconStyleImpl;
import us.mn.state.dot.tms.kml.KmlPlacemark;
import us.mn.state.dot.tms.kml.KmlRenderer;
import us.mn.state.dot.tms.kml.KmlStyle;
import us.mn.state.dot.tms.kml.KmlStyleImpl;
import us.mn.state.dot.tms.kml.KmlStyleSelector;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.SString;

/**
 * Dynamic Message Sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSImpl extends DeviceImpl implements DMS, KmlPlacemark {

	/** Action debug log */
	static protected final DebugLog ACTION_LOG = new DebugLog("action");

	/** Compare two (possibly-null) integers for equality */
	static protected boolean integerEquals(Integer i0, Integer i1) {
		if(i0 == null)
			return i1 == null;
		else
			return i0.equals(i1);
	}

	/** Calculate the maximum trip minute to display on the sign */
	static protected int maximumTripMinutes(float miles) {
		float hours = miles /
			SystemAttrEnum.TRAVEL_TIME_MIN_MPH.getInt();
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

	/** Load all the DMS */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading DMS...");
		namespace.registerType(SONAR_TYPE, DMSImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"camera, aws_allowed, aws_controlled FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DMSImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6),	// camera
					row.getBoolean(7),	// aws_allowed
					row.getBoolean(8)      // aws_controlled
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
		map.put("camera", camera);
		map.put("aws_allowed", awsAllowed);
		map.put("aws_controlled", awsControlled);
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
		int p, String nt, Camera cam, boolean aa, boolean ac)
	{
		super(n, c, p, nt);
		geo_loc = loc;
		camera = cam;
		awsAllowed = aa;
		awsControlled = ac;
		initTransients();
	}

	/** Create a dynamic message sign */
	protected DMSImpl(Namespace ns, String n, String loc, String c,
		int p, String nt, String cam, boolean aa, boolean ac)
	{
		this(n, (GeoLocImpl)ns.lookupObject(GeoLoc.SONAR_TYPE, loc),
		     (ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE, c),
		     p, nt, (Camera)ns.lookupObject(Camera.SONAR_TYPE, cam),
		     aa, ac);
	}

	/** Create a blank message for the sign */
	public SignMessage createBlankMessage() {
		return createBlankMessage(DMSMessagePriority.OVERRIDE);
	}

	/** Create a blank message for the sign */
	protected SignMessage createBlankMessage(DMSMessagePriority ap) {
		String bitmaps = Base64.encode(new byte[0]);
		return createMessage("", bitmaps, ap, DMSMessagePriority.BLANK,
			false, null);
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		MainServer.server.removeObject(geo_loc);
	}

	/** Set the controller to which this DMS is assigned */
	public void setController(Controller c) {
		super.setController(c);
		if(c != null)
			setConfigure(false);
	}

	/** Request to query configuration of the DMS */
	public void requestConfigure() {
		if(!configure) {
			DMSPoller p = getDMSPoller();
			if(p != null) {
				p.sendRequest(this,
					DeviceRequest.QUERY_CONFIGURATION);
			}
		}
	}

	/** Configure flag indicates that the sign has been configured */
	protected boolean configure;

	/** Set the configure flag.
	 *  @param c Set to true to indicate the DMS is configured. */
	public void setConfigure(boolean c) {
		configure = c;
	}

	/** Get the configure flag.
	 *  @return True to indicate the DMS is configured else false. */
	public boolean getConfigure() {
		return configure;
	}

	/** Device location */
	protected GeoLocImpl geo_loc;

	/** Get the device location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
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

	/** Administrator allowed AWS control */
	protected boolean awsAllowed;

	/** Allow (or deny) sign control by Automated Warning System */
	public void setAwsAllowed(boolean a) {
		awsAllowed = a;
	}

	/** Allow (or deny) sign control by Automated Warning System */
	public void doSetAwsAllowed(boolean a) throws TMSException {
		if(a == awsAllowed)
			return;
		store.update(this, "aws_allowed", a);
		setAwsAllowed(a);
	}

	/** Is sign allowed to be controlled by Automated Warning System? */
	public boolean getAwsAllowed() {
		return awsAllowed;
	}

	/** AWS controlled */
	protected boolean awsControlled;

	/** Set sign to Automated Warning System controlled */
	public void setAwsControlled(boolean a) {
		awsControlled = a;
	}

	/** Set sign to Automated Warning System controlled */
	public void doSetAwsControlled(boolean a) throws TMSException {
		if(a == awsControlled)
			return;
		store.update(this, "aws_controlled", a);
		setAwsControlled(a);
	}

	/** Is sign controlled by Automated Warning System? */
	public boolean getAwsControlled() {
		return awsControlled;
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
	protected transient DMSType dms_type = DMSType.UNKNOWN;

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
		if(!integerEquals(h, faceHeight)) {
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
		if(!integerEquals(w, faceWidth)) {
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
		if(!integerEquals(b, horizontalBorder)) {
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
		if(!integerEquals(b, verticalBorder)) {
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
		if(!integerEquals(p, horizontalPitch)) {
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
		if(!integerEquals(p, verticalPitch)) {
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
		if(!integerEquals(h, heightPixels)) {
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
		if(!integerEquals(w, widthPixels)) {
			widthPixels = w;
			// FIXME: update bitmap graphics plus stuck on/off
			notifyAttribute("widthPixels");
		}
	}

	/** Get sign width (pixels) */
	public Integer getWidthPixels() {
		return widthPixels;
	}

	/** Character height (pixels; 0 means variable) */
	protected transient Integer charHeightPixels;

	/** Set character height (pixels) */
	public void setCharHeightPixels(Integer h) {
		// NOTE: some crazy vendors think line-matrix signs should have
		//       a variable character height, so we have to fix their
		//       mistake here ... uggh
		if(h == 0 && DMSType.isFixedHeight(dms_type))
			h = estimateLineHeight();
		if(!integerEquals(h, charHeightPixels)) {
			charHeightPixels = h;
			notifyAttribute("charHeightPixels");
		}
	}

	/** Estimate the line height (pixels) */
	protected Integer estimateLineHeight() {
		Integer h = heightPixels;
		if(h != null) {
			int m = SystemAttrEnum.DMS_MAX_LINES.getInt();
			for(int i = m; i > 0; i--) {
				if(h % i == 0)
					return h / i;
			}
		}
		return null;
	}

	/** Get character height (pixels) */
	public Integer getCharHeightPixels() {
		return charHeightPixels;
	}

	/** Character width (pixels; 0 means variable) */
	protected transient Integer charWidthPixels;

	/** Set character width (pixels) */
	public void setCharWidthPixels(Integer w) {
		if(!integerEquals(w, charWidthPixels)) {
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
		if(!integerEquals(t, minCabinetTemp)) {
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
		if(!integerEquals(t, maxCabinetTemp)) {
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
		if(!integerEquals(t, minAmbientTemp)) {
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
		if(!integerEquals(t, maxAmbientTemp)) {
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
		if(!integerEquals(t, minHousingTemp)) {
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
		if(!integerEquals(t, maxHousingTemp)) {
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
		if(!integerEquals(l, lightOutput)) {
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

	/** Power supply status.  This is an array of status for each power
	 * supply.
	 * @see DMS.getPowerStatus */
	protected transient String[] powerStatus = new String[0];

	/** Set the power supply status table */
	public void setPowerStatus(String[] t) {
		powerStatus = t;
		notifyAttribute("powerStatus");
	}

	/** Get the power supply status table */
	public String[] getPowerStatus() {
		return powerStatus;
	}

	/** Request a device operation (query message, test pixels, etc.) */
	public void setDeviceRequest(int r) {
		DMSPoller p = getDMSPoller();
		if(p != null)
			p.sendRequest(this, DeviceRequest.fromOrdinal(r));
	}

	/** The owner of the next message to be displayed.  This is a write-only
	 * SONAR attribute. */
	protected transient User ownerNext;

	/** Set the message owner.  When a user sends a new message to the DMS,
	 * two attributes must be set: ownerNext and messageNext.  There can be
	 * a race between two clients setting these attributes.  If ownerNext
	 * is non-null when being set, then a race has been detected, meaning
	 * two clients are trying to send a message at the same time. */
	public synchronized void setOwnerNext(User o) {
		if(ownerNext != null && o != null) {
			System.err.println("DMSImpl.setOwnerNext: " + getName()+
				", " + ownerNext.getName() + " vs. " +
				o.getName());
			ownerNext = null;
		} else
			ownerNext = o;
	}

	/** The next message to be displayed.  This is a write-only SONAR
	 * attribute.  It is checked to prevent a lower priority message from
	 * getting queued during the time when a message gets queued and it
	 * becomes activated.  It must be set to null after an operation
	 * completes.
	 * @see DMSImpl#shouldActivate */
	protected transient SignMessage messageNext;

	/** Set the next sign message */
	public void setMessageNext(SignMessage sm) {
		messageNext = sm;
	}

	/** Set the next sign message.  This is called by SONAR when the
	 * messageNext attribute is set.  The ownerNext attribute should have
	 * been set by the client prior to setting this attribute. */
	public void doSetMessageNext(SignMessage sm) throws TMSException {
		// FIXME: make sure that ownerNext is non-null
		try {
			doSetMessageNext(sm, ownerNext);
		}
		finally {
			// Clear the owner even if there was an exception
			ownerNext = null;
		}
	}

	/** Set the next sign message and owner */
	public synchronized void doSetMessageNext(SignMessage sm, User o)
		throws TMSException
	{
		DMSPoller p = getDMSPoller();
		if(p == null) {
			throw new ChangeVetoException(name +
				": NO ACTIVE POLLER");
		}
		if(shouldActivate(sm))
			doSetMessageNext(sm, o, p);
	}

	/** Set the next sign message */
	protected void doSetMessageNext(SignMessage sm, User o, DMSPoller p)
		throws TMSException
	{
		SignMessage smn = validateMessage(sm);
		if(smn != sm)
			o = null;
		int ap = smn.getActivationPriority();
		if(ap == DMSMessagePriority.OVERRIDE.ordinal())
			s_routes.clear();
		p.sendMessage(this, smn, o);
	}

	/** Check if the sign has a reference to a sign message */
	public boolean hasReference(SignMessage sm) {
		return sm == messageCurrent ||
		       sm == messageSched ||
		       sm == messageNext;
	}

	/** Validate a sign message to send */
	protected SignMessage validateMessage(SignMessage sm)
		throws TMSException
	{
		MultiString multi = new MultiString(sm.getMulti());
		SignMessage sched = messageSched;	// Avoid race
		if(sched != null && multi.isBlank()) {
			// Don't blank the sign if there's a scheduled message
			// -- send the scheduled message instead.
			sm = sched;
			multi = new MultiString(sm.getMulti());
		}
		if(!multi.isValid()) {
			throw new InvalidMessageException(name +
				": INVALID MESSAGE, " + sm.getMulti());
		}
		validateBitmaps(sm);
		return sm;
	}

	/** Validate the message bitmaps */
	protected void validateBitmaps(SignMessage sm)
		throws ChangeVetoException
	{
		try {
			validateBitmaps(sm.getBitmaps());
		}
		catch(IOException e) {
			throw new ChangeVetoException("Base64 decode error");
		}
		catch(IndexOutOfBoundsException e) {
			throw new ChangeVetoException(e.getMessage());
		}
	}

	/** Validate the message bitmaps */
	protected void validateBitmaps(String bmaps) throws IOException,
		ChangeVetoException
	{
		byte[] bitmaps = Base64.decode(bmaps);
		BitmapGraphic bitmap = createBlankBitmap();
		int blen = bitmap.length();
		if(blen == 0)
			throw new ChangeVetoException("Invalid sign size");
		if(bitmaps.length % blen != 0)
			throw new ChangeVetoException("Invalid bitmap length");
		String[] pixels = pixelStatus;	// Avoid races
		if(pixels != null && pixels.length == 2)
			validateBitmaps(bitmaps, pixels, bitmap);
	}

	/** Validate the message bitmaps */
	protected void validateBitmaps(byte[] bitmaps, String[] pixels,
		BitmapGraphic bitmap) throws IOException, ChangeVetoException
	{
		int blen = bitmap.length();
		int off_limit = SystemAttrEnum.DMS_PIXEL_OFF_LIMIT.getInt();
		int on_limit = SystemAttrEnum.DMS_PIXEL_ON_LIMIT.getInt();
		BitmapGraphic stuckOff = bitmap.createBlankCopy();
		BitmapGraphic stuckOn = bitmap.createBlankCopy();
		byte[] b_off = Base64.decode(pixels[STUCK_OFF_BITMAP]);
		byte[] b_on = Base64.decode(pixels[STUCK_ON_BITMAP]);
		// Don't validate if the sign dimensions have changed
		if(b_off.length != blen || b_on.length != blen)
			return;
		stuckOff.setPixels(b_off);
		stuckOn.setPixels(b_on);
		int n_pages = bitmaps.length / blen;
		byte[] b = new byte[blen];
		for(int p = 0; p < n_pages; p++) {
			System.arraycopy(bitmaps, p * blen, b, 0, blen);
			bitmap.setPixels(b);
			bitmap.union(stuckOff);
			int n_lit = bitmap.getLitCount();
			if(n_lit > off_limit) {
				throw new ChangeVetoException(
					"Too many stuck off pixels: " + n_lit);
			}
			bitmap.setPixels(b);
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

	/** Check if a message should be activated based on priority.
	 * @param sm SignMessage being activated.
	 * @return true If priority is high enough to deploy. */
	public boolean shouldActivate(SignMessage sm) {
		return SignMessageHelper.shouldActivate(messageCurrent, sm) &&
		       SignMessageHelper.shouldActivate(messageNext, sm) &&
		       SignMessageHelper.lookup(sm.getName()) == sm;
	}

	/** Send a sign message created by IRIS server */
	public void sendMessage(SignMessage sm) throws TMSException {
		doSetMessageNext(sm, null);
	}

	/** Current message (Shall not be null) */
	protected transient SignMessage messageCurrent =
		createBlankMessage();

	/** Set the current message */
	public void setMessageCurrent(SignMessage sm, User o) {
		if(SignMessageHelper.isEquivalent(messageCurrent, sm))
			return;
		logMessage(sm, o);
		setDeployTime();
		messageCurrent = sm;
		notifyAttribute("messageCurrent");
		ownerCurrent = o;
		notifyAttribute("ownerCurrent");
	}

	/** Get the current messasge.
	 * @return Currently active message (cannot be null) */
	public SignMessage getMessageCurrent() {
		return messageCurrent;
	}

	/** Owner of current message */
	protected transient User ownerCurrent;

	/** Get the current message owner.
	 * @return User who deployed the current message. */
	public User getOwnerCurrent() {
		return ownerCurrent;
	}

	/** Log a message */
	protected void logMessage(SignMessage sm, User o) {
		EventType et = EventType.DMS_DEPLOYED;
		String text = sm.getMulti();
		if(SignMessageHelper.isBlank(sm)) {
			et = EventType.DMS_CLEARED;
			text = null;
		}
		String owner = null;
		if(o != null)
			owner = o.getName();
		SignStatusEvent ev = new SignStatusEvent(et, name, text, owner);
		try {
			ev.doStore();
		}
		catch(TMSException e) {
			e.printStackTrace();
		}
	}

	/** Message deploy time */
	protected long deployTime = 0;

	/** Set the message deploy time */
	protected void setDeployTime() {
		deployTime = System.currentTimeMillis();
		notifyAttribute("deployTime");
	}

	/** Get the message deploy time.
	 * @return Time message was deployed (ms since epoch).
	 * @see java.lang.System.currentTimeMillis */
	public long getDeployTime() {
		return deployTime;
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
		if(!integerEquals(base, ldcPotBase)) {
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
		if(!integerEquals(low, pixelCurrentLow)) {
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
		if(!integerEquals(high, pixelCurrentHigh)) {
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

	/** Feedback brightness sample data */
	public void feedbackBrightness(BrightnessSample s) {
		// FIXME: store brightness sample in database
	}

	/** Lookup recent brightness feedback sample data */
	public void queryBrightnessFeedback(BrightnessSample.Handler h) {
		// FIXME: lookup samples in database
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param p Activation priority.
	 * @return New sign message, or null on error. */
	public SignMessage createMessage(String m, DMSMessagePriority ap) {
		MultiString ms = new MultiString(m);
		if(ms.isBlank())
			return createBlankMessage(ap);
		else
			return createMessage(m, ap, DMSMessagePriority.OPERATOR,
				null);
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	public SignMessage createMessage(String m, DMSMessagePriority ap,
		DMSMessagePriority rp, Integer d)
	{
		return createMessage(m, ap, rp, false, d);
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param s Scheduled flag.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	protected SignMessage createMessage(String m, DMSMessagePriority ap,
		DMSMessagePriority rp, boolean s, Integer d)
	{
		PixelMapBuilder builder = DMSHelper.createPixelMapBuilder(this);
		if(builder != null) {
			MultiString ms = new MultiString(m);
			BitmapGraphic[] pages = builder.createPixmaps(ms);
			return createMessageB(m, pages, ap, rp, s, d);
		} else
			return null;
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param pages Pre-rendered graphics for all pages.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	public SignMessage createMessage(String m, BitmapGraphic[] pages,
		DMSMessagePriority ap, DMSMessagePriority rp, Integer d)
	{
		return createMessage(m, pages, ap, rp, false, d);
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param pages Pre-rendered graphics for all pages.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param s Scheduled flag.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	protected SignMessage createMessage(String m, BitmapGraphic[] pages,
		DMSMessagePriority ap, DMSMessagePriority rp, boolean s,
		Integer d)
	{
		Integer w = widthPixels;
		Integer h = heightPixels;
		if(w == null || w < 1)
			return null;
		if(h == null || h < 1)
			return null;
		BitmapGraphic[] bmaps = new BitmapGraphic[pages.length];
		for(int i = 0; i < bmaps.length; i++) {
			bmaps[i] = new BitmapGraphic(w, h);
			bmaps[i].copy(pages[i]);
		}
		return createMessageB(m, bmaps, ap, rp, s, d);
	}

	/** Create a new message (B version) */
	protected SignMessage createMessageB(String m, BitmapGraphic[] pages,
		DMSMessagePriority ap, DMSMessagePriority rp, boolean s,
		Integer d)
	{
		int blen = pages[0].length();
		byte[] bitmap = new byte[pages.length * blen];
		for(int i = 0; i < pages.length; i++) {
			byte[] page = pages[i].getPixels();
			System.arraycopy(page, 0, bitmap, i * blen, blen);
		}
		String bitmaps = Base64.encode(bitmap);
		return createMessage(m, bitmaps, ap, rp, s, d);
	}

	/** Create a sign message */
	protected SignMessage createMessage(final String m, final String b,
		DMSMessagePriority ap, DMSMessagePriority rp, final boolean s,
		final Integer d)
	{
		SignMessage esm = SignMessageHelper.find(m, b, ap, rp, s, d);
		if(esm != null)
			return esm;
		else
			return createMessageC(m, b, ap, rp, s, d);
	}

	/** Create a new sign message (C version) */
	protected SignMessage createMessageC(String m, String b,
		DMSMessagePriority ap, DMSMessagePriority rp, boolean s,
		Integer d)
	{
		SignMessageImpl sm = new SignMessageImpl(m, b, ap, rp, s, d);
		notifyNewSignMessage(sm);
		return sm;
	}

	/** Notify SONAR that a new sign message exists */
	protected void notifyNewSignMessage(SignMessageImpl sm) {
		try {
			if(MainServer.server != null)
				MainServer.server.createObject(sm);
			else
				namespace.storeObject(sm);
		}
		catch(SonarException e) {
			// FIXME: we should do something more here...
			e.printStackTrace();
		}
	}

	/** Flag for current scheduled message.  This is used to guarantee that
	 * performAction is called at least once between each call to
	 * updateScheduledMessage.  If not, then the scheduled message is
	 * cleared. */
	protected transient boolean is_scheduled;

	/** Current scheduled message */
	protected transient SignMessage messageSched;

	/** Check if a DMS action is deployable */
	public boolean isDeployable(DmsAction da) {
		if(isFailed())
			return false;
		SignMessage sm = createMessage(da);
		try {
			return sm == validateMessage(sm);
		}
		catch(TMSException e) {
			return false;
		}
	}

	/** Perform a DMS action */
	public void performAction(DmsAction da) {
		SignMessage sm = createMessage(da);
		if(sm != null) {
			if(shouldReplaceScheduled(sm)) {
				messageSched = sm;
				is_scheduled = true;
			}
		}
	}

	/** Test if the given sign message should replace the current
	 * scheduled message. */
	protected boolean shouldReplaceScheduled(SignMessage sm) {
		SignMessage s = messageSched;	// Avoid NPE
		return s == null ||
		       sm.getActivationPriority() > s.getActivationPriority() ||
		       sm.getRunTimePriority() >= s.getRunTimePriority();
	}

	/** Create a message for the sign.
	 * @param da DMS action
	 * @return New sign message, or null on error */
	protected SignMessage createMessage(DmsAction da) {
		Integer d = da.getActionPlan().getSyncActions() ? null : 2;
		DMSMessagePriority ap = DMSMessagePriority.fromOrdinal(
			da.getActivationPriority());
		DMSMessagePriority rp = DMSMessagePriority.fromOrdinal(
			da.getRunTimePriority());
		String m = createMulti(da.getQuickMessage());
		if(m != null)
			return createMessage(m, ap, rp, true, d);
		else
			return null;
	}

	/** Update the scheduled message on the sign */
	public void updateScheduledMessage() {
		if(!is_scheduled)
			messageSched = null;
		SignMessage sm = messageSched;
		if(shouldActivate(sm)) {
			try {
				doSetMessageNext(sm, null);
			}
			catch(TMSException e) {
				ACTION_LOG.log(getName() + ": " +
					e.getMessage());
			}
		}
		is_scheduled = false;
	}

	/** Create a MULTI string for a quick message */
	protected String createMulti(QuickMessage qm) {
		if(qm != null)
			return replaceTravelTimes(qm.getMulti());
		else
			return null;
	}

	/** MultiString for replacing travel time tags */
	protected class TravelCallback extends MultiString {

		/* If all routes are on the same corridor, when the
		 * "OVER X" form is used, it must be used for all
		 * destinations. So, first we must calculate the times
		 * for each destination. Then, determine if the "OVER"
		 * form should be used. After that, replace the travel
		 * time tags with the selected values. */
		protected boolean any_over = false;
		protected boolean all_over = false;

		protected boolean valid = true;

		/** Add a travel time destination */
		public void addTravelTime(String sid) {
			Route r = lookupRoute(sid);
			if(r != null)
				addTravelTime(r);
			else {
				logTravel("NO ROUTE TO " + sid);
				valid = false;
			}
		}

		/** Add a travel time for a route */
		protected void addTravelTime(Route r) {
			boolean final_dest = isFinalDest(r);
			try {
				int mn = calculateTravelTime(r, final_dest);
				int slow = maximumTripMinutes(r.getLength());
				addTravelTime(mn, slow);
			}
			catch(BadRouteException e) {
				logTravel("BAD ROUTE, " + e.getMessage());
				valid = false;
			}
		}

		/** Add a travel time */
		protected void addTravelTime(int mn, int slow) {
			boolean over = mn > slow;
			if(over) {
				any_over = true;
				mn = slow;
			}
			if(over || all_over) {
				mn = roundUp5Min(mn);
				addSpan("OVER " + String.valueOf(mn));
			} else
				addSpan(String.valueOf(mn));
		}

		/** Check if the callback has changed formatting mode */
		protected boolean isChanged() {
			all_over = any_over && isSingleCorridor();
			return all_over;
		}
	}

	/** Log a travel time error */
	protected void logTravel(String m) {
		if(RouteBuilder.TRAVEL_LOG.isOpen())
			RouteBuilder.TRAVEL_LOG.log(name + ": " + m);
	}

	/** Replace travel time tags in a MULTI string */
	protected String replaceTravelTimes(String trav) {
		MultiString m = new MultiString(trav);
		TravelCallback cb = new TravelCallback();
		m.parse(cb);
		if(cb.isChanged()) {
			cb.clear();
			m.parse(cb);
		}
		if(cb.valid)
			return cb.toString();
		else
			return null;
	}

	/** Mapping of station IDs to routes */
	protected transient final HashMap<String, Route> s_routes =
		new HashMap<String, Route>();

	/** Lookup a route by station ID */
	protected Route lookupRoute(String sid) {
		if(!s_routes.containsKey(sid)) {
			Route r = createRoute(sid);
			if(r != null)
				s_routes.put(sid, r);
		}
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
		throws BadRouteException
	{
		float hours = route.getTravelTime(final_dest);
		return (int)(hours * 60) + 1;
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
	protected Route createRoute(String sid) {
		StationImpl s = lookupStation(sid);
		if(s != null)
			return createRoute(s);
		else
			return null;
	}

	/** Create one route to a travel time destination */
	protected Route createRoute(StationImpl s) {
		GeoLoc dest = s.getR_Node().getGeoLoc();
		if(corridors != null)
			return createRoute(dest);
		else
			return null;
	}

	/** Create one route to a travel time destination */
	protected Route createRoute(GeoLoc dest) {
		RouteBuilder builder = new RouteBuilder(getName(), corridors);
		SortedSet<Route> routes = builder.findRoutes(geo_loc, dest);
		if(routes.size() > 0)
			return routes.first();
		else
			return null;
	}

	/** render to kml (KmlPlacemark interface) */
	public String renderKml() {
		return KmlRenderer.render(this);
	}

	/** render inner elements to kml (KmlPlacemark interface) */
	public String renderInnerKml() {
		return "";
	}

	/** get kml placemark name (KmlPlacemark interface) */
	public String getPlacemarkName() {
		return getName();
	}

	/** get geometry (KmlPlacemark interface) */
	public KmlGeometry getGeometry() {
		GeoLoc loc = getGeoLoc();
		if(loc == null)
			return null;
		return GeoLocHelper.getWgs84Position(loc);
	}

	/** get placemark description (KmlPlacemark interface) */
	public String getPlacemarkDesc() {
		// DMS name, e.g. CMS or DMS
		final String DMSABBR = I18N.get("dms.abbreviation");

		StringBuilder desc = new StringBuilder();

		desc.append(Kml.descItem("Location", 
			GeoLocHelper.getDescription(getGeoLoc())));

		desc.append(Kml.descItem(DMSABBR + " Status", 
			DMSHelper.getAllStyles(this)));

		SignMessage sm = getMessageCurrent();
		String[] ml = SignMessageHelper.createLines(sm);
		if(ml == null || ml.length <= 0)
			desc.append(Kml.descItem("Messages Lines", "none"));
		else {
			for(int i = 0; i < ml.length; ++i) {
				desc.append(Kml.descItem("Message Line " + 
					(i + 1), ml[i]));
			}
		}

		String owner = (getOwnerCurrent() == null ? "none" : 
			getOwnerCurrent().getFullName());
		desc.append(Kml.descItem("Author", owner));

		desc.append(Kml.descItem("Font", SString.toString(
			SignMessageHelper.getFontNames(sm, 1))));

		desc.append(Kml.descItem("Notes", getNotes()));
		desc.append(Kml.descItem("Last Operation", getOpStatus()));

		desc.append("<br>Updated by IRIS " + 
			new Date().toString() + "<br><br>");

		desc.append("<br>");

		return Kml.htmlDesc(desc.toString());
	}

	/** get kml style selector (KmlFolder interface) */
	public KmlStyleSelector getKmlStyleSelector() {
		KmlStyle style = new KmlStyleImpl();
		KmlIconStyle is = new KmlIconStyleImpl();
		is.setKmlColor(getKmlIconColor());
		is.setKmlScale(1);
		String icon = "http://maps.google.com/mapfiles/kml/paddle/" +
			"wht-blank.png";
		is.setKmlIcon(new KmlIconImpl(icon));
		style.setIconStyle(is);
		return style;
	}

	/** get kml icon color, which is a function of the DMS state */
	public KmlColor getKmlIconColor() {
		// note: this is a prioritized list
		if(DMSHelper.checkStyle(DMSHelper.STYLE_AVAILABLE, this))
			return KmlColorImpl.Blue;
		if(DMSHelper.checkStyle(DMSHelper.STYLE_DEPLOYED, this))
			return KmlColorImpl.Yellow;
		if(DMSHelper.checkStyle(DMSHelper.STYLE_AWS_DEPLOYED, this))
			return KmlColorImpl.Red;
		if(DMSHelper.checkStyle(DMSHelper.STYLE_SCHEDULED, this))
			return KmlColorImpl.Orange;
		if(DMSHelper.checkStyle(DMSHelper.STYLE_MAINTENANCE, this))
			return KmlColorImpl.Black;
		if(DMSHelper.checkStyle(DMSHelper.STYLE_INACTIVE, this))
			return KmlColorImpl.White;
		if(DMSHelper.checkStyle(DMSHelper.STYLE_FAILED, this))
			return KmlColorImpl.Gray;
		return KmlColorImpl.Black;
	}

	/** Render the DMS object as xml */
	public void printXmlElement(PrintWriter out) {
		// DMS name, e.g. CMS or DMS
		final String DMSABBR = I18N.get("dms.abbreviation");
		final User owner = getOwnerCurrent();	// Avoid race
		final String op_status = getOpStatus();	// Avoid race
		final GeoLoc loc = getGeoLoc();		// Avoid race
		out.print("<" + DMSABBR);
		out.print(XmlWriter.createAttribute("id", getName()));
		out.print(XmlWriter.createAttribute("status",
			DMSHelper.getAllStyles(this)));
		if(owner != null) {
			out.print(XmlWriter.createAttribute("owner",
				owner.getFullName()));
		}
		out.print(XmlWriter.createAttribute("notes", getNotes()));
		if(op_status != null) {
			out.print(XmlWriter.createAttribute("last_operation",
				op_status));
		}
		if(loc != null) {
			out.print(XmlWriter.createAttribute("geoloc",
				loc.getName()));
		}
		out.println(">");
		SignMessage msg = getMessageCurrent();
		if(msg instanceof SignMessageImpl)
			((SignMessageImpl)msg).printXmlElement(out);
		out.println("</" + DMSABBR + ">");
	}
}
