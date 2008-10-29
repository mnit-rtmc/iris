/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * SignMessage is a class which encapsulates all the properties of a single
 * message on a dynamic message sign (DMS). A SignMessage contains both the
 * text associated with the message and a bitmap. A single SignMessage
 * may contain a single or multiple pages. A single bitmap is stored per 
 * message page. There are no requirements that a 2 page message must contain
 * a BitmapGraphic for both pages--that is up to the caller.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @see MsgActPriority,MsgActPriorityProc,DMSImpl,MsgActPriorityCallBackBlank
 */
public class SignMessage implements Serializable 
{
	/** Create a one page map of graphics */
	static protected TreeMap<Integer, BitmapGraphic> createSinglePage(
		BitmapGraphic b)
	{
		TreeMap<Integer, BitmapGraphic> maps =
			new TreeMap<Integer, BitmapGraphic>();
		maps.put(0, b);
		return maps;
	}

	/** Message MULTI string, contains message text for all pages */
	protected final MultiString multi;

	/** 
	 * Create a new sign message. Use this constructor to create a message
	 * with a single or multiple page of text, and a single page
	 * BitmapGraphic.
	 * @param o Message owner.
	 * @param m Message text as a MultiString. 
	 * @param b Message BitmapGraphic for the first page.
	 * @param d Message duration in mins. Use 0 for a blank message. 
	 */
	public SignMessage(String o, MultiString m, BitmapGraphic b, int d) {
		this(o, m, createSinglePage(b), d);
	}

	/** 
	 * Create a new sign message. Use this constructor to create a message
	 * with a single or multiple page of text, single or multiple 
	 * BitmapGraphics, with a default activation priority.
	 * @param o Message owner.
	 * @param m Message text as a MultiString. 
	 * @param b Message Map of BitmapGraphic for all pages.
	 * @param d Message duration in mins. Use 0 for a blank message. 
	 */
	public SignMessage(String o, MultiString m,
		Map<Integer, BitmapGraphic> b, int d)
	{
		if(o == null || m == null || b == null)
			throw new NullPointerException();
		this.owner = o;
		this.multi = m;
		bitmaps = new TreeMap<Integer, BitmapGraphic>(b);
		this.deployTime = new Date();
		this.durationMins = d;
		this.m_activationPriority = MsgActPriority.PRI_OPER_MSG;
	}

	/** 
	 * Create a new sign message. Use this constructor to create a message
	 * with a single or multiple page of text, single or multiple 
	 * BitmapGraphics, and an activation priority.
	 * @param o Message owner.
	 * @param m Message text as a MultiString. 
	 * @param b Message Map of BitmapGraphic for all pages.
	 * @param d Message duration in mins. Use 0 for a blank message. 
	 * @param ap Message activation priority.
	 */
	public SignMessage(String o, MultiString m,
		Map<Integer, BitmapGraphic> b, int d, MsgActPriority ap)
	{
		if(o == null || m == null || b == null || ap == null)
			throw new NullPointerException();
		this.owner = o;
		this.multi = m;
		this.bitmaps = new TreeMap<Integer, BitmapGraphic>(b);
		this.deployTime = new Date();
		this.durationMins = d;
		this.m_activationPriority = ap;
	}

	/** Get the message MULTI string */
	public MultiString getMulti() {
		return multi;
	}

	/** Message owner */
	protected final String owner;

	/** Get the message owner */
	public String getOwner() {
		return owner;
	}

	/** return true if the message belongs to the specified owner */
	public boolean isOwner(String argOwner) {
		if (argOwner==null || owner==null)
			return false;
		return argOwner.equals(owner);
	}

	/** Time this message was deployed */
	protected final Date deployTime;	// FIXME: maybe better to use ticks here (a long)

	/** Get the message deployed time */
	public Date getDeployTime() { return deployTime; }

	/** Duration of this message, in minutes */
	protected int durationMins;

	/*
	 * Get the message duration.
	 * @return message duration in minutes.
	 */
	public int getDuration() {
		return durationMins;
	}

	/** 
	 * Set the message duration.
	 * @param d Message duration in minutes.
	 */
	public void setDuration(int d) {
		durationMins = d;
	}

	/** Constant definition infinite duration */
	static public final int DURATION_INFINITE = 65535;

	/** Check if another message has the same MultiString text */
	public boolean equals(Object o) {
		if(o instanceof SignMessage) {
			SignMessage m = (SignMessage)o;
			return multi.equals(m.multi);
		} else if( o instanceof String)
			return multi.equals((String)o);
		return false;
	}

	/** Calculate a hash code for the message */
	public int hashCode() {
		return multi.hashCode();
	}

	/** Is the message blank? */
	public boolean isBlank() {
		return multi.isBlank();
	}

	/** Bitmaps for each page, accessed by page number (zero based) */
	protected final TreeMap<Integer, BitmapGraphic> bitmaps;

	/** 
	 * Get the message BitmapGraphic of the 1st page.
	 * @return b BitmapGraphic, will return null if a bitmap has not been
	 * set.
	 */
	public BitmapGraphic getBitmap() {
		return getBitmap(0);
	}

	/** 
	 * Get the message BitmapGraphic of the specified page. 
	 * @param pg Page number of the returned BitmapGraphic, 0 based number.
	 * @return b BitmapGraphic to get for the specified page. Will return
	 *           null if a bitmap has not been set.
	 */
	public BitmapGraphic getBitmap(int pg) {
		assert pg >= 0;
		return bitmaps.get(pg);
	}

	/** 
	 * Get a blank bitmap the same size as the message bitmap.
	 * @return Empty BitmapGraphic the same size as the message's bitmap.
	 */
	public BitmapGraphic getBlankBitmap() {
		BitmapGraphic b = bitmaps.get(0);
		if(b != null)
			return new BitmapGraphic(b.width, b.height);
		else
			return null;
	}

	/** Set the message BitmapGraphic of the 1st page */
	public void setBitmap(BitmapGraphic b) {
		setBitmap(0, b);
	}

	/** Set the message BitmapGraphic for multiple pages */
	public void setBitmaps(Map<Integer, BitmapGraphic> b) {
		bitmaps.clear();
		bitmaps.putAll(b);
	}

	/** 
	 * Set the message BitmapGraphic of the specified page.
	 * @param pg Page number of the returned BitmapGraphic, 0 based number.
	 * @param b BitmapGraphic to set for the specified page.
	 */
	public void setBitmap(int pg, BitmapGraphic b) {
		assert pg >= 0;
		bitmaps.put(pg, b);
	}

	/** toString() returns the message as a MultiString */
	public String toString() {
		return multi.toString();
	}

	/** toString() returns the message as a MultiString, plus other debug info */
	public String toStringDebug() {
		return "Msg="+multi.toString()+", actPriority="+
			m_activationPriority.valueOf()+", owner="+owner+
			", durationMins="+durationMins+".";
	}

	/** return the number of pages in the message MultiString */
	public int getNumPages() {
		return multi.getNumPages();
	}

	/* message activation priority */
	protected MsgActPriority m_activationPriority = 
		MsgActPriority.PRI_OPER_MSG;

	/** get the message activation priority */
	public MsgActPriority getActivationPriority() {
		return m_activationPriority;
	}

	/** set the message activation priority */
	public void setActivationPriority(MsgActPriority m) {
		m_activationPriority = m;
	}

	/** 
	 * Determine if one message would supersede (replace) another on the 
	 * DMS using numeric NTCIP priority values.
	 *  @param mos Typically the message on the sign.
	 *  @return true if this supersedes the argument message.
	 */
	public boolean supersedeNumeric(SignMessage mos) {
		if(mos == null)
			return false;
		if(getActivationPriority() == null)
			return true;
		return this.getActivationPriority().supersede(mos.getActivationPriority());
	}

	/** 
	 * Determine if one message would supersede (replace) another on the 
	 * DMS. Note that we are asking the potential new message (this) to 
	 * evaluate whether it supersedes the message on the sign (the arg).
	 *  @param mos Typically the message on the sign.
	 *  @return true if this supersedes the argument message.
	 */
	public boolean supersede(SignMessage mos) {
		if(mos == null || this.getActivationPriority() == null)
			return true;
		// start with subclass
		else if(this.getActivationPriority() instanceof MsgActPriorityProc)
			return ((MsgActPriorityProc)this.getActivationPriority()).supersede(mos);
		// straight numeric priority comparison
		else if(this.getActivationPriority() instanceof MsgActPriority)
			return this.supersedeNumeric(mos);
		else {
			String err="Unknown type in SignMessage.supersede()";
			assert false : err;
			System.err.println(err);
			return true;
		}
	}
}
