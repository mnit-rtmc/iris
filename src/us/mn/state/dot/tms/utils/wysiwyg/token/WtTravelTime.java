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

package us.mn.state.dot.tms.utils.wysiwyg.token;

import us.mn.state.dot.tms.utils.Multi;
import static us.mn.state.dot.tms.utils.Multi.OverLimitMode;

import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WState;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Travel-time token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
//TODO:  [tt s,m,t ]     XX (we would also want to display the prepend/append text if possible)
public class WtTravelTime extends Wt_IrisToken {

	String sid;
	OverLimitMode mode; 
	String o_txt;

	/**
	 * @param sid
	 * @param mode
	 * @param o_txt
	 */
	public WtTravelTime(String sid, OverLimitMode mode, String o_txt) {
		super(WTokenType.travelTime, "[tt");
		this.sid =   sid;
		this.mode =  mode;
		this.o_txt = o_txt;
		updateString();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addTravelTime(sid, mode, o_txt);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(sid);
		if (mode != null) {
			sb.append(',');
			sb.append(mode);
			if (o_txt != null) {
				sb.append(',');
				sb.append(o_txt);
			}
		}
//		appendCharCntXParameter(sb);
	}

	@Override
	public Integer getDefaultCharCntX() {
		// TODO Auto-generated method stub
		return null;
	}
}
