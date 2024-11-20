/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayMatcher;
import us.mn.state.dot.tms.client.Session;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A tab for editing day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanTab extends JPanel {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(DayPlan.SONAR_TYPE);
	}

	/** User session */
	private final Session session;

	/** Day matcher type cache */
	private final TypeCache<DayMatcher> cache;

	/** Listener for day matcher events */
	private final ProxyListener<DayMatcher> listener =
		new ProxyListener<DayMatcher>()
	{
		boolean notify = false;
		public void proxyAdded(DayMatcher proxy) {
			if (notify)
				updateMatchers();
		}
		public void enumerationComplete() {
			notify = true;
		}
		public void proxyRemoved(DayMatcher proxy) {
			if (notify)
				updateMatchers();
		}
		public void proxyChanged(DayMatcher proxy, String a) { }
	};

	/** Day plan table panel */
	private final DayPlanPanel day_pnl;

	/** Day matcher table panel */
	private final DayMatcherPanel dm_pnl;

	/** Calendar panel */
	private final CalendarPanel cal_pnl = new CalendarPanel();

	/** Create a new day plan tab */
	public DayPlanTab(Session s) {
		session = s;
		cache = s.getSonarState().getDayMatchers();
		day_pnl = new DayPlanPanel(s) {
			@Override
			protected void selectProxy() {
				super.selectProxy();
				selectDayPlan();
			}
		};
		dm_pnl = new DayMatcherPanel(s);
	}

	/** Update day matchers */
	private void updateMatchers() {
		runSwing(new Runnable() {
			public void run() {
				cal_pnl.repaint();
			}
		});
	}

	/** Initialize the panel */
	public void initialize() {
		setBorder(UI.border);
		day_pnl.initialize();
		dm_pnl.initialize();
		cache.addProxyListener(listener);
		layoutPanel();
		selectDayPlan();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup pg = gl.createParallelGroup();
		GroupLayout.SequentialGroup sg = gl.createSequentialGroup();
		sg.addComponent(day_pnl);
		sg.addGap(UI.hgap);
		sg.addComponent(dm_pnl);
		pg.addGroup(sg);
		GroupLayout.SequentialGroup s2 = gl.createSequentialGroup();
		s2.addGap(UI.hgap, UI.hgap, 600);
		s2.addComponent(cal_pnl);
		pg.addGroup(s2);
		return pg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup sg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup pg = gl.createParallelGroup(
			GroupLayout.Alignment.BASELINE);
		pg.addComponent(day_pnl);
		pg.addComponent(dm_pnl);
		sg.addGroup(pg);
		sg.addGap(UI.vgap);
		sg.addComponent(cal_pnl);
		return sg;
	}

	/** Dispose of the panel */
	public void dispose() {
		cache.removeProxyListener(listener);
		dm_pnl.dispose();
		day_pnl.dispose();
	}

	/** Select a day plan */
	private void selectDayPlan() {
		DayPlan dp = day_pnl.getSelectedProxy();
		dm_pnl.setModel(new DayMatcherModel(session, dp));
		cal_pnl.selectDayPlan(dp);
	}
}
