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
 * WAction - WYSIWYG Message Editor Action
 * 
 * Abstract class for implementing an editing action in the WYSIWYG message
 * editor, e.g. adding/removing text, moving rectangles, adding/moving/
 * deleting pages, etc.
 * 
 * Subclasses must implement the perform() method to perform the action, as
 * well as the undo() method to undo the action, allowing users to undo and
 * re-do edits.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

abstract public class WAction {
	
	/** Base constructor */
	public WAction() {
		// TODO
	}
	
	/** Perform the action-specific edit */
	abstract public void perform();
	
	/** Undo the action-specific edit */
	abstract public void undo();
	
	// TODO We probably need more here 
}
