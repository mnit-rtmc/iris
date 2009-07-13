/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * Abstract MULTI string state adapter.
 *
 * Note: Fields in the class use the
 * "ms_" prefix to make it easier to distinguish their origin in subclasses.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class MultiStringStateAdapter implements MultiStringState {

	/** Page number, zero based */
	protected int ms_page;

	/** Add a page */
	public void addPage() {
		ms_page++;
		ms_line = 0;
	}

	/** Page justification */
	protected MultiString.JustificationPage ms_justp;

	/** Set the page justification */
	public void setPageJustification(MultiString.JustificationPage jp) {
		ms_justp = jp;
	}

	/** Page on time (tenths of a second) */
	protected Integer ms_pt_on;

	/** Page off time (tenths of a second) */
	protected Integer ms_pt_off;

	/** Set the page times.
	 * @param pt_on Page on time (tenths of second; null means default)
	 * @param pt_off Page off time (tenths of second; null means default) */
	public void setPageTimes(Integer pt_on, Integer pt_off) {
		ms_pt_on = pt_on;
		ms_pt_off = pt_off;
	}

	/** Line number on page, zero based */
	protected int ms_line;

	/** Add a line */
	public void addLine() {
		ms_line++;
	}

	/** Line justification */
	protected MultiString.JustificationLine ms_justl;

	/** Set the line justification */
	public void setLineJustification(MultiString.JustificationLine jl) {
		ms_justl = jl;
	}

	/** Font number */
	protected int ms_fnum;

	/** Set the font number */
	public void setFont(int fn) {
		ms_fnum = fn;
	}

	/** Create a new MULTI string adapter */
	public MultiStringStateAdapter() {
		ms_page = 0;
		ms_justp = MultiString.JustificationPage.fromOrdinal(
			SystemAttrEnum.DMS_DEFAULT_JUSTIFICATION_PAGE.getInt());
		ms_line = 0;
		ms_justl = MultiString.JustificationLine.fromOrdinal(
			SystemAttrEnum.DMS_DEFAULT_JUSTIFICATION_LINE.getInt());
		ms_fnum = 1;
	}

	/** Add a span of text */
	public void addText(String span) {
	}

	/** Add graphic tag fields */
	public void addGraphic(int g_num, int x, int y) {
	}
}
