/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2024  SRF Consulting Group
 * Copyright (C) 2020       Minnesota Department of Transportation
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;

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
 * @author John L. Stanley - SRF Consulting
 * @author Gordon Parikh   - SRF Consulting
 */
public class VidStreamMgrGst extends VidStreamMgr {

	/** Location on web server where GStreamer zip files are stored */
	static private final String GST_SERVER_DIR = "/iris-gstreamer/";

	/** Base file name for gstreamer zip */
	static private final String GST_BASE = "gstreamer-1.0";

	/** OS part for windows gstreamer zip */
	static private final String GST_WIN = "mingw";

	/** Architecture part for 32-bit x86 gstreamer zip */
	static private final String GST_ARCH_32 = "x86";

	/** Architecture part for 64-bit x86 gstreamer zip */
	static private final String GST_ARCH_64 = "x86_64";

	/** Binary path inside GStreamer directory */
	static private final String GST_BIN_DIR = "bin";

	/** Is Gstreamer installed? */
	static private boolean GST_INSTALLED;
	static {
		GST_INSTALLED = testGst();
	}

	/** Test whether the GStreamer native library is installed using a
	 *  temporary class loader.  Testing is performed in a contained
	 *  environment to allow proper initialization later after the JNA path
	 *  is modified.  */
	static private boolean testGst() {
		ClassLoader cl = VidStreamMgr.class.getClassLoader();
		Method m;
		System.out.println("Checking for GStreamer installation...");
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
		} catch (ClassNotFoundException | IllegalAccessException
			| IllegalArgumentException | NoSuchMethodException
			| InvocationTargetException | SecurityException e)
		{
			// loading failed, probably/hopefully because we don't
			// have GStreamer installed
		}
		finally {
			// garbage collect the class loader environment we used
			// for testing so we can reinit Gst/JNA later after
			// modifying the library path
			m = null;
			cl = null;
			System.gc();
		}
		System.out.println("NO GStreamer installation found!");
		return false;
	}

	/** Does config appear to be OK for this video manager? */
	static public boolean isOkConfig(String config) {
		return config.contains(" ! ") && isGstInstalled();
	}

	/** See if we have access to GStreamer library */
	static private boolean isGstInstalled() {
		if (!GST_INSTALLED)
			checkGstInstall();
		else
			initGst();
		return GST_INSTALLED;
	}

	/** Have we checked Gstreamer install yet? */
	static private boolean GST_CHECKED;

	/** Check whether GStreamer is installed locally or can be.
	 *
	 * The native GStreamer binaries are stored in the user's $HOME/iris/
	 * directory (the same place where user properties are stored).  If not
	 * found, the appropriate binaries are downloaded automatically.  In
	 * both cases, the Java system path is modified to allow gst1-java-core
	 * to find the native library.
	 *
	 * Synchronized to prevent starting multiple downloads. */
	static synchronized private void checkGstInstall() {
		if (!GST_CHECKED) {
			File path = makeGstPath();
			if (path != null) {
				if (path.isDirectory())
					checkGstInstallPrivate(path);
				else
					startGstDownload(path);
			}
		}
		GST_CHECKED = true;
	}

	/** Make local GStreamer library path */
	static private File makeGstPath() {
		String name = makeGstName();
		if (name != null) {
			File user_dir = UserProperty.getDir();
			if (!user_dir.canWrite())
				user_dir.mkdirs();
			File dir = new File(user_dir, name);
			try {
				return dir.getCanonicalFile();
			}
			catch (IOException e) {
				System.out.println("GStreamer path invalid (" +
					dir + "): " + e.getMessage());
			}
		}
		return null;
	}

	/** Make GStreamer native library name.
	 *  The name combined OS, arch and gstreamer version, like this:
	 *  	`gstreamer-1.0-mingw-x86_64-1.16.2[.zip]` */
	static private String makeGstName() {
		String os = Platform.isWindows() ? GST_WIN : null;
		if (os == null)
			return null;
		String arch = Platform.is64Bit() ? GST_ARCH_64 : GST_ARCH_32;
		String version = "@@GSTREAMER.VERSION@@";
		return String.join("-", GST_BASE, os, arch, version);
	}

	/** Start downloading GStreamer zip file */
	static private void startGstDownload(File gstPath) {
		String host = getJnlpHost();
		if (host == null)
			return;
		String urlStr = "http://" + host + GST_SERVER_DIR +
			gstPath.getName() + ".zip";
		try {
			URL url = new URL(urlStr);
			ZipDownloader zd = new ZipDownloader(
				"camera.gstreamer.downloading.title",
				"camera.gstreamer.downloading.msg", url,
				gstPath);
			// action to run when the download is complete
			zd.execute(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					checkGstInstallPrivate(gstPath);
					showConfirmDialog();
				}
			});
			JInternalFrame f = Session.getCurrent().getDesktop()
				.show(zd);
			zd.setFrame(f);
		}
		catch (MalformedURLException e) {
			System.out.println("Could not download GStreamer " +
				"from URL: '" + urlStr + "'");
		}
	}

	/** Get Java Network Launcher Protocol (WebStart) host */
	static private String getJnlpHost() {
		try {
			// Use reflection to allow client to run
			// without using Java WebStart
			Class<?> service_manager_class = Class.forName(
				"javax.jnlp.ServiceManager");
			Class<?> basic_service_class = Class.forName(
				"javax.jnlp.BasicService");
			Method lookup = service_manager_class.getDeclaredMethod(
				"lookup", String.class);
			Object basic_service = lookup.invoke(null,
				"javax.jnlp.BasicService");
			Method get_code_base = basic_service_class
				.getDeclaredMethod("getCodeBase");
			Object code_base = get_code_base.invoke(basic_service);
			if (code_base instanceof URL) {
				URL url = (URL) code_base;
				return url.getHost();
			}
			System.out.println("Failed to lookup WebStart host");
		}
		catch (ClassNotFoundException e) {
			System.out.println("NOT Running in WebStart");
		}
		catch (Exception e) {
			System.out.println("getServerAddress: " +
				e.getMessage());
		}
		return null;
	}

	/** Check if private GStreamer library is installed (Windows) */
	static private void checkGstInstallPrivate(File gstPath) {
		String binPath = new File(gstPath, GST_BIN_DIR).toString();
		try {
			Kernel32 k32 = Kernel32.INSTANCE;
			String path = System.getenv("path");
			System.out.println("path: " + path);
			if (path == null || path.trim().isEmpty()) {
				k32.SetEnvironmentVariable("path", binPath);
			} else {
				k32.SetEnvironmentVariable("path", binPath +
					File.pathSeparator + path);
			}
			GST_INSTALLED = initGst();
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static boolean gstVersionPrinted = false;
	
	/** Try to initialize GStreamer */
	static private boolean initGst() {
		try {
			Gst.init("StreamMgrGst");
			if (!gstVersionPrinted) {
				System.out.println(Gst.getVersionString()
						+ " installed");
				gstVersionPrinted = true;
			}
			return true;
		} catch (GstException | UnsatisfiedLinkError ex) {
			System.out.println("GStreamer not available: "
				+ ex.getMessage());
			return false;
		}
	}

	/** Show dialog confirming success or failure of download */
	static private void showConfirmDialog() {
		String title = GST_INSTALLED
			? I18N.get("camera.gstreamer.downloading.success")
			: I18N.get("camera.gstreamer.downloading.failed");
		String msg = GST_INSTALLED
			? I18N.get("camera.gstreamer.downloading.success.msg")
			: I18N.get("camera.gstreamer.downloading.failed.msg");
		JOptionPane.showConfirmDialog(Session.getCurrent().getDesktop(),
			msg, title, JOptionPane.DEFAULT_OPTION,
			JOptionPane.PLAIN_MESSAGE);
	}

	/** Generate a DOT filename with the current date and time. */
	static private String getDOTFileName() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
			"yyyyMMdd-HHmmss_SSS");
		return String.format("pipe_dbg_%s", dtf.format(
			LocalDateTime.now()));
	}

	Element srcElem;
	VidComponentGst gstComponent;

	/**
	 * Create a GStreamer stream manager.
	 * @param vp The VideoPanel to use.
	 * @param vr The StreamReq to use.
	 */
	protected VidStreamMgrGst(VidPanel vp, VidStreamReq vr) {
		super(vp, vr);
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
			if (appsink != null) {
				appsink.disconnect((AppSink.NEW_SAMPLE) appsinkListener);
				appsink.disconnect((AppSink.EOS)        appsinkListener);
			}
			if (bus != null) {
				bus.disconnect((Bus.ERROR)  busListener);
				bus.disconnect((Bus.WARNING)busListener);
				bus.disconnect((Bus.INFO)   busListener);
			}
			gstComponent.disconnectAll();
			gstComponent = null;
			try {
				p.setState(State.NULL);
				p.getState();
			}
			catch (java.lang.IllegalStateException e) {
				e.printStackTrace();
			}
		}
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
			System.out.println("###-- "+name+" = "+elem.get(name));
		}
	}
}
