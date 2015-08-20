/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
 * Copyright (C) 2010 AHMCT, University of California
 * Copyright (C) 2012  Iteris Inc.
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import static us.mn.state.dot.tms.DMSMessagePriority.AWS;
import static us.mn.state.dot.tms.DMSMessagePriority.TRAVEL_TIME;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.GateArmArrayHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LCSHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.event.BrightnessSample;
import us.mn.state.dot.tms.server.event.SignStatusEvent;
import us.mn.state.dot.tms.utils.Base64;
import us.mn.state.dot.tms.utils.SString;

/**
 * Dynamic Message Sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSImpl extends DeviceImpl implements DMS {

	/** DMS debug log */
	static private final DebugLog DMS_LOG = new DebugLog("dms");

	/** DMS schedule debug log */
	static private final DebugLog SCHED_LOG = new DebugLog("sched");

	/** Load all the DMS */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DMSImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"beacon, preset, aws_allowed, aws_controlled, " +
			"default_font FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DMSImpl(
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6),	// beacon
					row.getString(7),	// preset
					row.getBoolean(8),	// aws_allowed
					row.getBoolean(9),     // aws_controlled
					row.getString(10)	// default_font
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
		map.put("beacon", beacon);
		map.put("preset", preset);
		map.put("aws_allowed", awsAllowed);
		map.put("aws_controlled", awsControlled);
		map.put("default_font", default_font);
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

	/** MULTI message formatter */
	private final MultiFormatter formatter;

	/** Create a new DMS with a string name */
	public DMSImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
		formatter = new MultiFormatter(this);
	}

	/** Create a dynamic message sign */
	private DMSImpl(String n, GeoLocImpl loc, ControllerImpl c,
		int p, String nt, Beacon b, CameraPreset cp, boolean aa,
		boolean ac, Font df)
	{
		super(n, c, p, nt);
		geo_loc = loc;
		beacon = b;
		setPreset(cp);
		awsAllowed = aa;
		awsControlled = ac;
		default_font = df;
		formatter = new MultiFormatter(this);
		initTransients();
	}

	/** Create a dynamic message sign */
	private DMSImpl(String n, String loc, String c,
		int p, String nt, String b, String cp, boolean aa, boolean ac,
		String df)
	{
		this(n, lookupGeoLoc(loc), lookupController(c), p, nt,
		     lookupBeacon(b), lookupPreset(cp), aa, ac,
		     FontHelper.lookup(df));
	}

	/** Create a blank message for the sign */
	public SignMessage createBlankMessage() {
		return createBlankMessage(DMSMessagePriority.OVERRIDE);
	}

	/** Create a blank message for the sign */
	private SignMessage createBlankMessage(DMSMessagePriority ap) {
		String bitmaps = Base64.encode(new byte[0]);
		return createMessage("", false, bitmaps, ap,
			DMSMessagePriority.BLANK, false, null);
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

	/** External beacon */
	private Beacon beacon;

	/** Set external beacon */
	@Override
	public void setBeacon(Beacon b) {
		beacon = b;
	}

	/** Set external beacon */
	public void doSetBeacon(Beacon b) throws TMSException {
		if (b != beacon) {
			store.update(this, "beacon", b);
			setBeacon(b);
		}
	}

	/** Get external beacon */
	@Override
	public Beacon getBeacon() {
		return beacon;
	}

	/** Update external beacon */
	private void updateBeacon() {
		Beacon b = beacon;
		if (b != null) {
			boolean f = isOnline() && isMsgBeacon();
			b.setFlashing(f);
		}
	}

	/** Camera preset from which this can be seen */
	private CameraPreset preset;

	/** Set the verification camera preset */
	@Override
	public void setPreset(CameraPreset cp) {
		final CameraPreset ocp = preset;
		if (cp instanceof CameraPresetImpl) {
			CameraPresetImpl cpi = (CameraPresetImpl)cp;
			cpi.setAssignedNotify(true);
		}
		preset = cp;
		if (ocp instanceof CameraPresetImpl) {
			CameraPresetImpl ocpi = (CameraPresetImpl)ocp;
			ocpi.setAssignedNotify(false);
		}
	}

	/** Set the verification camera preset */
	public void doSetPreset(CameraPreset cp) throws TMSException {
		if (cp == preset)
			return;
		store.update(this, "preset", cp);
		setPreset(cp);
	}

	/** Get verification camera preset */
	@Override
	public CameraPreset getPreset() {
		return preset;
	}

	/** Administrator allowed AWS control */
	private boolean awsAllowed;

	/** Allow (or deny) sign control by Automated Warning System */
	@Override
	public void setAwsAllowed(boolean a) {
		awsAllowed = a;
	}

	/** Allow (or deny) sign control by Automated Warning System */
	public void doSetAwsAllowed(boolean a) throws TMSException {
		if (a != awsAllowed) {
			store.update(this, "aws_allowed", a);
			setAwsAllowed(a);
		}
	}

	/** Is sign allowed to be controlled by Automated Warning System? */
	@Override
	public boolean getAwsAllowed() {
		return awsAllowed;
	}

	/** AWS controlled */
	private boolean awsControlled;

	/** Set sign to Automated Warning System controlled */
	@Override
	public void setAwsControlled(boolean a) {
		awsControlled = a;
	}

	/** Set sign to Automated Warning System controlled */
	public void doSetAwsControlled(boolean a) throws TMSException {
		if (a != awsControlled) {
			store.update(this, "aws_controlled", a);
			setAwsControlled(a);
		}
	}

	/** Is sign controlled by Automated Warning System? */
	@Override
	public boolean getAwsControlled() {
		return awsControlled;
	}

	/** Default font */
	private Font default_font;

	/** Set the default font */
	@Override
	public void setDefaultFont(Font f) {
		default_font = f;
	}

	/** Set the default font */
	public void doSetDefaultFont(Font f) throws TMSException {
		if (f != default_font) {
			store.update(this, "default_font", f);
			setDefaultFont(f);
		}
	}

	/** Get the default font */
	@Override
	public Font getDefaultFont() {
		return default_font;
	}

	/** Make (manufacturer) */
	private transient String make;

	/** Set the make */
	public void setMake(String m) {
		if (!m.equals(make)) {
			make = m;
			notifyAttribute("make");
		}
	}

	/** Get the make */
	@Override
	public String getMake() {
		return make;
	}

	/** Model */
	private transient String model;

	/** Set the model */
	public void setModel(String m) {
		if (!m.equals(model)) {
			model = m;
			notifyAttribute("model");
		}
	}

	/** Get the model */
	@Override
	public String getModel() {
		return model;
	}

	/** Software version */
	private transient String version;

	/** Set the version */
	public void setVersion(String v) {
		if (!v.equals(version)) {
			version = v;
			notifyAttribute("version");
			ControllerImpl c = (ControllerImpl)getController();
			if (c != null)
				c.setVersion(version);
		}
	}

	/** Get the version */
	@Override
	public String getVersion() {
		return version;
	}

	/** Sign access description */
	private transient String signAccess;

	/** Set sign access description */
	public void setSignAccess(String a) {
		if (!a.equals(signAccess)) {
			signAccess = a;
			notifyAttribute("signAccess");
		}
	}

	/** Get sign access description */
	@Override
	public String getSignAccess() {
		return signAccess;
	}

	/** Sign type enum value */
	private transient DMSType dms_type = DMSType.UNKNOWN;

	/** Set sign type */
	public void setDmsType(DMSType t) {
		if (t != dms_type) {
			dms_type = t;
			notifyAttribute("dmsType");
		}
	}

	/** Get sign type as an int (via enum) */
	@Override
	public int getDmsType() {
		return dms_type.ordinal();
	}

	/** Sign legend string */
	private transient String legend;

	/** Set sign legend */
	public void setLegend(String l) {
		if (!l.equals(legend)) {
			legend = l;
			notifyAttribute("legend");
		}
	}

	/** Get sign legend */
	@Override
	public String getLegend() {
		return legend;
	}

	/** Beacon type description */
	private transient String beaconType;

	/** Set beacon type description */
	public void setBeaconType(String t) {
		if (!t.equals(beaconType)) {
			beaconType = t;
			notifyAttribute("beaconType");
		}
	}

	/** Get beacon type description */
	@Override
	public String getBeaconType() {
		return beaconType;
	}

	/** Sign technology description */
	private transient String technology;

	/** Set sign technology description */
	public void setTechnology(String t) {
		if (!t.equals(technology)) {
			technology = t;
			notifyAttribute("technology");
		}
	}

	/** Get sign technology description */
	@Override
	public String getTechnology() {
		return technology;
	}

	/** Height of sign face (mm) */
	private transient Integer faceHeight;

	/** Set height of sign face (mm) */
	public void setFaceHeight(Integer h) {
		if (!integerEquals(h, faceHeight)) {
			faceHeight = h;
			notifyAttribute("faceHeight");
			updateStyles();
		}
	}

	/** Get height of the sign face (mm) */
	@Override
	public Integer getFaceHeight() {
		return faceHeight;
	}

	/** Width of the sign face (mm) */
	private transient Integer faceWidth;

	/** Set width of sign face (mm) */
	public void setFaceWidth(Integer w) {
		if (!integerEquals(w, faceWidth)) {
			faceWidth = w;
			notifyAttribute("faceWidth");
			updateStyles();
		}
	}

	/** Get width of the sign face (mm) */
	@Override
	public Integer getFaceWidth() {
		return faceWidth;
	}

	/** Horizontal border (mm) */
	private transient Integer horizontalBorder;

	/** Set horizontal border (mm) */
	public void setHorizontalBorder(Integer b) {
		if (!integerEquals(b, horizontalBorder)) {
			horizontalBorder = b;
			notifyAttribute("horizontalBorder");
		}
	}

	/** Get horizontal border (mm) */
	@Override
	public Integer getHorizontalBorder() {
		return horizontalBorder;
	}

	/** Vertical border (mm) */
	private transient Integer verticalBorder;

	/** Set vertical border (mm) */
	public void setVerticalBorder(Integer b) {
		if (!integerEquals(b, verticalBorder)) {
			verticalBorder = b;
			notifyAttribute("verticalBorder");
		}
	}

	/** Get vertical border (mm) */
	@Override
	public Integer getVerticalBorder() {
		return verticalBorder;
	}

	/** Horizontal pitch (mm) */
	private transient Integer horizontalPitch;

	/** Set horizontal pitch (mm) */
	public void setHorizontalPitch(Integer p) {
		if (!integerEquals(p, horizontalPitch)) {
			horizontalPitch = p;
			notifyAttribute("horizontalPitch");
		}
	}

	/** Get horizontal pitch (mm) */
	@Override
	public Integer getHorizontalPitch() {
		return horizontalPitch;
	}

	/** Vertical pitch (mm) */
	private transient Integer verticalPitch;

	/** Set vertical pitch (mm) */
	public void setVerticalPitch(Integer p) {
		if (!integerEquals(p, verticalPitch)) {
			verticalPitch = p;
			notifyAttribute("verticalPitch");
		}
	}

	/** Get vertical pitch (mm) */
	@Override
	public Integer getVerticalPitch() {
		return verticalPitch;
	}

	/** Sign height (pixels) */
	private transient Integer heightPixels;

	/** Set sign height (pixels) */
	public void setHeightPixels(Integer h) {
		if (!integerEquals(h, heightPixels)) {
			heightPixels = h;
			// FIXME: update bitmap graphics plus stuck on/off
			notifyAttribute("heightPixels");
		}
	}

	/** Get sign height (pixels) */
	@Override
	public Integer getHeightPixels() {
		return heightPixels;
	}

	/** Sign width in pixels */
	private transient Integer widthPixels;

	/** Set sign width (pixels) */
	public void setWidthPixels(Integer w) {
		if (!integerEquals(w, widthPixels)) {
			widthPixels = w;
			// FIXME: update bitmap graphics plus stuck on/off
			notifyAttribute("widthPixels");
		}
	}

	/** Get sign width (pixels) */
	@Override
	public Integer getWidthPixels() {
		return widthPixels;
	}

	/** Character height (pixels; 0 means variable) */
	private transient Integer charHeightPixels;

	/** Set character height (pixels) */
	public void setCharHeightPixels(Integer h) {
		// NOTE: some crazy vendors think line-matrix signs should have
		//       a variable character height, so we have to fix their
		//       mistake here ... uggh
		if (h == 0 && DMSType.isFixedHeight(dms_type))
			h = estimateLineHeight();
		if (!integerEquals(h, charHeightPixels)) {
			charHeightPixels = h;
			notifyAttribute("charHeightPixels");
		}
	}

	/** Estimate the line height (pixels) */
	private Integer estimateLineHeight() {
		Integer h = heightPixels;
		if (h != null) {
			int m = SystemAttrEnum.DMS_MAX_LINES.getInt();
			for (int i = m; i > 0; i--) {
				if (h % i == 0)
					return h / i;
			}
		}
		return null;
	}

	/** Get character height (pixels) */
	@Override
	public Integer getCharHeightPixels() {
		return charHeightPixels;
	}

	/** Character width (pixels; 0 means variable) */
	private transient Integer charWidthPixels;

	/** Set character width (pixels) */
	public void setCharWidthPixels(Integer w) {
		if (!integerEquals(w, charWidthPixels)) {
			charWidthPixels = w;
			notifyAttribute("charWidthPixels");
		}
	}

	/** Get character width (pixels) */
	@Override
	public Integer getCharWidthPixels() {
		return charWidthPixels;
	}

	/** Does the sign have proportional fonts? */
	public boolean hasProportionalFonts() {
		Integer w = charWidthPixels;
		return w != null && w == 0;
	}

	/** Minimum cabinet temperature */
	private transient Integer minCabinetTemp;

	/** Set the minimum cabinet temperature */
	public void setMinCabinetTemp(Integer t) {
		if (!integerEquals(t, minCabinetTemp)) {
			minCabinetTemp = t;
			notifyAttribute("minCabinetTemp");
		}
	}

	/** Get the minimum cabinet temperature */
	@Override
	public Integer getMinCabinetTemp() {
		return minCabinetTemp;
	}

	/** Maximum cabinet temperature */
	private transient Integer maxCabinetTemp;

	/** Set the maximum cabinet temperature */
	public void setMaxCabinetTemp(Integer t) {
		if (!integerEquals(t, maxCabinetTemp)) {
			maxCabinetTemp = t;
			notifyAttribute("maxCabinetTemp");
		}
	}

	/** Get the maximum cabinet temperature */
	@Override
	public Integer getMaxCabinetTemp() {
		return maxCabinetTemp;
	}

	/** Minimum ambient temperature */
	private transient Integer minAmbientTemp;

	/** Set the minimum ambient temperature */
	public void setMinAmbientTemp(Integer t) {
		if (!integerEquals(t, minAmbientTemp)) {
			minAmbientTemp = t;
			notifyAttribute("minAmbientTemp");
		}
	}

	/** Get the minimum ambient temperature */
	@Override
	public Integer getMinAmbientTemp() {
		return minAmbientTemp;
	}

	/** Maximum ambient temperature */
	private transient Integer maxAmbientTemp;

	/** Set the maximum ambient temperature */
	public void setMaxAmbientTemp(Integer t) {
		if (!integerEquals(t, maxAmbientTemp)) {
			maxAmbientTemp = t;
			notifyAttribute("maxAmbientTemp");
		}
	}

	/** Get the maximum ambient temperature */
	@Override
	public Integer getMaxAmbientTemp() {
		return maxAmbientTemp;
	}

	/** Minimum housing temperature */
	private transient Integer minHousingTemp;

	/** Set the minimum housing temperature */
	public void setMinHousingTemp(Integer t) {
		if (!integerEquals(t, minHousingTemp)) {
			minHousingTemp = t;
			notifyAttribute("minHousingTemp");
		}
	}

	/** Get the minimum housing temperature */
	@Override
	public Integer getMinHousingTemp() {
		return minHousingTemp;
	}

	/** Maximum housing temperature */
	private transient Integer maxHousingTemp;

	/** Set the maximum housing temperature */
	public void setMaxHousingTemp(Integer t) {
		if (!integerEquals(t, maxHousingTemp)) {
			maxHousingTemp = t;
			notifyAttribute("maxHousingTemp");
		}
	}

	/** Get the maximum housing temperature */
	@Override
	public Integer getMaxHousingTemp() {
		return maxHousingTemp;
	}

	/** Current light output (percentage) of the sign */
	private transient Integer lightOutput;

	/** Set the light output of the sign (percentage) */
	public void setLightOutput(Integer l) {
		if (!integerEquals(l, lightOutput)) {
			lightOutput = l;
			notifyAttribute("lightOutput");
		}
	}

	/** Get the light output of the sign (percentage) */
	@Override
	public Integer getLightOutput() {
		return lightOutput;
	}

	/** Pixel status.  This is an array of two Base64-encoded bitmaps.
	 * The first indicates stuck-off pixels, the second stuck-on pixels. */
	private transient String[] pixelStatus;

	/** Set the pixel status array */
	public void setPixelStatus(String[] p) {
		if (!Arrays.equals(p, pixelStatus)) {
			pixelStatus = p;
			notifyAttribute("pixelStatus");
		}
	}

	/** Get the pixel status array */
	@Override
	public String[] getPixelStatus() {
		return pixelStatus;
	}

	/** Power supply status.  This is an array of status for each power
	 * supply.
	 * @see DMS.getPowerStatus */
	private transient String[] powerStatus = new String[0];

	/** Set the power supply status table */
	public void setPowerStatus(String[] t) {
		if (!Arrays.equals(t, powerStatus)) {
			powerStatus = t;
			notifyAttribute("powerStatus");
		}
	}

	/** Get the power supply status table */
	@Override
	public String[] getPowerStatus() {
		return powerStatus;
	}

	/** Photocell status.  This is an array of status for each photocell.
	 * @see DMS.getPhotocellStatus */
	private transient String[] photocellStatus = new String[0];

	/** Set the photocell status table */
	public void setPhotocellStatus(String[] t) {
		if (!Arrays.equals(t, photocellStatus)) {
			photocellStatus = t;
			notifyAttribute("photocellStatus");
		}
	}

	/** Get the photocell status table */
	@Override
	public String[] getPhotocellStatus() {
		return photocellStatus;
	}

	/** Request a device operation (query message, test pixels, etc.) */
	public void sendDeviceRequest(DeviceRequest dr) {
		DMSPoller p = getDMSPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Request a device operation (query message, test pixels, etc.) */
	@Override
	public void setDeviceRequest(int r) {
		sendDeviceRequest(DeviceRequest.fromOrdinal(r));
	}

	/** The owner of the next message to be displayed.  This is a write-only
	 * SONAR attribute. */
	private transient User ownerNext;

	/** Set the message owner.  When a user sends a new message to the DMS,
	 * two attributes must be set: ownerNext and messageNext.  There can be
	 * a race between two clients setting these attributes.  If ownerNext
	 * is non-null when being set, then a race has been detected, meaning
	 * two clients are trying to send a message at the same time. */
	@Override
	public synchronized void setOwnerNext(User o) {
		if (ownerNext != null && o != null) {
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
	 * becomes activated.
	 * @see DMSImpl#shouldActivate */
	private transient SignMessage messageNext;

	/** Set the next sign message.  This method is not called by SONAR
	 * automatically; instead, it must be called by operations after
	 * getting exclusive device ownership.  It must be set back to null
	 * after the operation completes.  This is necessary to prevent the
	 * ReaperJob from destroying a SignMessage before it has been sent to
	 * a sign.
	 * @see DeviceImpl.acquire */
	@Override
	public void setMessageNext(SignMessage sm) {
		messageNext = sm;
	}

	/** Set the next sign message.  This is called by SONAR when the
	 * messageNext attribute is set.  The ownerNext attribute should have
	 * been set by the client prior to setting this attribute. */
	public void doSetMessageNext(SignMessage sm) throws TMSException {
		User o_next = ownerNext;	// Avoid race
		// ownerNext is only valid for one message, clear it
		ownerNext = null;
		if (o_next == null)
			throw new ChangeVetoException("MUST SET OWNER FIRST");
		doSetMessageNext(sm, o_next);
	}

	/** Set the next sign message and owner */
	public synchronized void doSetMessageNext(SignMessage sm, User o)
		throws TMSException
	{
		DMSPoller p = getDMSPoller();
		if (p == null) {
			throw new ChangeVetoException(name +
				": NO ACTIVE POLLER");
		}
		if (shouldActivate(sm))
			doSetMessageNext(sm, o, p);
	}

	/**
	 * Set the next sign message.
	 * @param sm Sign message, may not be null.
	 * @param o User sending message, may be null.
	 * @param p DMS poller, may not be null.
	 */
	private void doSetMessageNext(SignMessage sm, User o, DMSPoller p)
		throws TMSException
	{
		SignMessage smn = validateMessage(sm);
		if (smn != sm)
			o = null;
		// FIXME: there should be a better way to clear cached routes
		//        in travel time estimator
		int ap = smn.getActivationPriority();
		if (ap == DMSMessagePriority.OVERRIDE.ordinal())
			formatter.clear();
		p.sendMessage(this, smn, o);
	}

	/** Check if the sign has a reference to a sign message */
	public boolean hasReference(SignMessage sm) {
		return sm == messageCurrent ||
		       sm == messageSched ||
		       sm == messageNext;
	}

	/** Validate a sign message to send.
	 * @param sm Sign message to validate.
	 * @return The sign message to send (may be a scheduled message). */
	private SignMessage validateMessage(SignMessage sm) throws TMSException{
		MultiString multi = new MultiString(sm.getMulti());
		SignMessage sched = messageSched;	// Avoid race
		if (sched != null && multi.isBlank()) {
			// Don't blank the sign if there's a scheduled message
			// -- send the scheduled message instead.
			try {
				validateBitmaps(sched,
					new MultiString(sched.getMulti()));
				return sched;
			}
			catch (TMSException e) {
				logSched("sched msg not valid: " +
					e.getMessage());
				// Ok, go ahead and blank the sign
			}
		}
		validateBitmaps(sm, multi);
		return sm;
	}

	/** Validate the message bitmaps */
	private void validateBitmaps(SignMessage sm, MultiString multi)
		throws TMSException
	{
		if (!multi.isValid()) {
			throw new InvalidMessageException(name +
				": INVALID MESSAGE, " + sm.getMulti());
		}
		try {
			validateBitmaps(sm.getBitmaps(), multi);
		}
		catch (IOException e) {
			throw new ChangeVetoException("Base64 decode error");
		}
		catch (IndexOutOfBoundsException e) {
			throw new ChangeVetoException(e.getMessage());
		}
	}

	/** Validate the message bitmaps */
	private void validateBitmaps(String bmaps, MultiString multi)
		throws IOException, ChangeVetoException
	{
		byte[] bitmaps = Base64.decode(bmaps);
		BitmapGraphic bitmap = createBlankBitmap();
		int blen = bitmap.length();
		if (blen == 0)
			throw new ChangeVetoException("Invalid sign size");
		if (bitmaps.length % blen != 0)
			throw new ChangeVetoException("Invalid bitmap length");
		if (!multi.isBlank()) {
			String[] pixels = pixelStatus;	// Avoid races
			if (pixels != null && pixels.length == 2)
				validateBitmaps(bitmaps, pixels, bitmap);
		}
	}

	/** Validate the message bitmaps */
	private void validateBitmaps(byte[] bitmaps, String[] pixels,
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
		if (b_off.length != blen || b_on.length != blen)
			return;
		stuckOff.setPixelData(b_off);
		stuckOn.setPixelData(b_on);
		int n_pages = bitmaps.length / blen;
		byte[] b = new byte[blen];
		for (int p = 0; p < n_pages; p++) {
			System.arraycopy(bitmaps, p * blen, b, 0, blen);
			bitmap.setPixelData(b);
			bitmap.union(stuckOff);
			int n_lit = bitmap.getLitCount();
			if (n_lit > off_limit) {
				throw new ChangeVetoException(
					"Too many stuck off pixels: " + n_lit);
			}
			bitmap.setPixelData(b);
			bitmap.outline();
			bitmap.union(stuckOn);
			n_lit = bitmap.getLitCount();
			if (n_lit > on_limit) {
				throw new ChangeVetoException(
					"Too many stuck on pixels: " + n_lit);
			}
		}
	}

	/** Create a blank bitmap */
	private BitmapGraphic createBlankBitmap()
		throws ChangeVetoException
	{
		Integer w = widthPixels;	// Avoid race
		Integer h = heightPixels;	// Avoid race
		if (w != null && h != null)
			return new BitmapGraphic(w, h);
		else
			throw new ChangeVetoException("Width/height is null");
	}

	/** Check if a message should be activated based on priority.
	 * @param sm SignMessage being activated.
	 * @return true If priority is high enough to deploy. */
	public boolean shouldActivate(SignMessage sm) {
		if (sm != null) {
			DMSMessagePriority ap = DMSMessagePriority.fromOrdinal(
			       sm.getActivationPriority());
			return shouldActivate(ap, sm.getScheduled()) &&
			       SignMessageHelper.lookup(sm.getName()) == sm;
		} else
			return false;
	}

	/** Test if a message should be activated.
	 * @param ap Activation priority.
	 * @param sched Scheduled flag.
	 * @return True if message should be activated; false otherwise. */
	private boolean shouldActivate(DMSMessagePriority ap, boolean sched) {
		return shouldActivate(messageCurrent, ap, sched) &&
		       shouldActivate(messageNext, ap, sched);
	}

	/** Test if a sign message should be activated.
	 * @param existing Message existing on DMS.
	 * @param ap Activation priority.
	 * @param sched Scheduled flag.
	 * @return True if message should be activated; false otherwise. */
	static private boolean shouldActivate(SignMessage existing,
		DMSMessagePriority ap, boolean sched)
	{
		if (existing == null)
			return true;
		if (existing.getScheduled() && sched)
			return true;
		return ap.ordinal() >= existing.getRunTimePriority();
	}

	/** Send a sign message created by IRIS server.
	 * @param m MULTI string.
	 * @param be Beacon enabled.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param sch Scheduled flag. */
	public void sendMessage(String m, boolean be, DMSMessagePriority ap,
		DMSMessagePriority rp, boolean sch)
	{
		if (getMessageCurrent().getMulti().equals(m))
			return;
		SignMessage sm = createMessage(m, be, ap, rp, sch, null);
		try {
			if (!isMessageCurrentEquivalent(sm))
				doSetMessageNext(sm, null);
		}
		catch (TMSException e) {
			logError(e.getMessage());
		}
	}

	/** Current message (Shall not be null) */
	private transient SignMessage messageCurrent = createBlankMessage();

	/**
	 * Set the current message.
	 * @param sm Sign message
	 * @param o User associated with sign message
	 */
	public void setMessageCurrent(SignMessage sm, User o) {
		if (!isMessageCurrentEquivalent(sm)) {
			logMessage(sm, o);
			setDeployTime();
			messageCurrent = sm;
			notifyAttribute("messageCurrent");
			ownerCurrent = o;
			notifyAttribute("ownerCurrent");
			updateStyles();
		}
		updateBeacon();
	}

	/** Get the current messasge.
	 * @return Currently active message (cannot be null) */
	@Override
	public SignMessage getMessageCurrent() {
		return messageCurrent;
	}

	/** Test if the current message is equivalent to a sign message */
	public boolean isMessageCurrentEquivalent(SignMessage sm) {
		return SignMessageHelper.isEquivalent(messageCurrent, sm);
	}

	/** Owner of current message */
	private transient User ownerCurrent;

	/** Get the current message owner.
	 * @return User who deployed the current message. */
	@Override
	public User getOwnerCurrent() {
		return ownerCurrent;
	}

	/**
	 * Log a message.
	 * @param sm Sign message
	 * @param o User associated with sign message
	 */
	private void logMessage(SignMessage sm, User o) {
		EventType et = EventType.DMS_DEPLOYED;
		String text = sm.getMulti();
		if (SignMessageHelper.isBlank(sm)) {
			et = EventType.DMS_CLEARED;
			text = null;
		}
		String owner = null;
		if (o != null)
			owner = o.getName();
		logEvent(new SignStatusEvent(et, name, text, owner));
	}

	/** Log a sign status event */
	private void logEvent(final SignStatusEvent ev) {
		FLUSH.addJob(new Job() {
			public void perform() throws TMSException {
				ev.doStore();
			}
		});
	}

	/** Message deploy time */
	private long deployTime = 0;

	/** Set the message deploy time */
	private void setDeployTime() {
		deployTime = TimeSteward.currentTimeMillis();
		notifyAttribute("deployTime");
	}

	/** Get the message deploy time.
	 * @return Time message was deployed (ms since epoch).
	 * @see java.lang.System.currentTimeMillis */
	@Override
	public long getDeployTime() {
		return deployTime;
	}

	/** Get the DMS poller */
	private DMSPoller getDMSPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof DMSPoller) ? (DMSPoller)dp : null;
	}

	/** LDC pot base (Ledstar-specific value) */
	private transient Integer ldcPotBase;

	/** Set the LDC pot base */
	@Override
	public void setLdcPotBase(Integer base) {
		if (!integerEquals(base, ldcPotBase)) {
			ldcPotBase = base;
			notifyAttribute("ldcPotBase");
		}
	}

	/** Get the LDC pot base */
	@Override
	public Integer getLdcPotBase() {
		return ldcPotBase;
	}

	/** Pixel low current threshold (Ledstar-specific value) */
	private transient Integer pixelCurrentLow;

	/** Set the pixel low curent threshold */
	@Override
	public void setPixelCurrentLow(Integer low) {
		if (!integerEquals(low, pixelCurrentLow)) {
			pixelCurrentLow = low;
			notifyAttribute("pixelCurrentLow");
		}
	}

	/** Get the pixel low current threshold */
	@Override
	public Integer getPixelCurrentLow() {
		return pixelCurrentLow;
	}

	/** Pixel high current threshold (Ledstar-specific value) */
	private transient Integer pixelCurrentHigh;

	/** Set the pixel high curent threshold */
	@Override
	public void setPixelCurrentHigh(Integer high) {
		if (!integerEquals(high, pixelCurrentHigh)) {
			pixelCurrentHigh = high;
			notifyAttribute("pixelCurrentHigh");
		}
	}

	/** Get the pixel high current threshold */
	@Override
	public Integer getPixelCurrentHigh() {
		return pixelCurrentHigh;
	}

	/** Sign face heat tape status */
	private transient String heatTapeStatus;

	/** Set sign face heat tape status */
	public void setHeatTapeStatus(String h) {
		if (!h.equals(heatTapeStatus)) {
			heatTapeStatus = h;
			notifyAttribute("heatTapeStatus");
		}
	}

	/** Get sign face heat tape status */
	@Override
	public String getHeatTapeStatus() {
		return heatTapeStatus;
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

	/** Interface for handling brightness samples */
	static public interface BrightnessHandler {
		void feedback(EventType et, int photo, int output);
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param p Activation priority.
	 * @return New sign message, or null on error. */
	public SignMessage createMessage(String m, boolean be,
		DMSMessagePriority ap)
	{
		MultiString ms = new MultiString(m);
		if (ms.isBlank())
			return createBlankMessage(ap);
		else {
			return createMessage(m, be, ap,
				DMSMessagePriority.OPERATOR, null);
		}
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	public SignMessage createMessage(String m, boolean be,
		DMSMessagePriority ap, DMSMessagePriority rp, Integer d)
	{
		boolean sch = DMSMessagePriority.isScheduled(rp);
		return createMessage(m, be, ap, rp, sch, d);
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param sch Scheduled flag.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage createMessage(String m, boolean be,
		DMSMessagePriority ap, DMSMessagePriority rp, boolean sch,
		Integer d)
	{
		try {
			BitmapGraphic[] pages = DMSHelper.createBitmaps(this,m);
			if (pages != null)
				return createMessageB(m, be, pages,ap,rp,sch,d);
		}
		catch (InvalidMessageException e) {
			logError("invalid msg: " + e.getMessage());
		}
		return null;
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pages Pre-rendered graphics for all pages.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	public SignMessage createMessage(String m, boolean be,
		BitmapGraphic[] pages, DMSMessagePriority ap,
		DMSMessagePriority rp, Integer d)
	{
		boolean sch = DMSMessagePriority.isScheduled(rp);
		return createMessage(m, be, pages, ap, rp, sch, d);
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pages Pre-rendered graphics for all pages.
	 * @return New sign message, or null on error. */
	public SignMessage createMessage(String m, boolean be,
		BitmapGraphic[] pages)
	{
		BitmapGraphic[] bmaps = copyBitmaps(pages);
		if (bmaps != null) {
			String ebm = encodeBitmaps(bmaps);
			SignMessage esm = SignMessageHelper.find(m, be, ebm);
			if (esm != null)
				return esm;
			else {
				DMSMessagePriority p =
					DMSMessagePriority.OTHER_SYSTEM;
				boolean sch = DMSMessagePriority.isScheduled(p);
				return createMessageC(m, be, ebm, p, p, sch,
					null);
			}
		} else
			return null;
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pages Pre-rendered graphics for all pages.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param sch Scheduled flag.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage createMessage(String m, boolean be,
		BitmapGraphic[] pages, DMSMessagePriority ap,
		DMSMessagePriority rp, boolean sch, Integer d)
	{
		BitmapGraphic[] bmaps = copyBitmaps(pages);
		if (bmaps != null)
			return createMessageB(m, be, bmaps, ap, rp, sch, d);
		else
			return null;
	}

	/** Copy an array of bitmaps into the DMS dimensions.
	 * @param pages Array of bitmap graphics.
	 * @return Bitmap graphics with same dimensions as DMS, or null. */
	private BitmapGraphic[] copyBitmaps(BitmapGraphic[] pages) {
		Integer w = widthPixels;
		Integer h = heightPixels;
		if (w == null || w < 1)
			return null;
		if (h == null || h < 1)
			return null;
		BitmapGraphic[] bmaps = new BitmapGraphic[pages.length];
		for (int i = 0; i < bmaps.length; i++) {
			bmaps[i] = new BitmapGraphic(w, h);
			bmaps[i].copy(pages[i]);
		}
		return bmaps;
	}

	/** Encode bitmap data */
	private String encodeBitmaps(BitmapGraphic[] pages) {
		int blen = pages[0].length();
		byte[] bitmap = new byte[pages.length * blen];
		for (int i = 0; i < pages.length; i++) {
			byte[] page = pages[i].getPixelData();
			System.arraycopy(page, 0, bitmap, i * blen, blen);
		}
		return Base64.encode(bitmap);
	}

	/** Create a new message (B version).
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pages Pre-rendered graphics for all pages.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param sch Scheduled flag.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage createMessageB(String m, boolean be,
		BitmapGraphic[] pages, DMSMessagePriority ap,
		DMSMessagePriority rp, boolean sch, Integer d)
	{
		return createMessage(m, be, encodeBitmaps(pages), ap, rp,sch,d);
	}

	/** Create a new sign message.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param b Message bitmaps (Base64).
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param sch Scheduled flag.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage createMessage(String m, boolean be, String b,
		DMSMessagePriority ap, DMSMessagePriority rp, boolean sch,
		Integer d)
	{
		SignMessage esm = SignMessageHelper.find(m, b, ap, rp, sch, d);
		if (esm != null)
			return esm;
		else
			return createMessageC(m, be, b, ap, rp, sch, d);
	}

	/** Create a new sign message (C version).
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param b Message bitmaps (Base64).
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param s Scheduled flag.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage createMessageC(String m, boolean be, String b,
		DMSMessagePriority ap, DMSMessagePriority rp, boolean s,
		Integer d)
	{
		SignMessageImpl sm = new SignMessageImpl(m, be, b, ap, rp, s,d);
		try {
			sm.notifyCreate();
			return sm;
		}
		catch (SonarException e) {
			// This can pretty much only happen when the SONAR task
			// processor does not store the sign message within 30
			// seconds.  It *shouldn't* happen, but there may be
			// a rare bug which triggers it.
			logError("createMessageC: " + e.getMessage());
			return null;
		}
	}

	/** Flag for current scheduled message.  This is used to guarantee that
	 * performAction is called at least once between each call to
	 * updateScheduledMessage.  If not, then the scheduled message is
	 * cleared. */
	private transient boolean is_scheduled;

	/** Current scheduled message */
	private transient SignMessage messageSched = null;

	/** Get the scheduled sign messasge.
	 * @return Scheduled sign message */
	@Override
	public SignMessage getMessageSched() {
		return messageSched;
	}

	/** Set the scheduled sign message.
	 * @param sm New scheduled sign message */
	private void setMessageSched(SignMessage sm) {
		if (!SignMessageHelper.isEquivalent(messageSched, sm)) {
			messageSched = sm;
			notifyAttribute("messageSched");
		}
	}

	/** Check if a DMS action is deployable */
	public boolean isDeployable(DmsAction da) {
		if (hasError())
			return false;
		SignMessage sm = createMessage(da);
		try {
			return sm == validateMessage(sm);
		}
		catch (TMSException e) {
			return false;
		}
	}

	/** Perform a DMS action */
	public void performAction(DmsAction da) {
		SignMessage sm = createMessage(da);
		if (sm != null) {
			if (shouldReplaceScheduled(sm)) {
				setMessageSched(sm);
				is_scheduled = true;
			}
		}
	}

	/** Test if the given sign message should replace the current
	 * scheduled message. */
	private boolean shouldReplaceScheduled(SignMessage sm) {
		SignMessage s = messageSched;	// Avoid NPE
		return s == null ||
		       sm.getActivationPriority() > s.getActivationPriority() ||
		       sm.getRunTimePriority() >= s.getRunTimePriority();
	}

	/** Create a message for the sign.
	 * @param da DMS action
	 * @return New sign message, or null on error */
	private SignMessage createMessage(DmsAction da) {
		String m = formatter.createMulti(da);
		if (m != null) {
			boolean be = da.getBeaconEnabled();
			DMSMessagePriority ap = DMSMessagePriority.fromOrdinal(
				da.getActivationPriority());
			DMSMessagePriority rp = DMSMessagePriority.fromOrdinal(
				da.getRunTimePriority());
			Integer d = getDuration(da);
			return createMessage(m, be, ap, rp, true, d);
		} else
			return null;
	}

	/** Get the duration of a DMS action. */
	private Integer getDuration(DmsAction da) {
		return da.getActionPlan().getSticky() ? null :
			getUnstickyDuration();
	}

	/** Get the duration of an unsticky action */
	private int getUnstickyDuration() {
		/** FIXME: this should be twice the polling period for the
		 *         sign.  Modem signs should have a longer duration. */
		return 1;
	}

	/** Log a DMS message */
	private void logError(String msg) {
		if (DMS_LOG.isOpen())
			DMS_LOG.log(getName() + ": " + msg);
	}

	/** Log a schedule message */
	private void logSched(String msg) {
		if (SCHED_LOG.isOpen())
			SCHED_LOG.log(getName() + ": " + msg);
	}

	/** Update the scheduled message on the sign */
	public void updateScheduledMessage() {
		if (!is_scheduled) {
			logSched("no message scheduled");
			setMessageSched(createBlankScheduledMessage());
		}
		SignMessage sm = messageSched;
		if (shouldActivate(sm)) {
			try {
				logSched("set message to " + sm.getMulti());
				doSetMessageNext(sm, null);
			}
			catch (TMSException e) {
				logSched(e.getMessage());
			}
		} else if (sm != null)
			logSched("sched msg not sent " + sm.getMulti());
		is_scheduled = false;
	}

	/** Create a blank scheduled message */
	private SignMessage createBlankScheduledMessage() {
		if (isCurrentScheduled())
			return createBlankMessage();
		else
			return null;
	}

	/** Test if the current message is scheduled */
	private boolean isCurrentScheduled() {
		// If either the current or next message is not scheduled,
		// then we won't consider the message scheduled
		SignMessage cur = messageCurrent;
		SignMessage nxt = messageNext;
		return (cur == null || cur.getScheduled()) &&
		       (nxt == null || nxt.getScheduled());
	}

	/** Test if DMS is part of an LCS array */
	private boolean isLCS() {
		return LCSHelper.lookup(name) != null;
	}

	/** Test if DMS is associated with a gate arm array */
	private boolean isForGateArm() {
		return GateArmArrayHelper.checkDMS(this);
	}

	/** Test if DMS is online (active and not failed) */
	public boolean isOnline() {
		return isActive() && !isFailed();
	}

	/** Test if DMS is available */
	private boolean isAvailable() {
		return isOnline() && isMsgBlank() && !needsMaintenance();
	}

	/** Test if current message is blank */
	public boolean isMsgBlank() {
		return SignMessageHelper.isBlank(messageCurrent);
	}

	/** Test if the current message is "scheduled" */
	private boolean isMsgScheduled() {
		return getMessageCurrent().getScheduled();
	}

	/** Test if the current message has beacon enabled */
	private boolean isMsgBeacon() {
		return getMessageCurrent().getBeaconEnabled();
	}

	/** Test if the current message is a travel time */
	private boolean isMsgTravelTime() {
		SignMessage sm = getMessageCurrent();
		return sm.getRunTimePriority() == TRAVEL_TIME.ordinal();
	}

	/** Test if the current message is AWS */
	private boolean isMsgAws() {
		SignMessage sm = getMessageCurrent();
		return sm.getRunTimePriority() == AWS.ordinal();
	}

	/** Test if a DMS is active, not failed and deployed */
	public boolean isMsgDeployed() {
		return isOnline() && !isMsgBlank();
	}

	/** Test if a DMS has been deployed by a user */
	public boolean isUserDeployed() {
		return isMsgDeployed() && !isMsgScheduled() && !isMsgAws();
	}

	/** Test if a DMS has been deployed by schedule */
	public boolean isScheduleDeployed() {
		return isMsgDeployed() && isMsgScheduled();
	}

	/** Test if a DMS has been deployed by travel time */
	private boolean isTravelTimeDeployed() {
		return isMsgDeployed() && isMsgTravelTime();
	}

	/** Test if a DMS is active, not failed and deployed by AWS */
	private boolean isAwsDeployed() {
		return isMsgDeployed() && isMsgAws();
	}

	/** Test if a DMS can be controlled by AWS */
	private boolean isAwsControlled() {
		return getAwsAllowed() && getAwsControlled();
	}

	/** Test if DMS needs maintenance */
	public boolean needsMaintenance() {
		if (!isOnline())
			return false;
		if (hasCriticalError())
			return true;
		return !DMSHelper.getMaintenance(this).isEmpty();
	}

	/** Test if DMS has a critical error */
	private boolean hasCriticalError() {
		return !DMSHelper.getCriticalError(this).isEmpty();
	}

	/** Item style bits */
	private transient long styles = 0;

	/** Update the DMS styles */
	@Override
	public void updateStyles() {
		long s = ItemStyle.ALL.bit();
		if (getController() == null)
			s |= ItemStyle.NO_CONTROLLER.bit();
		if (isLCS())
			s |= ItemStyle.LCS.bit();
		else {
			if (needsMaintenance())
				s |= ItemStyle.MAINTENANCE.bit();
			if (isActive() && isFailed())
				s |= ItemStyle.FAILED.bit();
			if (!isActive())
				s |= ItemStyle.INACTIVE.bit();
			if (!isForGateArm()) {
				if (isAvailable())
					s |= ItemStyle.AVAILABLE.bit();
				if (isUserDeployed())
					s |= ItemStyle.DEPLOYED.bit();
				if (isTravelTimeDeployed())
					s |= ItemStyle.TRAVEL_TIME.bit();
				if (isScheduleDeployed())
					s |= ItemStyle.SCHEDULED.bit();
				if (isAwsDeployed())
					s |= ItemStyle.AWS_DEPLOYED.bit();
				if (isAwsControlled())
					s |= ItemStyle.AWS_CONTROLLED.bit();
			}
		}
		setStyles(s);
	}

	/** Set the item style bits (and notify clients) */
	private void setStyles(long s) {
		if (s != styles) {
			styles = s;
			notifyAttribute("styles");
		}
	}

	/** Get item style bits */
	@Override
	public long getStyles() {
		return styles;
	}

	/** Write DMS as an XML element */
	public void writeXml(Writer w) throws IOException {
		w.write("<dms");
		w.write(createAttribute("name", getName()));
		w.write(createAttribute("description",
			GeoLocHelper.getDescription(geo_loc)));
		Position pos = GeoLocHelper.getWgs84Position(geo_loc);
		if (pos != null) {
			w.write(createAttribute("lon",
				formatDouble(pos.getLongitude())));
			w.write(createAttribute("lat",
				formatDouble(pos.getLatitude())));
		}
		w.write(createAttribute("width_pixels", getWidthPixels()));
		w.write(createAttribute("height_pixels", getHeightPixels()));
		w.write("/>\n");
	}

	/** Write the sign message as xml */
	public void writeSignMessageXml(Writer w) throws IOException {
		SignMessage msg = getMessageCurrent();
		if (msg instanceof SignMessageImpl)
			((SignMessageImpl)msg).writeXml(w, this);
	}

	/** Check if the sign is an active dialup sign */
	public boolean isActiveDialup() {
		return isActive() && (isDmsXMLDialup() || hasModemCommLink());
	}

	/** Check if the sign is periodically queriable */
	public boolean isPeriodicallyQueriable() {
		return (!isDmsXMLDialup()) &&
		       ((!hasModemCommLink()) || isConnected());
	}

	/** Check if the sign is a DMSXML dialup sign */
	private boolean isDmsXMLDialup() {
		// FIXME: signAccess is supposed to indicate the *physical*
		//        access of the DMS.  It was never intended to be used
		//        in this manner. This is an agency-specific hack
		//        (Caltrans).
		return SString.containsIgnoreCase(getSignAccess(), "dialup");
	}
}
