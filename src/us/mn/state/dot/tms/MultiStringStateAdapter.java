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
abstract public class MultiStringStateAdapter implements MultiStringState {

	/** Page number, zero based */
	protected int ms_page = 0;

	/** Add a page */
	public void addPage() {
		ms_page++;
		ms_line = 0;
	}

	/** Page justification */
	protected MultiString.JustificationPage ms_justp =
		MultiString.JustificationPage.fromOrdinal(
			SystemAttrEnum.DMS_DEFAULT_JUSTIFICATION_PAGE.getInt());

	/** Set the page justification */
	public void setPageJustification(MultiString.JustificationPage jp) {
		ms_justp = jp;
	}

	/** Line number on page, zero based */
	protected int ms_line = 0;

	/** Add a line */
	public void addLine() {
		ms_line++;
	}

	/** Line justification */
	protected MultiString.JustificationLine ms_justl =
		MultiString.JustificationLine.fromOrdinal(
			SystemAttrEnum.DMS_DEFAULT_JUSTIFICATION_LINE.getInt());

	/** Set the line justification */
	public void setLineJustification(MultiString.JustificationLine jl) {
		ms_justl = jl;
	}

	/** Font number */
	protected int ms_fnum;

	/** Span text */
	protected String ms_span;

	/** Page on-time in tenths */
	protected int ms_pont;

	/** Page off-time in tenths */
	protected int ms_pofft;

	/* Set multiple span fields.
	 * @param f_num Font number, one based.
	 * @param span Message text.
	 * @param pont Page on time, 1/10 secs.
	 * @param pofft Page off time, 1/10 secs. */
	// FIXME: remove this method and use setters for each field
	public void setFields(int fnum, String span, int pont, int pofft) {
		ms_fnum = fnum;
		ms_span = span;
		ms_pont = pont;
		ms_pofft = pofft;
	}

	/** Add graphic tag fields */
	public void addGraphic(int g_num, int x, int y) {
	}
}
