/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Catalog;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * CatalogProperties is a dialog for entering and editing catalogs.
 *
 * @author Douglas Lau
 */
public class CatalogProperties extends SonarObjectForm<Catalog> {

	/** Catalog panel */
	private final CatalogPanel cat_pnl;

	/** Create a new catalog properties form */
	public CatalogProperties(Session s, Catalog c) {
		super(I18N.get("catalog") + ": ", s, c);
		cat_pnl = new CatalogPanel(s, c);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<Catalog> getTypeCache() {
		return state.getCamCache().getCatalogs();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		cat_pnl.initialize();
		add(cat_pnl);
		super.initialize();
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		cat_pnl.updateEditMode();
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		cat_pnl.updateAttribute(a);
	}
}
