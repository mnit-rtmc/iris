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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import us.mn.state.dot.tms.RptConduit;
import us.mn.state.dot.tms.RptGenEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.reports.RptDeviceClassItem;
import us.mn.state.dot.tms.reports.RptDeviceItem;
import us.mn.state.dot.tms.reports.RptRequest;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Report setup dialog
 *
 * @author Michael Janson & John L. Stanley - SRF Consulting
 */

@SuppressWarnings("serial")
public class RptSetupForm extends AbstractForm {

	private RptRequest rpt_request = new RptRequest();
	
	/** Width of the date fields */
	static private final int FIELDWIDTH_DATE = 11;

	/** User session */
	private final Session session;

	//-------------------------------------------
	
	/** Start date text field */
	private final JTextField start_date_txt;
	
	/** End date text field */
	private final JTextField end_date_txt;

	/** Device type combo box */
	private final JComboBox<String> device_class_cbx;
			
	/** Device listing */
	private JList<String> device_list;
	
	/** Scroll pane for device listing */
	private JScrollPane scroll_pn;

	//-------------------------------------------
	
	/** Cancel action */
	private final IAction cancel = new IAction(
		"report.setup.cancel")
	{
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			close(session.getDesktop());
		}
	};

	/** Cancel button */
	private final JButton cancel_btn;
	
	/** Generate Report button */
	private final JButton report_btn;
	
	/** Datetime format for start and end date */
	private SimpleDateFormat dtFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	/** [Generate Report] action */
	private final IAction report = new IAction(
		"report.setup.generate")
	{
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			/** Un-select all devices */
			Iterator<RptDeviceItem> itDev = rpt_request.iterateDevices();
			while (itDev.hasNext())
				itDev.next().setSelected(false);
			
			/** Set the selected devices */
			Iterator<String> itSelDevList = device_list.getSelectedValuesList().iterator();
			while (itSelDevList.hasNext()){
				rpt_request.setDeviceSelected(itSelDevList.next(), true);
			}
	
			/** Set start and end datetime in request */
			rpt_request.setStartDatetime(calculateDateMillis(start_date_txt.getText()));
			rpt_request.setEndDatetime(calculateDateMillis(end_date_txt.getText()));
			
			/** submit request to server */
			RptProcess.submitRequest(session, rpt_request);
		}
	};
	
	/** Convert date to milliseconds and handle parsing errors */
	private long calculateDateMillis(String string_date){
		if (string_date.isEmpty())
			return 0;
		try {
		    Date d = dtFormatter.parse(string_date);
		    long milliseconds = d.getTime();
		    return milliseconds;
		} catch (ParseException e) {
		    e.printStackTrace();
		    return -1; //TODO: Handle incorrectly entered datetime by user
		}
	}
	
	@SuppressWarnings("synthetic-access")
	public RptSetupForm(Session s) {
		super(I18N.get("report.setup.title"));	
		session = s;
		
		/** Initialize device lists */
		rpt_request.init();

		// select default report generator
		String genName = RptGenEnum.RPTGEN_SIGN_EVENTS.getGuiName();
		rpt_request.setGeneratorSelected(genName, true);
		
		/** Create device class listing */
		DefaultComboBoxModel<String> deviceClassList = new DefaultComboBoxModel<String>() {
			public int getSize() { return rpt_request.getDeviceClassList().size(); }
			public String getElementAt(int index) { return rpt_request.getDeviceClassList().get(index).getName(); }
		};
		device_class_cbx = new JComboBox<String>(deviceClassList);
		device_class_cbx.setPreferredSize(new Dimension(150,(int)device_class_cbx.getPreferredSize().getHeight()));
		
		/** Set the device class and refresh the scroll pane to display visible devices */
		device_class_cbx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RptDeviceClassItem rdci = rpt_request.getDeviceClass(device_class_cbx.getSelectedItem().toString());
				rpt_request.setDeviceClassSelected(rdci.getDevClassEnum(), true);
				scroll_pn.repaint();
			}
		});
				
		/** Create device listing */
		ListModel<String> deviceList = new AbstractListModel<String>() {
			public int getSize() { return rpt_request.getDeviceList().size(); }
		    public String getElementAt(int index) { return rpt_request.getDeviceList().get(index).getVisible() ? rpt_request.getDeviceList().get(index).getName() : " "; }
		};
		device_list = new JList<String>(deviceList);
		scroll_pn = createScrollPane(device_list);
		scroll_pn.setPreferredSize(new Dimension(150,200));

		/** Display the top entry in the device class list */
		device_class_cbx.setSelectedIndex(0);

		/** Start and end datetime text fields */
		//TODO: Convert to calendar widgets
		Date dtEnd = new Date();                  // end now
		Date dtStart = new Date();
		dtStart.setTime(dtEnd.getTime() - 86400000); // start 24hrs earlier
		start_date_txt = new JTextField(FIELDWIDTH_DATE);
		start_date_txt.setText(dtFormatter.format(dtStart));
//		start_date_txt.setText("YYYY-MM-DD HH:mm");
		end_date_txt = new JTextField(FIELDWIDTH_DATE); // Upgrade to calendar widgets
		end_date_txt.setText(dtFormatter.format(dtEnd));
//		end_date_txt.setText("YYYY-MM-DD HH:mm");
		
		/** Cancel and Report Generator Buttons */
		cancel_btn = new JButton(cancel);	
		report_btn = new JButton(report);
	}

	/** Initialize the form */
	@Override
	protected void initialize() {

		// initialize layout
		GridBagLayout gbl = new GridBagLayout();
		JPanel p = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		// start date label
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(new ILabel("report.setup.dt.start"), gbc);
		// start date field
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(start_date_txt, gbc);
		// end date label
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(new ILabel("report.setup.dt.end"), gbc);
		// end date field
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(end_date_txt, gbc);
		// device type label
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(new ILabel("report.setup.devtype"), gbc);
		// device type combobox
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(device_class_cbx, gbc);
		// cancel button
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(cancel_btn, gbc);
		// device listing
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;		
		p.add(scroll_pn, gbc);
		// generate report button
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(report_btn, gbc);

		add(p);

	}
	
	/** Create a scroll pane */
	@SuppressWarnings("rawtypes")
	private JScrollPane createScrollPane(JList l) {
		return new JScrollPane(l,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
	
	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(RptConduit.SONAR_TYPE);
//		return true; //TODO: Change to see if user has access to reporting functionality;
	}

}
