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

import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Travel-time token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */

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
	
	/** Get station ID */
	public String getStationId() {
		return sid;
	}
	
	/** Get over limit mode */
	public OverLimitMode getOverLimitMode() {
		return mode;
	}
	
	/** Get over limit text */
	public String getOverLimitText() {
		return o_txt;
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
	}

	/** Max pixel-width of travel-time minutes
	 *  (without prefix/suffix strings).
	 * @param chsp Character spacing (null = use font default)
	 */
	private int getMaxTravelTimeWidth(Integer chsp) {
		/* The following is a hack based on the
		 * idea that the maximum reasonable
		 * travel-time prediction is 60 minutes.
		 * AND that travel times are rounded
		 * to the closest 5 minutes.
		 */
		int wid = 0;
		int w2;
		for (int curMin = 5; (curMin <= 60); curMin += 5) {
			w2 = wfont.getTextWidth(chsp, Integer.toString(curMin));
			if (wid < w2)
				wid = w2;
		}
		return wid;
	}

	/** get width of WYSIWYG box
	 * @param chsp Character spacing (null = use font default)
	 */
	@Override
	public Integer getBoxWidth(Integer chsp) {
		if ((mode != null) && (mode == OverLimitMode.blank))
			return null;
		String str = o_txt;
		if ((str == null) || str.isEmpty())
			str = "OVER ";
		int charSpacing = wfont.getCharSpacing(chsp);
		return charSpacing
		     + wfont.getTextWidth(chsp, str)
		     + getMaxTravelTimeWidth(chsp);
	}
}
