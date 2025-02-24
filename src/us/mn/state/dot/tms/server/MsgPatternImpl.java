/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * A message pattern is a partially or fully composed message for a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgPatternImpl extends BaseObjectImpl implements MsgPattern,
	Comparable<MsgPatternImpl>
{
	/** Create a unique MsgPattern record name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 20,
			(n)->lookupMsgPattern(n));
		return unc.createUniqueName();
	}

	/** Load all the message patterns */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, multi, flash_beacon, " +
			"pixel_service, compose_hashtag FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new MsgPatternImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("multi", multi);
		map.put("flash_beacon", flash_beacon);
		map.put("pixel_service", pixel_service);
		map.put("compose_hashtag", compose_hashtag);
		return map;
	}

	/** Compare to another message pattern */
	@Override
	public int compareTo(MsgPatternImpl o) {
		return name.compareTo(o.name);
	}

	/** Create a new message (by SONAR clients) */
	public MsgPatternImpl(String n) {
		super(n);
	}

	/** Create a message pattern */
	private MsgPatternImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // multi
		     row.getBoolean(3), // flash_beacon
		     row.getBoolean(4), // pixel_service
		     row.getString(5)   // compose_hashtag
		);
	}

	/** Create a message pattern */
	private MsgPatternImpl(String n, String m, boolean fb, boolean ps,
		String cht)
	{
		super(n);
		multi = m;
		flash_beacon = fb;
		pixel_service = ps;
		compose_hashtag = cht;
	}

	/** Get notes (including hashtags) */
	@Override
	public String getNotes() {
		return getComposeHashtag();
	}

	/** Message MULTI string, contains message text for all pages */
	private String multi = "";

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	@Override
	public String getMulti() {
		return multi;
	}

	/** Set the message MULTI string.
	 * @param m Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	@Override
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string */
	public void doSetMulti(String m) throws TMSException {
		if (!new MultiString(m).isValid())
			throw new ChangeVetoException("Invalid MULTI: " + m);
		if (!m.equals(multi)) {
			store.update(this, "multi", m);
			setMulti(m);
		}
	}

	/** Flash beacon flag */
	private boolean flash_beacon;

	/** Get flash beacon flag */
	@Override
	public boolean getFlashBeacon() {
		return flash_beacon;
	}

	/** Set flash beacon flag */
	@Override
	public void setFlashBeacon(boolean fb) {
		flash_beacon = fb;
	}

	/** Set flash beacon flag */
	public void doSetFlashBeacon(boolean fb) throws TMSException {
		if (fb != flash_beacon) {
			store.update(this, "flash_beacon", fb);
			setFlashBeacon(fb);
		}
	}

	/** Pixel service flag */
	private boolean pixel_service;

	/** Get pixel service flag */
	@Override
	public boolean getPixelService() {
		return pixel_service;
	}

	/** Set pixel service flag */
	@Override
	public void setPixelService(boolean ps) {
		pixel_service = ps;
	}

	/** Set pixel service flag */
	public void doSetPixelService(boolean ps) throws TMSException {
		if (ps != pixel_service) {
			store.update(this, "pixel_service", ps);
			setPixelService(ps);
		}
	}

	/** DMS hashtag for composing */
	private String compose_hashtag;

	/** Get the hashtag for composing with the pattern.
	 * @return hashtag; null for no composing. */
	@Override
	public String getComposeHashtag() {
		return compose_hashtag;
	}

	/** Set the hashtag for composing with the pattern.
	 * @param cht hashtag; null for no composing. */
	@Override
	public void setComposeHashtag(String cht) {
		compose_hashtag = cht;
	}

	/** Set the hashtag for composing with the pattern */
	public void doSetComposeHashtag(String cht) throws TMSException {
		String ht = Hashtags.normalize(cht);
		if (!objectEquals(ht, cht))
			throw new ChangeVetoException("Bad hashtag");
		if (!objectEquals(cht, compose_hashtag)) {
			store.update(this, "compose_hashtag", cht);
			setComposeHashtag(cht);
		}
	}
}
