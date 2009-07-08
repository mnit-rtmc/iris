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
abstract public class MultiStringStateAdapter 
	implements MultiStringState
{
	/** Page number, zero based */
	protected int ms_page;

	/* Page justification */
	protected MultiString.JustificationPage ms_justp;

	/* Line number on page, zero based */
	protected int ms_line;

	/* Line justification */
	protected MultiString.JustificationLine ms_justl;

	/* Font number */
	protected int ms_fnum;

	/* Span text */
	protected String ms_span;

	/* Page on-time in tenths */
	protected int ms_pont;

	/* Page off-time in tenths */
	protected int ms_pofft;

	/* Set multiple span fields.
	 * @param page Page number, zero based.
	 * @param justp Page justification.
	 * @param line Line number, zero based.
	 * @param justl Line justification.
	 * @param f_num Font number, one based.
	 * @param span Message text.
	 * @param pont Page on time, 1/10 secs.
	 * @param pofft Page off time, 1/10 secs. */
	// FIXME: remove this method and use setters for each field
	public void setFields(int page, MultiString.JustificationPage justp, 
		int line, MultiString.JustificationLine justl,
		int fnum, String span, int pont, int pofft)
	{
		ms_page = page;
		ms_justp = justp;
		ms_line = line;
		ms_justl = justl;
		ms_fnum = fnum;
		ms_span = span;
		ms_pont = pont;
		ms_pofft = pofft;
	}

	/** Add graphic tag fields. */
	public void addGraphic(int g_num, int x, int y) {
	}
}
