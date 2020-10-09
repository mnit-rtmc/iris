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

import us.mn.state.dot.tms.CapResponseTypeHelper;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** CAP response type substitution field token for WYSIWYG editor.
 * 
 * @author Gordon Parikh - SRF Consulting
 *
 */
public class WtCapResponse extends Wt_IrisToken {

	String[] rtypes;
	
	public WtCapResponse(String[] rtypes) {
		super(WTokenType.capResponse, "[capresponse");
		this.rtypes = rtypes;
		updateString();
	}

	/** Get list of applicable response types (all if empty) */
	public String[] getResponseTypes() {
		return rtypes;
	}
	
	/** Get width of WYSIWYG box
	 * @param chsp Character spacing (null = use font default)
	 */
	@Override
	public Integer getBoxWidth(Integer chsp) {
		// get all response type substitution MULTI strings
		ArrayList<String> multiStrs = CapResponseTypeHelper.getMaxLen(rtypes);
		
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
		cb.addCapResponse(rtypes);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		for (String rt: rtypes) {
			sb.append(rt);
			sb.append(",");
		}
		// remove any trailing comma if one was added 
		if (sb.charAt(sb.length()-1) == ',')
			sb.setLength(sb.length()-1);
	}
	
}
