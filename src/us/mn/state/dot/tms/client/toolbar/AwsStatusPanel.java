/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  University of California, Davis
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
import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JLabel;
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
 * A tool panel that displays the AWS status.
 *
 * @author Michael Darter
 */
public class AwsStatusPanel extends ToolPanel implements
	ProxyListener<DMS>
{
	/** AWS abbreviation */
	protected final String m_awsName = I18N.get("dms.aws.abbreviation");

	/** DMS abbreviation */
	protected final String m_dmsAbbr = I18N.get("dms.abbreviation");

	/** DMS cache */
	protected final DmsCache m_dms;

	/** SystemAttribute type cache */
	protected final TypeCache<SystemAttribute> m_sysattribs;

	/** Button to view all AWS messages */
	protected final String m_btnViewText = m_awsName + " Messages";
	protected final JButton m_btnView = new JButton(m_btnViewText);

	/** The label used for AWS messages */
	protected final JLabel m_awstext = new JLabel();

	/** listener object for system attributes */
	protected final saListener m_saListener = new saListener();

	/** desktop */
	final SmartDesktop m_desktop;

	/** List of deactivated DMS, can never be null */
	private String[] m_deactivated_dms = new String[0];

	/** Constructor */
	public AwsStatusPanel(SonarState st, final SmartDesktop desktop) {
		m_dms = st.getDmsCache();
		m_sysattribs = st.getSystemAttributes();
		m_desktop = desktop;
		createComponents();
		addComponents();

		// listen for changes to DMS
		m_dms.getDMSs().addProxyListener(this);
		//m_dms.addProxyListener(this);

		// listen for changes to SystemAttributes
		m_sysattribs.addProxyListener(m_saListener);
	}

	/** is this panel IRIS enabled? */
	public static boolean getIEnabled() {
		return SystemAttrEnum.DMS_AWS_ENABLE.getBoolean();
	}

	/** create components */
	protected void createComponents() {
		m_btnView.setToolTipText("View current " + 
			m_awsName + " messages.");

		// add action for view button click
		new ActionJob(this, m_btnView) {
			public void perform() throws Exception {
				m_desktop.show(new ViewAwsMsgsForm());
			}
		};
	}

	/** add components to panel */
	protected void addComponents() {
		add(m_awstext);
		add(m_btnView);
	}

	/** Update the AWS text on the toolbar and tooltip text */
	private void updateAWSText() {
		String tt = "";
		String mt = "";
		// AWS is enabled
		if(getIEnabled()) {
			updateDeactivatedDMSList();
			String list = genDeactivatedDmsList();
			if(m_deactivated_dms.length <= 0) {
				mt = "";
				tt = "";
			// short list
			} else if(m_deactivated_dms.length < 5) {
				mt = createRedHtml(m_awsName + 
					" deactivated: " + list);
				tt = "";
			// long list
			} else {
				mt = createRedHtml("Multiple " + m_dmsAbbr + 
					" deactivated (" + 
					m_deactivated_dms.length + ")");
				tt = createRedHtml(m_awsName + 
					" deactivated: " + list);
			}
		} else {
			mt = createRedHtml(m_awsName + " is deactivated");
			tt = "";
		}
		m_awstext.setText(mt);
		m_awstext.setToolTipText(tt);
	}

	/** Return the specified string as html in red. */
	private String createRedHtml(String argtext) {
		final String htmlStart = "<html>";
		final String htmlStop = "</html>";
		final String redFontStart = "<font color=#FF0000>";
		final String redFontStop = "</font>";
		return htmlStart + "<b>" + redFontStart + argtext + 
			redFontStop + "<b>" + htmlStop;
	}

	/** Update field which is a list of deactivated DMS */
	protected void updateDeactivatedDMSList() {
		m_deactivated_dms = createAwsStatusList(m_dms.getDMSs(), false);
	}

	/** Generate a comma separated list based on the current list field. 
	 * @return A comma separated string of DMS ids or the empty string. */
	protected String genDeactivatedDmsList() {
		if(m_deactivated_dms.length <= 0)
			return "";
		StringBuilder text = new StringBuilder();
		for(int i = 0; i<m_deactivated_dms.length; ++i ) {
			if(m_deactivated_dms[i] != null)
				text.append(m_deactivated_dms[i]);
			if(i < m_deactivated_dms.length - 1)
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
				if(d != null && d.getAwsAllowed())
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
