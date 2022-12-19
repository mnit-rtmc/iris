/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to send settings to an SS125.
 *
 * @author Douglas Lau
 */
public class OpSendSensorSettings extends OpSS125 {

	/** Error threshold for setting date / time */
	static private final int TIME_THRESHOLD = 5000;

	/** Binning interval (seconds) */
	private final int interval;

	/** Flag to perform a controller restart */
	private final boolean restart;

	/** General config property */
	private final GeneralConfigProperty gen_config =
		new GeneralConfigProperty();

	/** Data config property */
	private final DataConfigProperty data_config = new DataConfigProperty();

	/** Class config property */
	private final ClassConfigProperty class_config =
		new ClassConfigProperty();

	/** Flag to indicate config has been updated */
	private boolean config_updated = false;

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(PriorityLevel p, ControllerImpl c,
		boolean r)
	{
		super(PriorityLevel.SETTINGS, c);
		restart = r;
		interval = c.getPollPeriodSec();
	}

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(ControllerImpl c, boolean r) {
		this(PriorityLevel.SETTINGS, c, r);
	}

	/** Create the first phase of the operation */
	protected Phase<SS125Property> phaseOne() {
		return new QueryVersion();
	}

	/** Phase to query the firmware version */
	protected class QueryVersion extends Phase<SS125Property> {

		/** Query the firmware version */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			VersionProperty vr = new VersionProperty();
			mess.add(vr);
			mess.queryProps();
			controller.setVersionNotify(vr.getVersion());
			return new QueryGenConfig();
		}
	}

	/** Phase to query the general config  */
	protected class QueryGenConfig extends Phase<SS125Property> {

		/** Query the general config */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			mess.add(gen_config);
			mess.queryProps();
			controller.setSetupNotify("serial_num",
				gen_config.getSerialNumber());
			return shouldUpdateGenConfig()
				? new StoreGenConfig()
				: new QueryDataConfig();
		}
	}

	/** Check if the general config should be updated */
	private boolean shouldUpdateGenConfig() {
		String loc = ControllerHelper.getLocation(controller);
		return (!loc.equals(gen_config.getLocation())) ||
			gen_config.isMetric();
	}

	/** Phase to store the general config */
	protected class StoreGenConfig extends Phase<SS125Property> {

		/** Store the general config */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			gen_config.setLocation(ControllerHelper.getLocation(
				controller));
			gen_config.setMetric(false);
			mess.add(gen_config);
			mess.storeProps();
			config_updated = true;
			return new QueryDataConfig();
		}
	}

	/** Phase to query the data config  */
	protected class QueryDataConfig extends Phase<SS125Property> {

		/** Query the data config */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			mess.add(data_config);
			mess.queryProps();
			return shouldUpdateDataConfig()
				? new StoreDataConfig()
				: new QueryClassConfig();
		}
	}

	/** Check if the data config should be updated */
	private boolean shouldUpdateDataConfig() {
		if (data_config.getInterval() != interval)
			return true;
		if (data_config.getMode() !=
		    DataConfigProperty.StorageMode.CIRCULAR)
			return true;
		if (data_config.getEventPush().getEnable())
			return true;
		if (data_config.getIntervalPush().getEnable())
			return true;
		if (data_config.getPresencePush().getEnable())
			return true;
		return false;
	}

	/** Phase to store the data config */
	protected class StoreDataConfig extends Phase<SS125Property> {

		/** Store the data config */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			data_config.setInterval(interval);
			data_config.setMode(
				DataConfigProperty.StorageMode.CIRCULAR);
			data_config.getEventPush().setEnable(false);
			data_config.getIntervalPush().setEnable(false);
			data_config.getPresencePush().setEnable(false);
			mess.add(data_config);
			mess.storeProps();
			config_updated = true;
			return new QueryClassConfig();
		}
	}

	/** Phase to query the vehicle class config  */
	protected class QueryClassConfig extends Phase<SS125Property> {

		/** Query the vehicle class config */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			mess.add(class_config);
			mess.queryProps();
			return shouldUpdateClassConfig()
				? new StoreClassConfig()
				: new QueryDateTime();
		}
	}

	/** Check if the vehicle class config should be updated */
	private boolean shouldUpdateClassConfig() {
		for (SS125VehClass vc: SS125VehClass.values()) {
			if (!vc.v_class.upper_bound.equals(
			     class_config.getClassLen(vc)))
				return true;
		}
		return false;
	}

	/** Phase to store the vehicle class config */
	protected class StoreClassConfig extends Phase<SS125Property> {

		/** Store the vehicle class config */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			updateClassConfig();
			mess.add(class_config);
			mess.storeProps();
			config_updated = true;
			return new QueryDateTime();
		}
	}

	/** Update the vehicle class config bounds */
	private void updateClassConfig() {
		for (SS125VehClass vc: SS125VehClass.values())
			class_config.setClassLen(vc, vc.v_class.upper_bound);
	}

	/** Phase to query the date and time */
	protected class QueryDateTime extends Phase<SS125Property> {

		/** Query the date and time */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			DateTimeProperty date_time = new DateTimeProperty();
			mess.add(date_time);
			mess.queryProps();
			return shouldUpdateDateTime(date_time)
				? new SendDateTime()
				: configDonePhase();
		}
	}

	/** Check if the date / time should be updated */
	private boolean shouldUpdateDateTime(DateTimeProperty date_time) {
		long stamp = date_time.getStamp().getTime();
		long now = TimeSteward.currentTimeMillis();
		return stamp < (now - TIME_THRESHOLD) ||
		       stamp > (now + TIME_THRESHOLD);
	}

	/** Phase to send the date and time */
	protected class SendDateTime extends Phase<SS125Property> {

		/** Send the date and time */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			DateTimeProperty date_time = new DateTimeProperty();
			mess.add(date_time);
			mess.storeProps();
			return configDonePhase();
		}
	}

	/** Phase after configuration is done */
	private Phase<SS125Property> configDonePhase() {
		return config_updated ? new StoreConfigFlash() : null;
	}

	/** Phase to store config to flash */
	protected class StoreConfigFlash extends Phase<SS125Property> {

		/** Store the config to flash */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			FlashConfigProperty flash = new FlashConfigProperty();
			mess.add(flash);
			mess.storeProps();
			return null;
		}
	}
}
