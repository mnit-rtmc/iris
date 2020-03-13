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

import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WRaster;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;



// TODO NOTE MAY DELETE THIS!! (WState might be better, not sure...)


/**
 * Class for managing caret (text cursor) state in the WYSIWYG DMS Message
 * Editor
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WCaret {
	
	private WToken currentTok;
	
	private int lastClickX;
	private int lastClickY;
	
	private int mousePosX;
	private int mousePosY;
	
	private boolean mouseButtonLeft;
	private boolean mouseButtonRight;
	private boolean mouseButtonMiddle;
	
	private int cursorX;
	private int cursorY;
	private int cursorW;
	private int cursorH;
	
	// TODO keyboard stuff
	
	
	
}