/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2019  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.EncoderStream;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing encoder types.
 *
 * @author Douglas Lau
 */
public class EncoderTypeForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.isWritePermitted(EncoderType.SONAR_TYPE) ||
		       s.isWritePermitted(EncoderStream.SONAR_TYPE);
	}

	/** Encoder type panel */
	private final EncoderTypePanel enc_panel;

	/** Create a new encoder type form */
	public EncoderTypeForm(Session s) {
		super(I18N.get("encoder.type.plural"));
		enc_panel = new EncoderTypePanel(s);
	}

	/** Initializze the widgets in the form */
	@Override
	protected void initialize() {
		super.initialize();
		enc_panel.initialize();
		add(enc_panel);
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		enc_panel.dispose();
		super.dispose();
	}
}
