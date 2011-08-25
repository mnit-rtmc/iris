/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
	public void setJustificationPage(MultiString.JustificationPage jp) {
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

	/** Add a new line */
	public void addLine(Integer spacing) {
		ms_line++;
	}

	/** Line justification */
	protected MultiString.JustificationLine ms_justl;

	/** Set the line justification */
	public void setJustificationLine(MultiString.JustificationLine jl) {
		ms_justl = jl;
	}

	/** Foreground color */
	protected DmsColor ms_foreground = DmsColor.AMBER;

	/** Set the foreground color */
	public void setColorForeground(int r, int g, int b) {
		ms_foreground = new DmsColor(r, g, b);
	}

	/** Set the text rectangle */
	public void setTextRectangle(int x, int y, int w, int h) {
		// subclass must handle
	}

	/** Font number */
	protected int ms_fnum;

	/** Set the font number.
	 * @param f_num Font number (1 to 255)
	 * @param f_id Font version ID (4-digit hex) */
	public void setFont(int f_num, String f_id) {
		ms_fnum = f_num;
	}

	/** Create a new MULTI string adapter */
	public MultiStringStateAdapter() {
		ms_page = 0;
		ms_justp = MultiString.JustificationPage.DEFAULT;
		ms_line = 0;
		ms_justl = MultiString.JustificationLine.DEFAULT;
		ms_fnum = 1;
	}

	/** Add a span of text */
	public void addSpan(String span) {
		// subclass must handle
	}

	/** Add a graphic */
	public void addGraphic(int g_num, Integer x, Integer y, String g_id) {
		// subclass must handle
	}

	/** Add a travel time destination */
	public void addTravelTime(String sid) {
		// subclass must handle
	}

	/** Add a speed advisory */
	public void addSpeedAdvisory() {
		// subclass must handle
	}

	/** Add a feed message */
	public void addFeed(String fid) {
		// subclass must handle
	}
}
