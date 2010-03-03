/*
* Copyright (C) 2003-2010  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a wrapper around an input stream.  It can retrieve the
 * next image in the stream.
 * @author    Timothy Johnson
 */
public class MJPEGReader implements VideoStream {

	private final InputStream stream;

	public MJPEGReader(InputStream is) {
		stream = is;
	}

	private String readLine() throws IOException{
		StringBuffer buf = new StringBuffer();
		int ch;
		for(;;) {
			ch = stream.read();
			if(ch < 0) {
				if(buf.length() == 0) {
					return null;
				} else {
					break;
				}
			}
			buf.append((char)ch);
			if (ch == '\r') {
				continue;
			} else if (ch == '\n') {
				break;
			}
        }
		return (buf.toString());
	}

	/** Get the next image in the mjpeg stream
	 *
	 * @return
	 */
	public byte[] getImage(){
		try{
			int imageSize = getImageSize();
			byte[] image = new byte[imageSize];
			int bytesRead = 0;
			int currentRead = 0;
			while(bytesRead < imageSize){
				currentRead = stream.read(image, bytesRead,
						imageSize - bytesRead);
				if(currentRead==-1){
					break;
				}else{
					bytesRead = bytesRead + currentRead;
				}
			}
			return image;
		}catch(Exception e){
			return new byte[0];
		}
	}

	private int getImageSize() throws IOException{
		String s = readLine();
		while(s!=null){
			if(s.toLowerCase().indexOf("content-length")>-1){
				s = s.substring(s.indexOf(":") + 1);
				s = s.trim();
				//throw away an empty line after the
				//content-length header
				readLine();
				return Integer.parseInt(s);
			}
			s = readLine();
		}
		return 0;
	}
}
