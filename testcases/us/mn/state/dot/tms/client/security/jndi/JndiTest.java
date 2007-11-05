/*
 * JndiTest.java
 *
 * Created on December 14, 2001, 4:35 PM
 */

package us.mn.state.dot.tms.client.security.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 *
 * @author  engs1eri
 * @version 
 */
public class JndiTest {
	
	/** LDAP host to connect to. */
	//private static final String LDAP_HOST = "dot-nds3";
	private static final String LDAP_HOST  = "dot-nds5";
	
	/** Port on LDAP server to connect to. */
	//private static final int LDAP_PORT = 636;
	private static final int LDAP_PORT = 389;
	
	
	/** Creates new JndiTest */
    public JndiTest() {
		System.setProperty( "javax.net.ssl.keystore", "/home/engs1eri/workspace/iris_client/src/etc/.keystore" );
		System.setProperty( "javax.net.ssl.trustStore", "/home/engs1eri/workspace/iris_client/src/etc/.keystore" );
	}
	
	
	public boolean login( String dn, String password ){
		System.out.println("login");
		Hashtable env = new Hashtable();
		env.put( Context.INITIAL_CONTEXT_FACTORY,
			"com.sun.jndi.ldap.LdapCtxFactory" );
		env.put( Context.PROVIDER_URL, "ldap://" + LDAP_HOST + ":" + LDAP_PORT );
		System.out.println("a");
		//env.put( Context.SECURITY_PROTOCOL, "ssl" );
	//	env.put( Context.SECURITY_PRINCIPAL, dn );
	//	env.put( Context.SECURITY_CREDENTIALS, password );
		try {
			DirContext ctx = new InitialDirContext( env );
			System.out.println("b");
			Attributes atts = ctx.getAttributes( dn );
			System.out.println("starting");
			 ctx.addToEnvironment( Context.SECURITY_PRINCIPAL, dn);
			 ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
			 //ctx.bind("", null);
			 atts=ctx.getAttributes("", null);
			 System.out.println("done");
			 
			NamingEnumeration enum = atts.getIDs();
			while(enum.hasMore()){
				String id = enum.next().toString();
				System.out.println( id + "=" + atts.get(id).get().toString() );
			}
			Attribute groups = atts.get( "groupMembership" );
			 enum = groups.getAll(); 
			while( enum.hasMore() ) {
				System.out.println( enum.next().toString() );
			}
		} catch ( NamingException e ) {
			e.printStackTrace();
			return false;
		}
		return true;
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
		}*/
		String dn = "CN=engs1eri, OU=irm, OU=FNA, OU=CO,O=DOT";//args[0];
		String password = "baetisbaetis";//;args[1];
		JndiTest test = new JndiTest();
		test.login( dn, password );
	}

}
