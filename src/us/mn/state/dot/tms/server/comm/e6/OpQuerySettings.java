/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2018  Minnesota Department of Transportation
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

	/** Supported protocols */
	static private final RFProtocol[] PROTOCOLS = {
		RFProtocol.SeGo, RFProtocol.IAG
	};

	/** Create a new "query settings" operation */
	public OpQuerySettings(TagReaderImpl tr) {
		super(PriorityLevel.DOWNLOAD, tr);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<E6Property> phaseTwo() {
		return new QueryAckTimeout();
	}

	/** Phase to query the data ack timeout */
	private class QueryAckTimeout extends Phase<E6Property> {

		/** Query the ack timeout */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			AckTimeoutProp ato = new AckTimeoutProp(
				AckTimeoutProp.Protocol.udp_ip);
			sendQuery(mess, ato);
			mess.logQuery(ato);
			return new QueryDownlink();
		}
	}

	/** Phase to query the downlink frequency */
	private class QueryDownlink extends Phase<E6Property> {

		/** Query the downlink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.downlink);
			sendQuery(mess, freq);
			mess.logQuery(freq);
			tag_reader.setDownlinkFreqKhzNotify(freq.getFreqKhz());
			return new QueryUplink();
		}
	}

	/** Phase to query the uplink frequency */
	private class QueryUplink extends Phase<E6Property> {

		/** Query the uplink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.uplink);
			sendQuery(mess, freq);
			mess.logQuery(freq);
			tag_reader.setUplinkFreqKhzNotify(freq.getFreqKhz());
			return nextQueryPhase(null);
		}
	}

	/** Get the next query phase */
	private Phase<E6Property> nextQueryPhase(RFProtocol p_prot) {
		for (RFProtocol p: PROTOCOLS) {
			if (null == p_prot)
				return new QueryAtten(p);
			if (p == p_prot)
				p_prot = null;
		}
		return new QueryLineLoss();
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
			RFAttenProp atten = new RFAttenProp(protocol);
			try {
				sendQuery(mess, atten);
			}
			catch (ControllerException e) {
				// SUB_COMMAND_ERROR => protocol not supported
				return nextQueryPhase(protocol);
			}
			mess.logQuery(atten);
			setAtten(protocol, atten);
			return new QueryDataDetect(protocol);
		}
	}

	/** Set attenuation for one protocol */
	private void setAtten(RFProtocol protocol, RFAttenProp atten) {
		switch (protocol) {
		case SeGo:
			tag_reader.setSeGoAttenDownlinkDbNotify(
				atten.getDownlinkDb());
			tag_reader.setSeGoAttenUplinkDbNotify(
				atten.getUplinkDb());
			break;
		case IAG:
			tag_reader.setIAGAttenDownlinkDbNotify(
				atten.getDownlinkDb());
			tag_reader.setIAGAttenUplinkDbNotify(
				atten.getUplinkDb());
			break;
		default:
			break;
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
			DataDetectProp det = new DataDetectProp(protocol);
			sendQuery(mess, det);
			mess.logQuery(det);
			setDataDetect(protocol, det);
			return new QuerySeen(protocol);
		}
	}

	/** Set data detect for one protocol */
	private void setDataDetect(RFProtocol protocol, DataDetectProp det) {
		switch (protocol) {
		case SeGo:
			tag_reader.setSeGoDataDetectDbNotify(det.getValue());
			break;
		case IAG:
			tag_reader.setIAGDataDetectDbNotify(det.getValue());
			break;
		default:
			break;
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
			SeenCountProp seen = new SeenCountProp(protocol);
			sendQuery(mess, seen);
			mess.logQuery(seen);
			setSeenUnique(protocol, seen);
			return nextQueryPhase(protocol);
		}
	}

	/** Set seen/unique for one protocol */
	private void setSeenUnique(RFProtocol protocol, SeenCountProp seen) {
		switch (protocol) {
		case SeGo:
			tag_reader.setSeGoSeenCountNotify(seen.getSeen());
			tag_reader.setSeGoUniqueCountNotify(seen.getUnique());
			break;
		case IAG:
			tag_reader.setIAGSeenCountNotify(seen.getSeen());
			tag_reader.setIAGUniqueCountNotify(seen.getUnique());
			break;
		default:
			break;
		}
	}

	/** Phase to query the line loss */
	private class QueryLineLoss extends Phase<E6Property> {

		/** Query the line loss */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			LineLossProp loss = new LineLossProp();
			sendQuery(mess, loss);
			mess.logQuery(loss);
			tag_reader.setLineLossDbNotify(loss.getValue());
			return new QueryMuxMode();
		}
	}

	/** Phase to query the mux mode */
	private class QueryMuxMode extends Phase<E6Property> {

		/** Query the mux mode */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			MuxModeProp mode = new MuxModeProp();
			sendQuery(mess, mode);
			mess.logQuery(mode);
			return new QueryAntennaChannel();
		}
	}

	/** Phase to query the manual antenna channel control */
	private class QueryAntennaChannel extends Phase<E6Property> {

		/** Query the manual antenna channel */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			AntennaChannelProp chan = new AntennaChannelProp();
			sendQuery(mess, chan);
			mess.logQuery(chan);
			return new QueryMasterSlave();
		}
	}

	/** Phase to query the master/slave setting */
	private class QueryMasterSlave extends Phase<E6Property> {

		/** Query the master/slave setting */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			MasterSlaveProp mstr = new MasterSlaveProp();
			sendQuery(mess, mstr);
			mess.logQuery(mstr);
			tag_reader.setSyncModeNotify(mstr.getMode());
			tag_reader.setSlaveSelectCountNotify(
				mstr.getSlaveSelectCount());
			return new QueryFirmware();
		}
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
			return null;
		}
	}
}
