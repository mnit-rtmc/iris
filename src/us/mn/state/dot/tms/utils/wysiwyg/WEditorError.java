/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.utils.wysiwyg;

import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiSyntaxError;

/**
 * WYSIWYG Editor Error
 * 
 * Contains information about an error that
 * occurs somewhere in the WYSIWYG editor.
 * This will usually be a MULTI error, but
 * can be other things as well.
 * 
 * @author John L. Stanley - SRF Consulting
 */

public class WEditorError {
	MultiSyntaxError mse; // MultiSyntaxError
	String shortErrStr;   // short MULTI error name
	String longErrStr;    // human readable explanation

	/** Constructor for a MultiSyntaxError error */
	public WEditorError(MultiSyntaxError xmse) {
		mse = xmse;
		shortErrStr = mse.toString();
		String des = I18N.get(shortErrStr);
		if ("Undefined I18N string".equals(des))
			des = null;
		longErrStr = des;
	}

	/** Constructor for errors that are not
	 *  MultiSyntaxError errors */
	public WEditorError(String em) {
		mse = MultiSyntaxError.other;
		shortErrStr = "other";
		String des = I18N.get(em);
		if ("Undefined I18N string".equals(des))
			des = em;
		longErrStr = des;
	}

	//-------------------------------------------
	// Get info about token error

	/** Get short error description */
	public String getShortErrStr() {
		return shortErrStr;
	}

	/** Get long error description */
	public String getLongErrStr() {
		return longErrStr;
	}
}
