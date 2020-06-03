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

import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WState;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/**  Speed advisory token for WYSIWYG editor.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class WtSpeedAdvisory extends Wt_IrisToken {

	public WtSpeedAdvisory() {
		super(WTokenType.speedAdvisory, "[vsa");
		updateString();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addSpeedAdvisory();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		; // do nothing
	}

	/** get width of WYSIWYG box
	 * @param chsp Character spacing (null = use font default)
	 */
	@Override
	public Integer getBoxWidth(Integer chsp) {
		int minSpd = SystemAttrEnum.VSA_MIN_DISPLAY_MPH.getInt();
		int maxSpd = SystemAttrEnum.VSA_MAX_DISPLAY_MPH.getInt();
		return wfont.getIntWidth(chsp, minSpd, maxSpd);
	}
}
