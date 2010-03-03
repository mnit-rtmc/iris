/*
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera.stream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/** The ConnectionFactory is a convenience class for setting 
 * up URLConnections with the appropriate parameters including
 * connection timeout.
 * 
 * @author Timothy A. Johnson
 *
 */
abstract public class ConnectionFactory {

	public static HttpURLConnection createConnection(URL url)
			throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		HttpURLConnection.setFollowRedirects(true);
		c.setConnectTimeout(VideoThread.TIMEOUT_DIRECT);
		c.setReadTimeout(VideoThread.TIMEOUT_DIRECT);
		return c;
	}
	
	/** Read data from the URL into a file.
	 * 
	 * @param url The URL of the source
	 * @param f The file in which to save the data.
	 * @throws IOException
	 */
	public static void readData(URL url, File f)
			throws IOException{
		FileOutputStream out = new FileOutputStream(f);
		try{
			URLConnection c = createConnection(url);
			InputStream in = c.getInputStream();
			byte[] data = new byte[1024];
			int bytesRead = 0;
			while(true){
				bytesRead = in.read(data);
				if(bytesRead==-1) break;
				out.write(data, 0, bytesRead);
			}
		}finally{
			try{
				out.flush();
				out.close();
			}catch(Exception e){
			}
		}
	}

	/**
	 * Get an image from the given url
	 * @param url The location of the image file
	 * @return A byte[] containing the image data.
	 * @throws IOException
	 */
	public static byte[] getImage(URL url)
			throws IOException{
		InputStream in = null;
		try{
			URLConnection c = createConnection(url);
			in = c.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int bytesRead = 0;
			while(true){
				bytesRead = in.read(data);
				if(bytesRead==-1) break;
				bos.write(data, 0, bytesRead);
			}
			return bos.toByteArray();
		}finally{
			try{
				in.close();
			}catch(IOException ioe2){
			}catch(NullPointerException npe){
			}
		}
	}
}
