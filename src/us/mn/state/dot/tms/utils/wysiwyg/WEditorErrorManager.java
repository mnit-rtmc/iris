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

package us.mn.state.dot.tms.utils.wysiwyg;

import java.util.ArrayList;
import java.util.Iterator;

import us.mn.state.dot.tms.utils.MultiSyntaxError;

/**
 * Class for managing WYSIWYG renderer errors.
 *
 * @author Gordon Parikh - SRF Consulting
 */
public class WEditorErrorManager {
	public ArrayList<WEditorError> errors;
	
	public WEditorErrorManager() {
		errors = new ArrayList<WEditorError>();
	}
	
	/** Log an error with the manager */
	public void addError(MultiSyntaxError mse) {
		// for now print it
//		System.out.println("RENDERERROR: "+mse);
		errors.add(new WEditorError(mse));
	}
	
	/** Log an error with a token with the manager */
	public void addError(MultiSyntaxError mse, WToken tok) {
		// for now print it
//		System.out.println("RENDERERROR: "+mse);
		errors.add(new WEditorError(mse, tok));
	}
	
	/** Check if there are errors */
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	/** Clear errors */
	public void clearErrors() {
		errors.clear();
	}
	
	/** Print all errors to the terminal */
	public void printErrors() {
		System.out.println(errors.toString());
	}
	
	/** Iterator for all errors */
	public Iterator<WEditorError> iterator() {
		return errors.iterator();
	}
}
