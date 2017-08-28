/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A tag reader manager is a container for SONAR tag reader objects.
 *
 * @author Douglas Lau
 */
public class TagReaderManager extends DeviceManager<TagReader> {

	/** Color to display available readers */
	static private final Color COLOR_AVAILABLE = new Color(64, 128, 255);

	/** Create a proxy descriptor */
	static public ProxyDescriptor<TagReader> descriptor(final Session s) {
		return new ProxyDescriptor<TagReader>(
			s.getSonarState().getTagReaders(), true
		) {
			@Override
			public TagReaderProperties createPropertiesForm(
				TagReader tr)
			{
				return new TagReaderProperties(s, tr);
			}
			@Override
			public TagReaderForm makeTableForm() {
				return new TagReaderForm(s);
			}
		};
	}

	/** Create a new tag reader manager */
	public TagReaderManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 14);
	}

	/** Create the map tab */
	@Override
	public TagReaderTab createTab() {
		return new TagReaderTab(session, this);
	}

	/** Create a theme for tag readers */
	@Override
	protected ProxyTheme<TagReader> createTheme() {
		ProxyTheme<TagReader> theme = new ProxyTheme<TagReader>(this,
			new TagReaderMarker());
		theme.addStyle(ItemStyle.AVAILABLE, COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(ItemStyle.ALL);
		return theme;
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
