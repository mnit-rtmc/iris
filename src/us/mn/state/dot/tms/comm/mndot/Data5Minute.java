/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.mndot;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.Controller170Impl;
import us.mn.state.dot.tms.RampMeterImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to query 5-minute sample data + meter green counts
 *
 * @author Douglas Lau
 */
public class Data5Minute extends IntervalData {

	/** Maximum number of records to read with "BAD TIMESTAMP" errors */
	static protected final int MAX_BAD_RECORDS = 5;

	/** 170 controller */
	protected final Controller170Impl c170;

	/** Ramp meter being queried */
	protected final RampMeterImpl meter1;

	/** Ramp meter being queried */
	protected final RampMeterImpl meter2;

	/** 5-minute completer */
	protected final Completer completer;

	/** Time stamp */
	protected Calendar stamp = Calendar.getInstance();

	/** Oldest time stamp to accept from controller */
	protected final Calendar oldest = Calendar.getInstance();

	/** Newest timestamp to accept from controller */
	protected final Calendar newest = Calendar.getInstance();

	/** Count of records with "BAD TIMESTAMP" errors */
	protected int n_bad = 0;

	/** Create a new 5-minute data operation */
	public Data5Minute(Controller170Impl c, Completer comp) {
		super(DATA_5_MIN, c);
		c170 = c;
		meter1 = Controller170Operation.lookupMeter1(c170);
		meter2 = Controller170Operation.lookupMeter2(c170);
		completer = comp;
		completer.up();
		long s = System.currentTimeMillis();
		stamp.setTimeInMillis(s);
		oldest.setTimeInMillis(s);
		oldest.add(Calendar.DATE, -1);
		newest.setTimeInMillis(s);
		newest.add(Calendar.MINUTE, 4);
		newest.add(Calendar.SECOND, 20);
	}

	/** Begin the operation */
	public void begin() {
		phase = new GetNextRecord();
	}

	/** Phase to get the next sample data record */
	protected class GetNextRecord extends Phase {

		/** Binned data record */
		protected byte[] rec;

		/** Try to get and delete the next record */
		protected int tryNextRecord(AddressedMessage mess)
			throws IOException
		{
			BinnedDataRequest bin = new BinnedDataRequest();
			mess.add(bin);
			mess.getRequest();
			stamp = bin.getStamp();
			stamp.add(Calendar.MINUTE, -5);
			rec = bin.getRecord();
			// Delete the record from the controller
			mess.setRequest();
			return bin.getRecordCount();
		}

		/** Test if the timestamp is out of the valid range */
		protected boolean isStampBad() {
			if(stamp.before(oldest) || stamp.after(newest)) {
				System.err.println("BAD TIMESTAMP: " +
					stamp.getTime() + " for " + controller +
					" @ " + new Date());
				return true;
			} else
				return false;
		}

		/** Collect 5-minute data from the controller */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int recs = 0;
			try {
				recs = tryNextRecord(mess);
				if(isStampBad()) {
					if(++n_bad > MAX_BAD_RECORDS)
						return null;
					else
						return this;
				}
			}
			catch(ControllerException.NoSampleData e) {
				rec = new byte[75];
				mess.add(new MemoryRequest(
					Address.DATA_BUFFER_5_MINUTE, rec));
				mess.getRequest();
			}
			processData(rec);
			controller.storeData5Minute(stamp, volume, scans);
			updateGreenCount(meter1,
				rec[Address.OFF_GREEN_METER_1] & 0xFF);
			updateGreenCount(meter2,
				rec[Address.OFF_GREEN_METER_2] & 0xFF);
			if(recs > 0 && Calendar.getInstance().before(newest))
				return this;
			else
				return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		completer.down();
		super.cleanup();
	}

	/** Update meter with the most recent 5-minute green count */
	protected void updateGreenCount(RampMeterImpl meter, int g)
		throws IOException
	{
		if(meter != null)
			meter.updateGreenCount5(stamp, g);
	}
}
