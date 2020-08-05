/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  Minnesota Department of Transportation
 * Copyright (C) 2020 SRF Consulting Group
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
import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Dialog for creating and viewing CameraTemplate objects.
 *
 * @author Douglas Lau
 * @author Michael Janson
 */
public class CameraTemplateProperties extends SonarObjectForm<CameraTemplate> {

	/** Camera template panel */
	private final CameraVidSourceOrderPanel cam_vid_src_ord_pnl;

	/** Create a new camera template properties form */
	public CameraTemplateProperties(Session s, CameraTemplate ct) {
		super(I18N.get("camera.template") + ": ", s, ct);
		cam_vid_src_ord_pnl = new CameraVidSourceOrderPanel(s, ct);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<CameraTemplate> getTypeCache() {
		return state.getCamTemplates();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		cam_vid_src_ord_pnl.initialize();
		add(cam_vid_src_ord_pnl);
		super.initialize();
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		cam_vid_src_ord_pnl.updateEditMode();
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		cam_vid_src_ord_pnl.updateAttribute(a);
	}
}
