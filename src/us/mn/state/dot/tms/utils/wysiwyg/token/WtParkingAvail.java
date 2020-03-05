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

/** Parking-availability token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtParkingAvail extends WToken {

	String pid;
	String l_txt;
	String c_txt;

	/**
	 * @param pid
	 * @param l_txt
	 * @param c_txt
	 */
	public WtParkingAvail(String pid, String l_txt, String c_txt) {
		super(WTokenType.parkingAvail, "[pa");
		this.pid   = pid;
		this.l_txt = l_txt;
		this.c_txt = c_txt;
		updateString();
	}

	/** Get parking ID */
	public String getParkingID() {
		return pid;
	}

	/** Get parking-low text */
	public String getParkingLowText() {
		return l_txt;
	}

	/** Get parking-closed text */
	public String getClosedText() {
		return c_txt;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#updateState(us.mn.state.dot.tms.utils.wysiwyg.WState)
	 */
	@Override
	public WState updateState(WState before) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addParking(pid, l_txt, c_txt);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.addParking(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		if (pid != null) {
			sb.append(pid);
			if (l_txt != null) {
				sb.append(',');
				sb.append(l_txt);
				if (c_txt != null) {
					sb.append(',');
					sb.append(c_txt);
				}
			}
		}
	}
}
