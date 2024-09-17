/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MonitorStyle;
import us.mn.state.dot.tms.TMSException;

/**
 * Video monitor styles.
 *
 * @author Douglas Lau
 */
public class MonitorStyleImpl extends BaseObjectImpl implements MonitorStyle {

	/** Load all the monitor styles */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, force_aspect, accent, font_sz, " +
			"title_bar, auto_expand, hgap, vgap FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new MonitorStyleImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("force_aspect", force_aspect);
		map.put("accent", accent);
		map.put("font_sz", font_sz);
		map.put("title_bar", title_bar);
		map.put("auto_expand", auto_expand);
		map.put("hgap", hgap);
		map.put("vgap", vgap);
		return map;
	}

	/** Create a new encoder type */
	public MonitorStyleImpl(String n) {
		super(n);
		force_aspect = false;
		accent = DEFAULT_ACCENT;
		font_sz = DEFAULT_FONT_SZ;
		title_bar = true;
	}

	/** Create a monitor style */
	private MonitorStyleImpl(ResultSet row) throws SQLException {
		this(row.getString(1),      // name
		     row.getBoolean(2),     // force_aspect
		     row.getString(3),      // accent
		     row.getInt(4),         // font_sz
		     row.getBoolean(5),     // title_bar
		     row.getBoolean(6),     // auto_expand
		     row.getInt(7),         // hgap
		     row.getInt(8)          // vgap
		);
	}

	/** Create a new monitor style */
	private MonitorStyleImpl(String n, boolean fa, String a, int fs,
		boolean tb, boolean ae, int hg, int vg)
	{
		this(n);
		force_aspect = fa;
		accent = a;
		font_sz = fs;
		title_bar = tb;
		auto_expand = ae;
		hgap = hg;
		vgap = vg;
	}

	/** Force-aspect ratio flag */
	private boolean force_aspect;

	/** Set force-aspect ratio flag */
	@Override
	public void setForceAspect(boolean fa) {
		force_aspect = fa;
	}

	/** Set force-aspect ratio flag */
	public void doSetForceAspect(boolean fa) throws TMSException {
		if (fa == force_aspect)
			return;
		store.update(this, "force_aspect", fa);
		setForceAspect(fa);
	}

	/** Get force-aspect ratio flag */
	@Override
	public boolean getForceAspect() {
		return force_aspect;
	}

	/** Accent color */
	private String accent = "";

	/** Set the accent color (hex: RRGGBB) */
	@Override
	public void setAccent(String a) {
		accent = a;
	}

	/** Set the accent color (hex: RRGGBB) */
	public void doSetAccent(String a) throws TMSException {
		if (!a.equals(accent)) {
			store.update(this, "accent", a);
			setAccent(a);
		}
	}

	/** Get the accent color (hex: RRGGBB) */
	@Override
	public String getAccent() {
		return accent;
	}

	/** Font size (pt) */
	private int font_sz;

	/** Set the font size (pt) */
	@Override
	public void setFontSz(int fs) {
		font_sz = fs;
	}

	/** Set the font size (pt) */
	public void doSetFontSz(int fs) throws TMSException {
		if (fs != font_sz) {
			store.update(this, "font_sz", fs);
			setFontSz(fs);
		}
	}

	/** Get the font size (pt) */
	@Override
	public int getFontSz() {
		return font_sz;
	}

	/** Title-bar flag */
	private boolean title_bar;

	/** Set title-bar flag */
	@Override
	public void setTitleBar(boolean tb) {
		title_bar = tb;
	}

	/** Set title-bar flag */
	public void doSetTitleBar(boolean tb) throws TMSException {
		if (tb != title_bar) {
			store.update(this, "title_bar", tb);
			setTitleBar(tb);
		}
	}

	/** Get title-bar flag */
	@Override
	public boolean getTitleBar() {
		return title_bar;
	}

	/** Auto-expand flag */
	private boolean auto_expand;

	/** Set auto-expand flag */
	@Override
	public void setAutoExpand(boolean ae) {
		auto_expand = ae;
	}

	/** Set auto-expand flag */
	public void doSetAutoExpand(boolean ae) throws TMSException {
		if (ae != auto_expand) {
			store.update(this, "auto_expand", ae);
			setAutoExpand(ae);
		}
	}

	/** Get auto-expand flag */
	@Override
	public boolean getAutoExpand() {
		return auto_expand;
	}

	/** Horizontal gap */
	private int hgap;

	/** Set the horizontal gap */
	@Override
	public void setHGap(int g) {
		hgap = g;
	}

	/** Set the horizontal gap */
	public void doSetHGap(int g) throws TMSException {
		if (g != hgap) {
			store.update(this, "hgap", g);
			setHGap(g);
		}
	}

	/** Get the horizontal gap */
	@Override
	public int getHGap() {
		return hgap;
	}

	/** Vertical gap */
	private int vgap;

	/** Set the vertical gap */
	@Override
	public void setVGap(int g) {
		vgap = g;
	}

	/** Set the vertical gap */
	public void doSetVGap(int g) throws TMSException {
		if (g != vgap) {
			store.update(this, "vgap", g);
			setVGap(g);
		}
	}

	/** Get the vertical gap */
	@Override
	public int getVGap() {
		return vgap;
	}
}
