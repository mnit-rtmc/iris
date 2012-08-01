/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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

import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JLabel;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.dms.DmsCache;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
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
	private static final String m_awsName = 
		I18N.get("dms.aws.abbreviation");

	/** DMS abbreviation */
	private static final String m_dmsAbbr = I18N.get("dms");

	/** DMS cache */
	protected final DmsCache m_dms;

	/** SystemAttribute type cache */
	protected final TypeCache<SystemAttribute> m_sysattribs;

	/** The label used for AWS messages */
	protected final JLabel m_awstext = new JLabel();

	/** listener object for system attributes */
	protected final saListener m_saListener = new saListener();

	/** desktop */
	final SmartDesktop m_desktop;

	/** List of deactivated DMS, can never be null */
	private TreeMap<String, DMS> deact_dms = new TreeMap<String, DMS>();

	/** Constructor */
	public AwsStatusPanel(SonarState st, final SmartDesktop desktop) {
		m_dms = st.getDmsCache();
		m_sysattribs = st.getSystemAttributes();
		m_desktop = desktop;
		addComponents();

		// listen for changes to DMS
		m_dms.getDMSs().addProxyListener(this);

		// listen for changes to SystemAttributes
		m_sysattribs.addProxyListener(m_saListener);
	}

	/** is this panel IRIS enabled? */
	public static boolean getIEnabled() {
		return SystemAttrEnum.DMS_AWS_ENABLE.getBoolean();
	}

	/** add components to panel */
	protected void addComponents() {
		add(m_awstext);
	}

	/** Update the AWS text on the toolbar and tooltip text */
	private void updateAWSText() {
		final int SHORT_LIST_LEN = 5;
		String tt = "";
		String mt = "";
		// AWS is enabled
		if(getIEnabled()) {
			String list = genDeactivatedDmsList();
			if(deact_dms.size() <= 0) {
				mt = "";
				tt = "";
			// short list
			} else if(deact_dms.size() < SHORT_LIST_LEN) {
				mt = createRedHtml(m_awsName + 
					" deactivated: " + list);
				tt = "";
			// long list
			} else {
				mt = createRedHtml("Multiple " + m_dmsAbbr + 
					" deactivated (" + 
					deact_dms.size() + ")");
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

	/** Generate a comma separated list based on the current list field. 
	 * @return A comma separated string of DMS ids or the empty string. */
	protected String genDeactivatedDmsList() {
		if(deact_dms.size() <= 0)
			return "";
		StringBuilder text = new StringBuilder();
		Iterator i = (deact_dms.keySet()).iterator();
		while(i.hasNext()) {
			String dn = (String)i.next();
			DMS d = deact_dms.get(dn);
			if(isDmsDeact(d)) {
				text.append(dn);
				if(i.hasNext())
					text.append(", ");
			}
		}
		return text.toString();
	}

	/** Is proxy deactivated? */
	private static boolean isDmsDeact(DMS d) {
		return d != null && d.getAwsAllowed() && !d.getAwsControlled();
	}

	/** A proxy has been added */
	public void proxyAdded(DMS d) {
		if(isDmsDeact(d))
			deact_dms.put(d.getName(), d);
	}

	/** All proxies have been enumerated */
	public void enumerationComplete() {}

	/** A proxy has been removed */
	public void proxyRemoved(DMS d) {
		if(d != null)
			deact_dms.remove(d.getName());
	}

	/** A proxy has changed */
	public void proxyChanged(DMS d, String a) {
		if(d == null || a == null)
			return;
		if(a.equals("awsControlled") || a.equals("awsAllowed")) {
			proxyRemoved(d);
			proxyAdded(d);
			updateAWSText();
		}
	}

	/** Internal class to listen for changes to system attributes. On 
	 * client start-up, the dms_aws_enabled attribute is added, which
	 * triggers the initial (and only) call to updateAWSText(). */
	private class saListener implements ProxyListener<SystemAttribute> {
		/** A new proxy has been added */
		public void proxyAdded(SystemAttribute proxy) {
			if(isAwsEnableAttribute(proxy))
				updateAWSText();
		}

		/** All proxy have been enumerated */
		public void enumerationComplete() {}

		/** A proxy has been removed */
		public void proxyRemoved(SystemAttribute proxy) {
			if(isAwsEnableAttribute(proxy))
				updateAWSText();
		}

		/** A proxy has been changed */
		public void proxyChanged(SystemAttribute proxy, String a) {
			if(isAwsEnableAttribute(proxy))
				updateAWSText();
		}
	}

	/** Is the specified system attribute the AWS enable attribute? */
	private boolean isAwsEnableAttribute(SystemAttribute p) {
		return SystemAttributeHelper.same(p, 
			SystemAttrEnum.DMS_AWS_ENABLE);
	}

	/** cleanup */
	public void dispose() {
		m_dms.getDMSs().removeProxyListener(this);
		m_sysattribs.removeProxyListener(m_saListener);
	}
}
