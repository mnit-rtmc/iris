/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing comm links.
 *
 * @author Douglas Lau
 */
public class CommLinkForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(CommLink.SONAR_TYPE) &&
		       s.canRead(Controller.SONAR_TYPE);
	}

	/** User session */
	private final Session session;

	/** Comm link type cache */
	private final TypeCache<CommLink> comm_links;

	/** Proxy watcher */
	private final ProxyWatcher<CommLink> watcher;

	/** Proxy view for selected comm link */
	private final ProxyView<CommLink> view = new ProxyView<CommLink>() {
		public void enumerationComplete() { }
		public void update(CommLink cl, String a) {
			if (a == null || a.equals("status"))
				link_status.setText(cl.getStatus());
		}
		public void clear() {
			link_status.setText("");
		}
	};

	/** Comm link table panel */
	private final ProxyTablePanel<CommLink> link_pnl;

	/** Clear action */
	private final IAction clear_act = new IAction(
		"comm.link.clear")
	{
		protected void doActionPerformed(ActionEvent e) {
			link_pnl.selectProxy(null);
		}
	};

	/** Clear comm link filter button */
	private final JButton clear_btn = new JButton();

	/** Comm link label */
	private final JLabel link_lbl = new ILabel("comm.link.selected");

	/** Comm link status */
	private final JLabel link_status = new JLabel();

	/** Table panel for controllers */
	private final ControllerPanel controller_pnl;

	/** Create a new comm link form */
	public CommLinkForm(Session s) {
		super(I18N.get("comm.links"));
		session = s;
		comm_links = s.getSonarState().getConCache().getCommLinks();
		watcher = new ProxyWatcher<CommLink>(comm_links, view, false);
		link_pnl = new ProxyTablePanel<CommLink>(new CommLinkModel(s)) {
			protected void selectProxy() {
				selectCommLink();
				super.selectProxy();
			}
		};
		controller_pnl = new ControllerPanel(s);
	}

	/** Initializze the widgets in the form */
	@Override
	protected void initialize() {
		super.initialize();
		watcher.initialize();
		link_pnl.initialize();
		controller_pnl.initialize();
		clear_btn.setAction(clear_act);
		layoutPanel();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		watcher.dispose();
		link_pnl.dispose();
		controller_pnl.dispose();
		super.dispose();
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
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		hg.addComponent(link_pnl);
		GroupLayout.SequentialGroup g0 = gl.createSequentialGroup();
		g0.addComponent(clear_btn);
		g0.addGap(UI.hgap);
		g0.addComponent(link_lbl);
		g0.addGap(UI.hgap);
		g0.addComponent(link_status);
		hg.addGroup(g0);
		hg.addComponent(controller_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		vg.addComponent(link_pnl);
		GroupLayout.ParallelGroup g0 = gl.createBaselineGroup(false,
			false);
		g0.addComponent(clear_btn);
		g0.addComponent(link_lbl);
		g0.addComponent(link_status);
		vg.addGroup(g0);
		vg.addComponent(controller_pnl);
		return vg;
	}

	/** Change the selected comm link */
	private void selectCommLink() {
		CommLink cl = link_pnl.getSelectedProxy();
		watcher.setProxy(cl);
		controller_pnl.setCommLink(cl);
	}
}
