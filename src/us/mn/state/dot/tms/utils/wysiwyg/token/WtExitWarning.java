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
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Exit-warning token for WYSIWYG editor */
public class WtExitWarning extends Wt_IrisToken {

	String did;
	int occ;

	public WtExitWarning(String did, int occ) {
		super(WTokenType.exitWarning, "[exit");
		this.did  = did;
		this.occ = occ;
		updateString();
	}

	@Override
	public void doMulti(Multi cb) {
		cb.addExitWarning(did, occ);
	}

	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(did);
		sb.append(',');
		sb.append(occ);
	}

	/** Get width of WYSIWYG box.
	 * @param chsp Character spacing (null = use font default)
	 */
	@Override
	public Integer getBoxWidth(Integer chsp) {
		return null;
	}
}
