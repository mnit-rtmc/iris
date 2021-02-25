/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
import java.util.Set;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;

/**
 * Table model for DMS sign groups.
 *
 * @author Douglas Lau
 */
public class DmsGroupModel extends ProxyTableModel<DMS> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<DMS> descriptor(Session s) {
		return new ProxyDescriptor<DMS>(
			s.getSonarState().getDmsCache().getDMSs(),
			false,	/* has_properties */
			false,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<DMS>> createColumns() {
		ArrayList<ProxyColumn<DMS>> cols =
			new ArrayList<ProxyColumn<DMS>>(2);
		cols.add(new ProxyColumn<DMS>("dms", 100) {
			public Object getValueAt(DMS dms) {
				return dms.getName();
			}
		});
		cols.add(new ProxyColumn<DMS>("dms.group.member", 120,
			Boolean.class)
		{
			public Object getValueAt(DMS dms) {
				return signs.contains(dms);
			}
			public boolean isEditable(DMS dms) {
				return canEditDmsSignGroup(dms);
			}
			public void setValueAt(DMS dms, Object value) {
				if (value instanceof Boolean) {
					Boolean b = (Boolean) value;
					if (b.booleanValue())
						createDmsSignGroup(dms);
					else
						destroyDmsSignGroup(dms);
				}
			}
		});
		return cols;
	}

	/** Sign group */
	private final SignGroup sign_group;

	/** Signs in group */
	private final Set<DMS> signs;

	/** DMS sign group type cache */
	private final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Listener for DMS sign group proxy events */
	private final SwingProxyAdapter<DmsSignGroup> listener =
		new SwingProxyAdapter<DmsSignGroup>(false)
	{
		protected void proxyAddedSwing(DmsSignGroup dsg) {
			updateSign(dsg.getDms());
		}
		protected void proxyRemovedSwing(DmsSignGroup dsg) {
			updateSign(dsg.getDms());
		}
		protected boolean checkAttributeChange(String attr) {
			return false;
		}
	};

	/** Create a new DMS group model */
	public DmsGroupModel(Session s, SignGroup sg) {
		super(s, descriptor(s), 15);
		sign_group = sg;
		signs = SignGroupHelper.getAllSigns(sg);
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

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return false;
	}

	/** Update a sign member value */
	private void updateSign(DMS dms) {
		signs.clear();
		signs.addAll(SignGroupHelper.getAllSigns(sign_group));
		proxyChangedSwing(dms);
	}

	/** Check if the user is allowed to add / destroy a DMS sign group */
	private boolean canEditDmsSignGroup(DMS dms) {
		String name = createDmsSignGroupName(dms);
		return sign_group != null && 
		       session.canWrite(DmsSignGroup.SONAR_TYPE, name);
	}

	/** Create a DMS sign group name */
	private String createDmsSignGroupName(DMS dms) {
		return sign_group.getName() + "_" + dms.getName();
	}

	/** Create a new DMS sign group */
	private void createDmsSignGroup(DMS dms) {
		String oname = createDmsSignGroupName(dms);
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("dms", dms);
		attrs.put("sign_group", sign_group);
		dms_sign_groups.createObject(oname, attrs);
	}

	/** Destroy a DMS sign group */
	private void destroyDmsSignGroup(DMS dms) {
		DmsSignGroup dsg = DmsSignGroupHelper.find(dms, sign_group);
		if (dsg != null)
			dsg.destroy();
	}
}
