/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2025  Minnesota Department of Transportation
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
	 * @param hmk Hardware make.
	 * @param hmd Hardware model.
	 * @param smk Software make.
	 * @param smd Software model.
	 * @param st Supported MULTI tags (bit flags).
	 * @param mp Maximum number of pages.
	 * @param ml Maximum MULTI string length.
	 * @param ba Beacon activation flag.
	 * @param ps Pixel service flag.
	 * @return Matching existing, or new sign detail.
	 */
	static public SignDetailImpl findOrCreate(int dt, boolean p, String t,
		String sa, String l, String bt, String hmk, String hmd,
		String smk, String smd, int st, int mp, int ml, boolean ba,
		boolean ps)
	{
		DMSType dmt = DMSType.fromOrdinal(dt);
		bt = filterDesc(bt);
		hmk = filterDesc(hmk);
		hmd = filterDesc(hmd);
		smk = filterDesc(smk);
		smd = filterDesc(smd);
		SignDetail sd = SignDetailHelper.find(dmt, p, t, sa, l, bt, hmk,
			hmd, smk, smd, st, mp, ml, ba, ps);
		if (sd instanceof SignDetailImpl)
			return (SignDetailImpl) sd;
		else {
			String n = createUniqueName();
			SignDetailImpl sdi = new SignDetailImpl(n, dt, p, t, sa,
				l, bt, hmk, hmd, smk, smd, st, mp, ml, ba, ps);
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
		store.query("SELECT name, dms_type, portable, technology, " +
			"sign_access, legend, beacon_type, " +
			"hardware_make, hardware_model, software_make, " +
			"software_model, supported_tags, max_pages, " +
			"max_multi_len, beacon_activation_flag, " +
			"pixel_service_flag FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
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
		map.put("hardware_make", hardware_make);
		map.put("hardware_model", hardware_model);
		map.put("software_make", software_make);
		map.put("software_model", software_model);
		map.put("supported_tags", supported_tags);
		map.put("max_pages", max_pages);
		map.put("max_multi_len", max_multi_len);
		map.put("beacon_activation_flag", beacon_activation_flag);
		map.put("pixel_service_flag", pixel_service_flag);
		return map;
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
		     row.getString(8),   // hardware_make
		     row.getString(9),   // hardware_model
		     row.getString(10),  // software_make
		     row.getString(11),  // software_model
		     row.getInt(12),     // supported_tags
		     row.getInt(13),     // max_pages
		     row.getInt(14),     // max_multi_len
		     row.getBoolean(15), // beacon_activation_flag
		     row.getBoolean(16)  // pixel_service_flag
		);
	}

	/** Create a sign detail */
	private SignDetailImpl(String n, int dt, boolean p, String t, String sa,
		String l, String bt, String hmk, String hmd, String smk,
		String smd, int st, int mp, int ml, boolean ba, boolean ps)
	{
		super(n);
		dms_type = DMSType.fromOrdinal(dt);
		portable = p;
		technology = t;
		sign_access = sa;
		legend = l;
		beacon_type = bt;
		hardware_make = hmk;
		hardware_model = hmd;
		software_make = smk;
		software_model = smd;
		supported_tags = st;
		max_pages = mp;
		max_multi_len = ml;
		beacon_activation_flag = ba;
		pixel_service_flag = ps;
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

	/** Hardware make (manufacturer) */
	private final String hardware_make;

	/** Get the hardware make */
	@Override
	public String getHardwareMake() {
		return hardware_make;
	}

	/** Hardware model */
	private String hardware_model;

	/** Get the hardware model */
	@Override
	public String getHardwareModel() {
		return hardware_model;
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

	/** Supported MULTI tags (bit flags of MultiTag) */
	private int supported_tags;

	/** Get the supported MULTI tags (bit flags of MultiTag) */
	@Override
	public int getSupportedTags() {
		return supported_tags;
	}

	/** Maximum number of pages */
	private int max_pages;

	/** Get the maximum number of pages */
	@Override
	public int getMaxPages() {
		return max_pages;
	}

	/** Maximum MULTI string length */
	private int max_multi_len;

	/** Get the maximum MULTI string length */
	@Override
	public int getMaxMultiLen() {
		return max_multi_len;
	}

	/** Beacon activation flag */
	private final boolean beacon_activation_flag;

	/** Get beacon activation flag (3.6.6.5 in PRL) */
	@Override
	public boolean getBeaconActivationFlag() {
		return beacon_activation_flag;
	}

	/** Pixel service flag */
	private final boolean pixel_service_flag;

	/** Get pixel service flag (3.6.6.6 in PRL) */
	@Override
	public boolean getPixelServiceFlag() {
		return pixel_service_flag;
	}
}
