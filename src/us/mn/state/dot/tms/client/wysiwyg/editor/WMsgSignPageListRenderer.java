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
package us.mn.state.dot.tms.client.wysiwyg.editor;

import static us.mn.state.dot.tms.client.widget.Widgets.UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;

/**
 * Renderer for a DMS message page in a list.
 *
 * @author Gordon Parikh - SRF Consulting
 */
public class WMsgSignPageListRenderer extends JPanel implements ListCellRenderer<WPage> {

	/** List cell renderer (needed for colors) */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Title panel */
	private final JPanel title_pnl = new JPanel();

	/** Sign ID label */
	private final JLabel pgnum_lbl = new JLabel();

	/** Image panel to display sign message */
	
//	private final SignPixelPanel pixel_pnl = new SignPixelPanel(50, 200);
	private final WImagePanel signPanel = new WImagePanel(200,50);
	
	/** Location panel */
	private final JPanel info_pnl = new JPanel();

	/** Sign location label */
	private final JLabel info_lbl = new JLabel();
	
	public WMsgSignPageListRenderer() {
		setOpaque(true);
		initialize();
	}

	/** Initialize a large size DMS cell renderer
	 *  TODO alignment isn't doing what I want it to..... 
	 *  */
	private void initialize() {
		setBorder(UI.cellRendererBorder());
		setPreferredSize();
		title_pnl.setLayout(new BoxLayout(title_pnl, BoxLayout.X_AXIS));
		title_pnl.add(Box.createHorizontalGlue());
		title_pnl.add(pgnum_lbl);
//		title_pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
//		pgnum_lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
		info_pnl.setLayout(new BoxLayout(info_pnl, BoxLayout.X_AXIS));
		info_pnl.add(Box.createHorizontalGlue());
		info_pnl.add(info_lbl);
//		info_pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(title_pnl, BorderLayout.NORTH);
		add(signPanel, BorderLayout.CENTER);
		add(info_pnl, BorderLayout.SOUTH);
	}

	private void setPreferredSize() {
		Dimension pix_pnl_size = signPanel.getPreferredSize();
		int width = pix_pnl_size.width;
		// set a dummy page number so we can get a preferred height for the 
		// labels
		pgnum_lbl.setText("Page 1");
		int height = 8*pgnum_lbl.getPreferredSize().height + pix_pnl_size.height;
		System.out.println(String.format("Width = %d, Height = %d", width, height));
		setPreferredSize(new Dimension(width, height));
	}
	
	/** Get a component configured to render a cell of the list */
	@Override
	public Component getListCellRendererComponent(JList<? extends WPage> list,
			WPage sp, int index, boolean isSelected, boolean hasFocus)
	{
		if (isSelected) {
			title_pnl.setBackground(list.getSelectionBackground());
			signPanel.setBackground(list.getSelectionBackground());
			info_pnl.setBackground(list.getSelectionBackground());
			setBackground(list.getSelectionBackground());
//			title_pnl.setForeground(list.getSelectionForeground());
		} else {
			title_pnl.setBackground(pgnum_lbl.getBackground());
			signPanel.setBackground(pgnum_lbl.getBackground());
			info_pnl.setBackground(pgnum_lbl.getBackground());
			setBackground(pgnum_lbl.getBackground());
//			title_pnl.setForeground(pgnum_lbl.getForeground());
		}
		pgnum_lbl.setText(WController.getPageNumberLabel(index));
		renderSignPage(sp);
		return this;
	}

	/** Render the Sign Page */
	private void renderSignPage(WPage sp) {
		signPanel.setPage(sp);
		info_lbl.setText(getPageInfo(sp));
	}
	
	/** Create the page info label from the page on/off time */
	private String getPageInfo(WPage sp) {
		// calculate page on and off time in seconds (WPage gives deciseconds)
		int pgOnInt = sp.getPageOn()/10;
		int pgOffInt = sp.getPageOff()/10;
		
		String pgOnStr = String.format(I18N.get("wysiwyg.editor.page_on"), pgOnInt);
		String pgOffStr = String.format(I18N.get("wysiwyg.editor.page_off"), pgOffInt);
		return String.format("%s   %s", pgOnStr, pgOffStr);
	}
}
