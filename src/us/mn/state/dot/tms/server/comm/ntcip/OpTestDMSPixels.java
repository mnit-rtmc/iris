/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * This operation tests the pixel status of a DMS.
 *
 * @author Douglas Lau
 */
public class OpTestDMSPixels extends OpDMS {

	/** Flag to indicate whether a pixel test should be performed */
	protected final boolean perform_test;

	/** Stuck ON bitmap */
	protected final BitmapGraphic stuck_on;

	/** Stuck OFF bitmap */
	protected final BitmapGraphic stuck_off;

	/** Number of rows in pixel failure table */
	protected final PixelFailureTableNumRows total_rows =
		new PixelFailureTableNumRows();

	/** Number of rows in pixel failure table found by pixel testing */
	protected final DmsPixelFailureTestRows test_rows =
		new DmsPixelFailureTestRows();

	/** Number of rows in pixel failure table found by message display */
	protected final DmsPixelFailureMessageRows message_rows =
		new DmsPixelFailureMessageRows();

	/** Create a new test DMS pixel operation */
	public OpTestDMSPixels(DMSImpl d, boolean p) {
		super(PriorityLevel.DEVICE_DATA, d);
		perform_test = p;
		Integer w = d.getWidthPixels();
		Integer h = d.getHeightPixels();
		if(w == null)
			w = 0;
		if(h == null)
			h = 0;
		stuck_on = new BitmapGraphic(w, h);
		stuck_off = new BitmapGraphic(w, h);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		if(perform_test)
			return new QueryTestStatus();
		else
			return new QueryRowCount();
	}

	/** Phase to query the status of pixel test activation */
	protected class QueryTestStatus extends Phase {

		/** Query the status of pixel test activation */
		protected Phase poll(CommMessage mess) throws IOException {
			PixelTestActivation test = new PixelTestActivation();
			mess.add(test);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + test);
			if(test.getEnum() == PixelTestActivation.Enum.noTest)
				return new ActivatePixelTest();
			else
				return new CheckTestCompletion();
		}
	}

	/** Phase to activate the pixel test */
	protected class ActivatePixelTest extends Phase {

		/** Activate the pixel test */
		protected Phase poll(CommMessage mess) throws IOException {
			PixelTestActivation test = new PixelTestActivation();
			test.setEnum(PixelTestActivation.Enum.test);
			mess.add(test);
			DMS_LOG.log(dms.getName() + ":= " + test);
			mess.storeProps();
			return new CheckTestCompletion();
		}
	}

	/** Phase to check for test completion */
	protected class CheckTestCompletion extends Phase {

		/** Pixel test activation */
		protected final PixelTestActivation test =
			new PixelTestActivation();

		/** Time to stop checking if the test has completed */
		protected final long expire = TimeSteward.currentTimeMillis() +
		       1000*SystemAttrEnum.DMS_PIXEL_TEST_TIMEOUT_SECS.getInt();

		/** Check for test completion */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(test);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + test);
			if(test.getEnum() == PixelTestActivation.Enum.noTest)
				return new QueryRowCount();
			if(TimeSteward.currentTimeMillis() > expire) {
				DMS_LOG.log(dms.getName() + ": pixel test " +
					"timeout expired -- giving up");
				return new QueryRowCount();
			} else
				return this;
		}
	}

	/** Phase to query the row count in the pixel failure table */
	protected class QueryRowCount extends Phase {

		/** Query the row count in pixel failure table */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(total_rows);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + total_rows);
			if(total_rows.getInteger() > 0)
				return new QueryTestAndMessageRows();
			else
				return null;
		}
	}

	/** Phase to query (v2) test/message rows in pixel failure table */
	protected class QueryTestAndMessageRows extends Phase {

		/** Query test/message rows in pixel failure table */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(test_rows);
			mess.add(message_rows);
			try {
				mess.queryProps();
				DMS_LOG.log(dms.getName() + ": " + test_rows);
				DMS_LOG.log(dms.getName() + ": " +message_rows);
			}
			catch(SNMP.Message.NoSuchName e) {
				// Must be 1203v1 only, so assume all the
				// rows are pixelTest and hope for the best
				test_rows.setInteger(total_rows.getInteger());
				message_rows.setInteger(0);
			}
			if(test_rows.getInteger() > 0) {
				return new QueryRows(PixelFailureDetectionType.
					Enum.pixelTest);
			} else if(message_rows.getInteger() > 0) {
				return new QueryRows(PixelFailureDetectionType.
					Enum.messageDisplay);
			} else
				return null;
		}
	}

	/** Phase to query rows in the pixel failure table */
	protected class QueryRows extends Phase {

		/** Detection type */
		protected final PixelFailureDetectionType.Enum detectionType;

		/** Number of rows to query */
		protected final int n_rows;

		/** Row to query */
		protected int row = 1;

		/** Create a new phase to query the rows */
		public QueryRows(PixelFailureDetectionType.Enum dt) {
			detectionType = dt;
			if(isPixelTest())
				n_rows = test_rows.getInteger();
			else
				n_rows = message_rows.getInteger();
		}

		/** Is this phase querying pixel test rows? */
		protected boolean isPixelTest() {
			return detectionType ==
			       PixelFailureDetectionType.Enum.pixelTest;
		}

		/** Query one row in the pixel failure table */
		protected Phase poll(CommMessage mess) throws IOException {
			PixelFailureXLocation x_loc = new PixelFailureXLocation(
				detectionType, row);
			PixelFailureYLocation y_loc = new PixelFailureYLocation(
				detectionType, row);
			PixelFailureStatus status = new PixelFailureStatus(
				detectionType, row);
			mess.add(x_loc);
			mess.add(y_loc);
			mess.add(status);
			try {
				mess.queryProps();
			}
			catch(SNMP.Message.NoSuchName e) {
				// Okay... there is no pixel failure table for
				// this detection type.
				DMS_LOG.log(dms.getName() +
					" BAD PIXEL TABLE: " + detectionType);
				return null;
			}
			DMS_LOG.log(dms.getName() + ": " + x_loc);
			DMS_LOG.log(dms.getName() + ": " + y_loc);
			DMS_LOG.log(dms.getName() + ": " + status);
			int x = x_loc.getInteger() - 1;
			int y = y_loc.getInteger() - 1;
			try {
				if(status.isStuckOn())
					stuck_on.setPixel(x, y, DmsColor.AMBER);
				else
					stuck_off.setPixel(x, y,DmsColor.AMBER);
			}
			catch(IndexOutOfBoundsException e) {
				// Ignore; configuration has not been read yet
			}
			row++;
			if(row <= n_rows)
				return this;
			else if(isPixelTest() && message_rows.getInteger() > 0){
				return new QueryRows(PixelFailureDetectionType.
					Enum.messageDisplay);
			} else
				return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success) {
			String[] status = new String[2];
			status[DMS.STUCK_OFF_BITMAP] =
				Base64.encode(stuck_off.getPixels());
			status[DMS.STUCK_ON_BITMAP] =
				Base64.encode(stuck_on.getPixels());
			dms.setPixelStatus(status);
		}
		super.cleanup();
	}
}
