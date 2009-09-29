/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.StringTokenizer;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommLinkHelper;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SFile;
import us.mn.state.dot.tms.utils.SString;

/**
 * Form that displays all current AWS messages. This form is presently highly
 * Caltrans D10 specific but is anticipated to be generalized in the future. 
 * It reads current AWS messages from a URL directly, rather than getting 
 * this information from the IRIS server, which it should probably do in the 
 * future).
 *
 * @author Michael Darter
 */
public class ViewAwsMsgsForm extends AbstractForm {

	/** table */
	protected JTable m_table = null;

	/** refresh interval */
	//FIXME: add periodic reading of file
	protected final int timerTickLengthMS = 10*1000;

	/** Create a new form */
	public ViewAwsMsgsForm() {
		super("Current " + I18N.get("dms.aws.abbreviation") +
			" Messages");
		setPreferredSize(new Dimension(850,425));

		// create refresh timer
		Timer rt = new Timer(timerTickLengthMS, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshTimerTick();
			} 
		});
		rt.start();
	}

	/** Initialize form. Called from SmartDesktop.addForm() */
	protected void initialize() {

		// center panel, contains text
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel,
			BoxLayout.Y_AXIS));
		centerPanel.add(Box.createHorizontalStrut(10));

		// title
		//String title = Integer.toString(m_counter);
		//JLabel label1 = new JLabel(title);
		//centerPanel.add(label1);

		// create table
		m_table = createTable();
		JScrollPane scrollPane = new JScrollPane(m_table);
		centerPanel.add( scrollPane, BorderLayout.CENTER );

		centerPanel.add(Box.createHorizontalStrut(10));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(centerPanel);
	}

	/** Create table */
	protected JTable createTable() {
		// create table
		String[] columnNames = {"Time", I18N.get("dms.abbreviation"), 
			"State", "Font 1", "Font 2", "Line 1", "Line 2", 
			"Line 3", "Line 4", "Line 5", "Line 6", "On Time"};

		// read AWS file
		Object[][] data = getAwsValues(getAwsUrl());
		if(data == null)
			data = new Object[1][columnNames.length];
		JTable table = new JTable(data, columnNames);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setPreferredScrollableViewportSize(
			new Dimension(800, 800));
		return table;
	}

	/** Get the AWS message URL, which is specified in the comm link URL */
	protected String getAwsUrl() {
		CommLink cl = CommLinkHelper.getAwsCommLink();
		if(cl != null)
			return cl.getUrl();
		else
			return null;
	}

	/** refresh table timer tick */
	protected void refreshTimerTick() {
		//FIXME: refresh the table here
	}

	/** Read AWS message values from a URL */
	protected Object[][] getAwsValues(String awsFileUrl) {
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

	/** Parse a byte array of AWS messages */
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

	/**
	 * Parse a string that contains a single DMS message.
	 * @param argline a single DMS message, fields delimited with ';'.
	 */
	public Object[] parseAwsFileLine(String argline) {
		if(argline == null)
			argline = "";

		Object[] ret = new Object[12];
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
					numtoks + ", expected " + EXPNUMTOKS1 +
					" or " + EXPNUMTOKS2 + " (" + argline +
					").");
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
}
