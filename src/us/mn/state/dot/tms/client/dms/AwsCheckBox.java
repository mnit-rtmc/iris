/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

package us.mn.state.dot.tms.client.dms;

import java.awt.event.ItemEvent;
import java.rmi.RemoteException;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;

import us.mn.state.dot.tms.TrafficDeviceAttribute;
import us.mn.state.dot.tms.TrafficDeviceAttributeHelper;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.utils.SString;

/**
 * Checkbox to indicate if a particular DMS is part of an automated warning
 * system or not.
 * FIXME: it would be nice to turn this into a superclass SonarCheckBox.
 * @see DMSDispatcher
 * @author Michael Darter
 */
public class AwsCheckBox extends JCheckBox 
	implements ItemListener, ProxyListener<TrafficDeviceAttribute>
{
	/* sonar proxy name */
	protected String m_proxyName = null;

	/** constructor 
	 *  @param proxyName Name of sonar proxy associated w/ checkbox, 
	 *	   e.g. "V1_AWS_controlled".
	 *  @param text Checkbox text
	 *  @param tc TypeCache for the associated sonar proxy type
	 */
	public AwsCheckBox(String proxyName, String text, 
		TypeCache<TrafficDeviceAttribute> tc) 
	{
		super(text);
		System.err.println("AwsCheckBox.AwsCheckBox() called. proxyName = "+proxyName);
		setProxy(proxyName);
		addItemListener(this);   // listener for checkbox state change
		tc.addProxyListener(this);   // lisener for attribute change
	}

	/** get proxy for this control */
	protected TrafficDeviceAttribute getProxy() {
		if(m_proxyName == null)
			return null;
		return TrafficDeviceAttributeHelper.get(m_proxyName);
	}

	/** set the sonar proxy for this control using the proxy name */
	public void setProxy(String proxyName) {
		System.err.println("AwsCheckBox.setProxy(string) called. proxyName = "+proxyName);
		m_proxyName = proxyName;
		TrafficDeviceAttribute p = getProxy();
		update(p);
	}

	/** update control with proxy */
	protected void update(TrafficDeviceAttribute p) {
		System.err.println("AwsCheckBox.update() called.");
		if(p == null)
			setEnabled(false);
		else {
			setSelected(p.getAValueBoolean());
			setEnabled(true);
		}
		System.err.println("AwsCheckBox.update(): found proxy? "+(p != null));
	}

	/** get the id of the sonar proxy e.g. "V1" */
	public String getDmsId() {
		if(m_proxyName == null)
			return null;
		return TrafficDeviceAttributeHelper.
			extractDeviceId(m_proxyName);
	}

	/** listener for checkbox state change */
	public void itemStateChanged(ItemEvent e) {
		System.err.println("AwsCheckBox.itemStateChanged() called");
		if (this == e.getItemSelectable() )
			// the user clicked this checkbox and it changed state
			setProxyCheckValue(this.isSelected());
	}

	/** set state of proxy value */
	public void setProxyCheckValue(boolean newValue) {
		System.err.println("AwsCheckBox.setProxyCheckValue() called: newValue="+newValue);

		// now update sonar proxy, which will trigger a call
		// to the proxyChanged() method
		String sv = new Boolean(newValue).toString();
		System.err.println("AwsCheckBox.setProxyCheckValue(): will set proxy to new value="+sv);
		TrafficDeviceAttribute p = getProxy();
		if(p != null)
			p.setAValue(sv); //FIXME: create setAValueBoolean()
			//p.setAValueBoolean(newValue);
	}

	/** Does the specified proxy match the one associated with this 
	 *  control?
	 *  @return True if they match else false
	 */
	public final boolean proxyMatches(final TrafficDeviceAttribute proxy) {
		if(proxy == null || m_proxyName == null)
			return false;
		return proxy.getName().equals(m_proxyName);
	}

	/** a new sonar object was created */
	public final void proxyAdded(final TrafficDeviceAttribute proxy) {
		if(proxyMatches(proxy))
			update(proxy);
	}

	/** Change a proxy in the model, called with this user or another
	 *  user changes the state of the attribute */
	protected void proxyChangedSlow(TrafficDeviceAttribute proxy, 
		String attrib) 
	{
		System.err.println("AwsCheckBox.proxyChanged() called.");
		if(!proxyMatches(proxy))
			return;

		boolean pattrib = SString.stringToBoolean(proxy.getAValue());
		System.err.println("AwsCheckBox.proxyChanged(): proxy attribute="+pattrib);
		System.err.println("AwsCheckBox.proxyChanged(): proxy.getAValueBoolean="+proxy.getAValueBoolean());
		if(pattrib!=proxy.getAValueBoolean())
			System.err.println("ERROR....??");	// Doug?

		// checkbox needs to be updated?
		boolean cval = isSelected();
		System.err.println("AwsCheckBox.proxyChanged(): checkbox is currently="+cval);
		if(pattrib != cval) {
			System.err.println("AwsCheckBox.proxyChanged(): checkbox needs to be updated");
			setSelected(pattrib);
		}
		else
			System.err.println("AwsCheckBox.proxyChanged(): checkbox does NOT need to be updated");
	}

	/** a sonar object changed, possibly by another user */
	public final void proxyChanged(final TrafficDeviceAttribute proxy, final String attrib) {
		if(!proxyMatches(proxy))
			return;
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				proxyChangedSlow(proxy, attrib);
			}
		}.addToScheduler();
	}

	/** a sonar object was removed */
	public final void proxyRemoved(final TrafficDeviceAttribute proxy) {
		System.err.println("AwsCheckBox.proxyAdded() proxyRemoved");
		if(proxyMatches(proxy))
			setEnabled(false);
	}

	/** Enumeration of sonar objects complete */
	public void enumerationComplete() {
		// Nothing to do
	}
}

