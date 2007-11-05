/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.lcs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.tms.Roadway;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.device.TmsJList;
import us.mn.state.dot.tms.client.proxy.LocationProxy;
import us.mn.state.dot.tms.client.proxy.RefreshListener;

/**
 * The LCSChooser class provides a GUI for selecting LaneControlSignal objects.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LcsChooser extends JPanel {

	/** The color of the title bar when the icon is not selected */
	static protected final Color TITLE_COLOR = new Color(218, 218, 239);

	/** The Color of the title bar when the icon is selected */
	static protected final Color SELECTED_COLOR = Color.YELLOW;

	/** Create a new LCS chooser */
	public LcsChooser(LcsHandler handler) {
		super(new GridLayout(1, 2));
		add(createLcsList(handler, Roadway.WEST));
		add(createLcsList(handler, Roadway.EAST));
		handler.addRefreshListener(new RefreshListener() {
			public void dataChanged() {
				repaint();
			}
		});
	}

	/** Create a list component for one direction of LCS */
	protected JList createLcsList(LcsHandler handler, short dir) {
		TmsJList l = new TmsJList(handler);
		l.setBorder(BorderFactory.createTitledBorder(
			TMSObject.DIR_LONG[dir]));
		l.setModel(handler.getDirectionModel(dir));
		l.setCellRenderer(new LcsCellRenderer());
		l.setBackground(getBackground());
		return l;
	}

	/** Dispose of the LCS chooser */
	public void dispose() {
		removeAll();
	}

	/** Renderer used to paint LCS objects in the list */
	protected class LcsCellRenderer extends JPanel
		implements ListCellRenderer
	{
		private final JLabel id = new JLabel();

		private final JPanel titleBar = new JPanel(new BorderLayout());

		private final LcsPanel lcsPanel = new LcsPanel(30);

		/** Create a new LCS cell renderer */
		public LcsCellRenderer() {
			super( new BorderLayout() );
			setBorder(BorderFactory.createRaisedBevelBorder());
			titleBar.setBorder(BorderFactory.createEmptyBorder(
				0, 4, 0, 4));
			titleBar.add(id, BorderLayout.WEST);
			add(titleBar, BorderLayout.NORTH);
			JPanel panel = new JPanel();
			panel.add(lcsPanel);
			add(panel, BorderLayout.CENTER);
		}

		/**
		 * Gets the listCellRendererComponent attribute of the
		 * LcsCellRenderer object
		 *
		 * @param list          JList needing the rendering
		 * @param value         The object to render.
		 * @param index         The List index of the object to render.
		 * @param isSelected    Is the object selected?
		 * @param cellHasFocus  Does the object have focus?
		 * @return              Component to use for rendering the
		 * LaneControlSignal.
		 */
		public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected,
			boolean cellHasFocus)
		{
			if(isSelected)
				titleBar.setBackground(SELECTED_COLOR);
			else
				titleBar.setBackground(TITLE_COLOR);
			if(value instanceof LcsProxy)
				renderLcs((LcsProxy)value);
			return this;
		}

		protected void renderLcs(LcsProxy lcs) {
			LocationProxy loc = (LocationProxy)lcs.getLocation();
			id.setText(lcs.getId() + " - " +
				loc.getCrossDescription());
			lcsPanel.setLcs(lcs);
		}
	}
}
