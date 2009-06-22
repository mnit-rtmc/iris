/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008 Minnesota Department of Transportation
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
import javax.swing.JToolBar;
import javax.swing.JPanel;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toolbar.AwsStatusPanel;
import us.mn.state.dot.tms.client.toolbar.CoordinatePanel;

/**
 * This status bar contains JPanel components such as the real-time map 
 * coordinates, AWS status, etc.
 * @see CoordinatePanel, AwsStatusPanel
 *
 * @author Michael Darter
 * @company AHMCT
 * @created November 18, 2008
 */
public class IrisToolBar extends JToolBar
{
	/** coordinate panel */
	protected CoordinatePanel m_cp;

	/** AWS panel */
	protected AwsStatusPanel m_ap;

	/** sonar state */
	final SonarState m_st;

	/** Constructor */
	public IrisToolBar(MapBean m, SonarState st, SmartDesktop desktop) {
		super();
		m_st = st;
		add(buildComponents(m, desktop));
	}

	/** Build toolbar components */
	protected JPanel buildComponents(MapBean m, SmartDesktop desktop) {
		JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if(m_st == null)
			return ret;

		// always display coordinates
		m_cp = new CoordinatePanel(m);
		ret.add(m_cp);

		// optional AWS panel
		if(SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			m_ap = new AwsStatusPanel(m_st, desktop);
			ret.add(m_ap);
		}
		return ret;
	}

	/** cleanup */
	public void dispose() {
		if(m_cp != null)
			m_cp.dispose();
		if(m_ap != null)
			m_ap.dispose();
	}
}
