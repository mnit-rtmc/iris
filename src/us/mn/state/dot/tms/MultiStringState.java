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

	/** Called by parse methods to indicate span update is complete. */
	public void spanComplete();

	/** Set multiple span fields.
	 *  @param page Page number, zero based.
	 *  @param justp Page justification.
	 *  @param line Line number, zero based.
	 *  @param justl Line justification.
	 *  @param f_num Font number, one based.
	 *  @param span Message text.
	 *  @param pont Page on time, 1/10 secs.
	 *  @param pofft Page off time, 1/10 secs. */
	void setFields(int page, MultiString.JustificationPage justp, 
		int line, MultiString.JustificationLine justl,
		int f_num, String span, int pont, int pofft);

	/** add graphic info */
	void addGraphic(int g_num, int x, int y);
}
