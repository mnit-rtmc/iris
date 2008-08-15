/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.smartsensor;

import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.DebugLog;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ControllerOperation;
import us.mn.state.dot.tms.comm.DownloadRequestException;

/**
 * Operation to get binned samples from a SmartSensor device
 *
 * @author Douglas Lau
 */
public class GetBinnedSamples extends ControllerOperation {

	/** Sample debug log */
	static public final DebugLog SAMPLE_LOG = new DebugLog("samples");

	/** 30-Second interval completer */
	protected final Completer completer;

	/** Time stamp of sample data */
	protected final Calendar stamp = Calendar.getInstance();

	/** Oldest time stamp to accept from controller */
	protected final Calendar oldest = Calendar.getInstance();

	/** Newest timestamp to accept from controller */
	protected final Calendar newest = Calendar.getInstance();

	/** Volume data for each detector */
	protected int[] volume = new int[8];

	/** Scan data for each detector */
	protected int[] scans = new int[8];

	/** Speed data for each detector */
	protected int[] speed = new int[8];

	/** Create a new "get binned samples" operation */
	public GetBinnedSamples(ControllerImpl c, Completer comp) {
		super(DATA_30_SEC, c);
		completer = comp;
		completer.up();
		long s = comp.getStamp().getTimeInMillis();
		stamp.setTimeInMillis(s);
		oldest.setTimeInMillis(s);
		oldest.add(Calendar.HOUR, -4);
		newest.setTimeInMillis(s);
		newest.add(Calendar.MINUTE, 5);
		for(int i = 0; i < 8; i++) {
			volume[i] = Constants.MISSING_DATA;
			scans[i] = Constants.MISSING_DATA;
			speed[i] = Constants.MISSING_DATA;
		}
	}

	/** Begin the operation */
	public void begin() {
		phase = new GetCurrentSamples();
	}

	/** Phase to get the most recent binned samples */
	protected class GetCurrentSamples extends Phase {

		/** Get the most recent binned samples */
		protected Phase poll(AddressedMessage mess) throws IOException {
			BinnedSampleRequest bs = new BinnedSampleRequest();
			mess.add(bs);
			mess.getRequest();
			stamp.setTimeInMillis(bs.timestamp.getTime());
			volume = bs.getVolume();
			scans = bs.getScans();
			speed = bs.getSpeed();
			SAMPLE_LOG.log(bs.toString());
			if(stamp.before(oldest) || stamp.after(newest)) {
				SAMPLE_LOG.log("BAD TIMESTAMP: " +
					stamp.getTime() + " for " + controller);
				throw new DownloadRequestException(
					controller.toString());
			}
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success) {
			controller.storeData30Second(stamp, 1, volume, scans,
				speed);
		}
		completer.down();
		super.cleanup();
	}
}
