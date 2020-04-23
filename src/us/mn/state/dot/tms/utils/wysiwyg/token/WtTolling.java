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

/** Tolling-message token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtTolling extends Wt_IrisToken {

	String mode;
	String[] zones;
	
	/**
	 * @param mode
	 * @param zones
	 */
	public WtTolling(String mode, String[] zones) {
		super(WTokenType.tolling, "[tz");
		this.mode  = mode;
		this.zones = zones;
		updateString();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addTolling(mode, zones);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(mode);
		if (zones != null) {
			int len = zones.length;
			for (int i = 0; (i < len); ++i) {
				sb.append(',');
				sb.append(zones[i]);
			}
		}
		appendCharCntXParameter(sb);
	}

	/** Get Tolling Mode */
	public String getMode() {
		return mode;
	}

	/** Get Tolling zones */
	public String[] getZones() {
		return zones;
	}

	@Override
	public Integer getDefaultCharCntX() {
		// TODO Auto-generated method stub
		return null;
	}
}
