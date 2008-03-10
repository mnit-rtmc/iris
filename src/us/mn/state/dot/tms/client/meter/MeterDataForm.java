/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import us.mn.state.dot.data.HttpDataFactory;
import us.mn.state.dot.data.SystemConfig;
import us.mn.state.dot.data.TmsConfig;
import us.mn.state.dot.data.plot.Plotlet;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.utils.ExceptionDialog;

/**
 * Action to display a plotlet containing ramp meter data
 *
 * @author Douglas Lau
 */
public class MeterDataForm extends AbstractForm {

	/** Location of TMS configuration */
	static protected final String CFG_URL =
		"http://data.dot.state.mn.us:8080/dds/tms.xml.gz?nocache";

	/** Get a data factory */
	static protected HttpDataFactory getDataFactory()
		throws MalformedURLException, InstantiationException
	{
		SystemConfig[] cfgs = new SystemConfig[1];
		cfgs[0] = new TmsConfig("RTMC", new URL(CFG_URL));
		return new HttpDataFactory("http://tms-iris-bk:8080/trafdat",
			cfgs);
	}

	/** Ramp meter to display data */
	protected final MeterProxy proxy;

	/** Create a new meter data form */
	public MeterDataForm(MeterProxy p) {
		super("Data for Meter: " + p.getId());
		proxy = p;
	}

	/** Initialize the form */
	protected void doInit() throws RemoteException, MalformedURLException,
		InstantiationException
	{
		int index = proxy.meter.getDetector().getIndex();
		HttpDataFactory factory = getDataFactory();
		Plotlet plot = new Plotlet(factory, Integer.toString(index));
		// FIXME: Plotlet's preferred size is broken
		plot.setPreferredSize(new Dimension(800, 500));
		for(Detector det: proxy.meter.getDetectors())
			plot.addDetector(Integer.toString(det.getIndex()));
		add(plot);
	}

	/** Initialize the form */
	public void initialize() throws RemoteException {
		try {
			doInit();
		}
		catch(InstantiationException e) {
			new ExceptionDialog(e).setVisible(true);
		}
		catch(MalformedURLException e) {
			new ExceptionDialog(e).setVisible(true);
		}
	}
}
