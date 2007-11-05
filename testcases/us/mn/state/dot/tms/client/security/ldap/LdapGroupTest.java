/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.security.ldap;

import java.util.Enumeration;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSecureSocketFactory;
import com.novell.service.security.net.ssl.SSLProvider;

/**
 * LdapTest is a testcase for testing LDAP connectivity. FIXME make this a proper JUnit test.
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.4 $ $Date: 2003/05/15 14:39:42 $
 */
public class LdapGroupTest {

	/** LDAP host to connect to. */
	private final static String LDAP_HOST = "151.111.224.64";

	/** Port on LDAP server to connect to. */
	private final static int LDAP_PORT = LDAPConnection.DEFAULT_SSL_PORT;

	private final LDAPConnection connection = new LDAPConnection(
		new LDAPSecureSocketFactory() );

	/** Creates new LdapTest */
	public LdapGroupTest() {
		java.security.Security.addProvider( new SSLProvider() );
		System.setProperty( "ssl.keystore", "/conf/.keystore" );
	}

	public boolean login( String dn ){
		try {
			connection.connect( LDAP_HOST, LDAP_PORT );
			connection.bind( LDAPConnection.LDAP_V3, null, null );
			LDAPEntry entry = connection.read( dn );
			LDAPAttribute members = entry.getAttribute( "member" );
			Enumeration enum = members.getStringValues();
			System.out.println( "Members of " + dn + ":" );
			while ( enum.hasMoreElements() ) {
				System.out.println( "\t" + (String) enum.nextElement() );
			}
			connection.disconnect();
			return true;
		} catch ( LDAPException e ) {
			//System.out.println( "Error: " + e.toString() );
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * To run this you must pass a DN and a password at the command line.
	 * ie. java us.mn.state.dot.tms.client.security.LdapTest "CN=engs1eri,OU=USER,OU=TMC,OU=MSP,O=DOT" password
	 * You must have nssl1.2_exp.jar and ldap.jar in your classpath.
	 * @param args  the command line arguments
	 */
	public static void main( String args[] ) {
		/*if ( args.length < 2 ) {
			System.out.println( "You must ender a DN and a password" );
			System.exit( 1 );
		}
		String dn = args[0];
		String password = args[1];*/
		String dn = "CN=TMS_Integrators,OU=IR,OU=TMC,OU=MSP,O=DOT";
		System.out.println( "dn = " + dn );
		LdapGroupTest test = new LdapGroupTest();
		test.login( dn );
	}
}
