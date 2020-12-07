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
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;

/**
 * Renderer for a DMS message page in a list.
 *
 * @author Gordon Parikh - SRF Consulting
 */
public class WPageListRenderer extends JPanel implements ListCellRenderer<WPage> {

	/** List cell renderer (needed for colors) */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Title panel */
	private final JPanel title_pnl = new JPanel();

	/** Sign ID label */
	private final JLabel pgnum_lbl = new JLabel();

	/** Image panel to display sign message */
	private WImagePanel signPanel;
	
	/** Location panel */
	private final JPanel info_pnl = new JPanel();

	/** Sign location label */
	private final JLabel info_lbl = new JLabel();
	
	public WPageListRenderer() {
		super(new BorderLayout());
		setOpaque(true);
		initialize();
	}

	/** Initialize a large size DMS cell renderer
	 *  TODO alignment isn't doing what I want it to..... 
	 *  */
	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(UI.cellRendererBorder());
		JPanel spPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		signPanel = new WImagePanel(380,120, true);
		spPanel.add(signPanel);
		setPreferredSize();
		title_pnl.setLayout(new BoxLayout(title_pnl, BoxLayout.X_AXIS));
		title_pnl.add(Box.createHorizontalGlue());
		title_pnl.add(pgnum_lbl);
		title_pnl.add(Box.createHorizontalGlue());
		info_pnl.setLayout(new BoxLayout(info_pnl, BoxLayout.X_AXIS));
		info_pnl.add(Box.createHorizontalGlue());
		info_pnl.add(info_lbl);
		info_pnl.add(Box.createHorizontalGlue());
		add(title_pnl, BorderLayout.NORTH);
		add(spPanel, BorderLayout.CENTER);
		add(info_pnl, BorderLayout.SOUTH);
	}

	private void setPreferredSize() {
		Dimension pix_pnl_size = signPanel.getPreferredSize();
		int width = pix_pnl_size.width;
		// set a dummy page number so we can get a preferred height for the 
		// labels
		pgnum_lbl.setText("Page 1");
		int height = 2*pgnum_lbl.getPreferredSize().height+10 + pix_pnl_size.height;
		Dimension d = new Dimension(width, height);
		setMinimumSize(d);
		setPreferredSize(d);
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
		} else {
			title_pnl.setBackground(pgnum_lbl.getBackground());
			signPanel.setBackground(pgnum_lbl.getBackground());
			info_pnl.setBackground(pgnum_lbl.getBackground());
			setBackground(pgnum_lbl.getBackground());
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
		double pgOn = ((double) sp.getPageOn())/10;
		double pgOff = ((double) sp.getPageOff())/10;
		
		String pgOnStr = String.format(
				I18N.get("wysiwyg.editor.page_on"), pgOn);
		String pgOffStr = String.format(
				I18N.get("wysiwyg.editor.page_off"), pgOff);
		return String.format("%s   %s", pgOnStr, pgOffStr);
	}
}
