/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.util.LinkedList;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.LaneUseGraphic;
import us.mn.state.dot.tms.LaneUseGraphicHelper;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

/**
 * Operation to send new indicaitons to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSIndications extends OpLCS {

	/** Calculate the X position of a graphic */
	static protected int calculateGraphicX(DMS dms, Graphic g) {
		return 1 + (dms.getWidthPixels() - g.getWidth()) / 2;
	}

	/** Calculate the Y position of a graphic */
	static protected int caluclateGraphicY(DMS dms, Graphic g) {
		return 1 + (dms.getHeightPixels() - g.getHeight()) / 2;
	}

	/** Indications to send */
	protected final Integer[] indications;

	/** User who sent the indications */
	protected final User user;

	/** List of operations to send indications to DMS in LCS array */
	protected final LinkedList<OpDMS> ops = new LinkedList<OpDMS>();

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LCSArrayImpl l, Integer[] ind, User u) {
		super(DEVICE_DATA, l);
		indications = ind;
		user = u;
		createOperations();
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		if(ops.isEmpty())
			return null;
		else
			return new StartOperations();
	}

	/** Phase to start DMS operations */
	protected class StartOperations extends Phase {

		/** Start all DMS operations */
		protected Phase poll(AddressedMessage mess) {
			for(OpDMS op: ops)
				op.start();
			return new WaitForCompletion();
		}
	}

	/** Phase to wait for operations to complete */
	protected class WaitForCompletion extends Phase {

		/** Time to stop waiting for completion (20 seconds) */
		protected final long expire = System.currentTimeMillis() + 
			20 * 1000;

		/** Wait for operations to complete */
		protected Phase poll(AddressedMessage mess) {
			try {
				Thread.sleep(200);
			}
			catch(InterruptedException e) {
				// Ignore
			}
			OpDMS op = ops.peekFirst();
			if(op == null)
				return null;
			if(op.isDone())
				ops.removeFirst();
			else {
				if(System.currentTimeMillis() > expire) {
					System.err.println(lcs_array.getName() +
						": LCS timeout expired -- " +
						"giving up");
					success = false;
					return null;
				}
			}
			return this;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success)
			lcs_array.setIndicationsCurrent(indications, user);
		super.cleanup();
	}

	/** Create operations to send new indications to an LCS array */
	protected void createOperations() {
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		if(lcss.length != indications.length) {
			System.err.println("createOperations: array invalid");
			return;
		}
		for(int i = 0; i < lcss.length; i++) {
			DMS dms = DMSHelper.lookup(lcss[i].getName());
			if(dms instanceof DMSImpl) {
				OpDMS op = createGraphicOperation((DMSImpl)dms,
					indications[i]);
				if(op != null) {
					ops.add(op);
					continue;
				}
			}
			System.err.println("createOperations: aborted");
			return;
		}
	}

	/** Create an operation to set an indication on a DMS */
	protected OpDMS createGraphicOperation(DMSImpl dms, int ind) {
		String ms = createIndicationMulti(dms, ind);
		if(ms != null) {
			SignMessage sm = dms.createMessage(ms);
			if(sm != null)
				return NtcipPoller.createOperation(dms,sm,user);
		}
		return null;
	}

	/** Create a MULTI string for a lane use indication */
	protected String createIndicationMulti(DMS dms, int ind) {
		MultiString ms = new MultiString();
		for(LaneUseGraphic lug:
			LaneUseGraphicHelper.getIndicationGraphics(ind))
		{
			Graphic g = lug.getGraphic();
			int foreground = lug.getForeground();
			int red = (foreground >> 16) & 0xFF;
			int green = (foreground >> 8) & 0xFF;
			int blue = foreground & 0xFF;
			int x = calculateGraphicX(dms, g);
			int y = caluclateGraphicY(dms, g);
			if(x > 0 && y > 0) {
				if(ms.toString().length() > 0)
					ms.addPage();
				ms.setColorForeground(red, green, blue);
				ms.addGraphic(g.getGNumber(), x, y);
			}
		}
		String m = ms.toString();
		if(m.length() > 0 ||
		   LaneUseIndication.fromOrdinal(ind) == LaneUseIndication.DARK)
			return m;
		else
			return null;
	}
}
