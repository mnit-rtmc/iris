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

package us.mn.state.dot.tms.client.wysiwyg.editor.action;

import java.awt.event.ActionEvent;

import us.mn.state.dot.tms.client.wysiwyg.editor.WAction;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;

/**
 * WDeleteToken - WYSIWYG Message Editor Action to delete a token.
 * 
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WDeleteToken extends WAction {
	
	/** Page being edited */
	private WPage page;
	
	/** Token being removed
	 * TODO how to handle multiple tokens? (override constructor, then what? */
	private WToken tok;
	
	/** Index of the token before removal (for undoing insert) */
	private int tokIndx;
	
	/** Initialize the action to remove a single token */
	public WDeleteToken(WPage pg, WToken t) {
		super();
		
		// save everything we need to delete the token
		page = pg;
		tok = t;
		
		// get the current index of the token in case we need to put it back
		// later
		// TODO how should we handle if the token wasn't found?? (i.e. we get -1) 
		tokIndx = page.getTokenIndex(tok);
	}
	
	/** Perform the action by removing the token from the page. */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!done) {
			page.removeToken(tokIndx);
			markDone();
		}
	}
	
	/** Undo the action by re-inserting the token back in it's original place */
	public void undo(ActionEvent e) {
		if (done) {
			page.addToken(tokIndx, tok);
			markUnDone();
		}
	}
}
