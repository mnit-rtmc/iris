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
package us.mn.state.dot.tms.client.security.nds;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.novell.beans.NWSess.NWSess;
import com.novell.service.nds.net.NetDistinguishedName;

/**
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.2 $ $Date: 2003/05/15 14:39:42 $
 */
public class NwTest {
	
		/** Class to use for generating the initial context */
	public final static String CONTEXT_FACTORY=
		"com.novell.service.nds.naming.NdsInitialContextFactory";

	/** URL to use for connecting to tree */
	//public final static String NOVELL_URL = "NetWare://DOT-METROTMC.IR.TMC.MSP.DOT";
	public final static String NOVELL_URL = "NetWare://Mndot";
	
	private final static String CONTEXT = ".USER.TMC.MSP.DOT";

	
	/** Creates new NwTest */
    public NwTest() {
    }
    
    public void testSession() {
    	NWSess sess = new NWSess();
		try {
			sess.setAutoClose( true );
			//sess.setBindery( true );
			sess.setConnectionTab( true );
			sess.setRunScripts( false );
			sess.setScriptTab( false );
			sess.setVariablesTab( false );
			sess.setDisplayResults( true );
			//nwSession.login( nwSession.getDefaultFullNameFromTree( "MNDOT" ),
			//	"", "", true );
			sess.login( "NDS:\\\\MNDOT\\dot\\msp\\tmc\\user",
				"", "", true );
		System.out.println( sess.getDefaultFullNameFromTree( "MNDOT" ) );
		}catch( Exception e ) {
			e.printStackTrace();
		}
		Hashtable env = new Hashtable( 5, 0.75f );
		env.put( Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY );
		env.put( Context.PROVIDER_URL, NOVELL_URL );
		DirContext initialContext = null;
		try {
			initialContext = new InitialDirContext( env );
		} catch ( NamingException ne ) {
			ne.printStackTrace();
		}
		try {
			Attributes atts = initialContext.getAttributes( "engs1eri" +
				CONTEXT );
			Attribute members = atts.get( "Group Membership" );
			NamingEnumeration values = members.getAll();
			while ( values.hasMore() ) {
				NetDistinguishedName user = 
					( NetDistinguishedName ) values.next();
				String ndsName = user.getDistinguishedName();
				System.out.println( ndsName );
				//IrisUser newUser = new NdsUser( ndsName, initialContext );
				//group.addMember( newUser );
				//userList.add( newUser.getFullName() );
			}
		} catch ( NamingException ne ) {
			ne.printStackTrace();//FIXME handle exception properly.
		}
		/*NWNetworkNames names = sess.getServerNames();
		while( names.hasMoreElements() ) {
			NWNetworkName name = names.next();
			System.out.println( name.getFullName() );
		}*/
    }
	
	static public void main( String[] args ) {
		NwTest test = new NwTest();
		test.testSession();		
	}

}
