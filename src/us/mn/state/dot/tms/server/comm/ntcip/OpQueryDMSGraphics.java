/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.PixmapGraphic;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Flags;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;

/**
 * Operation to query graphics on a DMS.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSGraphics extends OpDMS {

	/** Directory to store graphics files */
	static private final String GIF_FILE_DIR =
		"/var/lib/iris/web/gif";

	/** Color scheme supported (for graphics) */
	private final ASN1Enum<ColorScheme> color_scheme = new ASN1Enum<
		ColorScheme>(ColorScheme.class, dmsColorScheme.node);

	/** Number of graphics supported */
	private final ASN1Integer max_graphics = dmsGraphicMaxEntries.makeInt();

	/** Size of graphic blocks (in bytes) */
	private final ASN1Integer block_size = dmsGraphicBlockSize.makeInt();

	/** Maximum size of a graphic */
	private final ASN1Integer max_size = dmsGraphicMaxSize.makeInt();

	/** Directory to write graphics files */
	private final File dir;

	/** Create a new query DMS graphics operation */
	public OpQueryDMSGraphics(DMSImpl d) {
		super(PriorityLevel.POLL_LOW, d);
		dir = new File(GIF_FILE_DIR, d.getName());
		if (!dir.exists())
			dir.mkdirs();
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryConfig();
	}

	/** Phase to query the graphics configuration */
	private class QueryConfig extends Phase {
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(color_scheme);
			mess.add(max_graphics);
			mess.add(block_size);
			mess.add(max_size);
			mess.queryProps();
			logQuery(color_scheme);
			logQuery(max_graphics);
			logQuery(block_size);
			logQuery(max_size);
			return nextRow(0);
		}
	}

	/** Get phase for the next row */
	private Phase nextRow(int row) {
		return (row < max_graphics.getInteger())
		      ? new QueryStatus(row + 1)
		      : null;
	}

	/** Phase to query the status of one graphic */
	private class QueryStatus extends Phase {
		private final int row;
		private QueryStatus(int r) {
			row = r;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsGraphicStatus> gst =
				new ASN1Enum<DmsGraphicStatus>(
					DmsGraphicStatus.class,
					dmsGraphicStatus.node,
					row
				);
			mess.add(gst);
			mess.queryProps();
			logQuery(gst);
			if (gst.getEnum().isValid())
				return new QueryEntry(row);
			else
				return nextRow(row);
		}
	}

	/** Phase to query a graphic entry */
	private class QueryEntry extends Phase {
		private final int row;
		private QueryEntry(int r) {
			row = r;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer height = dmsGraphicHeight.makeInt(row);
			ASN1Integer width = dmsGraphicWidth.makeInt(row);
			ASN1Enum<ColorScheme> type = new ASN1Enum<
				ColorScheme>(ColorScheme.class,
				dmsGraphicType.node, row);
			ASN1Integer trans_enabled =
				dmsGraphicTransparentEnabled.makeInt(row);
			ASN1OctetString trans_color = new ASN1OctetString(
				dmsGraphicTransparentColor.node, row);
			mess.add(height);
			mess.add(width);
			mess.add(type);
			mess.add(trans_enabled);
			mess.add(trans_color);
			mess.queryProps();
			logQuery(height);
			logQuery(width);
			logQuery(type);
			logQuery(trans_enabled);
			logQuery(trans_color);
			Integer tc = null;
			if (trans_enabled.getInteger() == 1) {
				tc = transparentColor(
					trans_color.getOctetString()
				);
			}
			RasterGraphic raster = createRaster(type.getEnum(),
				width.getInteger(), height.getInteger(), tc);
			if (raster != null)
				return new QueryBlock(row, raster);
			else {
				logError("Can not save graphic." + row +
					": " + type);
				return nextRow(row);
			}
		}
	}

	/** Make transparent color */
	private Integer transparentColor(byte[] b) {
		if (b.length == 1)
			return (int) b[0];
		else if (b.length == 3) {
			int red = b[0];
			int grn = b[1];
			int blu = b[2];
			return (red << 16) | (grn << 8) | blu;
		} else
			return null;
	}

	/** Create a raster graphic */
	private RasterGraphic createRaster(ColorScheme tp, int width,
		int height, Integer tc)
	{
		switch (tp) {
		case MONOCHROME_1_BIT:
			return new BitmapGraphic(width, height);
		case COLOR_24_BIT:
			return new PixmapGraphic(width, height, tc);
		default:
			return null;
		}
	}

	/** Phase to query a block of a graphic */
	private class QueryBlock extends Phase {
		private final int row;
		private final RasterGraphic raster;
		private final int blocks;
		private int block;
		private QueryBlock(int r, RasterGraphic rg) throws IOException {
			row = r;
			raster = rg;
			int sz = block_size.getInteger();
			blocks = (rg.length() + sz - 1) / sz;
			block = 1;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1OctetString block_bitmap = new ASN1OctetString(
				dmsGraphicBlockBitmap.node, row, block);
			mess.add(block_bitmap);
			mess.queryProps();
			logQuery(block_bitmap);
			copyBlock(block_bitmap.getOctetString(), raster, block);
			if (block < blocks) {
				block++;
				if (block % 20 == 0 && !controller.isOffline())
					setSuccess(true);
				return this;
			}
			return new QueryNumberName(row, raster);
		}
	}

	/** Copy block to raster */
	private void copyBlock(byte[] buf, RasterGraphic rg, int block) {
		byte[] pixels = rg.getPixelData();
		int sz = block_size.getInteger();
		int pos = (block - 1) * sz;
		int blen = Math.min(sz, pixels.length - pos);
		System.arraycopy(buf, pos, pixels, 0, blen);
	}

	/** Phase to query a graphic number/name */
	private class QueryNumberName extends Phase {
		private final int row;
		private final RasterGraphic raster;
		private QueryNumberName(int r, RasterGraphic rg) {
			row = r;
			raster = rg;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer number = dmsGraphicNumber.makeInt(row);
			DisplayString name = new DisplayString(
				dmsGraphicName.node, row);
			ASN1Integer gid = dmsGraphicID.makeInt(row);
			mess.add(number);
			mess.add(name);
			mess.add(gid);
			mess.queryProps();
			logQuery(number);
			logQuery(name);
			logQuery(gid);
			saveRaster(number.getInteger(), name.getValue(),
				gid.getInteger(), raster);
			return nextRow(row);
		}
	}

	/** Save raster to a .GIF file */
	private void saveRaster(int num, String name, int gid,
		RasterGraphic rg) throws IOException
	{
		String nm = "G" + num + "_" + gid + ".gif";
		int width = rg.getWidth();
		int height = rg.getHeight();
		BufferedImage buffer = new BufferedImage(width, height,
			BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				DmsColor clr = rg.getPixel(x, y);
				buffer.setRGB(x, y, clr.rgb());
			}
		}
		// FIXME: encode transparent color somehow
		ImageIO.write(buffer, "gif", new File(dir, nm));
	}
}
