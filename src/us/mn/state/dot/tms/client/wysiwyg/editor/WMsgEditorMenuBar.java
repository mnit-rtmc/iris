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

import javax.swing.JMenuBar;

import us.mn.state.dot.tms.client.widget.IMenu;

/**
 * WYSIWYG DMS Message Editor menu bar
 *
 * @author Gordon Parikh
 */

public class WMsgEditorMenuBar extends JMenuBar {
	/** NOTE These are pretty much all placeholders now */
	
	/* File menu */ 
	private IMenu file_menu;
	
	/* Edit menu */ 
	private IMenu edit_menu;
	
	/* View menu */ 
	private IMenu view_menu;
	
	/* Text menu */ 
	private IMenu text_menu;
	
	/* Graphic menu */ 
	private IMenu graphic_menu;
		
	/* MULTI menu */ 
	private IMenu multi_menu;
	
	/* Help menu */ 
	private IMenu help_menu;
	
	/** Create a new menu bar */
	public WMsgEditorMenuBar() {
		file_menu = new WMsgFileMenu();
		edit_menu = new WMsgEditMenu();
		view_menu = new WMsgViewMenu();
		text_menu = new WMsgTextMenu();
		graphic_menu = new WMsgGraphicMenu();
		multi_menu = new WMsgMultiMenu();
		help_menu = new WMsgHelpMenu();
		add(file_menu);
		add(edit_menu);
		add(view_menu);
		add(text_menu);
		add(graphic_menu);
		add(multi_menu);
		add(help_menu);
	}
}