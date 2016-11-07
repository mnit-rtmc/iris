/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.ControllerHelper;
import static us.mn.state.dot.tms.units.Distance.Units.DECIMETERS;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to send settings to an RTMS G4.
 *
 * @author Douglas Lau
 */
public class OpSendSensorSettings extends OpG4 {

	/** Binning interval (seconds) */
	private final int interval;

	/** Error threshold for setting date / time */
	static private final int TIME_THRESHOLD = 5000;

	/** Flag to perform a controller restart */
	private final boolean restart;

	/** Sensor information property */
	private final SensorInfoProperty sensor_info = new SensorInfoProperty();

	/** Setup information property */
	private final SetupInfoProperty setup_info = new SetupInfoProperty();

	/** Vehicle classification property */
	private final VehClassProperty class_config = new VehClassProperty();

	/** RTC property */
	private final RTCProperty rtc = new RTCProperty();

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(ControllerImpl c, boolean r) {
		super(PriorityLevel.DOWNLOAD, c);
		interval = c.getPollPeriod();
		restart = r;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<G4Property> phaseOne() {
		return new QuerySensorInfo();
	}

	/** Phase to query the sensor information */
	private class QuerySensorInfo extends Phase<G4Property> {

		/** Query the sensor information */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			mess.add(sensor_info);
			mess.queryProps();
			controller.setVersionNotify(sensor_info.getVersion());
			return new QuerySetupInfo();
		}
	}

	/** Phase to query the setup information */
	private class QuerySetupInfo extends Phase<G4Property> {

		/** Query the setup information */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			mess.add(setup_info);
			mess.queryProps();
			if (shouldUpdateSetupInfo())
				return new StoreSetupInfo();
			else
				return new QueryClassConfig();
		}
	}

	/** Check if the setup information should be updated */
	private boolean shouldUpdateSetupInfo() {
		return isConfigWrong() || isPeriodWrong() ||
		       isCompWrong() || areFlagsWrong();
	}

	/** Check if the port config is wrong */
	private boolean isConfigWrong() {
		PortConfig port_1 = setup_info.getPort1();
		return isPortConfigWrong(setup_info.getPort1()) ||
		       isPortConfigWrong(setup_info.getPort2());
	}

	/** Check if one port config is wrong */
	static boolean isPortConfigWrong(PortConfig pc) {
		return pc.isX3() || !pc.isHighOccupancy() ||
		       pc.getMode() != PortConfig.Mode.POLLED;
	}

	/** Check if period is wrong */
	private boolean isPeriodWrong() {
		return setup_info.getPeriod() != interval;
	}

	/** Check if msg composition is wrong */
	private boolean isCompWrong() {
		StatComposition comp = setup_info.getComp();
		return comp.hasGap() || comp.hasHeadway() ||
		       comp.hasSpeed85() || comp.getClassCount() != 4;
	}

	/** Check if status flags are wrong */
	private boolean areFlagsWrong() {
		StatusFlags stat_flags = setup_info.getStatusFlags();
		return !stat_flags.isFifo() || !stat_flags.isStamp() ||
		       !stat_flags.isMph();
	}

	/** Update the setup information */
	private void updateSetupInfo() {
		setup_info.setPort1(updatePortConfig(setup_info.getPort1()));
		setup_info.setPort2(updatePortConfig(setup_info.getPort2()));
		setup_info.setPeriod(interval);
		setup_info.setComp(new StatComposition(false, false, false, 4));
		StatusFlags f = setup_info.getStatusFlags();
		setup_info.setStatusFlags(new StatusFlags(true, f.isDualLoop(),
			f.isSixFoot(), f.isHighZ(), f.isMemory(), true,
			f.isClosure(), true));
		setup_info.setDate(TimeSteward.currentTimeMillis());
	}

	/** Create an update port config */
	static private PortConfig updatePortConfig(PortConfig pc) {
		return new PortConfig(pc.getPort(), PortConfig.Mode.POLLED,
			pc.isRS4xx(), pc.isRTSCTS(), pc.getBaudRate());
	}

	/** Phase to store the setup information */
	private class StoreSetupInfo extends Phase<G4Property> {

		/** Store the setup information */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			updateSetupInfo();
			mess.add(setup_info);
			mess.storeProps();
			return new QueryClassConfig();
		}
	}

	/** Phase to query the vehicle class config */
	private class QueryClassConfig extends Phase<G4Property> {

		/** Query the vehicle class config */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			mess.add(class_config);
			mess.queryProps();
			if (shouldUpdateClassConfig())
				return new StoreClassConfig();
			else
				return new QueryRTC();
		}
	}

	/** Check if the vehicle class config should be updated */
	private boolean shouldUpdateClassConfig() {
		for (G4VehClass vc: G4VehClass.values()) {
			int dm = class_config.getClassLen(vc);
			int lb = vc.v_class.lower_bound.round(DECIMETERS);
			if (dm != lb)
				return true;
		}
		return false;
	}

	/** Phase to store the vehicle class config */
	protected class StoreClassConfig extends Phase<G4Property> {

		/** Store the vehicle classes */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			updateClassConfig();
			mess.add(class_config);
			mess.storeProps();
			return new QueryRTC();
		}
	}

	/** Update the vehicle class config bounds */
	private void updateClassConfig() {
		for(G4VehClass vc: G4VehClass.values()) {
			int lb = vc.v_class.lower_bound.round(DECIMETERS);
			class_config.setClassLen(vc, lb);
		}
	}

	/** Phase to query RTC */
	private class QueryRTC extends Phase<G4Property> {

		/** Query the RTC */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			mess.add(rtc);
			mess.queryProps();
			if (shouldUpdateRTC())
				return new StoreRTC();
			else
				return null;
		}
	}

	/** Check if the RTC should be updated */
	private boolean shouldUpdateRTC() {
		long stamp = rtc.getStamp();
		long now = TimeSteward.currentTimeMillis();
		return stamp < (now - TIME_THRESHOLD) ||
		       stamp > (now + TIME_THRESHOLD);
	}

	/** Update the RTC */
	private void updateRTC() {
		rtc.setStamp(TimeSteward.currentTimeMillis());
	}

	/** Phase to store the RTC */
	private class StoreRTC extends Phase<G4Property> {

		/** Store the RTC */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			updateRTC();
			mess.add(rtc);
			mess.storeProps();
			return null;
		}
	}
}
