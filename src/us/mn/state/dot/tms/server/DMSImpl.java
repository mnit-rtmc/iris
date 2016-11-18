/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsMsgPriority;
import static us.mn.state.dot.tms.DmsMsgPriority.*;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.event.BrightnessSample;
import us.mn.state.dot.tms.server.event.PriceMessageEvent;
import us.mn.state.dot.tms.server.event.SignStatusEvent;
import us.mn.state.dot.tms.utils.Base64;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Dynamic Message Sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSImpl extends DeviceImpl implements DMS, Comparable<DMSImpl> {

	/** External operator source */
	static private final int EXT_OPER = SignMsgSource.toBits(
		SignMsgSource.external, SignMsgSource.operator);

	/** Minimum duration of a DMS action (minutes) */
	static private final int DURATION_MINIMUM_MINS = 1;

	/** Number of polling periods for DMS action duration */
	static private final int DURATION_PERIODS = 3;

	/** Interface for handling brightness samples */
	static public interface BrightnessHandler {
		void feedback(EventType et, int photo, int output);
	}

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
		map.put("beacon", beacon);
		map.put("preset", preset);
		map.put("aws_allowed", awsAllowed);
		map.put("aws_controlled", awsControlled);
		map.put("default_font", default_font);
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

	/** Compare to another holiday */
	@Override
	public int compareTo(DMSImpl o) {
		return name.compareTo(o.name);
	}

	/** Tolling formatter */
	private final TollingFormatter toll_formatter;

	/** MULTI message formatter */
	private final MultiFormatter formatter;

	/** Create a new DMS with a string name */
	public DMSImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
		toll_formatter = new TollingFormatter(n, g);
		formatter = new MultiFormatter(this, toll_formatter);
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
		toll_formatter = new TollingFormatter(n, loc);
		formatter = new MultiFormatter(this, toll_formatter);
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
	public void setVersionNotify(String v) {
		ControllerImpl c = (ControllerImpl) getController();
		if (c != null)
			c.setVersionNotify(v);
		if (!v.equals(version)) {
			version = v;
			notifyAttribute("version");
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
		if (!objectEquals(h, faceHeight)) {
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
		if (!objectEquals(w, faceWidth)) {
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
		if (!objectEquals(b, horizontalBorder)) {
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
		if (!objectEquals(b, verticalBorder)) {
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
		if (!objectEquals(p, horizontalPitch)) {
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
		if (!objectEquals(p, verticalPitch)) {
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
		if (!objectEquals(h, heightPixels)) {
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
		if (!objectEquals(w, widthPixels)) {
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
		if (!objectEquals(h, charHeightPixels)) {
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
		if (!objectEquals(w, charWidthPixels)) {
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
		if (!objectEquals(t, minCabinetTemp)) {
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
		if (!objectEquals(t, maxCabinetTemp)) {
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
		if (!objectEquals(t, minAmbientTemp)) {
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
		if (!objectEquals(t, maxAmbientTemp)) {
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
		if (!objectEquals(t, minHousingTemp)) {
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
		if (!objectEquals(t, maxHousingTemp)) {
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
		if (!objectEquals(l, lightOutput)) {
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

	/** Create a blank message for the sign */
	public SignMessage createMsgBlank() {
		return createMsgBlank(OVERRIDE);
	}

	/** Create a blank message for the sign */
	public SignMessage createMsgBlank(DmsMsgPriority ap) {
		String bmaps = Base64.encode(new byte[0]);
		return findOrCreateMsg("", false, bmaps, ap, BLANK,
			SignMsgSource.blank.ordinal(), null, null);
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param src Message source.
	 * @param o Owner name.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	public SignMessage createMsg(String m, boolean be, DmsMsgPriority ap,
		DmsMsgPriority rp, int src, String o, Integer d)
	{
		String bmaps = renderBitmaps(m);
		if (bmaps != null)
			return findOrCreateMsg(m, be, bmaps, ap, rp, src, o, d);
		else
			return null;
	}

	/** Render bitmaps for all pages of a message.
	 * @param m MULTI string for message.
	 * @return Base64-encoded bitmaps, or null on error. */
	private String renderBitmaps(String m) {
		try {
			BitmapGraphic[] pages = DMSHelper.createBitmaps(this,m);
			if (pages != null)
				return encodeBitmaps(pages);
		}
		catch (InvalidMsgException e) {
			logError(e.getMessage());
		}
		return null;
	}

	/** Encode bitmaps to Base64.
	 * @param pages Bitmap graphics for all pages.
	 * @return Base64-encoded bitmaps. */
	private String encodeBitmaps(BitmapGraphic[] pages) {
		int blen = pages[0].length();
		byte[] b_data = new byte[pages.length * blen];
		for (int i = 0; i < pages.length; i++) {
			byte[] page = pages[i].getPixelData();
			System.arraycopy(page, 0, b_data, i * blen, blen);
		}
		return Base64.encode(b_data);
	}

	/** Find or create a sign message.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param bmaps Message bitmaps (Base64).
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param src Message source.
	 * @param o Owner name.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage findOrCreateMsg(String m, boolean be, String bmaps,
		DmsMsgPriority ap, DmsMsgPriority rp, int src, String o,
		Integer d)
	{
		SignMessage esm = SignMessageHelper.find(m, bmaps, ap, rp, src,
			o, d);
		if (esm != null)
			return esm;
		else
			return createMsgNotify(m, be, bmaps, ap, rp, src, o, d);
	}

	/** Create a new sign message and notify clients.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param bmaps Message bitmaps (Base64).
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param src Message source.
	 * @param o Owner name.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage createMsgNotify(String m, boolean be, String bmaps,
		DmsMsgPriority ap, DmsMsgPriority rp, int src, String o,
		Integer d)
	{
		SignMessageImpl sm = new SignMessageImpl(m, be, bmaps, ap, rp,
			src, o, d);
		try {
			sm.notifyCreate();
			return sm;
		}
		catch (SonarException e) {
			// This can pretty much only happen when the SONAR task
			// processor does not store the sign message within 30
			// seconds.  It *shouldn't* happen, but there may be
			// a rare bug which triggers it.
			logError("createMsgNotify: " + e.getMessage());
			return null;
		}
	}

	/** Create a pre-rendered message for the sign (ADDCO, DMSXML).
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pages Pre-rendered graphics for all pages.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	public SignMessage createMsgRendered(String m, boolean be,
		BitmapGraphic[] pages, DmsMsgPriority ap,
		DmsMsgPriority rp, Integer d)
	{
		String bmaps = encodeAdjustedBitmaps(pages);
		if (bmaps != null) {
			return findOrCreateMsg(m, be, bmaps, ap, rp, EXT_OPER,
			                       null, d);
		} else
			return null;
	}

	/** Encode bitmaps to Base64 after adjusting dimensions.
	 * @param pages Bitmap graphics for all pages.
	 * @return Base64-encoded bitmaps, or null on error. */
	private String encodeAdjustedBitmaps(BitmapGraphic[] pages) {
		BitmapGraphic[] p = copyBitmaps(pages);
		return (p != null) ? encodeBitmaps(p) : null;
	}

	/** Copy an array of bitmaps into the DMS dimensions.
	 * @param pages Array of bitmap graphics.
	 * @return Bitmap graphics with same dimensions as DMS, or null. */
	private BitmapGraphic[] copyBitmaps(BitmapGraphic[] pages) {
		Integer w = widthPixels;
		Integer h = heightPixels;
		if (null == w || w < 1)
			return null;
		if (null == h || h < 1)
			return null;
		BitmapGraphic[] p = new BitmapGraphic[pages.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = new BitmapGraphic(w, h);
			p[i].copy(pages[i]);
		}
		return p;
	}

	/** Create a scheduled message.
	 * @param da DMS action
	 * @return New sign message, or null on error */
	private SignMessage createMsgSched(DmsAction da) {
		String m = formatter.createMulti(da);
		if (m != null) {
			boolean be = da.getBeaconEnabled();
			DmsMsgPriority ap = DmsMsgPriority.fromOrdinal(
				da.getActivationPriority());
			DmsMsgPriority rp = DmsMsgPriority.fromOrdinal(
				da.getRunTimePriority());
			int src = SignMsgSource.schedule.ordinal();
			if (formatter.isTolling(da))
				src |= SignMsgSource.tolling.ordinal();
			Integer d = getDuration(da);
			return createMsg(m, be, ap, rp, src, null, d);
		} else
			return null;
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
		return getPollPeriod() * DURATION_PERIODS / 60;
	}

	/** User selected sign message */
	private transient SignMessage msg_user = createMsgBlank();

	/** Set the user selected sign message */
	@Override
	public void setMsgUser(SignMessage sm) {
		msg_user = sm;
	}

	/** Set the user selected sign message.  Called by SONAR. */
	public void doSetMsgUser(SignMessage sm) throws TMSException {
		SignMessageHelper.validate(sm, this);
		setMsgUser(sm);
		sendMsg();
	}

	/** Scheduled sign message */
	private transient SignMessage msg_sched = createMsgBlank();

	/** Get the scheduled sign messasge.
	 * @return Scheduled sign message */
	@Override
	public SignMessage getMsgSched() {
		return msg_sched;
	}

	/** Check a DMS action.
	 * @param da DMS action.
	 * @return true if action is valid. */
	public boolean checkAction(DmsAction da) {
		return createMsgSched(da) != null;
	}

	/** Set the scheduled DMS action */
	public void setScheduledAction(DmsAction da) {
		SignMessage sm = (da != null) ? createMsgSched(da) : null;
		if (null == sm)
			sm = createMsgBlank();
		setMsgSchedNotify(sm);
		setPrices(da);
		try {
			sendMsg();
		}
		catch (TMSException e) {
			logError("sendMsg: " + e.getMessage());
		}
	}

	/** Set the scheduled sign message.
	 * @param sm New scheduled sign message */
	private void setMsgSchedNotify(SignMessage sm) {
		if (!SignMessageHelper.isEquivalent(msg_sched, sm)) {
			msg_sched = sm;
			notifyAttribute("msgSched");
		}
	}

	/** Tolling prices */
	private transient HashMap<String, Float> prices;

	/** Set tolling prices */
	private void setPrices(DmsAction da) {
		prices = (da != null) ? calculatePrices(da) : null;
	}

	/** Calculate prices for a tolling message */
	private HashMap<String, Float> calculatePrices(DmsAction da) {
		QuickMessage qm = da.getQuickMessage();
		if (qm != null)
			return toll_formatter.calculatePrices(qm.getMulti());
		else
			return null;
	}

	/** Log price (tolling) messages.
	 * @param et Event type. */
	private void logPriceMessages(EventType et) {
		HashMap<String, Float> p = prices;
		if (p != null) {
			for (Map.Entry<String, Float> ent: p.entrySet()) {
				String tz = ent.getKey();
				Float price = ent.getValue();
				logEvent(new PriceMessageEvent(et, name, tz,
				                               price));
			}
		}
	}

	/** Current message (Shall not be null) */
	private transient SignMessage msg_current = createMsgBlank();

	/** Set the current message.
	 * @param sm Sign message. */
	public void setMsgCurrentNotify(SignMessage sm) {
		if (SignMsgSource.tolling.checkBit(sm.getSource()))
			logPriceMessages(EventType.PRICE_VERIFIED);
		if (!isMsgCurrentEquivalent(sm)) {
			logMsg(sm);
			setDeployTime();
			msg_current = sm;
			notifyAttribute("msgCurrent");
			updateStyles();
		}
		updateBeacon();
	}

	/** Get the current messasge.
	 * @return Currently active message (cannot be null) */
	@Override
	public SignMessage getMsgCurrent() {
		return msg_current;
	}

	/** Test if the current message is equivalent to a sign message */
	public boolean isMsgCurrentEquivalent(SignMessage sm) {
		return SignMessageHelper.isEquivalent(msg_current, sm);
	}

	/** Log a message.
	 * @param sm Sign message. */
	private void logMsg(SignMessage sm) {
		EventType et = EventType.DMS_DEPLOYED;
		String text = sm.getMulti();
		if (SignMessageHelper.isBlank(sm)) {
			et = EventType.DMS_CLEARED;
			text = null;
		}
		String owner = sm.getOwner();
		logEvent(new SignStatusEvent(et, name, text, owner));
	}

	/** Next sign message (sending in process) */
	private transient SignMessage msg_next;

	/** Set the next sign message.  This must be called by operations after
	 * getting exclusive device ownership.  It must be set back to null
	 * after the operation completes.  This is necessary to prevent the
	 * ReaperJob from destroying a SignMessage before it has been sent to
	 * a sign.
	 * @see DeviceImpl.acquire */
	public void setMsgNext(SignMessage sm) {
		msg_next = sm;
	}

	/** Send message to DMS */
	private void sendMsg() throws TMSException {
		DMSPoller p = getDMSPoller();
		if (null == p) {
			throw new ChangeVetoException(name +
				": NO ACTIVE POLLER");
		}
		SignMessage sm = getMsgUserSched();
		SignMessageHelper.validate(sm, this);
		sendMsg(p, sm);
	}

	/** Get user/scheduled composite sign message.
	 * @return The composite sign message. */
	private SignMessage getMsgUserSched() throws TMSException {
		SignMessage user = msg_user;	// Avoid race
		SignMessage sched = msg_sched;	// Avoid race
		if (SignMessageHelper.isBlank(user) && isMsgValid(sched))
			return sched;
		if (isPrefixPage(sched)) {
			MultiString multi = new MultiString(user.getMulti());
			String ms = multi.addPagePrefix(sched.getMulti());
			boolean be = user.getBeaconEnabled();
			DmsMsgPriority ap = DmsMsgPriority.fromOrdinal(
				user.getActivationPriority());
			DmsMsgPriority rp = DmsMsgPriority.fromOrdinal(
				user.getRunTimePriority());
			int src = user.getSource() | sched.getSource();
			String o = user.getOwner();
			Integer d = user.getDuration();
			SignMessage sm = createMsg(ms, be, ap, rp, src, o, d);
			if (sm != null && isMsgValid(sm))
				return sm;
		}
		return user;
	}

	/** Is scheduled message using PREFIX_PAGE? */
	private boolean isPrefixPage(SignMessage sm) {
		return sm.getActivationPriority() ==
		       DmsMsgPriority.PREFIX_PAGE.ordinal();
	}

	/** Check if a sign message is valid */
	private boolean isMsgValid(SignMessage sm) {
		try {
			SignMessageHelper.validate(sm, this);
			return true;
		}
		catch (TMSException e) {
			logError("msg invalid: " + e.getMessage());
			return false;
		}
	}

	/** Set the next sign message.
	 * @param p DMS poller.
	 * @param sm Sign message. */
	private void sendMsg(DMSPoller p, SignMessage sm) {
		if (SignMsgSource.tolling.checkBit(sm.getSource()))
		    logPriceMessages(EventType.PRICE_DEPLOYED);
		// FIXME: there should be a better way to clear cached routes
		//        in travel time estimator
		if (OVERRIDE.ordinal() == sm.getActivationPriority())
			formatter.clear();
		p.sendMessage(this, sm);
	}

	/** Check if the sign has a reference to a sign message */
	public boolean hasReference(final SignMessage sm) {
		return sm == msg_user ||
		       sm == msg_sched ||
		       sm == msg_current ||
		       sm == msg_next;
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

	/** LDC pot base (Ledstar-specific value) */
	private transient Integer ldcPotBase;

	/** Set the LDC pot base */
	@Override
	public void setLdcPotBase(Integer base) {
		if (!objectEquals(base, ldcPotBase)) {
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
		if (!objectEquals(low, pixelCurrentLow)) {
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
		if (!objectEquals(high, pixelCurrentHigh)) {
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

	/** Test if DMS is available */
	@Override
	protected boolean isAvailable() {
		return super.isAvailable() && isMsgBlank();
	}

	/** Test if current message is blank */
	public boolean isMsgBlank() {
		return SignMessageHelper.isBlank(msg_current);
	}

	/** Test if the current message source contains "operator" */
	private boolean isMsgOperator() {
		int src = getMsgCurrent().getSource();
		return SignMsgSource.operator.checkBit(src);
	}

	/** Test if the current message source contains "scheduled" */
	private boolean isMsgScheduled() {
		int src = getMsgCurrent().getSource();
		return SignMsgSource.schedule.checkBit(src);
	}

	/** Test if the current message has beacon enabled */
	private boolean isMsgBeacon() {
		return getMsgCurrent().getBeaconEnabled();
	}

	/** Test if the current message is a travel time */
	private boolean isMsgTravelTime() {
		SignMessage sm = getMsgCurrent();
		return sm.getRunTimePriority() == TRAVEL_TIME.ordinal();
	}

	/** Test if the current message is AWS */
	private boolean isMsgAws() {
		// FIXME: use SignMsgSource.aws
		SignMessage sm = getMsgCurrent();
		return sm.getRunTimePriority() == AWS.ordinal();
	}

	/** Test if a DMS is active, not failed and deployed */
	public boolean isMsgDeployed() {
		return isOnline() && !isMsgBlank();
	}

	/** Test if a DMS has been deployed by a user */
	public boolean isUserDeployed() {
		return isMsgDeployed() && !isMsgOperator() && !isMsgAws();
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
		boolean hidden = DmsSignGroupHelper.isHidden(this);
		long s = ItemStyle.ALL.bit();
		if (getController() == null)
			s |= ItemStyle.NO_CONTROLLER.bit();
		if (isActive())
			s |= ItemStyle.ACTIVE.bit();
		else
			s |= ItemStyle.INACTIVE.bit();
		if (hidden)
			s |= ItemStyle.HIDDEN.bit();
		else {
			if (isOnline() && needsMaintenance())
				s |= ItemStyle.MAINTENANCE.bit();
			if (isActive() && isFailed())
				s |= ItemStyle.FAILED.bit();
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
		return s;
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
		SignMessage msg = getMsgCurrent();
		if (msg instanceof SignMessageImpl)
			((SignMessageImpl) msg).writeXml(w, this);
	}

	/** Check if the sign is an active dialup sign */
	public boolean isActiveDialup() {
		return isActive() && hasModemCommLink();
	}

	/** Check if the sign is periodically queriable */
	public boolean isPeriodicallyQueriable() {
		return isConnected() || !hasModemCommLink();
	}

	/** Get the DMS poller */
	private DMSPoller getDMSPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof DMSPoller) ? (DMSPoller) dp : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		DMSPoller p = getDMSPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll() {
		if (isPeriodLong())
			sendDeviceRequest(DeviceRequest.QUERY_STATUS);
		sendDeviceRequest(DeviceRequest.QUERY_MESSAGE);
		LCSArrayImpl la = lookupLCSArray();
		if (la != null)
			la.periodicPoll();
		// FIXME: perform DMS actions with feed tags now
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
