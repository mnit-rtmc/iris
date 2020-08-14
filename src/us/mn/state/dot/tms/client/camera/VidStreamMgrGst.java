/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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
package us.mn.state.dot.tms.client.camera;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;

import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.UserProperty;
import us.mn.state.dot.tms.client.widget.ZipDownloader;
import us.mn.state.dot.tms.utils.I18N;

import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.GstException;
import org.freedesktop.gstreamer.GstObject;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.State;
import org.freedesktop.gstreamer.elements.AppSink;

/**
 * Stream manager for a GStreamer stream.
 *
 * @author John L. Stanley - SRF Consulting Group
 */
public class VidStreamMgrGst extends VidStreamMgr {

	Element srcElem;
	VidComponentGst gstComponent;

	/**
	 * Create a GStreamer stream manager.
	 * @param vp The VideoPanel to use.
	 * @param vr The StreamReq to use.
	 */
	protected VidStreamMgrGst(VidPanel vp, VidStreamReq vr) {
		super(vp, vr);
//		Gst.init("StreamMgrGst");
//		setComponent(createScreenPanel());
	}

	@Override
	/** Are we currently streaming? */
	public boolean isStreaming() {
		return (pipe != null);
	}

	@Override
	protected void doStartStream() {
		if (bStreaming)
			return;
		openStream();
	}

	@Override
	protected void doStopStream() {
		if (pipe != null)
			closeStream();
	}

	/** Does config appear to be OK for this video manager? */
	public static boolean isOkConfig(String config) {
		return (config.contains(" ! ") && isGstInstalled());
	}

	/** Most recent streaming state.  State variable for event FSM. */
	private boolean bStreaming = false;

	private Pipeline pipe;

	private AppSink appsink;
	private Bus     bus;
	private AppSinkListener appsinkListener;
	private BusListener     busListener;

	/** Open the video stream */
	private synchronized boolean openStream() {
		try {
			pipe = (Pipeline)Gst.parseLaunch(
					vreq.getConfig()+" ! appsink name=appsink");
			appsink = (AppSink) pipe.getElementByName("appsink");
			List<Element> elements = pipe.getElements();
			srcElem = elements.get(0);
			gstComponent = new VidComponentGst(appsink, this);
			appsinkListener = new AppSinkListener();
			appsink.connect((AppSink.NEW_SAMPLE) appsinkListener);
			appsink.connect((AppSink.EOS)        appsinkListener);
			bus = pipe.getBus();
			busListener = new BusListener();
			bus.connect((Bus.ERROR)  busListener);
			bus.connect((Bus.WARNING)busListener);
			bus.connect((Bus.INFO)   busListener);
			pipe.play();
			setComponent(gstComponent);
			return true;
		} catch (GstException e) {
			setErrorMsg(e, "Unknown GStreamer error");
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			setErrorMsg(e, "Unknown error");
			e.printStackTrace();
			return false;
		}
	}

	/** Close the video stream */
	private synchronized void closeStream() {
		Pipeline p = pipe;
		if (p != null) {
			// NOTE - uncomment the lines below to generate debug DOT files of
			// the GStreamer pipeline. You must have the GST_DEBUG_DUMP_DOT_DIR
			// environment variable set for this to work.
//			pipe.debugToDotFile(Bin.DebugGraphDetails.SHOW_ALL,
//					getDOTFileName());
			pipe = null;
			if (appsink == null)
				System.out.println("appsink is null");
			else {
				appsink.disconnect((AppSink.NEW_SAMPLE) appsinkListener);
				appsink.disconnect((AppSink.EOS)        appsinkListener);
			}
			if (bus == null)
				System.out.println("bus is null");
			else {
				bus.disconnect((Bus.ERROR)  busListener);
				bus.disconnect((Bus.WARNING)busListener);
				bus.disconnect((Bus.INFO)   busListener);
			}
			gstComponent.disconnectAll();
			gstComponent = null;
//			setComponent(null);
//			p.stop();
//			p.close();
			try {
				p.setState(State.NULL);
				p.getState();
				System.out.println("CloseStream finished");
			}
			catch (java.lang.IllegalStateException e) {
				e.printStackTrace();
			}
//			setStatus("");
		}
	}

	/** Generate a DOT filename with the current date and time. */
	private String getDOTFileName() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
				"yyyyMMdd-HHmmss_SSS");
		return String.format("pipe_dbg_%s", dtf.format
				(LocalDateTime.now()));
	}

	/** Listen for start and stop of video stream */
	private class AppSinkListener implements AppSink.NEW_SAMPLE, AppSink.EOS {

		@Override
		public FlowReturn newSample(AppSink elem) {
			if (bStreaming) {
				// if we've been dropped, stop the stream
				if (videoPanel.getStreamMgr() != VidStreamMgrGst.this)
					queueStopStream();
			}
			else {
				streamingStarted();
				bStreaming = true;
			}
			return FlowReturn.OK;
		}

		@Override
		public void eos(AppSink elem) {
			if (bStreaming) {
				streamingStopped();
				System.out.println("### EOS");
//				dumpElement(elem, "EOS");
//				dumpElement(srcElem, "End");
				bStreaming = false;
			}
		}
	}

	/** Listen for pipeline messages */
	private class BusListener implements Bus.ERROR, Bus.WARNING, Bus.INFO {

		/**
		 * Called when a {@link Pipeline} element posts an error message.
		 *
		 * @param source the element which posted the message.
		 * @param code a numeric code representing the error.
		 * @param message a string representation of the error.
		 */
		public void errorMessage(GstObject source, int code, String message) {
			System.out.println("### ERROR: "+code+", "+message);
			setErrorMsg(message);
		}

		/**
		 * Called when a {@link Pipeline} element posts an warning message.
		 *
		 * @param source the element which posted the message.
		 * @param code a numeric code representing the warning.
		 * @param message a string representation of the warning.
		 */
		public void warningMessage(GstObject source, int code, String message) {
			System.out.println("### WARNING: "+code+", "+message);
		}

		/**
		 * Called when a {@link Pipeline} element posts an informational
		 * message.
		 *
		 * @param source the element which posted the message.
		 * @param code a numeric code representing the informational message.
		 * @param message a string representation of the informational message.
		 */
		public void infoMessage(GstObject source, int code, String message) {
			System.out.println("### INFO: "+code+", "+message);
		}

	}

	public void dumpElement(Element elem, String title) {
		System.out.println("## "+ title);
		List<String> names = elem.listPropertyNames();
		System.out.println("### "+ names);
		for (String name : names) {
			System.out.println("###-- "+name+" = "+elem.get(name).toString());
		}
	}

	private static Boolean bGstInstalled = null;

	/** See if we have access to GStreamer library */
	public static boolean isGstInstalled() {
		if (bGstInstalled == null) {
			// test to see if we can load GStreamer with whatever environment
			// settings we have now
			bGstInstalled = testGst();

			// if we didn't get it and we're using WebStart, try to load from
			// a JAR from the server
			if (!bGstInstalled && isRunningJavaWebStart()) {
				bGstInstalled = false;
				checkDownloadGStreamer();
			} else
				initGst();
		}
		return bGstInstalled;
	}

	/** Test whether the GStreamer native library is installed using a
	 *  temporary class loader. Testing is performed in a contained environment
	 *  to allow proper initialization later after the JNA path is modified.
	 */
	private static boolean testGst() {
		ClassLoader cl = VidStreamMgr.class.getClassLoader();
		Method m;
		System.out.println("Checking for existing GStreamer installation...");
		try {
			// load the low-level classes we need from GStreamer
			Class gstApi = cl.loadClass(
					"org.freedesktop.gstreamer.lowlevel.GstAPI");
			Class<?> gstNative = cl.loadClass(
					"org.freedesktop.gstreamer.lowlevel.GstNative");

			// try the load method
			m = gstNative.getDeclaredMethod("load", Class.class);
			m.invoke(null, gstApi);

			// nullify everything to allow garbage collection
			gstNative = null;
			gstApi = null;
			System.out.println("GStreamer installation found!");
			return true;
		} catch (ClassNotFoundException | NoSuchMethodException
				| SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
//			e.printStackTrace();
			// loading failed, probably/hopefully because we don't have
			// GStreamer installed
		} finally {
			// garbage collect the class loader environment we used for testing
			// so we can reinit Gst/JNA later after modifying the library path
			m = null;
			cl = null;
			System.gc();
		}
		System.out.println("NO GStreamer installation found!");
		return false;
	}

	/** Try to initialize GStreamer. */
	private static boolean initGst() {
		try {
			Gst.init("StreamMgrGst");
			System.out.println("GStreamer "+Gst.getVersionString()+" installed");
			return true;
		} catch (GstException|UnsatisfiedLinkError ex) {
			System.out.println("GStreamer not available: "+ex.getMessage());
			return false;
		}
	}

	/** Test if we're running via Java WebStart */
	private static boolean isRunningJavaWebStart() {
		boolean hasJNLP = false;
		try {
			Class.forName("javax.jnlp.ServiceManager");
			hasJNLP = true;
			System.out.println("Running in WebStart");
		} catch (ClassNotFoundException ex) {
			hasJNLP = false;
			System.out.println("NOT Running in WebStart");
		}
		return hasJNLP;
	}

	/** GStreamer native library naming conventions for building directory/
	 *  zipfile name. Combined with the version (taken from a system attribute)
	 *  it will look something like this:
	 *  	gstreamer-1.0-mingw-x86_64-1.16.2[.zip]
	 *  	gstreamer-1.0-linux-x86_64-1.17.1[.zip]
	 *
	 *  TODO may ultimately want to make this system attributes too.
	 */
	private static final String GST_SERVER_DIR = "/iris-client/lib/";
	private static final String GST_BASE = "gstreamer-1.0";
	private static final String GST_WIN = "mingw";

	// TODO Linux isn't working yet - difficult to package like Windows, but
	// there is promise from gst-build's new gstreamer-full feature
	private static final String GST_LINUX = "linux";
	// TODO BSD/other Unix would probably be supported along with Linux, but
	// should test first
	// TODO Mac (probably won't support for a while)
	private static final String GST_ARCH_32 = "x86";
	private static final String GST_ARCH_64 = "x86_64";

	/** Paths inside GStreamer directory (combined with other constants) */
	private static final String GST_BIN_DIR = "bin";
	private static final String GST_LIB_DIR = "lib"; // TODO not needed?

	/** Check for a private copy of the native GStreamer binaries in the
	 *  user's $HOME/iris/ directory (the same place where user properties
	 *  are stored). If not found, the appropriate binaries for the current
	 *  platform are downloaded automatically. In both cases, the Java system
	 *  path is modified to allow gst1-java-core to find the native library.
	 *
	 *  This should only be run if GStreamer is not found with the current
	 *  environment settings, and only if running in WebStart.
	 */
	private static void checkDownloadGStreamer() {
		// get the $HOME/iris directory and make sure it exists
		File uIris = UserProperty.getDir();
		if (!uIris.canWrite())
			uIris.mkdirs();

		// check the current OS and architecture
		String gstOS = null;
		String gstVersion = "@@GSTREAMER.VERSION@@";

		if (Platform.isWindows())
			gstOS = GST_WIN;
		else if (Platform.isLinux())
			gstOS = GST_LINUX;
		// TODO Mac, BSD

		// TODO no ARM support, maybe later
		String gstArch = Platform.is64Bit() ? GST_ARCH_64 : GST_ARCH_32;

		// if we don't have a matching OS and architecture, we're done
		if (gstOS == null || gstArch == null)
			return;

		// look for a directory with the files we want (indicated by the name)
		String gstDirName = GST_BASE + "-" + gstOS
			+ "-" + gstArch + "-" + gstVersion;

		File gstDir = new File(uIris, gstDirName);
		String gstPath;
		try {
			gstPath = gstDir.getCanonicalPath().toString();
		} catch (IOException e1) { return; }

		// check for the directory
		if (!gstDir.isDirectory()) {
			// if we can't find it, download it as a zip file from the server
			String gstZipFile = gstDirName + ".zip";

			// get the server host from the client properties and build the URL
			// we will take the host:port from SONAR and strip off the :port
			String hostport = Session.getCurrent().getSonarState().getName();
			String host = hostport.split(":")[0];
			String urlStr = "http://" + host + GST_SERVER_DIR + gstZipFile;

			// check the server for the file - if we can't find it, we're done
			URL url;
			try {
				url = new URL(urlStr);

			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.out.println("Could not download GStreamer from server"
						+ "With URL: '" + urlStr + "'");
				return;
			}

			// create an action to be completed when the download completes
			Action addToPath = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					addUserGstToPath(gstPath, true);
				}
			};

			// create a ZipDownloader, show it, and start the download
			ZipDownloader zd = new ZipDownloader(
					"camera.gstreamer.downloading.title",
					"camera.gstreamer.downloading.msg", url, gstDir);
			JInternalFrame f = Session.getCurrent().getDesktop().show(zd);
			zd.setFrame(f);

			// addToPath will be run when the download is complete
			zd.execute(addToPath);
		} else
			// if we already have the directory, add it to the path
			addUserGstToPath(gstPath, false);
	}

	private static void addUserGstToPath(String gstPath, boolean confirm) {
		// build the full paths we need
		String libPath = Paths.get(gstPath, GST_LIB_DIR).toString();
		String binPath = Paths.get(gstPath, GST_BIN_DIR).toString();

		// add the gstDir to the system path
		if (Platform.isWindows()) {
			try {
				Kernel32 k32 = Kernel32.INSTANCE;
				String path = System.getenv("path");
				System.out.println("path: " + path);
				if (path == null || path.trim().isEmpty()) {
					k32.SetEnvironmentVariable("path", binPath);
				} else {
					k32.SetEnvironmentVariable("path", binPath
						+ File.pathSeparator + path);
				}
				char[] pth = new char[32767];
				k32.GetEnvironmentVariable("path", pth, 32767);
				System.out.println(new String(pth));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			String jnaPath = System.getProperty(
				"jna.library.path", "").trim();
			System.out.println(jnaPath);
			if (jnaPath.isEmpty()) {
			    System.setProperty("jna.library.path", binPath
					+ File.pathSeparator + libPath);
			} else {
			    System.setProperty("jna.library.path",
					binPath + File.pathSeparator + libPath
					+ File.pathSeparator + jnaPath);
			}
			System.out.println(jnaPath);
		}

		// try to initialize GStreamer now
		bGstInstalled = initGst();

		if (confirm) {
			// show a success/failure dialog to the user
			String title = bGstInstalled
					? I18N.get("camera.gstreamer.downloading.success")
						: I18N.get("camera.gstreamer.downloading.failed");
			String msg = bGstInstalled
					? I18N.get("camera.gstreamer.downloading.success.msg")
						: I18N.get("camera.gstreamer.downloading.failed.msg");
			JOptionPane.showConfirmDialog(Session.getCurrent().getDesktop(),
					msg, title, JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE);
		}
	}
}
