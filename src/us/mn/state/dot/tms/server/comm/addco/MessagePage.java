/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.addco;

import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.units.Interval;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A message page contains the MULTI string and bitmap for one page of an
 * Addco sign message.
 *
 * @author Douglas Lau
 */
public class MessagePage {

	/** Lookup the MULTI string for the page */
	static private MultiString lookupMulti(String ms, int p) {
		return new MultiString(new MultiString(ms).getPage(p));
	}

	/** MULTI string for the page */
	private final MultiString multi;

	/** Bitmap graphic for the page */
	private final BitmapGraphic bitmap;

	/** Create a new message page.
	 * @param dms DMS for message.
	 * @param sm Sign message.
	 * @param p Page number (0-relative). */
	public MessagePage(DMSImpl dms, SignMessage sm, int p) {
		multi = lookupMulti(sm.getMulti(), p);
		bitmap = lookupBitmap(dms, sm, p);
	}

	/** Lookup a bitmap for the page */
	private BitmapGraphic lookupBitmap(DMSImpl dms, SignMessage sm, int p) {
		BitmapGraphic[] bmaps = SignMessageHelper.getBitmaps(sm, dms);
		if (bmaps != null && bmaps.length > p)
			return bmaps[p];
		else
			return null;
	}

	/** Create a new message page.
	 * @param dms DMS for message.
	 * @param ms MULTI string. */
	public MessagePage(DMSImpl dms, String ms) {
		multi = lookupMulti(ms, 0);
		bitmap = createBitmap(dms, ms);
	}

	/** Create a new message page.
	 * @param ms MULTI string.
	 * @param bmap Bitmap graphic. */
	public MessagePage(String ms, BitmapGraphic bmap) {
		multi = lookupMulti(ms, 0);
		bitmap = bmap;
	}

	/** Create a bitmap for a DMS */
	private BitmapGraphic createBitmap(DMSImpl dms, String ms) {
		try {
			BitmapGraphic[] bmaps = DMSHelper.createBitmaps(dms,ms);
			if (bmaps != null && bmaps.length > 0)
				return bmaps[0];
		}
		catch (InvalidMsgException e) {
			// fall thru
		}
		return DMSHelper.createBitmapGraphic(dms);
	}

	/** Get the name for the page (MULTI string without [pt] tag) */
	public String getName() {
		return multi.stripPageTime();
	}

	/** Get the MULTI string for the page */
	public MultiString getMulti() {
		return multi;
	}

	/** Get the bitmap graphic for the page */
	public BitmapGraphic getBitmap() {
		return bitmap;
	}

	/** Get the page on time (deciseconds) */
	public int getPageOnTime() {
		Interval p_on = multi.pageOnInterval();
		int p = p_on.round(Interval.Units.DECISECONDS);
		// Zero page-on time is invalid -- use 2.0 seconds
		return (p > 0) ? p : 20;
	}

	/** Get the page off time (deciseconds) */
	public int getPageOffTime() {
		Interval p_off = multi.pageOffInterval();
		return p_off.round(Interval.Units.DECISECONDS);
	}
}
