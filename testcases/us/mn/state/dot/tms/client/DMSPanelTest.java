/*
 * DMSPanetTest.java
 *
 * Created on July 12, 2001, 5:46 PM
 */

package us.mn.state.dot.tms.client;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.client.dms.DMSPanel;


/**
 *
 * @author    <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version   $Revision: 1.2 $ $Date: 2003/05/15 14:39:42 $
 */
public class DMSPanelTest extends JFrame {

	/** Creates new DMSPanelTest */
    public DMSPanelTest() {
    	super( "DMSPanelTest" );
		DMSPanel dmsPanel = new DMSPanel( );
		JPanel panel = new JPanel( new BorderLayout() );
		setContentPane( panel );
		panel.add( dmsPanel, BorderLayout.CENTER );
		dmsPanel.updateMessage( new SignMessage( "ENGS1ERI",
			"LINE 1 TEST[nl]LINE 2 TEST[nl]LINE 3 TEST", null, 6000 ) );
    }
    
    public void test() {
    	setVisible( true );
    }

    /**
    * @param args the command line arguments
    */
    public static void main ( String args[] ) {
		DMSPanelTest test = new DMSPanelTest();
		test.test();
    }

}
