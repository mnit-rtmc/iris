/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2020  Minnesota Department of Transportation
 * Copyright (C) 2010       AHMCT, University of California
 * Copyright (C) 2012       Iteris Inc.
 * Copyright (C) 2016-2017  SRF Consulting Group
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
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DevicePurpose;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsMsgPriority;
import static us.mn.state.dot.tms.DmsMsgPriority.BLANK;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.SignDetailHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.utils.SString;
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
 * @author John L. Stanley
 */
public class DMSImpl extends DeviceImpl implements DMS, Comparable<DMSImpl> {

	/** Test if a sign message is from a specified source */
	static private boolean isMsgSource(SignMessage sm, SignMsgSource src) {
		return (sm != null) && src.checkBit(sm.getSource());
	}

	/** Get the owner of a sign message */
	static private String getOwner(SignMessage sm, String user) {
		StringBuilder sb = new StringBuilder();
		if (isMsgSource(sm, SignMsgSource.operator) ||
		    isMsgSource(sm, SignMsgSource.blank))
			sb.append(user);
		if (isMsgSource(sm, SignMsgSource.schedule)) {
			if (sb.length() == 0) {
				String o = sm.getOwner();
				sb.append((o != null) ? o : "PLAN");
			} else
				sb.append('*');
		}
		return sb.toString();
	}

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
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"gps, static_graphic, purpose, hidden, beacon, " +
			"preset, sign_config, sign_detail, override_font, " +
			"override_foreground, override_background, msg_sched, " +
			"msg_current, expire_time FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
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
		map.put("purpose", getPurpose());
		map.put("hidden", hidden);
		map.put("beacon", beacon);
		map.put("preset", preset);
		map.put("sign_config", sign_config);
		map.put("sign_detail", sign_detail);
		map.put("override_font", override_font);
		map.put("override_foreground", override_foreground);
		map.put("override_background", override_background);
		map.put("msg_sched", msg_sched);
		map.put("msg_current", msg_current);
		map.put("expire_time", asTimestamp(expire_time));
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
	}

	/** Create a dynamic message sign */
	private DMSImpl(ResultSet row) throws SQLException {
		this(row.getString(1),            // name
		     row.getString(2),            // geo_loc
		     row.getString(3),            // controller
		     row.getInt(4),               // pin
		     row.getString(5),            // notes
		     row.getString(6),            // gps
		     row.getString(7),            // static_graphic
		     row.getInt(8),               // purpose
		     row.getBoolean(9),           // hidden
		     row.getString(10),           // beacon
		     row.getString(11),           // preset
		     row.getString(12),           // sign_config
		     row.getString(13),           // sign_detail
		     row.getString(14),           // override_font
		     (Integer) row.getObject(15), // override_foreground
		     (Integer) row.getObject(16), // override_background
		     row.getString(17),           // msg_sched
		     row.getString(18),           // msg_current
		     row.getTimestamp(19)         // expire_time
		);
	}

	/** Create a dynamic message sign */
	private DMSImpl(String n, String loc, String c, int p, String nt,
		String g, String sg, int dp, boolean h, String b, String cp,
		String sc, String sd, String of, Integer fg, Integer bg,
		String ms, String mc, Date et)
	{
		this(n, lookupGeoLoc(loc), lookupController(c), p, nt,
		     lookupGps(g), lookupGraphic(sg),
		     DevicePurpose.fromOrdinal(dp), h, lookupBeacon(b),
		     lookupPreset(cp), SignConfigHelper.lookup(sc),
		     SignDetailHelper.lookup(sd), FontHelper.lookup(of), fg, bg,
		     SignMessageHelper.lookup(ms), SignMessageHelper.lookup(mc),
		     et);
	}

	/** Create a dynamic message sign */
	private DMSImpl(String n, GeoLocImpl loc, ControllerImpl c,
		int p, String nt, GpsImpl g, Graphic sg, DevicePurpose dp,
		boolean h, Beacon b, CameraPreset cp, SignConfig sc,
		SignDetail sd, Font of, Integer fg, Integer bg, SignMessage ms,
		SignMessage mc, Date et)
	{
		super(n, c, p, nt);
		geo_loc = loc;
		gps = g;
		static_graphic = sg;
		purpose = dp;
		hidden = h;
		beacon = b;
		setPreset(cp);
		sign_config = sc;
		sign_detail = sd;
		override_font = of;
		override_foreground = fg;
		override_background = bg;
		msg_sched = ms;
		msg_current = mc;
		expire_time = stampMillis(et);
		initTransients();
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

	/** Dedicated device purpose */
	private DevicePurpose purpose;

	/** Set device purpose (ordinal of DevicePurpose) */
	@Override
	public void setPurpose(int p) {
		purpose = DevicePurpose.fromOrdinal(p);
	}

	/** Set device purpose (ordinal of DevicePurpose) */
	public void doSetPurpose(int p) throws TMSException {
		if (p != getPurpose()) {
			store.update(this, "purpose", p);
			setPurpose(p);
			updateStyles();
		}
	}

	/** Get device purpose (ordinal of DevicePurpose) */
	@Override
	public int getPurpose() {
		DevicePurpose dp = purpose;
		return (dp != null)
		      ? dp.ordinal()
		      : DevicePurpose.GENERAL.ordinal();
	}

	/** Flag indicating hidden sign */
	private boolean hidden;

	/** Set the hidden flag */
	@Override
	public void setHidden(boolean h) {
		hidden = h;
	}

	/** Set the hidden flag */
	public void doSetHidden(boolean h) throws TMSException {
		if (h != hidden) {
			store.update(this, "hidden", h);
			setHidden(h);
			updateStyles();
		}
	}

	/** Get the hidden flag */
	@Override
	public boolean getHidden() {
		return hidden;
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
		msg_user = null;
		setMsgSchedNotify(null);
		setMsgCurrentNotify(null, "RESET");
		setPixelStatusNotify(null);
	}

	/** Override font */
	private Font override_font;

	/** Set the override font */
	@Override
	public void setOverrideFont(Font f) {
		override_font = f;
	}

	/** Set the override font */
	public void doSetOverrideFont(Font f) throws TMSException {
		if (f != override_font) {
			store.update(this, "override_font", f);
			setOverrideFont(f);
		}
	}

	/** Get the override font */
	@Override
	public Font getOverrideFont() {
		return override_font;
	}

	/** Override foreground color (24-bit rgb) */
	private Integer override_foreground;

	/** Set override foreground color (24-bit rgb) */
	@Override
	public void setOverrideForeground(Integer fg) {
		override_foreground = fg;
	}

	/** Set override foreground color (24-bit rgb) */
	public void doSetOverrideForeground(Integer fg) throws TMSException {
		if (!objectEquals(fg, override_foreground)) {
			store.update(this, "override_foreground", fg);
			setOverrideForeground(fg);
		}
	}

	/** Get override foreground color (24-bit rgb) */
	@Override
	public Integer getOverrideForeground() {
		return override_foreground;
	}

	/** Override background color (24-bit rgb) */
	private Integer override_background;

	/** Set override background color (24-bit rgb) */
	@Override
	public void setOverrideBackground(Integer bg) {
		override_background = bg;
	}

	/** Set override background color (24-bit rgb) */
	public void doSetOverrideBackground(Integer bg) throws TMSException {
		if (!objectEquals(bg, override_background)) {
			store.update(this, "override_background", bg);
			setOverrideBackground(bg);
		}
	}

	/** Get override background color (24-bit rgb) */
	@Override
	public Integer getOverrideBackground() {
		return override_background;
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
	public void setPixelStatusNotify(String[] p) {
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
		return findOrCreateMsg(null, "", false, false, BLANK,
			SignMsgSource.blank.bit(), null, null);
	}

	/** Create a message for the sign.
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pp Prefix page flag.
	 * @param mp Message priority.
	 * @param src Message source.
	 * @param o Message owner.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	public SignMessage createMsg(String m, boolean be, boolean pp,
		DmsMsgPriority mp, int src, String o, Integer d)
	{
		return createMsg(null, m, be, pp, mp, src, o, d);
	}

	/** Create a message for the sign.
	 * @param inc Associated incident (original name).
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pp Prefix page flag.
	 * @param mp Message priority.
	 * @param src Message source.
	 * @param o Message owner.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage createMsg(String inc, String m, boolean be,
		boolean pp, DmsMsgPriority mp, int src, String o, Integer d)
	{
		return findOrCreateMsg(inc, m, be, pp, mp, src, o, d);
	}

	/** Find or create a sign message.
	 * @param inc Associated incident (original name).
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pp Prefix page flag.
	 * @param mp Message priority.
	 * @param src Message source.
	 * @param o Message owner.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage findOrCreateMsg(String inc, String m, boolean be,
		boolean pp, DmsMsgPriority mp, int src, String o, Integer d)
	{
		SignMessage esm = SignMessageHelper.find(sign_config, inc, m,
			be, mp, src, o, d);
		if (esm != null)
			return esm;
		else
			return createMsgNotify(inc, m, be, pp, mp, src, o, d);
	}

	/** Create a new sign message and notify clients.
	 * @param inc Associated incident (original name).
	 * @param m MULTI string for message.
	 * @param be Beacon enabled flag.
	 * @param pp Prefix page flag.
	 * @param mp Message priority.
	 * @param src Message source.
	 * @param o Message owner.
	 * @param d Duration in minutes; null means indefinite.
	 * @return New sign message, or null on error. */
	private SignMessage createMsgNotify(String inc, String m, boolean be,
		boolean pp, DmsMsgPriority mp, int src, String o, Integer d)
	{
		SignConfig sc = sign_config;
		if (null == sc)
			return null;
		SignMessageImpl sm = new SignMessageImpl(sc, inc, m, be, pp, mp,
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

	/** Create a scheduled message.
	 * @param amsg DMS action message.
	 * @return New sign message, or null on error. */
	private SignMessage createMsgSched(DmsActionMsg amsg) {
		assert (amsg != null);
		DmsAction da = amsg.action;
		boolean be = da.getBeaconEnabled();
		QuickMessage qm = da.getQuickMessage();
		boolean pp = (qm != null) ? qm.getPrefixPage() : false;
		DmsMsgPriority mp = DmsMsgPriority.fromOrdinal(
			da.getMsgPriority());
		String o = da.getActionPlan().getName();
		return createMsg(amsg.getMulti(), be, pp, mp, amsg.getSrc(), o,
			getDuration(da));
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
	private transient SignMessage msg_user;

	/** Set the user selected sign message */
	@Override
	public void setMsgUser(SignMessage sm) {
		msg_user = sm;
	}

	/** Set the user selected sign message */
	public void doSetMsgUser(SignMessage sm) throws TMSException {
		SignMessageHelper.validate(sm, this);
		setMsgUser(sm);
		sm = getMsgValidated();
		sendMsg(sm, getOwner(sm, getProcUser()));
	}

	/** Blank the user selected sign message */
	private void blankMsgUser() {
		try {
			doSetMsgUser(createMsgBlank());
		}
		catch (TMSException e) {
			logError("blankMsgUser: " + e.getMessage());
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

	/** Set the scheduled sign message */
	private void setMsgSched(SignMessage sm) {
		try {
			store.update(this, "msg_sched", sm);
			msg_sched = sm;
		}
		catch (TMSException e) {
			logError("msg_sched: " + e.getMessage());
		}
	}

	/** Set the scheduled sign message.
	 * @param sm New scheduled sign message.
	 * @return true If scheduled message changed. */
	private boolean setMsgSchedNotify(SignMessage sm) {
		if (!objectEquals(msg_sched, sm)) {
			setMsgSched(sm);
			notifyAttribute("msgSched");
			return true;
		} else
			return false;
	}

	/** Update scheduled message.
	 *
	 * This updates the message's expire time, so it should be called on each
	 * polling period.  Also, if a scheduled message has just expired, it
	 * will be replaced by the user message. */
	private void updateSchedMsg() {
		try {
			SignMessage usm = getMsgValidated();
			if (isMsgSource(usm, SignMsgSource.schedule) ||
			   isMsgScheduled())
			{
				if (usm != null)
					sendMsg(usm);
				else if (msg_queried)
					blankMsgUser();
			}
		}
		catch (TMSException e) {
			logError("updateSchedMsg: " + e.getMessage());
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
	 * @param sm Sign message.
	 * @param owner Message owner. */
	public void setMsgCurrentNotify(SignMessage sm, String owner) {
		if (isMsgSource(sm, SignMsgSource.tolling))
			logPriceMessages(EventType.PRICE_VERIFIED);
		if (sm != msg_current) {
			logMsg(sm, owner);
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
	 * @param sm Sign message.
	 * @param owner Message owner. */
	private void logMsg(SignMessage sm, String owner) {
		EventType et = EventType.DMS_DEPLOYED;
		String text = (sm != null) ? sm.getMulti() : null;
		if (SignMessageHelper.isBlank(sm)) {
			et = EventType.DMS_CLEARED;
			text = null;
		}
		logEvent(new SignEvent(et, name, text, owner));
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
	 * @return Validated sign message, or null. */
	private SignMessage getMsgValidated() throws TMSException {
		SignMessage sm = getMsgUserSched();
		if (sm != null)
			SignMessageHelper.validate(sm, this);
		return sm;
	}

	/** Get user and/or scheduled sign message.
	 * @return The appropriate sign message, or null. */
	private SignMessage getMsgUserSched() {
		SignMessage user = msg_user;	// Avoid race
		if (!msg_queried)
			return user;
		SignMessage sched = msg_sched;	// Avoid race
		if (null == user)
			return sched;
		boolean is_blank = SignMessageHelper.isBlank(user);
		if (isPrefixPage(sched) && !is_blank) {
			SignMessage sm = createMsgUserSched(user, sched);
			if (sm != null && isMsgValid(sm))
				return sm;
		}
		return (isMsgValid(sched) && checkPriority(sched, user))
		      ? sched
		      : user;
	}

	/** Is scheduled message using prefix page? */
	private boolean isPrefixPage(SignMessage sm) {
		return (sm != null) && sm.getPrefixPage();
	}

	/** Create a user/scheduled composite sign message. */
	private SignMessage createMsgUserSched(SignMessage user,
		SignMessage sched)
	{
		MultiString multi = new MultiString(user.getMulti());
		String ms = multi.addPagePrefix(sched.getMulti());
		boolean be = user.getBeaconEnabled();
		DmsMsgPriority mp = DmsMsgPriority.fromOrdinal(
			user.getMsgPriority());
		String inc = user.getIncident();
		int src = user.getSource() | sched.getSource();
		String o = user.getOwner();
		Integer dur = user.getDuration();
		return createMsg(inc, ms, be, false, mp, src, o, dur);
	}

	/** Check if a sign message is valid */
	private boolean isMsgValid(SignMessage sm) {
		if (null == sm)
			return false;
		try {
			SignMessageHelper.validate(sm, this);
			return true;
		}
		catch (TMSException e) {
			logError("msg invalid: " + e.getMessage());
			return false;
		}
	}

	/** Compare sign messages for higher priority */
	private boolean checkPriority(SignMessage sm1, SignMessage sm2) {
		return sm1.getMsgPriority() > sm2.getMsgPriority();
	}

	/** Send message to DMS.
	 * @param sm Sign message. */
	private void sendMsg(SignMessage sm) throws TMSException {
		sendMsg(sm, getOwner(sm, sm.getOwner()));
	}

	/** Send message to DMS.
	 * @param sm Sign message.
	 * @param owner Message owner. */
	private void sendMsg(SignMessage sm, String owner) throws TMSException {
		DMSPoller p = getDMSPoller();
		if (null == p) {
			throw new ChangeVetoException(name +
				": NO ACTIVE POLLER");
		}
		if (sm != null)
			sendMsg(p, sm, owner);
	}

	/** Set the next sign message.
	 * @param p DMS poller.
	 * @param sm Sign message.
	 * @param owner Message owner. */
	private void sendMsg(DMSPoller p, SignMessage sm, String owner) {
		if (isMsgSource(sm, SignMsgSource.tolling))
		    logPriceMessages(EventType.PRICE_DEPLOYED);
		p.sendMessage(this, sm, owner);
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

	/** LDC pot base (Ledstar-specific value) */
	private transient Integer ldcPotBase;

	/** Set the LDC pot base */
	public void setLdcPotBaseNotify(Integer base) {
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
	public void setPixelCurrentLowNotify(Integer low) {
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
	public void setPixelCurrentHighNotify(Integer high) {
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
		    && isMsgBlank()
		    && purpose == DevicePurpose.GENERAL;
	}

	/** Test if current message is blank */
	public boolean isMsgBlank() {
		return SignMessageHelper.isBlank(msg_current);
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

	/** Test if the current message has beacon enabled */
	private boolean isMsgBeacon() {
		SignMessage sm = getMsgCurrent();
		return (sm != null) && sm.getBeaconEnabled();
	}

	/** Test if a DMS is active, not failed and deployed */
	public boolean isMsgDeployed() {
		return isOnline() && !isMsgBlank();
	}

	/** Test if a DMS has been deployed by a user */
	public boolean isUserDeployed() {
		return isMsgDeployed() && isMsgOperator();
	}

	/** Test if a DMS has been deployed by schedule */
	public boolean isScheduleDeployed() {
		return isMsgDeployed() && isMsgScheduled();
	}

	/** Test if a DMS has been deployed by an external system */
	private boolean isExternalDeployed() {
		return isMsgDeployed() && isMsgExternal();
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
		if (hidden)
			s |= ItemStyle.HIDDEN.bit();
		else {
			if (isAvailable())
				s |= ItemStyle.AVAILABLE.bit();
			if (isUserDeployed())
				s |= ItemStyle.DEPLOYED.bit();
		}
		if (isScheduleDeployed())
			s |= ItemStyle.SCHEDULED.bit();
		if (isExternalDeployed())
			s |= ItemStyle.EXTERNAL.bit();
		if (isOnline() && needsMaintenance())
			s |= ItemStyle.MAINTENANCE.bit();
		if (isActive() && isFailed())
			s |= ItemStyle.FAILED.bit();
		if (purpose != DevicePurpose.GENERAL)
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
		DMSPoller p = getDMSPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll() {
		if (isLongPeriodModem())
			sendDeviceRequest(DeviceRequest.QUERY_STATUS);
		sendDeviceRequest(DeviceRequest.QUERY_MESSAGE);
		checkMsgExpiration();
		updateSchedMsg();
		LCSArrayImpl la = lookupLCSArray();
		if (la != null)
			la.periodicPoll();
	}

	/** Check if current sign message has expired */
	private void checkMsgExpiration() {
		Long et = expire_time;
		if (et != null) {
			long now = TimeSteward.currentTimeMillis();
			if (now >= et)
				blankMsgUser();
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

	/** Flag to indicate beacon object supported */
	private transient boolean supports_beacon_object = true;

	/** Set flag to indicate beacon object supported */
	public void setSupportsBeaconObject(boolean b) {
		if (!b) {
			System.err.println("DMSImpl." +
				"setSupportsBeaconObject(false): " + name);
		}
		supports_beacon_object = b;
	}

	/** Does sign have beacon object support? */
	public boolean getSupportsBeaconObject() {
		return supports_beacon_object;
	}

	/** Flag to indicate pixel service object supported */
	private transient boolean supports_pixel_service_object = true;

	/** Set flag to indicate pixel service object supported */
	public void setSupportsPixelServiceObject(boolean p) {
		if (!p) {
			System.err.println("DMSImpl." +
				"setSupportsPixelServiceObject(false): "+ name);
		}
		supports_pixel_service_object = p;
	}

	/** Does sign have pixel service object support? */
	public boolean getSupportsPixelServiceObject() {
		return supports_pixel_service_object;
	}

	/** Was message queried since startup? */
	private transient boolean msg_queried = false;

	/** Message was queried since startup */
	public void msgQueried() {
		msg_queried = true;
	}
}
