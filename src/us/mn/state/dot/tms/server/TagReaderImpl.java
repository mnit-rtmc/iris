/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2018  Minnesota Department of Transportation
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.TagReaderSyncMode;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.TagReaderPoller;
import us.mn.state.dot.tms.server.event.TagReadEvent;

/**
 * A tag reader is a sensor for vehicle transponders, which are used for
 * toll lanes.
 *
 * @author Douglas Lau
 */
public class TagReaderImpl extends DeviceImpl implements TagReader {

	/** Tag Reader / DMS table mapping */
	static private TableMapping mapping;

	/** Load all the tag readers */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, TagReaderImpl.class);
		mapping = new TableMapping(store, "iris", SONAR_TYPE,
			DMS.SONAR_TYPE);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"toll_zone, downlink_freq_khz, uplink_freq_khz, " +
			"sego_atten_downlink_db, sego_atten_uplink_db, " +
			"sego_data_detect_db, sego_seen_count, " +
			"sego_unique_count, iag_atten_downlink_db, " +
			"iag_atten_uplink_db, iag_data_detect_db, " +
			"iag_seen_count, iag_unique_count, line_loss_db, " +
			"sync_mode, slave_select_count FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new TagReaderImpl(row));
			}
		});
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
		map.put("toll_zone", toll_zone);
		map.put("downlink_freq_khz", downlink_freq_khz);
		map.put("uplink_freq_khz", uplink_freq_khz);
		map.put("sego_atten_downlink_db", sego_atten_downlink_db);
		map.put("sego_atten_uplink_db", sego_atten_uplink_db);
		map.put("sego_data_detect_db", sego_data_detect_db);
		map.put("sego_seen_count", sego_seen_count);
		map.put("sego_unique_count", sego_unique_count);
		map.put("iag_atten_downlink_db", iag_atten_downlink_db);
		map.put("iag_atten_uplink_db", iag_atten_uplink_db);
		map.put("iag_data_detect_db", iag_data_detect_db);
		map.put("iag_seen_count", iag_seen_count);
		map.put("iag_unique_count", iag_unique_count);
		map.put("line_loss_db", line_loss_db);
		TagReaderSyncMode sm = sync_mode;
		map.put("sync_mode", (sm != null) ? sm.ordinal() : null);
		map.put("slave_select_count", slave_select_count);
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

	/** Create a tag reader */
	private TagReaderImpl(ResultSet row) throws SQLException, TMSException {
		this(row.getString(1),           // name
		     row.getString(2),           // geo_loc
		     row.getString(3),           // controller
		     row.getInt(4),              // pin
		     row.getString(5),           // notes
		     row.getString(6),           // toll_zone
		     (Integer) row.getObject(7), // downlink_freq_khz
		     (Integer) row.getObject(8), // uplink_freq_khz
		     (Integer) row.getObject(9), // sego_atten_downlink_db
		     (Integer) row.getObject(10),// sego_atten_uplink_db
		     (Integer) row.getObject(11),// sego_data_detect_db
		     (Integer) row.getObject(12),// sego_seen_count
		     (Integer) row.getObject(13),// sego_unique_count
		     (Integer) row.getObject(14),// iag_atten_downlink_db
		     (Integer) row.getObject(15),// iag_atten_uplink_db
		     (Integer) row.getObject(16),// iag_data_detect_db
		     (Integer) row.getObject(17),// iag_seen_count
		     (Integer) row.getObject(18),// iag_unique_count
		     (Integer) row.getObject(19),// line_loss_db
		     (Integer) row.getObject(20),// sync_mode
		     (Integer) row.getObject(21) // slave_select_count
		);
	}

	/** Create a tag reader */
	private TagReaderImpl(String n, String l, String c, int p, String nt,
		String tz, Integer df, Integer uf, Integer sad, Integer sau,
		Integer sdd, Integer ssc, Integer suc, Integer iad, Integer iau,
		Integer idd, Integer isc, Integer iuc, Integer ll, Integer sm,
		Integer sc) throws TMSException
	{
		this(n, lookupGeoLoc(l), lookupController(c), p, nt,
		     lookupTollZone(tz), df, uf, sad, sau, sdd, ssc, suc, iad,
		     iau, idd, isc, iuc, ll, sm, sc);
	}

	/** Create a tag reader */
	private TagReaderImpl(String n, GeoLocImpl l, ControllerImpl c,
		int p, String nt, TollZone tz, Integer df, Integer uf,
		Integer sad, Integer sau, Integer sdd, Integer ssc, Integer suc,
		Integer iad, Integer iau, Integer idd, Integer isc, Integer iuc,
		Integer ll, Integer sm, Integer sc) throws TMSException
	{
		super(n, c, p, nt);
		geo_loc = l;
		toll_zone = tz;
		downlink_freq_khz = df;
		uplink_freq_khz = uf;
		sego_atten_downlink_db = sad;
		sego_atten_uplink_db = sau;
		sego_data_detect_db = sdd;
		sego_seen_count = ssc;
		sego_unique_count = suc;
		iag_atten_downlink_db = iad;
		iag_atten_uplink_db = iau;
		iag_data_detect_db = idd;
		iag_seen_count = isc;
		iag_unique_count = iuc;
		line_loss_db = ll;
		sync_mode = TagReaderSyncMode.fromOrdinal(sm);
		slave_select_count = sc;
		dmss = lookupDMSMapping();
		initTransients();
	}

	/** Create a new tag reader with a string name */
	public TagReaderImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Lookup mapping of DMS */
	private DMSImpl[] lookupDMSMapping() throws TMSException {
		TreeSet<DMSImpl> d_set = new TreeSet<DMSImpl>();
		for (String o: mapping.lookup(this)) {
			DMS dms = DMSHelper.lookup(o);
			if (dms instanceof DMSImpl)
				d_set.add((DMSImpl) dms);
		}
		return d_set.toArray(new DMSImpl[0]);
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
	}

	/** Device location */
	private GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Toll zone */
	private TollZone toll_zone;

	/** Set the toll zone */
	@Override
	public void setTollZone(TollZone tz) {
		toll_zone = tz;
	}

	/** Set the toll zone */
	public void doSetTollZone(TollZone tz) throws TMSException {
		if (tz != toll_zone) {
			store.update(this, "toll_zone", tz);
			setTollZone(tz);
		}
	}

	/** Get the toll zone */
	@Override
	public TollZone getTollZone() {
		return toll_zone;
	}

	/** Downlink frequency (khz) */
	private Integer downlink_freq_khz;

	/** Set the downlink frequency */
	private void setDownlinkFreqKhz(Integer df) {
		downlink_freq_khz = df;
	}

	/** Set the downlink frequency */
	public void setDownlinkFreqKhzNotify(Integer df) {
		if (!objectEquals(df, downlink_freq_khz)) {
			try {
				store.update(this, "downlink_freq_khz", df);
				setDownlinkFreqKhz(df);
			}
			catch (TMSException e) {
				logError("downlink_freq_khz: " +e.getMessage());
			}
		}
	}

	/** Get the downlink frequency (khz) */
	public Integer getDownlinkFreqKhz() {
		return downlink_freq_khz;
	}

	/** Uplink frequency (khz) */
	private Integer uplink_freq_khz;

	/** Set the uplink frequency */
	private void setUplinkFreqKhz(Integer uf) {
		uplink_freq_khz = uf;
	}

	/** Set the uplink frequency */
	public void setUplinkFreqKhzNotify(Integer uf) {
		if (!objectEquals(uf, uplink_freq_khz)) {
			try {
				store.update(this, "uplink_freq_khz", uf);
				setUplinkFreqKhz(uf);
			}
			catch (TMSException e) {
				logError("uplink_freq_khz: " + e.getMessage());
			}
		}
	}

	/** Get the uplink frequency (khz) */
	public Integer getUplinkFreqKhz() {
		return uplink_freq_khz;
	}

	/** SeGo downlink attenuation (db) */
	private Integer sego_atten_downlink_db;

	/** Set the SeGo downlink attenuation */
	private void setSeGoAttenDownlinkDb(Integer sad) {
		sego_atten_downlink_db = sad;
	}

	/** Set the SeGo downlink attenuation */
	public void setSeGoAttenDownlinkDbNotify(Integer sad) {
		if (!objectEquals(sad, sego_atten_downlink_db)) {
			try {
				store.update(this,"sego_atten_downlink_db",sad);
				setSeGoAttenDownlinkDb(sad);
			}
			catch (TMSException e) {
				logError("sego_atten_downlink_db: " +
					e.getMessage());
			}
		}
	}

	/** Get the SeGo downlink attenuation */
	public Integer getSeGoAttenDownlinkDb() {
		return sego_atten_downlink_db;
	}

	/** SeGo uplink attenuation (db) */
	private Integer sego_atten_uplink_db;

	/** Set the SeGo uplink attenuation */
	private void setSeGoAttenUplinkDb(Integer sau) {
		sego_atten_uplink_db = sau;
	}

	/** Set the SeGo uplink attenuation */
	public void setSeGoAttenUplinkDbNotify(Integer sau) {
		if (!objectEquals(sau, sego_atten_uplink_db)) {
			try {
				store.update(this, "sego_atten_uplink_db", sau);
				setSeGoAttenUplinkDb(sau);
			}
			catch (TMSException e) {
				logError("sego_atten_uplink_db: " +
					e.getMessage());
			}
		}
	}

	/** Get the SeGo uplink attenuation */
	public Integer getSeGoAttenUplinkDb() {
		return sego_atten_uplink_db;
	}

	/** SeGo data detect (db) */
	private Integer sego_data_detect_db;

	/** Set the SeGo data detect */
	private void setSeGoDataDetectDb(Integer sdd) {
		sego_data_detect_db = sdd;
	}

	/** Set the SeGo data detect */
	public void setSeGoDataDetectDbNotify(Integer sdd) {
		if (!objectEquals(sdd, sego_data_detect_db)) {
			try {
				store.update(this, "sego_data_detect_db", sdd);
				setSeGoDataDetectDb(sdd);
			}
			catch (TMSException e) {
				logError("sego_data_detect_db: " +
					e.getMessage());
			}
		}
	}

	/** Get the SeGo data detect */
	public Integer getSeGoDataDetectDb() {
		return sego_data_detect_db;
	}

	/** SeGo seen count */
	private Integer sego_seen_count;

	/** Set the SeGo seen count */
	private void setSeGoSeenCount(Integer ssc) {
		sego_seen_count = ssc;
	}

	/** Set the SeGo seen count */
	public void setSeGoSeenCountNotify(Integer ssc) {
		if (!objectEquals(ssc, sego_seen_count)) {
			try {
				store.update(this, "sego_seen_count", ssc);
				setSeGoSeenCount(ssc);
			}
			catch (TMSException e) {
				logError("sego_seen_count: " + e.getMessage());
			}
		}
	}

	/** Get the SeGo seen count */
	public Integer getSeGoSeenCount() {
		return sego_seen_count;
	}

	/** SeGo unique count */
	private Integer sego_unique_count;

	/** Set the SeGo unique count */
	private void setSeGoUniqueCount(Integer suc) {
		sego_unique_count = suc;
	}

	/** Set the SeGo unique count */
	public void setSeGoUniqueCountNotify(Integer suc) {
		if (!objectEquals(suc, sego_unique_count)) {
			try {
				store.update(this, "sego_unique_count", suc);
				setSeGoUniqueCount(suc);
			}
			catch (TMSException e) {
				logError("sego_unique_count: " +e.getMessage());
			}
		}
	}

	/** Get the SeGo unique count */
	public Integer getSeGoUniqueCount() {
		return sego_unique_count;
	}

	/** IAG downlink attenuation (db) */
	private Integer iag_atten_downlink_db;

	/** Set the IAG downlink attenuation */
	private void setIAGAttenDownlinkDb(Integer iad) {
		iag_atten_downlink_db = iad;
	}

	/** Set the IAG downlink attenuation */
	public void setIAGAttenDownlinkDbNotify(Integer iad) {
		if (!objectEquals(iad, iag_atten_downlink_db)) {
			try {
				store.update(this, "iag_atten_downlink_db",iad);
				setIAGAttenDownlinkDb(iad);
			}
			catch (TMSException e) {
				logError("iag_atten_downlink_db: " +
					e.getMessage());
			}
		}
	}

	/** Get the IAG downlink attenuation */
	public Integer getIAGAttenDownlinkDb() {
		return iag_atten_downlink_db;
	}

	/** IAG uplink attenuation (db) */
	private Integer iag_atten_uplink_db;

	/** Set the IAG uplink attenuation */
	private void setIAGAttenUplinkDb(Integer iau) {
		iag_atten_uplink_db = iau;
	}

	/** Set the IAG uplink attenuation */
	public void setIAGAttenUplinkDbNotify(Integer iau) {
		if (!objectEquals(iau, iag_atten_uplink_db)) {
			try {
				store.update(this, "iag_atten_uplink_db", iau);
				setIAGAttenUplinkDb(iau);
			}
			catch (TMSException e) {
				logError("iag_atten_uplink_db: " +
					e.getMessage());
			}
		}
	}

	/** Get the IAG uplink attenuation */
	public Integer getIAGAttenUplinkDb() {
		return iag_atten_uplink_db;
	}

	/** IAG data detect (db) */
	private Integer iag_data_detect_db;

	/** Set the IAG data detect */
	private void setIAGDataDetectDb(Integer idd) {
		iag_data_detect_db = idd;
	}

	/** Set the IAG data detect */
	public void setIAGDataDetectDbNotify(Integer idd) {
		if (!objectEquals(idd, iag_data_detect_db)) {
			try {
				store.update(this, "iag_data_detect_db", idd);
				setIAGDataDetectDb(idd);
			}
			catch (TMSException e) {
				logError("iag_data_detect_db: " +
					e.getMessage());
			}
		}
	}

	/** Get the IAG data detect */
	public Integer getIAGDataDetectDb() {
		return iag_data_detect_db;
	}

	/** IAG seen count */
	private Integer iag_seen_count;

	/** Set the IAG seen count */
	private void setIAGSeenCount(Integer isc) {
		iag_seen_count = isc;
	}

	/** Set the IAG seen count */
	public void setIAGSeenCountNotify(Integer isc) {
		if (!objectEquals(isc, iag_seen_count)) {
			try {
				store.update(this, "iag_seen_count", isc);
				setIAGSeenCount(isc);
			}
			catch (TMSException e) {
				logError("iag_seen_count: " + e.getMessage());
			}
		}
	}

	/** Get the IAG seen count */
	public Integer getIAGSeenCount() {
		return iag_seen_count;
	}

	/** IAG unique count */
	private Integer iag_unique_count;

	/** Set the IAG unique count */
	private void setIAGUniqueCount(Integer iuc) {
		iag_unique_count = iuc;
	}

	/** Set the IAG unique count */
	public void setIAGUniqueCountNotify(Integer iuc) {
		if (!objectEquals(iuc, iag_unique_count)) {
			try {
				store.update(this, "iag_unique_count", iuc);
				setIAGUniqueCount(iuc);
			}
			catch (TMSException e) {
				logError("iag_unique_count: " + e.getMessage());
			}
		}
	}

	/** Get the IAG unique count */
	public Integer getIAGUniqueCount() {
		return iag_unique_count;
	}

	/** Line loss (db) */
	private Integer line_loss_db;

	/** Set the line loss */
	private void setLineLossDb(Integer ll) {
		line_loss_db = ll;
	}

	/** Set the line loss */
	public void setLineLossDbNotify(Integer ll) {
		if (!objectEquals(ll, line_loss_db)) {
			try {
				store.update(this, "line_loss_db", ll);
				setLineLossDb(ll);
			}
			catch (TMSException e) {
				logError("line_loss_db: " + e.getMessage());
			}
		}
	}

	/** Get the line loss */
	public Integer getLineLossDb() {
		return line_loss_db;
	}

	/** Synchrnization mode */
	private TagReaderSyncMode sync_mode;

	/** Set the synchronization mode */
	public void setSyncModeNotify(TagReaderSyncMode sm) {
		if (!objectEquals(sm, sync_mode)) {
			int m = (sm != null) ? sm.ordinal() : null;
			try {
				store.update(this, "sync_mode", m);
				sync_mode = sm;
			}
			catch (TMSException e) {
				logError("sync_mode: " + e.getMessage());
			}
		}
	}

	/** Get the synchronization mode */
	public TagReaderSyncMode getSyncMode() {
		return sync_mode;
	}

	/** Slave select count */
	private Integer slave_select_count;

	/** Set the slave select count */
	private void setSlaveSelectCount(Integer sc) {
		slave_select_count = sc;
	}

	/** Set the slave select count */
	public void setSlaveSelectCountNotify(Integer sc) {
		if (!objectEquals(sc, slave_select_count)) {
			try {
				store.update(this, "slave_select_count", sc);
				setSlaveSelectCount(sc);
			}
			catch (TMSException e) {
				logError("slave_select_count: "+e.getMessage());
			}
		}
	}

	/** Get the slave select count */
	public Integer getSlaveSelectCount() {
		return slave_select_count;
	}

	/** DMSs for the tag reader */
	private DMSImpl[] dmss = new DMSImpl[0];

	/** Set the DMSs assigned to the tag reader */
	@Override
	public void setSigns(DMS[] ds) {
		dmss = makeDMSArray(ds);
	}

	/** Make an ordered array of DMSs */
	private DMSImpl[] makeDMSArray(DMS[] ds) {
		TreeSet<DMSImpl> d_set = new TreeSet<DMSImpl>();
		for (DMS d: ds) {
			if (d instanceof DMSImpl)
				d_set.add((DMSImpl) d);
		}
		return d_set.toArray(new DMSImpl[0]);
	}

	/** Set the DMSs assigned to the tag reader */
	public void doSetSigns(DMS[] ds) throws TMSException {
		TreeSet<Storable> d_set = new TreeSet<Storable>();
		for (DMS d: ds) {
			if (d instanceof DMSImpl)
				d_set.add((DMSImpl) d);
			else
				throw new ChangeVetoException("Invalid DMS");
		}
		mapping.update(this, d_set);
		setSigns(ds);
	}

	/** Get the DMSs assigned to the tag reader */
	@Override
	public DMS[] getSigns() {
		return dmss;
	}

	/** Test if device is available */
	@Override
	protected boolean isAvailable() {
		// Don't check maintenance status because it
		// always contains Error_log_entries
		return isOnline();
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		TagReaderPoller p = getTagReaderPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Get the tag reader poller */
	private TagReaderPoller getTagReaderPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof TagReaderPoller)
		      ? (TagReaderPoller) dp
		      : null;
	}

	/** Log a tag (transponder) read event.
	 * @param stamp Timestamp of read event.
	 * @param tt Tag Type.
	 * @param agency Agency ID.
	 * @param tid Tag (transponder) ID.
	 * @param hov HOV switch flag. */
	public void logRead(long stamp, TagType tt, Integer agency, int tid,
		boolean hov)
	{
		TagReadEvent ev = new TagReadEvent(EventType.TAG_READ,
			new Date(stamp), tt.ordinal(), agency, tid, name, hov);
		logEvent(ev);
	}
}
