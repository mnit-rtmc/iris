/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2017  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing camera templates.
 *
 * @author Douglas Lau
 * @author Michael Janson
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class CameraTemplateForm extends ProxyTableForm<CameraTemplate> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(CameraTemplate.SONAR_TYPE);
	}

	/** Create a new incident detail form */
	public CameraTemplateForm(Session s) {
		super(I18N.get("camera.templates"), 
				new CameraTemplatePanel(
						new CameraTemplateModel(s)));
	}
}

