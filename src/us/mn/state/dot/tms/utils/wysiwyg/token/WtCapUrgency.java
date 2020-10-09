/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.utils.wysiwyg.token;


import java.util.ArrayList;

import us.mn.state.dot.tms.CapUrgencyHelper;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** CAP urgency substitution field token for WYSIWYG editor.
 * 
 * @author Gordon Parikh - SRF Consulting
 *
 */
public class WtCapUrgency extends Wt_IrisToken {

	String[] uvals;
	
	public WtCapUrgency(String[] uvals) {
		super(WTokenType.capUrgency, "[capurgency");
		this.uvals = uvals;
		updateString();
	}

	/** Get list of applicable urgency values (all if empty) */
	public String[] getResponseTypes() {
		return uvals;
	}
	
	/** Get width of WYSIWYG box
	 * @param chsp Character spacing (null = use font default)
	 */
	@Override
	public Integer getBoxWidth(Integer chsp) {
		// get the maximum urgency substitution MULTI string length
		ArrayList<String> multiStrs = CapUrgencyHelper.getMaxLen(uvals);
		
		// find the maximum width string (in pixels given the current font)
		int maxWidth = 0;
		for (String ms: multiStrs) {
			int w = wfont.getTextWidth(chsp, ms);
			if (w > maxWidth)
				maxWidth = w;
		}
		return maxWidth;
	}
	
	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addCapResponse(uvals);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		for (String u: uvals) {
			sb.append(u);
			sb.append(",");
		}
		// remove any trailing comma if one was added 
		if (sb.charAt(sb.length()-1) == ',')
			sb.setLength(sb.length()-1);
	}
	
}
