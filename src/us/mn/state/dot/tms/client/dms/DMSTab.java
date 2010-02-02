/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
 * Copyright (C) 2010 AHMCT, University of California, Davis
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
package us.mn.state.dot.tms.client.dms;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import java.util.List;
import javax.swing.JPanel;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The DMSTab class provides the GUI for working with DMS objects.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSTab extends MapTab {

	/** DMS dispatcher component */
	protected final DMSDispatcher dispatcher;

	/** Summary of DMSs of each status */
	protected final StyleSummary<DMS> summary;

	/** DMS Manager */
	protected final DMSManager m_manager;

	/** Create a new DMS tab */
 	public DMSTab(Session session, DMSManager manager, 
		List<LayerState> lstates)
	{
		super(I18N.get("dms.abbreviation"), I18N.get("dms.title"));
		m_manager = manager;
		dispatcher = new DMSDispatcher(session, manager);
		summary = manager.createStyleSummary(m_ssclistener);
		for(LayerState ls: lstates) {
			map_model.addLayer(ls);
			String name = ls.getLayer().getName();
			if(name.equals(manager.getProxyType()))
				map_model.setHomeLayer(ls);
		}
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(dispatcher);
		add(summary);
	}

	/** Listener for StyleSummary ComponentListener messages. This object
	 *  listens for resize events for the JPanel in the StyleSummary. */
	private ComponentListener m_ssclistener = new ComponentListener() {
		public void componentHidden(ComponentEvent e) {}
		public void componentMoved(ComponentEvent e) {}
		public void componentShown(ComponentEvent e) {}
		public void componentResized(ComponentEvent e) {
			Object obj = e.getComponent();
			if(obj == null || !(obj instanceof JPanel))
				return;
			Dimension dim_vp = summary.getViewportExtentSize();
			if(DmsCellRenderer.willCellSizeChange(dim_vp)) {
				m_manager.styleSummaryResize(dim_vp);
				summary.updateRenderer();
			}
		}
	};

	/** Get the tab number */
	public int getNumber() {
		return 0;
	}

	/** Dispose of the DMS tab */
	public void dispose() {
		super.dispose();
		dispatcher.dispose();
		summary.dispose();
	}
}
