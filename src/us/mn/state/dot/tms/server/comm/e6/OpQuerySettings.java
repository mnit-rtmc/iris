/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.e6;

import java.io.IOException;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query settings from an E6.
 *
 * @author Douglas Lau
 */
public class OpQuerySettings extends OpE6 {

	/** Reader settings */
	private final ReaderSettings settings = new ReaderSettings();

	/** Create a new "query settings" operation */
	public OpQuerySettings(TagReaderImpl tr) {
		super(PriorityLevel.SETTINGS, tr);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<E6Property> phaseTwo() {
		return new QueryFirmware();
	}

	/** Phase to query the firmware versions */
	private class QueryFirmware extends Phase<E6Property> {

		/** Query the firmware versions */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FirmwareVersionsProp v = new FirmwareVersionsProp();
			sendQuery(mess, v);
			mess.logQuery(v);
			controller.setVersionNotify(v.toString());
			return new QueryAckTimeout();
		}
	}

	/** Phase to query the data ack timeout */
	private class QueryAckTimeout extends Phase<E6Property> {

		/** Query the ack timeout */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			sendQuery(mess, settings.ack_timeout);
			mess.logQuery(settings.ack_timeout);
			return new QueryDownlink();
		}
	}

	/** Phase to query the downlink frequency */
	private class QueryDownlink extends Phase<E6Property> {

		/** Query the downlink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			sendQuery(mess, settings.downlink_freq);
			mess.logQuery(settings.downlink_freq);
			tag_reader.setDownlinkFreqKhzNotify(
				settings.downlink_freq.getFreqKhz()
			);
			return new QueryUplink();
		}
	}

	/** Phase to query the uplink frequency */
	private class QueryUplink extends Phase<E6Property> {

		/** Query the uplink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			sendQuery(mess, settings.uplink_freq);
			mess.logQuery(settings.uplink_freq);
			tag_reader.setUplinkFreqKhzNotify(
				settings.uplink_freq.getFreqKhz()
			);
			return nextQueryPhase(null);
		}
	}

	/** Get the next query phase */
	private Phase<E6Property> nextQueryPhase(RFProtocol p_prot) {
		RFProtocol p = RFProtocol.next(p_prot);
		return (p != null) ? new QueryAtten(p) : new QueryLineLoss();
	}

	/** Phase to query the RF attenuation for one protocol */
	private class QueryAtten extends Phase<E6Property> {
		private final RFProtocol protocol;
		private QueryAtten(RFProtocol p) {
			protocol = p;
		}

		/** Query the RF attenuation */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFAttenProp atten = settings.getRfAtten(protocol);
			try {
				sendQuery(mess, atten);
			}
			catch (ControllerException e) {
				// SUB_COMMAND_ERROR => protocol not supported
				return nextQueryPhase(protocol);
			}
			mess.logQuery(atten);
			settings.storeRfAtten(tag_reader, protocol);
			return new QueryDataDetect(protocol);
		}
	}

	/** Phase to query the data detect for one protocol */
	private class QueryDataDetect extends Phase<E6Property> {
		private final RFProtocol protocol;
		private QueryDataDetect(RFProtocol p) {
			protocol = p;
		}

		/** Query the data detect */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			DataDetectProp det = settings.getDataDetect(protocol);
			sendQuery(mess, det);
			mess.logQuery(det);
			settings.storeDataDetect(tag_reader, protocol);
			return new QuerySeen(protocol);
		}
	}

	/** Phase to query the seen count for one protocol */
	private class QuerySeen extends Phase<E6Property> {
		private final RFProtocol protocol;
		private QuerySeen(RFProtocol p) {
			protocol = p;
		}

		/** Query the seen count */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			SeenCountProp seen = settings.getSeenCount(protocol);
			sendQuery(mess, seen);
			mess.logQuery(seen);
			settings.storeSeenUnique(tag_reader, protocol);
			return nextQueryPhase(protocol);
		}
	}

	/** Phase to query the line loss */
	private class QueryLineLoss extends Phase<E6Property> {

		/** Query the line loss */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			sendQuery(mess, settings.line_loss);
			mess.logQuery(settings.line_loss);
			tag_reader.setLineLossDbNotify(
				settings.line_loss.getValue()
			);
			return new QueryMuxMode();
		}
	}

	/** Phase to query the mux mode */
	private class QueryMuxMode extends Phase<E6Property> {

		/** Query the mux mode */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			sendQuery(mess, settings.mux_mode);
			mess.logQuery(settings.mux_mode);
			return new QueryAntennaChannel();
		}
	}

	/** Phase to query the manual antenna channel control */
	private class QueryAntennaChannel extends Phase<E6Property> {

		/** Query the manual antenna channel */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			sendQuery(mess, settings.antenna_channel);
			mess.logQuery(settings.antenna_channel);
			return new QueryMasterSlave();
		}
	}

	/** Phase to query the master/slave setting */
	private class QueryMasterSlave extends Phase<E6Property> {

		/** Query the master/slave setting */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			sendQuery(mess, settings.master_slave);
			mess.logQuery(settings.master_slave);
			tag_reader.setSyncModeNotify(
				settings.master_slave.getMode()
			);
			tag_reader.setSlaveSelectCountNotify(
				settings.master_slave.getSlaveSelectCount()
			);
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			// FIXME: store settings
		}
		super.cleanup();
	}
}
