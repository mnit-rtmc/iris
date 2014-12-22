/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
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
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing comm links
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
		public void update(CommLink cl, String a) {
			if (a == null || a.equals("status"))
				link_status.setText(cl.getStatus());
		}
		public void clear() {
			link_status.setText("");
		}
	};

	/** Tabbed pane */
	private final JTabbedPane tab = new JTabbedPane();

	/** Comm link table panel */
	private final ProxyTablePanel<CommLink> link_pnl;

	/** Comm link label */
	private final JLabel link_lbl = new ILabel("comm.link.selected");

	/** Comm link status */
	private final JLabel link_status = new JLabel();

	/** Table model for controllers */
	private final ProxyTablePanel<Controller> ctrl_pnl;

	/** Table panel for controllers */
	private final ProxyTablePanel<Controller> controller_pnl;

	/** Table to hold failed controllers */
	private final ZTable ftable = new ZTable();

	/** Failed controller table model */
	private final FailedControllerModel fmodel;

	/** Table row sorter */
	private final TableRowSorter<FailedControllerModel> sorter;

	/** Action to go to a failed controller */
	private final IAction go_ctrl = new IAction("controller.go") {
		protected void doActionPerformed(ActionEvent e) {
			goFailedController();
		}
	};

	/** Go button */
	private final JButton go_btn = new JButton(go_ctrl);

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
		ctrl_pnl = new ProxyTablePanel<Controller>(
			new ControllerModel(s, null));
		controller_pnl = new ProxyTablePanel<Controller>(
			new ControllerTableModel(s));
		fmodel = new FailedControllerModel(s);
		sorter = new TableRowSorter<FailedControllerModel>(fmodel);
		sorter.setSortsOnUpdates(true);
		LinkedList<RowSorter.SortKey> keys =
			new LinkedList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
		sorter.setSortKeys(keys);
	}

	/** Initializze the widgets in the form */
	@Override
	protected void initialize() {
		super.initialize();
		watcher.initialize();
		link_pnl.initialize();
		ctrl_pnl.initialize();
		controller_pnl.initialize();
		fmodel.initialize();
		tab.add(I18N.get("comm.link.all"), createCommLinkPanel());
		tab.add(I18N.get("controller"), controller_pnl);
		tab.add(I18N.get("controller.failed"),
			createFailedControllerPanel());
		add(tab);
		setBackground(Color.LIGHT_GRAY);
		createJobs();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		watcher.dispose();
		link_pnl.dispose();
		ctrl_pnl.dispose();
		controller_pnl.dispose();
		fmodel.dispose();
		super.dispose();
	}

	/** Create jobs */
	private void createJobs() {
		ListSelectionModel fs = ftable.getSelectionModel();
		fs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ftable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1)
					go_btn.doClick();
			}
		});
	}

	/** Create comm link panel */
	private JPanel createCommLinkPanel() {
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		p.setLayout(gl);
		return p;
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		hg.addComponent(link_pnl);
		hg.addGroup(gl.createSequentialGroup().addComponent(
			link_lbl).addGap(UI.hgap).addComponent(link_status));
		hg.addComponent(ctrl_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		vg.addComponent(link_pnl);
		GroupLayout.ParallelGroup g0 = gl.createBaselineGroup(false,
			false);
		g0.addComponent(link_lbl);
		g0.addComponent(link_status);
		vg.addGroup(g0);
		vg.addComponent(ctrl_pnl);
		return vg;
	}

	/** Change the selected comm link */
	private void selectCommLink() {
		CommLink cl = link_pnl.getSelectedProxy();
		watcher.setProxy(cl);
		ctrl_pnl.setModel(new ControllerModel(session, cl));
	}

	/** Create the failed controller panel */
	private JPanel createFailedControllerPanel() {
		ftable.setModel(fmodel);
		ftable.setAutoCreateColumnsFromModel(false);
		ftable.setColumnModel(fmodel.createColumnModel());
		ftable.setRowSorter(sorter);
		IPanel p = new IPanel();
		p.add(ftable, Stretch.FULL);
		p.add(go_btn, Stretch.RIGHT);
		return p;
	}

	/** Go to the failed controller (on the main tab) */
	private void goFailedController() {
		int row = ftable.getSelectedRow();
		if (row >= 0) {
			int mrow = ftable.convertRowIndexToModel(row);
			if (mrow >= 0) {
				Controller c = fmodel.getRowProxy(mrow);
				if (c != null)
					goController(c);
			}
		}
	}

	/** Go to the specified controller (on the main tab) */
	private void goController(Controller c) {
		link_pnl.selectProxy(c.getCommLink());
		tab.setSelectedIndex(0);
	}
}
