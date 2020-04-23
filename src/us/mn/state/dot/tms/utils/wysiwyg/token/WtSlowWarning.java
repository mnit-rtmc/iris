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
import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WState;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Slow-warning token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtSlowWarning extends Wt_IrisToken {

	int spd;
	int dist;
	String mode;

	/**
	 * @param spd
	 * @param dist
	 * @param mode
	 */
	public WtSlowWarning(int spd, int dist, String mode) {
		super(WTokenType.slowWarning, "[slow");
		this.spd  = spd;
		this.dist = dist;
		this.mode = mode;
		updateString();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addSlowWarning(spd, dist, mode);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(spd);
		sb.append(',');
		sb.append(dist);
		if (mode != null) {
			sb.append(',');
			sb.append(mode);
		}
//		appendCharCntXParameter(sb);
	}

	@Override
	public Integer getCharCntX() {
		int xx;
		if ((mode == null)
		 || "none".equalsIgnoreCase(mode)) {
			// a blank string
			return null;
		}
		if ("dist".equalsIgnoreCase(mode)) {
			// distance rounded to nearest mile
			xx = (int)((dist + 5) / 10);
		}
		else if ("speed".equalsIgnoreCase(mode)) {
			// speed rounded to nearest 5 mph
			xx = Math.round(spd / 5) * 5;
		}
		else {
			// give them something they can click on to fix the problem
			return 1;
		}
		return Integer.toString(xx).length();
	}
}
