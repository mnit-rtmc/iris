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
 * MULTI string state interface.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public interface MultiStringState {

	/** Add a page */
	void addPage();

	/** Set the page times.
	 * @param pt_on Page on time (tenths of second; null means default)
	 * @param pt_off Page off time (tenths of second; null means default) */
	void setPageTimes(Integer pt_on, Integer pt_off);

	/** Set the page justification */
	void setJustificationPage(MultiString.JustificationPage jp);

	/** Add a new line.
	 * @param spacing Pixel spacing (null means use font spacing) */
	void addLine(Integer spacing);

	/** Set the line justification */
	void setJustificationLine(MultiString.JustificationLine jl);

	/** Set the page background color */
	void setPageBackground(int r, int g, int b);

	/** Set the foreground color */
	void setColorForeground(int r, int g, int b);

	/** Set the text rectangle */
	void setTextRectangle(int x, int y, int w, int h);

	/** Set the font number.
	 * @param f_num Font number (1 to 255)
	 * @param f_id Font version ID (4-digit hex) */
	void setFont(int f_num, String f_id);

	/** Add a span of text */
	void addSpan(String span);

	/** Add a graphic */
	void addGraphic(int g_num, Integer x, Integer y, String g_id);

	/** Add a travel time destination */
	void addTravelTime(String sid);

	/** Add a speed advisory */
	void addSpeedAdvisory();

	/** Add a feed message */
	void addFeed(String fid);
}
