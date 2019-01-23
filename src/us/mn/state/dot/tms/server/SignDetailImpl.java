/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.SignDetailHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.SString;

/**
 * Sign detail defines detailed parameters of a sign.
 *
 * @author Douglas Lau
 */
public class SignDetailImpl extends BaseObjectImpl implements SignDetail {

	/** Maximum length for beacon_type, software_make, software_model */
	static private final int MAX_DESC_LEN = 32;

	/** Filter a description string */
	static private String filterDesc(String s) {
		return SString.truncate(s, MAX_DESC_LEN);
	}

	/** Find existing or create a new sign detail.
	 * @param dt DMS type.
	 * @param p Portable flag.
	 * @param t Sign technology.
	 * @param sa Sign access.
	 * @param l Sign legend.
	 * @param bt Beacon type.
	 * @param mf Monochrome foreground color (24-bit).
	 * @param mb Monochrome background color (24-bit).
	 * @param mk Software make.
	 * @param md Software model.
	 * @return Matching existing, or new sign detail.
	 */
	static public SignDetailImpl findOrCreate(int dt, boolean p, String t,
		String sa, String l, String bt, int mf, int mb, String mk,
		String md)
	{
		bt = filterDesc(bt);
		mk = filterDesc(mk);
		mk = filterDesc(md);
		SignDetail sd = SignDetailHelper.find(DMSType.fromOrdinal(dt),
			p, t, sa, l, bt, mf, mb, mk, md);
		if (sd instanceof SignDetailImpl)
			return (SignDetailImpl) sd;
		else {
			String n = createUniqueName();
			SignDetailImpl sdi = new SignDetailImpl(n, dt, p, t, sa,
				l, bt, mf, mb, mk, md);
			return createNotify(sdi);
		}
	}

	/** Notify clients of the new sign detail */
	static private SignDetailImpl createNotify(SignDetailImpl sd) {
		try {
			sd.notifyCreate();
			return sd;
		}
		catch (SonarException e) {
			System.err.println("createNotify: " + e.getMessage());
			return null;
		}
	}

	/** Find or create LCS sign detail */
	static public SignDetailImpl findOrCreateLCS() {
		return findOrCreate(DMSType.OTHER.ordinal(), false, "DLCS",
			"FRONT", "NONE", "NONE", DmsColor.AMBER.rgb(),
			DmsColor.BLACK.rgb(), "", "");
	}

	/** Last allocated sign detail ID */
	static private int last_id = 0;

	/** Create a unique sign detail name */
	static private synchronized String createUniqueName() {
		String n = createNextName();
		while (namespace.lookupObject(SONAR_TYPE, n) != null)
			n = createNextName();
		return n;
	}

	/** Create the next system detail name */
	static private String createNextName() {
		last_id++;
		// Check if the ID has rolled over to negative numbers
		if (last_id < 0)
			last_id = 0;
		return "dtl_" + last_id;
	}

	/** Load all the sign details */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, SignDetailImpl.class);
		store.query("SELECT name, dms_type, portable, technology, " +
			"sign_access, legend, beacon_type, " +
			"monochrome_foreground, monochrome_background, " +
			"software_make, software_model FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new SignDetailImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("dms_type", dms_type.ordinal());
		map.put("portable", portable);
		map.put("technology", technology);
		map.put("sign_access", sign_access);
		map.put("legend", legend);
		map.put("beacon_type", beacon_type);
		map.put("monochrome_foreground", monochrome_foreground);
		map.put("monochrome_background", monochrome_background);
		map.put("software_make", software_make);
		map.put("software_model", software_model);
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

	/** Create a sign detail */
	private SignDetailImpl(ResultSet row) throws SQLException {
		this(row.getString(1),   // name
		     row.getInt(2),      // dms_type
		     row.getBoolean(3),  // portable
		     row.getString(4),   // technology
		     row.getString(5),   // sign_access
		     row.getString(6),   // legend
		     row.getString(7),   // beacon_type
		     row.getInt(8),      // monochrome_foreground
		     row.getInt(9),      // monochrome_background
		     row.getString(10),  // software_make
		     row.getString(11)   // software_model
		);
	}

	/** Create a sign detail */
	private SignDetailImpl(String n, int dt, boolean p, String t, String sa,
		String l, String bt, int mf, int mb, String mk, String md)
	{
		super(n);
		dms_type = DMSType.fromOrdinal(dt);
		portable = p;
		technology = t;
		sign_access = sa;
		legend = l;
		beacon_type = bt;
		monochrome_foreground = mf;
		monochrome_background = mb;
		software_make = mk;
		software_model = md;
	}

	/** DMS type enum value */
	private final DMSType dms_type;

	/** Get DMS type */
	@Override
	public int getDmsType() {
		return dms_type.ordinal();
	}

	/** Portable flag */
	private final boolean portable;

	/** Get portable flag */
	@Override
	public boolean getPortable() {
		return portable;
	}

	/** Sign technology description */
	private final String technology;

	/** Get sign technology description */
	@Override
	public String getTechnology() {
		return technology;
	}

	/** Sign access description */
	private final String sign_access;

	/** Get sign access description */
	@Override
	public String getSignAccess() {
		return sign_access;
	}

	/** Sign legend string */
	private final String legend;

	/** Get sign legend */
	@Override
	public String getLegend() {
		return legend;
	}

	/** Beacon type description */
	private final String beacon_type;

	/** Get beacon type description */
	@Override
	public String getBeaconType() {
		return beacon_type;
	}

	/** Monochrome scheme foreground color (24-bit). */
	private final int monochrome_foreground;

	/** Get monochrome scheme foreground color (24-bit). */
	@Override
	public int getMonochromeForeground() {
		return monochrome_foreground;
	}

	/** Monochrome scheme background color (24-bit). */
	private final int monochrome_background;

	/** Get monochrome scheme background color (24-bit). */
	@Override
	public int getMonochromeBackground() {
		return monochrome_background;
	}

	/** Software make (manufacturer) */
	private final String software_make;

	/** Get the software make */
	@Override
	public String getSoftwareMake() {
		return software_make;
	}

	/** Software model */
	private String software_model;

	/** Get the software model */
	@Override
	public String getSoftwareModel() {
		return software_model;
	}
}
