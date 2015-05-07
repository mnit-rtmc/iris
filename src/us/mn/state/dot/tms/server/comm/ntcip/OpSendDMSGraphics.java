/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to send a set of graphicss to a DMS controller.
 *
 * @author Douglas Lau
 */
public class OpSendDMSGraphics extends OpDMS {

	/** Color scheme supported */
	private final DmsColorScheme color_scheme =
		new DmsColorScheme();

	/** Number of graphics supported */
	private final DmsGraphicMaxEntries max_graphics =
		new DmsGraphicMaxEntries();

	/** Number of graphics defined in graphic table */
	private final DmsGraphicNumEntries num_graphics =
		new DmsGraphicNumEntries();

	/** Maximum size of a graphic */
	private final DmsGraphicMaxSize max_size = new DmsGraphicMaxSize();

	/** Available memory for storing graphics */
	private final AvailableGraphicMemory available_memory =
		new AvailableGraphicMemory();

	/** Size of graphic blocks (in bytes) */
	private final DmsGraphicBlockSize block_size =
		new DmsGraphicBlockSize();

	/** Mapping of graphic numbers to indices (row in table) */
	private final TreeMap<Integer, Integer> num_2_row =
		new TreeMap<Integer, Integer>();

	/** Set of open rows in the graphic table */
	private final TreeSet<Integer> open_rows = new TreeSet<Integer>();

	/** Iterator of graphics to be sent to the sign */
	private Iterator<Graphic> graphic_iterator;

	/** Current graphic */
	private Graphic graphic;

	/** Graphic row for graphic table */
	private int row;

	/** Create a new operation to send graphics to a DMS */
	public OpSendDMSGraphics(DMSImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryGraphicsConfiguration();
	}

	/** Phase to query the graphics configuration */
	private class QueryGraphicsConfiguration extends Phase {

		/** Query the graphics configuration */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(color_scheme);
			mess.add(max_graphics);
			mess.add(num_graphics);
			mess.add(max_size);
			mess.add(available_memory);
			mess.add(block_size);
			try {
				mess.queryProps();
			}
			catch (SNMP.Message.NoSuchName e) {
				// Must be 1203v1 only (no graphics) ...
				return null;
			}
			logQuery(color_scheme);
			logQuery(max_graphics);
			logQuery(num_graphics);
			logQuery(max_size);
			logQuery(available_memory);
			logQuery(block_size);
			for (row = 1; row <= max_graphics.getInteger(); row++)
				open_rows.add(row);
			row = 1;
			lookupGraphics();
			return new QueryGraphicNumbers();
		}
	}

	/** Lookup all graphics which have the proper color scheme */
	private void lookupGraphics() {
		LinkedList<Graphic> graphics = new LinkedList<Graphic>();
		Iterator<Graphic> it = GraphicHelper.iterator();
		while (it.hasNext()) {
			Graphic g = it.next();
			Integer g_num = g.getGNumber();
			if (shouldSend(g_num, g)) {
				graphics.add(g);
				num_2_row.put(g_num, null);
			}
		}
		graphic_iterator = graphics.iterator();
	}

	/** Test if we should send the given graphic */
	private boolean shouldSend(Integer g_num, Graphic g) {
		Integer w = dms.getWidthPixels();
		Integer h = dms.getHeightPixels();
		int bpp = color_scheme.getEnum().getBpp();
		return (g_num != null && w != null && h != null) &&
		       (g.getWidth() <= w) && (g.getHeight() <= h) &&
		       (g.getBpp() == 1 || g.getBpp() == bpp);
	}

	/** Phase to query all graphic numbers */
	private class QueryGraphicNumbers extends Phase {

		/** Query the graphic number for one graphic */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicNumber number = new DmsGraphicNumber(row);
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			mess.add(number);
			mess.add(status);
			mess.queryProps();
			logQuery(number);
			logQuery(status);
			Integer g_num = number.getInteger();
			if (num_2_row.containsKey(g_num)) {
				num_2_row.put(g_num, row);
				open_rows.remove(row);
			}
			if (row < max_graphics.getInteger()) {
				row++;
				return this;
			} else
				return populateNum2Row();
		}
	}

	/** Populate the num_2_row mapping */
	private Phase populateNum2Row() {
		for (Integer g_num: num_2_row.keySet()) {
			if (num_2_row.get(g_num) == null) {
				Integer r = open_rows.pollLast();
				if (r != null)
					num_2_row.put(g_num, r);
				else
					num_2_row.remove(g_num);
			}
		}
		return nextGraphicPhase();
	}

	/** Get the first phase of the next graphic */
	private Phase nextGraphicPhase() {
		while (graphic_iterator.hasNext()) {
			graphic = graphic_iterator.next();
			Integer g_num = graphic.getGNumber();
			if (num_2_row.containsKey(g_num)) {
				row = num_2_row.get(g_num);
				return new VerifyGraphic();
			}
			logError("Skipping graphic " + g_num);
		}
		return null;
	}

	/** Phase to verify a graphic */
	private class VerifyGraphic extends Phase {

		/** Verify a graphic */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicID gid = new DmsGraphicID(row);
			mess.add(gid);
			mess.queryProps();
			logQuery(gid);
			if (isIDCorrect(gid.getInteger())) {
				logError("Graphic valid");
				return nextGraphicPhase();
			} else
				return new QueryInitialStatus();
		}
	}

	/** Compare the graphic ID */
	private boolean isIDCorrect(int g) throws IOException {
		GraphicInfoList gil = new GraphicInfoList(graphic);
		return g == gil.getCrcSwapped();
	}

	/** Phase to query the initial graphic status */
	private class QueryInitialStatus extends Phase {

		/** Query the initial graphic status */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			switch(status.getEnum()) {
			case notUsed:
				return new RequestModify();
			case modifying:
			case calculatingID:
			case readyForUse:
				return new InvalidateGraphic();
			default:
				logError("skipping graphic #" + row);
				return nextGraphicPhase();
			}
		}
	}

	/** Invalidate the graphic */
	private class InvalidateGraphic extends Phase {

		/** Invalidate the graphic entry in the graphic table */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			status.setEnum(DmsGraphicStatus.Enum.notUsedReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new RequestModify();
		}
	}

	/** Phase to request modifying the graphic */
	private class RequestModify extends Phase {

		/** Set the graphic status to modifyReq */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			status.setEnum(DmsGraphicStatus.Enum.modifyReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyStatusModifying();
		}
	}

	/** Phase to verify the graphic status is modifying */
	private class VerifyStatusModifying extends Phase {

		/** Verify the graphic status is modifying */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() !=DmsGraphicStatus.Enum.modifying){
				logError("graphic send aborted");
				return nextGraphicPhase();
			}
			return new CreateGraphic();
		}
	}

	/** Create the graphic */
	private class CreateGraphic extends Phase {

		/** Create a new graphic in the graphic table */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicNumber number = new DmsGraphicNumber(row);
			DmsGraphicName name = new DmsGraphicName(row);
			DmsGraphicHeight height = new DmsGraphicHeight(row);
			DmsGraphicWidth width = new DmsGraphicWidth(row);
			DmsGraphicType type = new DmsGraphicType(row);
			DmsGraphicTransparentEnabled trans_enabled =
				new DmsGraphicTransparentEnabled(row);
			DmsGraphicTransparentColor trans_color =
				new DmsGraphicTransparentColor(row);
			number.setInteger(graphic.getGNumber());
			name.setString(graphic.getName());
			height.setInteger(graphic.getHeight());
			width.setInteger(graphic.getWidth());
			type.setEnum(DmsColorScheme.Enum.fromBpp(
				graphic.getBpp()));
			trans_enabled.setInteger(1);
			if (graphic.getBpp() == 24) {
				trans_color.setOctetString(
					new byte[] { 0, 0, 0 });
			} else
				trans_color.setOctetString(new byte[] { 0 });
			mess.add(number);
			mess.add(name);
			mess.add(height);
			mess.add(width);
			mess.add(type);
			mess.add(trans_enabled);
			mess.add(trans_color);
			logStore(number);
			logStore(name);
			logStore(height);
			logStore(width);
			logStore(type);
			logStore(trans_enabled);
			logStore(trans_color);
			mess.storeProps();
			return new SendGraphicBlock();
		}
	}

	/** Phase to send a block of a graphic */
	private class SendGraphicBlock extends Phase {

		/** Graphic bitmap */
		private final byte[] bitmap;

		/** Current block */
		private int block;

		/** Create a phase to send graphic blocks */
		private SendGraphicBlock() throws IOException {
			bitmap = Base64.decode(graphic.getPixels());
			block = 1;
		}

		/** Send a graphic block */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicBlockBitmap block_bitmap =
				new DmsGraphicBlockBitmap(row, block);
			block_bitmap.setOctetString(createBlock());
			mess.add(block_bitmap);
			logStore(block_bitmap);
			mess.storeProps();
			if (block * block_size.getInteger() < bitmap.length) {
				block++;
				if (block % 20 == 0 && !controller.isFailed())
					setSuccess(true);
				return this;
			} else
				return new ValidateGraphic();
		}

		/** Create a graphic block */
		private byte[] createBlock() {
			int bsize = block_size.getInteger();
			int pos = (block - 1) * bsize;
			int blen = Math.min(bsize, bitmap.length - pos);
			byte[] bdata = new byte[blen];
			System.arraycopy(bitmap, pos, bdata, 0, blen);
			return bdata;
		}
	}

	/** Phase to validate the graphic */
	private class ValidateGraphic extends Phase {

		/** Validate a graphic entry in the graphic table */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			status.setEnum(DmsGraphicStatus.Enum.readyForUseReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyStatusReadyForUse();
		}
	}

	/** Phase to verify the graphic status is ready for use */
	private class VerifyStatusReadyForUse extends Phase {

		/** Time to stop checking if the graphic is ready for use */
		private final long expire = TimeSteward.currentTimeMillis() +
			10 * 1000;

		/** Verify the graphic status is ready for use */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() ==
			    DmsGraphicStatus.Enum.readyForUse)
				return new VerifyGraphicFinal();
			if (TimeSteward.currentTimeMillis() > expire) {
				logError("graphic status timeout expired -- " +
					"aborted");
				return nextGraphicPhase();
			} else
				return this;
		}
	}

	/** Phase to verify a graphic after validating */
	private class VerifyGraphicFinal extends Phase {

		/** Verify a graphic */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsGraphicID gid = new DmsGraphicID(row);
			mess.add(gid);
			mess.queryProps();
			logQuery(gid);
			if (!isIDCorrect(gid.getInteger())) {
				setErrorStatus("Graphic " +graphic.getGNumber()+
					" ID incorrect after validating");
			}
			return nextGraphicPhase();
		}
	}
}
