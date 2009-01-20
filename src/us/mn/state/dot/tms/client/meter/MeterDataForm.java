/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.meter;

import java.awt.Dimension;
import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.plot.Plotlet;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * Action to display a plotlet containing ramp meter data
 *
 * @author Douglas Lau
 */
public class MeterDataForm extends AbstractForm {

	/** Ramp meter proxy object */
	protected final RampMeter proxy;

	/** Traffic data factory */
	protected final DataFactory factory;

	/** Create a new meter data form */
	public MeterDataForm(RampMeter p, DataFactory f) {
		super("Data for Meter: " + p.getName());
		proxy = p;
		factory = f;
	}

	/** Initialize the form */
	public void initialize() {
		Plotlet plot = new Plotlet(factory);
		// FIXME: Plotlet's preferred size is broken
		plot.setPreferredSize(new Dimension(800, 500));
		for(Detector det: proxy.getDetectors())
			plot.addDetector(det.getName());
		add(plot);
	}
}
