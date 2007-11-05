/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.rmi.RemoteException;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.PixFont;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.RemoteListModel;

/**
 * PixFontListForm
 *
 * @author Douglas Lau
 */
public class PixFontListForm extends IndexedListForm {

	/** Frame title */
	static private final String TITLE = "Fonts";

	/** Create a new font list form */
	public PixFontListForm(TmsConnection tc) {
		super(TITLE, tc, tc.getProxy().getFonts(),
			Icons.getIcon("font"));
	}

	/** Initializze the widgets in the form */
	protected void initialize() throws RemoteException {
		add(createListPanel());
		super.initialize();
	}

	/** Edit an item in the list */
	protected void editItem() throws RemoteException {
		int i = list.getList().getSelectedIndex();
		if(i >= 0) {
			int index = i + 1;
			IndexedList fonts = (IndexedList)
				connection.getProxy().getFonts().getList();
			PixFont font = (PixFont)fonts.getElement(index);
			RemoteListModel list = new RemoteListModel(
				font.getCharacterList());
			connection.getDesktop().show(
				new PixFontForm(connection, list, index));
		}
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "   4  ITALIC";
	}
}
