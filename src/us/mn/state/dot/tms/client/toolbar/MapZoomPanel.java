/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009 Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toolbar;

import java.awt.FlowLayout;
import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A JPanel that displays map zoom buttons.
 *
 * @author Michael Darter
 * @created June 21, 2009
 */
public class MapZoomPanel extends JPanel
{
	/** Button to view all AWS messages */
	protected final JButton m_btnZoomIn = new JButton("+");
	protected final JButton m_btnZoomOut = new JButton("-");

	/** mapbean */
	final private MapBean m_map;

	/** Constructor */
	public MapZoomPanel(MapBean m) {
		m_map = m;
		createComponents();
		addComponents();
	}

	/** is this control IRIS enabled? */
	public static boolean getIEnabled() {
		return SystemAttrEnum.
			MAP_TOOLBAR_ZOOMBTNS_ENABLE.getBoolean();
	}

	/** create components */
	protected void createComponents() {
		m_btnZoomIn.setToolTipText("Zoom map view in.");
		m_btnZoomOut.setToolTipText("Zoom map view out.");
		new ActionJob(this, m_btnZoomIn) {
			public void perform() throws Exception {
				if(m_map != null)
					m_map.zoom(true);
			}
		};
		new ActionJob(this, m_btnZoomOut) {
			public void perform() throws Exception {
				if(m_map != null)
					m_map.zoom(false);
			}
		};
	}

	/** add components to panel */
	protected void addComponents()
	{
		setLayout(new FlowLayout());
		setBorder(BorderFactory.
			createBevelBorder(BevelBorder.LOWERED));
		add(m_btnZoomIn);
		add(m_btnZoomOut);
	}

	/** cleanup */
	public void dispose() {
	}
}
