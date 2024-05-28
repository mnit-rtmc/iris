/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a dynamic message
 * sign (DMS).
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSProperties extends SonarObjectForm<DMS> {

	/** Operation panel */
	private final PropOp op_pnl;

	/** Location panel */
	private final PropLocation location_pnl;

	/** Setup panel */
	private final PropSetup setup_pnl;

	/** RWIS panel */
	private final PropRwis rwis_pnl;

	/** Status panel */
	private final PropStatus status_pnl;

	/** Pixel panel */
	private final PropPixels pixel_pnl;

	/** Brightness panel */
	private final PropBrightness bright_pnl;

	/** Manufacturer panel */
	private final PropManufacturer manufacturer_pnl;

	/** Create a new DMS properties form */
	public DMSProperties(Session s, DMS sign) {
		super(I18N.get("dms") + ": ", s, sign);
		setHelpPageName("help.dmsproperties");
		op_pnl = new PropOp(sign);
		location_pnl = new PropLocation(s, sign);
		setup_pnl = new PropSetup(s, sign);
		rwis_pnl = new PropRwis(s, sign);
		status_pnl = new PropStatus(s, sign);
		pixel_pnl = new PropPixels(s, sign);
		bright_pnl = new PropBrightness(s, sign);
		manufacturer_pnl = new PropManufacturer(s, sign);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<DMS> getTypeCache() {
		return state.getDmsCache().getDMSs();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		op_pnl.initialize();
		location_pnl.initialize();
		setup_pnl.initialize();
		rwis_pnl.initialize();
		status_pnl.initialize();
		pixel_pnl.initialize();
		bright_pnl.initialize();
		manufacturer_pnl.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), location_pnl);
		tab.add(I18N.get("device.setup"), setup_pnl);
		tab.add(I18N.get("dms.rwis"), rwis_pnl);
		tab.add(I18N.get("device.status"), status_pnl);
		tab.add(I18N.get("dms.pixels"), pixel_pnl);
		tab.add(I18N.get("dms.brightness"), bright_pnl);
		tab.add(I18N.get("dms.manufacturer"), manufacturer_pnl);
		add(tab);
		add(op_pnl, BorderLayout.SOUTH);
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		location_pnl.dispose();
		setup_pnl.dispose();
		rwis_pnl.dispose();
		op_pnl.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		location_pnl.updateEditMode();
		setup_pnl.updateEditMode();
		rwis_pnl.updateEditMode();
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		op_pnl.updateAttribute(a);
		location_pnl.updateAttribute(a);
		setup_pnl.updateAttribute(a);
		rwis_pnl.updateAttribute(a);
		status_pnl.updateAttribute(a);
		pixel_pnl.updateAttribute(a);
		bright_pnl.updateAttribute(a);
		manufacturer_pnl.updateAttribute(a);
	}
}
