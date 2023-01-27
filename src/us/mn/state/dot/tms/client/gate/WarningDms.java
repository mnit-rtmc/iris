/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.SignPixelPager;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;

/**
 * A warning DMS is a sign associated with a gate arm array.
 *
 * @author Douglas Lau
 */
public class WarningDms {

	/** Get the filter color for a DMS */
	static private Color filterColor(DMS dms) {
		return (dms != null) ? SignPixelPanel.filterColor(dms) : null;
	}

	/** SONAR session */
	private final Session session;

	/** Proxy view for DMS */
	private final ProxyView<DMS> view = new ProxyView<DMS>() {
		public void enumerationComplete() { }
		public void update(DMS d, String a) {
			if (null == a ||
			    "styles".equals(a) ||
			    "msgCurrent".equals(a))
				updateDms(d);
		}
		public void clear() {
			clearDms();
		}
	};

	/** Watcher for DMS */
	private final ProxyWatcher<DMS> watcher;

	/** Sign pixel panel */
	public final SignPixelPanel pix_pnl = new SignPixelPanel(132, 80);

	/** Pager for sign pixel panel */
	private SignPixelPager pager;

	/** Create a new warning DMS */
	public WarningDms(Session s) {
		session = s;
		TypeCache<DMS> cache = s.getSonarState().getDmsCache()
			.getDMSs();
		watcher = new ProxyWatcher<DMS>(cache, view, false);
	}

	/** Initialize the warning DMS */
	public void initialize() {
		watcher.initialize();
		pix_pnl.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				selectDms();
			}
		});
	}

	/** Select the DMS */
	private void selectDms() {
		DMS d = watcher.getProxy();
		if (d != null) {
			session.getDMSManager().getSelectionModel().
				setSelected(d);
		}
	}

	/** Dispose of the warning DMS */
	public void dispose() {
		watcher.dispose();
		setPager(null);
	}

	/** Update the DMS */
	private void updateDms(DMS d) {
		pix_pnl.setFilterColor(filterColor(d));
		RasterGraphic[] rg = DMSHelper.createRasters(d);
		if (rg != null) {
			String ms = DMSHelper.getMultiString(d);
			pix_pnl.setDimensions(d.getSignConfig());
			setPager(new SignPixelPager(pix_pnl, rg, ms));
		} else
			clearDms();
	}

	/** Clear the DMS */
	private void clearDms() {
		setPager(null);
		pix_pnl.setFilterColor(null);
		pix_pnl.setDimensions(null);
	}

	/** Set the pager */
	private void setPager(SignPixelPager p) {
		SignPixelPager op = pager;
		if (op != null)
			op.dispose();
		pager = p;
	}

	/** Set the selected DMS */
	public void setSelected(DMS dms) {
		watcher.setProxy(dms);
	}
}
