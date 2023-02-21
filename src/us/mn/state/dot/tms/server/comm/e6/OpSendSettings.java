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
import us.mn.state.dot.tms.TagReaderSyncMode;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send settings to an E6.
 * If the reader is in stop mode, send all settings.
 * Otherwise, check that settings are ok, and if not, put into stop mode and
 * start over.
 *
 * @author Douglas Lau
 */
public class OpSendSettings extends OpE6 {

	/** Flag to indicate stop mode */
	private boolean stop = false;

	/** Create a new "send settings" operation */
	public OpSendSettings(TagReaderImpl tr) {
		super(PriorityLevel.SETTINGS, tr);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<E6Property> phaseTwo() {
		// store ACK timeout without checking it first
		// to prevent timeout errors on an invalid config
		return new StoreAckTimeout();
	}

	/** Phase to store the data ACK timeout */
	private class StoreAckTimeout extends Phase<E6Property> {

		/** Store the ACK timeout */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			AckTimeoutProp ato = new AckTimeoutProp(
				AckTimeoutProp.Protocol.udp_ip,
				getTimeout(mess));
			mess.logStore(ato);
			sendStore(mess, ato);
			return new QueryTimeDate();
		}
	}

	/** Phase to query the time / date */
	private class QueryTimeDate extends Phase<E6Property> {

		/** Query the time / date */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			TimeDateProp stamp = new TimeDateProp();
			sendQuery(mess, stamp);
			mess.logQuery(stamp);
			if (stamp.isNear(500))
				return new QueryMode();
			else
				return new StoreTimeDate();
		}
	}

	/** Phase to store the time / date */
	private class StoreTimeDate extends Phase<E6Property> {

		/** Store the time / date */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			TimeDateProp stamp = new TimeDateProp();
			mess.logStore(stamp);
			sendStore(mess, stamp);
			return new QueryMode();
		}
	}

	/** Phase to query the mode */
	private class QueryMode extends Phase<E6Property> {

		/** Query the mode */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			ModeProp mode = new ModeProp();
			sendQuery(mess, mode);
			mess.logQuery(mode);
			if (mode.getMode() == ModeProp.Mode.stop)
				stop = true;
			return new QueryBufferingEnabled();
		}
	}

	/** Phase to query the buffering enabled */
	private class QueryBufferingEnabled extends Phase<E6Property> {

		/** Query the buffering enabled */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			BufferingEnabledProp enabled =
				new BufferingEnabledProp();
			sendQuery(mess, enabled);
			mess.logQuery(enabled);
			return enabled.isEnabled()
			     ? new QueryAppendData()
			     : new StoreBufferingEnabled();
		}
	}

	/** Phase to store the buffering enabled */
	private class StoreBufferingEnabled extends Phase<E6Property> {

		/** Store the buffering enabled */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			BufferingEnabledProp enabled =
				new BufferingEnabledProp(true);
			mess.logStore(enabled);
			sendStore(mess, enabled);
			return new QueryAppendData();
		}
	}

	/** Phase to query the append data */
	private class QueryAppendData extends Phase<E6Property> {

		/** Query the append data */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			AppendDataProp append = new AppendDataProp();
			sendQuery(mess, append);
			mess.logQuery(append);
			AppendDataProp.Value v = append.getValue();
			return (AppendDataProp.Value.disabled == v)
			     ? new StoreAppendData()
			     : new QueryRFControl();
		}
	}

	/** Phase to store the append data */
	private class StoreAppendData extends Phase<E6Property> {

		/** Store the append data */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			AppendDataProp append = new AppendDataProp(
				AppendDataProp.Value.date_time_stamp);
			mess.logStore(append);
			sendStore(mess, append);
			return new QueryRFControl();
		}
	}

	/** Phase to query the RF control */
	private class QueryRFControl extends Phase<E6Property> {

		/** Query the RF control */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFControlProp ctrl = new RFControlProp();
			sendQuery(mess, ctrl);
			mess.logQuery(ctrl);
			RFControlProp.Value v = ctrl.getValue();
			return (RFControlProp.Value.continuous == v)
			     ? checkDownlink()
			     : new StoreRFControl();
		}
	}

	/** Phase to store the RF control */
	private class StoreRFControl extends Phase<E6Property> {

		/** Store the RF control */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFControlProp ctrl = new RFControlProp(
				RFControlProp.Value.continuous);
			mess.logStore(ctrl);
			sendStore(mess, ctrl);
			return checkDownlink();
		}
	}

	/** Create phase to check the downlink frequency */
	private Phase<E6Property> checkDownlink() {
		Integer df = tag_reader.getDownlinkFreqKhz();
		return (df != null) ? new CheckDownlink(df) : checkUplink();
	}

	/** Phase to check the downlink frequency */
	private class CheckDownlink extends Phase<E6Property> {
		private final int downlink_freq;
		private CheckDownlink(int df) {
			downlink_freq = df;
		}

		/** Check the downlink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.downlink);
			sendQuery(mess, freq);
			mess.logQuery(freq);
			Integer f = freq.getFreqKhz();
			return (f == null || f != downlink_freq)
			      ? new StoreDownlink(downlink_freq)
			      : checkUplink();
		}
	}

	/** Phase to store the downlink frequency */
	private class StoreDownlink extends Phase<E6Property> {
		private final int downlink_freq;
		private StoreDownlink(int df) {
			downlink_freq = df;
		}

		/** Store the downlink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.downlink, downlink_freq);
			mess.logStore(freq);
			sendStore(mess, freq);
			return checkUplink();
		}
	}

	/** Create phase to check the uplink frequency */
	private Phase<E6Property> checkUplink() {
		Integer uf = tag_reader.getUplinkFreqKhz();
		return (uf != null) ? new CheckUplink(uf) : nextProtocol(null);
	}

	/** Phase to check the uplink frequency */
	private class CheckUplink extends Phase<E6Property> {
		private final int uplink_freq;
		private CheckUplink(int uf) {
			uplink_freq = uf;
		}

		/** Check the uplink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.uplink);
			sendQuery(mess, freq);
			mess.logQuery(freq);
			Integer f = freq.getFreqKhz();
			return (f == null || f != uplink_freq)
			      ? new StoreUplink(uplink_freq)
			      : nextProtocol(null);
		}
	}

	/** Phase to store the uplink frequency */
	private class StoreUplink extends Phase<E6Property> {
		private final int uplink_freq;
		private StoreUplink(int uf) {
			uplink_freq = uf;
		}

		/** Store the uplink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.uplink, uplink_freq);
			mess.logStore(freq);
			sendStore(mess, freq);
			return nextProtocol(null);
		}
	}

	/** Get the next protocol phase */
	private Phase<E6Property> nextProtocol(RFProtocol p_prot) {
		RFProtocol p = RFProtocol.next(p_prot);
		return (p != null) ? checkAtten(p) : checkLineLoss();
	}

	/** Get check attenuation phase (or later) */
	private Phase<E6Property> checkAtten(RFProtocol p) {
		Integer ad = getAttenDownlink(p);
		Integer au = getAttenUplink(p);
		return (ad != null && au != null)
		      ? new CheckAtten(p, ad, au)
		      : checkDataDetect(p);
	}

	/** Get downlink attenuation for one protocol */
	private Integer getAttenDownlink(RFProtocol p) {
		switch (p) {
		case SeGo:
			return tag_reader.getSeGoAttenDownlinkDb();
		case IAG:
			return tag_reader.getIAGAttenDownlinkDb();
		default:
			return null;
		}
	}

	/** Get uplink attenuation for one protocol */
	private Integer getAttenUplink(RFProtocol p) {
		switch (p) {
		case SeGo:
			return tag_reader.getSeGoAttenUplinkDb();
		case IAG:
			return tag_reader.getIAGAttenUplinkDb();
		default:
			return null;
		}
	}

	/** Phase to check RF attenuation for one protocol */
	private class CheckAtten extends Phase<E6Property> {
		private final RFProtocol protocol;
		private final int atten_down;
		private final int atten_up;
		private CheckAtten(RFProtocol p, int ad, int au) {
			protocol = p;
			atten_down = ad;
			atten_up = au;
		}

		/** Check RF attenuation */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFAttenProp atten = new RFAttenProp(protocol);
			try {
				sendQuery(mess, atten);
			}
			catch (ControllerException e) {
				// SUB_COMMAND_ERROR => protocol not supported
				return nextProtocol(protocol);
			}
			mess.logQuery(atten);
			Integer dl = atten.getDownlinkDb();
			Integer ul = atten.getUplinkDb();
			return (dl == null || dl != atten_down ||
			        ul == null || ul != atten_up)
			      ? new StoreAtten(protocol, atten_down, atten_up)
			      : checkDataDetect(protocol);
		}
	}

	/** Phase to store RF attenuation for one protocol */
	private class StoreAtten extends Phase<E6Property> {
		private final RFProtocol protocol;
		private final int atten_down;
		private final int atten_up;
		private StoreAtten(RFProtocol p, int ad, int au) {
			protocol = p;
			atten_down = ad;
			atten_up = au;
		}

		/** Store RF attenuation */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFAttenProp atten = new RFAttenProp(protocol,
				atten_down, atten_up);
			mess.logStore(atten);
			sendStore(mess, atten);
			return checkDataDetect(protocol);
		}
	}

	/** Get check data detect phase (or later) */
	private Phase<E6Property> checkDataDetect(RFProtocol p) {
		Integer dd = getDataDetect(p);
		return (dd != null)
		      ? new CheckDataDetect(p, dd)
		      : checkSeen(p);
	}

	/** Get data detect for one protocol */
	private Integer getDataDetect(RFProtocol p) {
		switch (p) {
		case SeGo:
			return tag_reader.getSeGoDataDetectDb();
		case IAG:
			return tag_reader.getIAGDataDetectDb();
		default:
			return null;
		}
	}

	/** Phase to check the data detect for one protocol */
	private class CheckDataDetect extends Phase<E6Property> {
		private final RFProtocol protocol;
		private final int data_detect;
		private CheckDataDetect(RFProtocol p, int dd) {
			protocol = p;
			data_detect = dd;
		}

		/** Check the data detect */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			DataDetectProp det = new DataDetectProp(protocol,
				data_detect);
			sendQuery(mess, det);
			mess.logQuery(det);
			Integer dd = det.getValue();
			return (dd == null || dd != data_detect)
			      ? new StoreDataDetect(protocol, data_detect)
			      : checkSeen(protocol);
		}
	}

	/** Phase to store data detect for one protocol */
	private class StoreDataDetect extends Phase<E6Property> {
		private final RFProtocol protocol;
		private final int data_detect;
		private StoreDataDetect(RFProtocol p, int dd) {
			protocol = p;
			data_detect = dd;
		}

		/** Store data detect */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			DataDetectProp det = new DataDetectProp(protocol,
				data_detect);
			mess.logStore(det);
			sendStore(mess, det);
			return checkSeen(protocol);
		}
	}

	/** Get check seen count phase (or later) */
	private Phase<E6Property> checkSeen(RFProtocol p) {
		Integer sc = getSeenCount(p);
		Integer uc = getUniqueCount(p);
		return (sc != null && uc != null)
		      ? new CheckSeen(p, sc, uc)
		      : nextProtocol(p);
	}

	/** Get seen count for one protocol */
	private Integer getSeenCount(RFProtocol p) {
		switch (p) {
		case SeGo:
			return tag_reader.getSeGoSeenCount();
		case IAG:
			return tag_reader.getIAGSeenCount();
		default:
			return null;
		}
	}

	/** Get unique count for one protocol */
	private Integer getUniqueCount(RFProtocol p) {
		switch (p) {
		case SeGo:
			return tag_reader.getSeGoUniqueCount();
		case IAG:
			return tag_reader.getIAGUniqueCount();
		default:
			return null;
		}
	}

	/** Phase to check seen count for one protocol */
	private class CheckSeen extends Phase<E6Property> {
		private final RFProtocol protocol;
		private final int seen_count;
		private final int unique_count;
		private CheckSeen(RFProtocol p, int sc, int uc) {
			protocol = p;
			seen_count = sc;
			unique_count = uc;
		}

		/** Check seen count */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			SeenCountProp seen = new SeenCountProp(protocol);
			sendQuery(mess, seen);
			mess.logQuery(seen);
			Integer sc = seen.getSeen();
			Integer uc = seen.getUnique();
			return (sc == null || sc != seen_count ||
			        uc == null || uc != unique_count)
			      ? new StoreSeen(protocol, seen_count,unique_count)
			      : nextProtocol(protocol);
		}
	}

	/** Phase to store seen and unique count for one protocol */
	private class StoreSeen extends Phase<E6Property> {
		private final RFProtocol protocol;
		private final int seen_count;
		private final int unique_count;
		private StoreSeen(RFProtocol p, int sc, int uc) {
			protocol = p;
			seen_count = sc;
			unique_count = uc;
		}

		/** Store seen and unique counts */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			SeenCountProp seen = new SeenCountProp(protocol,
				seen_count, unique_count);
			mess.logStore(seen);
			sendStore(mess, seen);
			return nextProtocol(protocol);
		}
	}

	/** Create phase to check the line loss */
	private Phase<E6Property> checkLineLoss() {
		Integer ll = tag_reader.getLineLossDb();
		return (ll != null) ? new CheckLineLoss(ll) : checkSyncMode();
	}

	/** Phase to check the line loss */
	private class CheckLineLoss extends Phase<E6Property> {
		private final int line_loss;
		private CheckLineLoss(int ll) {
			line_loss = ll;
		}

		/** Check the line loss */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			LineLossProp loss = new LineLossProp();
			sendQuery(mess, loss);
			mess.logQuery(loss);
			Integer ll = loss.getValue();
			return (ll == null || ll != line_loss)
			      ? new StoreLineLoss(line_loss)
			      : checkSyncMode();
		}
	}

	/** Phase to store line loss */
	private class StoreLineLoss extends Phase<E6Property> {
		private final int line_loss;
		private StoreLineLoss(int ll) {
			line_loss = ll;
		}

		/** Store line loss */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			LineLossProp loss = new LineLossProp(line_loss);
			mess.logStore(loss);
			sendStore(mess, loss);
			return checkSyncMode();
		}
	}

	/** Create phase to check sync mode / slave select count */
	private Phase<E6Property> checkSyncMode() {
		TagReaderSyncMode sm = tag_reader.getSyncMode();
		Integer sc = tag_reader.getSlaveSelectCount();
		return (sm != null && sc != null)
		      ? new CheckSyncMode(sm, sc)
		      : lastPhase();
	}

	/** Phase to check sync mode / slave select count */
	private class CheckSyncMode extends Phase<E6Property> {
		private final TagReaderSyncMode sync_mode;
		private final int slave_select;
		private CheckSyncMode(TagReaderSyncMode sm, int sc) {
			sync_mode = sm;
			slave_select = sc;
		}

		/** Query sync mode / slave select count */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			MasterSlaveProp mstr = new MasterSlaveProp();
			sendQuery(mess, mstr);
			mess.logQuery(mstr);
			TagReaderSyncMode mode = mstr.getMode();
			Integer slave = mstr.getSlaveSelectCount();
			return (mode == null || mode != sync_mode ||
			        slave == null || slave != slave_select)
			      ? new StoreSyncMode(sync_mode, slave_select)
			      : lastPhase();
		}
	}

	/** Phase to store sync mode / slave select count */
	private class StoreSyncMode extends Phase<E6Property> {
		private final TagReaderSyncMode sync_mode;
		private final int slave_select;
		private StoreSyncMode(TagReaderSyncMode sm, int sc) {
			sync_mode = sm;
			slave_select = sc;
		}

		/** Store sync mode / slave select count */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			MasterSlaveProp mstr = new MasterSlaveProp(sync_mode,
				slave_select);
			mess.logStore(mstr);
			sendStore(mess, mstr);
			return lastPhase();
		}
	}

	/** Get the last phase */
	private Phase<E6Property> lastPhase() {
		return (stop) ? new StoreMode() : null;
	}

	/** Phase to store the mode */
	private class StoreMode extends Phase<E6Property> {

		/** Store the mode */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			ModeProp mode = new ModeProp(ModeProp.Mode.read_write);
			mess.logStore(mode);
			sendStore(mess, mode);
			return null;
		}
	}
}
