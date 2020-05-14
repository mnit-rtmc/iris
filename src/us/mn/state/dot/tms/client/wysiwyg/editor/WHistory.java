/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import us.mn.state.dot.tms.DmsColor;

/**
 * WHistory - WYSIWYG Message Editor History
 * 
 * Class for saving state before an editing action in the WYSIWYG message
 * editor (e.g. adding/removing text, moving rectangles, adding/moving/
 * deleting pages, etc.). Saves the previous MULTI string, along with caret
 * placement (and TODO eventually/maybe other things).
 * 
 * Used for undo/redo functionality (so sometimes it's history in the future).
 * 
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WHistory {
	
	/** MULTI String containing message state */
	private String multiString;
	
	/** Caret location (and selection size, if applicable) */
	private int caretIndx;
	private int selectionLength;
	
	/** Selected page for when pages are added/removed */
	private int selectedPageIndx;
	
	/** Current foreground/background color */
	private DmsColor fgColor;
	private DmsColor bgColor;
	
	/** Justification setting */
	
	
	public WHistory(String ms, int ci, int sl) {
		multiString = ms;
		caretIndx = ci;
		selectionLength = sl;
	}

	public WHistory(String ms, int ci, int sl, int spg) {
		multiString = ms;
		caretIndx = ci;
		selectionLength = sl;
		selectedPageIndx = spg;
	}
	
	public WHistory(String ms, int ci, int sl, int spg, DmsColor fgc,
			DmsColor bgc) {
		multiString = ms;
		caretIndx = ci;
		selectionLength = sl;
		selectedPageIndx = spg;
		fgColor = fgc;
		bgColor = bgc;
	}
	
	public String getMultiString() {
		return multiString;
	}
	
	public int getCaretIndex() {
		return caretIndx;
	}
	
	public int getSelectionLength() {
		return selectionLength;
	}

	public int getSelectedPageIndex() {
		return selectedPageIndx;
	}
}
