/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import java.awt.Color;
import javax.swing.JLabel;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.widget.IPanel;

/**
 * PropOp is a GUI panel for displaying current operation on a DMS properties
 * form.
 *
 * @author Douglas Lau
 */
public class PropOp extends IPanel {

	/** Operation description label */
	private final JLabel operation_lbl = createValueLabel();

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties operation panel */
	public PropOp(DMS sign) {
		dms = sign;
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		add("device.operation");
		add(operation_lbl, Stretch.LEFT);
		setBackground(SonarObjectForm.LIGHTER_GRAY);
		updateAttribute(null);
	}

	/** Update one attribute on the panel */
	public void updateAttribute(String a) {
		if (a == null || a.equals("operation"))
			operation_lbl.setText(dms.getOperation());
	}
}
