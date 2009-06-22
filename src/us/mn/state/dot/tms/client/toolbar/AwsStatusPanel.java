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

import java.util.ArrayList;
import java.util.Arrays;
import java.awt.FlowLayout;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.dms.DmsCache;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A JPanel that displays the AWS status.
 *
 * @author Michael Darter
 * @created November 18, 2008
 */
public class AwsStatusPanel extends JPanel implements
	ProxyListener<DMS>
{
	/** DMS cache */
	protected final DmsCache m_dms;

	/** SystemAttribute type cache */
	protected final TypeCache<SystemAttribute> m_sysattribs;

	/** The label used for cursor coordinates */
	protected final JLabel m_coordinates = new JLabel();

	/** Button to view all AWS messages */
	protected final String m_btnViewText = "Messages";
	protected final JButton m_btnView = new JButton(m_btnViewText);

	/** The label used for AWS messages */
	protected final JLabel m_awstext = new JLabel();

	/** AWS abbreviation */
	protected final String m_awsName = I18N.get("dms.aws.abbreviation");

	/** listener object for system attributes */
	protected final saListener m_saListener = new saListener();

	/** DMS abbreviation */
	protected final String m_dmsAbbr = I18N.get("dms.abbreviation");

	/** desktop */
	final SmartDesktop m_desktop;

	/** sonar state */
	final SonarState m_st;

	/** Constructor */
	public AwsStatusPanel(SonarState st, final SmartDesktop desktop) {
		assert st !=  null;
		m_st = st;
		m_dms = m_st.getDmsCache();
		m_sysattribs = m_st.getSystemAttributes();
		m_desktop = desktop;
		createComponents();
		addComponents();
		setToolTipText();

		// listen for changes to DMS
		m_dms.getDMSs().addProxyListener(this);
		//m_dms.addProxyListener(this);

		// listen for changes to SystemAttributes
		m_sysattribs.addProxyListener(m_saListener);
	}

	/** set tooltip text */
	public void setToolTipText() {
		// none
	}

	/** create components */
	protected void createComponents() {
		m_btnView.setToolTipText("View current " + 
			m_awsName + " messages.");

		// add action for view button click
		new ActionJob(this, m_btnView) {
			public void perform() throws Exception {
				m_desktop.show(new ViewAwsMsgsForm(m_st));
			}
		};
	}

	/** add components to panel */
	protected void addComponents()
	{
		setLayout(new FlowLayout());
		setBorder(BorderFactory.
			createBevelBorder(BevelBorder.LOWERED));
		add(m_awstext);
		add(m_btnView);
		return;
	}

	/** Set the optional AWS text on the status bar */
	protected void setAWSText(String text) {
		text = (text == null ? "" : text);
		m_awstext.setText(text);
	}

	/** Update the AWS text on the toolbar */
	protected void updateAWSText() {
		m_awstext.setText(buildAWSText());
	}

	/** Return the AWS toolbar text, which consists of a color
	 *  coded list of deactivated DMS. */
	protected String buildAWSText() {
		final String htmlStart = "<html>";
		final String htmlStop = "</html>";
		final String redFontStart = "<font color=#FF0000>";
		final String redFontStop = "</font>";

		// AWS not activated?
		if(!SystemAttrEnum.DMS_AWS_ENABLE.getBoolean())
			return htmlStart + "<b>" + redFontStart + m_awsName + 
				" is not activated" + redFontStop + "<b>" + 
				htmlStop;

		// build list of deactivated DMS
		String dal = createDeactivatedDMSList();

		// no deactivated dms?
		if(dal == null || dal.length() <= 0) {
			StringBuilder text = new StringBuilder();
			text.append(htmlStart);
			text.append("All ");
			text.append(m_dmsAbbr);
			text.append(" are activated for ");
			text.append(m_awsName);
			text.append(" messages ");
			text.append(htmlStop);
			return text.toString();
		}

		// construct list of deactivated dms
		StringBuilder text = new StringBuilder();
		text.append(htmlStart);
		text.append(redFontStart);
		text.append(m_awsName);
		text.append(" deactivated ");
		text.append(m_dmsAbbr);
		text.append(": ");
		text.append(dal);
		text.append(redFontStop);
		text.append(htmlStop);

		return text.toString();
	}

	/** create list of deactivated DMS */
	protected String createDeactivatedDMSList() {
		String[] ids = createAwsStatusList(m_dms.getDMSs(), false);
		if(ids == null || ids.length == 0)
			return null;
		StringBuilder text = new StringBuilder();
		for(int i = 0; i<ids.length; ++i ) {
			if(ids[i] != null)
				text.append(ids[i]);
			if(i < ids.length - 1)
				text.append(", ");
		}
		return text.toString();
	}

	/** A new proxy has been added */
	public void proxyAdded(DMS proxy) {
		updateAWSText();
	}

	/** All proxies have been enumerated */
	public void enumerationComplete() {}

	/** A proxy has been removed */
	public void proxyRemoved(DMS proxy) {
		updateAWSText();
	}

	/** A proxy has been changed */
	public void proxyChanged(DMS proxy, String a) {
		updateAWSText();
	}

	/** internal class to listen for changes to system attributes */
	private class saListener implements ProxyListener<SystemAttribute> {
		/** A new proxy has been added */
		public void proxyAdded(SystemAttribute proxy) {
			updateAWSText();
		}

		/** All proxy have been enumerated */
		public void enumerationComplete() {}

		/** A proxy has been removed */
		public void proxyRemoved(SystemAttribute proxy) {
			updateAWSText();
		}

		/** A proxy has been changed */
		public void proxyChanged(SystemAttribute proxy, String a) {
			updateAWSText();
		}
	}

	/** Create a numerically sorted array of device ids that each contain
	 *  the specified attribute name with cooresponding attribute value.
	 *  @param tc DMS type cache.
	 *  @param avalue All returned device ids have this attribute value.
	 *  @return array of Strings, which contain device ids, e.g. "V13". */
	static public String[] createAwsStatusList(TypeCache<DMS> tc, 
		final boolean avalue)
	{
		if(tc == null)
			return new String[0];
		// enumerate DMS
		final ArrayList<String> list = 
			new ArrayList<String>(); // e.g. "V13"
		tc.findObject(new Checker<DMS>()
		{
			public boolean check(DMS d) {
				if(d.getAwsAllowed())
					if(avalue == d.getAwsControlled())
						list.add(d.getName());
				return false;
			}
		});

		// sort array of device ids in numeric ascending order
		String[] dms_ids = new String[list.size()];
		dms_ids = list.toArray(dms_ids);
		Arrays.sort(dms_ids, new NumericAlphaComparator<String>());
		return dms_ids;
	}

	/** cleanup */
	public void dispose() {
		m_dms.getDMSs().removeProxyListener(this);
		m_sysattribs.removeProxyListener(m_saListener);
	}
}
