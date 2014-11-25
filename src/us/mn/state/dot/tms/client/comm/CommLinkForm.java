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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
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
public class CommLinkForm extends AbstractForm implements ProxyView<CommLink> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(CommLink.SONAR_TYPE) &&
		       s.canRead(Controller.SONAR_TYPE);
	}

	/** Comm link table row height */
	static private final int ROW_HEIGHT = UI.scaled(24);

	/** Tabbed pane */
	private final JTabbedPane tab = new JTabbedPane();

	/** Table model for comm links */
	private final CommLinkModel model;

	/** Table to hold the comm link list */
	private final ZTable table = new ZTable();

	/** Table model for controllers */
	private ControllerModel cmodel;

	/** Table to hold controllers */
	private final ZTable ctable = new ZTable();

	/** Comm link status */
	private final JLabel link_status = new JLabel();

	/** Update one attribute (from ProxyView). */
	@Override
	public void update(CommLink cl, String a) {
		if(a == null || a.equals("status"))
			link_status.setText(cl.getStatus());
	}

	/** Clear all attributes (from ProxyView). */
	@Override
	public void clear() {
		link_status.setText("");
	}

	/** Action to delete the selected comm link */
	private final IAction delete_link = new IAction("comm.link.delete") {
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel s = table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				model.deleteRow(row);
		}
	};

	/** Table to hold failed controllers */
	private final ZTable ftable = new ZTable();

	/** Failed controller table model */
	private final FailedControllerModel fmodel;

	/** Table row sorter */
	private final TableRowSorter<FailedControllerModel> sorter;

	/** Action to show controller properties */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			doPropertiesAction();
		}
	};

	/** Controller properties button */
	private final JButton ctr_props = new JButton(controller);

	/** Action to delete the selected controller */
	private final IAction del_ctr = new IAction("controller.delete") {
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel cs = ctable.getSelectionModel();
			int row = cs.getMinSelectionIndex();
			if(row >= 0)
				cmodel.deleteRow(row);
		}
	};

	/** Action to go to a failed controller */
	private final IAction go_ctrl = new IAction("controller.go") {
		protected void doActionPerformed(ActionEvent e) {
			goFailedController();
		}
	};

	/** Go button */
	private final JButton go_btn = new JButton(go_ctrl);

	/** User session */
	private final Session session;

	/** Proxy watcher */
	private final ProxyWatcher<CommLink> watcher;

	/** Create a new comm link form */
	public CommLinkForm(Session s) {
		super(I18N.get("comm.links"));
		session = s;
		model = new CommLinkModel(s);
		watcher = new ProxyWatcher<CommLink>(
			s.getSonarState().getConCache().getCommLinks(), this,
			false);
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
		model.initialize();
		watcher.initialize();
		fmodel.initialize();
		setLayout(new BorderLayout());
		tab.add(I18N.get("comm.link.all"), createCommLinkPanel());
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
		model.dispose();
		fmodel.dispose();
		if (cmodel != null)
			cmodel.dispose();
		super.dispose();
	}

	/** Create jobs */
	private void createJobs() {
		ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectCommLink();
			}
		});
		ListSelectionModel cs = ctable.getSelectionModel();
		cs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cs.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectController();
			}
		});
		ctable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() > 1)
					ctr_props.doClick();
			}
		});
		ListSelectionModel fs = ftable.getSelectionModel();
		fs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ftable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() > 1)
					go_btn.doClick();
			}
		});
	}

	/** Create comm link panel */
	private JPanel createCommLinkPanel() {
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(ROW_HEIGHT);
		table.setVisibleRowCount(5);
		delete_link.setEnabled(false);
		ctable.setAutoCreateColumnsFromModel(false);
		ctable.setRowHeight(ROW_HEIGHT);
		// NOTE: the width of the controller table is the
		//       same as the comm link table on purpose.
		ctable.setPreferredScrollableViewportSize(new Dimension(
			table.getPreferredSize().width, ROW_HEIGHT * 12));
		controller.setEnabled(false);
		del_ctr.setEnabled(false);
		IPanel p = new IPanel();
		p.add(table, Stretch.FULL);
		p.add("comm.link.selected");
		p.add(link_status);
		p.add(new JButton(delete_link), Stretch.RIGHT);
		p.add(ctable, Stretch.FULL);
		p.add("controller.selected");
		p.add(ctr_props);
		p.add(new JButton(del_ctr), Stretch.RIGHT);
		return p;
	}

	/** Change the selected comm link */
	private void selectCommLink() {
		int row = table.getSelectedRow();
		CommLink cl = model.getProxy(row);
		watcher.setProxy(cl);
		delete_link.setEnabled(model.canRemove(cl));
		del_ctr.setEnabled(false);
		ControllerModel old_model = cmodel;
		cmodel = new ControllerModel(session, cl);
		cmodel.initialize();
		ctable.clearSelection();
		ctable.setModel(cmodel);
		ctable.setColumnModel(cmodel.createColumnModel());
		if(old_model != null)
			old_model.dispose();
	}

	/** Change the selected controller */
	private void selectController() {
		int row = ctable.getSelectedRow();
		Controller c = cmodel.getProxy(row);
		controller.setEnabled(c != null);
		del_ctr.setEnabled(cmodel.canRemove(c));
	}

	/** Do the action for controller properties button */
	private void doPropertiesAction() {
		ListSelectionModel cs = ctable.getSelectionModel();
		int row = cs.getMinSelectionIndex();
		if(row >= 0) {
			Controller c = cmodel.getProxy(row);
			session.getDesktop().show(new ControllerForm(
				session, c));
		}
	}

	/** Create the failed controller panel */
	private JPanel createFailedControllerPanel() {
		ftable.setModel(fmodel);
		ftable.setAutoCreateColumnsFromModel(false);
		ftable.setColumnModel(fmodel.createColumnModel());
		ftable.setRowHeight(ROW_HEIGHT);
		ftable.setVisibleRowCount(16);
		ftable.setRowSorter(sorter);
		IPanel p = new IPanel();
		p.add(ftable, Stretch.FULL);
		p.add(go_btn, Stretch.RIGHT);
		return p;
	}

	/** Go to the failed controller (on the main tab) */
	private void goFailedController() {
		int row = ftable.getSelectedRow();
		if(row >= 0) {
			int mrow = ftable.convertRowIndexToModel(row);
			if(mrow >= 0) {
				Controller c = fmodel.getRowProxy(mrow);
				if(c != null)
					goController(c);
			}
		}
	}

	/** Go to the specified controller (on the main tab) */
	private void goController(Controller c) {
		CommLink l = c.getCommLink();
		int row = model.getRow(l);
		if(row >= 0) {
			ListSelectionModel s = table.getSelectionModel();
			s.setSelectionInterval(row, row);
			table.scrollRectToVisible(
				table.getCellRect(row, 0, true));
			tab.setSelectedIndex(0);
		}
	}
}
