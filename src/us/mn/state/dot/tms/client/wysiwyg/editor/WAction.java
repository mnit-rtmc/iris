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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * WAction - WYSIWYG Message Editor Action
 * 
 * Abstract class for implementing an editing action in the WYSIWYG message
 * editor, e.g. adding/removing text, moving rectangles, adding/moving/
 * deleting pages, etc.
 * 
 * Subclasses must implement the actionPerformed() method to perform the
 * action, as well as the undo() method to undo the action, allowing users
 * to undo and re-do edits.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

abstract public class WAction extends AbstractAction {
	
	/** Keep track of the state of the action (done or not) */
	protected boolean done = false;
	
	/** Base constructor */
	public WAction() {
		// TODO may want/need to do more here
	}
	
	/** Perform the action-specific edit */
	abstract public void actionPerformed(ActionEvent e);
	
	/** Undo the action-specific edit */
	abstract public void undo(ActionEvent e);
	
	public void markDone() {
		done = true;
	}
	
	public void markUnDone() {
		done = false;
	}
	
	public boolean isDone() {
		return done;
	}
	
	// TODO We probably need more here 
}
