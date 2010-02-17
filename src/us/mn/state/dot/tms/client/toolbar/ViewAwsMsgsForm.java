/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009 Minnesota Department of Transportation
 * Copyright (C) 2009-2010 AHMCT, University of California
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

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommLinkHelper;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SFile;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;

/**
 * Form that displays all current AWS messages. This form is presently highly
 * Caltrans D10 specific. It reads current AWS messages from a URL directly, 
 * rather than getting this information from the IRIS server, which it should 
 * probably do in the future.
 *
 * @author Michael Darter
 */
public class ViewAwsMsgsForm extends AbstractForm {

	/** Scheduler that runs refresh job */
	private Scheduler m_scheduler;

	/** Sonar state */
	private final SonarState m_st;

	/** Scheduler refresh job */
	private Job m_rjob;

	/** Number of columns in table */
	private static final int NUM_COLS = 12;

	/** Create a new form */
	public ViewAwsMsgsForm(SonarState st) {
		super("Current " + I18N.get("dms.aws.abbreviation") +
			" Messages");
 		m_st = st;
		setPreferredSize(new Dimension(880, 480));
	}

	/** Initialize form. Called from SmartDesktop.addForm() */
	protected void initialize() {
		add(createFormPanel());
		m_scheduler = new Scheduler("Scheduler: AWS form refresh");
		m_rjob = new RefreshTimerJob();
		m_scheduler.addJob(m_rjob);
	}

	/** Create form panel */
	private JPanel createFormPanel() {

		// center panel, contains text
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel,
			BoxLayout.Y_AXIS));
		centerPanel.add(Box.createHorizontalStrut(10));

		JTable table = createTable();
		JScrollPane scrollPane = new JScrollPane(table);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		centerPanel.add(Box.createHorizontalStrut(10));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		return centerPanel;	
	}

	/** Create table */
	private JTable createTable() {
		// create table
		String[] columnNames = {"Time", I18N.get("dms.abbreviation"), 
			"State", "Font 1", "Font 2", "Line 1", "Line 2", 
			"Line 3", "Line 4", "Line 5", "Line 6", "On Time"};

		// read AWS file
		Object[][] data = getAwsValues(getAwsUrl());
		if(data == null)
			data = new Object[1][columnNames.length];
		JTable table = new JTable(data, columnNames);
		setTableColWidths(table);
		return table;
	}

	/** Set table column widths */
	private static void setTableColWidths(JTable jt) {
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		int[] cw = {110, 30, 100, 80, 80, 80, 80, 80, 80, 80, 80, 60};
		assert cw.length == NUM_COLS;
		for(int i = 0; i < cw.length; ++i)
			setTableColWidth(jt, i, cw[i]);
	}

	/** Set table column width */
	private static void setTableColWidth(JTable jt, int col, int wid) {
		jt.getColumnModel().getColumn(col).setPreferredWidth(wid);
	}

	/** Refresh timer job */
	protected class RefreshTimerJob extends Job {

		/** Job completer */
		protected final Completer m_comp;

		/** Current time stamp */
		protected Calendar stamp;

		/** Job to be performed on completion */
		protected final Job job = new Job() {
			public void perform() {
				// nothing
			}
		};

		/** Create a new 30-second timer job */
		protected RefreshTimerJob() {
			super(Calendar.SECOND, 30, Calendar.SECOND, 
				getAwsReadTimeSecs());
			m_comp = new Completer("30-Second", m_scheduler, job);
		}

		/** Perform the 30-second timer job */
		public void perform() throws Exception {
			if(!m_comp.checkComplete())
				return;
			Calendar s = Calendar.getInstance();
			s.add(Calendar.SECOND, -30);
			stamp = s;
			m_comp.reset(stamp);
			try {
				doWork();
			} finally {
				m_comp.makeReady();
			}
		}

		/** do the job work */
		private void doWork() {
			JPanel jp = createFormPanel();
			removeAll();
			add(jp);
			revalidate();
		}
	}

	/** Get the AWS message URL, which is specified in the CommLink URL. */
	private String getAwsUrl() {

		CommLink cl = CommLinkHelper.getCommLink(CommProtocol.AWS);
		if(cl != null)
			return cl.getUrl();
		else
			return null;
	}

	/** Read AWS message values from a URL */
	private Object[][] getAwsValues(String awsFileUrl) {
		if(awsFileUrl == null || awsFileUrl.length() <= 0)
			return null;
		byte[] msgs = SFile.readUrl(awsFileUrl);
		ArrayList lines = null;
		Object[][] ret = null;
		if(msgs != null) {
			lines = parseAwsFile(msgs);
			if(lines != null) {
				ret = new Object[lines.size()][];
				for(int i=0; i<lines.size(); ++i)
					ret[i] = parseAwsFileLine(
						(String)lines.get(i));
			}
		}
		return ret;
	}

	/** Parse a byte array of AWS messages. */
	private ArrayList parseAwsFile(byte[] argmsgs) {
		ArrayList ret = new ArrayList<String>();

		// cycle through each line, which is terminated by '\n'
		String msgs = SString.byteArrayToString(argmsgs);
		StringTokenizer lineTok = new StringTokenizer(msgs, "\n");
		while(lineTok.hasMoreTokens()) {
			String line = lineTok.nextToken();
			ret.add(line);
		}
		return ret;
	}

	/** Parse a string that contains a single DMS message.
	 * @param argline a single DMS message, fields delimited with ';'. */
	private Object[] parseAwsFileLine(String argline) {
		if(argline == null)
			argline = "";

		Object[] ret = new Object[NUM_COLS];
		for(int i = 0; i < ret.length; ++i)
			ret[i] = "";
		try {
			// add a space between successive delimiters. This is 
			// done so the tokenizer doesn't skip over delimeters 
			// with nothing between them.
			String line = argline.replace(";;", "; ;");
			line = line.replace(";;", "; ;");

			// verify syntax
			StringTokenizer tok = new StringTokenizer(line, ";");

			// validity check, should be 12 or 13 tokens
			int numtoks = tok.countTokens();
			final int EXPNUMTOKS1 = 12;
			final int EXPNUMTOKS2 = 13;
			if(numtoks != EXPNUMTOKS1 && numtoks != EXPNUMTOKS2) {
				throw new IllegalArgumentException("Bogus " +
					"CMS message format, numtoks was " + 
					numtoks + ", expected " + EXPNUMTOKS1
					+ " or " + EXPNUMTOKS2 + " (" + 
					argline	+ ").");
			}

			// #1, date: 20080403085910
			ret[0] = new String(tok.nextToken());

			// #2, id: 39
			ret[1] = new String(tok.nextToken());

			// #3, message description
			ret[2] = new String(tok.nextToken());

			// #4, pg 1 font
			ret[3] = new String(tok.nextToken());

			// #5, pg 2 font
			ret[4] = new String(tok.nextToken());

			// #6 - #11, rows of text
			{
				final int numrows = 6;
				String[] row = new String[numrows];
				for(int i = 0; i < numrows; ++i)
					ret[5+i] = tok.nextToken().trim();
			}

			// #12, on time: 0.0
			ret[11] = new String(tok.nextToken());

			// #13, ignore this field, follows last semicolon if
			//      there are 13 tokens.

		} catch(Exception ex) {
			Log.warning("ViewAwsMsgsForm.parse(): " +
				"unexpected exception: " + ex + ", line=" + 
				argline);
		}

		return ret;
	}

	/** Get the AWS read time in seconds. This is the number of seconds
	 *  to wait after :00 and :30 to read the AWS messages. */
	public static int getAwsReadTimeSecs() {
		int secs = SystemAttrEnum.DMS_AWS_READ_TIME.getInt();
		secs = (secs < 0 ? 0 : secs);
		secs = (secs > 29 ? 29 : secs);
		return secs;
	}

	/** Form closed */
	protected void dispose() {
		m_scheduler.removeJob(m_rjob);
		m_scheduler = null;
		super.dispose();
	}
}
