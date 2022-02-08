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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.dms.GraphicListCellRenderer;

/**
 * ComboBox containing graphics for use in the WYSIWYG DMS Message Editor.
 *
 * @author Gordon Parikh - SRF Consulting
 */

@SuppressWarnings("serial")
public class WGraphicMenu extends JComboBox<Graphic> {
	
	/** Controller for interacting with message being edited. */
	private WController controller;
	
	/** List of graphics supported by the current sign. */
	private DefaultComboBoxModel<Graphic> supportedGraphics;
	
	public WGraphicMenu(WController c) {
		controller = c;
		
		// get the list of supported graphics from the controller
		supportedGraphics = controller.getGraphicModel();

		// use the list as our model
		setModel(supportedGraphics);
		
		// use a popup menu listener to update the graphic list when the menu
		// is opened
		addPopupMenuListener(graphicMenuListener);
		
		// use a renderer that shows a preview of the graphic
		setRenderer(new WGraphicListCellRenderer());
	}
	
	/** Listener for updating graphic list when popup menu is opened */
	private PopupMenuListener graphicMenuListener = new PopupMenuListener() {
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			// update the list of supported graphics - this should carry to
			// the combobox
			controller.updateGraphicModel();
		}
		
		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) { }
		public void popupMenuCanceled(PopupMenuEvent arg0) { }
	};
	
	private class WGraphicListCellRenderer extends GraphicListCellRenderer {
		public WGraphicListCellRenderer() {
			cell.setPreferredSize(new Dimension(200, 30));
			cell.setHorizontalTextPosition(SwingConstants.LEFT);
		}
		
		/** Modified cell renderer to scale images. */
		@Override
		public Component getListCellRendererComponent(
			JList<? extends Graphic> list, Graphic g, int index,
			boolean isSelected, boolean hasFocus)
		{
			String v = (g != null) ? Integer.toString(g.getGNumber()) : "";
			cell.getListCellRendererComponent(list, v, index, isSelected,
				hasFocus);
			Image im = null;
			if (g != null) {
				im = createImage(g).getScaledInstance(
					-1, 25, BufferedImage.SCALE_DEFAULT);
			}
			cell.setIcon((im != null) ? new ImageIcon(im) : null);
			return cell;
		}
	}
	
	@Override
	public Graphic getSelectedItem() {
		return (Graphic) super.getSelectedItem();
	}
	
	public int getGraphicCount() {
		if (supportedGraphics != null)
			return supportedGraphics.getSize();
		return 0;
	}
}
