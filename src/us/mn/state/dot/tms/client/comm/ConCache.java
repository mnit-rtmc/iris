/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Cache for Controller-related proxy objects.
 *
 * @author Douglas Lau
 */
public class ConCache {

	/** Cache of cabinet style proxies */
	protected final TypeCache<CabinetStyle> cabinet_styles;

	/** Get the cabinet style type cache */
	public TypeCache<CabinetStyle> getCabinetStyles() {
		return cabinet_styles;
	}

	/** Cabinet style proxy list model */
	protected final ProxyListModel<CabinetStyle> cab_style_model;

	/** Get the Cabinet Style list model */
	public ProxyListModel<CabinetStyle> getCabinetStyleModel() {
		return cab_style_model;
	}

	/** Cache of cabinet proxies */
	protected final TypeCache<Cabinet> cabinets;

	/** Get the cabinet type cache */
	public TypeCache<Cabinet> getCabinets() {
		return cabinets;
	}

	/** Cache of comm link proxies */
	protected final TypeCache<CommLink> comm_links;

	/** Get the comm link type cache */
	public TypeCache<CommLink> getCommLinks() {
		return comm_links;
	}

	/** Comm link proxy list model */
	protected final ProxyListModel<CommLink> comm_link_model;

	/** Get the CommLink list model */
	public ProxyListModel<CommLink> getCommLinkModel() {
		return comm_link_model;
	}

	/** Cache of modem proxies */
	protected final TypeCache<Modem> modems;

	/** Get the modem type cache */
	public TypeCache<Modem> getModems() {
		return modems;
	}

	/** Cache of controller proxies */
	protected final TypeCache<Controller> controllers;

	/** Get the controller type cache */
	public TypeCache<Controller> getControllers() {
		return controllers;
	}

	/** Create a new con cache */
	public ConCache(SonarState client) throws IllegalAccessException,
		NoSuchFieldException
	{
		cabinet_styles = new TypeCache<CabinetStyle>(
			CabinetStyle.class, client);
		cab_style_model = new ProxyListModel<CabinetStyle>(
			cabinet_styles);
		cab_style_model.initialize();
		cabinets = new TypeCache<Cabinet>(Cabinet.class, client);
		comm_links = new TypeCache<CommLink>(CommLink.class, client);
		comm_link_model = new ProxyListModel<CommLink>(comm_links);
		comm_link_model.initialize();
		modems = new TypeCache<Modem>(Modem.class, client);
		controllers = new TypeCache<Controller>(Controller.class,
			client);
	}

	/** Populate the type caches */
	public void populate(SonarState client) {
		client.populateReadable(cabinet_styles);
		client.populateReadable(cabinets);
		client.populateReadable(comm_links);
		client.populateReadable(modems);
		client.populateReadable(controllers);
		if(client.canRead(Controller.SONAR_TYPE)) {
			controllers.ignoreAttribute("timeoutErr");
			controllers.ignoreAttribute("checksumErr");
			controllers.ignoreAttribute("parsingErr");
			controllers.ignoreAttribute("controllerErr");
			controllers.ignoreAttribute("successOps");
			controllers.ignoreAttribute("failedOps");
		}
	}
}
