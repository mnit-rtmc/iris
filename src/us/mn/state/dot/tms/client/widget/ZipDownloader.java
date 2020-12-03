/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020 SRF Consulting Group
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.UserProperty;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Class and GUI for downloading and decompressing of a zip file, for instance
 * one containing a native library, and showing progress to the user. The file
 * will be downloaded and decompressed into a directory with the same name as
 * the file (without the .zip extension) placed in the user's $HOME/iris
 * directory (next to the user.properties file), after which the zip file will
 * be deleted. This is view only, the user has no ability to control what is
 * happening.
 *
 * This class also contains the code used to perform the actual download.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class ZipDownloader extends AbstractForm {

	/** Frame containing this form */
	private JInternalFrame frame;

	/** Message displayed to the user */
	private ILabel msgLbl;

	/** Progress bar for tracking download */
	private JProgressBar progressBar;

	/** URL to the file being downloaded */
	private URL url;

	/** Directory into which the contents of the zip will be placed */
	private File destDir;

	/** Size of the file to be downloaded */
	private int fileSize;

	/** Current number of bytes read from the server */
	private int bytesRead;

	/** Construct a ZipDownloader to download from the specified URL and
	 *  unzip into directory d, showing progress in a window with the given
	 *  I18N'd title and message.
	 */
	public ZipDownloader(String tId, String mId, URL u, File d) {
		super(I18N.get(tId));
		url = u;
		destDir = d;
		msgLbl = new ILabel(mId);
		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(200, 20));
	}

	/** Initialize the form */
	@Override
	protected void initialize() {
		// initialize layout
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		/* Message for the user */
		add(msgLbl);

		/* Progress bar */
		add(progressBar);
	}

	/** Update the progress bar with the current status of the download. */
	private void updateProgressBar(int b) {
		// set the value to the bytes read - 50% means we have downloaded the
		// entire file, and 100% means we've unzipped it
		bytesRead = b;
		if (bytesRead <= progressBar.getMaximum())
			progressBar.setValue(bytesRead);
	}

	public void setFrame(JInternalFrame f) {
		frame = f;

		// disable closing the frame - only let it happen when it's done
		frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
	}

	/** Start the background process. If an Action is provided, the action
	 *  will be performed (from the event dispatch thread) at the end of the
	 *  background process.
	 */
	public void execute(Action cb) {
		// set the callback on the worker, then call execute
		dlWorker = new DownloadWorker(cb);
		dlWorker.execute();
	}

	/** Worker for downloading and unzipping the file. */
	private DownloadWorker dlWorker;

	private class DownloadWorker extends SwingWorker<File, Integer> {

		/** Action to be executed (in EDT) when we're done */
		private Action cbAction;

		/** Construct a new DownloadWorker with a callback action. */
		public DownloadWorker(Action cb) {
			cbAction = cb;
		}

		/** Process updates from the background thread */
		@Override
		protected void process(List<Integer> byteCounts) {
			// check that our progress bar has the correct maximum (2*fileSize
			// to give some time to unzip)
			if (fileSize != -1 && progressBar.getMaximum() != 2*fileSize)
				progressBar.setMaximum(2*fileSize);

			// update the progress bar with whatever chunks we got
			for (Integer b: byteCounts) {
				updateProgressBar(b);
			}
		}

		/** Finish up. Runs the callback action (if provided) and closes the
		 *  GUI containing the progress tracker.
		 */
		@Override
		protected void done() {
			if (cbAction != null)
				cbAction.actionPerformed(null);
			close(Session.getCurrent().getDesktop());
		}

		/** Bytes "read" - used when downloading and unzipping */
		private int totalBytesRead = 0;

		@Override
		protected File doInBackground() throws Exception {
			// first check the size of the file for progress tracking
			System.out.println("Checking download size...");
			fileSize = getFileSize(url);

			// if this didn't work, we're done
			if (fileSize == -1) {
				System.out.println("Download size check failed!");
				return null;
			}

			System.out.println("Download size: " + Integer.toString(fileSize));

			// get the $HOME/iris directory and make sure it exists
			File uIris = UserProperty.getDir();
			if (!uIris.canWrite())
				uIris.mkdirs();

			// setup the ReadableByteChannel to handle the download
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());

			// create the filename and output stream for writing
			String zipName = Paths.get(url.getPath()).getFileName().toString();
			String zipPath = Paths.get(
					uIris.getCanonicalPath(), zipName).toString();
			FileOutputStream fos = new FileOutputStream(zipPath);

			// download the file
			System.out.println("Downloading " + Integer.toString(fileSize)
						+ " bytes from " + url.toString() + " to " + zipPath);
			ReadableProgressByteChannel rpbc =
					new ReadableProgressByteChannel(rbc);
			long r = fos.getChannel().transferFrom(rpbc, 0, Long.MAX_VALUE);

			// close the streams
			rpbc.close();
			fos.close();

			// stop if the file couldn't be downloaded
			if (r == 0)
				return null;

			// once the file is done, create a directory for the contents and
			// unzip
			if (!destDir.canWrite())
				destDir.mkdirs();
			unzipFile(zipPath, destDir);

			// whether or not the file was unzipped successfully, delete the
			// zip file
			System.out.println("Deleting zip file: " + zipPath);
			File zipFile = new File(Paths.get(zipPath).toString());
			boolean d = zipFile.delete();

			// return the directory containing the files we just unzipped
			return destDir;
		}

		/** Unzip the file at zipName into the directory oDir. */
		private void unzipFile(String zipName, File destDir) {
			try {
				System.out.println("Unzipping file " + zipName + " into "
						+ "directory " + destDir.getCanonicalPath().toString());
				InputStream is = Files.newInputStream(Paths.get(zipName));
				ZipInputStream zis = new ZipInputStream(is);
				ZipEntry e = zis.getNextEntry();
				while (e != null) {
					Path p = newFile(destDir, e);

					// check if the entry is a file or directory
					if (!e.isDirectory()) {
						// if it's a file, write out the file
						Files.createDirectories(p.getParent());
						Files.copy(zis, p);
					} else {
						// if it's a directory, create it
						Files.createDirectories(p);
					}

					// update the progress tracker using the compressed size
					// of each entry
					totalBytesRead += e.getCompressedSize();
					publish(totalBytesRead);
					e = zis.getNextEntry();
				}
				zis.closeEntry();
				zis.close();
				is.close();
				System.out.println("Unzipping complete!");
			} catch (IOException ex) {
				// stop if we hit an exception - it might be innocuous, or
				// maybe not...
				ex.printStackTrace();
			}
		}

		/** Create a new file in destDir for unzipping the ZipEntry e. This
		 *  guards against "Zip Slipping" (see here:
		 *  https://snyk.io/research/zip-slip-vulnerability).
		 * @throws IOException
		 */
		private Path newFile(File destDir, ZipEntry e) throws IOException {
			Path destPath = Paths.get(
					destDir.getCanonicalPath(), e.getName());
//			File destFile = new File(destDir, e.getName());
			String destDirPath = destDir.getCanonicalPath();
			String newFilePath = destPath.toString();
			if (!newFilePath.startsWith(destDirPath + File.separator)) {
				throw new IOException("Entry '" + e.getName()
										+ "' is outside target directory");
			}
			return destPath;
		}

		/** A wrapper class implementing a ReadableByteChannel for providing
		 *  progress updates.
		 */
		class ReadableProgressByteChannel implements ReadableByteChannel {

			private final ReadableByteChannel rbc;

			public ReadableProgressByteChannel(ReadableByteChannel rbc) {
				this.rbc = rbc;
			}

			@Override
			public void close() throws IOException { rbc.close(); }

			@Override
			public boolean isOpen() { return rbc.isOpen(); }

			/** Read data from remote and update progress */
			@Override
			public int read(ByteBuffer dst) throws IOException {
				int nRead = rbc.read(dst);
				totalBytesRead += nRead;
				publish(totalBytesRead);
				return nRead;
			}
		}
	};

	/** Query the size of the file to be downloaded from the server using a
	 *  GET request.
	 */
	private static int getFileSize(URL url) {
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			if(conn instanceof HttpURLConnection) {
				((HttpURLConnection)conn).setRequestMethod("GET");
			}
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			if(conn instanceof HttpURLConnection)
				((HttpURLConnection)conn).disconnect();
		}
	}
}
