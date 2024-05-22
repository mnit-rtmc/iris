/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2024  Minnesota Department of Transportation
 * Copyright (C) 2009-2010  AHMCT, University of California
 * Copyright (C) 2021  Iteris Inc.
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

import java.util.Iterator;
import java.util.Objects;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Helper for dealing with sign messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageHelper extends BaseHelper {

	/** Do not allow objects of this class */
	private SignMessageHelper() {
		assert false;
	}

	/** Lookup the sign message with the specified name */
	static public SignMessage lookup(String name) {
		return (SignMessage) namespace.lookupObject(
			SignMessage.SONAR_TYPE, name);
	}

	/** Get a sign message iterator */
	static public Iterator<SignMessage> iterator() {
		return new IteratorWrapper<SignMessage>(namespace.iterator(
			SignMessage.SONAR_TYPE));
	}

	/** Make a message owner string */
	static public String makeMsgOwner(int src) {
		return makeMsgOwner(src, USER_AUTO);
	}

	/** Make a message owner string with name */
	static public String makeMsgOwner(int src, String name) {
		return "IRIS; " + SignMsgSource.toString(src) + "; " +
			name;
	}

	/** Get the system part of message owner */
	static public String getMsgOwnerSystem(SignMessage sm) {
		String[] owner = sm.getMsgOwner().split(";", 3);
		return (owner.length > 0) ? owner[0].trim() : "";
	}

	/** Get the sources part of message owner */
	static public String getMsgOwnerSources(SignMessage sm) {
		String[] owner = sm.getMsgOwner().split(";", 3);
		return (owner.length > 1)
		      ? owner[1].trim()
		      : SignMsgSource.external.toString();
	}

	/** Get the name part of message owner */
	static public String getMsgOwnerName(SignMessage sm) {
		String[] owner = sm.getMsgOwner().split(";", 3);
		return (owner.length > 2) ? owner[2].trim() : "";
	}

	/** Find a sign message with matching attributes.
	 * @param sc Sign configuration.
	 * @param inc Associated incident (original name).
	 * @param ms MULTI string.
	 * @param owner Message owner.
	 * @param fb Flash beacon flag.
	 * @param mp Message priority.
	 * @param dur Duration (null for indefinite).
	 * @return Matching sign message, or null if not found. */
	static public SignMessage find(SignConfig sc, String inc, String ms,
		String owner, boolean fb, SignMsgPriority mp, Integer dur)
	{
		// FIXME: use hash to implement this
		int mpi = mp.ordinal();
		Iterator<SignMessage> it = iterator();
		while (it.hasNext()) {
			SignMessage sm = it.next();
			if (objectEquals(sc, sm.getSignConfig()) &&
			    objectEquals(inc, sm.getIncident()) &&
			    ms.equals(sm.getMulti()) &&
			    objectEquals(owner, sm.getMsgOwner()) &&
			    fb == sm.getFlashBeacon() &&
			    mpi == sm.getMsgPriority() &&
			    objectEquals(dur, sm.getDuration()))
				return sm;
		}
		return null;
	}

	/** Check if a sign message is blank */
	static public boolean isBlank(SignMessage sm) {
		return (null == sm) ||
		       new MultiString(sm.getMulti()).isBlank();
	}

	/** Get source bits for a sign message */
	static public int sourceBits(SignMessage sm) {
		return (sm != null)
		      ? SignMsgSource.fromString(getMsgOwnerSources(sm))
		      : SignMsgSource.unknown.bit();
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
		catch (IndexOutOfBoundsException e) {
			throw new InvalidMsgException(e.getMessage());
		}
	}

	/** Validate sign message bitmaps.
	 * @param multi Message MULTI string.
	 * @param dms Sign to check.
	 * @throws InvalidMsgException. */
	static private void validateBitmaps(MultiString multi, DMS dms)
		throws InvalidMsgException
	{
		BitmapGraphic[] bmaps = DMSHelper.createBitmaps(dms,
			multi.toString());
		if (null == bmaps)
			throw new InvalidMsgException("no sign config");
		if (bmaps.length == 0)
			throw new InvalidMsgException("no pages");
		if (!multi.isBlank()) {
			BitmapGraphic stuck_off = DMSHelper.createStuckBitmap(
				dms, DMS.STUCK_OFF_BITMAP);
			BitmapGraphic stuck_on = DMSHelper.createStuckBitmap(
				dms, DMS.STUCK_ON_BITMAP);
			if (stuck_off != null && stuck_on != null) {
				for (BitmapGraphic bg : bmaps) {
					validateBitmap(bg, stuck_off, stuck_on);
				}
			}
		}
	}

	/** Validate one message bitmap.
	 * @param bg Bitmap graphic to validate.
	 * @param stuck_off Stuck off pixel bitmap.
	 * @param stuck_on Stuck on pixel bitmap.
	 * @throws InvalidMsgException. */
	static private void validateBitmap(BitmapGraphic bg,
		BitmapGraphic stuck_off, BitmapGraphic stuck_on)
		throws InvalidMsgException
	{
		if (bg.length() == 0)
			throw new InvalidMsgException("sign size");
		// This should never happen
		if (stuck_off.length() != bg.length())
			throw new InvalidMsgException("stuck off size", true);
		if (stuck_on.length() != bg.length())
			throw new InvalidMsgException("stuck on size", true);
		BitmapGraphic temp = bg.createBlankCopy();
		temp.setPixelData(bg.getPixelData());
		temp.clearTransparent(stuck_off);
		int n_off = temp.getLitCount();
		int off_lim = SystemAttrEnum.DMS_PIXEL_OFF_LIMIT.getInt();
		if (off_lim >= 0 && n_off > off_lim) {
			throw new InvalidMsgException(
				"Too many stuck off pixels: " + n_off, true);
		}
		temp.setPixelData(bg.getPixelData());
		temp.outlineLitPixels();
		temp.clearTransparent(stuck_on);
		int n_on = temp.getLitCount();
		int on_lim = SystemAttrEnum.DMS_PIXEL_ON_LIMIT.getInt();
		if (on_lim >= 0 && n_on > on_lim) {
			throw new InvalidMsgException(
				"Too many stuck on pixels: " + n_on, true);
		}
	}

	/** Check if a message is scheduled and has indefinite duration.
	 * This should only be true for messages from "sticky" DMS actions.
	 * @param sm The sign message. */
	static public boolean isScheduledSticky(SignMessage sm) {
		int bits = sourceBits(sm);
		return SignMsgSource.schedule.checkBit(bits) &&
		      (sm.getDuration() == null) &&
		      !SignMsgSource.operator.checkBit(bits);
	}

	/** Check if a message is scheduled and expires.
	 * @param sm The sign message. */
	static public boolean isScheduledExpiring(SignMessage sm) {
		int bits = sourceBits(sm);
		return SignMsgSource.schedule.checkBit(bits) &&
		      (sm.getDuration() != null) &&
		      !SignMsgSource.operator.checkBit(bits);
	}

	/** Check if a message is operator created and expires.
	 * @param sm The sign message. */
	static public boolean isOperatorExpiring(SignMessage sm) {
		return (!isBlank(sm)) &&
		       (sm.getDuration() != null) &&
		       SignMsgSource.operator.checkBit(sourceBits(sm));
	}

	/** Check if a message came from RWIS subsystem.
	 * @param sm The sign message. */
	static public boolean isRwis(SignMessage sm) {
		return (!isBlank(sm)) &&
				SignMsgSource.rwis.checkBit(sourceBits(sm));
	}

	/** Make a hash code given sign message attributes.
	 * @param sc Sign configuration.
	 * @param inc Associated incident (original name).
	 * @param ms MULTI string.
	 * @param owner Message owner.
	 * @param fb Flash beacon flag.
	 * @param mp Message priority.
	 * @param dur Duration (null for indefinite).
	 * @return Matching sign message, or null if not found. */
	static public int hash(SignConfig sc, String inc, String ms,
		String owner, boolean fb, SignMsgPriority mp, Integer dur)
	{
		return Objects.hash(sc.getName(), inc, ms, owner, fb, mp, dur);
	}
}
