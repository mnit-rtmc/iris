/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.server;

import java.net.InetAddress;
import javax.naming.CommunicationException;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.Work;
import us.mn.state.dot.sched.Worker;
import us.mn.state.dot.sonar.Domain;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.utils.CidrBlock;
import us.mn.state.dot.tms.server.HashProvider;

/**
 * Simple class to authenticate a user with an LDAP server.
 *
 * @author Douglas Lau
 */
public class Authenticator {

	/** Check that a user is enabled */
	static private boolean isUserEnabled(UserImpl u) {
		return u.getEnabled()
		    && isRoleEnabled(u.getRole());
	}

	/** Check that a role is enabled */
	static private boolean isRoleEnabled(Role r) {
		return (r != null) && r.getEnabled();
	}

	/** Check whethere a distinguished name is set */
	static private boolean isDnSet(String dn) {
		return (dn != null) && (dn.length() > 0);
	}

	/** Check that a password is sane */
	static private boolean isPasswordSane(char[] pwd) {
		return (pwd != null) && (pwd.length >= 6);
	}

	/** Clear a password buffer in memory */
	static private void clearPassword(char[] pwd) {
		for (int i = 0; i < pwd.length; i++)
			pwd[i] = '\0';
	}

	/** Authentication thread */
	private final Worker auth_sched = new Worker("sonar_auth",
		new ExceptionHandler()
	{
		public boolean handle(Exception e) {
			System.err.println("SONAR: auth_sched error " +
				e.getMessage());
			e.printStackTrace();
			return true;
		}
	});

	/** Task processor */
	private final TaskProcessor processor;

	/** Password hash authentication provider */
	private final HashProvider hash_provider;

	/** LDAP authentication provider (optional) */
	private LdapProvider ldap_provider;

	/** Set LDAP authentication provider */
	public void setLdapProvider(final LdapProvider lp) {
		auth_sched.addWork(new Work() {
			public void perform() {
				ldap_provider = lp;
			}
		});
	}

	/** Create a new user authenticator */
	public Authenticator(TaskProcessor tp, HashProvider hp) {
		processor = tp;
		hash_provider = hp;
		ldap_provider = null;
	}

	/** Authenticate a user connection */
	void authenticate(final ConnectionImpl c, final UserImpl user,
		final char[] password)
	{
		if (user != null) {
			auth_sched.addWork(new Work() {
				public void perform() {
					doAuthenticate(c, user, password);
				}
			});
		}
	}

	/** Perform a user authentication (auth_sched thread) */
	private void doAuthenticate(ConnectionImpl c, UserImpl user,
		char[] pwd)
	{
		try {
			if (authenticate(c, user, user.getName(), pwd)) {
				processor.finishLogin(c, user);
			}
		}
		finally {
			clearPassword(pwd);
		}
	}

	/** Authenticate a user's credentials (auth_sched thread) */
	private boolean authenticate(ConnectionImpl c, UserImpl user,
		String name, char[] pwd)
	{
		if (!isUserEnabled(user) || !isPasswordSane(pwd)) {
			processor.failLogin(c, name, false);
			return false;
		}
		if (!checkDomain(c, user)) {
			processor.failLogin(c, name, true);
			return false;
		}
		String dn = user.getDn();
		if (isDnSet(dn) && ldap_provider != null) {
			try {
				if (ldap_provider.authenticate(dn, pwd)) {
					// update cached password hash
					processor.finishPassword(c, user, pwd);
					return true;
				}
			}
			catch (CommunicationException e) {
				// error communicating with LDAP server;
				// fall thru and use hash_provider
			}
		}
		if (hash_provider.authenticate(user, pwd)) {
			return true;
		} else {
			processor.failLogin(c, name, false);
			return false;
		}
	}

	/** Check if user is connecting from an allowed domain */
	private boolean checkDomain(ConnectionImpl c, UserImpl user) {
		if (c != null && user != null) {
			Role role = user.getRole();
			if (isRoleEnabled(role)) {
				InetAddress addr = c.getAddress();
				for (Domain d : role.getDomains()) {
					if (checkDomain(d, addr))
						return true;
				}
			}
		}
		return false;
	}

	/** Check if an address is within a domain */
	private boolean checkDomain(Domain d, InetAddress addr) {
		try {
			return d.getEnabled()
			    && new CidrBlock(d.getBlock()).matches(addr);
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	/** Change a user password */
	void changePassword(final ConnectionImpl c, final UserImpl u,
		final char[] pwd_current, final char[] pwd_new)
	{
		if (u != null) {
			auth_sched.addWork(new Work() {
				public void perform() {
					doChangePassword(c, u, pwd_current,
						pwd_new);
				}
			});
		}
	}

	/** Perform a user password change (auth_sched thread) */
	private void doChangePassword(ConnectionImpl c, UserImpl user,
		char[] pwd_current, char[] pwd_new)
	{
		try {
			String dn = user.getDn();
			if (isDnSet(dn)) {
				processor.failPassword(c, PermissionDenied.
					authenticationFailed().getMessage());
			} else if (authenticate(c, user, user.getName(),
				pwd_current))
			{
				processor.finishPassword(c, user, pwd_new);
			} else {
				processor.failPassword(c, PermissionDenied.
					authenticationFailed().getMessage());
			}
		}
		finally {
			clearPassword(pwd_current);
			clearPassword(pwd_new);
		}
	}
}
