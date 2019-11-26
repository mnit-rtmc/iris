/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * DeviceDeployModel is a model of devices to deploy for an incident.
 *
 * @author Douglas Lau
 */
public class DeviceDeployModel extends DefaultListModel<Device> {

	/** Upstream device finder */
	private final UpstreamDeviceFinder finder;

	/** LCS indication builder */
	private final LcsIndicationBuilder ind_builder;

	/** DMS deploy message builder */
	private final DmsDeployBuilder dms_builder;

	/** Mapping of LCS array names to proposed indications */
	private final HashMap<String, Integer []> indications =
		new HashMap<String, Integer []>();

	/** Get the proposed indications for an LCS array */
	public Integer[] getIndications(String lcs_a) {
		return indications.get(lcs_a);
	}

	/** Mapping of DMS names to proposed MULTI strings */
	private final HashMap<String, String> messages =
		new HashMap<String, String>();

	/** Get the proposed MULTI for a DMS */
	public String getMulti(String dms) {
		String multi = messages.get(dms);
		return (multi != null) ? multi : "";
	}

	/** Mapping of DMS names to proposed page one graphics */
	private final HashMap<String, RasterGraphic> graphics =
		new HashMap<String, RasterGraphic>();

	/** Get the proposed graphics for a DMS */
	public RasterGraphic getGraphic(String dms) {
		return graphics.get(dms);
	}

	/** Create a new device deploy model */
	public DeviceDeployModel(IncidentManager man, Incident inc) {
		finder = new UpstreamDeviceFinder(man, inc);
		ind_builder = new LcsIndicationBuilder(man, inc);
		dms_builder = new DmsDeployBuilder(man, inc);
		populateList();
		// Already deployed signs should be blanked if not found
		ArrayList<DMS> signs = IncidentHelper.getDeployedSigns(inc);
		Iterator<DMS> it = signs.iterator();
		while (it.hasNext())
			addExistingDMS(it.next());
	}

	/** Populate list model with device deployments */
	private void populateList() {
		finder.findDevices();
		Iterator<UpstreamDevice> it = finder.iterator();
		while (it.hasNext()) {
			UpstreamDevice ud = it.next();
			Device dev = ud.device;
			if (dev instanceof LCSArray)
				addUpstreamLCS((LCSArray) dev, ud);
			if (dev instanceof DMS)
				addUpstreamDMS((DMS) dev, ud);
		}
	}

	/** Add an upstream LCS array */
	private void addUpstreamLCS(LCSArray lcs_array, UpstreamDevice ud) {
		Integer[] ind = ind_builder.createIndications(lcs_array,
			ud.distance);
		if (ind != null) {
			addElement(lcs_array);
			indications.put(lcs_array.getName(), ind);
		}
	}

	/** Add an upstream DMS */
	private void addUpstreamDMS(DMS dms, UpstreamDevice ud) {
		String multi = dms_builder.createMulti(dms, ud, false);
		if (multi != null) {
			RasterGraphic rg = DMSHelper.createPageOne(dms, multi);
			if (rg != null) {
				addElement(dms);
				messages.put(dms.getName(), multi);
				graphics.put(dms.getName(), rg);
			}
		}
	}

	/** Add an existing (already deployed) DMS, to be blanked */
	private void addExistingDMS(DMS dms) {
		if (!messages.containsKey(dms.getName())) {
			addElement(dms);
			messages.put(dms.getName(), "");
		}
	}
}
