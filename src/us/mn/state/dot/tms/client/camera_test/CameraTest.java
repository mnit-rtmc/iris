/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 */
package us.mn.state.dot.tms.client.camera_test;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
//import us.mn.state.dot.tms.utils.I18N;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CameraTest extends AbstractForm {

    /**
     * @param args the command line arguments
     */
    
    private static Pipeline pipe;

	/** User session */
	private final Session session;
	
	public CameraTest(Session s) {
		super("Camera Test", true);
		session = s;
		
        Gst.init("CameraTest");
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                SimpleVideoComponent vc = new SimpleVideoComponent();
                Bin bin = Gst.parseBinFromDescription(
                        "autovideosrc ! videoconvert ! capsfilter caps=video/x-raw,width=640,height=480",
                        true);
                pipe = new Pipeline();
                pipe.addMany(bin, vc.getElement());
                Pipeline.linkMany(bin, vc.getElement());           

                JFrame f = new JFrame("Camera Test");
                f.add(vc);
                vc.setPreferredSize(new Dimension(640, 480));
                f.pack();
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                pipe.play();
                f.setVisible(true);
            }
        });
    }

}
