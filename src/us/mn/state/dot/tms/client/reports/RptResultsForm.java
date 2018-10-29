/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.client.reports;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.reports.RptResultItem;
import us.mn.state.dot.tms.reports.RptResults;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Report results form
 *
 * @author Michael Janson & John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")
public class RptResultsForm extends AbstractForm {

	private static final String reportDelim = ",";
	
	/** Result report object */
	RptResults results = new RptResults();

	/** Scroll pane for report results */
	private JScrollPane scroll_pn = new JScrollPane();

	/** Report results table */
	private ZTable report_tbl = new ZTable(); 
	
	//-------------------------------------------
	
	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return true; //TODO: substitute check for access to reporting
	}
	
	//-------------------------------------------
	
	/** Copy to Clipboard action */
	private IAction copy_clipboard = new IAction("report.copy.clipboard") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e) {
			copyToClipboard();
		}
	};
	
	/** Copy report results to the clipboard (not including headings) */
	private void copyToClipboard(){
		ActionEvent nev = new ActionEvent(report_tbl, ActionEvent.ACTION_PERFORMED, "copy");
		report_tbl.selectAll();
		report_tbl.getActionMap().get(nev.getActionCommand()).actionPerformed(nev);
		report_tbl.clearSelection();
	}
	
	//-------------------------------------------
	
	/** Export to CSV action */
	private IAction export_csv = new IAction("report.copy.csv") {
		protected void doActionPerformed(ActionEvent e) throws IOException{
			exportToCsv();
		}
	};
	
	/** Date formatter for CSV file export */
	private SimpleDateFormat dtFormatter = new SimpleDateFormat("yyyy_mm_dd_HH_mm_ss");

	/** Report CSV export file */
	//TODO: Convert directory to be a system attribute
	private File REPORT_FILE = new File(System.getProperty("user.home"), generateFileName());
	
	/** Write report to CSV file */
	public void exportToCsv() throws IOException {
		FileWriter fw = new FileWriter(REPORT_FILE, true);
		try {
			fw.write(createReportHeader());
			Iterator<RptResultItem> rpt_itr = results.iterateRptResults();
			while (rpt_itr.hasNext()){
				fw.write(createReportEntry(rpt_itr.next()));
			}
		}
		finally {
			fw.close();
		}
	}
	
	/** Generate header for CSV file */
	private String createReportHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append(I18N.get("report.field.datetime"));
		sb.append(reportDelim);
		sb.append(I18N.get("report.field.device"));
		sb.append(reportDelim);
		sb.append(I18N.get("report.field.user"));
		sb.append(reportDelim);
		sb.append(I18N.get("report.field.description"));
		sb.append('\n'); 
		return sb.toString();
	}
	
	/** Write row of report */
	private String createReportEntry(RptResultItem rri) {
		StringBuilder sb = new StringBuilder();
		sb.append(rri.getDatetimeStr());
		sb.append(reportDelim);
		sb.append(rri.getName());
		sb.append(reportDelim);
		sb.append(rri.getUsername());
		sb.append(reportDelim);
		sb.append(rri.getDescription());
		sb.append('\n');
		return sb.toString();
	}
	
	/** Generate a filename for the CSV report export */
	private String generateFileName(){
		return dtFormatter.format(new Timestamp(TimeSteward.currentTimeMillis())) + ".csv";
	}
	
	//-------------------------------------------

	/** Create a new report results form with unique title based on datetime */
	public RptResultsForm(RptResults res) {
		super(I18N.get("report.results.title") + " - " + TimeSteward.currentTimeShortString(), true);
//		setLayout(new LayoutManager BoxLayout(this, BoxLayout.Y_AXIS));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		updateForm();
		results = res;
//		invalidate();
		queueUpdateForm();
	}
	
	//-------------------------------------------

	/** Create or update the report results form */
	public void updateForm() {
		removeAll();
		RptResultsModel m = new RptResultsModel(results);
		report_tbl = new ZTable();
		report_tbl.setModel(m);
		report_tbl.setColumnModel(m.createColumnModel());

		((DefaultTableCellRenderer)report_tbl.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
		report_tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		report_tbl.setPreferredScrollableViewportSize(new Dimension(report_tbl.getPreferredSize().width-85, report_tbl.getPreferredScrollableViewportSize().height));

		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(m);
		report_tbl.setRowSorter(sorter);
		scroll_pn = createScrollPane(report_tbl);
		add(scroll_pn);
		add(createButtonPanel());
		validate();
		repaint();
	}

	/** Queue task to update the GUI from the event dispatch thread */
	public void queueUpdateForm() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateForm();
			}
		});
	}

	//-------------------------------------------
	
	/** Create the button panel */
	private JPanel createButtonPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
		pnl.add(Box.createHorizontalGlue());
		JButton btn = new JButton(copy_clipboard);
		pnl.add(btn);
		int btnHeight = btn.getMinimumSize().height * 140 / 100;
		pnl.add(Box.createRigidArea(new Dimension(5, btnHeight)));
		JButton export_csv_button = new JButton(export_csv);
		export_csv_button.setToolTipText(I18N.get("report.copy.csv.tooltip"));
		pnl.add(export_csv_button);
		pnl.add(Box.createRigidArea(new Dimension(1, btnHeight)));
		return pnl;
	}

//	/** Create the button panel */
//	private JPanel createButtonPanel() {
//		JPanel pnl = new JPanel();
//		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
////		pnl.add(Box.createHorizontalStrut(500));
//		pnl.add(Box.createHorizontalGlue());
//		pnl.add(new JButton(copy_clipboard));
//		pnl.add(new JButton(export_csv));
//		Dimension minsize = pnl.getMinimumSize();
//		pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, minsize.height));
//		return pnl;
//	}
	
	//-------------------------------------------
	
	/** Create a scroll pane */
	private JScrollPane createScrollPane(ZTable t) {
		return new JScrollPane(t,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
}
