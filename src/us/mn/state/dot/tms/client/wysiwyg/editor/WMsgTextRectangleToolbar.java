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

/**
 * WYSIWYG DMS Message Editor Text Rectangle Option Panel containing buttons
 * with various options for text rectangle mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgTextRectangleToolbar extends WMsgTextToolbar {
	
	public WMsgTextRectangleToolbar(WController c) {
		super(c, false);
		
		addMoveRegionForwardButton();
		addMoveRegionBackwardButton();
	}
}