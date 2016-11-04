/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2016  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.HashMap;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;

/**
 * Table model for sign groups.
 *
 * @author Douglas Lau
 */
public class SignGroupTableModel extends ProxyTableModel<SignGroup> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<SignGroup> descriptor(Session s) {
		return new ProxyDescriptor<SignGroup>(
			s.getSonarState().getDmsCache().getSignGroups(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<SignGroup>> createColumns() {
		ArrayList<ProxyColumn<SignGroup>> cols =
			new ArrayList<ProxyColumn<SignGroup>>(3);
		cols.add(new ProxyColumn<SignGroup>("dms.group", 200) {
			public Object getValueAt(SignGroup sg) {
				return sg.getName();
			}
		});
		cols.add(new ProxyColumn<SignGroup>("dms.group.member", 50,
			Boolean.class)
		{
			public Object getValueAt(SignGroup sg) {
				return isSignGroupMember(sg);
			}
			public boolean isEditable(SignGroup sg) {
				return canEditDmsSignGroup(sg);
			}
			public void setValueAt(SignGroup sg, Object value) {
				if (value instanceof Boolean) {
					Boolean b = (Boolean)value;
					if (b.booleanValue())
						createDmsSignGroup(sg);
					else
						destroyDmsSignGroup(sg);
				}
			}
		});
		cols.add(new ProxyColumn<SignGroup>("dms.group.hidden", 50,
			Boolean.class)
		{
			public Object getValueAt(SignGroup sg) {
				return sg.getHidden();
			}
			public boolean isEditable(SignGroup sg) {
				return canUpdate(sg);
			}
			public void setValueAt(SignGroup sg, Object value) {
				if (value instanceof Boolean)
					sg.setHidden((Boolean) value);
			}
		});
		return cols;
	}

	/** DMS identifier */
	private final DMS dms;

	/** DMS sign group type cache */
	private final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Listener for DMS sign group proxy events */
	private final SwingProxyAdapter<DmsSignGroup> listener =
		new SwingProxyAdapter<DmsSignGroup>(true)
	{
		protected void proxyAddedSwing(DmsSignGroup proxy) {
			SignGroupTableModel.this.proxyChangedSwing(
				proxy.getSignGroup());
		}
		protected void proxyRemovedSwing(DmsSignGroup proxy) {
			SignGroupTableModel.this.proxyChangedSwing(
				proxy.getSignGroup());
		}
		protected boolean checkAttributeChange(String attr) {
			return false;
		}
	};

	/** 
	 * Create a new sign group table model.
	 * @param s Session
	 * @param dms DMS proxy object.
	 */
	public SignGroupTableModel(Session s, DMS proxy) {
		super(s, descriptor(s), 12);
		dms = proxy;
		dms_sign_groups =
			s.getSonarState().getDmsCache().getDmsSignGroups();
	}

	/** Initialize the proxy table model */
	@Override
	public void initialize() {
		super.initialize();
		dms_sign_groups.addProxyListener(listener);
	}

	/** Dispose of the proxy table model */
	@Override
	public void dispose() {
		dms_sign_groups.removeProxyListener(listener);
		super.dispose();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(SignGroup proxy) {
		return isListed(proxy);
	}

	/** Check if a sign group should be listed */
	private boolean isListed(SignGroup group) {
		if (!group.getLocal())
			return true;
		else
			return dms.getName().equals(group.getName());
	}

	/** Lookup a DMS sign group */
	private DmsSignGroup lookupDmsSignGroup(SignGroup group) {
		for (DmsSignGroup g: dms_sign_groups) {
			if (g.getSignGroup() == group && g.getDms() == dms)
				return g;
		}
		return null;
	}

	/** Test if the DMS is a member of a sign group */
	private boolean isSignGroupMember(SignGroup group) {
		return lookupDmsSignGroup(group) != null;
	}

	/** Check if the user is allowed to add / destroy a DMS sign group */
	private boolean canEditDmsSignGroup(SignGroup g) {
		return g != null && canAddAndRemove(createDmsSignGroupName(g));
	}

	/** Create a DMS sign group name */
	private String createDmsSignGroupName(SignGroup sg) {
		return sg.getName() + "_" + dms.getName();
	}

	/** Check if the user can add and remove the specified name */
	private boolean canAddAndRemove(String oname) {
		return session.canAdd(DmsSignGroup.SONAR_TYPE, oname) &&
		       session.canRemove(DmsSignGroup.SONAR_TYPE, oname);
	}

	/** Create a new sign group */
	@Override
	public void createObject(String name) {
		boolean local = name.equals(dms.getName());
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("local", local);
		descriptor.cache.createObject(name, attrs);
	}

	/** Create a new DMS sign group */
	private void createDmsSignGroup(SignGroup g) {
		String oname = createDmsSignGroupName(g);
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("dms", dms);
		attrs.put("sign_group", g);
		dms_sign_groups.createObject(oname, attrs);
	}

	/** Destroy a DMS sign group */
	private void destroyDmsSignGroup(SignGroup g) {
		DmsSignGroup dsg = lookupDmsSignGroup(g);
		if (dsg != null)
			dsg.destroy();
	}

	/** Check if the user can remove a sign group */
	@Override
	public boolean canRemove(SignGroup sg) {
		return super.canRemove(sg) && !hasReferences(sg);
	}

	/** Check if a sign group has references */
	private boolean hasReferences(SignGroup sg) {
		return SignGroupHelper.hasMembers(sg)
		    || SignGroupHelper.hasSignText(sg);
	}
}
