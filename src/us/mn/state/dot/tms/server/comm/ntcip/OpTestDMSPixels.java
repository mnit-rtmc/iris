/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Flags;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;

/**
 * This operation tests the pixel status of a DMS.
 *
 * @author Douglas Lau
 */
public class OpTestDMSPixels extends OpDMS {

	/** Flag to indicate whether a pixel test should be performed */
	private final boolean perform_test;

	/** Pixel test activation */
	private final ASN1Enum<PixelTestActivation> activation = new ASN1Enum<
		PixelTestActivation>(PixelTestActivation.class,
		pixelTestActivation.node);

	/** Stuck ON bitmap */
	private final BitmapGraphic stuck_on;

	/** Stuck OFF bitmap */
	private final BitmapGraphic stuck_off;

	/** Number of rows in pixel failure table */
	private final ASN1Integer total_rows =
		pixelFailureTableNumRows.makeInt();

	/** Number of rows in pixel failure table found by pixel testing */
	private final ASN1Integer test_rows = dmsPixelFailureTestRows.makeInt();

	/** Number of rows in pixel failure table found by message display */
	private final ASN1Integer message_rows =
		dmsPixelFailureMessageRows.makeInt();

	/** Create a new test DMS pixel operation */
	public OpTestDMSPixels(DMSImpl d, boolean p) {
		super(PriorityLevel.DEVICE_DATA, d);
		perform_test = p;
		SignConfig sc = d.getSignConfig();
		int w = (sc != null) ? sc.getPixelWidth() : 0;
		int h = (sc != null) ? sc.getPixelHeight() : 0;
		stuck_on = new BitmapGraphic(w, h);
		stuck_off = new BitmapGraphic(w, h);
	}

	/** Create the second phase of the operation */
	@Override 
	protected Phase phaseTwo() {
		if (perform_test)
			return new QueryTestStatus();
		else
			return new QueryRowCount();
	}

	/** Phase to query the status of pixel test activation */
	protected class QueryTestStatus extends Phase {

		/** Query the status of pixel test activation */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(activation);
			mess.queryProps();
			logQuery(activation);
			if (activation.getEnum() == PixelTestActivation.noTest)
				return new ActivatePixelTest();
			else
				return new CheckTestCompletion();
		}
	}

	/** Phase to activate the pixel test */
	protected class ActivatePixelTest extends Phase {

		/** Activate the pixel test */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			activation.setEnum(PixelTestActivation.test);
			mess.add(activation);
			logStore(activation);
			mess.storeProps();
			return new CheckTestCompletion();
		}
	}

	/** Phase to check for test completion */
	protected class CheckTestCompletion extends Phase {

		/** Time to stop checking if the test has completed */
		private final long expire = TimeSteward.currentTimeMillis() +
		       1000*SystemAttrEnum.DMS_PIXEL_TEST_TIMEOUT_SECS.getInt();

		/** Check for test completion */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(activation);
			mess.queryProps();
			logQuery(activation);
			if (activation.getEnum() == PixelTestActivation.noTest)
				return new QueryRowCount();
			if(TimeSteward.currentTimeMillis() > expire) {
				logError("pixel test timeout expired -- " +
					"giving up");
				return new QueryRowCount();
			} else
				return this;
		}
	}

	/** Phase to query the row count in the pixel failure table */
	protected class QueryRowCount extends Phase {

		/** Query the row count in pixel failure table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(total_rows);
			mess.queryProps();
			logQuery(total_rows);
			if (total_rows.getInteger() > 0)
				return new QueryTestAndMessageRows();
			else
				return null;
		}
	}

	/** Phase to query (v2) test/message rows in pixel failure table */
	protected class QueryTestAndMessageRows extends Phase {

		/** Query test/message rows in pixel failure table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(test_rows);
			mess.add(message_rows);
			try {
				mess.queryProps();
				logQuery(test_rows);
				logQuery(message_rows);
			}
			catch (NoSuchName e) {
				// Must be 1203v1 only, so try reading the
				// total row count from each table
				int n_rows = total_rows.getInteger();
				test_rows.setInteger(n_rows);
				message_rows.setInteger(n_rows);
			}
			if (test_rows.getInteger() > 0) {
				return new QueryRows(PixelFailureDetectionType.
					pixelTest);
			} else if (message_rows.getInteger() > 0) {
				return new QueryRows(PixelFailureDetectionType.
					messageDisplay);
			} else
				return null;
		}
	}

	/** Phase to query rows in the pixel failure table */
	protected class QueryRows extends Phase {

		/** Detection type */
		private final PixelFailureDetectionType detectionType;

		/** Number of rows to query */
		private final int n_rows;

		/** Row to query */
		private int row = 1;

		/** Create a new phase to query the rows */
		public QueryRows(PixelFailureDetectionType dt) {
			detectionType = dt;
			if (isPixelTest())
				n_rows = test_rows.getInteger();
			else
				n_rows = message_rows.getInteger();
		}

		/** Is this phase querying pixel test rows? */
		private boolean isPixelTest() {
			return detectionType ==
			       PixelFailureDetectionType.pixelTest;
		}

		/** Query one row in the pixel failure table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer x_loc = pixelFailureXLocation.makeInt(
				detectionType.ordinal(), row);
			ASN1Integer y_loc = pixelFailureYLocation.makeInt(
				detectionType.ordinal(), row);
			ASN1Flags<PixelFailureStatus> status = new ASN1Flags<
				PixelFailureStatus>(PixelFailureStatus.class,
				pixelFailureStatus.node,detectionType.ordinal(),
				row);
			mess.add(x_loc);
			mess.add(y_loc);
			mess.add(status);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// We've gone past the end of the table for
				// this detection type.  Must be a v1 sign.
				return nextTablePhase();
			}
			logQuery(x_loc);
			logQuery(y_loc);
			logQuery(status);
			int x = x_loc.getInteger() - 1;
			int y = y_loc.getInteger() - 1;
			if (PixelFailureStatus.isStuckOn(status.getInteger()))
				setStuckOn(x, y);
			if (PixelFailureStatus.isStuckOff(status.getInteger()))
				setStuckOff(x, y);
			row++;
			if (row <= n_rows)
				return this;
			else
				return nextTablePhase();
		}

		/** Get the next table phase */
		private Phase nextTablePhase() {
			if (isPixelTest() && message_rows.getInteger() > 0) {
				return new QueryRows(PixelFailureDetectionType.
					messageDisplay);
			} else
				return null;
		}
	}

	/** Set a pixel to "stuck on" status */
	private void setStuckOn(int x, int y) {
		try {
			stuck_on.setPixel(x, y, DmsColor.AMBER);
		}
		catch (IndexOutOfBoundsException e) {
			// Ignore; configuration has not been read yet
		}
	}

	/** Set a pixel to "stuck off" status */
	private void setStuckOff(int x, int y) {
		try {
			stuck_off.setPixel(x, y, DmsColor.AMBER);
		}
		catch (IndexOutOfBoundsException e) {
			// Ignore; configuration has not been read yet
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			String[] status = new String[2];
			status[DMS.STUCK_OFF_BITMAP] =
				stuck_off.getEncodedPixels();
			status[DMS.STUCK_ON_BITMAP] =
				stuck_on.getEncodedPixels();
			dms.setPixelStatus(status);
		}
		super.cleanup();
	}
}
