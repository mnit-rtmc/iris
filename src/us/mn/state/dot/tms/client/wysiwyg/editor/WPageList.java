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

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.wysiwyg.WMessage;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;

/**
 * List for displaying a list of WYSIWYG DMS pages.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WPageList extends JList<WPage> {
	
	WMessage wmsg;
	DefaultListModel<WPage> model;
	WPageListRenderer rndr;
	
	/** Create a page list without a selection handler. If enableSelection is
	 *  false, selection is disabled.
	 */
	public WPageList(boolean enableSelection) {
		// initialize the list, then disable selection if requested
		init();
		if (!enableSelection) {
			setSelectionModel(new DisabledSelectionModel());
		}
	}
	
	/** Create a page list that uses the given selection handler. */
	public WPageList(ListSelectionListener lstnr) {
		// initialize the list model then add the provided selection listener
		init();
		getSelectionModel().addListSelectionListener(lstnr);
	}
	
	/** Initialize the page list */
	private void init() {
		// initialize the list model
		model = new DefaultListModel<WPage>();
		setModel(model);
		
		// use a custom cell renderer
		rndr = new WPageListRenderer();
		setCellRenderer(rndr);
		
		// don't allow multi-select
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	/** Update the page list given the MULTI string and MultiConfig */
	public void updatePageList(String multiString, MultiConfig mcfg) {
		// render the message
		if (wmsg == null)
			wmsg = new WMessage(multiString);
		else
			wmsg.parseMulti(multiString);
		
		if (mcfg.isUseable())
			wmsg.renderMsg(mcfg);
		
		// clear the model and refill it with pages (if the message is valid)
		model.clear();
		if (wmsg.isValid()) {
			for (int i = 1; i <= wmsg.getNumPages(); ++i) {
				model.addElement(wmsg.getPage(i));
			}
		}
	}
	
	/** Update the page list given a rendered WMessage. */
	public void updatePageList(WMessage wm) {
		model.clear();
		if (wm != null && wm.isValid()) {
			for (int i = 1; i <= wm.getNumPages(); ++i) {
				model.addElement(wm.getPage(i));
			}
		}
	}
	
	public void clearPageList() {
		model.clear();
	}
	
	/** Get the page in the List Model with index i. If the index is out of
	 *  bounds, null is returned.
	 */
	public WPage getPage(int i) {
		return model.get(i);
	}
	
	/** Return the number of pages in the list. */
	public int getNumPages() {
		return model.getSize();
	}
}
	
/** A class for disabling selection in a list */
@SuppressWarnings("serial")
class DisabledSelectionModel extends DefaultListSelectionModel {
	@Override
	public void setSelectionInterval(int index0, int index1) {
		super.setSelectionInterval(-1, -1);
 	}

	@Override
	public void addSelectionInterval(int index0, int index1) {
		super.setSelectionInterval(-1, -1);
	}
}