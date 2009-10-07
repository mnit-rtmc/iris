/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.smartsensor;

import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DownloadRequestException;

/**
 * Operation to get binned samples from a SmartSensor device
 *
 * @author Douglas Lau
 */
public class OpQuerySamples extends OpSS105 {

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

	/** Create a new "query binned samples" operation */
	public OpQuerySamples(ControllerImpl c, Completer comp) {
		super(DATA_30_SEC, c);
		completer = comp;
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
		completer.up();
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
			SS105_LOG.log(controller.getName() + ": " + bs);
			if(stamp.before(oldest) || stamp.after(newest)) {
				SS105_LOG.log("BAD TIMESTAMP: " +
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
