/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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

	/** Time interval for data binning */
	static protected final int BINNING_INTERVAL = 30;

	/** Error threshold for setting date / time */
	static protected final int TIME_THRESHOLD = 5000;

	/** Flag to perform a controller restart */
	protected final boolean restart;

	/** General config property */
	protected final GeneralConfigProperty gen_config =
		new GeneralConfigProperty();

	/** General config property */
	protected final DataConfigProperty data_config =
		new DataConfigProperty();

	/** Flag to indicate config has been updated */
	protected boolean config_updated = false;

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(ControllerImpl c, boolean r) {
		super(PriorityLevel.DOWNLOAD, c);
		restart = r;
	}

	/** Begin the sensor initialization operation */
	public boolean begin() {
		phase = new QueryGenConfig();
		return true;
	}

	/** Phase to query the general config  */
	protected class QueryGenConfig extends Phase {

		/** Query the general config */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(gen_config);
			mess.queryProps();
			SS125_LOG.log(controller.getName() + ": orientation " +
				gen_config.getOrientation());
			SS125_LOG.log(controller.getName() + ": location " +
				gen_config.getLocation());
			SS125_LOG.log(controller.getName() + ": description " +
				gen_config.getDescription());
			SS125_LOG.log(controller.getName() + ": serial # " +
				gen_config.getSerialNumber());
			SS125_LOG.log(controller.getName() + ": metric " +
				gen_config.isMetric());
			if(shouldUpdateGenConfig())
				return new SendGenConfig();
			else
				return new QueryDataConfig();
		}
	}

	/** Check if the general config should be updated */
	protected boolean shouldUpdateGenConfig() {
		String loc = ControllerHelper.getLocation(controller);
		if(!loc.equals(gen_config.getLocation()))
			return true;
		return gen_config.isMetric();
	}

	/** Phase to send the general config */
	protected class SendGenConfig extends Phase {

		/** Send the general config */
		protected Phase poll(CommMessage mess) throws IOException {
			gen_config.setLocation(ControllerHelper.getLocation(
				controller));
			gen_config.setMetric(false);
			mess.add(gen_config);
			SS125_LOG.log(controller.getName() + ":= location " +
				gen_config.getLocation());
			SS125_LOG.log(controller.getName() + ":= metric " +
				gen_config.isMetric());
			mess.storeProps();
			config_updated = true;
			return new QueryDataConfig();
		}
	}

	/** Phase to query the data config  */
	protected class QueryDataConfig extends Phase {

		/** Query the data config */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(data_config);
			mess.queryProps();
			if(SS125_LOG.isOpen())
				logDataConfig();
			if(shouldUpdateDataConfig())
				return new SendDataConfig();
			else
				return configDonePhase();
		}
	}

	/** Log data configuration */
	protected void logDataConfig() {
		SS125_LOG.log(controller.getName() + ": interval " +
			data_config.getInterval());
		SS125_LOG.log(controller.getName() + ": mode " +
			data_config.getMode());
		logPushConfig(data_config.getEventPush(), "event");
		logPushConfig(data_config.getIntervalPush(), "interval");
		logPushConfig(data_config.getPresencePush(), "presence");
		SS125_LOG.log(controller.getName() + ": default separation " +
			data_config.getDefaultSeparation());
		SS125_LOG.log(controller.getName() + ": default size " +
			data_config.getDefaultSize());
	}

	/** Log push config for one data type */
	protected void logPushConfig(DataConfigProperty.PushConfig pc,
		String dtype)
	{
		SS125_LOG.log(controller.getName() + ": " + dtype +
			" enable " + pc.enable);
		if(pc.enable) {
			SS125_LOG.log(controller.getName() + ": " + dtype +
				" port " + pc.port);
			SS125_LOG.log(controller.getName() + ": " + dtype +
				" protocol " + pc.protocol);
			SS125_LOG.log(controller.getName() + ": " + dtype +
				" dest_sub_id " + pc.dest_sub_id);
			SS125_LOG.log(controller.getName() + ": " + dtype +
				" dest_id " + pc.dest_id);
		}
	}

	/** Check if the data config should be updated */
	protected boolean shouldUpdateDataConfig() {
		if(data_config.getInterval() != BINNING_INTERVAL)
			return true;
		if(data_config.getMode() !=
		   DataConfigProperty.StorageMode.CIRCULAR)
			return true;
		if(data_config.getEventPush().enable)
			return true;
		if(data_config.getIntervalPush().enable)
			return true;
		if(data_config.getPresencePush().enable)
			return true;
		return false;
	}

	/** Phase to send the data config */
	protected class SendDataConfig extends Phase {

		/** Send the data config */
		protected Phase poll(CommMessage mess) throws IOException {
			data_config.setInterval(BINNING_INTERVAL);
			data_config.setMode(
				DataConfigProperty.StorageMode.CIRCULAR);
			data_config.getEventPush().enable = false;
			data_config.getIntervalPush().enable = false;
			data_config.getPresencePush().enable = false;
			mess.add(data_config);
			SS125_LOG.log(controller.getName() + ":= data config");
			mess.storeProps();
			config_updated = true;
			return configDonePhase();
		}
	}

	/** Phase after configuration is done */
	protected Phase configDonePhase() {
		if(config_updated)
			return new StoreConfigFlash();
		else
			return new QueryDateTime();
	}

	/** Phase to store config to flash */
	protected class StoreConfigFlash extends Phase {

		/** Store the config to flash */
		protected Phase poll(CommMessage mess) throws IOException {
			FlashConfigProperty flash = new FlashConfigProperty();
			mess.add(flash);
			SS125_LOG.log(controller.getName() + ":= flash config");
			mess.storeProps();
			return new QueryDateTime();
		}
	}

	/** Phase to query the date and time */
	protected class QueryDateTime extends Phase {

		/** Query the date and time */
		protected Phase poll(CommMessage mess) throws IOException {
			DateTimeProperty date_time = new DateTimeProperty();
			mess.add(date_time);
			mess.queryProps();
			SS125_LOG.log(controller.getName() + ": date/time " +
				date_time.getStamp());
			if(shouldUpdateDateTime(date_time.getStamp().getTime()))
				return new SendDateTime();
			else
				return null;
		}
	}

	/** Check if the date / time should be updated */
	protected boolean shouldUpdateDateTime(long stamp) {
		long now = TimeSteward.currentTimeMillis();
		return stamp < (now - TIME_THRESHOLD) ||
		       stamp > (now + TIME_THRESHOLD);
	}

	/** Phase to send the date and time */
	protected class SendDateTime extends Phase {

		/** Send the date and time */
		protected Phase poll(CommMessage mess) throws IOException {
			DateTimeProperty date_time = new DateTimeProperty();
			mess.add(date_time);
			mess.storeProps();
			SS125_LOG.log(controller.getName() + ":= date/time " +
				date_time.getStamp());
			return null;
		}
	}
}
