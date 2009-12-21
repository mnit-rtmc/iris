/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.system;

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * The system attribute allows administrators to change system-wide policy
 * attributes.
 *
 * @author Douglas Lau
 */
public class SystemAttributeForm extends AbstractForm {

	/** Frame title */
	static private final String TITLE = "System Attributes";

	/** SystemAttribute type cache */
	protected final TypeCache<SystemAttribute> cache;

	/** SONAR namespace */
	protected final Namespace namespace;

	/** System attribute editor tab */
	protected final SystemAttributeTab systemAttributeTab;

	/** SONAR User for permission checks */
	protected final User user;

	/** Create a new system attribute form */
	public SystemAttributeForm(SonarState st, User u) {
		super(TITLE);
		setHelpPageName("Help.SystemAttributeForm");
		cache = st.getSystemAttributes();
		namespace = st.getNamespace();
		user = u;
		systemAttributeTab = new SystemAttributeTab(cache, this);
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		add(systemAttributeTab);
	}

	/** Dispose of the form */
	protected void dispose() {
		systemAttributeTab.dispose();
	}

	/** Check if the user can add the named attribute */
	public boolean canAdd(String name) {
		return name != null && namespace.canAdd(user,
			new Name(SystemAttribute.SONAR_TYPE, name));
	}

	/** Check if the user can update the named attribute */
	public boolean canUpdate(String oname) {
		return oname != null && namespace.canUpdate(user,
			new Name(SystemAttribute.SONAR_TYPE, oname));
	}

	/** Check if the user can update the named attribute */
	public boolean canUpdate(String oname, String aname) {
		return namespace.canUpdate(user,
			new Name(SystemAttribute.SONAR_TYPE, oname, aname));
	}

	/** Check if the user can change the named attribute */
	public boolean canChange(String oname) {
		SystemAttribute sa = cache.lookupObject(oname);
		if(sa != null)
			return canUpdate(oname);
		else
			return canAdd(oname);
	}

	/** Check if the user can remove a system attribute */
	public boolean canRemove(String name) {
		return name != null && namespace.canRemove(user,
			new Name(SystemAttribute.SONAR_TYPE, name));
	}
}
