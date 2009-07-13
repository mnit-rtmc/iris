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
 * MULTI string state interface.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public interface MultiStringState {

	/** Add a page */
	void addPage();

	/** Set the page justification */
	void setPageJustification(MultiString.JustificationPage jp);

	/** Set the page times.
	 * @param pt_on Page on time (tenths of second; null means default)
	 * @param pt_off Page off time (tenths of second; null means default) */
	void setPageTimes(Integer pt_on, Integer pt_off);

	/** Add a line */
	void addLine();

	/** Set the line justification */
	void setLineJustification(MultiString.JustificationLine jl);

	/** Set the font number.
	 * @param f_num Font number (1 to 255)
	 * @param f_id Font version ID */
	void setFont(int f_num, Integer f_id);

	/** Add a span of text */
	void addText(String span);

	/** Add a graphic */
	void addGraphic(int g_num, Integer x, Integer y, Integer g_id);
}
