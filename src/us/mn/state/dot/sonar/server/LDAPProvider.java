/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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

import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

/**
 * LDAP user authentication provider.
 *
 * @author Douglas Lau
 */
public class LDAPProvider implements AuthProvider {

	/** Check that a DN is sane */
	static private boolean isDnSane(String dn) {
		return dn != null && dn.length() > 0;
	}

	/** Get a useful message string from a naming exception */
	static private String namingMessage(NamingException e) {
		Throwable c = e.getCause();
		return c != null ? c.getMessage() :e.getClass().getSimpleName();
	}

	/** Environment for creating a directory context */
	private final Hashtable<String, Object> env =
		new Hashtable<String, Object>();

	/** Create a new LDAP authentication provider */
	public LDAPProvider(String url) {
		env.put(Context.INITIAL_CONTEXT_FACTORY,
			"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, url);
		env.put("com.sun.jndi.ldap.connect.timeout", "5000");
		if(url.startsWith("ldaps")) {
			env.put(Context.SECURITY_PROTOCOL, "ssl");
			env.put("java.naming.ldap.factory.socket",
				LDAPSocketFactory.class.getName());
		}
	}

	/** Get a string representation of the provider (URL) */
	public String toString() {
		Object url = env.get(Context.PROVIDER_URL);
		if(url != null)
			return url.toString();
		else
			return "";
	}

	/** Authenticate a user with an LDAP server.
	 * @param user User to be authenticated.
	 * @param pwd Password to check for user.
	 * @return true if user was authenticated, otherwise false. */
	public boolean authenticate(UserImpl user, char[] pwd) {
		String dn = user.getDn();
		if(isDnSane(dn)) {
			try {
				authenticate(dn, pwd);
				return true;
			}
			catch(AuthenticationException e) {
				// fall to end of method
			}
			catch(NamingException e) {
				TaskProcessor.DEBUG.log(namingMessage(e) +
					" on " + toString());
			}
		}
		// Failed to authenticate
		return false;
	}

	/** Authenticate a dn and password */
	private void authenticate(String dn, char[] pwd)
		throws AuthenticationException, NamingException
	{
		env.put(Context.SECURITY_PRINCIPAL, dn);
		env.put(Context.SECURITY_CREDENTIALS, pwd);
		try {
			InitialDirContext ctx =
				new InitialDirContext(env);
			ctx.close();
		}
		finally {
			// We shouldn't keep these around
			env.remove(Context.SECURITY_PRINCIPAL);
			env.remove(Context.SECURITY_CREDENTIALS);
		}
	}
}
