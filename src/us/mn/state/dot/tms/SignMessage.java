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

/**
 * SignMessage is a class which encapsulates all the properties of a single
 * message on a dynamic message sign (DMS).
 *
 * @author Douglas Lau
 */
public class SignMessage implements Serializable {

	/** Message MULTI string */
	protected final MultiString multi;

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

	/** Time this message was deployed */
	protected final Date deployTime;

	/** Get the message deployed time */
	public Date getDeployTime() { return deployTime; }

	/** Duration of this message */
	protected int duration;

	/** Get the message duration */
	public int getDuration() {
		return duration;
	}

	/** Set the message duration */
	public void setDuration(int d) {
		duration = d;
	}

	/** Constant definition infinite duration */
	static public final int DURATION_INFINITE = 65535;

	/** Check if another message is the same */
	public boolean equals(Object o) {
		if(o instanceof SignMessage) {
			SignMessage m = (SignMessage)o;
			return multi.equals(m.multi);
		} else
			return false;
	}

	/** Check if a string matches the message */
	public boolean equalsString(String s) {
		return multi.equalsString(s);
	}

	/** Calculate a hash code for the message */
	public int hashCode() {
		return multi.hashCode();
	}

	/** Is the message blank? */
	public boolean isBlank() {
		return multi.isBlank();
	}

	/** Bitmap graphic */
	protected BitmapGraphic bitmap;

	/** Get a bitmap graphic of the message */
	public BitmapGraphic getBitmap() {
		return bitmap;
	}

	/** Set a bitmap graphic of the message */
	public void setBitmap(BitmapGraphic b) {
		bitmap = b;
	}

	/** Create a new sign message */
	public SignMessage(String o, MultiString m, BitmapGraphic b, int d) {
		if(o == null || m == null)
			throw new NullPointerException();
		owner = o;
		multi = m;
		bitmap = b;
		deployTime = new Date();
		duration = d;
	}
}
