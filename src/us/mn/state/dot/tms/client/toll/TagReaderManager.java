/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toll;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A tag reader manager is a container for SONAR tag reader objects.
 *
 * @author Douglas Lau
 */
public class TagReaderManager extends ProxyManager<TagReader> {

	/** Color to display available readers */
	static private final Color COLOR_AVAILABLE = new Color(64, 128, 255);

	/** Tag reader tab */
	private final TagReaderTab tab;

	/** Create a new tag reader manager */
	public TagReaderManager(Session s, GeoLocManager lm) {
		super(s, lm, true, 14);
		tab = new TagReaderTab(s, this);
	}

	/** Get the sonar type name */
	@Override
	public String getSonarType() {
		return TagReader.SONAR_TYPE;
	}

	/** Get the tag reader cache */
	@Override
	public TypeCache<TagReader> getCache() {
		return session.getSonarState().getTagReaders();
	}

	/** Create the map tab */
	@Override
	public TagReaderTab createTab() {
		return tab;
	}

	/** Create a theme for tag readers */
	@Override
	protected ProxyTheme<TagReader> createTheme() {
		ProxyTheme<TagReader> theme = new ProxyTheme<TagReader>(this,
			new TagReaderMarker());
		theme.addStyle(ItemStyle.AVAILABLE, COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(ItemStyle.NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, TagReader proxy) {
		switch(is) {
		case AVAILABLE:
			return !ControllerHelper.isFailed(
			       proxy.getController());
		case FAILED:
			return ControllerHelper.isFailed(proxy.getController());
		case NO_CONTROLLER:
			return proxy.getController() == null;
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Create a properties form for the specified proxy */
	@Override
	protected TagReaderProperties createPropertiesForm(TagReader tr) {
		return new TagReaderProperties(session, tr);
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " " + I18N.get(
			"tag_reader.title")));
		p.addSeparator();
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(TagReader proxy) {
		return proxy.getGeoLoc();
	}
}
