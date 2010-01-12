/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to send a set of graphicss to a DMS controller.
 *
 * @author Douglas Lau
 */
public class OpSendDMSGraphics extends OpDMS {

	/** Color scheme supported */
	protected final DmsColorScheme color_scheme =
		new DmsColorScheme();

	/** Number of graphics supported */
	protected final DmsGraphicMaxEntries max_graphics =
		new DmsGraphicMaxEntries();

	/** Number of graphics defined in graphic table */
	protected final DmsGraphicNumEntries num_graphics =
		new DmsGraphicNumEntries();

	/** Maximum size of a graphic */
	protected final DmsGraphicMaxSize max_size = new DmsGraphicMaxSize();

	/** Available memory for storing graphics */
	protected final AvailableGraphicMemory available_memory =
		new AvailableGraphicMemory();

	/** Size of graphic blocks (in bytes) */
	protected final DmsGraphicBlockSize block_size =
		new DmsGraphicBlockSize();

	/** Mapping of graphic numbers to indices (row in table) */
	protected final TreeMap<Integer, Integer> num_2_row =
		new TreeMap<Integer, Integer>();

	/** Set of open rows in the graphic table */
	protected final TreeSet<Integer> open_rows = new TreeSet<Integer>();

	/** Iterator of graphics to be sent to the sign */
	protected Iterator<Graphic> graphic_iterator;

	/** Current graphic */
	protected Graphic graphic;

	/** Graphic row for graphic table */
	protected int row;

	/** Create a new operation to send graphics to a DMS */
	public OpSendDMSGraphics(DMSImpl d) {
		super(DOWNLOAD, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryGraphicsConfiguration();
	}

	/** Phase to query the graphics configuration */
	protected class QueryGraphicsConfiguration extends Phase {

		/** Query the graphics configuration */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(color_scheme);
			mess.add(max_graphics);
			mess.add(num_graphics);
			mess.add(max_size);
			mess.add(available_memory);
			mess.add(block_size);
			try {
				mess.getRequest();
			}
			catch(SNMP.Message.NoSuchName e) {
				// Must be 1203v1 only (no graphics) ...
				return null;
			}
			DMS_LOG.log(dms.getName() + ": " + color_scheme);
			DMS_LOG.log(dms.getName() + ": " + max_graphics);
			DMS_LOG.log(dms.getName() + ": " + num_graphics);
			DMS_LOG.log(dms.getName() + ": " + max_size);
			DMS_LOG.log(dms.getName() + ": " + available_memory);
			DMS_LOG.log(dms.getName() + ": " + block_size);
			for(row = 1; row <= max_graphics.getInteger(); row++)
				open_rows.add(row);
			row = 1;
			lookupGraphics();
			return new QueryGraphicNumbers();
		}
	}

	/** Lookup all graphics which have the proper color scheme */
	protected void lookupGraphics() {
		final LinkedList<Graphic> graphics = new LinkedList<Graphic>();
		GraphicHelper.find(new Checker<Graphic>() {
			public boolean check(Graphic g) {
				Integer g_num = g.getGNumber();
				if(shouldSend(g_num, g)) {
					graphics.add(g);
					num_2_row.put(g_num, null);
				}
				return false;
			}
		});
		graphic_iterator = graphics.iterator();
	}

	/** Test if we should send the given graphic */
	protected boolean shouldSend(Integer g_num, Graphic g) {
		Integer w = dms.getWidthPixels();
		Integer h = dms.getHeightPixels();
		int bpp = getBpp();
		return (g_num != null && w != null && h != null) &&
		       (g.getWidth() <= w) && (g.getHeight() <= h) &&
		       (g.getBpp() == 1 || g.getBpp() == bpp);
	}

	/** Get the bpp of the sign's color scheme */
	protected int getBpp() {
		switch(color_scheme.getEnum()) {
		case monochrome1bit:
			return 1;
		case monochrome8bit:
			return 8;
		case colorClassic:
			return 8;
		case color24bit:
			return 24;
		default:
			return 0;
		}
	}

	/** Phase to query all graphic numbers */
	protected class QueryGraphicNumbers extends Phase {

		/** Query the graphic number for one graphic */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicNumber number = new DmsGraphicNumber(row);
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			mess.add(number);
			mess.add(status);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + number);
			DMS_LOG.log(dms.getName() + ": " + status);
			Integer g_num = number.getInteger();
			if(num_2_row.containsKey(g_num)) {
				num_2_row.put(g_num, row);
				open_rows.remove(row);
			}
			if(row < max_graphics.getInteger()) {
				row++;
				return this;
			} else
				return populateNum2Row();
		}
	}

	/** Populate the num_2_row mapping */
	protected Phase populateNum2Row() {
		for(Integer g_num: num_2_row.keySet()) {
			if(num_2_row.get(g_num) == null) {
				Integer r = open_rows.pollLast();
				if(r != null)
					num_2_row.put(g_num, r);
				else
					num_2_row.remove(g_num);
			}
		}
		return nextGraphicPhase();
	}

	/** Get the first phase of the next graphic */
	protected Phase nextGraphicPhase() {
		while(graphic_iterator.hasNext()) {
			graphic = graphic_iterator.next();
			Integer g_num = graphic.getGNumber();
			if(num_2_row.containsKey(g_num)) {
				row = num_2_row.get(g_num);
				return new VerifyGraphic();
			}
			DMS_LOG.log(dms.getName() + ": Skipping graphic " +
				g_num);
		}
		return null;
	}

	/** Phase to verify a graphic */
	protected class VerifyGraphic extends Phase {

		/** Verify a graphic */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicID gid = new DmsGraphicID(row);
			mess.add(gid);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + gid);
			if(isIDCorrect(gid.getInteger())) {
				DMS_LOG.log(dms.getName() + ": Graphic valid");
				return nextGraphicPhase();
			} else
				return new QueryInitialStatus();
		}
	}

	/** Compare the graphic ID */
	protected boolean isIDCorrect(int g) throws IOException {
		GraphicInfoList gil = new GraphicInfoList(graphic,
			graphic.getGNumber());
		int crc = gil.getCrc() ^ CRC16.INITIAL_CRC;
		int gid = ((crc & 0xFF) << 8) | ((crc >> 8) & 0xFF);
		return g == gid;
	}

	/** Phase to query the initial graphic status */
	protected class QueryInitialStatus extends Phase {

		/** Query the initial graphic status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			mess.add(status);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			switch(status.getEnum()) {
			case notUsed:
				return new RequestModify();
			case modifying:
			case calculatingID:
			case readyForUse:
				return new InvalidateGraphic();
			default:
				DMS_LOG.log(dms.getName() +
					": skipping graphic #" + row);
				return nextGraphicPhase();
			}
		}
	}

	/** Invalidate the graphic */
	protected class InvalidateGraphic extends Phase {

		/** Invalidate the graphic entry in the graphic table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			status.setEnum(DmsGraphicStatus.Enum.notUsedReq);
			mess.add(status);
			DMS_LOG.log(dms.getName() + ":= " + status);
			mess.setRequest();
			return new RequestModify();
		}
	}

	/** Phase to request modifying the graphic */
	protected class RequestModify extends Phase {

		/** Set the graphic status to modifyReq */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			status.setEnum(DmsGraphicStatus.Enum.modifyReq);
			mess.add(status);
			DMS_LOG.log(dms.getName() + ":= " + status);
			mess.setRequest();
			return new VerifyStatusModifying();
		}
	}

	/** Phase to verify the graphic status is modifying */
	protected class VerifyStatusModifying extends Phase {

		/** Verify the graphic status is modifying */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			mess.add(status);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			if(status.getEnum() != DmsGraphicStatus.Enum.modifying){
				DMS_LOG.log(dms.getName() +
					": graphic send aborted");
				return nextGraphicPhase();
			}
			return new CreateGraphic();
		}
	}

	/** Create the graphic */
	protected class CreateGraphic extends Phase {

		/** Create a new graphic in the graphic table */
		protected Phase poll(AddressedMessage mess) throws IOException {
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
			if(graphic.getBpp() == 24) {
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
			DMS_LOG.log(dms.getName() + ":= " + number);
			DMS_LOG.log(dms.getName() + ":= " + name);
			DMS_LOG.log(dms.getName() + ":= " + height);
			DMS_LOG.log(dms.getName() + ":= " + width);
			DMS_LOG.log(dms.getName() + ":= " + type);
			DMS_LOG.log(dms.getName() + ":= " + trans_enabled);
			DMS_LOG.log(dms.getName() + ":= " + trans_color);
			mess.setRequest();
			return new SendGraphicBlock();
		}
	}

	/** Phase to send a block of a graphic */
	protected class SendGraphicBlock extends Phase {

		/** Graphic bitmap */
		protected final byte[] bitmap;

		/** Current block */
		protected int block;

		/** Create a phase to send graphic blocks */
		protected SendGraphicBlock() throws IOException {
			bitmap = Base64.decode(graphic.getPixels());
			block = 1;
		}

		/** Send a graphic block */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicBlockBitmap block_bitmap =
				new DmsGraphicBlockBitmap(row, block);
			block_bitmap.setOctetString(createBlock());
			mess.add(block_bitmap);
			DMS_LOG.log(dms.getName() + ":= " + block_bitmap);
			mess.setRequest();
			if(block * block_size.getInteger() < bitmap.length) {
				block++;
				if(block % 20 == 0 && !controller.isFailed())
					errorCounter = 0;
				return this;
			} else
				return new ValidateGraphic();
		}

		/** Create a graphic block */
		protected byte[] createBlock() {
			int bsize = block_size.getInteger();
			int pos = (block - 1) * bsize;
			int blen = Math.min(bsize, bitmap.length - pos);
			byte[] bdata = new byte[blen];
			System.arraycopy(bitmap, pos, bdata, 0, blen);
			return bdata;
		}
	}

	/** Phase to validate the graphic */
	protected class ValidateGraphic extends Phase {

		/** Validate a graphic entry in the graphic table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			status.setEnum(DmsGraphicStatus.Enum.readyForUseReq);
			mess.add(status);
			DMS_LOG.log(dms.getName() + ":= " + status);
			mess.setRequest();
			return new VerifyStatusReadyForUse();
		}
	}

	/** Phase to verify the graphic status is ready for use */
	protected class VerifyStatusReadyForUse extends Phase {

		/** Time to stop checking if the graphic is ready for use */
		protected final long expire = System.currentTimeMillis() +
			10 * 1000;

		/** Verify the graphic status is ready for use */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsGraphicStatus status = new DmsGraphicStatus(row);
			mess.add(status);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			if(status.getEnum() ==
			   DmsGraphicStatus.Enum.readyForUse)
				return nextGraphicPhase();
			if(System.currentTimeMillis() > expire) {
				DMS_LOG.log(dms.getName() + ": graphic status" +
					" timeout expired -- aborted");
				return nextGraphicPhase();
			} else
				return this;
		}
	}
}
