/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

/** Interface used to watch for message changes.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public interface WMsgWatcher {
	/** Called when more than a page is changed.
	 * (Called after all page-renders have been updated.) */
	public void msgChanged(WMessage wm);
	
	/** Called when a page is changed.
	 * (Called after page-render has been updated.) */
	public void pageChanged(WMessage wm, int pageNo);
}
