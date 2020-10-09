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

import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.wysiwyg.token.*;

/** WYSIWYG-editor Multi Parser
 * 
 * Parses a MULTI string.  Generates a WMessage containing a list
 * of one or more WPage objects each of which contain a list of
 * WToken objects.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WMultiParser implements Multi {

	private WMessage msg;
	private WPage    page;

	/**
	 * @param msg 
	 * 
	 */
	WMultiParser(WMessage wmsg) {
		this.msg  = wmsg;
		this.page = wmsg.startPage1();
	}

	//===========================================
	// Helper methods
	
	/** Parse a MULTI string into an existing WMessage */
	static public void parseToWMessage(String multiStr, WMessage wMsg) {
		MultiString ms = new MultiString(multiStr);
		WMultiParser parser = new WMultiParser(wMsg);
		ms.parse(parser);
	}

	/** Convert a MULTI string into a new WMessage */
	static public WMessage parse(String multiStr) {
		WMessage wMsg = new WMessage();
		parseToWMessage(multiStr, wMsg);
		return wMsg;
	}

	//===========================================
	// Multi interface methods

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#unsupportedTag(java.lang.String)
	 */
	@Override
	public void unsupportedTag(String tag) {
		page.addToken(new WtUnsupportedTag(tag));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addSpan(java.lang.String)
	 */
	@Override
	public void addSpan(String span) {
		// Convert span string to a series of span characters
		int len = span.length();
		WToken tok;
		for (int i = 0; (i < len); ++i) {
			tok = new WtTextChar(span.charAt(i));
			page.addToken(tok);
		}
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setColorBackground(int)
	 */
	@Override
	public void setColorBackground(Integer x) {
		page.addToken(new WtColorBackground(x));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setPageBackground(int)
	 */
	@Override
	public void setPageBackground(Integer z) {
		page.addToken(new WtPageBackground(z));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setPageBackground(int, int, int)
	 */
	@Override
	public void setPageBackground(int r, int g, int b) {
		page.addToken(new WtPageBackground(r, g, b));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setColorForeground(int)
	 */
	@Override
	public void setColorForeground(Integer x) {
		page.addToken(new WtColorForeground(x));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setColorForeground(int, int, int)
	 */
	@Override
	public void setColorForeground(int r, int g, int b) {
		page.addToken(new WtColorForeground(r, g, b));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addColorRectangle(int, int, int, int, int)
	 */
	@Override
	public void addColorRectangle(int x, int y, int w, int h, int z) {
		page.addToken(new WtColorRectangle(x, y, w, h, z));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addColorRectangle(int, int, int, int, int, int, int)
	 */
	@Override
	public void addColorRectangle(int x, int y, int w, int h, int r, int g, int b) {
		page.addToken(new WtColorRectangle(x, y, w, h, r, g, b));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setFont(int, java.lang.String)
	 */
	@Override
	public void setFont(Integer f_num, String f_id) {
		WToken tok = new WtFont(f_num, f_id);
		page.addToken(tok);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addGraphic(int, java.lang.Integer, java.lang.Integer, java.lang.String)
	 */
	@Override
	public void addGraphic(int g_num, Integer x, Integer y, String g_id) {
		page.addToken(new WtGraphic(g_num, x, y, g_id));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setJustificationLine(us.mn.state.dot.tms.utils.Multi.JustificationLine)
	 */
	@Override
	public void setJustificationLine(JustificationLine jl) {
		page.addToken(new WtJustLine(jl));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setJustificationPage(us.mn.state.dot.tms.utils.Multi.JustificationPage)
	 */
	@Override
	public void setJustificationPage(JustificationPage jp) {
		page.addToken(new WtJustPage(jp));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addLine(java.lang.Integer)
	 */
	@Override
	public void addLine(Integer spacing) {
		page.addToken(new WtNewLine(spacing));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addPage()
	 */
	@Override
	public void addPage() {
		page = msg.newPage();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setPageTimes(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void setPageTimes(Integer pt_on, Integer pt_off) {
		page.addToken(new WtPageTime(pt_on, pt_off));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setCharSpacing(java.lang.Integer)
	 */
	@Override
	public void setCharSpacing(Integer sc) {
		page.addToken(new WtCharSpacing(sc));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#setTextRectangle(int, int, int, int)
	 */
	@Override
	public void setTextRectangle(int x, int y, int w, int h) {
		page.addToken(new WtTextRectangle(x, y, w, h));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addTravelTime(java.lang.String, us.mn.state.dot.tms.utils.Multi.OverLimitMode, java.lang.String)
	 */
	@Override
	public void addTravelTime(String sid, OverLimitMode mode, String o_txt) {
		page.addToken(new WtTravelTime(sid, mode, o_txt));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addSpeedAdvisory()
	 */
	@Override
	public void addSpeedAdvisory() {
		page.addToken(new WtSpeedAdvisory());
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addSlowWarning(int, int, java.lang.String)
	 */
	@Override
	public void addSlowWarning(int spd, int dist, String mode) {
		page.addToken(new WtSlowWarning(spd, dist, mode));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addFeed(java.lang.String)
	 */
	@Override
	public void addFeed(String fid) {
		page.addToken(new WtFeedMsg(fid));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addTolling(java.lang.String, java.lang.String[])
	 */
	@Override
	public void addTolling(String mode, String[] zones) {
		page.addToken(new WtTolling(mode, zones));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addParking(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void addParking(String pid, String l_txt, String c_txt) {
		page.addToken(new WtParkingAvail(pid, l_txt, c_txt));
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.Multi#addLocator(java.lang.String)
	 */
	@Override
	public void addLocator(String code) {
		page.addToken(new WtLocator(code));
	}

	@Override
	public void addCapTime(String f_txt, String a_txt, String p_txt) {
		page.addToken(new WtCapTime(f_txt, a_txt, p_txt));
	}

	@Override
	public void addCapResponse(String[] rtypes) {
		page.addToken(new WtCapResponse(rtypes));
	}

	@Override
	public void addCapUrgency(String[] uvals) {
		page.addToken(new WtCapUrgency(uvals));
	}
}
