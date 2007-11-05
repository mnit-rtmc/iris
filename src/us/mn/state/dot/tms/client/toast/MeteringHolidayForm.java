/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.Dimension;
import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.MeteringHolidayList;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.ActionJob;

/**
 * A form for displaying and editing the list of metering holidays
 *
 * @author Douglas Lau
 */
public class MeteringHolidayForm extends TMSObjectForm {

	/** Frame title */
	static protected final String TITLE = "Metering Holidays";

	/** Remote metering holiday list */
	protected MeteringHolidayList holidays;

	/** Table model for metering holidays */
	protected MeteringHolidayModel model;

	/** Table to hold the metering holiday list */
	protected final JTable holiday_table = new JTable();

	/** Button to delete the selected holiday */
	protected final JButton del_holiday = new JButton("Delete");

	/** Create a new metering holiday form */
	public MeteringHolidayForm(TmsConnection tc) {
		super(TITLE, tc);
	}

	/** Initializze the widgets in the form */
	protected void initialize() throws RemoteException {
		holidays = (MeteringHolidayList)
			connection.getProxy().getMeteringHolidayList();
		model = new MeteringHolidayModel(holidays, admin);
		super.initialize();
		add(createHolidayPanel());
	}

	/** Get the minimum size of the holiday table */
	public Dimension getMinimumSize() {
		return new Dimension(640, 460);
	}

	/** Get the preferred size of the holiday table */
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	/** Close the form */
	protected void close() {
		super.close();
		model.dispose();
	}

	/** Create metering holiday panel */
	protected JPanel createHolidayPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BORDER);
		final ListSelectionModel s = holiday_table.getSelectionModel();
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;
				del_holiday.setEnabled(!s.isSelectionEmpty());
			}
		});
		holiday_table.setModel(model);
		holiday_table.setAutoCreateColumnsFromModel(false);
		holiday_table.setColumnModel(model.createColumnModel());
		holiday_table.setRowHeight(24);
		panel.add(new JScrollPane(holiday_table));
		if(admin) {
			panel.add(Box.createHorizontalStrut(VGAP));
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(del_holiday);
			box.add(Box.createHorizontalGlue());
			panel.add(box);
			new ActionJob(this, del_holiday) {
				public void perform() throws Exception {
					deleteHoliday();
				}
			};
		}
		return panel;
	}

	/** Delete the selected holiday */
	protected void deleteHoliday() throws TMSException, RemoteException {
		int row = holiday_table.getSelectedRow();
		if(row < 0)
			return;
		String name = model.getName(row);
		if(!name.equals(""))
			holidays.remove(name);
	}
}
