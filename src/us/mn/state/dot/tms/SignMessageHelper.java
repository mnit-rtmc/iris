/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2018  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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
package us.mn.state.dot.tms;

import java.io.IOException;
import java.util.Iterator;
import us.mn.state.dot.tms.utils.Base64;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Helper for dealing with sign messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageHelper extends BaseHelper {

	/** Maximum allowed pages for any message */
	static public final int DMS_MESSAGE_MAX_PAGES = 6;

	/** Do not allow objects of this class */
	private SignMessageHelper() {
		assert false;
	}

	/** Lookup the sign message with the specified name */
	static public SignMessage lookup(String name) {
		return (SignMessage)namespace.lookupObject(
			SignMessage.SONAR_TYPE, name);
	}

	/** Get a sign message iterator */
	static public Iterator<SignMessage> iterator() {
		return new IteratorWrapper<SignMessage>(namespace.iterator(
			SignMessage.SONAR_TYPE));
	}

	/** Find a sign message with matching attributes.
	 * @param multi MULTI string.
	 * @param be Beacon enabled flag.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param src Message source.
	 * @param owner Use name (null for any).
	 * @param d Duration (null for indefinite).
	 * @return Matching sign message, or null if not found. */
	static public SignMessage find(String multi, boolean be,
		DmsMsgPriority ap,
		DmsMsgPriority rp, int src, String owner, Integer d)
	{
		int api = ap.ordinal();
		int rpi = rp.ordinal();
		Iterator<SignMessage> it = iterator();
		while (it.hasNext()) {
			SignMessage sm = it.next();
			if (multi.equals(sm.getMulti()) &&
			    be == sm.getBeaconEnabled() &&
			    api == sm.getActivationPriority() &&
			    rpi == sm.getRunTimePriority() &&
			    sourceEquals(src, sm) &&
			    objectEquals(owner, sm.getOwner()) &&
			    objectEquals(d, sm.getDuration()))
				return sm;
		}
		return null;
	}

	/** Sign msg source bits to ignore */
	static private final int SRC_IGNORE = SignMsgSource.toBits(
		SignMsgSource.tolling,
		SignMsgSource.travel_time,
		SignMsgSource.external
	);

	/** Check sign message source.
	 * @param src Message source.
	 * @param sm Sign message to check.
	 * @return true if source matches. */
	static private boolean sourceEquals(int src, SignMessage sm) {
		// ignore tolling and external bits for comparison
		int srct = src           | SRC_IGNORE;
		int sms = sm.getSource() | SRC_IGNORE;
		return srct == sms;
	}

	/** Compare the attributes of 2 sign messages.
	 * @param sm1 SignMessage which may be null.
	 * @param sm2 SignMessage which may be null.
	 * @return True if sm1 and sm2 have equal MULTIs, priorities,
	 *         and bitmaps. */
	static public boolean isEquivalent(SignMessage sm1, SignMessage sm2) {
		if (sm1 == null && sm2 == null)
			return true;
		if (sm1 == null || sm2 == null)
			return false;
		if (!new MultiString(sm1.getMulti()).equals(sm2.getMulti()))
			return false;
		if (sm1.getActivationPriority() != sm2.getActivationPriority())
			return false;
		if (sm1.getRunTimePriority() != sm2.getRunTimePriority())
			return false;
		else
			return objectEquals(sm1.getOwner(), sm2.getOwner());
	}

	/** Return an array of font names in a message.
	 * @param f_num Default font number, one based.
	 * @return A string array with length equal to the number
	 *	    of pages in the message */
	static public String[] getFontNames(SignMessage sm, int f_num) {
		int[] fn = getFonts(sm, f_num);
		if (fn == null || fn.length <= 0)
			return new String[0];
		String[] fns = new String[fn.length];
		for (int i = 0; i < fns.length; ++i) {
			Font font = FontHelper.find(fn[i]);
			if (font != null)
				fns[i] = font.getName();
			else
				fns[i] = "Font #" + fn[i];
		}
		return fns;
	}

	/** Get an array of font numbers in a message.
	 * @param f_num Default font number, one based.
	 * @return An array of font numbers for each page of the message. */
	static private int[] getFonts(SignMessage sm, int f_num) {
		if (sm == null)
			return new int[0];
		else {
			MultiString m = new MultiString(sm.getMulti());
			return m.getFonts(f_num);
		}
	}

	/** Check if a sign message is blank */
	static public boolean isBlank(SignMessage sm) {
		return (null == sm) || isMultiBlank(sm);
	}

	/** Check if the MULTI string is blank */
	static private boolean isMultiBlank(SignMessage sm) {
		String ms = sm.getMulti();
		return ms == null || new MultiString(ms).isBlank();
	}

	/** Get the bitmap graphic for all pages of the specified DMS.
	 * @param sm SignMessage in question.
	 * @param dms Sign with the graphic.
	 * @return Array of bitmaps, one for each page, or null on error. */
	static public BitmapGraphic[] getBitmaps(SignMessage sm, DMS dms) {
		if (sm != null && dms != null) {
			try {
				return DMSHelper.createBitmaps(dms,
					sm.getMulti());
			}
			catch (InvalidMsgException e) {
				// fall thru and return null
			}
		}
		return null;
	}

	/** Validate a sign message for a DMS.
	 * @param sm SignMessage to validate.
	 * @param dms Sign to validate message on.
	 * @throws InvalidMsgException if message is not valid. */
	static public void validate(SignMessage sm, DMS dms)
		throws InvalidMsgException
	{
		if (null == sm)
			throw new InvalidMsgException("Sign message null");
		MultiString multi = new MultiString(sm.getMulti());
		if (!multi.isValid())
			throw new InvalidMsgException("MULTI " + sm.getMulti());
		try {
			validateBitmaps(multi, dms);
		}
		catch (IOException e) {
			throw new InvalidMsgException("Base64 decode error");
		}
		catch (IndexOutOfBoundsException e) {
			throw new InvalidMsgException(e.getMessage());
		}
	}

	/** Validate sign message bitmaps.
	 * @param multi Message MULTI string.
	 * @param dms Sign to check.
	 * @throws IOException, InvalidMsgException. */
	static private void validateBitmaps(MultiString multi, DMS dms)
		throws IOException, InvalidMsgException
	{
		BitmapGraphic[] bmaps = DMSHelper.createBitmaps(dms,
			multi.toString());
		if (null == bmaps)
			throw new InvalidMsgException("no sign config");
		if (bmaps.length == 0)
			throw new InvalidMsgException("no pages");
		if (!multi.isBlank()) {
			BitmapGraphic[] stuck = createStuckBitmaps(dms);
			if (stuck != null) {
				for (BitmapGraphic bg : bmaps) {
					validateBitmap(bg, stuck);
				}
			}
		}
	}

	/** Create stuck pixel bitmaps */
	static private BitmapGraphic[] createStuckBitmaps(DMS dms)
		throws IOException
	{
		String[] ps = dms.getPixelStatus();
		if (ps != null && ps.length == 2) {
			BitmapGraphic off = createStuckBitmap(dms,
				ps[DMS.STUCK_OFF_BITMAP]);
			BitmapGraphic on = createStuckBitmap(dms,
				ps[DMS.STUCK_ON_BITMAP]);
			if (off != null && on != null) {
				BitmapGraphic[] bg = new BitmapGraphic[2];
				bg[DMS.STUCK_OFF_BITMAP] = off;
				bg[DMS.STUCK_ON_BITMAP] = on;
				return bg;
			}
		}
		return null;
	}

	/** Create one stuck pixel bitmap */
	static private BitmapGraphic createStuckBitmap(DMS dms, String p)
		throws IOException
	{
		byte[] bd = Base64.decode(p);
		BitmapGraphic bg = DMSHelper.createBitmapGraphic(dms);
		if (bg != null && bd.length == bg.length()) {
			try {
				bg.setPixelData(bd);
				return bg;
			}
			catch (IndexOutOfBoundsException e) {
				// stuck bitmap doesn't match current dimensions
			}
		}
		return null;
	}

	/** Validate one message bitmap.
	 * @param bg Bitmap graphic to validate.
	 * @param stuck Stuck pixel bitmaps (off and on).
	 * @throws IOException, InvalidMsgException. */
	static private void validateBitmap(BitmapGraphic bg,
		BitmapGraphic[] stuck) throws IOException, InvalidMsgException
	{
		if (bg.length() == 0)
			throw new InvalidMsgException("sign size");
		for (BitmapGraphic s : stuck) {
			// This should never happen
			if (s.length() != bg.length())
				throw new InvalidMsgException("stuck size");
		}
		BitmapGraphic temp = bg.createBlankCopy();
		temp.setPixelData(bg.getPixelData());
		temp.union(stuck[DMS.STUCK_OFF_BITMAP]);
		int n_off = temp.getLitCount();
		if (n_off > SystemAttrEnum.DMS_PIXEL_OFF_LIMIT.getInt()) {
			throw new InvalidMsgException(
				"Too many stuck off pixels: " + n_off);
		}
		temp.setPixelData(bg.getPixelData());
		temp.outline();
		temp.union(stuck[DMS.STUCK_ON_BITMAP]);
		int n_on = temp.getLitCount();
		if (n_on > SystemAttrEnum.DMS_PIXEL_ON_LIMIT.getInt()) {
			throw new InvalidMsgException(
				"Too many stuck on pixels: " + n_on);
		}
	}

	/** Check if a message is scheduled and has indefinite duration.
	 * @param sm The sign message. */
	static public boolean isScheduledIndefinite(SignMessage sm) {
		int src = sm.getSource();
		return SignMsgSource.schedule.checkBit(src) &&
		      (sm.getDuration() == null) &&
		      !SignMsgSource.operator.checkBit(src);
	}

	/** Check if a message is operator created and expires.
	 * @param sm The sign message. */
	static public boolean isOperatorExpiring(SignMessage sm) {
		return (sm.getDuration() != null)
		    && SignMsgSource.operator.checkBit(sm.getSource());
	}
}
