/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import us.mn.state.dot.sonar.Domain;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.UserImpl;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.utils.SString.countLetters;
import static us.mn.state.dot.tms.utils.SString.countUnique;
import static us.mn.state.dot.tms.utils.SString.isDisplayable;
import static us.mn.state.dot.tms.utils.SString.longestCommonSubstring;

/**
 * IRIS user
 *
 * @author Douglas lau
 */
public class IrisUserImpl extends UserImpl implements Storable {

	/** Get required number of unique characters for a password length */
	static private int uniqueRequirement(int plen) {
		return (plen < 24) ? (plen / 2) : 12;
	}

	/** SQL connection to database */
	static private SQLConnection store;

	/** User/Domain table mapping */
	static private TableMapping mapping;

	/** Lookup all the users */
	static public void lookup(SQLConnection c, final ServerNamespace ns)
		throws TMSException
	{
		store = c;
		mapping = new TableMapping(store, "iris", "user_id",
			Domain.SONAR_TYPE);
		store.query("SELECT name, full_name, password, dn, role, " +
			"enabled FROM iris.user_id;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				ns.addObject(new IrisUserImpl(ns, row));
			}
		});
	}

	/** Lookup a role */
	static private IrisRoleImpl lookupRole(ServerNamespace ns, String name){
		SonarObject so = ns.lookupObject(IrisRoleImpl.SONAR_TYPE, name);
		return (so instanceof IrisRoleImpl) ? (IrisRoleImpl) so : null;
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("full_name", fullName);
		map.put("password", password);
		map.put("dn", dn);
		map.put("role", role);
		map.put("enabled", enabled);
		return map;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris.user_id";
	}

	/** Create a new IRIS user */
	public IrisUserImpl(String n) throws TMSException {
		super(n);
		if (!n.equals(n.toLowerCase())) {
			throw new ChangeVetoException(
				"Must not contain upper-case characters");
		}
		fullName = "";
		password = "";
		dn = "";
		role = null;
		enabled = false;
	}

	/** Create an IRIS user from a database row */
	private IrisUserImpl(ServerNamespace ns, ResultSet row)
		throws SQLException, TMSException
	{
		this(ns, row.getString(1),  // name
		         row.getString(2),  // full_name
		         row.getString(3),  // password
		         row.getString(4),  // dn
		         row.getString(5),  // role
		         row.getBoolean(6)  // enabled
		);
	}

	/** Create an IRIS user from database lookup */
	private IrisUserImpl(ServerNamespace ns, String n, String fn,
		String pwd, String d, String r, boolean e) throws TMSException
	{
		this(n, fn, pwd, d, lookupRole(ns, r), e);
		setDomains(lookupDomains(ns));
	}

	/** Create an IRIS user from database lookup */
	private IrisUserImpl(String n, String fn, String pwd, String d,
		IrisRoleImpl r, boolean e)
	{
		super(n);
		fullName = fn;
		password = pwd;
		dn = d;
		role = r;
		enabled = e;
	}

	/** Lookup all the domains for a user */
	private IrisDomainImpl[] lookupDomains(ServerNamespace ns)
		throws TMSException
	{
		TreeSet<IrisDomainImpl> doms = new TreeSet<IrisDomainImpl>();
		for (String o: mapping.lookup(this)) {
			Object d = ns.lookupObject(Domain.SONAR_TYPE, o);
			if (d instanceof IrisDomainImpl)
				doms.add((IrisDomainImpl) d);
		}
		return doms.toArray(new IrisDomainImpl[0]);
	}

	/** Get the primary key name */
	@Override
	public String getPKeyName() {
		return "name";
	}

	/** Get the primary key */
	@Override
	public String getPKey() {
		return name;
	}

	/** Get a string representation of the object */
	@Override
	public String toString() {
		return name;
	}

	/** Destroy an IRIS user */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Set the full (display) name */
	public void doSetFullName(String n) throws TMSException {
		if (!n.equals(fullName)) {
			store.update(this, "full_name", n);
			super.setFullName(n);
		}
	}

	/** Set the password */
	public void doSetPassword(String pwd) throws TMSException,
		InvalidKeySpecException
	{
		checkPassword(pwd);
		String ph = MainServer.auth_provider.createHash(
			pwd.toCharArray());
		store.update(this, "password", ph);
		setPassword(ph);
	}

	/** Check a password */
	private void checkPassword(String pwd) throws ChangeVetoException {
		final int plen = pwd.length();
		if (plen < 8) {
			throw new ChangeVetoException(
				"Must be at least 8 characters");
		}
		if (!isDisplayable(pwd)) {
			throw new ChangeVetoException(
				"All characters must be displayable");
		}
		String lpwd = pwd.toLowerCase();
		String c = longestCommonSubstring(name.toLowerCase(), lpwd);
		if (c.length() > 4)
			throw new ChangeVetoException("Based on user name");
		if (longestCommonSubstring("password", lpwd).length() > 4)
			throw new ChangeVetoException("Invalid password");
		int n_let = countLetters(pwd);
		if (0 == n_let)
			throw new ChangeVetoException("Must contain letters");
		if (plen < 20 && plen == n_let) {
			throw new ChangeVetoException(
				"Must contain non-letters");
		}
		if (countUnique(pwd) < uniqueRequirement(plen)) {
			throw new ChangeVetoException(
				"Must contain more unique characters");
		}
	}

	/** Get the password */
	public String getPassword() {
		return password;
	}

	/** Set the LDAP distinguished name */
	public void doSetDn(String d) throws TMSException {
		if (!d.equals(dn)) {
			store.update(this, "dn", d);
			super.setDn(d);
		}
	}

	/** Set the role assigned to the user */
	public void doSetRole(Role r) throws TMSException {
		if (r != role) {
			store.update(this, "role", r);
			super.setRole(r);
		}
	}

	/** Set the enabled flag */
	public void doSetEnabled(boolean e) throws TMSException {
		if (e != enabled) {
			store.update(this, "enabled", e);
			super.setEnabled(e);
		}
	}

	/** Set the domains assigned to the user */
	public void doSetDomains(Domain[] doms) throws TMSException {
		TreeSet<Storable> dset = new TreeSet<Storable>();
		for (Domain d: doms) {
			if (d instanceof IrisDomainImpl)
				dset.add((IrisDomainImpl) d);
			else
				throw new ChangeVetoException("Bad domain");
		}
		mapping.update(this, dset);
		setDomains(doms);
	}
}
