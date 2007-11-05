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

package us.mn.state.dot.tms.client.security.jndi;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import us.mn.state.dot.tms.client.security.LoginEvent;
import us.mn.state.dot.tms.client.security.LoginListener;

/**
 * Tests the NdsUserManager class.
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.2 $ $Date: 2003/09/22 18:28:58 $
 */
public class JndiUserManagerTest extends TestCase implements LoginListener {
	
	private boolean login = false;
	
	private boolean logout = false;
	
	private final JndiUserManager manager = new JndiUserManager();
	
	public JndiUserManagerTest( String testName ) {
		super( testName );
	}
	
	public static void main( java.lang.String[] args ) {
		junit.textui.TestRunner.run( suite() );
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite( JndiUserManagerTest.class );
		return suite;
	}
	
	public void setUp() {
		manager.addLoginListener( this );
	}
	
	public void testLogin() {
		manager.login();
		//assertTrue( "Login was not successful", login );
		assertNotNull( "User is null.", manager.getUser() );
	}
	
	public void testLogout() {
		manager.login();
		assertNotNull( "User is null.", manager.getUser() );
		manager.logout();
		//assertTrue( "Logout was not successful", logout );
		assertNull( "User did not get set to null.", manager.getUser() );
	}
	
	public void login( LoginEvent event ) {
		login = true;
	}	
	
	public void logout() {
		logout = true;
	}
	
}
